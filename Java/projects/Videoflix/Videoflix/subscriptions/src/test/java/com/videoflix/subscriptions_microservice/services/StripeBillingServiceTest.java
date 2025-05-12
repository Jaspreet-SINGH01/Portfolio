package com.videoflix.subscriptions_microservice.services;

import com.stripe.exception.StripeException;
import com.stripe.model.*;
import com.stripe.param.*;
import com.videoflix.subscriptions_microservice.integration.PaymentFailedEventPublisher;
import com.videoflix.subscriptions_microservice.integration.StripeClient;
import com.videoflix.subscriptions_microservice.repositories.SubscriptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
 class StripeBillingServiceTest {

    private static final String TEST_SECRET_KEY = "sk_test_xxx";

    @Mock private PaymentFailedEventPublisher paymentFailedEventPublisher;
    @Mock private SubscriptionRepository subscriptionRepository;
    @Mock private Logger logger;
    @Mock private StripeClient stripeClient;

    @InjectMocks private StripeBillingService stripeBillingService;

    @Captor private ArgumentCaptor<Subscription> subscriptionCaptor;
    @Captor private ArgumentCaptor<String> failureReasonCaptor;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(stripeBillingService, "secretKey", TEST_SECRET_KEY);
    }


    @Test
    void createSubscription_shouldCreateStripeSubscription() throws StripeException {
        String customerId = "cus_123";
        String priceId = "price_abc";
    
        SubscriptionCreateParams params = SubscriptionCreateParams.builder()
            .setCustomer(customerId)
            .addItem(
                SubscriptionCreateParams.Item.builder()
                    .setPrice(priceId)
                    .build()
            )
            .build();
    
        Subscription mockSub = new Subscription();
        mockSub.setId("sub_test");
    
        // Utilisation de params dans l'appel à createSubscription
        when(stripeClient.createSubscription(params)).thenReturn(mockSub);
    
        Subscription result = stripeBillingService.createSubscription(customerId, priceId);
    
        assertEquals("sub_test", result.getId()); // Comparaison OK car types égaux
        verify(stripeClient, times(1)).createSubscription(params); // Vérification de l'appel avec params
    }
    


    @Test
    void createCharge_shouldCreateStripeCharge() throws StripeException {
        String customerId = "cus_123";
        Long amount = 1000L;
        String currency = "eur";
        String description = "Test";

        Charge mockCharge = new Charge();
        mockCharge.setId("ch_test");

        when(stripeClient.createCharge(any())).thenReturn(mockCharge);

        Charge result = stripeBillingService.createCharge(customerId, amount, currency, description);

        assertEquals("ch_test", result.getId());
        verify(stripeClient).createCharge(any());
    }

    @Test
    void createInvoice_shouldCreateStripeInvoice() throws StripeException {
        String customerId = "cus_123";
        Invoice mockInvoice = new Invoice();
        mockInvoice.setId("inv_test");

        when(stripeClient.createInvoice(any())).thenReturn(mockInvoice);

        Invoice result = stripeBillingService.createInvoice(customerId);

        assertEquals("inv_test", result.getId());
        verify(stripeClient).createInvoice(any());
    }

    @Test
    void refundCharge_shouldRefundStripeCharge() throws StripeException {
        String chargeId = "ch_123";
        RefundCreateParams.Reason reason = RefundCreateParams.Reason.REQUESTED_BY_CUSTOMER;
        Refund mockRefund = new Refund();
        mockRefund.setId("re_test");

        when(stripeClient.createRefund(any())).thenReturn(mockRefund);

        Refund result = stripeBillingService.refundCharge(chargeId, reason);

        assertEquals("re_test", result.getId());
        verify(stripeClient).createRefund(any());
    }

    @Test
    void refundPaymentIntent_shouldRefundStripePaymentIntent() throws StripeException {
        String intentId = "pi_123";
        RefundCreateParams.Reason reason = RefundCreateParams.Reason.REQUESTED_BY_CUSTOMER;
        Refund mockRefund = new Refund();
        mockRefund.setId("re_test");

        when(stripeClient.createRefund(any())).thenReturn(mockRefund);

        Refund result = stripeBillingService.refundPaymentIntent(intentId, reason);

        assertEquals("re_test", result.getId());
        verify(stripeClient).createRefund(any());
    }

    @Test
    void handleStripeWebhookEvent_shouldHandleInvoicePaymentFailedEvent() {
        String subscriptionId = "sub_123";
        String invoiceId = "in_123";
        String failureReason = "card_declined";

        Event event = mock(Event.class);
        when(event.getType()).thenReturn("invoice.payment_failed");

        EventDataObjectDeserializer deserializer = mock(EventDataObjectDeserializer.class);
        Invoice invoice = mock(Invoice.class);
        InvoiceFinalizationError error = mock(InvoiceFinalizationError.class);

        when(event.getDataObjectDeserializer()).thenReturn(deserializer);
        when(deserializer.getObject()).thenReturn(Optional.of(invoice));
        when(invoice.getSubscription()).thenReturn(subscriptionId);
        when(invoice.getId()).thenReturn(invoiceId);
        when(invoice.getLastFinalizationError()).thenReturn(error);
        when(error.getMessage()).thenReturn(failureReason);

        Subscription localSub = new Subscription();
        localSub.setStripeSubscriptionId(subscriptionId);

        when(subscriptionRepository.findByStripeSubscriptionId(subscriptionId)).thenReturn(Optional.of(localSub));
        when(subscriptionRepository.save(any())).thenReturn(localSub);

        stripeBillingService.handleStripeWebhookEvent(event);

        verify(paymentFailedEventPublisher).publishPaymentFailedEvent(subscriptionCaptor.capture(), failureReasonCaptor.capture());
        assertEquals(localSub, subscriptionCaptor.getValue());
        assertEquals(failureReason, failureReasonCaptor.getValue());
        assertNotNull(localSub.getNextPendingInvoiceItemInvoice());
        verify(logger).warn("Paiement échoué pour la facture Stripe {} (Abonnement {}). Raison : {}", invoiceId, subscriptionId, failureReason);
    }

    @Test
    void handleStripeWebhookEvent_shouldLogWarningIfInvoiceDeserializationFails() {
        Event event = mock(Event.class);
        when(event.getType()).thenReturn("invoice.payment_failed");
        EventDataObjectDeserializer deserializer = mock(EventDataObjectDeserializer.class);
        when(event.getDataObjectDeserializer()).thenReturn(deserializer);
        when(deserializer.getObject()).thenReturn(Optional.empty());

        stripeBillingService.handleStripeWebhookEvent(event);

        verify(logger).warn("Impossible de désérialiser la facture Stripe.");
        verifyNoInteractions(paymentFailedEventPublisher);
    }

    @Test
    void handleStripeWebhookEvent_shouldLogErrorIfSubscriptionIdIsNull() {
        Event event = mock(Event.class);
        when(event.getType()).thenReturn("invoice.payment_failed");
        Invoice invoice = mock(Invoice.class);
        EventDataObjectDeserializer deserializer = mock(EventDataObjectDeserializer.class);
        when(event.getDataObjectDeserializer()).thenReturn(deserializer);
        when(deserializer.getObject()).thenReturn(Optional.of(invoice));
        when(invoice.getSubscription()).thenReturn(null);

        stripeBillingService.handleStripeWebhookEvent(event);

        verify(logger).error("Aucun ID d'abonnement trouvé dans la facture Stripe.");
        verifyNoInteractions(paymentFailedEventPublisher);
    }

    @Test
    void handleStripeWebhookEvent_shouldLogErrorIfLocalSubscriptionNotFound() {
        String subscriptionId = "sub_123";
        Event event = mock(Event.class);
        when(event.getType()).thenReturn("invoice.payment_failed");

        Invoice invoice = mock(Invoice.class);
        EventDataObjectDeserializer deserializer = mock(EventDataObjectDeserializer.class);
        when(event.getDataObjectDeserializer()).thenReturn(deserializer);
        when(deserializer.getObject()).thenReturn(Optional.of(invoice));
        when(invoice.getSubscription()).thenReturn(subscriptionId);

        when(subscriptionRepository.findByStripeSubscriptionId(subscriptionId)).thenReturn(Optional.empty());

        stripeBillingService.handleStripeWebhookEvent(event);

        verify(logger).error("Abonnement local non trouvé pour l'abonnement Stripe ID : {}", subscriptionId);
    }

    @Test
    void handleStripeWebhookEvent_shouldLogInfoForUnhandledEvent() {
        String type = "customer.created";
        Event event = mock(Event.class);
        when(event.getType()).thenReturn(type);

        stripeBillingService.handleStripeWebhookEvent(event);

        verify(logger).info("Évènement Stripe non géré : {}", type);
    }

    @Test
    void createSubscription_shouldThrowStripeException() throws StripeException {
        when(stripeClient.createSubscription(any())).thenThrow(new StripeException("Erreur", null, null, 500, null));

        assertThrows(StripeException.class, () -> stripeBillingService.createSubscription("cus_test", "price_test"));
    }
}
