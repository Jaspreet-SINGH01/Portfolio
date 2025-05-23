package com.videoflix.subscriptions_microservice.services;

import com.stripe.exception.ApiException;
import com.stripe.exception.StripeException;
import com.stripe.model.Refund;
import com.videoflix.subscriptions_microservice.dtos.AdminUpdateSubscriptionRequest;
import com.videoflix.subscriptions_microservice.entities.Promotion;
import com.videoflix.subscriptions_microservice.entities.Subscription;
import com.videoflix.subscriptions_microservice.entities.SubscriptionLevel;
import com.videoflix.subscriptions_microservice.entities.User;
import com.videoflix.subscriptions_microservice.exceptions.StripeIntegrationException;
import com.videoflix.subscriptions_microservice.integration.*;
import com.videoflix.subscriptions_microservice.repositories.PromotionRepository;
import com.videoflix.subscriptions_microservice.repositories.SubscriptionLevelRepository;
import com.videoflix.subscriptions_microservice.repositories.SubscriptionRepository;
import com.videoflix.subscriptions_microservice.repositories.UserRepository;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Classe de test pour SubscriptionService
 * Tests unitaires couvrant toutes les fonctionnalités du service d'abonnement
 */
@ExtendWith(MockitoExtension.class)
class SubscriptionServiceTest {

    // Mocks des dépendances
    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private SubscriptionLevelRepository subscriptionLevelRepository;

    @Mock
    private PromotionRepository promotionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EntityManager entityManager;

    @Mock
    private SubscriptionLevelChangedEventPublisher levelChangedEventPublisher;

    @Mock
    private SubscriptionCancelledEventPublisher subscriptionCancelledEventPublisher;

    @Mock
    private BillingCalculationService billingCalculationService;

    @Mock
    private SubscriptionRenewedEventPublisher subscriptionRenewedEventPublisher;

    @Mock
    private SubscriptionReactivatedEventPublisher subscriptionReactivatedEventPublisher;

    @Mock
    private AccessControlEventPublisher accessControlEventPublisher;

    @Mock
    private StripeBillingService stripeBillingService;

    // Service à tester avec injection des mocks
    @InjectMocks
    private SubscriptionService subscriptionService;

    // Données de test
    private User testUser;
    private Subscription testSubscription;
    private SubscriptionLevel testSubscriptionLevel;
    private Promotion testPromotion;

    /**
     * Configuration initiale des données de test avant chaque test
     */
    @BeforeEach
    void setUp() {
        // Initialisation d'un utilisateur de test
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setStripeCustomerId("cus_test123");
        testUser.setPushToken("push_token_123");

        // Initialisation d'un niveau d'abonnement de test
        testSubscriptionLevel = new SubscriptionLevel();
        testSubscriptionLevel.setId(1L);
        testSubscriptionLevel.setLevel(SubscriptionLevel.Level.BASIC);
        testSubscriptionLevel.setPrice(9.99);
        testSubscriptionLevel.setStripePriceId("price_test123");

        // Initialisation d'un abonnement de test
        testSubscription = new Subscription();
        testSubscription.setId(1L);
        testSubscription.setUser(testUser);
        testSubscription.setSubscriptionLevel(testSubscriptionLevel);
        testSubscription.setStartDate(LocalDateTime.now());
        testSubscription.setEndDate(LocalDateTime.now().plusMonths(1));
        testSubscription.setStatus(Subscription.SubscriptionStatus.ACTIVE);
        testSubscription.setStripeSubscriptionId("sub_test123");
        testSubscription.setStripeChargeId("ch_test123");

        // Initialisation d'une promotion de test
        testPromotion = new Promotion();
        testPromotion.setId(1L);
        testPromotion.setPromotionCode("PROMO2024");
        testPromotion.setActive(true);
        testPromotion.setStartDate(LocalDateTime.now().minusDays(1));
        testPromotion.setEndDate(LocalDateTime.now().plusDays(30));
    }

