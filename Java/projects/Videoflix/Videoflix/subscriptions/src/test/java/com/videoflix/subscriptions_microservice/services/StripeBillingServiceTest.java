// Définition du package de test
package com.videoflix.subscriptions_microservice.services;

// Importation des dépendances Stripe
import com.stripe.exception.ApiException;
import com.stripe.exception.StripeException;
import com.stripe.model.Invoice;
import com.stripe.model.Event;
import com.stripe.model.Charge;
import com.stripe.model.Refund;
import com.stripe.model.EventDataObjectDeserializer;
// import com.stripe.param.ChargeCreateParams;
// import com.stripe.param.InvoiceCreateParams;
import com.stripe.param.RefundCreateParams;
import com.stripe.param.SubscriptionCreateParams;

// Importation des classes métier de l'application
import com.videoflix.subscriptions_microservice.entities.Subscription;
import com.videoflix.subscriptions_microservice.integration.PaymentFailedEventPublisher;
import com.videoflix.subscriptions_microservice.integration.StripeClient;
import com.videoflix.subscriptions_microservice.repositories.SubscriptionRepository;

// Importation des outils de test (JUnit, Mockito, etc.)
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

@ExtendWith(MockitoExtension.class) // Extension Mockito pour les tests JUnit 5
class StripeBillingServiceTest {

    private static final String TEST_SECRET_KEY = "sk_test_xxx"; // Clé API fictive pour Stripe

    // Dépendances simulées
    @Mock
    private PaymentFailedEventPublisher paymentFailedEventPublisher;
    @Mock
    private SubscriptionRepository subscriptionRepository;
    @Mock
    private Logger logger;
    @Mock
    private StripeClient stripeClient;

    // Service testé
    @InjectMocks
    private StripeBillingService stripeBillingService;

    // Captures d’arguments pour les vérifications
    @Captor
    private ArgumentCaptor<Subscription> subscriptionCaptor;
    @Captor
    private ArgumentCaptor<String> failureReasonCaptor;

    @BeforeEach
    void setUp() {
        // Injection manuelle de la clé secrète dans le service via reflection
        ReflectionTestUtils.setField(stripeBillingService, "secretKey", TEST_SECRET_KEY);
    }

    @Test
    void createSubscription_shouldCreateStripeSubscription() throws StripeException {
        // Préparation des données d’entrée
        String customerId = "cus_123";
        String priceId = "price_abc";

        // Construction des paramètres Stripe
        SubscriptionCreateParams params = SubscriptionCreateParams.builder()
                .setCustomer(customerId)
                .addItem(SubscriptionCreateParams.Item.builder().setPrice(priceId).build())
                .build();

        // Simulation de la réponse Stripe
        com.stripe.model.Subscription stripeSub = new com.stripe.model.Subscription();
        stripeSub.setId("sub_test");

        // Comportement attendu du client Stripe simulé
        when(stripeClient.createSubscription(params)).thenReturn(stripeSub);

        // Appel de la méthode testée
        com.stripe.model.Subscription result = stripeBillingService.createSubscription(customerId, priceId);

        // Vérifications
        assertNotNull(result);
        assertEquals("sub_test", result.getId());
        verify(stripeClient).createSubscription(params);
    }

    @Test
    void createCharge_shouldCreateStripeCharge() throws StripeException {
        // Simulation d’un objet Charge
        Charge mockCharge = new Charge();
        mockCharge.setId("ch_test");

        when(stripeClient.createCharge(any())).thenReturn(mockCharge);

        // Appel de la méthode à tester
        Charge result = stripeBillingService.createCharge("cus_123", 1000L, "eur", "Test");

        assertNotNull(result);
        assertEquals("ch_test", result.getId());
        verify(stripeClient).createCharge(any());
    }

    @Test
    void createInvoice_shouldCreateStripeInvoice() throws StripeException {
        Invoice mockInvoice = new Invoice();
        mockInvoice.setId("inv_test");

        when(stripeClient.createInvoice(any())).thenReturn(mockInvoice);

        Invoice result = stripeBillingService.createInvoice("cus_123");

        assertNotNull(result);
        assertEquals("inv_test", result.getId());
        verify(stripeClient).createInvoice(any());
    }

    @Test
    void refundCharge_shouldRefundStripeCharge() throws StripeException {
        Refund mockRefund = new Refund();
        mockRefund.setId("re_test");

        when(stripeClient.createRefund(any())).thenReturn(mockRefund);

        Refund result = stripeBillingService.refundCharge("ch_123", RefundCreateParams.Reason.REQUESTED_BY_CUSTOMER);

        assertNotNull(result);
        assertEquals("re_test", result.getId());
        verify(stripeClient).createRefund(any());
    }

    @Test
    void refundPaymentIntent_shouldRefundStripePaymentIntent() throws StripeException {
        Refund mockRefund = new Refund();
        mockRefund.setId("re_test");

        when(stripeClient.createRefund(any())).thenReturn(mockRefund);

        Refund result = stripeBillingService.refundPaymentIntent("pi_123",
                RefundCreateParams.Reason.REQUESTED_BY_CUSTOMER);

        assertNotNull(result);
        assertEquals("re_test", result.getId());
        verify(stripeClient).createRefund(any());
    }

