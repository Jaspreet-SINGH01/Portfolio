package com.videoflix.subscriptions_microservice.dtos;

import com.videoflix.subscriptions_microservice.entities.Subscription.SubscriptionStatus;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class AdminUpdateSubscriptionRequestTest {

    // Test pour vérifier la création et la récupération des attributs de l'objet
    @Test
    void adminUpdateSubscriptionRequest_shouldSetAndGetValues() {
        // GIVEN : Création d'un objet AdminUpdateSubscriptionRequest et définition de
        // ses attributs
        AdminUpdateSubscriptionRequest request = new AdminUpdateSubscriptionRequest();
        SubscriptionStatus status = SubscriptionStatus.ACTIVE;
        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime endDate = LocalDateTime.now().plusMonths(1);
        Long subscriptionLevelId = 2L;

        // WHEN : Définition des valeurs des attributs de l'objet
        request.setStatus(status);
        request.setStartDate(startDate);
        request.setEndDate(endDate);
        request.setSubscriptionLevelId(subscriptionLevelId);

        // THEN : Vérification que les valeurs définies sont correctement récupérées
        assertEquals(status, request.getStatus(), "Le statut doit correspondre à la valeur définie.");
        assertEquals(startDate, request.getStartDate(), "La date de début doit correspondre à la valeur définie.");
        assertEquals(endDate, request.getEndDate(), "La date de fin doit correspondre à la valeur définie.");
        assertEquals(subscriptionLevelId, request.getSubscriptionLevelId(),
                "L'ID du niveau d'abonnement doit correspondre à la valeur définie.");
    }

    // Test pour vérifier que les attributs sont initialisés à null par défaut
    @Test
    void adminUpdateSubscriptionRequest_defaultValuesShouldBeNull() {
        // GIVEN : Création d'un objet AdminUpdateSubscriptionRequest sans définir de
        // valeurs
        AdminUpdateSubscriptionRequest request = new AdminUpdateSubscriptionRequest();

        // THEN : Vérification que tous les attributs sont null par défaut
        assertNull(request.getStatus(), "Le statut par défaut devrait être null.");
        assertNull(request.getStartDate(), "La date de début par défaut devrait être null.");
        assertNull(request.getEndDate(), "La date de fin par défaut devrait être null.");
        assertNull(request.getSubscriptionLevelId(), "L'ID du niveau d'abonnement par défaut devrait être null.");
    }

    // Test pour vérifier que les valeurs peuvent être modifiées après la création
    // de l'objet
    @Test
    void adminUpdateSubscriptionRequest_shouldAllowValueModification() {
        // GIVEN : Création d'un objet AdminUpdateSubscriptionRequest et définition
        // d'une valeur initiale
        AdminUpdateSubscriptionRequest request = new AdminUpdateSubscriptionRequest();
        SubscriptionStatus initialStatus = SubscriptionStatus.PENDING;
        request.setStatus(initialStatus);

        // WHEN : Modification de la valeur de l'attribut status
        SubscriptionStatus newStatus = SubscriptionStatus.EXPIRED;
        request.setStatus(newStatus);

        // THEN : Vérification que la valeur a été correctement modifiée
        assertEquals(newStatus, request.getStatus(), "Le statut devrait avoir été modifié.");
    }
}