package com.videoflix.subscriptions_microservice.services;

import com.videoflix.subscriptions_microservice.entities.Subscription;
import com.videoflix.subscriptions_microservice.entities.User;
import com.videoflix.subscriptions_microservice.templates.EmailTemplates;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Service responsable de l'envoi des notifications par e-mail aux utilisateurs
 * concernant leurs abonnements.
 */
@Service
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);
    static final String NULL_PARAMS_ERROR = "Impossible d'envoyer la notification: utilisateur ou abonnement null";

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String notificationSender;

    /**
     * Constructeur avec injection du mailSender.
     */
    public NotificationService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * Envoie un e-mail pour avertir l'utilisateur que son abonnement va expirer.
     */
    public void sendSubscriptionExpiringNotification(User user, Subscription subscription) {
        if (!isValid(user, subscription))
            return;

        sendEmail(
                user.getEmail(),
                EmailTemplates.SUBJECT_SUBSCRIPTION_EXPIRING,
                String.format(EmailTemplates.SUBSCRIPTION_EXPIRING_EMAIL,
                        user.getFirstname(),
                        subscription.getSubscriptionLevel(),
                        subscription.getEndDate()),
                user.getId(),
                "expiration imminente");
    }

    /**
     * Envoie un e-mail pour notifier que l’abonnement de l’utilisateur a expiré.
     */
    public void sendSubscriptionExpiredNotification(User user, Subscription subscription) {
        if (!isValid(user, subscription))
            return;

        sendEmail(
                user.getEmail(),
                EmailTemplates.SUBJECT_SUBSCRIPTION_EXPIRED,
                String.format(EmailTemplates.SUBJECT_SUBSCRIPTION_EXPIRED,
                        user.getFirstname(),
                        subscription.getSubscriptionLevel(),
                        subscription.getEndDate()),
                user.getId(),
                "expiration");
    }

    /**
     * Envoie un e-mail pour avertir que la période d'essai se termine bientôt.
     */
    public void sendTrialPeriodEndingNotification(User user, Subscription subscription) {
        if (!isValid(user, subscription))
            return;

        sendEmail(
                user.getEmail(),
                EmailTemplates.SUBJECT_TRIAL_ENDING,
                String.format(EmailTemplates.TRIAL_PERIOD_ENDING_NOTIFICATION,
                        user.getFirstname(),
                        subscription.getSubscriptionLevel(),
                        subscription.getTrialEndDate()),
                user.getId(),
                "fin de période d’essai imminente");
    }

    /**
     * Envoie un e-mail pour notifier que la période d’essai est terminée.
     */
    public void sendTrialPeriodEndedNotification(User user, Subscription subscription) {
        if (!isValid(user, subscription))
            return;

        sendEmail(
                user.getEmail(),
                EmailTemplates.SUBJECT_TRIAL_ENDED,
                String.format(EmailTemplates.SUBJECT_TRIAL_ENDED,
                        user.getFirstname(),
                        subscription.getSubscriptionLevel(),
                        subscription.getTrialEndDate()),
                user.getId(),
                "fin de période d’essai");
    }

    /**
     * Envoie un e-mail de bienvenue à l'utilisateur.
     */
    public void sendWelcomeEmail(User user, Subscription subscription) {
        if (!isValid(user, subscription))
            return;

        sendEmail(
                user.getEmail(),
                EmailTemplates.SUBJECT_WELCOME,
                String.format(EmailTemplates.WELCOME_EMAIL,
                        user.getFirstname(),
                        subscription.getSubscriptionLevel()),
                user.getId(),
                "de bienvenue");
    }

    /**
     * Envoie un rappel de paiement à l’utilisateur.
     */
    public void sendPaymentReminderNotification(User user, Subscription subscription, int daysBefore) {
        if (!isValid(user, subscription))
            return;

        String body = String.format(EmailTemplates.SUBSCRIPTION_NOTIFICATION,
                user.getFirstname(),
                subscription.getSubscriptionLevel(),
                subscription.getNextBillingDate(),
                daysBefore);

        sendEmail(
                user.getEmail(),
                EmailTemplates.SUBJECT_PAYMENT_REMINDER,
                body,
                user.getId(),
                "de rappel de paiement");
    }

    /**
     * Méthode utilitaire pour envoyer un e-mail générique avec logs d’erreur.
     */
    private void sendEmail(String to, String subject, String body, Long userId, String type) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(notificationSender);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);

            mailSender.send(message);
            logger.info("E-mail {} envoyé à l'utilisateur {}", type, userId);
        } catch (Exception e) {
            logger.error("Erreur lors de l'envoi de l'e-mail {} à l'utilisateur {}", type, userId, e);
        }
    }

    /**
     * Vérifie si les objets utilisateur et abonnement sont valides.
     */
    private boolean isValid(User user, Subscription subscription) {
        if (user == null || subscription == null) {
            logger.error(NULL_PARAMS_ERROR);
            return false;
        }
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            logger.warn("Impossible d'envoyer l'e-mail à l'utilisateur {} car l'adresse e-mail est manquante.",
                    user.getId());
            return false;
        }
        return true;
    }
}
