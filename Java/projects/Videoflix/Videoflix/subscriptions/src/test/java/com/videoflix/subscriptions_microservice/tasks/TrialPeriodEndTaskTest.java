package com.videoflix.subscriptions_microservice.tasks;

import com.videoflix.subscriptions_microservice.entities.Subscription;
import com.videoflix.subscriptions_microservice.entities.User;
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
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

// @ExtendWith(MockitoExtension.class) permet d'initialiser les mocks automatiquement.
@ExtendWith(MockitoExtension.class)
@DisplayName("Tests pour TrialPeriodEndTask")
class TrialPeriodEndTaskTest {

    // @Mock pour le repository des abonnements, nous allons simuler ses comportements.
    @Mock
    private SubscriptionRepository subscriptionRepository;

    // @Mock pour le service de notification, nous allons vérifier les appels à ses méthodes.
    @Mock
    private NotificationService notificationService;

    // @InjectMocks injecte les mocks dans l'instance de la tâche à tester.
    @InjectMocks
    private TrialPeriodEndTask trialPeriodEndTask;

    // Horodatage fixe pour simuler `LocalDateTime.now()` et rendre les tests déterministes.
    // La tâche est planifiée à 4h00 du matin.
    private LocalDateTime fixedNow;

    /**
     * Méthode exécutée avant chaque test.
     * Initialise `fixedNow` et réinitialise les mocks.
     */
    @BeforeEach
    void setUp() {
        // Définit une date et heure fixe pour "aujourd'hui" (le 24 mai 2025 à 4h00 du matin)
        // cela correspond à l'heure d'exécution planifiée de la tâche.
        fixedNow = LocalDateTime.of(2025, 5, 24, 4, 0, 0);

        // Réinitialise les mocks pour garantir l'indépendance des tests.
        reset(subscriptionRepository, notificationService);
    }

    @Test
    @DisplayName("Devrait traiter les abonnements dont la période d'essai se termine aujourd'hui")
    void processTrialPeriodEnd_shouldProcessTrialsEndingToday() {
        // GIVEN: Un abonnement dont la période d'essai se termine "aujourd'hui".
        User user1 = new User();
        user1.setId(1L);
        user1.setEmail("user1@example.com");

        Subscription subEndingToday = new Subscription();
        subEndingToday.setId(101L);
        subEndingToday.setTrialEndDate(fixedNow); // Date de fin d'essai est 'aujourd'hui'
        subEndingToday.setStatus(Subscription.SubscriptionStatus.TRIAL); // Statut TRIAL
        subEndingToday.setUser(user1);

        // Configure le repository pour retourner cet abonnement pour `findByTrialEndDate`.
        when(subscriptionRepository.findByTrialEndDate(fixedNow))
                .thenReturn(Collections.singletonList(subEndingToday));

        // Configure le repository pour ne retourner aucun abonnement déjà terminé.
        when(subscriptionRepository.findByTrialEndDateBeforeAndStatus(
                any(LocalDateTime.class), Subscription.SubscriptionStatus.TRIAL))
                .thenReturn(Collections.emptyList());

        // WHEN: La tâche est exécutée.
        trialPeriodEndTask.processTrialPeriodEnd();

        // THEN:
        // Vérifie que `findByTrialEndDate` a été appelé une fois avec la date correcte.
        verify(subscriptionRepository, times(1)).findByTrialEndDate(fixedNow);
        // Vérifie que `findByTrialEndDateBeforeAndStatus` a été appelé une fois.
        verify(subscriptionRepository, times(1)).findByTrialEndDateBeforeAndStatus(
                any(LocalDateTime.class), Subscription.SubscriptionStatus.TRIAL);

        // Vérifie que la notification "finissant aujourd'hui" a été envoyée une fois.
        verify(notificationService, times(1))
                .sendTrialPeriodEndingNotification(user1, subEndingToday);

        // Vérifie qu'aucune notification "déjà terminée" n'a été envoyée.
        verify(notificationService, never()).sendTrialPeriodEndedNotification(any(), any());
        // Vérifie qu'aucun abonnement n'a été sauvegardé car le statut n'a pas changé pour ceux finissant aujourd'hui.
        verify(subscriptionRepository, never()).save(any(Subscription.class));
    }

