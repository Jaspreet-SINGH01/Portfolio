package com.videoflix.subscriptions_microservice.services;

import com.videoflix.subscriptions_microservice.entities.Subscription;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.springframework.web.client.RestTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

// @ExtendWith(MockitoExtension.class) permet d'utiliser les fonctionnalités de Mockito dans ce test.
@ExtendWith(MockitoExtension.class)
class DataSynchronizationServiceTest {

    // @Mock crée un mock de l'interface RestTemplate.
    @Mock
    private RestTemplate restTemplate;

    // @Mock crée un mock de l'interface Logger.
    @Mock
    private Logger logger;

    // @InjectMocks crée une instance de DataSynchronizationService et injecte les
    // mocks annotés avec @Mock.
    @InjectMocks
    private DataSynchronizationService dataSynchronizationService;

    // @Captor pour capturer la liste des abonnements envoyée au CRM.
    @Captor
    private ArgumentCaptor<List<Subscription>> crmSubscriptionsCaptor;

    // @Captor pour capturer la liste des abonnements envoyée au système
    // d'analytics.
    @Captor
    private ArgumentCaptor<List<Subscription>> analyticsSubscriptionsCaptor;

    // @Captor pour capturer le payload envoyé à l'analytics lors de l'annulation
    // Stripe.
    @Captor
    private ArgumentCaptor<Map<String, Object>> analyticsCancellationPayloadCaptor;

    // Test pour vérifier que synchronizeSubscriptions envoie correctement les
    // abonnements au CRM si l'URL est configurée.
    @Test
    void synchronizeSubscriptions_shouldSendSubscriptionsToCrmIfUrlConfigured() {
        // GIVEN : Une liste d'abonnements et une URL CRM configurée.
        List<Subscription> subscriptions = Arrays.asList(new Subscription(), new Subscription());
        String crmSyncUrl = "http://crm.example.com/sync";
        ReflectionTestUtils.setField(dataSynchronizationService, "crmSyncUrl", crmSyncUrl);
        ReflectionTestUtils.setField(dataSynchronizationService, "logger", logger);

        // WHEN : Appel de la méthode synchronizeSubscriptions.
        dataSynchronizationService.synchronizeSubscriptions(subscriptions);

        // THEN : Vérification que postForObject sur restTemplate a été appelé avec
        // l'URL CRM et la liste d'abonnements.
        verify(restTemplate, times(1)).postForObject(eq(crmSyncUrl), crmSubscriptionsCaptor.capture(), eq(Void.class));
        assertEquals(subscriptions, crmSubscriptionsCaptor.getValue());
        // Vérification que le logger info a été appelé pour confirmer la
        // synchronisation avec le CRM.
        verify(logger, times(1)).info("{} abonnements synchronisés avec succès avec le CRM à : {}",
                subscriptions.size(), crmSyncUrl);
        // Vérification que le logger warn pour l'URL analytics a également été appelé
        // (si non configurée).
        verify(logger, atLeastOnce()).warn("L'URL de synchronisation du système d'analytics n'est pas configurée.");
    }

    // Test pour vérifier que synchronizeSubscriptions n'appelle pas le CRM si l'URL
    // n'est pas configurée.
    @Test
    void synchronizeSubscriptions_shouldNotCallCrmIfUrlNotConfigured() {
        // GIVEN : Une liste d'abonnements et une URL CRM non configurée (null ou vide).
        List<Subscription> subscriptions = Arrays.asList(new Subscription());
        ReflectionTestUtils.setField(dataSynchronizationService, "crmSyncUrl", null);
        ReflectionTestUtils.setField(dataSynchronizationService, "logger", logger);

        // WHEN : Appel de la méthode synchronizeSubscriptions.
        dataSynchronizationService.synchronizeSubscriptions(subscriptions);

        // THEN : Vérification que postForObject sur restTemplate n'a pas été appelé
        // avec l'URL CRM.
        verify(restTemplate, never()).postForObject(anyString(), anyList(), eq(Void.class));
        // Vérification que le logger warn a été appelé pour indiquer que l'URL CRM
        // n'est pas configurée.
        verify(logger, times(1)).warn("L'URL de synchronisation CRM n'est pas configurée.");
        // Vérification que le logger warn pour l'URL analytics a également été appelé
        // (si non configurée).
        verify(logger, atLeastOnce()).warn("L'URL de synchronisation du système d'analytics n'est pas configurée.");
    }

