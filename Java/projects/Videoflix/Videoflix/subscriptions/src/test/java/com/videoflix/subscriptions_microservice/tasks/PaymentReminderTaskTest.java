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
import org.springframework.test.util.ReflectionTestUtils; // Pour injecter les valeurs des champs @Value

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

// @ExtendWith(MockitoExtension.class) permet d'initialiser les mocks automatiquement avant chaque test.
@ExtendWith(MockitoExtension.class)
@DisplayName("Tests pour PaymentReminderTask")
class PaymentReminderTaskTest {

    // @Mock crée une instance mockée de SubscriptionRepository.
    // Nous allons simuler son comportement pour retourner des abonnements.
    @Mock
    private SubscriptionRepository subscriptionRepository;

    // @Mock crée une instance mockée de NotificationService.
    // Nous allons vérifier que les méthodes d'envoi de notifications sont appelées.
    @Mock
    private NotificationService notificationService;

    // @InjectMocks crée une instance de PaymentReminderTask et y injecte les mocks.
    @InjectMocks
    private PaymentReminderTask paymentReminderTask;

    // Nombre de jours avant la date de facturation pour envoyer le rappel (valeur
    // @Value).
    private final int daysBeforeReminder = 3;

    // Horodatage fixe pour simuler 'LocalDateTime.now()' afin de rendre les tests
    // déterministes.
    // La tâche est planifiée à 10h00 du matin, donc nous fixons cette heure pour la
    // cohérence.
    private LocalDateTime fixedNow;

    /**
     * Méthode exécutée avant chaque test.
     * Configure les valeurs des champs annotés par @Value dans la tâche,
     * et initialise `fixedNow` pour les calculs de date de rappel.
     */
    @BeforeEach
    void setUp() {
        // Définit une date et heure fixe pour "aujourd'hui" (le 24 mai 2025 à 10h00)
        // cela correspond à l'heure d'exécution planifiée de la tâche.
        fixedNow = LocalDateTime.of(2025, 5, 24, 10, 0, 0);

        // Injecte la valeur `daysBeforeReminder` dans la tâche.
        ReflectionTestUtils.setField(paymentReminderTask, "daysBeforeReminder", daysBeforeReminder);

        // Réinitialise les mocks pour garantir l'indépendance des tests.
        reset(subscriptionRepository, notificationService);
    }

    @Test
    @DisplayName("Devrait envoyer des rappels de paiement pour les abonnements dont la date de facturation est imminente")
    void sendPaymentReminders_shouldSendRemindersForDueSubscriptions() {
        // GIVEN: Deux abonnements dont la prochaine date de facturation est dans
        // daysBeforeReminder jours.
        User user1 = new User();
        user1.setId(1L);
        user1.setEmail("user1@example.com");
        User user2 = new User();
        user2.setId(2L);
        user2.setEmail("user2@example.com");

        Subscription sub1 = new Subscription();
        sub1.setId(101L);
        sub1.setNextBillingDate(fixedNow.plusDays(daysBeforeReminder)); // Date de facturation dans 3 jours
        sub1.setUser(user1);

        Subscription sub2 = new Subscription();
        sub2.setId(102L);
        sub2.setNextBillingDate(fixedNow.plusDays(daysBeforeReminder));
        sub2.setUser(user2);

        List<Subscription> subscriptionsDue = Arrays.asList(sub1, sub2);

        // Calcule la date de rappel attendue par la tâche.
        LocalDateTime expectedReminderDate = fixedNow.plus(daysBeforeReminder, ChronoUnit.DAYS);

        // Configure le repository pour retourner ces abonnements lorsque la méthode
        // `findByNextBillingDate` est appelée.
        when(subscriptionRepository.findByNextBillingDate(expectedReminderDate))
                .thenReturn(subscriptionsDue);

        // WHEN: La tâche de rappel de paiement est exécutée.
        paymentReminderTask.sendPaymentReminders();

        // THEN:
        // Vérifie que `findByNextBillingDate` a été appelé une fois avec la date de
        // rappel calculée.
        verify(subscriptionRepository, times(1)).findByNextBillingDate(expectedReminderDate);

        // Vérifie que la notification a été envoyée pour chaque abonnement dû.
        verify(notificationService, times(1)).sendPaymentReminderNotification(user1, sub1, daysBeforeReminder);
        verify(notificationService, times(1)).sendPaymentReminderNotification(user2, sub2, daysBeforeReminder);

        // Vérifie que `sendPaymentReminderNotification` a été appelé un total de 2
        // fois.
        verify(notificationService, times(2)).sendPaymentReminderNotification(any(User.class), any(Subscription.class),
                eq(daysBeforeReminder));
    }

