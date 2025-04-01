package com.videoflix.subscriptions_microservice.services;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Invoice;
import com.stripe.model.Subscription;
import com.stripe.param.InvoiceCreateParams;
import com.stripe.param.SubscriptionCreateParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class StripeBillingService {

    public StripeBillingService(@Value("${stripe.api.secretKey}") String secretKey) {
        Stripe.apiKey = secretKey;
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
}