    // Test pour vérifier la gestion des erreurs lors de la synchronisation avec le
    // CRM.
    @Test
    void synchronizeSubscriptions_shouldLogErrorIfCrmSyncFails() {
        // GIVEN : Une liste d'abonnements, une URL CRM configurée et une exception
        // lancée par restTemplate.
        List<Subscription> subscriptions = Arrays.asList(new Subscription());
        String crmSyncUrl = "http://crm.example.com/sync";
        String errorMessage = "Connection refused";
        RuntimeException exception = new RuntimeException(errorMessage);
        ReflectionTestUtils.setField(dataSynchronizationService, "crmSyncUrl", crmSyncUrl);
        ReflectionTestUtils.setField(dataSynchronizationService, "logger", logger);
        when(restTemplate.postForObject(eq(crmSyncUrl), anyList(), eq(Void.class))).thenThrow(exception);

        // WHEN : Appel de la méthode synchronizeSubscriptions.
        dataSynchronizationService.synchronizeSubscriptions(subscriptions);

        // THEN : Vérification que le logger error a été appelé avec le message d'erreur
        // correct.
        verify(logger, times(1)).error("Erreur lors de la synchronisation avec le CRM à {} : {}", crmSyncUrl,
                errorMessage, exception);
        // Vérification que la tentative d'envoi au CRM a eu lieu.
        verify(restTemplate, times(1)).postForObject(eq(crmSyncUrl), anyList(), eq(Void.class));
        // Vérification que le logger warn pour l'URL analytics a également été appelé
        // (si non configurée).
        verify(logger, atLeastOnce()).warn("L'URL de synchronisation du système d'analytics n'est pas configurée.");
    }

    // Test pour vérifier que synchronizeSubscriptions envoie correctement les
    // abonnements au système d'analytics si l'URL est configurée.
    @Test
    void synchronizeSubscriptions_shouldSendSubscriptionsToAnalyticsIfUrlConfigured() {
        // GIVEN : Une liste d'abonnements et une URL analytics configurée.
        List<Subscription> subscriptions = Arrays.asList(new Subscription(), new Subscription());
        String analyticsSyncUrl = "http://analytics.example.com/sync";
        ReflectionTestUtils.setField(dataSynchronizationService, "analyticsSyncUrl", analyticsSyncUrl);
        ReflectionTestUtils.setField(dataSynchronizationService, "logger", logger);

        // WHEN : Appel de la méthode synchronizeSubscriptions.
        dataSynchronizationService.synchronizeSubscriptions(subscriptions);

        // THEN : Vérification que postForObject sur restTemplate a été appelé avec
        // l'URL analytics et la liste d'abonnements.
        verify(restTemplate, times(1)).postForObject(eq(analyticsSyncUrl), analyticsSubscriptionsCaptor.capture(),
                eq(Void.class));
        assertEquals(subscriptions, analyticsSubscriptionsCaptor.getValue());
        // Vérification que le logger info a été appelé pour confirmer la
        // synchronisation avec l'analytics.
        verify(logger, times(1)).info("{} abonnements synchronisés avec succès avec le système d'analytics à : {}",
                subscriptions.size(), analyticsSyncUrl);
        // Vérification que le logger warn pour l'URL CRM a également été appelé (si non
        // configurée).
        verify(logger, atLeastOnce()).warn("L'URL de synchronisation CRM n'est pas configurée.");
    }

    // Test pour vérifier que synchronizeSubscriptions n'appelle pas le système
    // d'analytics si l'URL n'est pas configurée.
    @Test
    void synchronizeSubscriptions_shouldNotCallAnalyticsIfUrlNotConfigured() {
        // GIVEN : Une liste d'abonnements et une URL analytics non configurée.
        List<Subscription> subscriptions = Arrays.asList(new Subscription());
        ReflectionTestUtils.setField(dataSynchronizationService, "analyticsSyncUrl", null);
        ReflectionTestUtils.setField(dataSynchronizationService, "logger", logger);

        // WHEN : Appel de la méthode synchronizeSubscriptions.
        dataSynchronizationService.synchronizeSubscriptions(subscriptions);

        // THEN : Vérification que postForObject sur restTemplate n'a pas été appelé
        // avec l'URL analytics.
        verify(restTemplate, never()).postForObject(eq(null), anyList(), eq(Void.class));
        // Vérification que le logger warn a été appelé pour indiquer que l'URL
        // analytics n'est pas configurée.
        verify(logger, times(1)).warn("L'URL de synchronisation du système d'analytics n'est pas configurée.");
        // Vérification que le logger warn pour l'URL CRM a également été appelé (si non
        // configurée).
        verify(logger, atLeastOnce()).warn("L'URL de synchronisation CRM n'est pas configurée.");
    }

