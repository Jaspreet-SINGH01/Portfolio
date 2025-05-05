package com.videoflix.subscriptions_microservice.controllers;

import com.videoflix.subscriptions_microservice.services.StatsService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Map;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class AdminStatsControllerTest {

    @InjectMocks
    private AdminStatsController adminStatsController; // L'instance du contrôleur à tester

    @Mock
    private StatsService statsService; // Mock du service de statistiques

    @Test
    void getActiveSubscriberCount_shouldReturnOkWithCount() {
        // GIVEN : Un nombre d'abonnés actifs à retourner par le service
        long activeCount = 150L;
        when(statsService.getTotalActiveSubscribers()).thenReturn(activeCount);

        // WHEN : L'appel à la méthode getActiveSubscriberCount du contrôleur
        ResponseEntity<Long> response = adminStatsController.getActiveSubscriberCount();

        // THEN : Vérification que la réponse est OK et contient le nombre correct
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(activeCount, response.getBody());
        verify(statsService, times(1)).getTotalActiveSubscribers(); // Vérifie que la méthode du service a été appelée
                                                                    // une fois
    }

    @Test
    void getNewSubscriptionCount_shouldReturnOkWithCount_whenDatesAreProvided() {
        // GIVEN : Des dates de début et de fin valides et un nombre de nouveaux
        // abonnements à retourner par le service
        LocalDate startDate = LocalDate.now().minusDays(7);
        LocalDate endDate = LocalDate.now();
        long newCount = 50L;
        when(statsService.getNewSubscriptionsCount(startDate, endDate)).thenReturn(newCount);

        // WHEN : L'appel à la méthode getNewSubscriptionCount du contrôleur avec les
        // dates
        ResponseEntity<Long> response = adminStatsController.getNewSubscriptionCount(startDate, endDate);

        // THEN : Vérification que la réponse est OK et contient le nombre correct
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(newCount, response.getBody());
        verify(statsService, times(1)).getNewSubscriptionsCount(startDate, endDate); // Vérifie l'appel au service avec
                                                                                     // les dates
    }

    @Test
    void getNewSubscriptionCount_shouldReturnBadRequest_whenDatesAreNull() {
        // WHEN : L'appel à la méthode getNewSubscriptionCount du contrôleur sans dates
        ResponseEntity<Long> response = adminStatsController.getNewSubscriptionCount(null, null);

        // THEN : Vérification que la réponse est BadRequest et le corps est 0
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(0L, response.getBody());
        verify(statsService, never()).getNewSubscriptionsCount(any(), any()); // Vérifie que le service n'a pas été
                                                                              // appelé
    }

    @Test
    void getSubscribersByType_shouldReturnOkWithMap() {
        // GIVEN : Une map de types d'abonnés et leurs nombres à retourner par le
        // service
        Map<Object, Long> subscribersByType = Collections.singletonMap("Premium", 100L);
        when(statsService.getSubscribersByType()).thenReturn(subscribersByType);

        // WHEN : L'appel à la méthode getSubscribersByType du contrôleur
        ResponseEntity<Map<Object, Long>> response = adminStatsController.getSubscribersByType();

        // THEN : Vérification que la réponse est OK et contient la map correcte
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(subscribersByType, response.getBody());
        verify(statsService, times(1)).getSubscribersByType(); // Vérifie l'appel au service
    }

    @Test
    void getTotalRevenue_shouldReturnOkWithRevenue_whenDatesAreProvided() {
        // GIVEN : Des dates valides et un revenu total à retourner par le service
        LocalDate startDate = LocalDate.now().minusDays(30);
        LocalDate endDate = LocalDate.now();
        double totalRevenue = 1250.50;
        when(statsService.getTotalRevenue(startDate, endDate)).thenReturn(totalRevenue);

        // WHEN : L'appel à la méthode getTotalRevenue du contrôleur avec les dates
        ResponseEntity<Double> response = adminStatsController.getTotalRevenue(startDate, endDate);

        // THEN : Vérification que la réponse est OK et contient le revenu correct
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(totalRevenue, response.getBody());
        verify(statsService, times(1)).getTotalRevenue(startDate, endDate); // Vérifie l'appel au service avec les dates
    }

    @Test
    void getTotalRevenue_shouldReturnBadRequest_whenDatesAreNull() {
        // WHEN : L'appel à la méthode getTotalRevenue du contrôleur sans dates
        ResponseEntity<Double> response = adminStatsController.getTotalRevenue(null, null);

        // THEN : Vérification que la réponse est BadRequest et le corps est 0.0
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(0.0, response.getBody());
        verify(statsService, never()).getTotalRevenue(any(), any()); // Vérifie que le service n'a pas été appelé
    }

    @Test
    void getRetentionRate_shouldReturnOkWithRate_whenDatesAreProvided() {
        // GIVEN : Des dates valides et un taux de rétention à retourner par le service
        LocalDate startDate = LocalDate.now().minusMonths(1);
        LocalDate endDate = LocalDate.now();
        double retentionRate = 0.85;
        when(statsService.getRetentionRate(startDate, endDate)).thenReturn(retentionRate);

        // WHEN : L'appel à la méthode getRetentionRate du contrôleur avec les dates
        ResponseEntity<Double> response = adminStatsController.getRetentionRate(startDate, endDate);

        // THEN : Vérification que la réponse est OK et contient le taux correct
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(retentionRate, response.getBody());
        verify(statsService, times(1)).getRetentionRate(startDate, endDate); // Vérifie l'appel au service avec les
                                                                             // dates
    }

    @Test
    void getRetentionRate_shouldReturnBadRequest_whenDatesAreNull() {
        // WHEN : L'appel à la méthode getRetentionRate du contrôleur sans dates
        ResponseEntity<Double> response = adminStatsController.getRetentionRate(null, null);

        // THEN : Vérification que la réponse est BadRequest et le corps est 0.0
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(0.0, response.getBody());
        verify(statsService, never()).getRetentionRate(any(), any()); // Vérifie que le service n'a pas été appelé
    }

    @Test
    void getFailedPaymentCount_shouldReturnOkWithCount() {
        // GIVEN : Un nombre de paiements échoués à retourner par le service
        long failedCount = 10L;
        when(statsService.getFailedPaymentCount()).thenReturn(failedCount);

        // WHEN : L'appel à la méthode getFailedPaymentCount du contrôleur
        ResponseEntity<Long> response = adminStatsController.getFailedPaymentCount();

        // THEN : Vérification que la réponse est OK et contient le nombre correct
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(failedCount, response.getBody());
        verify(statsService, times(1)).getFailedPaymentCount(); // Vérifie l'appel au service
    }
}