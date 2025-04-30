package com.videoflix.subscriptions_microservice.batch.writer;

import com.videoflix.subscriptions_microservice.entities.Subscription;
import com.videoflix.subscriptions_microservice.repositories.SubscriptionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.item.Chunk;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class InactiveSubscriptionWriterTest {

    @InjectMocks
    private InactiveSubscriptionWriter writer; // L'instance du writer à tester

    @Mock
    private SubscriptionRepository subscriptionRepository; // Mock du repository

    @SuppressWarnings("unchecked")
    @Test
    void write_shouldDeleteAllSubscriptionsInChunk() throws Exception {
        // GIVEN : Une liste d'abonnements à supprimer dans un Chunk
        Subscription sub1 = new Subscription();
        sub1.setId(1L);
        Subscription sub2 = new Subscription();
        sub2.setId(2L);
        List<Subscription> subscriptions = Arrays.asList(sub1, sub2);
        Chunk<Subscription> chunk = new Chunk<>(subscriptions);

        // WHEN : La méthode write est appelée avec le chunk
        writer.write(chunk);

        // THEN : Vérification que la méthode deleteAll du repository a été appelée avec
        // la liste correcte
        ArgumentCaptor<List<Subscription>> subscriptionsToDeleteCaptor = ArgumentCaptor.forClass(List.class);
        verify(subscriptionRepository, times(1)).deleteAll(subscriptionsToDeleteCaptor.capture());

        List<Subscription> capturedSubscriptionsToDelete = subscriptionsToDeleteCaptor.getValue();
        assertEquals(2, capturedSubscriptionsToDelete.size());
        assertTrue(capturedSubscriptionsToDelete.contains(sub1));
        assertTrue(capturedSubscriptionsToDelete.contains(sub2));
    }

    @Test
    void write_shouldNotCallDeleteAll_whenChunkIsEmpty() throws Exception {
        // GIVEN : Un Chunk vide
        Chunk<Subscription> emptyChunk = new Chunk<>(List.of());

        // WHEN : La méthode write est appelée avec le chunk vide
        writer.write(emptyChunk);

        // THEN : Vérification que la méthode deleteAll du repository n'a pas été
        // appelée
        verify(subscriptionRepository, never()).deleteAll(any());
    }

    @Test
    void write_shouldHandleExceptionFromRepository() throws Exception {
        // GIVEN : Un Chunk d'abonnements et un repository qui lève une exception lors
        // de la suppression
        Subscription sub = new Subscription();
        Chunk<Subscription> chunk = new Chunk<>(List.of(sub));
        RuntimeException repositoryException = new RuntimeException("Erreur de suppression");
        doThrow(repositoryException).when(subscriptionRepository).deleteAll(any());

        // WHEN : Appel de la méthode write qui devrait propager l'exception
        try {
            writer.write(chunk);
            fail("Une exception aurait dû être levée par le writer.");
        } catch (RuntimeException e) {
            // THEN : Vérification que l'exception levée est celle du repository
            assertEquals("Erreur de suppression", e.getMessage());
            verify(subscriptionRepository, times(1)).deleteAll(any());
        }
    }
}