package com.videoflix.subscriptions_microservice.integration;

import com.stripe.exception.StripeException;
import com.stripe.model.Charge;
import com.stripe.model.Invoice;
import com.stripe.model.Refund;
import com.stripe.model.Subscription;
import com.stripe.param.InvoiceCreateParams;
import com.stripe.param.RefundCreateParams;
import com.stripe.param.SubscriptionCreateParams;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// @ExtendWith(MockitoExtension.class) permet d'utiliser les fonctionnalités de Mockito dans ce test.
@ExtendWith(MockitoExtension.class)
 class StripeClientTest {

    // @Mock crée un mock de la classe Subscription (de Stripe).
    @Mock
    private Subscription stripeSubscription;

    // @Mock crée un mock de la classe Charge (de Stripe).
    @Mock
    private Charge stripeCharge;

    // @Mock crée un mock de la classe Invoice (de Stripe).
    @Mock
    private Invoice stripeInvoice;

    // @Mock crée un mock de la classe Refund (de Stripe).
    @Mock
    private Refund stripeRefund;

    // @InjectMocks crée une instance de StripeClient et y injecte les mocks annotés.
    @InjectMocks
    private StripeClient stripeClient;

    // Test pour vérifier la création d'un abonnement via StripeClient.
    @Test
    void createSubscription_shouldCallStripeSubscriptionCreate() throws StripeException {
        // GIVEN : Des paramètres de création d'abonnement mockés.
        SubscriptionCreateParams params = SubscriptionCreateParams.builder()
                .setCustomer("cus_123")
                .addItem(SubscriptionCreateParams.Item.builder().setPrice("price_abc").build())
                .build();

        // Configuration du mock stripeSubscription pour retourner un objet lorsqu'il est créé.
        when(Subscription.create(any(SubscriptionCreateParams.class))).thenReturn(stripeSubscription);

        // WHEN : Appel de la méthode createSubscription de StripeClient.
        Subscription result = stripeClient.createSubscription(params);

        // THEN : Vérification que la méthode Subscription.create de Stripe a été appelée une fois avec les paramètres.
        verify(Subscription, times(1)).create(params);
        // Vérification que l'objet retourné est le mock de Subscription.
        assertEquals(stripeSubscription, result);
    }

    // Test pour vérifier la création d'une charge via StripeClient.
    @SuppressWarnings("unchecked")
    @Test
    void createCharge_shouldCallStripeChargeCreate() throws StripeException {
        // GIVEN : Des paramètres de création de charge mockés.
        Map<String, Object> params = new HashMap<>();
        params.put("amount", 1000L);
        params.put("currency", "eur");
        params.put("customer", "cus_456");

        // Configuration du mock stripeCharge pour retourner un objet lorsqu'il est créé.
        when(Charge.create(any(Map.class))).thenReturn(stripeCharge);

        // WHEN : Appel de la méthode createCharge de StripeClient.
        Charge result = stripeClient.createCharge(params);

        // THEN : Vérification que la méthode Charge.create de Stripe a été appelée une fois avec les paramètres.
        verify(Charge, times(1)).create(params);
        // Vérification que l'objet retourné est le mock de Charge.
        assertEquals(stripeCharge, result);
    }

    // Test pour vérifier la création d'une facture via StripeClient.
    @Test
    void createInvoice_shouldCallStripeInvoiceCreate() throws StripeException {
        // GIVEN : Des paramètres de création de facture mockés.
        InvoiceCreateParams params = InvoiceCreateParams.builder()
                .setCustomer("cus_789")
                .build();

        // Configuration du mock stripeInvoice pour retourner un objet lorsqu'il est créé.
        when(Invoice.create(any(InvoiceCreateParams.class))).thenReturn(stripeInvoice);

        // WHEN : Appel de la méthode createInvoice de StripeClient.
        Invoice result = stripeClient.createInvoice(params);

        // THEN : Vérification que la méthode Invoice.create de Stripe a été appelée une fois avec les paramètres.
        verify(Invoice, times(1)).create(params);
        // Vérification que l'objet retourné est le mock de Invoice.
        assertEquals(stripeInvoice, result);
    }

    // Test pour vérifier la création d'un remboursement via StripeClient.
    @Test
    void createRefund_shouldCallStripeRefundCreate() throws StripeException {
        // GIVEN : Des paramètres de création de remboursement mockés.
        RefundCreateParams params = RefundCreateParams.builder()
                .setCharge("ch_abc")
                .setReason(RefundCreateParams.Reason.REQUESTED_BY_CUSTOMER)
                .build();

        // Configuration du mock stripeRefund pour retourner un objet lorsqu'il est créé.
        when(Refund.create(any(RefundCreateParams.class))).thenReturn(stripeRefund);

        // WHEN : Appel de la méthode createRefund de StripeClient.
        Refund result = stripeClient.createRefund(params);

        // THEN : Vérification que la méthode Refund.create de Stripe a été appelée une fois avec les paramètres.
        verify(Refund, times(1)).create(params);
        // Vérification que l'objet retourné est le mock de Refund.
        assertEquals(stripeRefund, result);
    }
}