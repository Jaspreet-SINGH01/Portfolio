package com.videoflix.subscriptions_microservice.controllers;

import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.Invoice;
import com.stripe.model.StripeObject;
import com.stripe.net.Webhook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
public class StripeWebhookController {

    private static final Logger logger = LoggerFactory.getLogger(StripeWebhookController.class);

    @Value("${stripe.webhook.secret}")
    private String webhookSecret;

    public StripeWebhookController(@Value("${stripe.api.secretKey}") String secretKey) {
        Stripe.apiKey = secretKey;
    }

    @PostMapping("/stripe/webhook")
    public ResponseEntity<String> handleStripeWebhook(
            @RequestBody String payload,
            @RequestHeader(value = "Stripe-Signature", required = false) String sigHeader) {

        if (sigHeader == null) {
            logger.error("Webhook signature missing");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Webhook signature missing");
        }

        try {
            Event event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
            EventDataObjectDeserializer dataObjectDeserializer = event.getDataObjectDeserializer();
            Optional<StripeObject> stripeObjectOptional = dataObjectDeserializer.getObject();

            if (stripeObjectOptional.isPresent()) {
                handleEvent(event.getType(), stripeObjectOptional.get());
            } else {
                logger.warn("Failed to deserialize event object: {}", event.getId());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid payload");
            }

            return ResponseEntity.ok("Webhook processed successfully");

        } catch (SignatureVerificationException e) {
            logger.error("Webhook signature verification failed", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Signature verification failed");
        } catch (Exception e) {
            logger.error("Error processing webhook", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing webhook");
        }
    }

    private void handleEvent(String eventType, StripeObject stripeObject) {
        switch (eventType) {
            case "invoice.payment_succeeded":
                if (stripeObject instanceof Invoice invoice) {
                    logger.info("Invoice payment succeeded: {}", invoice.getId());
                    // Logique de traitement pour les paiements réussis
                }
                break;

            case "invoice.payment_failed":
                if (stripeObject instanceof Invoice invoice) {
                    logger.error("Invoice payment failed: {}", invoice.getId());
                    // Logique de traitement pour les paiements échoués
                }
                break;

            case "customer.subscription.created":
                logger.info("Subscription created: {}", stripeObject);
                // Logique pour les abonnements créés
                break;

            case "customer.subscription.updated":
                logger.info("Subscription updated: {}", stripeObject);
                // Logique pour les abonnements mis à jour
                break;

            case "customer.subscription.deleted":
                logger.info("Subscription deleted: {}", stripeObject);
                // Logique pour les abonnements supprimés
                break;

            case "customer.subscription.trial_will_end":
                logger.info("Trial will end for subscription: {}", stripeObject);
                // Notification de la fin de l'essai gratuit
                break;

            default:
                logger.info("Unhandled event type: {}", eventType);
        }
    }
}