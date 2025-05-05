package com.videoflix.subscriptions_microservice.controllers;

import com.stripe.exception.StripeException;
import com.videoflix.subscriptions_microservice.entities.Promotion;
import com.videoflix.subscriptions_microservice.entities.Subscription;
import com.videoflix.subscriptions_microservice.entities.SubscriptionLevel;
import com.videoflix.subscriptions_microservice.services.StripePaymentService;
import com.videoflix.subscriptions_microservice.services.SubscriptionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import java.util.Arrays;
import java.util.List;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class SubscriptionControllerTest {

    @InjectMocks
    private SubscriptionController subscriptionController; // Instance du contrôleur à tester

    @Mock
    private SubscriptionService subscriptionService; // Mock du service d'abonnements

    @Mock
    private RestTemplate restTemplate; // Mock du RestTemplate pour les appels à d'autres microservices

    @Mock
    private StripePaymentService stripePaymentService; // Mock du service de paiement Stripe

    // Test pour la récupération de tous les abonnements
    @Test
    void getAllSubscriptions_shouldReturnOkAndListOfSubscriptions() {
        // GIVEN : Création d'une liste d'abonnements mockée
        List<Subscription> mockSubscriptions = Arrays.asList(new Subscription(), new Subscription());
        // WHEN : Configuration du comportement du service pour retourner la liste
        // mockée
        when(subscriptionService.getAllSubscriptions(0, 0)).thenReturn(mockSubscriptions);
        // WHEN : Appel de la méthode du contrôleur
        ResponseEntity<List<Subscription>> response = subscriptionController.getAllSubscriptions();
        // THEN : Vérification du statut de la réponse et du corps
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockSubscriptions, response.getBody());
        // THEN : Vérification que la méthode du service a été appelée
        verify(subscriptionService, times(1)).getAllSubscriptions(0, 0);
    }

    // Test pour la récupération d'un abonnement par ID (abonnement trouvé)
    @Test
    void getSubscriptionById_shouldReturnOkAndSubscription_whenIdExists() {
        // GIVEN : Création d'un abonnement mocké et d'un ID existant
        Long subscriptionId = 1L;
        Subscription mockSubscription = new Subscription();
        mockSubscription.setId(subscriptionId);
        // WHEN : Configuration du comportement du service pour retourner l'abonnement
        // mocké pour l'ID donné
        when(subscriptionService.getSubscriptionById(subscriptionId)).thenReturn(mockSubscription);
        // WHEN : Appel de la méthode du contrôleur
        ResponseEntity<Subscription> response = subscriptionController.getSubscriptionById(subscriptionId);
        // THEN : Vérification du statut de la réponse et du corps
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockSubscription, response.getBody());
        // THEN : Vérification que la méthode du service a été appelée avec l'ID correct
        verify(subscriptionService, times(1)).getSubscriptionById(subscriptionId);
    }

    // Test pour la récupération d'un abonnement par ID (abonnement non trouvé)
    @Test
    void getSubscriptionById_shouldReturnNotFound_whenIdDoesNotExist() {
        // GIVEN : Un ID inexistant
        Long subscriptionId = 1L;
        // WHEN : Configuration du comportement du service pour retourner null pour l'ID
        // donné
        when(subscriptionService.getSubscriptionById(subscriptionId)).thenReturn(null);
        // WHEN : Appel de la méthode du contrôleur
        ResponseEntity<Subscription> response = subscriptionController.getSubscriptionById(subscriptionId);
        // THEN : Vérification du statut de la réponse
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        // THEN : Vérification que la méthode du service a été appelée avec l'ID correct
        verify(subscriptionService, times(1)).getSubscriptionById(subscriptionId);
    }

    // Test pour la création d'un abonnement
    @Test
    void createSubscription_shouldReturnCreatedAndSubscription_andCallUserService() {
        // GIVEN : Création d'un abonnement à créer et d'un ID utilisateur
        Subscription subscriptionToCreate = new Subscription();
        Long userId = 10L;
        Subscription createdSubscription = new Subscription();
        createdSubscription.setId(1L); // Simuler un ID attribué lors de la création
        // WHEN : Configuration du comportement du service pour retourner l'abonnement
        // créé
        when(subscriptionService.createSubscription(subscriptionToCreate)).thenReturn(createdSubscription);
        // WHEN : Appel de la méthode du contrôleur
        ResponseEntity<Subscription> response = subscriptionController.createSubscription(subscriptionToCreate, userId);
        // THEN : Vérification du statut de la réponse et du corps
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(createdSubscription, response.getBody());
        // THEN : Vérification que la méthode de création du service a été appelée avec
        // l'abonnement correct
        verify(subscriptionService, times(1)).createSubscription(subscriptionToCreate);
        // THEN : Vérification que l'appel au microservice users a été effectué avec
        // l'ID utilisateur et l'ID d'abonnement corrects
        verify(restTemplate, times(1)).put("http://users-microservice/users/" + userId + "/subscription",
                createdSubscription.getId());
    }

    // Test pour la mise à jour d'un abonnement (abonnement trouvé)
    @Test
    void updateSubscription_shouldReturnOkAndUpdatedSubscription_whenIdExists() {
        // GIVEN : Création d'un ID existant et d'un abonnement mis à jour
        Long subscriptionId = 1L;
        Subscription subscriptionToUpdate = new Subscription();
        Subscription updatedSubscription = new Subscription();
        updatedSubscription.setId(subscriptionId);
        // WHEN : Configuration du comportement du service pour retourner l'abonnement
        // mis à jour pour l'ID donné
        when(subscriptionService.updateSubscription(subscriptionId, subscriptionToUpdate))
                .thenReturn(updatedSubscription);
        // WHEN : Appel de la méthode du contrôleur
        ResponseEntity<Subscription> response = subscriptionController.updateSubscription(subscriptionId,
                subscriptionToUpdate);
        // THEN : Vérification du statut de la réponse et du corps
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(updatedSubscription, response.getBody());
        // THEN : Vérification que la méthode de mise à jour du service a été appelée
        // avec l'ID et l'abonnement corrects
        verify(subscriptionService, times(1)).updateSubscription(subscriptionId, subscriptionToUpdate);
    }

    // Test pour la mise à jour d'un abonnement (abonnement non trouvé)
    @Test
    void updateSubscription_shouldReturnNotFound_whenIdDoesNotExist() {
        // GIVEN : Un ID inexistant et un abonnement à mettre à jour
        Long subscriptionId = 1L;
        Subscription subscriptionToUpdate = new Subscription();
        // WHEN : Configuration du comportement du service pour retourner null pour l'ID
        // donné
        when(subscriptionService.updateSubscription(subscriptionId, subscriptionToUpdate)).thenReturn(null);
        // WHEN : Appel de la méthode du contrôleur
        ResponseEntity<Subscription> response = subscriptionController.updateSubscription(subscriptionId,
                subscriptionToUpdate);
        // THEN : Vérification du statut de la réponse
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        // THEN : Vérification que la méthode de mise à jour du service a été appelée
        // avec l'ID et l'abonnement corrects
        verify(subscriptionService, times(1)).updateSubscription(subscriptionId, subscriptionToUpdate);
    }

    // Test pour le changement de niveau d'abonnement (abonnement trouvé)
    @Test
    void changeSubscriptionLevel_shouldReturnOkAndUpdatedSubscription_whenIdExists() {
        // GIVEN : Création d'un ID existant et d'un nouvel ID de niveau d'abonnement
        Long subscriptionId = 1L;
        Long newSubscriptionLevelId = 2L;
        Subscription updatedSubscription = new Subscription();
        when(subscriptionService.changeSubscriptionLevel(subscriptionId, newSubscriptionLevelId))
                .thenReturn(updatedSubscription);
        // WHEN : Appel de la méthode du contrôleur
        ResponseEntity<Subscription> response = subscriptionController.changeSubscriptionLevel(subscriptionId,
                newSubscriptionLevelId);
        // THEN : Vérification du statut de la réponse et du corps
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(updatedSubscription, response.getBody());
        // THEN : Vérification que la méthode de changement de niveau du service a été
        // appelée avec les IDs corrects
        verify(subscriptionService, times(1)).changeSubscriptionLevel(subscriptionId, newSubscriptionLevelId);
    }

    // Test pour le changement de niveau d'abonnement (abonnement non trouvé)
    @Test
    void changeSubscriptionLevel_shouldReturnNotFound_whenIdDoesNotExist() {
        // GIVEN : Un ID inexistant et un nouvel ID de niveau d'abonnement
        Long subscriptionId = 1L;
        Long newSubscriptionLevelId = 2L;
        when(subscriptionService.changeSubscriptionLevel(subscriptionId, newSubscriptionLevelId)).thenReturn(null);
        // WHEN : Appel de la méthode du contrôleur
        ResponseEntity<Subscription> response = subscriptionController.changeSubscriptionLevel(subscriptionId,
                newSubscriptionLevelId);
        // THEN : Vérification du statut de la réponse
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        // THEN : Vérification que la méthode de changement de niveau du service a été
        // appelée avec les IDs corrects
        verify(subscriptionService, times(1)).changeSubscriptionLevel(subscriptionId, newSubscriptionLevelId);
    }

    // Test pour la suppression d'un abonnement
    @Test
    void deleteSubscription_shouldReturnNoContent_whenIdExists() {
        // GIVEN : Un ID existant
        Long subscriptionId = 1L;
        // WHEN : Appel de la méthode du contrôleur
        ResponseEntity<Void> response = subscriptionController.deleteSubscription(subscriptionId);
        // THEN : Vérification du statut de la réponse
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        // THEN : Vérification que la méthode de suppression du service a été appelée
        // avec l'ID correct
        verify(subscriptionService, times(1)).deleteSubscription(subscriptionId);
    }

    // Tests pour les niveaux d'abonnement (similaires aux tests d'abonnements)
    @Test
    void createSubscriptionLevel_shouldReturnCreatedAndSubscriptionLevel() {
        SubscriptionLevel levelToCreate = new SubscriptionLevel();
        SubscriptionLevel createdLevel = new SubscriptionLevel();
        when(subscriptionService.createSubscriptionLevel(levelToCreate)).thenReturn(createdLevel);
        ResponseEntity<SubscriptionLevel> response = subscriptionController.createSubscriptionLevel(levelToCreate);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(createdLevel, response.getBody());
        verify(subscriptionService, times(1)).createSubscriptionLevel(levelToCreate);
    }

    @Test
    void getSubscriptionLevelById_shouldReturnOkAndLevel_whenIdExists() {
        Long levelId = 1L;
        SubscriptionLevel mockLevel = new SubscriptionLevel();
        when(subscriptionService.getSubscriptionLevelById(levelId)).thenReturn(mockLevel);
        ResponseEntity<SubscriptionLevel> response = subscriptionController.getSubscriptionLevelById(levelId);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockLevel, response.getBody());
        verify(subscriptionService, times(1)).getSubscriptionLevelById(levelId);
    }

    @Test
    void getSubscriptionLevelById_shouldReturnNotFound_whenIdDoesNotExist() {
        Long levelId = 1L;
        when(subscriptionService.getSubscriptionLevelById(levelId)).thenReturn(null);
        ResponseEntity<SubscriptionLevel> response = subscriptionController.getSubscriptionLevelById(levelId);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(subscriptionService, times(1)).getSubscriptionLevelById(levelId);
    }

    @Test
    void getAllSubscriptionLevels_shouldReturnOkAndListOfLevels() {
        List<SubscriptionLevel> mockLevels = Arrays.asList(new SubscriptionLevel(), new SubscriptionLevel());
        when(subscriptionService.getAllSubscriptionLevels()).thenReturn(mockLevels);
        ResponseEntity<List<SubscriptionLevel>> response = subscriptionController.getAllSubscriptionLevels();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockLevels, response.getBody());
        verify(subscriptionService, times(1)).getAllSubscriptionLevels();
    }

    @Test
    void updateSubscriptionLevel_shouldReturnOkAndUpdatedLevel_whenIdExists() {
        Long levelId = 1L;
        SubscriptionLevel levelToUpdate = new SubscriptionLevel();
        SubscriptionLevel updatedLevel = new SubscriptionLevel();
        when(subscriptionService.updateSubscriptionLevel(levelId, levelToUpdate)).thenReturn(updatedLevel);
        ResponseEntity<SubscriptionLevel> response = subscriptionController.updateSubscriptionLevel(levelId,
                levelToUpdate);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(updatedLevel, response.getBody());
        verify(subscriptionService, times(1)).updateSubscriptionLevel(levelId, levelToUpdate);
    }

    @Test
    void updateSubscriptionLevel_shouldReturnNotFound_whenIdDoesNotExist() {
        Long levelId = 1L;
        SubscriptionLevel levelToUpdate = new SubscriptionLevel();
        when(subscriptionService.updateSubscriptionLevel(levelId, levelToUpdate)).thenReturn(null);
        ResponseEntity<SubscriptionLevel> response = subscriptionController.updateSubscriptionLevel(levelId,
                levelToUpdate);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(subscriptionService, times(1)).updateSubscriptionLevel(levelId, levelToUpdate);
    }

    @Test
    void deleteSubscriptionLevel_shouldReturnNoContent_whenIdExists() {
        Long levelId = 1L;
        ResponseEntity<Void> response = subscriptionController.deleteSubscriptionLevel(levelId);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(subscriptionService, times(1)).deleteSubscriptionLevel(levelId);
    }

    // Tests pour les promotions (similaires aux tests d'abonnements)
    @Test
    void createPromotion_shouldReturnOkAndPromotion() {
        Promotion promotionToCreate = new Promotion();
        Promotion createdPromotion = new Promotion();
        when(subscriptionService.createPromotion(promotionToCreate)).thenReturn(createdPromotion);
        ResponseEntity<Promotion> response = subscriptionController.createPromotion(promotionToCreate);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(createdPromotion, response.getBody());
        verify(subscriptionService, times(1)).createPromotion(promotionToCreate);
    }

    @Test
    void getPromotionByCode_shouldReturnOkAndPromotion_whenCodeExists() {
        String promotionCode = "SUMMER20";
        Promotion mockPromotion = new Promotion();
        when(subscriptionService.getPromotionByCode(promotionCode)).thenReturn(mockPromotion);
        ResponseEntity<Promotion> response = subscriptionController.getPromotionByCode(promotionCode);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockPromotion, response.getBody());
        verify(subscriptionService, times(1)).getPromotionByCode(promotionCode);
    }

    @Test
    void getPromotionByCode_shouldReturnNotFound_whenCodeDoesNotExist() {
        String promotionCode = "INVALID";
        when(subscriptionService.getPromotionByCode(promotionCode)).thenReturn(null);
        ResponseEntity<Promotion> response = subscriptionController.getPromotionByCode(promotionCode);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(subscriptionService, times(1)).getPromotionByCode(promotionCode);
    }

    @Test
    void getAllPromotions_shouldReturnOkAndListOfPromotions() {
        List<Promotion> mockPromotions = Arrays.asList(new Promotion(), new Promotion());
        when(subscriptionService.getAllPromotions()).thenReturn(mockPromotions);
        ResponseEntity<List<Promotion>> response = subscriptionController.getAllPromotions();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockPromotions, response.getBody());
        verify(subscriptionService, times(1)).getAllPromotions();
    }

    @Test
    void applyPromotion_shouldReturnOkAndUpdatedSubscription_whenIdAndCodeExist() {
        Long subscriptionId = 1L;
        String promotionCode = "SUMMER20";
        Subscription updatedSubscription = new Subscription();
        when(subscriptionService.applyPromotion(subscriptionId, promotionCode)).thenReturn(updatedSubscription);
        ResponseEntity<Subscription> response = subscriptionController.applyPromotion(subscriptionId, promotionCode);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(updatedSubscription, response.getBody());
        verify(subscriptionService, times(1)).applyPromotion(subscriptionId, promotionCode);
    }

    @Test
    void applyPromotion_shouldReturnNotFound_whenIdOrCodeDoesNotExist() {
        Long subscriptionId = 1L;
        String promotionCode = "INVALID";
        when(subscriptionService.applyPromotion(subscriptionId, promotionCode)).thenReturn(null);
        ResponseEntity<Subscription> response = subscriptionController.applyPromotion(subscriptionId, promotionCode);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(subscriptionService, times(1)).applyPromotion(subscriptionId, promotionCode);
    }

    // Tests pour les opérations Stripe
    @Test
    void createStripeCustomer_shouldReturnOkAndCustomerId_whenSuccessful() throws StripeException {
        // GIVEN : Un ID d'abonnement, un email et un nom
        Long subscriptionId = 1L;
        String email = "test@example.com";
        String name = "Test User";
        String mockCustomerId = "cus_TESTID";
        // WHEN : Configuration du comportement du service Stripe pour retourner un ID
        // client mocké
        when(stripePaymentService.createStripeCustomer(email, name)).thenReturn(mockCustomerId);
        // WHEN : Appel de la méthode du contrôleur
        ResponseEntity<String> response = subscriptionController.createStripeCustomer(subscriptionId, email, name);
        // THEN : Vérification du statut de la réponse et du corps
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockCustomerId, response.getBody());
        // THEN : Vérification que la méthode du service Stripe a été appelée avec les
        // bons arguments
        verify(stripePaymentService, times(1)).createStripeCustomer(email, name);
    }

    @Test
    void createStripeSubscription_shouldReturnOkAndStripeSubscriptionId_whenSuccessful() throws StripeException {
        // GIVEN : Un ID d'abonnement, un ID client Stripe et un ID de prix Stripe
        Long subscriptionId = 1L;
        String customerId = "cus_TESTID";
        String priceId = "price_TEST";
        Subscription mockCreatedSubscription = new Subscription();
        mockCreatedSubscription.setStripeSubscriptionId("sub_TESTSUBID");
        // WHEN : Configuration du comportement du service Stripe pour retourner un
        // abonnement mocké avec un ID Stripe
        when(stripePaymentService.createStripeSubscription(any(Subscription.class)))
                .thenReturn(mockCreatedSubscription);
        // WHEN : Appel de la méthode du contrôleur
        ResponseEntity<String> response = subscriptionController.createStripeSubscription(subscriptionId, customerId,
                priceId);
        // THEN : Vérification du statut de la réponse et du corps
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("sub_TESTSUBID", response.getBody());
        // THEN : Vérification que la méthode du service Stripe a été appelée avec un
        // abonnement contenant les bons IDs
        verify(stripePaymentService, times(1)).createStripeSubscription(
                argThat(sub -> sub.getCustomerId().equals(customerId) && sub.getPriceId().equals(priceId)));
    }

    @Test
    void createStripeSubscriptionWithTrial_shouldReturnOkAndSubscription_whenSuccessful() {
        // GIVEN : Un ID d'abonnement, un objet Subscription et une durée d'essai
        Long subscriptionId = 1L;
        Subscription subscriptionWithTrial = new Subscription();
        long trialPeriodDays = 7L;
        Subscription mockCreatedSubscription = new Subscription();
        // WHEN : Configuration du comportement du service Stripe pour retourner un
        // abonnement mocké
        when(stripePaymentService.createStripeSubscriptionWithTrial(subscriptionWithTrial, trialPeriodDays))
                .thenReturn(mockCreatedSubscription);
        // WHEN : Appel de la méthode du contrôleur
        ResponseEntity<Subscription> response = subscriptionController.createStripeSubscriptionWithTrial(subscriptionId,
                subscriptionWithTrial, trialPeriodDays);
        // THEN : Vérification du statut de la réponse et du corps
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockCreatedSubscription, response.getBody());
        // THEN : Vérification que la méthode du service Stripe a été appelée avec les
        // bons arguments
        verify(stripePaymentService, times(1)).createStripeSubscriptionWithTrial(subscriptionWithTrial,
                trialPeriodDays);
    }

    @Test
    void refundSubscription_shouldReturnOkAndRefundedSubscription_whenSuccessful() throws StripeException {
        // GIVEN : Un ID d'abonnement, un montant et une raison de remboursement
        Long subscriptionId = 1L;
        double amount = 10.0;
        String reason = "Requested by customer";
        Subscription mockSubscription = new Subscription();
        Subscription mockRefundedSubscription = new Subscription();
        // WHEN : Configuration du comportement du service d'abonnements pour retourner
        // un abonnement par ID
        when(subscriptionService.getSubscriptionById(subscriptionId)).thenReturn(mockSubscription);
        // WHEN : Configuration du comportement du service Stripe pour retourner un
        // abonnement remboursé mocké
        when(stripePaymentService.refundSubscription(mockSubscription, amount, reason))
                .thenReturn(mockRefundedSubscription);
        // WHEN : Appel de la méthode du contrôleur
        ResponseEntity<Subscription> response = subscriptionController.refundSubscription(subscriptionId, amount,
                reason);
        // THEN : Vérification du statut de la réponse et du corps
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockRefundedSubscription, response.getBody());
        // THEN : Vérification que les méthodes des services ont été appelées avec les
        // bons arguments
        verify(subscriptionService, times(1)).getSubscriptionById(subscriptionId);
        verify(stripePaymentService, times(1)).refundSubscription(mockSubscription, amount, reason);
    }

    @Test
    void refundSubscription_shouldReturnNotFound_whenSubscriptionIdDoesNotExist() throws StripeException {
        // GIVEN : Un ID d'abonnement inexistant
        Long subscriptionId = 1L;
        double amount = 10.0;
        String reason = "Requested by customer";
        // WHEN : Configuration du comportement du service d'abonnements pour retourner
        // null
        when(subscriptionService.getSubscriptionById(subscriptionId)).thenReturn(null);
        // WHEN : Appel de la méthode du contrôleur
        ResponseEntity<Subscription> response = subscriptionController.refundSubscription(subscriptionId, amount,
                reason);
        // THEN : Vérification du statut de la réponse
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        // THEN : Vérification que la méthode de récupération de l'abonnement a été
        // appelée
        verify(subscriptionService, times(1)).getSubscriptionById(subscriptionId);
        // THEN : Vérification que la méthode de remboursement Stripe n'a pas été
        // appelée (car l'abonnement n'existe pas)
        verify(stripePaymentService, never()).refundSubscription(any(), anyDouble(), anyString());
    }
}