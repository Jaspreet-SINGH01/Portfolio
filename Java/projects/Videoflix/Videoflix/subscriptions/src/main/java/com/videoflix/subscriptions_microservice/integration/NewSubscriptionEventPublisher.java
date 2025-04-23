package com.videoflix.subscriptions_microservice.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.videoflix.subscriptions_microservice.events.NewSubscriptionCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import static com.videoflix.subscriptions_microservice.config.RabbitMQConfig.*;

@Component
public class NewSubscriptionEventPublisher {

    private static final Logger logger = LoggerFactory.getLogger(NewSubscriptionEventPublisher.class);

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    public NewSubscriptionEventPublisher(RabbitTemplate rabbitTemplate, ObjectMapper objectMapper) {
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
    }

    public void publishNewSubscriptionEvent(NewSubscriptionCreatedEvent event) {
        try {
            String message = objectMapper.writeValueAsString(event);
            rabbitTemplate.convertAndSend(NEW_SUBSCRIPTION_EXCHANGE, WELCOME_EMAIL_ROUTING_KEY, message);
            logger.info("Évènement NewSubscriptionCreatedEvent publié sur l'échange {} avec la clé de routage {}",
                    NEW_SUBSCRIPTION_EXCHANGE, WELCOME_EMAIL_ROUTING_KEY);
        } catch (Exception e) {
            logger.error("Erreur lors de la publication de l'Évènement NewSubscriptionCreatedEvent : {}",
                    e.getMessage(), e);
            // Gérer l'erreur (log, potentiellement un mécanisme de réessai de publication)
        }
    }
}