    // Test pour vérifier la gestion des erreurs lors de la synchronisation avec le
    // système d'analytics.
    @Test
    void synchronizeSubscriptions_shouldLogErrorIfAnalyticsSyncFails() {
        // GIVEN : Une liste d'abonnements, une URL analytics configurée et une
        // exception lancée par restTemplate.
        List<Subscription> subscriptions = Arrays.asList(new Subscription());
        String analyticsSyncUrl = "http://analytics.example.com/sync";
        String errorMessage = "Timeout";
        RuntimeException exception = new RuntimeException(errorMessage);
        ReflectionTestUtils.setField(dataSynchronizationService, "analyticsSyncUrl", analyticsSyncUrl);
        ReflectionTestUtils.setField(dataSynchronizationService, "logger", logger);
        when(restTemplate.postForObject(eq(analyticsSyncUrl), anyList(), eq(Void.class))).thenThrow(exception);

        // WHEN : Appel de la méthode synchronizeSubscriptions.
        dataSynchronizationService.synchronizeSubscriptions(subscriptions);

        // THEN : Vérification que le logger error a été appelé avec le message d'erreur
        // correct.
        verify(logger, times(1)).error("Erreur lors de la synchronisation avec le système d'analytics à {} : {}",
                analyticsSyncUrl, errorMessage, exception);
        // Vérification que la tentative d'envoi à l'analytics a eu lieu.
        verify(restTemplate, times(1)).postForObject(eq(analyticsSyncUrl), anyList(), eq(Void.class));
        // Vérification que le logger warn pour l'URL CRM a également été appelé (si non
        // configurée).
        verify(logger, atLeastOnce()).warn("L'URL de synchronisation CRM n'est pas configurée.");
    }

    // Test pour vérifier que getLastSuccessfulSyncTimestamp retourne la valeur
    // stockée.
    @Test
    void getLastSuccessfulSyncTimestamp_shouldReturnStoredTimestamp() {
        // GIVEN : Un horodatage de dernière synchronisation stocké.
        LocalDateTime timestamp = LocalDateTime.now().minusHours(1);
        ReflectionTestUtils.setField(dataSynchronizationService, "lastSuccessfulSyncTimestamp", timestamp);

        // WHEN : Appel de getLastSuccessfulSyncTimestamp.
        LocalDateTime returnedTimestamp = dataSynchronizationService.getLastSuccessfulSyncTimestamp();

        // THEN : Vérification que l'horodatage retourné est le même que celui stocké.
        assertEquals(timestamp, returnedTimestamp);
    }

    // Test pour vérifier que updateLastSuccessfulSyncTimestamp met à jour la valeur
    // stockée et enregistre un log.
    @Test
    void updateLastSuccessfulSyncTimestamp_shouldUpdateTimestampAndLog() {
        // GIVEN : Un nouvel horodatage.
        LocalDateTime newTimestamp = LocalDateTime.now();
        ReflectionTestUtils.setField(dataSynchronizationService, "logger", logger);

        // WHEN : Appel de updateLastSuccessfulSyncTimestamp.
        dataSynchronizationService.updateLastSuccessfulSyncTimestamp(newTimestamp);

        // THEN : Vérification que l'horodatage stocké a été mis à jour.
        assertEquals(newTimestamp,
                ReflectionTestUtils.getField(dataSynchronizationService, "lastSuccessfulSyncTimestamp"));
        // Vérification que le logger info a été appelé avec le nouvel horodatage.
        verify(logger, times(1)).info("Horodatage de la dernière synchronisation mis à jour à : {}", newTimestamp);
    }