    // ========== TESTS DE CRÉATION D'ABONNEMENT ==========

    @Test
    @DisplayName("Création d'abonnement - Cas de succès")
    void testCreateSubscription_Success() {
        // Given - Configuration des mocks
        when(userRepository.existsById(testUser.getId())).thenReturn(true);
        when(subscriptionRepository.save(any(Subscription.class))).thenReturn(testSubscription);

        // When - Exécution de la méthode à tester
        Subscription result = subscriptionService.createSubscription(testSubscription);

        // Then - Vérifications
        assertNotNull(result, "L'abonnement créé ne doit pas être null");
        assertEquals(testSubscription.getId(), result.getId(), "L'ID de l'abonnement doit correspondre");
        verify(subscriptionRepository).save(testSubscription);
    }

    @Test
    @DisplayName("Création d'abonnement - Échec avec utilisateur inexistant")
    void testCreateSubscription_UserNotFound() {
        // Given - Utilisateur inexistant
        when(userRepository.existsById(testUser.getId())).thenReturn(false);

        // When & Then - Vérification de l'exception
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> subscriptionService.createSubscription(testSubscription),
                "Une exception doit être levée pour un utilisateur inexistant");
        assertTrue(exception.getMessage().contains("n'existe pas"),
                "Le message d'erreur doit mentionner l'utilisateur inexistant");
        verify(subscriptionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Création d'abonnement - Échec avec date de début invalide")
    void testCreateSubscription_InvalidStartDate() {
        // Given - Date de début dans le passé
        testSubscription.setStartDate(LocalDateTime.now().minusDays(1));
        when(userRepository.existsById(testUser.getId())).thenReturn(true);

        // When & Then - Vérification de l'exception
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> subscriptionService.createSubscription(testSubscription),
                "Une exception doit être levée pour une date de début invalide");
        assertTrue(exception.getMessage().contains("date de début"),
                "Le message d'erreur doit mentionner la date de début");
    }

    // ========== TESTS DE RÉCUPÉRATION D'ABONNEMENT ==========

    @Test
    @DisplayName("Récupération d'abonnement par ID - Cas de succès")
    void testGetSubscriptionById_Success() {
        // Given
        when(subscriptionRepository.findById(1L)).thenReturn(Optional.of(testSubscription));

        // When
        Subscription result = subscriptionService.getSubscriptionById(1L);

        // Then
        assertNotNull(result, "L'abonnement récupéré ne doit pas être null");
        assertEquals(testSubscription.getId(), result.getId(), "L'ID doit correspondre");
        verify(subscriptionRepository).findById(1L);
    }

    @Test
    @DisplayName("Récupération d'abonnement par ID - Abonnement inexistant")
    void testGetSubscriptionById_NotFound() {
        // Given
        when(subscriptionRepository.findById(1L)).thenReturn(Optional.empty());

        // When
        Subscription result = subscriptionService.getSubscriptionById(1L);

        // Then
        assertNull(result, "Le résultat doit être null pour un abonnement inexistant");
        verify(subscriptionRepository).findById(1L);
    }

    @Test
    @DisplayName("Récupération de tous les abonnements avec pagination")
    void testGetAllSubscriptions_WithPagination() {
        // Given
        List<Subscription> subscriptions = Arrays.asList(testSubscription);
        Page<Subscription> subscriptionPage = new PageImpl<>(subscriptions);
        when(subscriptionRepository.findAll(any(Pageable.class))).thenReturn(subscriptionPage);

        // When
        List<Subscription> result = subscriptionService.getAllSubscriptions(0, 10);

        // Then
        assertNotNull(result, "La liste des abonnements ne doit pas être null");
        assertEquals(1, result.size(), "La liste doit contenir un abonnement");
        assertEquals(testSubscription.getId(), result.get(0).getId(), "L'ID doit correspondre");
        verify(subscriptionRepository).findAll(PageRequest.of(0, 10));
    }

