package com.videoflix.users_microservice.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendWelcomeEmail(String to, String username) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Bienvenue sur Videoflix !");
        message.setText("Cher " + username + ",\n\nBienvenue sur Videoflix !");

        try {
            mailSender.send(message);
            logger.info("Email de bienvenue envoyé à : {}", to);
        } catch (MailSendException e) {
            logger.error("Échec de l'envoi de l'email à : {}. Erreur spécifique : {}", to, e.getMessage(), e);
        } catch (MailException e) {
            logger.error("Erreur générale lors de l'envoi de l'email à : {}", to, e);
        } catch (Exception e) {
            logger.error("Erreur inattendue lors de l'envoi de l'email à : {}", to, e);
        }

        mailSender.send(message);
    }
}