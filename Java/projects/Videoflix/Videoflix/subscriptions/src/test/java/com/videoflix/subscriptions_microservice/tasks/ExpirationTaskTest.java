package com.videoflix.subscriptions_microservice.tasks;

import com.videoflix.subscriptions_microservice.entities.Subscription;
import com.videoflix.subscriptions_microservice.entities.User; // Importation de l'entité User pour les tests
import com.videoflix.subscriptions_microservice.repositories.SubscriptionRepository;
import com.videoflix.subscriptions_microservice.services.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

// @ExtendWith(MockitoExtension.class) permet d'initialiser les mocks automatiquement.
@ExtendWith(MockitoExtension.class)
@DisplayName("Tests pour ExpirationTask")
class ExpirationTaskTest {

    // @Mock crée une instance mockée de SubscriptionRepository.
    // Simulation les comportements de recherche et de sauvegarde du repository.
    @Mock
    private SubscriptionRepository subscriptionRepository;

    // @Mock crée une instance mockée de NotificationService.
    // Nous allons vérifier que les notifications sont envoyées lorsque c'est
    // nécessaire.
    @Mock
    private NotificationService notificationService;

    // @InjectMocks crée une instance de ExpirationTask et y injecte les mocks.
    @InjectMocks
    private ExpirationTask expirationTask;

    // Utilisé pour simuler la date et l'heure actuelle, crucial pour les méthodes
    // `findByEndDate` et `findByEndDateBeforeAndStatusNot`.
    // On doit la définir de manière contrôlée pour que les tests soient
    // déterministes.
    // Pour des tests plus robustes sur `LocalDateTime.now()`, on utiliserait une
    // solution comme `java.time.Clock`.
    // Pour cet exemple, nous allons mocker les appels au repository qui utilisent
    // `LocalDateTime.now()`.
    private LocalDateTime fixedNow;

    @BeforeEach
    void setUp() {
        // Définit une date et heure fixe pour "aujourd'hui" afin de rendre les tests
        // déterministes.
        // Cela simule ce que `LocalDateTime.now()` renverrait au moment de l'exécution
        // de la tâche.
        fixedNow = LocalDateTime.of(2025, 5, 24, 3, 0, 0); // Date/heure à 3h du matin le 24 mai 2025

        // Initialise les mocks.
        // Pas besoin de reset car @ExtendWith(MockitoExtension.class) s'en charge pour
        // chaque test.

        // Simule le comportement du repository pour les abonnements expirant
        // aujourd'hui
        // (ceux dont la date de fin est 'fixedNow').
        when(subscriptionRepository.findByEndDate(fixedNow.toLocalDate().atStartOfDay()))
                .thenReturn(Collections.emptyList()); // Comportement par défaut, surchargé dans les tests spécifiques.

        // Simule le comportement du repository pour les abonnements déjà expirés.
        when(subscriptionRepository.findByEndDateBeforeAndStatusNot(
                fixedNow.toLocalDate().atStartOfDay(), Subscription.SubscriptionStatus.EXPIRED))
                .thenReturn(Collections.emptyList()); // Comportement par défaut, surchargé dans les tests spécifiques.

        // Puisque `processExpiredSubscriptions` utilise `LocalDateTime.now()`
        // directement, nous devons stubber
        // les appels du repository qui reçoivent cette valeur. Il est préférable de
        // mocker `LocalDateTime.now()`
        // directement si possible (avec une librairie comme `java.time.Clock` ou
        // `PowerMockito` pour `static` mocking),
        // mais pour une solution simple, nous nous assurons que le repository est
        // stubbé correctement.
    }

    // --- Tests pour processExpiredSubscriptions ---

