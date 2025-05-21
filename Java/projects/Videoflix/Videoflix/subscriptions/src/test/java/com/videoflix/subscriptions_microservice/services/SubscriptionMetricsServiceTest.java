package com.videoflix.subscriptions_microservice.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class SubscriptionMetricsServiceTest {

    private SubscriptionMetricsService metricsService;

    @BeforeEach
    void setUp() {
        metricsService = new SubscriptionMetricsService();
    }

    @Test
    void testRecordDailyMetric() {
        // Given
        String metricName = "new_subscriptions";
        LocalDateTime now = LocalDateTime.now();
        Integer value = 10;

        // When
        metricsService.recordDailyMetric(metricName, now, value);

        // Then
        Number retrievedValue = metricsService.getDailyMetric(metricName, now.toLocalDate());
        assertEquals(value, retrievedValue);
    }

    @Test
    void testRecordMultipleMetricsForSameDay() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        LocalDate today = now.toLocalDate();

        // When
        metricsService.recordDailyMetric("new_subscriptions", now, 10);
        metricsService.recordDailyMetric("daily_revenue", now, 1500.50);
        metricsService.recordDailyMetric("cancelled_subscriptions", now, 2);

        // Then
        Map<String, Number> todayMetrics = metricsService.getDailyMetrics(today);
        assertEquals(3, todayMetrics.size());
        assertEquals(10, todayMetrics.get("new_subscriptions"));
        assertEquals(1500.50, todayMetrics.get("daily_revenue"));
        assertEquals(2, todayMetrics.get("cancelled_subscriptions"));
    }

    @Test
    void testUpdateExistingMetric() {
        // Given
        String metricName = "new_subscriptions";
        LocalDateTime now = LocalDateTime.now();

        // When
        metricsService.recordDailyMetric(metricName, now, 10);
        metricsService.recordDailyMetric(metricName, now, 15); // Update with new value

        // Then
        Number updatedValue = metricsService.getDailyMetric(metricName, now.toLocalDate());
        assertEquals(15, updatedValue);
    }

    @Test
    void testGetDailyMetricsForNonExistentDate() {
        // Given
        LocalDate futureDate = LocalDate.now().plusMonths(1);

        // When
        Map<String, Number> metrics = metricsService.getDailyMetrics(futureDate);

        // Then
        assertNotNull(metrics);
        assertTrue(metrics.isEmpty());
    }

    @Test
    void testGetDailyMetricForNonExistentMetric() {
        // Given
        LocalDate today = LocalDate.now();
        String nonExistentMetric = "non_existent_metric";

        // When
        Number metricValue = metricsService.getDailyMetric(nonExistentMetric, today);

        // Then
        assertNull(metricValue);
    }

    @Test
    void testRecordMetricsForDifferentDates() {
        // Given
        LocalDateTime today = LocalDateTime.now();
        LocalDateTime yesterday = today.minusDays(1);
        LocalDateTime tomorrow = today.plusDays(1);

        // When
        metricsService.recordDailyMetric("new_subscriptions", today, 10);
        metricsService.recordDailyMetric("new_subscriptions", yesterday, 5);
        metricsService.recordDailyMetric("new_subscriptions", tomorrow, 15);

        // Then
        assertEquals(10, metricsService.getDailyMetric("new_subscriptions", today.toLocalDate()));
        assertEquals(5, metricsService.getDailyMetric("new_subscriptions", yesterday.toLocalDate()));
        assertEquals(15, metricsService.getDailyMetric("new_subscriptions", tomorrow.toLocalDate()));
    }

    @Test
    void testDifferentMetricTypes() {
        // Given
        LocalDateTime now = LocalDateTime.now();

        // When
        metricsService.recordDailyMetric("integer_metric", now, 10);
        metricsService.recordDailyMetric("double_metric", now, 10.5);
        metricsService.recordDailyMetric("long_metric", now, 1000000000L);

        // Then
        assertEquals(10, metricsService.getDailyMetric("integer_metric", now.toLocalDate()));
        assertEquals(10.5, metricsService.getDailyMetric("double_metric", now.toLocalDate()));
        assertEquals(1000000000L, metricsService.getDailyMetric("long_metric", now.toLocalDate()));
    }

    @Test
    void testAggregateMonthlyMetrics() {
        // Cette méthode n'a pas encore d'implémentation concrète selon le code source
        // On teste simplement qu'elle ne génère pas d'exception
        assertDoesNotThrow(() -> metricsService.aggregateMonthlyMetrics());
    }

    @Test
    void testPersistMetrics() {
        // Cette méthode n'a pas encore d'implémentation concrète selon le code source
        // On teste simplement qu'elle ne génère pas d'exception
        assertDoesNotThrow(() -> metricsService.persistMetrics());
    }

    @Test
    void testConcurrentAccess() throws InterruptedException {
        // Simuler un accès concurrent de plusieurs threads
        int threadCount = 10;
        Thread[] threads = new Thread[threadCount];
        LocalDate today = LocalDate.now();

        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            threads[i] = new Thread(() -> {
                LocalDateTime dateTime = LocalDateTime.now();
                String metricName = "concurrent_metric_" + threadId;
                metricsService.recordDailyMetric(metricName, dateTime, threadId);
            });
            threads[i].start();
        }

        // Attendre que tous les threads terminent
        for (Thread thread : threads) {
            thread.join();
        }

        // Vérifier que toutes les métriques ont été enregistrées
        Map<String, Number> metrics = metricsService.getDailyMetrics(today);
        assertEquals(threadCount, metrics.size());

        // Vérifier chaque métrique individuelle
        for (int i = 0; i < threadCount; i++) {
            String metricName = "concurrent_metric_" + i;
            Number value = metricsService.getDailyMetric(metricName, today);
            assertNotNull(value);
            assertEquals(i, value.intValue());
        }
    }
}