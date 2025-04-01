package com.videoflix.subscriptions_microservice.tasks;

import com.videoflix.subscriptions_microservice.entities.Subscription;
import com.videoflix.subscriptions_microservice.services.StripePaymentService;
import com.videoflix.subscriptions_microservice.services.SubscriptionService;
import com.stripe.exception.StripeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class RenewalTask {

    private final SubscriptionService subscriptionService;
    private final StripePaymentService stripePaymentService;
    private static final Logger logger = LoggerFactory.getLogger(RenewalTask.class);

    public RenewalTask(SubscriptionService subscriptionService, StripePaymentService stripePaymentService) {
        this.subscriptionService = subscriptionService;
        this.stripePaymentService = stripePaymentService;
    }

    @Scheduled(cron = "0 0 0 * * *") // Exécute tous les jours à minuit
    public void checkAndRenewSubscriptions() {
        logger.info("Début de la tâche de renouvellement des abonnements.");
        List<Subscription> subscriptions = subscriptionService.getAllSubscriptions();
        LocalDateTime now = LocalDateTime.now();

        for (Subscription subscription : subscriptions) {
            if (subscription.isAutoRenew() && subscription.getNextRenewalDate() != null
                    && subscription.getNextRenewalDate().isBefore(now)) {
                try {
                    // Renouveler l'abonnement via Stripe
                    Subscription renewedSubscription = stripePaymentService
                            .createStripeSubscription(subscription);

                    // Mettre à jour l'abonnement en base de données
                    subscription.setStripeSubscriptionId(renewedSubscription.getStripeSubscriptionId());
                    subscription.setNextRenewalDate(now.plusMonths(1)); // Exemple : renouvellement mensuel
                    subscriptionService.updateSubscription(subscription.getId(), subscription);

                    logger.info("Abonnement renouvelé avec succès : {}", subscription.getId());
                } catch (StripeException e) {
                    logger.error("Erreur lors du renouvellement de l'abonnement : {}", subscription.getId(), e);
                    // Gérer l'erreur (par exemple, envoyer une notification à l'utilisateur)
                }
            }
        }
        logger.info("Fin de la tâche de renouvellement des abonnements.");
    }
}