    @Test
    void handleStripeWebhookEvent_shouldHandleInvoicePaymentFailedEvent() {
        // Préparation d’un événement de type invoice.payment_failed
        String subscriptionId = "sub_123";
        String invoiceId = "in_123";
        String failureReason = "card_declined";

        Event event = mock(Event.class);
        Invoice invoice = mock(Invoice.class);
        EventDataObjectDeserializer deserializer = mock(EventDataObjectDeserializer.class);
        @SuppressWarnings("unchecked")
        Map<String, Object> error = mock(Map.class); // Erreur simulée

        // Configuration du comportement des mocks
        when(event.getType()).thenReturn("invoice.payment_failed");
        when(event.getDataObjectDeserializer()).thenReturn(deserializer);
        when(deserializer.getObject()).thenReturn(Optional.of(invoice));
        when(invoice.getSubscription()).thenReturn(subscriptionId);
        when(invoice.getId()).thenReturn(invoiceId);
        // when(invoice.getLastFinalizationError()).thenReturn(error);
        when(error.get("message")).thenReturn(failureReason);

        // Simulation d’un abonnement local
        Subscription localSub = new Subscription();
        localSub.setStripeSubscriptionId(subscriptionId);
        when(subscriptionRepository.findByStripeSubscriptionId(subscriptionId)).thenReturn(Optional.of(localSub));
        when(subscriptionRepository.save(any())).thenReturn(localSub);

        // Appel du service
        stripeBillingService.handleStripeWebhookEvent(event);

        // Vérifications de la publication d’un événement
        verify(paymentFailedEventPublisher).publishPaymentFailedEvent(null, failureReason);;
        assertEquals(localSub, subscriptionCaptor.getValue());
        assertEquals(failureReason, failureReasonCaptor.getValue());

        // Vérification des logs
        verify(logger).warn("Paiement échoué pour la facture Stripe {} (Abonnement {}). Raison : {}",
                invoiceId, subscriptionId, failureReason);
    }

    @Test
    void handleStripeWebhookEvent_shouldLogWarningIfInvoiceDeserializationFails() {
        // Cas où l’objet Invoice ne peut pas être désérialisé
        Event event = mock(Event.class);
        EventDataObjectDeserializer deserializer = mock(EventDataObjectDeserializer.class);

        when(event.getType()).thenReturn("invoice.payment_failed");
        when(event.getDataObjectDeserializer()).thenReturn(deserializer);
        when(deserializer.getObject()).thenReturn(Optional.empty());

        stripeBillingService.handleStripeWebhookEvent(event);

        // Vérifie qu’un warning est bien loggé
        verify(logger).warn("Impossible de désérialiser la facture Stripe.");
        verifyNoInteractions(paymentFailedEventPublisher);
    }

    @Test
    void handleStripeWebhookEvent_shouldLogErrorIfSubscriptionIdIsNull() {
        // Cas où l’abonnement est absent dans la facture
        Event event = mock(Event.class);
        EventDataObjectDeserializer deserializer = mock(EventDataObjectDeserializer.class);
        Invoice invoice = mock(Invoice.class);

        when(event.getType()).thenReturn("invoice.payment_failed");
        when(event.getDataObjectDeserializer()).thenReturn(deserializer);
        when(deserializer.getObject()).thenReturn(Optional.of(invoice));
        when(invoice.getSubscription()).thenReturn(null);

        stripeBillingService.handleStripeWebhookEvent(event);

        verify(logger).error("Aucun ID d'abonnement trouvé dans la facture Stripe.");
        verifyNoInteractions(paymentFailedEventPublisher);
    }

    @Test
    void handleStripeWebhookEvent_shouldLogErrorIfLocalSubscriptionNotFound() {
        // Cas où l’abonnement Stripe n’est pas trouvé localement
        String subscriptionId = "sub_123";
        Event event = mock(Event.class);
        EventDataObjectDeserializer deserializer = mock(EventDataObjectDeserializer.class);
        Invoice invoice = mock(Invoice.class);

        when(event.getType()).thenReturn("invoice.payment_failed");
        when(event.getDataObjectDeserializer()).thenReturn(deserializer);
        when(deserializer.getObject()).thenReturn(Optional.of(invoice));
        when(invoice.getSubscription()).thenReturn(subscriptionId);
        when(subscriptionRepository.findByStripeSubscriptionId(subscriptionId)).thenReturn(Optional.empty());

        stripeBillingService.handleStripeWebhookEvent(event);

        verify(logger).error("Abonnement local non trouvé pour l'abonnement Stripe ID : {}", subscriptionId);
        verifyNoInteractions(paymentFailedEventPublisher);
    }

    @Test
    void handleStripeWebhookEvent_shouldLogInfoForUnhandledEvent() {
        // Cas d’un événement non géré (type inconnu)
        String type = "customer.created";
        Event event = mock(Event.class);
        when(event.getType()).thenReturn(type);

        stripeBillingService.handleStripeWebhookEvent(event);

        verify(logger).info("Évènement Stripe non géré : {}", type);
    }

    @Test
    void createSubscription_shouldThrowStripeException() throws StripeException {
        // Cas d’une exception levée par Stripe lors de la création de l’abonnement
        String errorMessage = "Erreur de création d'abonnement";
        ApiException stripeError = new ApiException(errorMessage, null, null, 500, null);

        when(stripeClient.createSubscription(any())).thenThrow(stripeError);

        StripeException exception = assertThrows(StripeException.class,
                () -> stripeBillingService.createSubscription("cus_test", "price_test"));

        assertEquals(errorMessage, exception.getMessage());
    }
}