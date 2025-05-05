package com.videoflix.subscriptions_microservice.controllers;

import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.Invoice;
import com.stripe.model.StripeObject;
import com.stripe.net.Webhook;
import com.videoflix.subscriptions_microservice.services.NotificationService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class StripeWebhookControllerTest {

    @InjectMocks
    private StripeWebhookController stripeWebhookController; // Instance du contrôleur à tester

    @Mock
    private Logger logger; // Mock du logger pour vérifier les messages

    @Mock
    private NotificationService notificationService; // Mock du service de notification

    @Value("${stripe.webhook.secret}") // Ceci ne sera pas injecté dans un test unitaire standard
    private String webhookSecret;

    @Value("${stripe.api.secretKey}") // Ceci ne sera pas injecté dans un test unitaire standard
    private String apiKey;

    @BeforeEach
    void setUp() {
        // Injecter la clé secrète du webhook dans le contrôleur en utilisant la
        // réflexion
        ReflectionTestUtils.setField(stripeWebhookController, "webhookSecret", "testWebhookSecret");
        // Injecter la clé API Stripe (même si elle n'est pas directement utilisée dans
        // handleWebhook pour la vérification)
        ReflectionTestUtils.setField(stripeWebhookController, "apiKey", "testApiKey");
        // Initialiser Stripe avec la clé API pour éviter les erreurs lors de la
        // construction du contrôleur
        Stripe.apiKey = "testApiKey";
    }

    @Test
    void handleStripeWebhook_shouldReturnBadRequest_whenSignatureIsMissing() {
        // GIVEN : Un payload de webhook et une signature manquante dans l'en-tête
        String payload = "{\"type\": \"invoice.payment_succeeded\", \"data\": {\"object\": {\"id\": \"in_test\"}}}";
        String sigHeader = null;

        // WHEN : L'appel à la méthode handleStripeWebhook
        ResponseEntity<String> response = stripeWebhookController.handleStripeWebhook(payload, sigHeader);

        // THEN : Vérification que la réponse est BadRequest et contient le message
        // d'erreur attendu
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Webhook signature missing", response.getBody());
        verify(logger).error("Webhook signature missing"); // Vérifie que le logger a enregistré l'erreur
    }

    @Test
    void handleStripeWebhook_shouldReturnBadRequest_whenSignatureVerificationFails() throws Exception {
        // GIVEN : Un payload de webhook et une signature invalide
        String payload = "{\"type\": \"invoice.payment_succeeded\", \"data\": {\"object\": {\"id\": \"in_test\"}}}";
        String sigHeader = "invalid_signature";

        // Mock de la méthode Webhook.constructEvent pour lancer une
        // SignatureVerificationException
        when(Webhook.constructEvent(payload, sigHeader, "testWebhookSecret"))
                .thenThrow(new SignatureVerificationException("Signature verification failed", sigHeader));

        // WHEN : L'appel à la méthode handleStripeWebhook
        ResponseEntity<String> response = stripeWebhookController.handleStripeWebhook(payload, sigHeader);

        // THEN : Vérification que la réponse est BadRequest et contient le message
        // d'erreur de vérification
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Signature verification failed", response.getBody());
        verify(logger).error(eq("Webhook signature verification failed"), any(SignatureVerificationException.class)); // Vérifie
                                                                                                                      // l'erreur
                                                                                                                      // loguée
    }

    @Test
    void handleStripeWebhook_shouldReturnBadRequest_whenEventObjectDeserializationFails() throws Exception {
        // GIVEN : Un payload de webhook valide (pour la signature) mais avec des
        // données incorrectes
        String payload = "{\"type\": \"invalid.event\", \"data\": {\"object\": {}}}";
        String sigHeader = "valid_signature";
        Event mockEvent = mock(Event.class);
        EventDataObjectDeserializer mockDeserializer = mock(EventDataObjectDeserializer.class);

        // Mock de Webhook.constructEvent pour retourner un événement valide (signature
        // vérifiée)
        when(Webhook.constructEvent(payload, sigHeader, "testWebhookSecret")).thenReturn(mockEvent);
        when(mockEvent.getDataObjectDeserializer()).thenReturn(mockDeserializer);
        when(mockDeserializer.getObject()).thenReturn(Optional.empty()); // Simule une déserialisation échouée
        when(mockEvent.getId()).thenReturn("evt_test");

        // WHEN : L'appel à la méthode handleStripeWebhook
        ResponseEntity<String> response = stripeWebhookController.handleStripeWebhook(payload, sigHeader);

        // THEN : Vérification que la réponse est BadRequest et contient le message
        // d'erreur de payload invalide
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid payload", response.getBody());
        verify(logger).warn("Failed to deserialize event object: {}", "evt_test"); // Vérifie l'avertissement logué
    }

    @Test
    void handleStripeWebhook_shouldReturnOk_whenEventIsProcessedSuccessfully() throws Exception {
        // GIVEN : Un payload et une signature valides, et la gestion de l'événement
        // réussit
        String payload = "{\"type\": \"invoice.payment_succeeded\", \"data\": {\"object\": {\"id\": \"in_test\"}}}";
        String sigHeader = "valid_signature";
        Event mockEvent = mock(Event.class);
        EventDataObjectDeserializer mockDeserializer = mock(EventDataObjectDeserializer.class);
        Invoice mockInvoice = mock(Invoice.class);

        // Mock de Webhook.constructEvent pour retourner un événement valide
        when(Webhook.constructEvent(payload, sigHeader, "testWebhookSecret")).thenReturn(mockEvent);
        when(mockEvent.getDataObjectDeserializer()).thenReturn(mockDeserializer);
        when(mockDeserializer.getObject()).thenReturn(Optional.of(mockInvoice));
        when(mockEvent.getType()).thenReturn("invoice.payment_succeeded");
        when(mockInvoice.getId()).thenReturn("in_test");

        // WHEN : L'appel à la méthode handleStripeWebhook
        ResponseEntity<String> response = stripeWebhookController.handleStripeWebhook(payload, sigHeader);

        // THEN : Vérification que la réponse est OK et contient le message de succès
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Webhook processed successfully", response.getBody());
        verify(logger).info("Invoice payment succeeded: {}", "in_test"); // Vérifie que l'événement a été géré (logué
                                                                         // ici)
    }

    @Test
    void handleStripeWebhook_shouldReturnInternalServerError_whenAnUnexpectedErrorOccurs() throws Exception {
        // GIVEN : Un payload et une signature valides, mais une exception inattendue se
        // produit lors de la vérification
        String payload = "{\"type\": \"invoice.payment_succeeded\", \"data\": {\"object\": {\"id\": \"in_test\"}}}";
        String sigHeader = "valid_signature";

        // Mock de Webhook.constructEvent pour lancer une exception générique
        when(Webhook.constructEvent(payload, sigHeader, "testWebhookSecret"))
                .thenThrow(new RuntimeException("Unexpected error"));

        // WHEN : L'appel à la méthode handleStripeWebhook
        ResponseEntity<String> response = stripeWebhookController.handleStripeWebhook(payload, sigHeader);

        // THEN : Vérification que la réponse est InternalServerError et contient un
        // message d'erreur générique
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Error processing webhook", response.getBody());
        verify(logger).error(eq("Error processing webhook"), any(RuntimeException.class)); // Vérifie l'erreur loguée
    }

    @Test
    void handleEvent_shouldLogInvoicePaymentSucceeded() throws Exception {
        // GIVEN : Un type d'événement et un objet Stripe (Invoice) mocké
        String eventType = "invoice.payment_succeeded";
        Invoice mockInvoice = mock(Invoice.class);
        when(mockInvoice.getId()).thenReturn("in_success_test");

        // Utilisation de la réflexion pour accéder à la méthode privée handleEvent
        java.lang.reflect.Method method = StripeWebhookController.class.getDeclaredMethod("handleEvent", String.class,
                StripeObject.class);
        method.setAccessible(true); // Permet d'accéder à la méthode privée

        // WHEN : L'invocation de la méthode handleEvent
        method.invoke(stripeWebhookController, eventType, mockInvoice);

        // THEN : Vérification que le logger a été appelé correctement
        verify(logger).info("Invoice payment succeeded: {}", "in_success_test");
        // Ajouter ici d'autres vérifications si la logique de handleEvent était plus
        // complexe
    }

    @Test
    void handleEvent_shouldLogErrorInvoicePaymentFailed() throws Exception {
        // GIVEN : Un type d'événement et un objet Stripe (Invoice) mocké
        String eventType = "invoice.payment_failed";
        Invoice mockInvoice = mock(Invoice.class);
        when(mockInvoice.getId()).thenReturn("in_failed_test");

        // Utilisation de la réflexion pour accéder à la méthode privée handleEvent
        java.lang.reflect.Method method = StripeWebhookController.class.getDeclaredMethod("handleEvent", String.class,
                StripeObject.class);
        method.setAccessible(true);

        // WHEN : L'invocation de la méthode handleEvent
        method.invoke(stripeWebhookController, eventType, mockInvoice);

        // THEN : Vérification que le logger d'erreur a été appelé correctement
        verify(logger).error("Invoice payment failed: {}", "in_failed_test");
    }

    @Test
    void handleEvent_shouldLogSubscriptionCreated() throws Exception {
        // GIVEN : Un type d'événement et un objet Stripe (Subscription - ici représenté
        // par un mock générique)
        String eventType = "customer.subscription.created";
        StripeObject mockSubscription = mock(StripeObject.class);
        when(mockSubscription.toString()).thenReturn("sub_created_test"); // Simuler une représentation de l'objet

        // Utilisation de la réflexion pour accéder à la méthode privée handleEvent
        java.lang.reflect.Method method = StripeWebhookController.class.getDeclaredMethod("handleEvent", String.class,
                StripeObject.class);
        method.setAccessible(true);

        // WHEN : L'invocation de la méthode handleEvent
        method.invoke(stripeWebhookController, eventType, mockSubscription);

        // THEN : Vérification que le logger info a été appelé correctement
        verify(logger).info("Subscription created: {}", "sub_created_test");
        // Ajouter ici d'autres vérifications si la logique pour cet événement était
        // plus complexe
    }

    @Test
    void handleEvent_shouldLogSubscriptionUpdated() throws Exception {
        // GIVEN : Un type d'événement et un objet Stripe (Subscription) mocké
        String eventType = "customer.subscription.updated";
        StripeObject mockSubscription = mock(StripeObject.class);
        when(mockSubscription.toString()).thenReturn("sub_updated_test");

        // Utilisation de la réflexion pour accéder à la méthode privée handleEvent
        java.lang.reflect.Method method = StripeWebhookController.class.getDeclaredMethod("handleEvent", String.class,
                StripeObject.class);
        method.setAccessible(true);

        // WHEN : L'invocation de la méthode handleEvent
        method.invoke(stripeWebhookController, eventType, mockSubscription);

        // THEN : Vérification que le logger info a été appelé correctement
        verify(logger).info("Subscription updated: {}", "sub_updated_test");
        // Ajouter ici d'autres vérifications spécifiques à la logique de mise à jour
        // d'abonnement
    }

    @Test
    void handleEvent_shouldLogSubscriptionDeleted() throws Exception {
        // GIVEN : Un type d'événement et un objet Stripe (Subscription) mocké
        String eventType = "customer.subscription.deleted";
        StripeObject mockSubscription = mock(StripeObject.class);
        when(mockSubscription.toString()).thenReturn("sub_deleted_test");

        // Utilisation de la réflexion pour accéder à la méthode privée handleEvent
        java.lang.reflect.Method method = StripeWebhookController.class.getDeclaredMethod("handleEvent", String.class,
                StripeObject.class);
        method.setAccessible(true);

        // WHEN : L'invocation de la méthode handleEvent
        method.invoke(stripeWebhookController, eventType, mockSubscription);

        // THEN : Vérification que le logger info a été appelé correctement
        verify(logger).info("Subscription deleted: {}", "sub_deleted_test");
        // Ajouter ici des vérifications pour toute logique de nettoyage ou de
        // désactivation associée à la suppression d'abonnement
    }

    @Test
    void handleEvent_shouldLogTrialWillEnd() throws Exception {
        // GIVEN : Un type d'événement et un objet Stripe (Subscription) mocké
        String eventType = "customer.subscription.trial_will_end";
        StripeObject mockSubscription = mock(StripeObject.class);
        when(mockSubscription.toString()).thenReturn("trial_end_test");

        // Utilisation de la réflexion pour accéder à la méthode privée handleEvent
        java.lang.reflect.Method method = StripeWebhookController.class.getDeclaredMethod("handleEvent", String.class,
                StripeObject.class);
        method.setAccessible(true);

        // WHEN : L'invocation de la méthode handleEvent
        method.invoke(stripeWebhookController, eventType, mockSubscription);

        // THEN : Vérification que le logger info a été appelé correctement
        verify(logger).info("Trial will end for subscription: {}", "trial_end_test");
    }

    @Test
    void handleEvent_shouldLogTrialWillEndAndTriggerNotification() throws Exception {
        // GIVEN : Un type d'événement et un objet Stripe (Subscription) mocké
        String eventType = "customer.subscription.trial_will_end";
        StripeObject mockSubscription = mock(StripeObject.class);
        when(mockSubscription.toString()).thenReturn("trial_end_test");

        // Utilisation de la réflexion pour accéder à la méthode privée handleEvent
        java.lang.reflect.Method method = StripeWebhookController.class.getDeclaredMethod("handleEvent", String.class,
                StripeObject.class);
        method.setAccessible(true);

        // WHEN : L'invocation de la méthode handleEvent
        method.invoke(stripeWebhookController, eventType, mockSubscription);

        // THEN : Vérification que le logger info a été appelé
        verify(logger).info("Trial will end for subscription: {}", "trial_end_test");

        // ET : Vérification que la méthode CORRECTE du service de notification a été
        // appelée
        verify(notificationService, times(1)).sendTrialPeriodEndingNotification(null, null);
    }

    @Test
    void handleEvent_shouldLogUnhandledEvent() throws Exception {
        // GIVEN : Un type d'événement non géré
        String eventType = "unknown.event";
        StripeObject mockStripeObject = mock(StripeObject.class);

        // Utilisation de la réflexion pour accéder à la méthode privée handleEvent
        java.lang.reflect.Method method = StripeWebhookController.class.getDeclaredMethod("handleEvent", String.class,
                StripeObject.class);
        method.setAccessible(true);

        // WHEN : L'invocation de la méthode handleEvent
        method.invoke(stripeWebhookController, eventType, mockStripeObject);

        // THEN : Vérification que le logger info a été appelé pour l'événement non géré
        verify(logger).info("Unhandled event type: {}", "unknown.event");
    }
}