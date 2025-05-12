package com.videoflix.subscriptions_microservice.integration;

import java.util.Map;

import com.stripe.exception.StripeException;
import com.stripe.model.*;
import com.stripe.param.*;

public class StripeClient {

    /**
     * Crée un nouvel abonnement Stripe.
     *
     * @param params Les paramètres nécessaires à la création de l'abonnement (par
     *               exemple, customer, items).
     * @return L'objet Subscription créé par Stripe.
     * @throws StripeException En cas d'erreur lors de la communication avec l'API
     *                         Stripe.
     */
    public Subscription createSubscription(SubscriptionCreateParams params) throws StripeException {
        return Subscription.create(params);
    }

    /**
     * Crée une nouvelle charge (paiement unique) sur Stripe.
     *
     * @param params Une map contenant les paramètres de la charge (par exemple,
     *               amount, currency, customer).
     * @return L'objet Charge créé par Stripe.
     * @throws StripeException En cas d'erreur lors de la communication avec l'API
     *                         Stripe.
     */
    public Charge createCharge(Map<String, Object> params) throws StripeException {
        return Charge.create(params);
    }

    /**
     * Crée une nouvelle facture sur Stripe.
     *
     * @param params Les paramètres nécessaires à la création de la facture (par
     *               exemple, customer).
     * @return L'objet Invoice créé par Stripe.
     * @throws StripeException En cas d'erreur lors de la communication avec l'API
     *                         Stripe.
     */
    public Invoice createInvoice(InvoiceCreateParams params) throws StripeException {
        return Invoice.create(params);
    }

    /**
     * Crée un nouveau remboursement sur Stripe.
     *
     * @param params Les paramètres nécessaires à la création du remboursement (par
     *               exemple, charge ou paymentIntent, reason).
     * @return L'objet Refund créé par Stripe.
     * @throws StripeException En cas d'erreur lors de la communication avec l'API
     *                         Stripe.
     */
    public Refund createRefund(RefundCreateParams params) throws StripeException {
        return Refund.create(params);
    }
}