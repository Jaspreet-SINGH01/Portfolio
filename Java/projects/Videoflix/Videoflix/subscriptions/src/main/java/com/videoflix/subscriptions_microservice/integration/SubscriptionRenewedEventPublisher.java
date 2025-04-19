package com.videoflix.subscriptions_microservice.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.videoflix.subscriptions_microservice.entities.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class SubscriptionRenewedEventPublisher {

    private static final Logger logger = LoggerFactory.getLogger(SubscriptionRenewedEventPublisher.class);

    private static final String SUBSCRIPTION_RENEWED_EXCHANGE = "subscription.events";
    private static final String SUBSCRIPTION_RENEWED_ROUTING_KEY = "subscription.renewed";

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    public SubscriptionRenewedEventPublisher(RabbitTemplate rabbitTemplate, ObjectMapper objectMapper) {
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
    }

    public void publishSubscriptionRenewedEvent(Subscription subscription) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("subscriptionId", subscription.getId());
            if (subscription.getUser() != null) {
                payload.put("userId", subscription.getUser().getId());
            } else {
                logger.warn("Utilisateur non trouvé pour l'abonnement renouvelé ID: {}", subscription.getId());
                payload.put("userId", null);
            }
            payload.put("renewalDate", java.time.LocalDateTime.now().toString());
            payload.put("nextBillingDate",
                    subscription.getNextBillingDate() != null ? subscription.getNextBillingDate().toString() : null);
            payload.put("subscriptionLevelId",
                    subscription.getSubscriptionLevel() != null ? subscription.getSubscriptionLevel().getId() : null);
            payload.put("subscriptionLevelName",
                    subscription.getSubscriptionLevel() != null ? subscription.getSubscriptionLevel().getLevel().name()
                            : null);

            String message = objectMapper.writeValueAsString(payload);
            rabbitTemplate.convertAndSend(SUBSCRIPTION_RENEWED_EXCHANGE, SUBSCRIPTION_RENEWED_ROUTING_KEY, message);
            logger.info(
                    "Événement SubscriptionRenewedEvent publié sur l'échange {} avec la clé de routage {}, payload: {}",
                    SUBSCRIPTION_RENEWED_EXCHANGE, SUBSCRIPTION_RENEWED_ROUTING_KEY, payload);
        } catch (Exception e) {
            logger.error("Erreur lors de la publication de l'événement SubscriptionRenewedEvent : {}",
                    e.getMessage(), e);
        }
    }
}