    @Test
    @DisplayName("Devrait traiter les abonnements expirant aujourd'hui et envoyer des notifications")
    void processExpiredSubscriptions_shouldProcessExpiringToday() {
        // GIVEN: Deux abonnements qui expirent "aujourd'hui".
        User user1 = new User();
        user1.setId(1L);
        User user2 = new User();
        user2.setId(2L);

        Subscription sub1 = new Subscription();
        sub1.setId(101L);
        sub1.setEndDate(fixedNow.toLocalDate().atStartOfDay());
        sub1.setUser(user1);

        Subscription sub2 = new Subscription();
        sub2.setId(102L);
        sub2.setEndDate(fixedNow.toLocalDate().atStartOfDay());
        sub2.setUser(user2);

        List<Subscription> expiringTodayList = Arrays.asList(sub1, sub2);

        // Configure le repository pour retourner ces abonnements lors de l'appel pour
        // "expiring today".
        when(subscriptionRepository.findByEndDate(fixedNow.toLocalDate().atStartOfDay()))
                .thenReturn(expiringTodayList);
        // Assurez-vous que l'autre appel retourne vide pour ce test.
        when(subscriptionRepository.findByEndDateBeforeAndStatusNot(any(LocalDateTime.class), any()))
                .thenReturn(Collections.emptyList());

        // WHEN: La tâche est exécutée.
        expirationTask.processExpiredSubscriptions();

        // THEN:
        // Vérifie que `findByEndDate` a été appelé avec la date correcte d'aujourd'hui.
        verify(subscriptionRepository, times(1)).findByEndDate(fixedNow.toLocalDate().atStartOfDay());
        // Vérifie que `findByEndDateBeforeAndStatusNot` a été appelé avec la date
        // correcte et le statut.
        verify(subscriptionRepository, times(1)).findByEndDateBeforeAndStatusNot(fixedNow.toLocalDate().atStartOfDay(),
                Subscription.SubscriptionStatus.EXPIRED);

        // Vérifie que les notifications d'expiration imminente ont été envoyées pour
        // chaque abonnement.
        verify(notificationService, times(1)).sendSubscriptionExpiringNotification(user1, sub1);
        verify(notificationService, times(1)).sendSubscriptionExpiringNotification(user2, sub2);
        // Vérifie que `sendSubscriptionExpiringNotification` a été appelé un total de 2
        // fois.
        verify(notificationService, times(2)).sendSubscriptionExpiringNotification(any(User.class),
                any(Subscription.class));

        // Vérifie qu'aucune méthode liée aux abonnements "déjà expirés" n'a été
        // appelée.
        verify(subscriptionRepository, never()).save(any(Subscription.class));
        verify(notificationService, never()).sendSubscriptionExpiredNotification(any(User.class),
                any(Subscription.class));
    }

