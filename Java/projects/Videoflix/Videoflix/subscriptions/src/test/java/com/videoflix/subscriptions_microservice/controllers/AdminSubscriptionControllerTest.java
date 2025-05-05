package com.videoflix.subscriptions_microservice.controllers;

import com.videoflix.subscriptions_microservice.dtos.AdminUpdateSubscriptionRequest;
import com.videoflix.subscriptions_microservice.entities.Subscription;
import com.videoflix.subscriptions_microservice.services.SubscriptionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class AdminSubscriptionControllerTest {

    @InjectMocks
    private AdminSubscriptionController adminSubscriptionController; // Instance du contrôleur à tester

    @Mock
    private SubscriptionService subscriptionService; // Mock du service des abonnements

    @Test
    void listAllSubscriptions_shouldReturnOkWithListOfSubscriptions() {
        // GIVEN : Une liste d'abonnements simulée retournée par le service
        int page = 0;
        int size = 10;
        List<Subscription> subscriptions = Arrays.asList(new Subscription(), new Subscription());
        when(subscriptionService.getAllSubscriptions(page, size)).thenReturn(subscriptions);

        // WHEN : Appel de la méthode listAllSubscriptions du contrôleur
        ResponseEntity<List<Subscription>> response = adminSubscriptionController.listAllSubscriptions(page, size);

        // THEN : Vérification que la réponse a le statut OK et contient la liste des
        // abonnements
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(subscriptions, response.getBody());
        verify(subscriptionService, times(1)).getAllSubscriptions(page, size); // Vérifie que la méthode du service a
                                                                               // été appelée une fois avec les bons
                                                                               // paramètres
    }

    @Test
    void searchSubscriptions_shouldReturnOkWithFilteredSubscriptions() {
        // GIVEN : Des filtres de recherche et une liste d'abonnements correspondante
        // simulée par le service
        Long userId = 1L;
        String status = "ACTIVE";
        String subscriptionLevel = "PREMIUM";
        int page = 0;
        int size = 10;
        List<Subscription> filteredSubscriptions = Arrays.asList(new Subscription(), new Subscription());
        when(subscriptionService.getAllSubscriptionsByFilters(userId, status, subscriptionLevel, page, size))
                .thenReturn(filteredSubscriptions);

        // WHEN : Appel de la méthode searchSubscriptions du contrôleur avec les filtres
        ResponseEntity<List<Subscription>> response = adminSubscriptionController.searchSubscriptions(userId, status,
                subscriptionLevel, page, size);

        // THEN : Vérification que la réponse a le statut OK et contient la liste
        // filtrée des abonnements
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(filteredSubscriptions, response.getBody());
        verify(subscriptionService, times(1)).getAllSubscriptionsByFilters(userId, status, subscriptionLevel, page,
                size); // Vérifie l'appel au service avec les bons filtres
    }

    @Test
    void updateSubscription_shouldReturnOkWithUpdatedSubscription_whenUpdateSuccessful() {
        // GIVEN : Un ID d'abonnement et une requête de mise à jour valides, avec
        // l'abonnement mis à jour simulé par le service
        Long subscriptionId = 1L;
        AdminUpdateSubscriptionRequest updateRequest = new AdminUpdateSubscriptionRequest(); // Simuler une requête
                                                                                             // valide
        Subscription updatedSubscription = new Subscription();
        updatedSubscription.setId(subscriptionId);
        when(subscriptionService.updateSubscriptionByAdmin(subscriptionId, updateRequest))
                .thenReturn(updatedSubscription);

        // WHEN : Appel de la méthode updateSubscription du contrôleur
        ResponseEntity<Subscription> response = adminSubscriptionController.updateSubscription(subscriptionId,
                updateRequest);

        // THEN : Vérification que la réponse a le statut OK et contient l'abonnement
        // mis à jour
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(updatedSubscription, response.getBody());
        verify(subscriptionService, times(1)).updateSubscriptionByAdmin(subscriptionId, updateRequest); // Vérifie
                                                                                                        // l'appel au
                                                                                                        // service avec
                                                                                                        // l'ID et la
                                                                                                        // requête
    }

    // Vous pourriez ajouter d'autres tests pour les cas d'erreur potentiels,
    // par exemple, si le service lance une exception ou retourne null.
}