package com.videoflix.users_microservice.services.async;

import com.videoflix.users_microservice.services.EmailService;
import com.videoflix.users_microservice.services.SmsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class NotificationServiceAsync {

    private static EmailService emailService; // Déclaration statique du service email. Potentiellement problématique.
    private final SmsService smsService;
    private static final Logger logger = LoggerFactory.getLogger(NotificationServiceAsync.class);

    /**
     * Constructeur pour NotificationServiceAsync.
     * Injecte le service SmsService.
     *
     * @param smsService Le service pour l'envoi de SMS.
     */
    public NotificationServiceAsync(SmsService smsService) {
        this.smsService = smsService;
    }

    /**
     * Méthode asynchrone pour envoyer un email de bienvenue.
     * Cette méthode est annotée avec @Async("taskExecutor"), ce qui signifie
     * qu'elle est exécutée
     * dans un thread séparé par le taskExecutor Spring.
     *
     * @param to       L'adresse email du destinataire.
     * @param username Le nom d'utilisateur du destinataire.
     */
    @Async("taskExecutor")
    public static void sendWelcomeEmailAsync(String to, String username) {
        try {
            // Envoie l'email de bienvenue en utilisant le service EmailService.
            emailService.sendWelcomeEmail(to, username);
            // Journalise l'envoi réussi de l'email.
            logger.info("Email de bienvenue envoyé à : {}", to);
        } catch (Exception e) {
            // Journalise l'échec de l'envoi de l'email.
            logger.error("Échec de l'envoi de l'email de bienvenue à : {}", to, e);
        }
    }

    /**
     * Méthode asynchrone pour envoyer un code de vérification par SMS.
     * Cette méthode est annotée avec @Async("taskExecutor"), ce qui signifie
     * qu'elle est exécutée
     * dans un thread séparé par le taskExecutor Spring.
     *
     * @param to   Le numéro de téléphone du destinataire.
     * @param code Le code de vérification à envoyer.
     */
    @Async("taskExecutor")
    public void sendVerificationCodeAsync(String to, String code) {
        try {
            // Envoie le code de vérification par SMS en utilisant le service SmsService.
            smsService.sendVerificationCode(to, code);
            // Journalise l'envoi réussi du SMS.
            logger.info("Code de vérification envoyé par SMS à : {}", to);
        } catch (Exception e) {
            // Journalise l'échec de l'envoi du SMS.
            logger.error("Échec de l'envoi du code de vérification par SMS à : {}", to, e);
        }
    }
}