    @Test
    @DisplayName("Devrait traiter les abonnements dont la période d'essai est déjà terminée et mettre à jour le statut")
    void processTrialPeriodEnd_shouldProcessTrialsAlreadyEnded() {
        // GIVEN: Un abonnement dont la période d'essai est passée et le statut est toujours TRIAL.
        User user2 = new User();
        user2.setId(2L);
        user2.setEmail("user2@example.com");

        Subscription subAlreadyEnded = new Subscription();
        subAlreadyEnded.setId(201L);
        subAlreadyEnded.setTrialEndDate(fixedNow.minusDays(5)); // Date de fin d'essai passée
        subAlreadyEnded.setStatus(Subscription.SubscriptionStatus.TRIAL); // Statut toujours TRIAL
        subAlreadyEnded.setUser(user2);

        // Configure le repository pour ne retourner aucun abonnement finissant aujourd'hui.
        when(subscriptionRepository.findByTrialEndDate(fixedNow))
                .thenReturn(Collections.emptyList());

        // Configure le repository pour retourner cet abonnement déjà terminé.
        when(subscriptionRepository.findByTrialEndDateBeforeAndStatus(
                fixedNow, Subscription.SubscriptionStatus.TRIAL))
                .thenReturn(Collections.singletonList(subAlreadyEnded));

        // Configure le save pour simuler la persistance.
        when(subscriptionRepository.save(any(Subscription.class))).thenReturn(subAlreadyEnded);


        // WHEN: La tâche est exécutée.
        trialPeriodEndTask.processTrialPeriodEnd();

        // THEN:
        // Vérifie les appels aux méthodes du repository comme attendu.
        verify(subscriptionRepository, times(1)).findByTrialEndDate(fixedNow);
        verify(subscriptionRepository, times(1)).findByTrialEndDateBeforeAndStatus(
                any(LocalDateTime.class), Subscription.SubscriptionStatus.TRIAL);

        // Vérifie que le statut de l'abonnement a été mis à jour et sauvegardé.
        verify(subscriptionRepository, times(1)).save(argThat(sub ->
                sub.getId().equals(201L) && sub.getStatus() == Subscription.SubscriptionStatus.TRIAL_ENDED
        ));

        // Vérifie que la notification "déjà terminée" a été envoyée.
        verify(notificationService, times(1))
                .sendTrialPeriodEndedNotification(user2, subAlreadyEnded);
        // Vérifie qu'aucune notification "finissant aujourd'hui" n'a été envoyée.
        verify(notificationService, never()).sendTrialPeriodEndingNotification(any(), any());
    }

    @Test
    @DisplayName("Devrait traiter les deux types d'abonnements simultanément")
    void processTrialPeriodEnd_shouldProcessBothTypesOfTrials() {
        // GIVEN: Un abonnement finissant aujourd'hui et un abonnement déjà terminé.
        User user1 = new User(); user1.setId(1L); user1.setEmail("user1@example.com");
        User user2 = new User(); user2.setId(2L); user2.setEmail("user2@example.com");

        Subscription subEndingToday = new Subscription();
        subEndingToday.setId(101L);
        subEndingToday.setTrialEndDate(fixedNow);
        subEndingToday.setStatus(Subscription.SubscriptionStatus.TRIAL);
        subEndingToday.setUser(user1);

        Subscription subAlreadyEnded = new Subscription();
        subAlreadyEnded.setId(201L);
        subAlreadyEnded.setTrialEndDate(fixedNow.minusDays(5));
        subAlreadyEnded.setStatus(Subscription.SubscriptionStatus.TRIAL);
        subAlreadyEnded.setUser(user2);

        // Configure le repository pour retourner les deux types d'abonnements.
        when(subscriptionRepository.findByTrialEndDate(fixedNow))
                .thenReturn(Collections.singletonList(subEndingToday));
        when(subscriptionRepository.findByTrialEndDateBeforeAndStatus(
                any(LocalDateTime.class), Subscription.SubscriptionStatus.TRIAL))
                .thenReturn(Collections.singletonList(subAlreadyEnded));

        // Configure le save.
        when(subscriptionRepository.save(any(Subscription.class))).thenReturn(subAlreadyEnded);

        // WHEN: La tâche est exécutée.
        trialPeriodEndTask.processTrialPeriodEnd();

        // THEN:
        // Vérifie les appels du repository.
        verify(subscriptionRepository, times(1)).findByTrialEndDate(fixedNow);
        verify(subscriptionRepository, times(1)).findByTrialEndDateBeforeAndStatus(
                any(LocalDateTime.class), Subscription.SubscriptionStatus.TRIAL);

        // Vérifie les appels de notification pour chaque type.
        verify(notificationService, times(1))
                .sendTrialPeriodEndingNotification(user1, subEndingToday);
        verify(notificationService, times(1))
                .sendTrialPeriodEndedNotification(user2, subAlreadyEnded);

        // Vérifie que le statut a été mis à jour et sauvegardé pour l'abonnement déjà terminé.
        verify(subscriptionRepository, times(1)).save(argThat(sub ->
                sub.getId().equals(201L) && sub.getStatus() == Subscription.SubscriptionStatus.TRIAL_ENDED
        ));
    }

