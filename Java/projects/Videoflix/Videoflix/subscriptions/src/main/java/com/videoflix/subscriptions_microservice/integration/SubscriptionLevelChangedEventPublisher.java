package com.videoflix.subscriptions_microservice.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.videoflix.subscriptions_microservice.entities.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

import static com.videoflix.subscriptions_microservice.config.RabbitMQConfig.SUBSCRIPTION_LEVEL_CHANGED_EXCHANGE;
import static com.videoflix.subscriptions_microservice.config.RabbitMQConfig.SUBSCRIPTION_LEVEL_CHANGED_ROUTING_KEY;

@Component
public class SubscriptionLevelChangedEventPublisher {

    private static final Logger logger = LoggerFactory.getLogger(SubscriptionLevelChangedEventPublisher.class);

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    public SubscriptionLevelChangedEventPublisher(RabbitTemplate rabbitTemplate, ObjectMapper objectMapper) {
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
    }

    public void publishSubscriptionLevelChangedEvent(Subscription subscription, String oldLevel) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("subscriptionId", subscription.getId());
            payload.put("userId", subscription.getUser().getId()); // Ajouter l'ID de l'utilisateur
            payload.put("newLevel", subscription.getSubscriptionType());
            payload.put("oldLevel", oldLevel);
            payload.put("changeTimestamp", java.time.LocalDateTime.now().toString()); // Ajouter un horodatage
            String message = objectMapper.writeValueAsString(payload);
            rabbitTemplate.convertAndSend(SUBSCRIPTION_LEVEL_CHANGED_EXCHANGE, SUBSCRIPTION_LEVEL_CHANGED_ROUTING_KEY, message);
            logger.info("Événement SubscriptionLevelChangedEvent publié sur l'échange {} avec la clé de routage {}, payload: {}",
                        SUBSCRIPTION_LEVEL_CHANGED_EXCHANGE, SUBSCRIPTION_LEVEL_CHANGED_ROUTING_KEY, payload);
        } catch (Exception e) {
            logger.error("Erreur lors de la publication de l'événement SubscriptionLevelChangedEvent : {}", e.getMessage(), e);
        }
    }
}