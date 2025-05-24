package com.videoflix.subscriptions_microservice.tasks;

import com.videoflix.subscriptions_microservice.entities.Subscription;
import com.videoflix.subscriptions_microservice.repositories.SubscriptionRepository;
import com.videoflix.subscriptions_microservice.services.DataSynchronizationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils; // Pour injecter les valeurs @Value

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

// @ExtendWith(MockitoExtension.class) permet l'initialisation des mocks par Mockito.
@ExtendWith(MockitoExtension.class)
@DisplayName("Tests pour DataSynchronizationTask")
class DataSynchronizationTaskTest {

    // @Mock crée un mock du repository des abonnements.
    // Nous allons contrôler son comportement pour simuler des données de base de
    // données.
    @Mock
    private SubscriptionRepository subscriptionRepository;

    // @Mock crée un mock du service de synchronisation de données.
    // Nous allons vérifier que la tâche appelle les méthodes de ce service.
    @Mock
    private DataSynchronizationService dataSynchronizationService;

    // @InjectMocks crée une instance de DataSynchronizationTask et injecte
    // les mocks (subscriptionRepository et dataSynchronizationService) dans son
    // constructeur.
    @InjectMocks
    private DataSynchronizationTask dataSynchronizationTask;

    // Horodatage de synchronisation initial pour nos tests.
    private final LocalDateTime initialSyncTimestamp = LocalDateTime.of(2024, 1, 1, 0, 0);

    /**
     * Méthode exécutée avant chaque test.
     * Configure les valeurs des champs annotés par @Value dans la tâche,
     * et réinitialise les mocks pour garantir l'indépendance des tests.
     */
    @BeforeEach
    void setUp() {
        // Injecte la valeur 'true' pour 'synchronizationEnabled', activant ainsi la
        // tâche par défaut.
        ReflectionTestUtils.setField(dataSynchronizationTask, "synchronizationEnabled", true);
        // Injecte une taille de lot de 2 pour faciliter les tests de pagination.
        ReflectionTestUtils.setField(dataSynchronizationTask, "batchSize", 2);

        // Réinitialise tous les mocks pour s'assurer qu'ils sont propres avant chaque
        // test.
        reset(subscriptionRepository, dataSynchronizationService);

        // Comportement par défaut pour getLastSuccessfulSyncTimestamp : retourne un
        // horodatage fixe.
        when(dataSynchronizationService.getLastSuccessfulSyncTimestamp()).thenReturn(initialSyncTimestamp);
    }

    @Test
    @DisplayName("La synchronisation devrait être désactivée si 'synchronizationEnabled' est à false")
    void synchronizeSubscriptionData_shouldBeDisabledWhenFeatureFlagIsFalse() {
        // GIVEN: Le drapeau de fonctionnalité 'synchronizationEnabled' est à false.
        ReflectionTestUtils.setField(dataSynchronizationTask, "synchronizationEnabled", false);

        // WHEN: La tâche de synchronisation est exécutée.
        dataSynchronizationTask.synchronizeSubscriptionData();

        // THEN: Aucune interaction avec les dépendances ne devrait avoir lieu.
        // On vérifie que `getLastSuccessfulSyncTimestamp` N'A PAS été appelé.
        verify(dataSynchronizationService, never()).getLastSuccessfulSyncTimestamp();
        // On vérifie que `findSubscriptionsUpdatedSince` N'A PAS été appelé.
        verify(subscriptionRepository, never()).findSubscriptionsUpdatedSince(any(), anyInt(), anyInt());
        // On vérifie que `synchronizeSubscriptions` N'A PAS été appelé.
        verify(dataSynchronizationService, never()).synchronizeSubscriptions(anyList());
        // On vérifie que `updateLastSuccessfulSyncTimestamp` N'A PAS été appelé.
        verify(dataSynchronizationService, never()).updateLastSuccessfulSyncTimestamp(any());
    }

