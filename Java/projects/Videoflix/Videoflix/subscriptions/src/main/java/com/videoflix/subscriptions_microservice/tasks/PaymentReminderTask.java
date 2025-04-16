package com.videoflix.subscriptions_microservice.tasks;

import com.videoflix.subscriptions_microservice.entities.Subscription;
import com.videoflix.subscriptions_microservice.entities.User;
import com.videoflix.subscriptions_microservice.repositories.SubscriptionRepository;
import com.videoflix.subscriptions_microservice.services.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
public class PaymentReminderTask {

    private static final Logger logger = LoggerFactory.getLogger(PaymentReminderTask.class);

    private final SubscriptionRepository subscriptionRepository;
    private final NotificationService notificationService;

    @Value("${payment.reminder.days-before:3}")
    private int daysBeforeReminder;

    public PaymentReminderTask(SubscriptionRepository subscriptionRepository, NotificationService notificationService) {
        this.subscriptionRepository = subscriptionRepository;
        this.notificationService = notificationService;
    }

    @Scheduled(cron = "0 0 10 * * *") // Runs every day at 10:00 AM
    public void sendPaymentReminders() {
        LocalDate reminderDate = LocalDate.now().plus(daysBeforeReminder, ChronoUnit.DAYS);
        List<Subscription> subscriptionsDueForReminder = subscriptionRepository.findByNextBillingDate(reminderDate);

        logger.info("Traitement de {} abonnements devant recevoir un rappel de paiement pour le {} (dans {} jours).",
                subscriptionsDueForReminder.size(), reminderDate, daysBeforeReminder);

        for (Subscription subscription : subscriptionsDueForReminder) {
            User user = subscription.getUser();
            if (user != null) {
                try {
                    notificationService.sendPaymentReminderNotification(user, subscription, daysBeforeReminder);
                    logger.info(
                            "Rappel de paiement envoyé à l'utilisateur {} (abonnement {}), facturation prévue le {}.",
                            user.getId(), subscription.getId(), subscription.getNextBillingDate());
                } catch (Exception e) {
                    logger.error("Erreur lors de l'envoi du rappel de paiement à l'utilisateur {} (abonnement {}) : {}",
                            user.getId(), subscription.getId(), e.getMessage(), e);
                    // Gérer l'erreur (log, potentiellement enregistrer l'échec pour une révision)
                }
            } else {
                logger.warn(
                        "Utilisateur non trouvé pour l'abonnement {} lors de la tentative d'envoi du rappel de paiement.",
                        subscription.getId());
            }
        }
    }
}