    @Test
    @DisplayName("Ne devrait rien faire si aucun abonnement n'est éligible au traitement")
    void processTrialPeriodEnd_shouldDoNothingIfNoEligibleSubscriptions() {
        // GIVEN: Aucune abonnement pour les deux catégories.
        when(subscriptionRepository.findByTrialEndDate(fixedNow))
                .thenReturn(Collections.emptyList());
        when(subscriptionRepository.findByTrialEndDateBeforeAndStatus(
                any(LocalDateTime.class), Subscription.SubscriptionStatus.TRIAL))
                .thenReturn(Collections.emptyList());

        // WHEN: La tâche est exécutée.
        trialPeriodEndTask.processTrialPeriodEnd();

        // THEN:
        // Vérifie que les méthodes du repository ont été appelées pour la recherche.
        verify(subscriptionRepository, times(1)).findByTrialEndDate(fixedNow);
        verify(subscriptionRepository, times(1)).findByTrialEndDateBeforeAndStatus(
                any(LocalDateTime.class), Subscription.SubscriptionStatus.TRIAL);

        // Vérifie qu'aucune notification n'a été envoyée.
        verify(notificationService, never()).sendTrialPeriodEndingNotification(any(), any());
        verify(notificationService, never()).sendTrialPeriodEndedNotification(any(), any());
        // Vérifie qu'aucun abonnement n'a été sauvegardé.
        verify(subscriptionRepository, never()).save(any(Subscription.class));
    }

    @Test
    @DisplayName("Devrait gérer les abonnements dont l'utilisateur est null pour les abonnements finissant aujourd'hui")
    void processTrialsEndingToday_shouldHandleNullUser() {
        // GIVEN: Un abonnement finissant aujourd'hui avec un utilisateur null.
        Subscription subEndingTodayNoUser = new Subscription();
        subEndingTodayNoUser.setId(102L);
        subEndingTodayNoUser.setTrialEndDate(fixedNow);
        subEndingTodayNoUser.setStatus(Subscription.SubscriptionStatus.TRIAL);
        subEndingTodayNoUser.setUser(null); // Utilisateur null

        when(subscriptionRepository.findByTrialEndDate(fixedNow))
                .thenReturn(Collections.singletonList(subEndingTodayNoUser));
        when(subscriptionRepository.findByTrialEndDateBeforeAndStatus(any(), any()))
                .thenReturn(Collections.emptyList());

        // WHEN: La tâche est exécutée.
        trialPeriodEndTask.processTrialPeriodEnd();

        // THEN:
        // Vérifie que la notification n'a PAS été envoyée pour cet abonnement car l'utilisateur est null.
        verify(notificationService, never()).sendTrialPeriodEndingNotification(any(), any());
        // (Un log d'avertissement serait attendu ici dans une implémentation robuste, à vérifier avec les tests de logs.)
    }

    @Test
    @DisplayName("Devrait gérer les abonnements dont l'utilisateur est null pour les abonnements déjà terminés")
    void processTrialsAlreadyEnded_shouldHandleNullUser() {
        // GIVEN: Un abonnement déjà terminé avec un utilisateur null.
        Subscription subAlreadyEndedNoUser = new Subscription();
        subAlreadyEndedNoUser.setId(202L);
        subAlreadyEndedNoUser.setTrialEndDate(fixedNow.minusDays(2));
        subAlreadyEndedNoUser.setStatus(Subscription.SubscriptionStatus.TRIAL);
        subAlreadyEndedNoUser.setUser(null); // Utilisateur null

        when(subscriptionRepository.findByTrialEndDate(any())).thenReturn(Collections.emptyList());
        when(subscriptionRepository.findByTrialEndDateBeforeAndStatus(any(), any()))
                .thenReturn(Collections.singletonList(subAlreadyEndedNoUser));

        // Configure le save pour simuler la persistance.
        when(subscriptionRepository.save(any(Subscription.class))).thenReturn(subAlreadyEndedNoUser);


        // WHEN: La tâche est exécutée.
        trialPeriodEndTask.processTrialPeriodEnd();

        // THEN:
        // Vérifie que le statut a été mis à jour et sauvegardé même si l'utilisateur est null.
        verify(subscriptionRepository, times(1)).save(argThat(sub ->
                sub.getId().equals(202L) && sub.getStatus() == Subscription.SubscriptionStatus.TRIAL_ENDED
        ));
        // Vérifie que la notification n'a PAS été envoyée pour cet abonnement car l'utilisateur est null.
        verify(notificationService, never()).sendTrialPeriodEndedNotification(any(), any());
        // (Un log d'avertissement serait attendu ici dans une implémentation robuste, à vérifier avec les tests de logs.)
    }