    @Test
    @DisplayName("Devrait synchroniser les abonnements en un seul lot s'il y a peu de données")
    void synchronizeSubscriptionData_shouldSyncInSingleBatch() {
        // GIVEN: Moins d'abonnements que la taille du lot.
        List<Subscription> subscriptions = Arrays.asList(new Subscription(), new Subscription()); // 2 abonnements,
                                                                                                  // batchSize = 2

        // Configure le repository pour retourner ces abonnements une seule fois, puis
        // une liste vide.
        when(subscriptionRepository.findSubscriptionsUpdatedSince(initialSyncTimestamp, 0, 2))
                .thenReturn(subscriptions)
                .thenReturn(Collections.emptyList()); // Simule la fin des données après le premier appel

        // WHEN: La tâche de synchronisation est exécutée.
        dataSynchronizationTask.synchronizeSubscriptionData();

        // THEN:
        // Vérifie que `getLastSuccessfulSyncTimestamp` a été appelé une fois.
        verify(dataSynchronizationService, times(1)).getLastSuccessfulSyncTimestamp();
        // Vérifie que `findSubscriptionsUpdatedSince` a été appelé deux fois :
        // une fois pour le lot de données, et une deuxième fois pour vérifier qu'il n'y
        // a plus de données.
        verify(subscriptionRepository, times(2)).findSubscriptionsUpdatedSince(eq(initialSyncTimestamp), anyInt(),
                eq(2));
        // Vérifie que `synchronizeSubscriptions` a été appelé une fois avec le lot
        // complet d'abonnements.
        verify(dataSynchronizationService, times(1)).synchronizeSubscriptions(subscriptions);
        // Vérifie que `updateLastSuccessfulSyncTimestamp` a été appelé une fois avec un
        // horodatage récent.
        verify(dataSynchronizationService, times(1)).updateLastSuccessfulSyncTimestamp(any(LocalDateTime.class));
    }

    @Test
    @DisplayName("Devrait synchroniser les abonnements en plusieurs lots si les données dépassent la taille du lot")
    void synchronizeSubscriptionData_shouldSyncInMultipleBatches() {
        // GIVEN: Plus d'abonnements que la taille du lot, nécessitant plusieurs lots.
        // Nous allons simuler 3 abonnements avec un batchSize de 2.
        List<Subscription> batch1 = Arrays.asList(new Subscription(), new Subscription()); // 2 abonnements
        List<Subscription> batch2 = Collections.singletonList(new Subscription()); // 1 abonnement restant

        // Configure le repository pour retourner les lots séquentiellement.
        // Appel 1: (offset 0, limit 2) -> batch1
        // Appel 2: (offset 2, limit 2) -> batch2
        // Appel 3: (offset 4, limit 2) -> liste vide (fin des données)
        when(subscriptionRepository.findSubscriptionsUpdatedSince(initialSyncTimestamp, 0, 2))
                .thenReturn(batch1);
        when(subscriptionRepository.findSubscriptionsUpdatedSince(initialSyncTimestamp, 2, 2))
                .thenReturn(batch2);
        when(subscriptionRepository.findSubscriptionsUpdatedSince(initialSyncTimestamp, 4, 2))
                .thenReturn(Collections.emptyList());

        // WHEN: La tâche de synchronisation est exécutée.
        dataSynchronizationTask.synchronizeSubscriptionData();

        // THEN:
        // Vérifie que `getLastSuccessfulSyncTimestamp` a été appelé une fois.
        verify(dataSynchronizationService, times(1)).getLastSuccessfulSyncTimestamp();
        // Vérifie que `findSubscriptionsUpdatedSince` a été appelé trois fois (pour les
        // deux lots + l'appel final vide).
        verify(subscriptionRepository, times(3)).findSubscriptionsUpdatedSince(eq(initialSyncTimestamp), anyInt(),
                eq(2));
        // Vérifie que `synchronizeSubscriptions` a été appelé une fois pour chaque lot.
        verify(dataSynchronizationService, times(1)).synchronizeSubscriptions(batch1);
        verify(dataSynchronizationService, times(1)).synchronizeSubscriptions(batch2);
        // Vérifie que `synchronizeSubscriptions` a été appelé un total de 2 fois.
        verify(dataSynchronizationService, times(2)).synchronizeSubscriptions(anyList());
        // Vérifie que `updateLastSuccessfulSyncTimestamp` a été appelé une fois.
        verify(dataSynchronizationService, times(1)).updateLastSuccessfulSyncTimestamp(any(LocalDateTime.class));
    }

