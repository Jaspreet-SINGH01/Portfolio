package com.videoflix.subscriptions_microservice.services;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.videoflix.subscriptions_microservice.entities.SubscriptionLevel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.mockito.Mockito.*;

/**
 * Teste les différentes notifications push envoyées via FirebaseMessaging.
 * Chaque test vérifie que le message envoyé contient les bons titres et corps,
 * et que les erreurs sont correctement journalisées.
 */
@ExtendWith(MockitoExtension.class)
class PushNotificationServiceTest {

    @Mock
    private FirebaseMessaging firebaseMessaging;

    @Mock
    private Logger logger;

    @InjectMocks
    private PushNotificationService pushNotificationService;

    @Captor
    private ArgumentCaptor<Message> messageCaptor;

    private static final String TEST_SUBSCRIPTION_PREFIX = "Votre abonnement ";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Test
    void sendPushNotification_whenSuccessful_shouldSendMessage() throws FirebaseMessagingException {
        String token = "testToken";
        String title = "Test Title";
        String body = "Test Body";
        String response = "Successfully sent";

        when(firebaseMessaging.send(any(Message.class))).thenReturn(response);

        pushNotificationService.sendPushNotification(token, title, body);

        verify(firebaseMessaging).send(messageCaptor.capture());
        assertNotification(messageCaptor.getValue(), title, body, token);
        verify(logger).info("Successfully sent message: {}", response);
    }

    @Test
    void sendPushNotification_whenExceptionThrown_shouldLogError() throws FirebaseMessagingException {
        String token = "errorToken";
        String title = "Error Title";
        String body = "Error Body";

        FirebaseMessagingException exception = mock(FirebaseMessagingException.class);
        when(firebaseMessaging.send(any(Message.class))).thenThrow(exception);

        pushNotificationService.sendPushNotification(token, title, body);

        verify(firebaseMessaging).send(any(Message.class));
        verify(logger).error("Error sending message", exception);
    }

    @Test
    void sendSubscriptionRenewalPushNotification_shouldContainCorrectTitleAndBody() throws FirebaseMessagingException {
        String token = "renewalToken";
        String level = "Premium";
        String expectedTitle = "Abonnement renouvelé";
        String expectedBody = TEST_SUBSCRIPTION_PREFIX + level + " a été renouvelé.";

        when(firebaseMessaging.send(any(Message.class))).thenReturn("Renewal sent");

        pushNotificationService.sendSubscriptionRenewalPushNotification(token, level);

        verify(firebaseMessaging).send(messageCaptor.capture());
        assertNotification(messageCaptor.getValue(), expectedTitle, expectedBody, token);
    }

    @Test
    void sendSubscriptionExpirationPushNotification_shouldContainCorrectTitleAndBody()
            throws FirebaseMessagingException {
        String token = "expirationToken";
        SubscriptionLevel level = new SubscriptionLevel();
        level.setLevel(SubscriptionLevel.Level.BASIC);
        String expectedTitle = "Abonnement expire bientôt";
        String expectedBody = TEST_SUBSCRIPTION_PREFIX + level.getLevel() + " expire bientôt. Renouvelez-le !";

        when(firebaseMessaging.send(any(Message.class))).thenReturn("Expiration sent");

        pushNotificationService.sendSubscriptionExpirationPushNotification(token, level);

        verify(firebaseMessaging).send(messageCaptor.capture());
        assertNotification(messageCaptor.getValue(), expectedTitle, expectedBody, token);
    }

    @Test
    void sendSubscriptionExpirationImminentPushNotification_shouldContainCorrectTitleAndFormattedDate()
            throws FirebaseMessagingException {
        String token = "imminentToken";
        SubscriptionLevel level = new SubscriptionLevel();
        level.setLevel(SubscriptionLevel.Level.PREMIUM);
        LocalDateTime expirationDate = LocalDateTime.of(2025, 5, 12, 10, 0);
        String formattedDate = expirationDate.format(DATE_FORMATTER);

        String expectedTitle = "Expiration imminente !";
        String expectedBody = TEST_SUBSCRIPTION_PREFIX + level.getLevel() + " expire le " + formattedDate
                + ". Ne manquez rien !";

        when(firebaseMessaging.send(any(Message.class))).thenReturn("Imminent expiration sent");

        pushNotificationService.sendSubscriptionExpirationImminentPushNotification(token, level, expirationDate);

        verify(firebaseMessaging).send(messageCaptor.capture());
        assertNotification(messageCaptor.getValue(), expectedTitle, expectedBody, token);
    }

    @Test
    void sendPaymentFailedPushNotification_shouldContainCorrectMessage() throws FirebaseMessagingException {
        String token = "failedToken";
        String level = "Basic";
        String expectedTitle = "Paiement échoué";
        String expectedBody = "Le paiement de votre abonnement " + level
                + " a échoué. Veuillez vérifier vos informations.";

        when(firebaseMessaging.send(any(Message.class))).thenReturn("Payment failed sent");

        pushNotificationService.sendPaymentFailedPushNotification(token, level);

        verify(firebaseMessaging).send(messageCaptor.capture());
        assertNotification(messageCaptor.getValue(), expectedTitle, expectedBody, token);
    }

    @Test
    void sendPaymentRetrySuccessPushNotification_shouldContainCorrectMessage() throws FirebaseMessagingException {
        String token = "successToken";
        String level = "Ultra";
        String expectedTitle = "Paiement réussi";
        String expectedBody = "Le paiement de votre abonnement " + level + " a été effectué avec succès.";

        when(firebaseMessaging.send(any(Message.class))).thenReturn("Payment success sent");

        pushNotificationService.sendPaymentRetrySuccessPushNotification(token, level);

        verify(firebaseMessaging).send(messageCaptor.capture());
        assertNotification(messageCaptor.getValue(), expectedTitle, expectedBody, token);
    }

    /**
     * Vérifie que la notification contient les bons champs.
     */
    private void assertNotification(Message message, String expectedTitle, String expectedBody, String expectedToken) {
        String messageString = message.toString();

        assert messageString.contains(expectedToken) : "Token non trouvé dans le message";
        assert messageString.contains(expectedTitle) : "Titre non trouvé dans le message";
        assert messageString.contains(expectedBody) : "Corps non trouvé dans le message";
    }
}