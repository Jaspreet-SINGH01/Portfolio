package com.videoflix.subscriptions_microservice.tasks;

import com.videoflix.subscriptions_microservice.entities.Subscription;
import com.videoflix.subscriptions_microservice.entities.User;
import com.videoflix.subscriptions_microservice.services.AdminNotificationService;
import com.videoflix.subscriptions_microservice.services.NotificationService;
import com.videoflix.subscriptions_microservice.events.NewSubscriptionCreatedEvent;
import com.videoflix.subscriptions_microservice.repositories.FailedEmailRepository;
import com.videoflix.subscriptions_microservice.entities.FailedEmail;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class WelcomeEmailTask {

    private static final Logger logger = LoggerFactory.getLogger(WelcomeEmailTask.class);

    private final NotificationService notificationService;
    private final AdminNotificationService adminNotificationService;
    private final FailedEmailRepository failedEmailRepository;

    static final int MAX_RETRIES = 3;
    private static final long BACKOFF_DELAY = 5000;

    public WelcomeEmailTask(NotificationService notificationService, FailedEmailRepository failedEmailRepository,
            AdminNotificationService adminNotificationService) {
        this.adminNotificationService = adminNotificationService;
        this.notificationService = notificationService;
        this.failedEmailRepository = failedEmailRepository;
    }

    @Async
    @EventListener
    @Retryable(retryFor = {
            Exception.class }, maxAttempts = MAX_RETRIES, backoff = @Backoff(delay = BACKOFF_DELAY, multiplier = 2), recover = "recovery")
    public void handleNewSubscriptionCreatedEvent(NewSubscriptionCreatedEvent event) {
        User user = event.getUser();
        Subscription subscription = event.getSubscription();

        logger.info("Réception de l'Évènement de création d'un nouvel abonnement pour l'utilisateur {}", user.getId());

        try {
            notificationService.sendWelcomeEmail(user, subscription);
            logger.info("E-mail de bienvenue envoyé avec succès à l'utilisateur {}", user.getId());
        } catch (Exception e) {
            // Loguer l'exception avec son contexte complet
            logger.error("Échec de l'envoi de l'e-mail de bienvenue à l'utilisateur {} (tentative {}/{})",
                    user.getId(), 1, MAX_RETRIES, e);
        }
    }

    @Recover
    public void recovery(NewSubscriptionCreatedEvent event, Exception e) {
        User user = event.getUser();
        Subscription subscription = event.getSubscription();

        logger.error("Échec persistant de l'envoi de l'e-mail de bienvenue à l'utilisateur {} après {} tentatives : {}",
                user.getId(), MAX_RETRIES, e.getMessage(), e);

        FailedEmail failedEmail = new FailedEmail(null, null, null, 0, null, null);
        failedEmail.setRecipientEmail(user.getEmail());
        failedEmail.setSubject("Bienvenue chez Videoflix ! (Échec d'envoi)");
        failedEmail.setBody(String.format(
                "Tentative d'envoi de l'e-mail de bienvenue échouée pour l'utilisateur %s (ID: %d) lors de la création de l'abonnement %d. Erreur : %s",
                user.getFirstname(), user.getId(), subscription.getId(), e.getMessage()));
        failedEmail.setAttemptCount(MAX_RETRIES);
        failedEmail.setFailureReason(e.getMessage());
        failedEmail.setCreationTimestamp(java.time.LocalDateTime.now());

        try {
            failedEmailRepository.save(failedEmail);
            logger.info(
                    "Échec de l'envoi de l'e-mail de bienvenue enregistré dans la base de données pour l'utilisateur {}",
                    user.getId());
        } catch (Exception dbException) {
            logger.error(
                    "Erreur lors de l'enregistrement de l'échec d'envoi de l'e-mail dans la base de données pour l'utilisateur {} : {}",
                    user.getId(), dbException.getMessage(), dbException);
        }

        try {
            adminNotificationService.notifyAdminEmailSendFailure(user, "Bienvenue", e.getMessage());
            logger.info(
                    "Notification d'échec d'envoi de l'e-mail de bienvenue envoyée à l'équipe d'administration pour l'utilisateur {}",
                    user.getId());
        } catch (Exception adminNotificationException) {
            logger.error(
                    "Erreur lors de l'envoi de la notification d'échec d'e-mail à l'équipe d'administration pour l'utilisateur {} : {}",
                    user.getId(), adminNotificationException.getMessage(), adminNotificationException);
        }
    }
}