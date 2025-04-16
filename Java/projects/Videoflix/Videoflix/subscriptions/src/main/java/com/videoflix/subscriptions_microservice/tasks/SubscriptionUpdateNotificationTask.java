package com.videoflix.subscriptions_microservice.tasks;

import com.videoflix.subscriptions_microservice.entities.Subscription;
import com.videoflix.subscriptions_microservice.entities.User;
import com.videoflix.subscriptions_microservice.services.NotificationService;
import com.videoflix.subscriptions_microservice.repositories.SubscriptionRepository; // Si nécessaire pour récupérer des infos supplémentaires
import com.videoflix.subscriptions_microservice.repositories.UserRepository; // Si nécessaire pour récupérer l'utilisateur
import com.videoflix.subscriptions_microservice.events.PaymentInfoUpdatedEvent;
import com.videoflix.subscriptions_microservice.events.SubscriptionLevelChangedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;


@Component
public class SubscriptionUpdateNotificationTask {

    private static final Logger logger = LoggerFactory.getLogger(SubscriptionUpdateNotificationTask.class);

    private final NotificationService notificationService;
    private final SubscriptionRepository subscriptionRepository; // Injection si nécessaire
    private final UserRepository userRepository; // Injection si nécessaire

    public SubscriptionUpdateNotificationTask(NotificationService notificationService,
                                              SubscriptionRepository subscriptionRepository,
                                              UserRepository userRepository) {
        this.notificationService = notificationService;
        this.subscriptionRepository = subscriptionRepository;
        this.userRepository = userRepository;
    }

    @Async
    @EventListener
    public void handleSubscriptionLevelChangedEvent(SubscriptionLevelChangedEvent event) {
        Subscription subscription = event.getSubscription();
        User user = event.getUser(); // L'événement devrait contenir l'utilisateur

        logger.info("Réception de l'événement de changement de niveau d'abonnement pour l'utilisateur {} (abonnement {}).",
                    user.getId(), subscription.getId());

        try {
            notificationService.sendSubscriptionLevelChangedNotification(user, subscription, event.getOldLevel());
            logger.info("Notification de changement de niveau d'abonnement envoyée à l'utilisateur {}", user.getId());
        } catch (Exception e) {
            logger.error("Erreur lors de l'envoi de la notification de changement de niveau d'abonnement à l'utilisateur {} : {}",
                        user.getId(), e.getMessage(), e);
            // Gérer l'erreur (log, réessai si nécessaire)
        }
    }

    @Async
    @EventListener
    public void handlePaymentInfoUpdatedEvent(PaymentInfoUpdatedEvent event) {
        Subscription subscription = event.getSubscription();
        User user = event.getUser(); // L'événement devrait contenir l'utilisateur

        logger.info("Réception de l'événement de mise à jour des informations de paiement pour l'utilisateur {} (abonnement {}).",
                    user.getId(), subscription.getId());

        try {
            notificationService.sendPaymentInfoUpdatedNotification(user); // L'événement contient déjà l'utilisateur
            logger.info("Notification de mise à jour des informations de paiement envoyée à l'utilisateur {}", user.getId());
        } catch (Exception e) {
            logger.error("Erreur lors de l'envoi de la notification de mise à jour des informations de paiement à l'utilisateur {} : {}",
                        user.getId(), e.getMessage(), e);
            // Gérer l'erreur (log, réessai si nécessaire)
        }
    }

    // Ajouter d'autres méthodes @EventListener pour d'autres types de mises à jour d'abonnement
    // (par exemple, date de renouvellement modifiée, abonnement annulé par l'utilisateur, etc.)
}