    // ========== TESTS DE MISE À JOUR D'ABONNEMENT ==========

    @Test
    @DisplayName("Mise à jour d'abonnement - Cas de succès")
    void testUpdateSubscription_Success() {
        // Given
        when(subscriptionRepository.findById(1L)).thenReturn(Optional.of(testSubscription));
        when(subscriptionRepository.save(any(Subscription.class))).thenReturn(testSubscription);

        // When
        Subscription result = subscriptionService.updateSubscription(1L, testSubscription);

        // Then
        assertNotNull(result, "L'abonnement mis à jour ne doit pas être null");
        assertEquals(1L, result.getId(), "L'ID doit être correctement assigné");
        verify(subscriptionRepository).save(testSubscription);
    }

    @Test
    @DisplayName("Mise à jour d'abonnement - Abonnement inexistant")
    void testUpdateSubscription_NotFound() {
        // Given
        when(subscriptionRepository.findById(1L)).thenReturn(Optional.empty());

        // When
        Subscription result = subscriptionService.updateSubscription(1L, testSubscription);

        // Then
        assertNull(result, "Le résultat doit être null pour un abonnement inexistant");
        verify(subscriptionRepository, never()).save(any());
    }

    // ========== TESTS DE CHANGEMENT DE NIVEAU D'ABONNEMENT ==========

    @Test
    @DisplayName("Changement de niveau d'abonnement - Cas de succès")
    void testChangeSubscriptionLevel_Success() {
        // Given
        SubscriptionLevel newLevel = new SubscriptionLevel();
        newLevel.setId(2L);
        newLevel.setLevel(SubscriptionLevel.Level.PREMIUM);

        when(subscriptionRepository.findById(1L)).thenReturn(Optional.of(testSubscription));
        when(subscriptionLevelRepository.findById(2L)).thenReturn(Optional.of(newLevel));
        when(subscriptionRepository.save(any(Subscription.class))).thenReturn(testSubscription);

        // When
        Subscription result = subscriptionService.changeSubscriptionLevel(1L, 2L);

        // Then
        assertNotNull(result, "L'abonnement modifié ne doit pas être null");
        assertEquals(newLevel, testSubscription.getSubscriptionLevel(),
                "Le niveau d'abonnement doit être mis à jour");
        verify(subscriptionRepository).save(testSubscription);
    }

    @Test
    @DisplayName("Changement de niveau d'abonnement avec événement")
    void testChangeSubscriptionLevelWithEvent_Success() {
        // Given
        String newLevelName = "PREMIUM";
        SubscriptionLevel newLevel = new SubscriptionLevel();
        newLevel.setLevel(SubscriptionLevel.Level.PREMIUM);

        when(subscriptionRepository.findById(1L)).thenReturn(Optional.of(testSubscription));
        when(subscriptionLevelRepository.findByLevel(SubscriptionLevel.Level.PREMIUM))
                .thenReturn(Optional.of(newLevel));
        when(subscriptionRepository.save(any(Subscription.class))).thenReturn(testSubscription);

        // When
        subscriptionService.changeSubscriptionLevel(1L, newLevelName);

        // Then
        verify(subscriptionRepository).save(testSubscription);
        verify(levelChangedEventPublisher).publishSubscriptionLevelChangedEvent(
                testSubscription,
                "BASIC");
    }

    // ========== TESTS DE SUPPRESSION D'ABONNEMENT ==========

    @Test
    @DisplayName("Suppression d'abonnement")
    void testDeleteSubscription() {
        // When
        subscriptionService.deleteSubscription(1L);

        // Then
        verify(subscriptionRepository).deleteById(1L);
    }

    // ========== TESTS DE GESTION DES NIVEAUX D'ABONNEMENT ==========

