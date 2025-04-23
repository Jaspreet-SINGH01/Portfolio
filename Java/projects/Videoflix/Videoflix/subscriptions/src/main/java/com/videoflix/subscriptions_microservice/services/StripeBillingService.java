package com.videoflix.subscriptions_microservice.services;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Charge;
import com.stripe.model.Event;
import com.stripe.model.Invoice;
import com.stripe.model.Refund;
import com.stripe.param.InvoiceCreateParams;
import com.stripe.param.RefundCreateParams;
import com.stripe.param.RefundCreateParams.Reason;
import com.stripe.param.SubscriptionCreateParams;
import com.videoflix.subscriptions_microservice.entities.Subscription;
import com.videoflix.subscriptions_microservice.integration.PaymentFailedEventPublisher;
import com.videoflix.subscriptions_microservice.repositories.SubscriptionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class StripeBillingService {

    // Logger pour loguer les événements ou erreurs dans ce service
    private static final Logger logger = LoggerFactory.getLogger(StripeBillingService.class);

    // Dépendances du service
    private final PaymentFailedEventPublisher paymentFailedEventPublisher;
    private final SubscriptionRepository subscriptionRepository;

    // Constructeur injectant les dépendances via l'initialisation des champs privés
    public StripeBillingService(
            @Value("${stripe.api.secretKey}") String secretKey, // Récupère la clé secrète Stripe depuis la config
            PaymentFailedEventPublisher paymentFailedEventPublisher, // Éditeur d'événements en cas d'échec de paiement
            SubscriptionRepository subscriptionRepository) { // Reposiory pour gérer les abonnements locaux
        Stripe.apiKey = secretKey; // Configuration de la clé API Stripe
        this.paymentFailedEventPublisher = paymentFailedEventPublisher;
        this.subscriptionRepository = subscriptionRepository;
    }

    // Méthode pour créer un abonnement sur Stripe
    public com.stripe.model.Subscription createSubscription(String customerId, String priceId) throws StripeException {
        // Paramètres de l'abonnement à créer
        SubscriptionCreateParams params = SubscriptionCreateParams.builder()
                .setCustomer(customerId) // Identifiant client Stripe
                .addItem(SubscriptionCreateParams.Item.builder()
                        .setPrice(priceId) // Identifiant du prix auquel s'abonner
                        .build())
                .build();

        // Création de l'abonnement sur Stripe et retour de l'objet Subscription
        return com.stripe.model.Subscription.create(params);
    }

    // Méthode pour effectuer un paiement unique non géré par l'abonnement initial
    public Charge createCharge(String customerId, Long amountInCents, String currency, String description)
            throws StripeException {
        java.util.Map<String, Object> chargeParams = new java.util.HashMap<>();
        chargeParams.put("amount", amountInCents);
        chargeParams.put("currency", currency);
        chargeParams.put("customer", customerId);
        chargeParams.put("description", description);
        return Charge.create(chargeParams);
    }

    // Méthode pour créer une facture sur Stripe
    public Invoice createInvoice(String customerId) throws StripeException {
        // Paramètres pour créer une facture
        InvoiceCreateParams params = InvoiceCreateParams.builder()
                .setCustomer(customerId) // Identifiant client Stripe
                .build();

        // Création de la facture et retour de l'objet Invoice
        return Invoice.create(params);
    }

    // Méthode qui gère les événements reçus du webhook Stripe
    public void handleStripeWebhookEvent(Event event) {
        String eventType = event.getType(); // Récupère le type d'événement Stripe

        // Si l'événement est un échec de paiement de facture, on traite l'événement
        if ("invoice.payment_failed".equals(eventType)) {
            handleInvoicePaymentFailedEvent(event);
        } else {
            // Si l'événement est autre, on l'ignore (on pourrait aussi l'enregistrer pour
            // une analyse ultérieure)
            logger.info("Évènement Stripe non géré : {}", eventType);
        }
    }

    // Méthode pour traiter l'événement "invoice.payment_failed"
    private void handleInvoicePaymentFailedEvent(Event event) {
        // On tente de récupérer l'objet Invoice à partir de l'événement
        Optional<Invoice> optionalInvoice = event.getDataObjectDeserializer().getObject()
                .filter(Invoice.class::isInstance) // Vérifie si l'objet est bien une instance de Invoice
                .map(Invoice.class::cast); // Cast l'objet en Invoice

        // Si la facture est absente ou mal formatée, on arrête le traitement
        if (optionalInvoice.isEmpty()) {
            logger.warn("Impossible de désérialiser la facture Stripe.");
            return;
        }

        // Récupération de la facture
        Invoice invoice = optionalInvoice.get();
        String subscriptionId = invoice.getSubscription(); // Récupération de l'ID de l'abonnement depuis la facture

        // Si l'ID de l'abonnement est absent, on arrête le traitement
        if (subscriptionId == null || subscriptionId.isEmpty()) {
            logger.error("Aucun ID d'abonnement trouvé dans la facture Stripe.");
            return;
        }

        // On recherche l'abonnement local correspondant à l'ID de l'abonnement Stripe
        Optional<Subscription> optionalLocalSubscription = Optional.empty();

        // Si l'abonnement local n'existe pas, on log l'erreur et on arrête
        if (optionalLocalSubscription.isEmpty()) {
            logger.error("Abonnement local non trouvé pour l'abonnement Stripe ID : {}", subscriptionId);
            return;
        }

        // Récupération de l'abonnement local trouvé
        Subscription localSubscription = optionalLocalSubscription.get();

        // Récupération de la raison de l'échec, ou valeur par défaut
        String failureReason = invoice.getLastFinalizationError() != null
                ? invoice.getLastFinalizationError().getMessage()
                : "Raison d'échec inconnue.";

        // Mise à jour de la date du prochain essai pour cet abonnement
        localSubscription.setNextRetryDate(LocalDateTime.now().plusDays(3));
        // Sauvegarde de l'abonnement avec la nouvelle date de retry
        subscriptionRepository.save(localSubscription);

        // Envoi de l'événement d'échec de paiement
        paymentFailedEventPublisher.publishPaymentFailedEvent(localSubscription, failureReason);

        // Log du détail de l'échec de paiement
        logger.warn("Paiement échoué pour la facture Stripe {} (Abonnement {}). Raison : {}",
                invoice.getId(), subscriptionId, failureReason);
    }

    public Refund refundCharge(String chargeId, Reason reason) throws StripeException {
        RefundCreateParams params = RefundCreateParams.builder()
                .setCharge(chargeId)
                .setReason(reason)
                .build();
        return Refund.create(params);
    }

    public Refund refundPaymentIntent(String paymentIntentId, Reason reason) throws StripeException {
        RefundCreateParams params = RefundCreateParams.builder()
                .setPaymentIntent(paymentIntentId)
                .setReason(reason)
                .build();
        return Refund.create(params);
    }
}