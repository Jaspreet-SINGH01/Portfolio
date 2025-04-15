package com.videoflix.subscriptions_microservice.services;

import com.videoflix.subscriptions_microservice.entities.Subscription;
import com.videoflix.subscriptions_microservice.entities.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Service responsable de l'envoi des notifications par email aux utilisateurs
 * concernant leurs abonnements.
 */
@Service
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);
    private static final String NULL_PARAMS_ERROR = "Impossible d'envoyer la notification: utilisateur ou abonnement null";

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String notificationSender;

    /**
     * Constructeur du service de notification
     * 
     * @param mailSender Service d'envoi d'emails injecté par Spring
     */
    public NotificationService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * Envoie une notification à l'utilisateur lorsque son abonnement est sur le
     * point d'expirer
     * 
     * @param user         L'utilisateur concerné
     * @param subscription L'abonnement qui va expirer
     */
    public void sendSubscriptionExpiringNotification(User user, Subscription subscription) {
        if (user == null || subscription == null) {
            logger.error(NULL_PARAMS_ERROR);
            return;
        }

        if (user.getEmail() == null || user.getEmail().isEmpty()) {
            logger.warn(
                    "Impossible d'envoyer la notification d'expiration imminente à l'utilisateur {} car l'adresse e-mail est manquante.",
                    user.getId());
            return;
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(notificationSender);
        message.setTo(user.getEmail());
        message.setSubject("Votre abonnement Videoflix expire bientôt !");
        message.setText(String.format("""
                Cher %s,

                Votre abonnement %s expire le %s.
                Pour continuer à profiter de Videoflix sans interruption, veuillez renouveler votre abonnement.

                Merci,
                L'équipe Videoflix""",
                user.getFirstname(), subscription.getSubscriptionLevel(), subscription.getEndDate()));

        try {
            mailSender.send(message);
            logger.info("Notification d'expiration imminente envoyée à l'utilisateur {}", user.getId());
        } catch (Exception e) {
            logger.error("Erreur lors de l'envoi de la notification d'expiration imminente à l'utilisateur {}",
                    user.getId(), e);
        }
    }

    /**
     * Envoie une notification à l'utilisateur lorsque son abonnement a expiré
     * 
     * @param user         L'utilisateur concerné
     * @param subscription L'abonnement qui a expiré
     */
    public void sendSubscriptionExpiredNotification(User user, Subscription subscription) {
        if (user == null || subscription == null) {
            logger.error(NULL_PARAMS_ERROR);
            return;
        }

        if (user.getEmail() == null || user.getEmail().isEmpty()) {
            logger.warn(
                    "Impossible d'envoyer la notification d'expiration à l'utilisateur {} car l'adresse e-mail est manquante.",
                    user.getId());
            return;
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(notificationSender);
        message.setTo(user.getEmail());
        message.setSubject("Votre abonnement Videoflix a expiré");
        message.setText(String.format(
                """
                        Cher %s,

                        Votre abonnement %s a expiré le %s.
                        Pour retrouver l'accès à tout le contenu de Videoflix, veuillez renouveler votre abonnement dès aujourd'hui.

                        Merci,
                        L'équipe Videoflix""",
                user.getFirstname(), subscription.getSubscriptionLevel(), subscription.getEndDate()));

        try {
            mailSender.send(message);
            logger.info("Notification d'expiration envoyée à l'utilisateur {}", user.getId());
        } catch (Exception e) {
            logger.error("Erreur lors de l'envoi de la notification d'expiration à l'utilisateur {}", user.getId(),
                    e);
        }
    }

    /**
     * Envoie une notification à l'utilisateur lorsque sa période d'essai est sur le
     * point de se terminer
     * 
     * @param user         L'utilisateur concerné
     * @param subscription L'abonnement en période d'essai
     */
    public void sendTrialPeriodEndingNotification(User user, Subscription subscription) {
        if (user == null || subscription == null) {
            logger.error(NULL_PARAMS_ERROR);
            return;
        }

        if (user.getEmail() == null || user.getEmail().isEmpty()) {
            logger.warn(
                    "Impossible d'envoyer la notification de fin de période d'essai imminente à l'utilisateur {} car l'adresse e-mail est manquante.",
                    user.getId());
            return;
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(notificationSender);
        message.setTo(user.getEmail());
        message.setSubject("Votre période d'essai Videoflix se termine bientôt !");
        message.setText(String.format(
                """
                        Cher %s,

                        Votre période d'essai pour l'abonnement %s se termine le %s.
                        Profitez-en au maximum jusqu'à la fin !

                        Pour continuer à bénéficier de Videoflix sans interruption, vous pouvez souscrire à un plan payant dès maintenant.

                        Merci,
                        L'équipe Videoflix""",
                user.getFirstname(), subscription.getSubscriptionLevel(), subscription.getTrialEndDate()));

        try {
            mailSender.send(message);
            logger.info("Notification de fin de période d'essai imminente envoyée à l'utilisateur {}",
                    user.getId());
        } catch (Exception e) {
            logger.error(
                    "Erreur lors de l'envoi de la notification de fin de période d'essai imminente à l'utilisateur {}",
                    user.getId(), e);
        }
    }

    /**
     * Envoie une notification à l'utilisateur lorsque sa période d'essai est
     * terminée
     * 
     * @param user         L'utilisateur concerné
     * @param subscription L'abonnement dont la période d'essai est terminée
     */
    public void sendTrialPeriodEndedNotification(User user, Subscription subscription) {
        if (user == null || subscription == null) {
            logger.error(NULL_PARAMS_ERROR);
            return;
        }

        if (user.getEmail() == null || user.getEmail().isEmpty()) {
            logger.warn(
                    "Impossible d'envoyer la notification de fin de période d'essai à l'utilisateur {} car l'adresse e-mail est manquante.",
                    user.getId());
            return;
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(notificationSender);
        message.setTo(user.getEmail());
        message.setSubject("Votre période d'essai Videoflix est terminée");
        message.setText(String.format("""
                Cher %s,

                Votre période d'essai pour l'abonnement %s s'est terminée le %s.
                Nous espérons que vous avez apprécié votre essai !

                Pour continuer à accéder à tout notre contenu, veuillez souscrire à un plan payant dès aujourd'hui.

                Merci,
                L'équipe Videoflix""",
                user.getFirstname(), subscription.getSubscriptionLevel(), subscription.getTrialEndDate()));

        try {
            mailSender.send(message);
            logger.info("Notification de fin de période d'essai envoyée à l'utilisateur {}", user.getId());
        } catch (Exception e) {
            logger.error("Erreur lors de l'envoi de la notification de fin de période d'essai à l'utilisateur {}",
                    user.getId(), e);
        }
    }

    // Méthode pour envoyer l'e-mail de bienvenue
    public void sendWelcomeEmail(User user, Subscription subscription) {
        if (user.getEmail() != null) {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(notificationSender);
            message.setTo(user.getEmail());
            message.setSubject("Bienvenue chez Videoflix !");
            message.setText(String.format(
                    "Cher %s,\n\nBienvenue chez Videoflix ! Vous avez souscrit à l'abonnement %s.\n" +
                            "Profitez de notre vaste bibliothèque de contenu !\n\n" +
                            "Voici quelques liens utiles :\n" +
                            "- [Lien vers votre compte]\n" +
                            "- [Lien vers notre catalogue]\n" +
                            "- [Lien vers la FAQ ou l'aide]\n\n" +
                            "Merci de nous rejoindre,\nL'équipe Videoflix",
                    user.getFirstname(), subscription.getSubscriptionLevel()));

            try {
                mailSender.send(message);
                logger.info("E-mail de bienvenue envoyé à l'utilisateur {}", user.getId());
            } catch (Exception e) {
                logger.error("Erreur lors de l'envoi de l'e-mail de bienvenue à l'utilisateur {}", user.getId(), e);
            }
        } else {
            logger.warn(
                    "Impossible d'envoyer l'e-mail de bienvenue à l'utilisateur {} car l'adresse e-mail est manquante.",
                    user.getId());
        }
    }
}