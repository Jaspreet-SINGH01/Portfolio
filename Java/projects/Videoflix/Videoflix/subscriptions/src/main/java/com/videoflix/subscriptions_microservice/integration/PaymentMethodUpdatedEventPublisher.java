package com.videoflix.subscriptions_microservice.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.videoflix.subscriptions_microservice.entities.User; // Importez l'entité User
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class PaymentMethodUpdatedEventPublisher {

    private static final Logger logger = LoggerFactory.getLogger(PaymentMethodUpdatedEventPublisher.class);

    private static final String PAYMENT_METHOD_UPDATED_EXCHANGE = "user.events";
    private static final String PAYMENT_METHOD_UPDATED_ROUTING_KEY = "user.payment.method.updated";

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    public PaymentMethodUpdatedEventPublisher(RabbitTemplate rabbitTemplate, ObjectMapper objectMapper) {
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
    }

    public void publishPaymentMethodUpdatedEvent(User user) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("userId", user.getId());
            payload.put("updateTimestamp", java.time.LocalDateTime.now().toString());
            // Vous pourriez ajouter des informations supplémentaires sur la méthode de
            // paiement mise à jour
            // si nécessaire, mais soyez prudent quant aux informations sensibles.
            // Par exemple, vous pourriez inclure le type de carte (Visa, Mastercard) sans
            // le numéro complet.

            String message = objectMapper.writeValueAsString(payload);
            rabbitTemplate.convertAndSend(PAYMENT_METHOD_UPDATED_EXCHANGE, PAYMENT_METHOD_UPDATED_ROUTING_KEY, message);
            logger.info(
                    "Événement PaymentMethodUpdatedEvent publié sur l'échange {} avec la clé de routage {}, payload: {}",
                    PAYMENT_METHOD_UPDATED_EXCHANGE, PAYMENT_METHOD_UPDATED_ROUTING_KEY, payload);
        } catch (Exception e) {
            logger.error("Erreur lors de la publication de l'événement PaymentMethodUpdatedEvent : {}",
                    e.getMessage(), e);
        }
    }
}