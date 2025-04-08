package com.videoflix.subscriptions_microservice.services;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import org.springframework.stereotype.Service;

@Service
public class PushNotificationService {

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
            System.out.println("Successfully sent message: " + response);
        } catch (FirebaseMessagingException e) {
            e.printStackTrace();
        }
    }

    // Méthodes spécifiques pour les notifications d'abonnement
    public void sendSubscriptionRenewalPushNotification(String registrationToken, String subscriptionType) {
        String title = "Abonnement renouvelé";
        String body = "Votre abonnement " + subscriptionType + " a été renouvelé.";
        sendPushNotification(registrationToken, title, body);
    }

    public void sendSubscriptionExpirationPushNotification(String registrationToken, String subscriptionType) {
        String title = "Abonnement expire bientôt";
        String body = "Votre abonnement " + subscriptionType + " expire bientôt. Renouvelez-le !";
        sendPushNotification(registrationToken, title, body);
    }
}