    @Test
    @DisplayName("Création de niveau d'abonnement")
    void testCreateSubscriptionLevel_Success() {
        // Given
        when(subscriptionLevelRepository.save(testSubscriptionLevel)).thenReturn(testSubscriptionLevel);

        // When
        SubscriptionLevel result = subscriptionService.createSubscriptionLevel(testSubscriptionLevel);

        // Then
        assertNotNull(result, "Le niveau créé ne doit pas être null");
        assertEquals(testSubscriptionLevel.getId(), result.getId(), "L'ID doit correspondre");
        verify(subscriptionLevelRepository).save(testSubscriptionLevel);
    }

    @Test
    @DisplayName("Récupération de tous les niveaux d'abonnement")
    void testGetAllSubscriptionLevels() {
        // Given
        List<SubscriptionLevel> levels = Arrays.asList(testSubscriptionLevel);
        when(subscriptionLevelRepository.findAll()).thenReturn(levels);

        // When
        List<SubscriptionLevel> result = subscriptionService.getAllSubscriptionLevels();

        // Then
        assertNotNull(result, "La liste ne doit pas être null");
        assertEquals(1, result.size(), "La liste doit contenir un niveau");
        verify(subscriptionLevelRepository).findAll();
    }

    // ========== TESTS DE GESTION DES PROMOTIONS ==========

    @Test
    @DisplayName("Création de promotion")
    void testCreatePromotion_Success() {
        // Given
        when(promotionRepository.save(testPromotion)).thenReturn(testPromotion);

        // When
        Promotion result = subscriptionService.createPromotion(testPromotion);

        // Then
        assertNotNull(result, "La promotion créée ne doit pas être null");
        assertEquals(testPromotion.getId(), result.getId(), "L'ID doit correspondre");
        verify(promotionRepository).save(testPromotion);
    }

    @Test
    @DisplayName("Application de promotion - Cas de succès")
    void testApplyPromotion_Success() {
        // Given
        when(subscriptionRepository.findById(1L)).thenReturn(Optional.of(testSubscription));
        when(promotionRepository.findByPromotionCode("PROMO2024")).thenReturn(testPromotion);
        when(subscriptionRepository.save(any(Subscription.class))).thenReturn(testSubscription);

        // When
        Subscription result = subscriptionService.applyPromotion(1L, "PROMO2024");

        // Then
        assertNotNull(result, "L'abonnement avec promotion ne doit pas être null");
        assertEquals(testPromotion, testSubscription.getPromotion(),
                "La promotion doit être appliquée");
        verify(subscriptionRepository).save(testSubscription);
    }

    @Test
    @DisplayName("Application de promotion - Promotion expirée")
    void testApplyPromotion_ExpiredPromotion() {
        // Given - Promotion expirée
        testPromotion.setEndDate(LocalDateTime.now().minusDays(1));
        when(subscriptionRepository.findById(1L)).thenReturn(Optional.of(testSubscription));
        when(promotionRepository.findByPromotionCode("PROMO2024")).thenReturn(testPromotion);

        // When
        Subscription result = subscriptionService.applyPromotion(1L, "PROMO2024");

        // Then
        assertNull(result, "L'application d'une promotion expirée doit retourner null");
        verify(subscriptionRepository, never()).save(any());
    }

    // ========== TESTS DE RENOUVELLEMENT D'ABONNEMENT ==========

    @Test
    @DisplayName("Traitement du renouvellement d'abonnement - Cas de succès")
    void testProcessSubscriptionRenewal_Success() {
        // Given
        List<Subscription> userSubscriptions = Arrays.asList(testSubscription);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(subscriptionRepository.findByUser(testUser)).thenReturn(userSubscriptions);
        when(subscriptionRepository.save(any(Subscription.class))).thenReturn(testSubscription);

        // When
        subscriptionService.processSubscriptionRenewal(1L, "test@example.com", "BASIC", "push_token");

        // Then
        // Vérifier que la date de fin a été prolongée d'un mois
        assertTrue(testSubscription.getEndDate().isAfter(LocalDateTime.now().plusDays(25)),
                "La date de fin doit être prolongée");
        assertEquals(Subscription.SubscriptionStatus.ACTIVE, testSubscription.getStatus(),
                "Le statut doit rester actif");
        verify(subscriptionRepository).save(testSubscription);
    }