    @Test
    @DisplayName("Ne devrait pas envoyer de rappels si aucun abonnement n'est dû pour un rappel")
    void sendPaymentReminders_shouldNotSendRemindersIfNoSubscriptionsDue() {
        // GIVEN: Aucune abonnement dont la prochaine date de facturation correspond à
        // la date de rappel.
        LocalDateTime expectedReminderDate = fixedNow.plus(daysBeforeReminder, ChronoUnit.DAYS);

        // Configure le repository pour retourner une liste vide.
        when(subscriptionRepository.findByNextBillingDate(expectedReminderDate))
                .thenReturn(Collections.emptyList());

        // WHEN: La tâche de rappel de paiement est exécutée.
        paymentReminderTask.sendPaymentReminders();

        // THEN:
        // Vérifie que `findByNextBillingDate` a été appelé une fois avec la date
        // correcte.
        verify(subscriptionRepository, times(1)).findByNextBillingDate(expectedReminderDate);

        // Vérifie qu'aucune notification n'a été envoyée.
        verify(notificationService, never()).sendPaymentReminderNotification(any(), any(), anyInt());
    }

    @Test
    @DisplayName("Devrait gérer les abonnements sans utilisateur associé et loguer un avertissement")
    void sendPaymentReminders_shouldHandleSubscriptionsWithoutUser() {
        // GIVEN: Un abonnement dont la prochaine date de facturation est imminente mais
        // sans utilisateur associé.
        Subscription subWithoutUser = new Subscription();
        subWithoutUser.setId(301L);
        subWithoutUser.setNextBillingDate(fixedNow.plusDays(daysBeforeReminder));
        subWithoutUser.setUser(null); // Utilisateur nul

        List<Subscription> subscriptionsDue = Collections.singletonList(subWithoutUser);
        LocalDateTime expectedReminderDate = fixedNow.plus(daysBeforeReminder, ChronoUnit.DAYS);

        when(subscriptionRepository.findByNextBillingDate(expectedReminderDate))
                .thenReturn(subscriptionsDue);

        // WHEN: La tâche de rappel de paiement est exécutée.
        paymentReminderTask.sendPaymentReminders();

        // THEN:
        // Vérifie que `findByNextBillingDate` a été appelé.
        verify(subscriptionRepository, times(1)).findByNextBillingDate(expectedReminderDate);

        // Vérifie qu'aucune notification n'a été envoyée car l'utilisateur est nul.
        verify(notificationService, never()).sendPaymentReminderNotification(any(), any(), anyInt());
        // (Il serait également possible de vérifier les logs ici pour confirmer
        // l'avertissement,
        // mais cela nécessite des bibliothèques de test de log ou un mock du logger, ce
        // qui est plus complexe.)
    }

    @Test
    @DisplayName("Devrait gérer les exceptions lors de l'envoi d'une notification et continuer avec les autres")
    void sendPaymentReminders_shouldHandleNotificationExceptionAndContinue() {
        // GIVEN: Deux abonnements sont dus, mais l'envoi de notification échoue pour le
        // premier.
        User user1 = new User();
        user1.setId(1L);
        user1.setEmail("user1@example.com");
        User user2 = new User();
        user2.setId(2L);
        user2.setEmail("user2@example.com");

        Subscription sub1 = new Subscription();
        sub1.setId(101L);
        sub1.setNextBillingDate(fixedNow.plusDays(daysBeforeReminder));
        sub1.setUser(user1);

        Subscription sub2 = new Subscription();
        sub2.setId(102L);
        sub2.setNextBillingDate(fixedNow.plusDays(daysBeforeReminder));
        sub2.setUser(user2);

        List<Subscription> subscriptionsDue = Arrays.asList(sub1, sub2);
        LocalDateTime expectedReminderDate = fixedNow.plus(daysBeforeReminder, ChronoUnit.DAYS);

        when(subscriptionRepository.findByNextBillingDate(expectedReminderDate))
                .thenReturn(subscriptionsDue);

        // Simule une exception lors du premier appel à
        // `sendPaymentReminderNotification`.
        doThrow(new RuntimeException("Erreur d'envoi de notification simulée"))
                .when(notificationService).sendPaymentReminderNotification(user1, sub1, daysBeforeReminder);

        // WHEN: La tâche est exécutée.
        paymentReminderTask.sendPaymentReminders();

        // THEN:
        // Vérifie que `findByNextBillingDate` a été appelé.
        verify(subscriptionRepository, times(1)).findByNextBillingDate(expectedReminderDate);

        // Vérifie que la notification a été tentée pour le premier abonnement (et a
        // échoué).
        verify(notificationService, times(1)).sendPaymentReminderNotification(user1, sub1, daysBeforeReminder);

        // Vérifie que la notification a quand même été envoyée pour le deuxième
        // abonnement.
        verify(notificationService, times(1)).sendPaymentReminderNotification(user2, sub2, daysBeforeReminder);

        // Vérifie que `sendPaymentReminderNotification` a été appelé un total de 2
        // fois, malgré l'erreur.
        verify(notificationService, times(2)).sendPaymentReminderNotification(user2, sub2, daysBeforeReminder);
        // (La gestion des logs d'erreur serait ici à vérifier avec un framework de log
        // approprié si nécessaire.)
    }
}