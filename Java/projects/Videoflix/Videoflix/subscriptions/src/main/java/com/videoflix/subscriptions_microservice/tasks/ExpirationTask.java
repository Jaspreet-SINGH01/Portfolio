package com.videoflix.subscriptions_microservice.tasks;

import com.videoflix.subscriptions_microservice.entities.Subscription;
import com.videoflix.subscriptions_microservice.repositories.SubscriptionRepository;
import com.videoflix.subscriptions_microservice.services.NotificationService; // Hypothetical service
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Component
public class ExpirationTask {

    private static final Logger logger = LoggerFactory.getLogger(ExpirationTask.class);

    private final SubscriptionRepository subscriptionRepository;
    private final NotificationService notificationService; // Inject your notification service

    public ExpirationTask(SubscriptionRepository subscriptionRepository, NotificationService notificationService) {
        this.subscriptionRepository = subscriptionRepository;
        this.notificationService = notificationService;
    }

    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void processExpiredSubscriptions() {
        LocalDate today = LocalDate.now();
        List<Subscription> expiringToday = subscriptionRepository.findByEndDate(today);
        List<Subscription> alreadyExpired = subscriptionRepository.findByEndDateBeforeAndStatusNot(today,
                Subscription.SubscriptionStatus.EXPIRED);

        processExpiringToday(expiringToday);
        processAlreadyExpired(alreadyExpired);
    }

    private void processExpiringToday(List<Subscription> expiringToday) {
        for (Subscription subscription : expiringToday) {
            logger.info("L'abonnement {} de l'utilisateur {} expire aujourd'hui.",
                    subscription.getId(), subscription.getUser().getId());
            // Send notification to user about imminent expiration
            notificationService.sendSubscriptionExpiringNotification(subscription.getUser(), subscription);
        }
    }

    private void processAlreadyExpired(List<Subscription> alreadyExpired) {
        for (Subscription subscription : alreadyExpired) {
            logger.warn("L'abonnement {} de l'utilisateur {} a expiré le {}. Statut actuel : {}",
                    subscription.getId(), subscription.getUser().getId(), subscription.getEndDate(),
                    subscription.getStatus());
            subscription.setStatus(Subscription.SubscriptionStatus.EXPIRED);
            subscriptionRepository.save(subscription);
            logger.info("Statut de l'abonnement {} mis à jour à EXPIRED.", subscription.getId());
            notificationService.sendSubscriptionExpiredNotification(subscription.getUser(), subscription);
        }
    }
}