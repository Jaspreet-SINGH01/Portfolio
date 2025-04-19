package com.videoflix.subscriptions_microservice.services;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.Invoice;
import com.stripe.model.Subscription;
import com.stripe.param.InvoiceCreateParams;
import com.stripe.param.SubscriptionCreateParams;
import com.videoflix.subscriptions_microservice.entities.Subscription as LocalSubscription; // Alias pour éviter la confusion
import com.videoflix.subscriptions_microservice.entities.User; // Assurez-vous d'avoir l'entité User
import com.videoflix.subscriptions_microservice.integration.PaymentFailedEventPublisher;
import com.videoflix.subscriptions_microservice.repositories.SubscriptionRepository; // Assurez-vous d'avoir le repository
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class StripeBillingService {

    private final String stripeApiKey;
    private final PaymentFailedEventPublisher paymentFailedEventPublisher;
    private final SubscriptionRepository subscriptionRepository;

    public StripeBillingService(@Value("${stripe.api.secretKey}") String secretKey,
                                 PaymentFailedEventPublisher paymentFailedEventPublisher,
                                 SubscriptionRepository subscriptionRepository) {
        this.stripeApiKey = secretKey;
        Stripe.apiKey = secretKey;
        this.paymentFailedEventPublisher = paymentFailedEventPublisher;
        this.subscriptionRepository = subscriptionRepository;
    }

    public Subscription createSubscription(String customerId, String priceId) throws StripeException {
        SubscriptionCreateParams params = SubscriptionCreateParams.builder()
                .setCustomer(customerId)
                .addItem(SubscriptionCreateParams.Item.builder()
                        .setPrice(priceId)
                        .build())
                .build();
        return Subscription.create(params);
    }

    public Invoice createInvoice(String customerId) throws StripeException {
        InvoiceCreateParams params = InvoiceCreateParams.builder()
                .setCustomer(customerId)
                .build();
        return Invoice.create(params);
    }

    // Méthode pour gérer les événements Webhook de Stripe
    public void handleStripeWebhookEvent(Event event) {
        switch (event.getType()) {
            case "invoice.payment_failed":
                Invoice invoice = (Invoice) event.getDataObjectDeserializer().getObject().orElse(null);
                if (invoice != null) {
                    handleInvoicePaymentFailed(invoice);
                }
                break;
            // Gérer d'autres types d'événements si nécessaire
            default:
                System.out.println("Événement Stripe non géré : " + event.getType());
        }
    }

    private void handleInvoicePaymentFailed(Invoice stripeInvoice) {
        // Récupérer l'abonnement local à partir de l'ID Stripe de l'abonnement
        LocalSubscription localSubscription = subscriptionRepository.findByStripeSubscriptionId(stripeInvoice.getSubscription())
                .orElse(null);

        if (localSubscription != null) {
            String failureReason = stripeInvoice.getLastPaymentError() != null ?
                    stripeInvoice.getLastPaymentError().getMessage() : "Payment failed for an unknown reason.";

            // Mettre à jour l'abonnement local avec la tentative de paiement échouée
            localSubscription.setNextRetryDate(java.time.LocalDateTime.now().plusDays(3)); // Exemple de logique de réessai
            subscriptionRepository.save(localSubscription);

            // Récupérer l'utilisateur associé à l'abonnement (vous devrez adapter cela à votre modèle)
            User user = localSubscription.getUser(); // Supposant une relation directe

            // Publier l'événement d'échec de paiement
            paymentFailedEventPublisher.publishPaymentFailedEvent(localSubscription, failureReason);

            // Potentiellement notifier l'utilisateur via un autre service
            System.out.println("Paiement de la facture Stripe " + stripeInvoice.getId() + " a échoué pour l'abonnement " + stripeInvoice.getSubscription() + ". Raison : " + failureReason);
        } else {
            System.out.println("Abonnement local non trouvé pour l'abonnement Stripe ID : " + stripeInvoice.getSubscription());
        }
    }
}