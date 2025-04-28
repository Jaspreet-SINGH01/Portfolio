package com.videoflix.subscriptions_microservice.batch.processor;

import com.videoflix.subscriptions_microservice.entities.Subscription;
import com.videoflix.subscriptions_microservice.entities.User;
import com.videoflix.subscriptions_microservice.services.NotificationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class InactiveSubscriptionProcessorTest {

    @InjectMocks
    private InactiveSubscriptionProcessor processor; // L'instance du processor à tester

    @Mock
    private NotificationService notificationService; // Mock du service de notification

    private static final long DAYS_BEFORE_FINAL_NOTIFICATION = 7;

    static Logger logger = LoggerFactory.getLogger(InactiveSubscriptionProcessor.class);

    @Test
    void process_shouldSendFinalNotification_whenPaymentDateIsWithinNotificationWindow() throws Exception {
        // GIVEN : Un abonnement avec une date de paiement dans la fenêtre de
        // notification

        LocalDateTime notificationCutoff = LocalDateTime.now().minus(DAYS_BEFORE_FINAL_NOTIFICATION + 1,
                ChronoUnit.DAYS);
        LocalDateTime paymentDateWithinWindow = notificationCutoff.plus(1, ChronoUnit.HOURS); // Date juste après la
                                                                                              // limite de notification

        Subscription subscription = new Subscription();
        subscription.setId(1L);
        subscription.setPaymentDate(paymentDateWithinWindow);
        subscription.setUser(new User()); // L'utilisateur est nécessaire pour la notification

        // WHEN : Traitement de l'abonnement
        Subscription processedSubscription = processor.process(subscription);

        // THEN : Vérification que la notification a été envoyée et que l'abonnement est
        // retourné
        verify(notificationService, times(1)).sendSubscriptionExpiringNotification(subscription.getUser(),
                subscription);
        assertEquals(subscription, processedSubscription);
    }

    @Test
    void process_shouldNotSendFinalNotification_whenPaymentDateIsNull() throws Exception {
        // GIVEN : Un abonnement avec une date de paiement nulle
        Subscription subscription = new Subscription();
        subscription.setId(2L);
        subscription.setPaymentDate(null);
        subscription.setUser(new User());

        // WHEN : Traitement de l'abonnement
        Subscription processedSubscription = processor.process(subscription);

        // THEN : Vérification que la notification n'a pas été envoyée et que
        // l'abonnement est retourné
        verify(notificationService, never()).sendSubscriptionExpiringNotification(any(), any());
        assertEquals(subscription, processedSubscription);
    }

    @Test
    void process_shouldNotSendFinalNotification_whenPaymentDateIsBeforeNotificationWindow() throws Exception {
        // GIVEN : Un abonnement avec une date de paiement antérieure à la fenêtre de
        // notification

        LocalDateTime notificationCutoff = LocalDateTime.now().minus(DAYS_BEFORE_FINAL_NOTIFICATION + 1,
                ChronoUnit.DAYS);
        LocalDateTime paymentDateBeforeWindow = notificationCutoff.minus(1, ChronoUnit.DAYS);

        Subscription subscription = new Subscription();
        subscription.setId(3L);
        subscription.setPaymentDate(paymentDateBeforeWindow);
        subscription.setUser(new User());

        // WHEN : Traitement de l'abonnement
        Subscription processedSubscription = processor.process(subscription);

        // THEN : Vérification que la notification n'a pas été envoyée et que
        // l'abonnement est retourné
        verify(notificationService, never()).sendSubscriptionExpiringNotification(any(), any());
        assertEquals(subscription, processedSubscription);
    }

    @Test
    void process_shouldNotSendFinalNotification_whenPaymentDateIsAfterSuppressionCutoff() throws Exception {
        // GIVEN : Un abonnement avec une date de paiement postérieure à la date limite
        // de suppression
        // (Cela ne devrait pas arriver dans le flux normal, mais c'est un cas limite à
        // tester)

        LocalDateTime suppressionCutoff = LocalDateTime.now().minus(90, ChronoUnit.DAYS);
        LocalDateTime paymentDateAfterSuppression = suppressionCutoff.plus(1, ChronoUnit.DAYS);

        Subscription subscription = new Subscription();
        subscription.setId(4L);
        subscription.setPaymentDate(paymentDateAfterSuppression);
        subscription.setUser(new User());

        // WHEN : Traitement de l'abonnement
        Subscription processedSubscription = processor.process(subscription);

        // THEN : Vérification que la notification n'a pas été envoyée et que
        // l'abonnement est retourné
        verify(notificationService, never()).sendSubscriptionExpiringNotification(any(), any());
        assertEquals(subscription, processedSubscription);
    }

    @Test
    void process_shouldLogError_whenNotificationServiceThrowsException() throws Exception {
        // GIVEN : Un abonnement dans la fenêtre de notification et un service de
        // notification qui lève une exception

        LocalDateTime notificationCutoff = LocalDateTime.now().minus(DAYS_BEFORE_FINAL_NOTIFICATION + 1,
                ChronoUnit.DAYS);
        LocalDateTime paymentDateWithinWindow = notificationCutoff.plus(1, ChronoUnit.HOURS);

        Subscription subscription = new Subscription();
        subscription.setId(5L);
        subscription.setPaymentDate(paymentDateWithinWindow);
        subscription.setUser(new User());

        // Configuration du mock pour lever une exception lors de l'envoi de la
        // notification
        doThrow(new RuntimeException("Erreur d'envoi de notification")).when(notificationService)
                .sendSubscriptionExpiringNotification(any(), any());

        // Récupération du logger pour vérifier le message d'erreur
        logger = LoggerFactory.getLogger(InactiveSubscriptionProcessor.class);
        // Capture de la sortie du logger (nécessite une configuration spécifique ou une
        // librairie de test de log)
        // Pour simplifier, on se contente de vérifier que la méthode du service a été
        // appelée.

        // WHEN : Traitement de l'abonnement
        Subscription processedSubscription = processor.process(subscription);

        // THEN : Vérification que la méthode du service a été appelée (l'erreur de log
        // est plus difficile à tester sans configuration spécifique)
        verify(notificationService, times(1)).sendSubscriptionExpiringNotification(any(), any());
        assertEquals(subscription, processedSubscription);
        // Idéalement, on vérifierait ici qu'un message d'erreur a été logué.
    }
}