    @Test
    @DisplayName("Devrait gérer le cas où il n'y a pas d'abonnements à synchroniser")
    void synchronizeSubscriptionData_shouldHandleNoSubscriptionsToSync() {
        // GIVEN: Aucune souscription n'a été mise à jour depuis le dernier horodatage.
        when(subscriptionRepository.findSubscriptionsUpdatedSince(eq(initialSyncTimestamp), anyInt(), anyInt()))
                .thenReturn(Collections.emptyList()); // Retourne immédiatement une liste vide

        // WHEN: La tâche de synchronisation est exécutée.
        dataSynchronizationTask.synchronizeSubscriptionData();

        // THEN:
        // Vérifie que `getLastSuccessfulSyncTimestamp` a été appelé une fois.
        verify(dataSynchronizationService, times(1)).getLastSuccessfulSyncTimestamp();
        // Vérifie que `findSubscriptionsUpdatedSince` a été appelé une seule fois (le
        // premier appel retourne vide).
        verify(subscriptionRepository, times(1)).findSubscriptionsUpdatedSince(initialSyncTimestamp, 0, 2);
        // Vérifie que `synchronizeSubscriptions` N'A PAS été appelé car il n'y a pas
        // d'abonnements.
        verify(dataSynchronizationService, never()).synchronizeSubscriptions(anyList());
        // Vérifie que `updateLastSuccessfulSyncTimestamp` a été appelé une fois.
        verify(dataSynchronizationService, times(1)).updateLastSuccessfulSyncTimestamp(any(LocalDateTime.class));
    }

    @Test
    @DisplayName("Devrait gérer une exception pendant la synchronisation et loguer l'erreur")
    void synchronizeSubscriptionData_shouldHandleExceptionAndLogError() {
        // GIVEN: Une exception se produit lors de la synchronisation des données.
        String errorMessage = "Erreur simulée de synchronisation";
        // Configure le service de synchronisation pour lancer une RuntimeException.
        doThrow(new RuntimeException(errorMessage))
                .when(dataSynchronizationService).synchronizeSubscriptions(anyList());

        // Configure le repository pour retourner un lot initial afin de déclencher
        // l'erreur.
        List<Subscription> subscriptions = Collections.singletonList(new Subscription());
        when(subscriptionRepository.findSubscriptionsUpdatedSince(initialSyncTimestamp, 0, 2))
                .thenReturn(subscriptions);

        // WHEN: La tâche de synchronisation est exécutée.
        dataSynchronizationTask.synchronizeSubscriptionData();

        // THEN:
        // Vérifie que `getLastSuccessfulSyncTimestamp` a été appelé.
        verify(dataSynchronizationService, times(1)).getLastSuccessfulSyncTimestamp();
        // Vérifie que `findSubscriptionsUpdatedSince` a été appelé (au moins une fois
        // avant l'erreur).
        verify(subscriptionRepository, times(1)).findSubscriptionsUpdatedSince(eq(initialSyncTimestamp), anyInt(),
                eq(2));
        // Vérifie que `synchronizeSubscriptions` a été appelé (et a déclenché
        // l'erreur).
        verify(dataSynchronizationService, times(1)).synchronizeSubscriptions(subscriptions);
        // Vérifie que `updateLastSuccessfulSyncTimestamp` N'A PAS été appelé, car la
        // synchronisation a échoué.
        verify(dataSynchronizationService, never()).updateLastSuccessfulSyncTimestamp(any());
        // Bien que nous ne puissions pas directement vérifier le log sans un framework
        // spécifique,
        // nous testons le comportement de gestion d'erreur : l'absence d'exception non
        // gérée.
    }
}