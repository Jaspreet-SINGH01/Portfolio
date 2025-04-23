package com.videoflix.subscriptions_microservice.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.videoflix.subscriptions_microservice.entities.Subscription;
import com.videoflix.subscriptions_microservice.entities.SubscriptionLevel; // Importez l'entité SubscriptionLevel
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class SubscriptionLevelChangedEventPublisher {

    private static final Logger logger = LoggerFactory.getLogger(SubscriptionLevelChangedEventPublisher.class);

    private static final String SUBSCRIPTION_LEVEL_CHANGED_EXCHANGE = "subscription.events";
    private static final String SUBSCRIPTION_LEVEL_CHANGED_ROUTING_KEY = "subscription.level.changed";

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    public SubscriptionLevelChangedEventPublisher(RabbitTemplate rabbitTemplate, ObjectMapper objectMapper) {
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
    }

    public void publishSubscriptionLevelChangedEvent(Subscription subscription, String oldLevelString) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("subscriptionId", subscription.getId());
            if (subscription.getUser() != null) {
                payload.put("userId", subscription.getUser().getId()); // Ajouter l'ID de l'utilisateur
            } else {
                logger.warn("Utilisateur non trouvé pour l'abonnement ID: {}", subscription.getId());
                payload.put("userId", null);
            }
            SubscriptionLevel newLevel = subscription.getSubscriptionLevel();
            if (newLevel != null) {
                payload.put("newLevelId", newLevel.getId());
                payload.put("newLevelName", newLevel.getLevel().name());
            } else {
                logger.warn("Nouveau niveau d'abonnement non trouvé pour l'abonnement ID: {}", subscription.getId());
                payload.put("newLevelId", null);
                payload.put("newLevelName", null);
            }
            payload.put("oldLevel", oldLevelString);
            payload.put("changeTimestamp", java.time.LocalDateTime.now().toString()); // Ajouter un horodatage
            String message = objectMapper.writeValueAsString(payload);
            rabbitTemplate.convertAndSend(SUBSCRIPTION_LEVEL_CHANGED_EXCHANGE, SUBSCRIPTION_LEVEL_CHANGED_ROUTING_KEY,
                    message);
            logger.info(
                    "Évènement SubscriptionLevelChangedEvent publié sur l'échange {} avec la clé de routage {}, payload: {}",
                    SUBSCRIPTION_LEVEL_CHANGED_EXCHANGE, SUBSCRIPTION_LEVEL_CHANGED_ROUTING_KEY, payload);
        } catch (Exception e) {
            logger.error("Erreur lors de la publication de l'Évènement SubscriptionLevelChangedEvent : {}",
                    e.getMessage(), e);
        }
    }
}