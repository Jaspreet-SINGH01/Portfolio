package com.videoflix.subscriptions_microservice.tasks;

import com.videoflix.subscriptions_microservice.entities.Subscription;
import com.videoflix.subscriptions_microservice.entities.User;
import com.videoflix.subscriptions_microservice.services.NotificationService;
import com.videoflix.subscriptions_microservice.services.EmailService;
import com.videoflix.subscriptions_microservice.repositories.SubscriptionRepository; // Si nécessaire pour récupérer des infos supplémentaires
import com.videoflix.subscriptions_microservice.repositories.UserRepository; // Si nécessaire pour récupérer l'utilisateur
import com.videoflix.subscriptions_microservice.events.PaymentInfoUpdatedEvent;
import com.videoflix.subscriptions_microservice.events.SubscriptionLevelChangedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.mail.javamail.JavaMailSender;

@Component
public class SubscriptionUpdateNotificationTask {

    private static final Logger logger = LoggerFactory.getLogger(SubscriptionUpdateNotificationTask.class);

    @SuppressWarnings("unused")
    private final NotificationService notificationService;

    @SuppressWarnings("unused")
    private final SubscriptionRepository subscriptionRepository;

    @SuppressWarnings("unused")
    private final UserRepository userRepository;

    private final EmailService emailService;

    public SubscriptionUpdateNotificationTask(NotificationService notificationService,
            SubscriptionRepository subscriptionRepository,
            UserRepository userRepository,
            JavaMailSender mailSender) {
        this.notificationService = notificationService;
        this.subscriptionRepository = subscriptionRepository;
        this.userRepository = userRepository;
        this.emailService = new EmailService(mailSender);
    }

    @Async
    @EventListener
    public void handleSubscriptionLevelChangedEvent(SubscriptionLevelChangedEvent event) {
        Subscription subscription = event.getSubscription();
        User user = event.getUser(); // L'Évènement devrait contenir l'utilisateur

        logger.info(
                "Réception de l'Évènement de changement de niveau d'abonnement pour l'utilisateur {} (abonnement {}).",
                user.getId(), subscription.getId());
        try {
            emailService.sendSubscriptionNotification(user.getEmail(),
                    "Changement de niveau d'abonnement",
                    """
                            Cher %s,

                            Votre abonnement a changé du niveau %s au niveau %s.

                            Cordialement,
                            L'équipe Videoflix""".formatted(user.getFirstname(), event.getOldLevel(),
                            subscription.getSubscriptionLevel()));
            logger.info("Notification de changement de niveau d'abonnement envoyée à l'utilisateur {}", user.getId());
        } catch (Exception e) {
            logger.error(
                    "Erreur lors de l'envoi de la notification de changement de niveau d'abonnement à l'utilisateur {} : {}",
                    user.getId(), e.getMessage(), e);
            // Gérer l'erreur (log, réessai si nécessaire)
        }
    }

    @Async
    @EventListener
    public void handlePaymentInfoUpdatedEvent(PaymentInfoUpdatedEvent event) {
        Subscription subscription = event.getSubscription();
        User user = event.getUser(); // L'Évènement devrait contenir l'utilisateur

        logger.info(
                "Réception de l'Évènement de mise à jour des informations de paiement pour l'utilisateur {} (abonnement {}).",
                user.getId(), subscription.getId());

        try {
            emailService.sendSubscriptionNotification(user.getEmail(),
                    "Mise à jour des informations de paiement",
                    "Cher " + user.getFirstname()
                            + ",\n\nVos informations de paiement ont été mises à jour avec succès.\n\nCordialement,\nL'équipe Videoflix");
            logger.info("Notification de mise à jour des informations de paiement envoyée à l'utilisateur {}",
                    user.getId());
        } catch (Exception e) {
            logger.error(
                    "Erreur lors de l'envoi de la notification de mise à jour des informations de paiement à l'utilisateur {} : {}",
                    user.getId(), e.getMessage(), e);
            // Gérer l'erreur (log, réessai si nécessaire)
        }
    }
}