    @Test
    @DisplayName("Devrait gérer les exceptions lors de l'envoi de la notification de fin d'essai (aujourd'hui)")
    void processTrialsEndingToday_shouldHandleNotificationException() {
        // GIVEN: Un abonnement finissant aujourd'hui, et l'envoi de notification lève une exception.
        User user1 = new User(); user1.setId(1L);
        Subscription subEndingToday = new Subscription();
        subEndingToday.setId(101L);
        subEndingToday.setTrialEndDate(fixedNow);
        subEndingToday.setStatus(Subscription.SubscriptionStatus.TRIAL);
        subEndingToday.setUser(user1);

        when(subscriptionRepository.findByTrialEndDate(fixedNow))
                .thenReturn(Collections.singletonList(subEndingToday));
        when(subscriptionRepository.findByTrialEndDateBeforeAndStatus(any(), any()))
                .thenReturn(Collections.emptyList());

        // Simule une exception lors de l'envoi de la notification.
        doThrow(new RuntimeException("Erreur de notification simulée"))
                .when(notificationService).sendTrialPeriodEndingNotification(any(), any());

        // WHEN: La tâche est exécutée.
        trialPeriodEndTask.processTrialPeriodEnd();

        // THEN:
        // Vérifie que la tentative de notification a bien eu lieu.
        verify(notificationService, times(1)).sendTrialPeriodEndingNotification(user1, subEndingToday);
        // Aucun save n'est attendu car le statut ne change pas pour ces abonnements.
        verify(subscriptionRepository, never()).save(any(Subscription.class));
        // (Un log d'erreur serait attendu ici pour l'exception, à vérifier avec les tests de logs.)
    }

    @Test
    @DisplayName("Devrait gérer les exceptions lors de l'envoi de la notification de fin d'essai (déjà terminée)")
    void processTrialsAlreadyEnded_shouldHandleNotificationException() {
        // GIVEN: Un abonnement déjà terminé, et l'envoi de notification lève une exception.
        User user2 = new User(); user2.setId(2L);
        Subscription subAlreadyEnded = new Subscription();
        subAlreadyEnded.setId(201L);
        subAlreadyEnded.setTrialEndDate(fixedNow.minusDays(5));
        subAlreadyEnded.setStatus(Subscription.SubscriptionStatus.TRIAL);
        subAlreadyEnded.setUser(user2);

        when(subscriptionRepository.findByTrialEndDate(any())).thenReturn(Collections.emptyList());
        when(subscriptionRepository.findByTrialEndDateBeforeAndStatus(any(), any()))
                .thenReturn(Collections.singletonList(subAlreadyEnded));

        // Configure le save pour simuler la persistance.
        when(subscriptionRepository.save(any(Subscription.class))).thenReturn(subAlreadyEnded);


        // Simule une exception lors de l'envoi de la notification.
        doThrow(new RuntimeException("Erreur de notification simulée"))
                .when(notificationService).sendTrialPeriodEndedNotification(any(), any());

        // WHEN: La tâche est exécutée.
        trialPeriodEndTask.processTrialPeriodEnd();

        // THEN:
        // Vérifie que l'abonnement a bien été sauvegardé avec le nouveau statut.
        verify(subscriptionRepository, times(1)).save(argThat(sub ->
                sub.getId().equals(201L) && sub.getStatus() == Subscription.SubscriptionStatus.TRIAL_ENDED
        ));
        // Vérifie que la tentative de notification a bien eu lieu.
        verify(notificationService, times(1)).sendTrialPeriodEndedNotification(user2, subAlreadyEnded);
        // (Un log d'erreur serait attendu ici pour l'exception, à vérifier avec les tests de logs.)
    }
}