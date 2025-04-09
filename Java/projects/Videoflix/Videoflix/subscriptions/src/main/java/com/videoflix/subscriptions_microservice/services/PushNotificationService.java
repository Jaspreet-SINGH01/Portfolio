package com.videoflix.subscriptions_microservice.services;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.videoflix.subscriptions_microservice.entities.SubscriptionLevel;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class PushNotificationService {

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(PushNotificationService.class);
    private static final String SUBSCRIPTION_PREFIX = "Votre abonnement ";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final FirebaseMessaging firebaseMessaging;

    public PushNotificationService(FirebaseMessaging firebaseMessaging) {
        this.firebaseMessaging = firebaseMessaging;
    }

    public void sendPushNotification(String registrationToken, String title, String body) {
        Notification notification = Notification.builder()
                .setTitle(title)
                .setBody(body)
                .build();

        Message message = Message.builder()
                .setToken(registrationToken)
                .setNotification(notification)
                .build();

        try {
            String response = firebaseMessaging.send(message);
            logger.info("Successfully sent message: {}", response);
        } catch (FirebaseMessagingException e) {
            logger.error("Error sending message", e);
        }
    }

    // Méthodes spécifiques pour les notifications d'abonnement
    public void sendSubscriptionRenewalPushNotification(String registrationToken, String subscriptionLevel) {
        String title = "Abonnement renouvelé";
        String body = SUBSCRIPTION_PREFIX + subscriptionLevel + " a été renouvelé.";
        sendPushNotification(registrationToken, title, body);
    }

    public void sendSubscriptionExpirationPushNotification(String registrationToken, SubscriptionLevel subscriptionLevel) {
        String title = "Abonnement expire bientôt";
        String body = SUBSCRIPTION_PREFIX + subscriptionLevel + " expire bientôt. Renouvelez-le !";
        sendPushNotification(registrationToken, title, body);
    }

    public void sendSubscriptionExpirationImminentPushNotification(String registrationToken, SubscriptionLevel subscriptionLevel,
            LocalDateTime expirationDate) {
        String formattedDate = expirationDate.format(DATE_FORMATTER);
        String title = "Expiration imminente !";
        String body = SUBSCRIPTION_PREFIX + subscriptionLevel + " expire le " + formattedDate + ". Ne manquez rien !";
        sendPushNotification(registrationToken, title, body);
    }

    public void sendPaymentFailedPushNotification(String registrationToken, String subscriptionLevel) {
        String title = "Paiement échoué";
        String body = "Le paiement de votre abonnement " + subscriptionLevel
                + " a échoué. Veuillez vérifier vos informations.";
        sendPushNotification(registrationToken, title, body);
    }

    public void sendPaymentRetrySuccessPushNotification(String registrationToken, String subscriptionLevel) {
        String title = "Paiement réussi";
        String body = "Le paiement de votre abonnement " + subscriptionLevel + " a été effectué avec succès.";
        sendPushNotification(registrationToken, title, body);
    }
}