    // Test pour vérifier que notifyAnalyticsSubscriptionCancelledOnStripe envoie
    // une notification à l'analytics si l'URL est configurée.
    @Test
    void notifyAnalyticsSubscriptionCancelledOnStripe_shouldSendNotificationIfUrlConfigured() {
        // GIVEN : Un ID d'abonnement Stripe, une date d'annulation et une URL analytics
        // configurée pour les annulations.
        String stripeSubscriptionId = "sub_123";
        LocalDateTime cancellationDate = LocalDateTime.now();
        String analyticsCancelledUrl = "http://analytics.example.com/cancelled";
        ReflectionTestUtils.setField(dataSynchronizationService, "analyticsSubscriptionCancelledUrl",
                analyticsCancelledUrl);
        ReflectionTestUtils.setField(dataSynchronizationService, "logger", logger);

        // WHEN : Appel de notifyAnalyticsSubscriptionCancelledOnStripe.
        dataSynchronizationService.notifyAnalyticsSubscriptionCancelledOnStripe(stripeSubscriptionId, cancellationDate);

        // THEN : Vérification que postForObject a été appelé avec l'URL, le payload et
        // le type Void.class.
        verify(restTemplate, times(1)).postForObject(eq(analyticsCancelledUrl),
                analyticsCancellationPayloadCaptor.capture(), eq(Void.class));
        Map<String, Object> payload = analyticsCancellationPayloadCaptor.getValue();
        assertEquals(stripeSubscriptionId, payload.get("stripeSubscriptionId"));
        assertEquals(cancellationDate.toString(), payload.get("cancellationDate"));
        // Vérification que le logger info a été appelé pour confirmer l'envoi.
        verify(logger, times(1)).info(
                "Notification d'annulation d'abonnement Stripe (ID: {}) envoyée avec succès à l'analytics à : {}",
                stripeSubscriptionId, analyticsCancelledUrl);
    }

    // Test pour vérifier que notifyAnalyticsSubscriptionCancelledOnStripe n'envoie
    // rien si l'URL analytics pour les annulations n'est pas configurée.
    @Test
    void notifyAnalyticsSubscriptionCancelledOnStripe_shouldNotSendNotificationIfUrlNotConfigured() {
        // GIVEN : Un ID d'abonnement Stripe, une date d'annulation et une URL analytics
        // non configurée.
        String stripeSubscriptionId = "sub_456";
        LocalDateTime cancellationDate = LocalDateTime.now().minusDays(1);
        ReflectionTestUtils.setField(dataSynchronizationService, "analyticsSubscriptionCancelledUrl", null);
        ReflectionTestUtils.setField(dataSynchronizationService, "logger", logger);

        // WHEN : Appel de notifyAnalyticsSubscriptionCancelledOnStripe.
        dataSynchronizationService.notifyAnalyticsSubscriptionCancelledOnStripe(stripeSubscriptionId, cancellationDate);

        // THEN : Vérification que postForObject n'a jamais été appelé.
        verify(restTemplate, never()).postForObject(anyString(), anyMap(), eq(Void.class));
        // Vérification que le logger warn a été appelé pour indiquer que l'URL n'est
        // pas configurée.
        verify(logger, times(1)).warn(
                "L'URL de notification d'annulation d'abonnement Stripe pour l'analytics n'est pas configurée.");
    }

    // Test pour vérifier la gestion des erreurs lors de l'envoi de la notification
    // d'annulation à l'analytics.
    @Test
    void notifyAnalyticsSubscriptionCancelledOnStripe_shouldLogErrorIfAnalyticsNotificationFails() {
        // GIVEN : Un ID d'abonnement Stripe, une date d'annulation, une URL configurée
        // et une exception lancée par restTemplate.
        String stripeSubscriptionId = "sub_789";
        LocalDateTime cancellationDate = LocalDateTime.now();
        String analyticsCancelledUrl = "http://analytics.example.com/cancelled";
        String errorMessage = "Service unavailable";
        RuntimeException exception = new RuntimeException(errorMessage);
        ReflectionTestUtils.setField(dataSynchronizationService, "analyticsSubscriptionCancelledUrl",
                analyticsCancelledUrl);
        ReflectionTestUtils.setField(dataSynchronizationService, "logger", logger);
        when(restTemplate.postForObject(eq(analyticsCancelledUrl), anyMap(), eq(Void.class))).thenThrow(exception);

        // WHEN : Appel de notifyAnalyticsSubscriptionCancelledOnStripe.
        dataSynchronizationService.notifyAnalyticsSubscriptionCancelledOnStripe(stripeSubscriptionId, cancellationDate);

        // THEN : Vérification que postForObject a été appelé.
        verify(restTemplate, times(1)).postForObject(eq(analyticsCancelledUrl), anyMap(), eq(Void.class));
        // Vérification que le logger error a été appelé avec le message d'erreur
        // correct.
        verify(logger, times(1)).error(
                "Erreur lors de l'envoi de la notification d'annulation d'abonnement Stripe (ID: {}) à l'analytics à {} : {}",
                stripeSubscriptionId, analyticsCancelledUrl, errorMessage, exception);
    }
}