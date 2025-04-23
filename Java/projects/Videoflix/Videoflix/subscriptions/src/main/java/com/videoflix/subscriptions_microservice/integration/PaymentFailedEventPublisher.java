package com.videoflix.subscriptions_microservice.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.videoflix.subscriptions_microservice.entities.Subscription;
import com.videoflix.subscriptions_microservice.entities.User; // Importez l'entité User
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class PaymentFailedEventPublisher {

    private static final Logger logger = LoggerFactory.getLogger(PaymentFailedEventPublisher.class);

    private static final String PAYMENT_FAILED_EXCHANGE = "billing.events";
    private static final String PAYMENT_FAILED_ROUTING_KEY = "billing.payment.failed";

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    public PaymentFailedEventPublisher(RabbitTemplate rabbitTemplate, ObjectMapper objectMapper) {
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
    }

    public void publishPaymentFailedEvent(Subscription subscription, String failureReason) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("subscriptionId", subscription.getId());
            User user = subscription.getUser();
            if (user != null) {
                payload.put("userId", user.getId());
            } else {
                logger.warn("Utilisateur non trouvé pour l'abonnement dont le paiement a échoué ID: {}",
                        subscription.getId());
                payload.put("userId", null);
            }
            payload.put("failureTimestamp", java.time.LocalDateTime.now().toString());
            payload.put("failureReason", failureReason);
            payload.put("nextRetryDate",
                    subscription.getNextRenewalDate() != null ? subscription.getNextRenewalDate().toString() : null);
            payload.put("amountDue", subscription.getPriceId()); // Ou le montant exact qui a échoué
            payload.put("currency", "EUR"); // Ou récupérez la devise de l'abonnement

            String message = objectMapper.writeValueAsString(payload);
            rabbitTemplate.convertAndSend(PAYMENT_FAILED_EXCHANGE, PAYMENT_FAILED_ROUTING_KEY, message);
            logger.warn(
                    "Évènement PaymentFailedEvent publié sur l'échange {} avec la clé de routage {}, payload: {}",
                    PAYMENT_FAILED_EXCHANGE, PAYMENT_FAILED_ROUTING_KEY, payload);
        } catch (Exception e) {
            logger.error("Erreur lors de la publication de l'Évènement PaymentFailedEvent : {}",
                    e.getMessage(), e);
        }
    }
}