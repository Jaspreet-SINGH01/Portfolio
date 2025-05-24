package com.videoflix.subscriptions_microservice.tasks;

import com.videoflix.subscriptions_microservice.entities.Subscription;
import com.videoflix.subscriptions_microservice.entities.User; // Importation pour créer des objets User factices
import com.videoflix.subscriptions_microservice.repositories.SubscriptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils; // Pour injecter les valeurs des champs @Value

import java.time.LocalDateTime;
import java.time.Period;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

// @ExtendWith(MockitoExtension.class) permet d'initialiser les mocks automatiquement avant chaque test.
@ExtendWith(MockitoExtension.class)
@DisplayName("Tests pour InactiveSubscriptionCleanupTask")
class InactiveSubscriptionCleanupTaskTest {

    // @Mock crée une instance mockée de SubscriptionRepository.
    // Nous allons simuler les comportements de recherche, sauvegarde et suppression
    // du repository.
    @Mock
    private SubscriptionRepository subscriptionRepository;

    // @InjectMocks crée une instance de InactiveSubscriptionCleanupTask et injecte
    // le mock.
    @InjectMocks
    private InactiveSubscriptionCleanupTask cleanupTask;

    // Horodatage fixe pour simuler 'LocalDateTime.now()' afin de rendre les tests
    // déterministes.
    private LocalDateTime fixedNow;

    /**
     * Méthode exécutée avant chaque test.
     * Configure les valeurs des champs annotés par @Value dans la tâche,
     * et initialise `fixedNow` pour les calculs de seuil.
     */
    @BeforeEach
    void setUp() {
        // Définit une date et heure fixe pour "aujourd'hui" (le 24 mai 2025 à 5h du
        // matin)
        // cela correspond à l'heure d'exécution planifiée de la tâche.
        fixedNow = LocalDateTime.of(2025, 5, 24, 5, 0, 0);

        // Injecte les périodes de rétention/suppression.
        // P90D = 90 jours
        ReflectionTestUtils.setField(cleanupTask, "cancelledRetentionPeriod", Period.ofDays(90));
        // P365D = 365 jours
        ReflectionTestUtils.setField(cleanupTask, "inactiveDeletionPeriod", Period.ofDays(365));

        // Réinitialise les mocks pour garantir l'indépendance des tests.
        reset(subscriptionRepository);

        // Puisque LocalDateTime.now() est utilisé directement dans la tâche,
        // nous devons simuler le comportement du repository en fonction de nos seuils
        // calculés avec `fixedNow`.
        // Comportement par défaut des méthodes de recherche : retournent des listes
        // vides.
        when(subscriptionRepository.findByStatusAndCancellationDateBefore(any(), any()))
                .thenReturn(Collections.emptyList());
        when(subscriptionRepository.findInactiveBefore(any()))
                .thenReturn(Collections.emptyList());
    }

    @Test
    @DisplayName("Devrait archiver les abonnements annulés qui ont dépassé la période de rétention")
    void cleanupInactiveSubscriptions_shouldArchiveCancelledSubscriptions() {
        // GIVEN: Un abonnement annulé il y a plus de 90 jours (la période de
        // rétention).
        User user = new User();
        user.setId(1L);
        Subscription subToArchive = new Subscription();
        subToArchive.setId(1L);
        subToArchive.setStatus(Subscription.SubscriptionStatus.CANCELLED);
        subToArchive.setCancelledAt(fixedNow.minusDays(100)); // Annulé il y a 100 jours
        subToArchive.setUser(user);

        List<Subscription> cancelledSubs = Collections.singletonList(subToArchive);

        // Calcule le seuil d'archivage attendu.
        LocalDateTime expectedArchiveThreshold = fixedNow.minus(Period.ofDays(90));

        // Configure le repository pour retourner cet abonnement pour l'archivage.
        when(subscriptionRepository.findByStatusAndCancellationDateBefore(
                Subscription.SubscriptionStatus.CANCELLED,
                expectedArchiveThreshold))
                .thenReturn(cancelledSubs);

        // Configure le repository pour simuler la sauvegarde de l'abonnement archivé.
        when(subscriptionRepository.save(any(Subscription.class))).thenReturn(subToArchive);

        // WHEN: La tâche de nettoyage est exécutée.
        cleanupTask.cleanupInactiveSubscriptions();

        // THEN:
        // Vérifie que `findByStatusAndCancellationDateBefore` a été appelé avec les
        // bons paramètres.
        verify(subscriptionRepository, times(1))
                .findByStatusAndCancellationDateBefore(Subscription.SubscriptionStatus.CANCELLED,
                        expectedArchiveThreshold);

        // Vérifie que le statut de l'abonnement a été mis à jour à ARCHIVED.
        assertEquals(Subscription.SubscriptionStatus.ARCHIVED, subToArchive.getStatus(),
                "Le statut de l'abonnement devrait être ARCHIVED.");

        // Vérifie que l'abonnement mis à jour a été sauvegardé.
        verify(subscriptionRepository, times(1)).save(subToArchive);

        // Vérifie qu'aucune suppression n'a eu lieu dans ce scénario.
        verify(subscriptionRepository, never()).delete(any(Subscription.class));
    }

