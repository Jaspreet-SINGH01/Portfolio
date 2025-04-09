package com.videoflix.subscriptions_microservice.services;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.videoflix.subscriptions_microservice.entities.SubscriptionLevel;

@Service
public class EmailService {

    private static final String SUBSCRIPTION_PREFIX = "Votre abonnement ";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendSubscriptionNotification(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
    }

    // Méthodes spécifiques pour différents types de notifications
    public void sendSubscriptionRenewalNotification(String to, String subscriptionLevel) {
        String subject = SUBSCRIPTION_PREFIX + subscriptionLevel + " a été renouvelé";
        String body = "Cher client,\n\n" + SUBSCRIPTION_PREFIX + subscriptionLevel +
                " a été renouvelé avec succès.\n\nMerci de votre fidélité.";
        sendSubscriptionNotification(to, subject, body);
    }

    public void sendSubscriptionExpirationNotification(String to, String subscriptionLevel) {
        String subject = SUBSCRIPTION_PREFIX + subscriptionLevel + " va expirer bientôt";
        String body = "Cher client,\n\nVotre abonnement " + subscriptionLevel
                + " expirera le [Date d'expiration].\nPensez à le renouveler pour continuer à profiter de nos services.\n\nCordialement.";
        sendSubscriptionNotification(to, subject, body);
    }

    public void sendSubscriptionExpirationImminentNotification(String to, SubscriptionLevel subscriptionLevel,
            LocalDateTime expirationDate) {
        String formattedDate = expirationDate.format(DATE_FORMATTER);
        String subject = SUBSCRIPTION_PREFIX + subscriptionLevel + " expire dans 3 jours !";
        String body = "Cher client,\n\nVotre abonnement " + subscriptionLevel + " expire le " + formattedDate
                + ".\nNe manquez pas la fin de votre accès, renouvelez dès maintenant !\n\nCordialement.";
        sendSubscriptionNotification(to, subject, body);
    }

    public void sendPaymentFailedNotification(String to, String subscriptionLevel) {
        String subject = "Échec du paiement de votre abonnement " + subscriptionLevel;
        String body = "Cher client,\n\nLe paiement de votre abonnement " + subscriptionLevel
                + " a échoué. Veuillez vérifier vos informations de paiement et réessayer.\n\nCordialement.";
        sendSubscriptionNotification(to, subject, body);
    }

    public void sendPaymentRetrySuccessNotification(String to, String subscriptionLevel) {
        String subject = "Paiement réussi pour votre abonnement " + subscriptionLevel;
        String body = "Cher client,\n\nLe paiement de votre abonnement " + subscriptionLevel
                + " a été effectué avec succès.\n\nMerci !";
        sendSubscriptionNotification(to, subject, body);
    }
}