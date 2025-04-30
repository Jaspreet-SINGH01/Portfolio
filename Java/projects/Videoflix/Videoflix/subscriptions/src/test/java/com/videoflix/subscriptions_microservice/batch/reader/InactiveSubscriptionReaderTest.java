package com.videoflix.subscriptions_microservice.batch.reader;

import com.videoflix.subscriptions_microservice.entities.Subscription;
import com.videoflix.subscriptions_microservice.repositories.SubscriptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class InactiveSubscriptionReaderTest {

    @Mock
    private SubscriptionRepository subscriptionRepository; // Mock du repository

    private InactiveSubscriptionReader reader; // L'instance du reader à tester
    private LocalDateTime cutoffDate;

    @BeforeEach
    void setUp() {
        cutoffDate = LocalDateTime.now().minus(90, ChronoUnit.DAYS);
        reader = new InactiveSubscriptionReader(subscriptionRepository, cutoffDate);
    }

    @Test
    void read_shouldReturnInactiveSubscriptionsBeforeCutoffDate() throws Exception {
        // GIVEN : Configuration du comportement mocké du repository

        List<Subscription> expectedSubscriptions = Arrays.asList(
                new Subscription(),
                new Subscription());

        when(subscriptionRepository.findByStatusAndLastActivityBefore(
                Subscription.SubscriptionStatus.INACTIVE,
                cutoffDate,
                (Pageable) Sort.by(Sort.Direction.ASC, "id") // Vérification de l'ordre
        )).thenReturn(expectedSubscriptions);

        // WHEN : Lecture des abonnements
        Subscription subscription1 = reader.read();
        Subscription subscription2 = reader.read();
        Subscription subscription3 = reader.read(); // Doit retourner null après avoir lu tous les éléments

        // THEN : Vérification que les abonnements lus correspondent aux attentes
        assertEquals(expectedSubscriptions.get(0), subscription1);
        assertEquals(expectedSubscriptions.get(1), subscription2);
        assertNull(subscription3);

        // Vérification que la méthode du repository a été appelée avec les bons
        // arguments
        verify(subscriptionRepository, times(1)).findByStatusAndLastActivityBefore(
                Subscription.SubscriptionStatus.INACTIVE,
                cutoffDate,
                (Pageable) Sort.by(Sort.Direction.ASC, "id"));
    }

    @Test
    void read_shouldReturnNull_whenNoInactiveSubscriptionsBeforeCutoffDate() throws Exception {
        // GIVEN : Configuration du repository pour retourner une liste vide

        when(subscriptionRepository.findByStatusAndLastActivityBefore(
                Subscription.SubscriptionStatus.INACTIVE,
                cutoffDate,
                (Pageable) Sort.by(Sort.Direction.ASC, "id"))).thenReturn(Collections.emptyList());

        // WHEN : Lecture de l'abonnement
        Subscription subscription = reader.read();

        // THEN : Vérification que null est retourné
        assertNull(subscription);

        // Vérification que la méthode du repository a été appelée
        verify(subscriptionRepository, times(1)).findByStatusAndLastActivityBefore(
                Subscription.SubscriptionStatus.INACTIVE,
                cutoffDate,
                (Pageable) Sort.by(Sort.Direction.ASC, "id"));
    }

    @Test
    void open_shouldInitializeRepositoryItemReader() throws Exception {
        // GIVEN : Le reader est créé dans le setUp

        // WHEN : On appelle la méthode read() une fois
        reader.read();

        // THEN : Vérification que la méthode du repository a été appelée
        verify(subscriptionRepository, times(1)).findByStatusAndLastActivityBefore(
                eq(Subscription.SubscriptionStatus.INACTIVE),
                eq(cutoffDate),
                (Pageable) any(Sort.class) // On ne vérifie pas l'ordre ici, car c'est testé ailleurs
        );
    }
}