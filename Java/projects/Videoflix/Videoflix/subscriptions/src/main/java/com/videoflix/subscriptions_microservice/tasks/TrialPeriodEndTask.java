package com.videoflix.subscriptions_microservice.tasks;

import com.videoflix.subscriptions_microservice.entities.Subscription;
import com.videoflix.subscriptions_microservice.repositories.SubscriptionRepository;
import com.videoflix.subscriptions_microservice.services.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class TrialPeriodEndTask {

    private static final Logger logger = LoggerFactory.getLogger(TrialPeriodEndTask.class);

    private final SubscriptionRepository subscriptionRepository;
    private final NotificationService notificationService;

    // Injection des dépendances pour le repository des abonnements et le service de
    // notification
    public TrialPeriodEndTask(SubscriptionRepository subscriptionRepository, NotificationService notificationService) {
        this.subscriptionRepository = subscriptionRepository;
        this.notificationService = notificationService;
    }

    // Planification de l'exécution de cette tâche tous les jours à 4h00 du matin
    @Scheduled(cron = "0 0 4 * * *")
    @Transactional
    public void processTrialPeriodEnd() {
        LocalDateTime today = LocalDateTime.now();
        // Récupère la liste des abonnements dont la date de fin de période d'essai est
        // aujourd'hui et dont le statut est toujours en période d'essai
        List<Subscription> trialsEndingToday = subscriptionRepository.findByTrialEndDate(today);
        // Récupère la liste des abonnements dont la date de fin de période d'essai est
        // passée et dont le statut est toujours en période d'essai
        List<Subscription> trialsAlreadyEnded = subscriptionRepository.findByTrialEndDateBeforeAndStatus(today,
                Subscription.SubscriptionStatus.TRIAL);

        processTrialsEndingToday(trialsEndingToday);
        processTrialsAlreadyEnded(trialsAlreadyEnded);
    }

    // Traitement des abonnements dont la période d'essai se termine aujourd'hui
    private void processTrialsEndingToday(List<Subscription> trialsEndingToday) {
        for (Subscription subscription : trialsEndingToday) {
            logger.info("La période d'essai de l'abonnement {} de l'utilisateur {} se termine aujourd'hui.",
                    subscription.getId(), subscription.getUser().getId());
            // Envoi d'une notification à l'utilisateur pour l'informer de la fin de sa
            // période d'essai et des prochaines étapes
            notificationService.sendTrialPeriodEndingNotification(subscription.getUser(), subscription);
        }
    }

    // Traitement des abonnements dont la période d'essai est déjà terminée
    private void processTrialsAlreadyEnded(List<Subscription> trialsAlreadyEnded) {
        for (Subscription subscription : trialsAlreadyEnded) {
            logger.warn(
                    "La période d'essai de l'abonnement {} de l'utilisateur {} s'est terminée le {}. Statut actuel : {}",
                    subscription.getId(), subscription.getUser().getId(), subscription.getTrialEndDate(),
                    subscription.getStatus());
            // Mise à jour du statut de l'abonnement pour indiquer que la période d'essai
            // est terminée (par exemple, INACTIVE ou en attente de paiement)
            subscription.setStatus(Subscription.SubscriptionStatus.TRIAL_ENDED);
            subscriptionRepository.save(subscription);
            logger.info("Statut de l'abonnement {} mis à jour à TRIAL_ENDED.", subscription.getId());
            // Envoi d'une notification à l'utilisateur pour l'informer de la fin de sa
            // période d'essai
            notificationService.sendTrialPeriodEndedNotification(subscription.getUser(), subscription);
        }
    }
}