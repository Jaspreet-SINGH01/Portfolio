package com.videoflix.subscriptions_microservice.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.videoflix.subscriptions_microservice.entities.User;
import com.videoflix.subscriptions_microservice.services.UserService;
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
    private final UserService userService;

    private static final String USER_ID_KEY = "userId";

    public PaymentFailedEventPublisher(RabbitTemplate rabbitTemplate, ObjectMapper objectMapper,
            UserService userService) {
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
        this.userService = userService;
    }

    private User findUserByStripeCustomerId(String customerId) {
        return userService.findByStripeCustomerId(customerId);
    }

    public void publishPaymentFailedEvent(com.stripe.model.Subscription subscription, String failureReason) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("subscriptionId", subscription.getId());
            String customerId = subscription.getCustomer();

            if (customerId != null) {
                User user = findUserByStripeCustomerId(customerId);
                if (user != null) {
                    payload.put(USER_ID_KEY, user.getId());
                } else {
                    logger.warn("Utilisateur non trouvé pour le client Stripe: {}", customerId);
                    payload.put(USER_ID_KEY, null);
                }
            } else {
                logger.warn("Aucun client trouvé pour l'abonnement dont le paiement a échoué ID: {}",
                        subscription.getId());
                payload.put(USER_ID_KEY, null);
            }

            payload.put("failureTimestamp", java.time.LocalDateTime.now().toString());
            payload.put("failureReason", failureReason);
            payload.put("nextRetryDate",
                    subscription.getCurrentPeriodEnd() != null
                            ? new java.util.Date(subscription.getCurrentPeriodEnd() * 1000L).toString()
                            : null);
            payload.put("amountDue", subscription.getId()); // Ou le montant exact qui a échoué
            payload.put("currency", "EUR"); // Ou récupérer la devise de l'abonnement

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