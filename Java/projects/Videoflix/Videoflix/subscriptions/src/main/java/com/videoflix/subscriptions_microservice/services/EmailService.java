package com.videoflix.subscriptions_microservice.services;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

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
    public void sendSubscriptionRenewalNotification(String to, String subscriptionType) {
        String subject = "Votre abonnement " + subscriptionType + " a été renouvelé";
        String body = "Cher client,\n\nVotre abonnement " + subscriptionType + " a été renouvelé avec succès.\n\nMerci de votre fidélité.";
        sendSubscriptionNotification(to, subject, body);
    }

    public void sendSubscriptionExpirationNotification(String to, String subscriptionType) {
        String subject = "Votre abonnement " + subscriptionType + " va expirer bientôt";
        String body = "Cher client,\n\nVotre abonnement " + subscriptionType + " expirera le [Date d'expiration].\nPensez à le renouveler pour continuer à profiter de nos services.\n\nCordialement.";
        sendSubscriptionNotification(to, subject, body);
    }

    // ... autres méthodes pour les notifications d'expiration imminente, d'échec de paiement, etc.
}