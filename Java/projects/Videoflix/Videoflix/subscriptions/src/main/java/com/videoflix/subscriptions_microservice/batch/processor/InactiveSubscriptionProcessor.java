package com.videoflix.subscriptions_microservice.batch.processor;

import com.videoflix.subscriptions_microservice.entities.Subscription;
import com.videoflix.subscriptions_microservice.services.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Component
public class InactiveSubscriptionProcessor implements ItemProcessor<Subscription, Subscription> {

    private static final Logger logger = LoggerFactory.getLogger(InactiveSubscriptionProcessor.class);
    private final NotificationService notificationService;
    private static final long DAYS_BEFORE_FINAL_NOTIFICATION = 7; // Envoyer une dernière notification 7 jours avant la
                                                                  // suppression

    public InactiveSubscriptionProcessor(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Override
    public Subscription process(Subscription subscription) throws Exception {
        logger.debug("Traitement de l'abonnement inactif ID: {}", subscription.getId());

        if (shouldSendFinalNotification(subscription.getPaymentDate())) {
            try {
                logger.info("Envoi d'une dernière notification avant suppression pour l'abonnement ID: {}",
                        subscription.getId());
                notificationService.sendSubscriptionExpiringNotification(subscription.getUser(), subscription);
            } catch (Exception e) {
                logger.error("Erreur lors de l'envoi de la dernière notification pour l'abonnement ID: {}",
                        subscription.getId(), e);
            }
            return subscription;
        } else {
            logger.debug("L'abonnement inactif ID: {} est éligible pour la suppression.", subscription.getId());
            return subscription;
        }
    }

    private boolean shouldSendFinalNotification(LocalDateTime paymentDate) {
        if (paymentDate == null) {
            return false;
        }

        LocalDateTime notificationCutoff = LocalDateTime.now().minus(DAYS_BEFORE_FINAL_NOTIFICATION + 1,
                ChronoUnit.DAYS);
        LocalDateTime suppressionCutoff = LocalDateTime.now().minus(90, ChronoUnit.DAYS); // La date limite d'inactivité
                                                                                          // pour la suppression (celle
                                                                                          // utilisée dans le reader)

        return paymentDate.isBefore(suppressionCutoff) && paymentDate.isAfter(notificationCutoff);
    }
}