    @Test
    @DisplayName("Ne devrait PAS archiver les abonnements annulés qui sont dans la période de rétention")
    void cleanupInactiveSubscriptions_shouldNotArchiveRecentCancelledSubscriptions() {
        // GIVEN: Un abonnement annulé il y a moins de 90 jours.
        User user = new User();
        user.setId(2L);
        Subscription subToKeep = new Subscription();
        subToKeep.setId(2L);
        subToKeep.setStatus(Subscription.SubscriptionStatus.CANCELLED);
        subToKeep.setCancelledAt(fixedNow.minusDays(50)); // Annulé il y a 50 jours
        subToKeep.setUser(user);

        // Configure le repository pour ne retourner aucun abonnement répondant au seuil
        // d'archivage.
        // Simule que `findByStatusAndCancellationDateBefore` avec le seuil (90 jours)
        // ne trouve rien.
        when(subscriptionRepository.findByStatusAndCancellationDateBefore(
                eq(Subscription.SubscriptionStatus.CANCELLED),
                any(LocalDateTime.class))) // On met any() car on a déjà configuré un seuil de 90 jours
                .thenReturn(Collections.emptyList());

        // WHEN: La tâche de nettoyage est exécutée.
        cleanupTask.cleanupInactiveSubscriptions();

        // THEN:
        // Vérifie que `findByStatusAndCancellationDateBefore` a été appelé.
        verify(subscriptionRepository, times(1))
                .findByStatusAndCancellationDateBefore(eq(Subscription.SubscriptionStatus.CANCELLED),
                        any(LocalDateTime.class));

        // Vérifie qu'aucun abonnement n'a été sauvegardé (non archivé).
        verify(subscriptionRepository, never()).save(any(Subscription.class));
        // Vérifie qu'aucun abonnement n'a été supprimé.
        verify(subscriptionRepository, never()).delete(any(Subscription.class));
    }

    @Test
    @DisplayName("Devrait supprimer les abonnements inactifs qui ont dépassé la période de suppression")
    void cleanupInactiveSubscriptions_shouldDeleteOldInactiveSubscriptions() {
        // GIVEN: Un abonnement inactif datant de plus de 365 jours (la période de
        // suppression).
        User user = new User();
        user.setId(3L);
        Subscription subToDelete = new Subscription();
        subToDelete.setId(3L);
        // Supposons qu'un abonnement inactif est défini par son `endDate` très ancien
        // ou un statut spécifique.
        // Ici, `findInactiveBefore` semble cibler les abonnements par leur `endDate` ou
        // `cancellationDate`
        // selon l'implémentation réelle du repository. Pour ce test, nous utilisons la
        // logique basée sur `fixedNow`.
        subToDelete.setEndDate(fixedNow.minusDays(400)); // Fin de l'abonnement il y a 400 jours
        subToDelete.setStatus(Subscription.SubscriptionStatus.EXPIRED); // Ou tout autre statut "inactif"
        subToDelete.setUser(user);

        List<Subscription> oldInactiveSubs = Collections.singletonList(subToDelete);

        // Calcule le seuil de suppression attendu.
        LocalDateTime expectedDeletionThreshold = fixedNow.minus(Period.ofDays(365));

        // Configure le repository pour retourner cet abonnement pour la suppression.
        when(subscriptionRepository.findInactiveBefore(expectedDeletionThreshold))
                .thenReturn(oldInactiveSubs);

        // Assurez-vous que l'appel d'archivage ne trouve rien pour ce test.
        when(subscriptionRepository.findByStatusAndCancellationDateBefore(any(), any()))
                .thenReturn(Collections.emptyList());

        // WHEN: La tâche de nettoyage est exécutée.
        cleanupTask.cleanupInactiveSubscriptions();

        // THEN:
        // Vérifie que `findInactiveBefore` a été appelé avec le bon seuil.
        verify(subscriptionRepository, times(1))
                .findInactiveBefore(expectedDeletionThreshold);

        // Vérifie que l'abonnement a été supprimé.
        verify(subscriptionRepository, times(1)).delete(subToDelete);

        // Vérifie qu'aucune sauvegarde n'a eu lieu (pour l'archivage) dans ce scénario
        // de suppression.
        verify(subscriptionRepository, never()).save(any(Subscription.class));
    }

