package com.videoflix.subscriptions_microservice.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Component
public class AccessControlEventPublisher {

    private static final Logger logger = LoggerFactory.getLogger(AccessControlEventPublisher.class);
    private static final String ACCESS_CONTROL_EXCHANGE = "access.control.events"; // Définir l'échange
    private static final String SUBSCRIPTION_REACTIVATED_ROUTING_KEY = "subscription.reactivated"; // Définir la clé de
                                                                                                   // routage
                                                                                                   // pour la
                                                                                                   // réactivation
    private static final String SUBSCRIPTION_CANCELLED_ROUTING_KEY = "subscription.cancelled"; // Définir la clé
                                                                                               // de routage pour
                                                                                               // l'annulation

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    public AccessControlEventPublisher(RabbitTemplate rabbitTemplate, ObjectMapper objectMapper) {
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
    }

    public void publishSubscriptionReactivatedForAccessControl(Long userId, String subscriptionLevelName) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("userId", userId);
            payload.put("subscriptionLevel", subscriptionLevelName);
            payload.put("reactivatedAt", LocalDateTime.now().toString());

            String message = objectMapper.writeValueAsString(payload);
            rabbitTemplate.convertAndSend(ACCESS_CONTROL_EXCHANGE, SUBSCRIPTION_REACTIVATED_ROUTING_KEY, message);
            logger.info(
                    "Événement SubscriptionReactivatedForAccessControl publié sur l'échange {} avec la clé de routage {}, payload: {}",
                    ACCESS_CONTROL_EXCHANGE, SUBSCRIPTION_REACTIVATED_ROUTING_KEY, payload);

        } catch (Exception e) {
            logger.error("Erreur lors de la publication de l'événement SubscriptionReactivatedForAccessControl : {}",
                    e.getMessage(), e);
        }
    }

    public void publishSubscriptionCancelledForAccessControl(Long userId, String subscriptionLevelName, String reason) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("userId", userId);
            payload.put("subscriptionLevel", subscriptionLevelName);
            payload.put("cancelledAt", LocalDateTime.now().toString());
            payload.put("reason", reason);

            String message = objectMapper.writeValueAsString(payload);
            rabbitTemplate.convertAndSend(ACCESS_CONTROL_EXCHANGE, SUBSCRIPTION_CANCELLED_ROUTING_KEY, message);
            logger.info(
                    "Événement SubscriptionCancelledForAccessControl publié sur l'échange {} avec la clé de routage {}, payload: {}",
                    ACCESS_CONTROL_EXCHANGE, SUBSCRIPTION_CANCELLED_ROUTING_KEY, payload);

        } catch (Exception e) {
            logger.error("Erreur lors de la publication de l'événement SubscriptionCancelledForAccessControl : {}",
                    e.getMessage(), e);
        }
    }
}