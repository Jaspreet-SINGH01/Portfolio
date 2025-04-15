package com.videoflix.subscriptions_microservice.services;

import com.videoflix.subscriptions_microservice.entities.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminNotificationService {

    private static final Logger logger = LoggerFactory.getLogger(AdminNotificationService.class);

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String notificationSender;

    @Value("${admin.notification.emails}")
    private List<String> adminEmails; // Liste des adresses e-mail des administrateurs

    public AdminNotificationService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void notifyAdminEmailSendFailure(User user, String emailType, String errorMessage) {
        if (adminEmails != null && !adminEmails.isEmpty()) {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(notificationSender);
            message.setTo(adminEmails.toArray(new String[0])); // Convertir la liste en tableau de String
            message.setSubject("[ALERTE] Échec d'envoi d'e-mail utilisateur");
            message.setText(String.format(
                    """
                            L'envoi de l'e-mail de type '%s' à l'utilisateur %s (ID: %d, Email: %s) a échoué après plusieurs tentatives.

                            Message d'erreur : %s

                            Veuillez vérifier les logs pour plus de détails.""",
                    emailType, user.getFirstname() + " " + user.getLastname(), user.getId(), user.getEmail(),
                    errorMessage));

            try {
                mailSender.send(message);
                logger.info(
                        "Notification d'échec d'envoi d'e-mail envoyée à l'équipe d'administration pour l'utilisateur {}",
                        user.getId());
            } catch (Exception e) {
                logger.error(
                        "Erreur lors de l'envoi de la notification d'échec d'e-mail à l'équipe d'administration pour l'utilisateur {} : {}",
                        user.getId(), e);
            }
        } else {
            logger.warn(
                    "Aucune adresse e-mail d'administrateur configurée pour les notifications d'échec d'envoi d'e-mail.");
        }
    }
}