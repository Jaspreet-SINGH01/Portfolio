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
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

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

    // @InjectMocks crée une instance de StripeClient et y injecte les mocks
    // annotés.
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

        // Utilisation de mockStatic pour pouvoir vérifier les appels aux méthodes
        // statiques de Subscription.
        try (MockedStatic<Subscription> mockedStaticSubscription = Mockito.mockStatic(Subscription.class)) {
            // Configuration du comportement de la méthode statique create.
            when(Subscription.create(any(SubscriptionCreateParams.class))).thenReturn(stripeSubscription);

            // WHEN : Appel de la méthode createSubscription de StripeClient.
            Subscription result = stripeClient.createSubscription(params);

            // THEN : Vérification que la méthode Subscription.create de Stripe a été
            // appelée une fois avec les paramètres.
            mockedStaticSubscription.verify(() -> Subscription.create(params), times(1));
            // Vérification que l'objet retourné est le mock de Subscription.
            assertEquals(stripeSubscription, result);
        }
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

        try (MockedStatic<Charge> mockedStaticCharge = Mockito.mockStatic(Charge.class)) {
            when(Charge.create(any(Map.class))).thenReturn(stripeCharge);

            Charge result = stripeClient.createCharge(params);

            mockedStaticCharge.verify(() -> Charge.create(params), times(1));
            assertEquals(stripeCharge, result);
        }
    }

    // Test pour vérifier la création d'une facture via StripeClient.
    @Test
    void createInvoice_shouldCallStripeInvoiceCreate() throws StripeException {
        // GIVEN : Des paramètres de création de facture mockés.
        InvoiceCreateParams params = InvoiceCreateParams.builder()
                .setCustomer("cus_789")
                .build();

        try (MockedStatic<Invoice> mockedStaticInvoice = Mockito.mockStatic(Invoice.class)) {
            when(Invoice.create(any(InvoiceCreateParams.class))).thenReturn(stripeInvoice);

            Invoice result = stripeClient.createInvoice(params);

            mockedStaticInvoice.verify(() -> Invoice.create(params), times(1));
            assertEquals(stripeInvoice, result);
        }
    }

    // Test pour vérifier la création d'un remboursement via StripeClient.
    @Test
    void createRefund_shouldCallStripeRefundCreate() throws StripeException {
        // GIVEN : Des paramètres de création de remboursement mockés.
        RefundCreateParams params = RefundCreateParams.builder()
                .setCharge("ch_abc")
                .setReason(RefundCreateParams.Reason.REQUESTED_BY_CUSTOMER)
                .build();

        try (MockedStatic<Refund> mockedStaticRefund = Mockito.mockStatic(Refund.class)) {
            when(Refund.create(any(RefundCreateParams.class))).thenReturn(stripeRefund);

            Refund result = stripeClient.createRefund(params);

            mockedStaticRefund.verify(() -> Refund.create(params), times(1));
            assertEquals(stripeRefund, result);
        }
    }
}