    @Test
    @DisplayName("Ne devrait PAS supprimer les abonnements inactifs qui sont dans la période de suppression")
    void cleanupInactiveSubscriptions_shouldNotDeleteRecentInactiveSubscriptions() {
        // GIVEN: Un abonnement inactif datant de moins de 365 jours.
        User user = new User();
        user.setId(4L);
        Subscription subToKeep = new Subscription();
        subToKeep.setId(4L);
        subToKeep.setEndDate(fixedNow.minusDays(300)); // Fin de l'abonnement il y a 300 jours
        subToKeep.setStatus(Subscription.SubscriptionStatus.EXPIRED);
        subToKeep.setUser(user);

        // Configure le repository pour ne retourner aucun abonnement répondant au seuil
        // de suppression.
        when(subscriptionRepository.findInactiveBefore(any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());

        // Assurez-vous que l'appel d'archivage ne trouve rien pour ce test.
        when(subscriptionRepository.findByStatusAndCancellationDateBefore(any(), any()))
                .thenReturn(Collections.emptyList());

        // WHEN: La tâche de nettoyage est exécutée.
        cleanupTask.cleanupInactiveSubscriptions();

        // THEN:
        // Vérifie que `findInactiveBefore` a été appelé.
        verify(subscriptionRepository, times(1))
                .findInactiveBefore(any(LocalDateTime.class)); // Avec le seuil calculé, pas le souscription

        // Vérifie qu'aucun abonnement n'a été supprimé.
        verify(subscriptionRepository, never()).delete(any(Subscription.class));
        // Vérifie qu'aucune sauvegarde n'a eu lieu.
        verify(subscriptionRepository, never()).save(any(Subscription.class));
    }

    @Test
    @DisplayName("Ne devrait rien faire si aucun abonnement ne correspond aux critères d'archivage ou de suppression")
    void cleanupInactiveSubscriptions_shouldDoNothingIfNoMatchingSubscriptions() {
        // GIVEN: Le repository est configuré pour ne retourner aucun abonnement pour
        // les deux requêtes.
        when(subscriptionRepository.findByStatusAndCancellationDateBefore(any(), any()))
                .thenReturn(Collections.emptyList());
        when(subscriptionRepository.findInactiveBefore(any()))
                .thenReturn(Collections.emptyList());

        // WHEN: La tâche de nettoyage est exécutée.
        cleanupTask.cleanupInactiveSubscriptions();

        // THEN:
        // Vérifie que les méthodes de recherche ont été appelées.
        verify(subscriptionRepository, times(1))
                .findByStatusAndCancellationDateBefore(any(), any());
        verify(subscriptionRepository, times(1))
                .findInactiveBefore(any());

        // Vérifie qu'aucune opération de sauvegarde ou de suppression n'a eu lieu.
        verify(subscriptionRepository, never()).save(any(Subscription.class));
        verify(subscriptionRepository, never()).delete(any(Subscription.class));
    }

    @Test
    @DisplayName("Devrait gérer la combinaison d'abonnements à archiver et à supprimer")
    void cleanupInactiveSubscriptions_shouldHandleMixedScenarios() {
        // GIVEN: Un abonnement à archiver et un autre à supprimer.
        User user1 = new User();
        user1.setId(5L);
        User user2 = new User();
        user2.setId(6L);

        // Abonnement à archiver (annulé il y a 100 jours)
        Subscription subToArchive = new Subscription();
        subToArchive.setId(5L);
        subToArchive.setStatus(Subscription.SubscriptionStatus.CANCELLED);
        subToArchive.setCancelledAt(fixedNow.minusDays(100));
        subToArchive.setUser(user1);

        // Abonnement à supprimer (inactif depuis 400 jours)
        Subscription subToDelete = new Subscription();
        subToDelete.setId(6L);
        subToDelete.setEndDate(fixedNow.minusDays(400));
        subToDelete.setStatus(Subscription.SubscriptionStatus.EXPIRED);
        subToDelete.setUser(user2);

        // Calcule les seuils attendus.
        LocalDateTime expectedArchiveThreshold = fixedNow.minus(Period.ofDays(90));
        LocalDateTime expectedDeletionThreshold = fixedNow.minus(Period.ofDays(365));

        // Configure le repository pour retourner les listes appropriées.
        when(subscriptionRepository.findByStatusAndCancellationDateBefore(
                Subscription.SubscriptionStatus.CANCELLED, expectedArchiveThreshold))
                .thenReturn(Collections.singletonList(subToArchive));
        when(subscriptionRepository.findInactiveBefore(expectedDeletionThreshold))
                .thenReturn(Collections.singletonList(subToDelete));

        // Configure la sauvegarde pour l'abonnement archivé.
        when(subscriptionRepository.save(any(Subscription.class))).thenReturn(subToArchive);

        // WHEN: La tâche de nettoyage est exécutée.
        cleanupTask.cleanupInactiveSubscriptions();

        // THEN:
        // Vérifie les appels aux méthodes de recherche du repository.
        verify(subscriptionRepository, times(1))
                .findByStatusAndCancellationDateBefore(Subscription.SubscriptionStatus.CANCELLED,
                        expectedArchiveThreshold);
        verify(subscriptionRepository, times(1))
                .findInactiveBefore(expectedDeletionThreshold);

        // Vérifie l'archivage de l'abonnement : statut mis à jour et sauvegarde.
        assertEquals(Subscription.SubscriptionStatus.ARCHIVED, subToArchive.getStatus(),
                "Le statut de l'abonnement à archiver devrait être mis à jour.");
        verify(subscriptionRepository, times(1)).save(subToArchive);

        // Vérifie la suppression de l'abonnement.
        verify(subscriptionRepository, times(1)).delete(subToDelete);
    }
}