    @Test
    @DisplayName("Devrait traiter les abonnements déjà expirés, mettre à jour le statut et envoyer des notifications")
    void processExpiredSubscriptions_shouldProcessAlreadyExpired() {
        // GIVEN: Un abonnement déjà expiré mais dont le statut n'est pas "EXPIRED".
        User user = new User();
        user.setId(3L);

        Subscription sub = new Subscription();
        sub.setId(201L);
        sub.setEndDate(fixedNow.toLocalDate().minusDays(5).atStartOfDay()); // Expiré il y a 5 jours
        sub.setStatus(Subscription.SubscriptionStatus.ACTIVE); // Statut incorrect
        sub.setUser(user);

        List<Subscription> alreadyExpiredList = Collections.singletonList(sub);

        // Configure le repository pour retourner cet abonnement pour les "déjà
        // expirés".
        when(subscriptionRepository.findByEndDateBeforeAndStatusNot(fixedNow.toLocalDate().atStartOfDay(),
                Subscription.SubscriptionStatus.EXPIRED))
                .thenReturn(alreadyExpiredList);
        // Assurez-vous que l'autre appel retourne vide pour ce test.
        when(subscriptionRepository.findByEndDate(any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());

        // Configure le repository pour simuler la sauvegarde de l'abonnement mis à
        // jour.
        when(subscriptionRepository.save(any(Subscription.class))).thenReturn(sub);

        // WHEN: La tâche est exécutée.
        expirationTask.processExpiredSubscriptions();

        // THEN:
        // Vérifie que `findByEndDate` a été appelé.
        verify(subscriptionRepository, times(1)).findByEndDate(fixedNow.toLocalDate().atStartOfDay());
        // Vérifie que `findByEndDateBeforeAndStatusNot` a été appelé avec la date
        // correcte et le statut.
        verify(subscriptionRepository, times(1)).findByEndDateBeforeAndStatusNot(fixedNow.toLocalDate().atStartOfDay(),
                Subscription.SubscriptionStatus.EXPIRED);

        // Vérifie que le statut de l'abonnement a été mis à jour à EXPIRED.
        assertEquals(Subscription.SubscriptionStatus.EXPIRED, sub.getStatus(),
                "Le statut de l'abonnement devrait être mis à jour à EXPIRED.");
        // Vérifie que l'abonnement mis à jour a été sauvegardé.
        verify(subscriptionRepository, times(1)).save(sub);

        // Vérifie que la notification d'abonnement expiré a été envoyée.
        verify(notificationService, times(1)).sendSubscriptionExpiredNotification(user, sub);

        // Vérifie qu'aucune méthode liée aux abonnements "expirant aujourd'hui" n'a été
        // appelée.
        verify(notificationService, never()).sendSubscriptionExpiringNotification(any(User.class),
                any(Subscription.class));
    }

    @Test
    @DisplayName("Ne devrait rien faire si aucun abonnement n'est trouvé pour les deux catégories")
    void processExpiredSubscriptions_shouldDoNothingIfNoSubscriptionsFound() {
        // GIVEN: Le repository est configuré pour ne retourner aucun abonnement pour
        // les deux requêtes.
        when(subscriptionRepository.findByEndDate(any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());
        when(subscriptionRepository.findByEndDateBeforeAndStatusNot(any(LocalDateTime.class), any()))
                .thenReturn(Collections.emptyList());

        // WHEN: La tâche est exécutée.
        expirationTask.processExpiredSubscriptions();

        // THEN:
        // Vérifie que les méthodes de recherche du repository ont été appelées.
        verify(subscriptionRepository, times(1)).findByEndDate(fixedNow.toLocalDate().atStartOfDay());
        verify(subscriptionRepository, times(1)).findByEndDateBeforeAndStatusNot(fixedNow.toLocalDate().atStartOfDay(),
                Subscription.SubscriptionStatus.EXPIRED);

        // Vérifie qu'aucune méthode de notification ou de sauvegarde n'a été appelée.
        verify(notificationService, never()).sendSubscriptionExpiringNotification(any(), any());
        verify(notificationService, never()).sendSubscriptionExpiredNotification(any(), any());
        verify(subscriptionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Devrait gérer la combinaison d'abonnements expirant et déjà expirés")
    void processExpiredSubscriptions_shouldHandleMixedScenarios() {
        // GIVEN: Un mélange d'abonnements.
        User user1 = new User();
        user1.setId(1L);
        User user2 = new User();
        user2.setId(2L);
        User user3 = new User();
        user3.setId(3L);

        // Abonnement expirant aujourd'hui
        Subscription subExpiring = new Subscription();
        subExpiring.setId(301L);
        subExpiring.setEndDate(fixedNow.toLocalDate().atStartOfDay());
        subExpiring.setUser(user1);

        // Abonnement déjà expiré
        Subscription subAlreadyExpired = new Subscription();
        subAlreadyExpired.setId(302L);
        subAlreadyExpired.setEndDate(fixedNow.toLocalDate().atStartOfDay().minusDays(10));
        subAlreadyExpired.setStatus(Subscription.SubscriptionStatus.ACTIVE); // Statut incorrect
        subAlreadyExpired.setUser(user2);

        // Un autre abonnement qui expire aujourd'hui (pour le compte de l'utilisateur
        // 3)
        Subscription subExpiring2 = new Subscription();
        subExpiring2.setId(303L);
        subExpiring2.setEndDate(fixedNow.toLocalDate().atStartOfDay());
        subExpiring2.setUser(user3);

        List<Subscription> expiringTodayList = Arrays.asList(subExpiring, subExpiring2);
        List<Subscription> alreadyExpiredList = Collections.singletonList(subAlreadyExpired);

        // Configure le repository
        when(subscriptionRepository.findByEndDate(fixedNow.toLocalDate().atStartOfDay()))
                .thenReturn(expiringTodayList);
        when(subscriptionRepository.findByEndDateBeforeAndStatusNot(fixedNow.toLocalDate().atStartOfDay(),
                Subscription.SubscriptionStatus.EXPIRED))
                .thenReturn(alreadyExpiredList);
        when(subscriptionRepository.save(any(Subscription.class))).thenReturn(subAlreadyExpired); // Simule la
                                                                                                  // sauvegarde

        // WHEN: La tâche est exécutée.
        expirationTask.processExpiredSubscriptions();

        // THEN:
        // Vérifie les appels aux méthodes de recherche du repository.
        verify(subscriptionRepository, times(1)).findByEndDate(fixedNow.toLocalDate().atStartOfDay());
        verify(subscriptionRepository, times(1)).findByEndDateBeforeAndStatusNot(fixedNow.toLocalDate().atStartOfDay(),
                Subscription.SubscriptionStatus.EXPIRED);

        // Vérifie les notifications pour les abonnements expirant aujourd'hui.
        verify(notificationService, times(1)).sendSubscriptionExpiringNotification(user1, subExpiring);
        verify(notificationService, times(1)).sendSubscriptionExpiringNotification(user3, subExpiring2);
        verify(notificationService, times(2)).sendSubscriptionExpiringNotification(any(User.class),
                any(Subscription.class));

        // Vérifie la mise à jour du statut et la sauvegarde pour l'abonnement déjà
        // expiré.
        assertEquals(Subscription.SubscriptionStatus.EXPIRED, subAlreadyExpired.getStatus(),
                "Le statut de l'abonnement déjà expiré devrait être mis à jour.");
        verify(subscriptionRepository, times(1)).save(subAlreadyExpired);

        // Vérifie la notification pour l'abonnement déjà expiré.
        verify(notificationService, times(1)).sendSubscriptionExpiredNotification(user2, subAlreadyExpired);
    }
}