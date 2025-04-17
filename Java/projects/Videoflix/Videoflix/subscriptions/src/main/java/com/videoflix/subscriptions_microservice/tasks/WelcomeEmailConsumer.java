package com.videoflix.subscriptions_microservice.tasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.videoflix.subscriptions_microservice.events.NewSubscriptionCreatedEvent;
import com.videoflix.subscriptions_microservice.services.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

import static com.videoflix.subscriptions_microservice.config.RabbitMQConfig.WELCOME_EMAIL_QUEUE;

@Component
public class WelcomeEmailConsumer {

    private static final Logger logger = LoggerFactory.getLogger(WelcomeEmailConsumer.class);

    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    public WelcomeEmailConsumer(NotificationService notificationService, ObjectMapper objectMapper) {
        this.notificationService = notificationService;
        this.objectMapper = objectMapper;
    }

    @RabbitListener(queues = WELCOME_EMAIL_QUEUE)
    public void handleNewSubscriptionCreatedEvent(String message) {
        logger.info("Message reçu de la queue {} : {}", WELCOME_EMAIL_QUEUE, message);
        try {
            NewSubscriptionCreatedEvent event = objectMapper.readValue(message, NewSubscriptionCreatedEvent.class);
            notificationService.sendWelcomeEmail(event.getUser(), event.getSubscription());
            logger.info("E-mail de bienvenue traité avec succès pour l'utilisateur {}", event.getUser().getId());
        } catch (IOException e) {
            logger.error("Erreur lors de la désérialisation du message : {}", e.getMessage(), e);
            // Gérer l'erreur (log, DLQ - Dead Letter Queue pour les messages qui ne peuvent
            // pas être traités)
        } catch (Exception e) {
            logger.error("Erreur lors de l'envoi de l'e-mail de bienvenue (consommateur) : {}", e.getMessage(), e);
            // Gérer l'erreur (réessayer, DLQ)
        }
    }
}