    @Test
    @DisplayName("Traitement du renouvellement - Utilisateur inexistant")
    void testProcessSubscriptionRenewal_UserNotFound() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> subscriptionService.processSubscriptionRenewal(1L, "test@example.com", "BASIC", "push_token"),
                "Une exception doit être levée pour un utilisateur inexistant");
        assertTrue(exception.getMessage().contains("User not found"),
                "Le message doit mentionner l'utilisateur introuvable");
    }

    // ========== TESTS DE GESTION DES ÉCHECS DE PAIEMENT ==========

    @Test
    @DisplayName("Gestion d'échec de paiement - Cas de succès")
    void testHandlePaymentFailure_Success() {
        // Given
        List<Subscription> userSubscriptions = Arrays.asList(testSubscription);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(subscriptionRepository.findByUser(testUser)).thenReturn(userSubscriptions);
        when(subscriptionRepository.save(any(Subscription.class))).thenReturn(testSubscription);

        // When
        subscriptionService.handlePaymentFailure(1L, "test@example.com", "BASIC", "push_token");

        // Then
        assertEquals(Subscription.SubscriptionStatus.PAYMENT_FAILED, testSubscription.getStatus(),
                "Le statut doit être mis à jour à PAYMENT_FAILED");
        verify(subscriptionRepository).save(testSubscription);
    }

    @Test
    @DisplayName("Gestion de succès de paiement - Cas de succès")
    void testHandlePaymentSuccess_Success() {
        // Given
        testSubscription.setStatus(Subscription.SubscriptionStatus.PAYMENT_FAILED);
        List<Subscription> userSubscriptions = Arrays.asList(testSubscription);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(subscriptionRepository.findByUser(testUser)).thenReturn(userSubscriptions);
        when(subscriptionRepository.save(any(Subscription.class))).thenReturn(testSubscription);

        // When
        subscriptionService.handlePaymentSuccess(1L, "test@example.com", "BASIC", "push_token");

        // Then
        assertEquals(Subscription.SubscriptionStatus.ACTIVE, testSubscription.getStatus(),
                "Le statut doit être remis à ACTIVE");
        verify(subscriptionRepository).save(testSubscription);
    }

    // ========== TESTS DE CRÉATION AVEC STRIPE ==========

    @Test
    @DisplayName("Création d'abonnement avec Stripe - Cas de succès")
    void testCreateNewSubscription_Success() throws StripeException {
        // Given
        com.stripe.model.Subscription stripeSubscription = mock(com.stripe.model.Subscription.class);
        when(stripeSubscription.getId()).thenReturn("sub_stripe123");
        when(stripeSubscription.getCurrency()).thenReturn("eur");
        when(stripeSubscription.getCurrentPeriodEnd()).thenReturn(System.currentTimeMillis() / 1000 + 2592000); // +30
                                                                                                                // jours

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(stripeBillingService.createSubscription(testUser.getStripeCustomerId(),
                testSubscriptionLevel.getStripePriceId()))
                .thenReturn(stripeSubscription);
        when(subscriptionRepository.save(any(Subscription.class))).thenReturn(testSubscription);

        // When
        Subscription result = subscriptionService.createNewSubscription(1L, testSubscriptionLevel);

        // Then
        assertNotNull(result, "L'abonnement créé ne doit pas être null");
        verify(stripeBillingService).createSubscription(testUser.getStripeCustomerId(),
                testSubscriptionLevel.getStripePriceId());
        verify(subscriptionRepository).save(any(Subscription.class));
    }

    @Test
    @DisplayName("Création d'abonnement avec Stripe - Échec Stripe")
    void testCreateNewSubscription_StripeFailure() throws StripeException {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(stripeBillingService.createSubscription(anyString(), anyString()))
                .thenThrow(new ApiException("Erreur Stripe", "request_id", "code", null, null));

        // When & Then
        StripeIntegrationException exception = assertThrows(
                StripeIntegrationException.class,
                () -> subscriptionService.createNewSubscription(1L, testSubscriptionLevel),
                "Une StripeIntegrationException doit être levée");
        assertTrue(exception.getMessage().contains("Erreur lors de la création"),
                "Le message doit mentionner l'erreur de création");
        verify(subscriptionRepository, never()).save(any());
    }

    // ========== TESTS D'ANNULATION D'ABONNEMENT ==========

    @Test
    @DisplayName("Annulation d'abonnement - Cas de succès avec remboursement")
    void testCancelSubscription_WithRefund() throws StripeException {
        // Given - Abonnement récent (dans les 7 jours)
        testSubscription.setStartDate(LocalDateTime.now().minusDays(3));
        String reason = "customer_request";
        Refund mockRefund = mock(Refund.class);
        when(mockRefund.getId()).thenReturn("re_test123");

        when(subscriptionRepository.findById(1L)).thenReturn(Optional.of(testSubscription));
        when(subscriptionRepository.save(any(Subscription.class))).thenReturn(testSubscription);
        when(stripeBillingService.refundCharge(eq("ch_test123"), any())).thenReturn(mockRefund);

        // When
        subscriptionService.cancelSubscription(1L, reason);

        // Then
        assertEquals(Subscription.SubscriptionStatus.CANCELLED, testSubscription.getStatus(),
                "Le statut doit être CANCELLED");
        assertNotNull(testSubscription.getCancelledAt(), "La date d'annulation doit être définie");
        verify(subscriptionCancelledEventPublisher).publishSubscriptionCancelledEvent(testSubscription, reason);
        verify(accessControlEventPublisher).publishSubscriptionCancelledForAccessControl(
                testUser.getId(), "BASIC", reason);
        verify(stripeBillingService).refundCharge(eq("ch_test123"), any());
    }

    @Test
    @DisplayName("Annulation d'abonnement - Sans remboursement")
    void testCancelSubscription_WithoutRefund() throws StripeException {
        // Given - Abonnement ancien (plus de 7 jours)
        testSubscription.setStartDate(LocalDateTime.now().minusDays(15));
        String reason = "customer_request";

        when(subscriptionRepository.findById(1L)).thenReturn(Optional.of(testSubscription));
        when(subscriptionRepository.save(any(Subscription.class))).thenReturn(testSubscription);

        // When
        subscriptionService.cancelSubscription(1L, reason);

        // Then
        assertEquals(Subscription.SubscriptionStatus.CANCELLED, testSubscription.getStatus(),
                "Le statut doit être CANCELLED");
        verify(subscriptionCancelledEventPublisher).publishSubscriptionCancelledEvent(testSubscription, reason);
        verify(stripeBillingService, never()).refundCharge(anyString(), any());
    }

    // ========== TESTS DE RÉACTIVATION D'ABONNEMENT ==========

    @Test
    @DisplayName("Réactivation d'abonnement - Cas de succès")
    void testReactivateSubscription_Success() {
        // Given
        testSubscription.setStatus(Subscription.SubscriptionStatus.CANCELLED);
        LocalDateTime nextBillingDate = LocalDateTime.now().plusMonths(1);

        when(subscriptionRepository.findById(1L)).thenReturn(Optional.of(testSubscription));
        when(subscriptionRepository.save(any(Subscription.class))).thenReturn(testSubscription);
        when(billingCalculationService.calculateNextBillingDateAfterReactivation(testSubscription))
                .thenReturn(nextBillingDate);

        // When
        subscriptionService.reactivateSubscription(1L);

        // Then
        assertEquals(Subscription.SubscriptionStatus.ACTIVE, testSubscription.getStatus(),
                "Le statut doit être ACTIVE");
        assertEquals(nextBillingDate, testSubscription.getNextBillingDate(),
                "La prochaine date de facturation doit être mise à jour");
        verify(subscriptionReactivatedEventPublisher).publishSubscriptionReactivatedEvent(testSubscription);
        verify(accessControlEventPublisher).publishSubscriptionReactivatedForAccessControl(
                testUser.getId(), "BASIC");
    }

    // ========== TESTS DE RENOUVELLEMENT RÉUSSI ==========

    @Test
    @DisplayName("Traitement de renouvellement réussi")
    void testProcessSuccessfulRenewal() {
        // Given
        LocalDateTime nextBillingDate = LocalDateTime.now().plusMonths(1);
        when(subscriptionRepository.findById(1L)).thenReturn(Optional.of(testSubscription));
        when(subscriptionRepository.save(any(Subscription.class))).thenReturn(testSubscription);
        when(billingCalculationService.calculateNextBillingDate(testSubscription))
                .thenReturn(nextBillingDate);

        // When
        subscriptionService.processSuccessfulRenewal(1L);

        // Then
        assertEquals(nextBillingDate, testSubscription.getNextBillingDate(),
                "La prochaine date de facturation doit être mise à jour");
        verify(subscriptionRenewedEventPublisher).publishSubscriptionRenewedEvent(testSubscription);
        verify(subscriptionRepository).save(testSubscription);
    }

    // ========== TESTS DE RECHERCHE D'ABONNEMENTS ==========

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("Recherche d'abonnements avec filtres")
    void testSearchSubscriptions_WithFilters() {
        // Given
        List<Subscription> subscriptions = Arrays.asList(testSubscription);
        Page<Subscription> subscriptionPage = new PageImpl<>(subscriptions);
        when(subscriptionRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(subscriptionPage);

        // When
        List<Subscription> result = subscriptionService.searchSubscriptions(1L, "ACTIVE", "BASIC", 0, 10);

        // Then
        assertNotNull(result, "Le résultat ne doit pas être null");
        assertEquals(1, result.size(), "Un abonnement doit être trouvé");
        verify(subscriptionRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    // ========== TESTS DE MISE À JOUR ADMINISTRATIVE ==========

    @Test
    @DisplayName("Mise à jour administrative d'abonnement")
    void testUpdateSubscriptionByAdmin_Success() {
        // Given
        AdminUpdateSubscriptionRequest updateRequest = new AdminUpdateSubscriptionRequest();
        updateRequest.setStatus(Subscription.SubscriptionStatus.INACTIVE);
        updateRequest.setStartDate(LocalDateTime.now().plusDays(1));
        updateRequest.setEndDate(LocalDateTime.now().plusMonths(2));
        updateRequest.setSubscriptionLevelId(2L);

        SubscriptionLevel newLevel = new SubscriptionLevel();
        newLevel.setId(2L);

        when(subscriptionRepository.findById(1L)).thenReturn(Optional.of(testSubscription));
        when(subscriptionLevelRepository.findById(2L)).thenReturn(Optional.of(newLevel));
        when(subscriptionRepository.save(any(Subscription.class))).thenReturn(testSubscription);

        // When
        Subscription result = subscriptionService.updateSubscriptionByAdmin(1L, updateRequest);

        // Then
        assertEquals(Subscription.SubscriptionStatus.INACTIVE, result.getStatus(),
                "Le statut doit être INACTIVE");
        assertEquals(LocalDateTime.now().plusDays(1), result.getStartDate(),
                "La date de début doit être mise à jour");
        assertEquals(LocalDateTime.now().plusMonths(2), result.getEndDate(),
                "La date de fin doit être mise à jour");
        assertEquals(newLevel, result.getSubscriptionLevel(), "Le niveau doit être mis à jour");
        verify(subscriptionRepository).save(testSubscription);
    }
}