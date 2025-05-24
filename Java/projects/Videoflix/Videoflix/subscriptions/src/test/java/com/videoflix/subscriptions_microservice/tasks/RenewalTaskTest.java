package com.videoflix.subscriptions_microservice.tasks;

import com.videoflix.subscriptions_microservice.entities.Subscription;
import com.videoflix.subscriptions_microservice.entities.User; // Importation de l'entité User pour créer des abonnements
import com.videoflix.subscriptions_microservice.services.StripePaymentService;
import com.videoflix.subscriptions_microservice.services.SubscriptionService;
import com.stripe.exception.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

// @ExtendWith(MockitoExtension.class) permet l'initialisation des mocks par Mockito.
@ExtendWith(MockitoExtension.class)
@DisplayName("Tests pour RenewalTask")
class RenewalTaskTest {

    // @Mock crée un mock du service de gestion des abonnements.
    @Mock
    private SubscriptionService subscriptionService;

    // @Mock crée un mock du service de paiement Stripe.
    @Mock
    private StripePaymentService stripePaymentService;

    // @InjectMocks crée une instance de RenewalTask et y injecte les mocks.
    @InjectMocks
    private RenewalTask renewalTask;

    // Horodatage fixe pour simuler 'LocalDateTime.now()' afin de rendre les tests
    // déterministes.
    // La tâche est planifiée à minuit, donc nous fixons cette heure pour la
    // cohérence.
    private LocalDateTime fixedNow;

    /**
     * Méthode exécutée avant chaque test.
     * Initialise `fixedNow` pour les calculs de date de renouvellement.
     */
    @BeforeEach
    void setUp() {
        // Définit une date et heure fixe pour "aujourd'hui" (le 24 mai 2025 à minuit)
        // cela correspond à l'heure d'exécution planifiée de la tâche.
        fixedNow = LocalDateTime.of(2025, 5, 24, 0, 0, 0);

        // Réinitialise les mocks pour garantir l'indépendance des tests.
        reset(subscriptionService, stripePaymentService);
    }

    @Test
    @DisplayName("Devrait renouveler un abonnement éligible avec succès")
    void checkAndRenewSubscriptions_shouldRenewEligibleSubscriptionSuccessfully() throws StripeException {
        // GIVEN: Un utilisateur et un abonnement éligible au renouvellement.
        User user = new User();
        user.setId(1L);

        Subscription subscriptionToRenew = new Subscription();
        subscriptionToRenew.setId(101L);
        subscriptionToRenew.setAutoRenew(true); // Renouvellement automatique activé
        subscriptionToRenew.setNextRenewalDate(fixedNow.minusDays(1)); // Date de renouvellement passée
        subscriptionToRenew.setUser(user);
        subscriptionToRenew.setStripeSubscriptionId("old_stripe_id_1");

        // Liste des abonnements retournée par le service.
        List<Subscription> allSubscriptions = Collections.singletonList(subscriptionToRenew);

        // Crée une nouvelle instance de l'abonnement qui serait retournée par Stripe
        // après renouvellement.
        // C'est important car `createStripeSubscription` dans le code réel pourrait
        // renvoyer un nouvel objet
        // ou un objet mis à jour. Ici, nous simulons la mise à jour de l'ID Stripe.
        Subscription renewedSubscriptionFromStripe = new Subscription();
        renewedSubscriptionFromStripe.setId(101L); // Même ID
        renewedSubscriptionFromStripe.setStripeSubscriptionId("new_stripe_id_1"); // Nouvel ID Stripe après
                                                                                  // renouvellement
        renewedSubscriptionFromStripe.setNextRenewalDate(fixedNow.plusMonths(1)); // Date de renouvellement mise à jour
        renewedSubscriptionFromStripe.setUser(user);

        // Configure le comportement des mocks:
        // 1. `getAllSubscriptions` retourne notre liste d'abonnements.
        when(subscriptionService.getAllSubscriptions(anyInt(), anyInt())).thenReturn(allSubscriptions);
        // 2. `createStripeSubscription` retourne l'abonnement avec l'ID Stripe mis à
        // jour.
        when(stripePaymentService.createStripeSubscription(subscriptionToRenew))
                .thenReturn(renewedSubscriptionFromStripe);
        // 3. `updateSubscription` ne fait rien (void method).
        doNothing().when(subscriptionService).updateSubscription(subscriptionToRenew.getId(), any(Subscription.class));

        // WHEN: La tâche de renouvellement est exécutée.
        renewalTask.checkAndRenewSubscriptions();

        // THEN:
        // 1. Vérifie que `getAllSubscriptions` a été appelé une fois.
        verify(subscriptionService, times(1)).getAllSubscriptions(0, Integer.MAX_VALUE);
        // 2. Vérifie que `createStripeSubscription` a été appelé une fois avec le bon
        // abonnement.
        verify(stripePaymentService, times(1)).createStripeSubscription(subscriptionToRenew);
        // 3. Vérifie que `updateSubscription` a été appelé une fois avec l'abonnement
        // mis à jour.
        // Note: Nous capturons l'argument pour vérifier son état après les
        // modifications.
        // Le `nextRenewalDate` devrait être mis à jour à `fixedNow.plusMonths(1)`.
        // L'ID Stripe devrait être mis à jour à "new_stripe_id_1".
        verify(subscriptionService, times(1)).updateSubscription(subscriptionToRenew.getId(),
                argThat(arg -> arg.getStripeSubscriptionId().equals("new_stripe_id_1") &&
                        arg.getNextRenewalDate().equals(fixedNow.plusMonths(1))));
    }

    @Test
    @DisplayName("Ne devrait PAS renouveler un abonnement si le renouvellement automatique est désactivé")
    void checkAndRenewSubscriptions_shouldNotRenewIfAutoRenewIsDisabled() throws StripeException {
        // GIVEN: Un abonnement dont le renouvellement automatique est désactivé.
        User user = new User();
        user.setId(1L);

        Subscription subscription = new Subscription();
        subscription.setId(101L);
        subscription.setAutoRenew(false); // Renouvellement automatique désactivé
        subscription.setNextRenewalDate(fixedNow.minusDays(1)); // Date de renouvellement passée (mais non pertinent)
        subscription.setUser(user);

        List<Subscription> allSubscriptions = Collections.singletonList(subscription);

        // Configure le service d'abonnement.
        when(subscriptionService.getAllSubscriptions(anyInt(), anyInt())).thenReturn(allSubscriptions);

        // WHEN: La tâche de renouvellement est exécutée.
        renewalTask.checkAndRenewSubscriptions();

        // THEN:
        // 1. Vérifie que `getAllSubscriptions` a été appelé.
        verify(subscriptionService, times(1)).getAllSubscriptions(0, Integer.MAX_VALUE);
        // 2. Vérifie qu'aucune interaction avec Stripe n'a eu lieu.
        verify(stripePaymentService, never()).createStripeSubscription(any(Subscription.class));
        // 3. Vérifie qu'aucune mise à jour d'abonnement n'a eu lieu.
        verify(subscriptionService, never()).updateSubscription(anyLong(), any(Subscription.class));
    }

    @Test
    @DisplayName("Ne devrait PAS renouveler un abonnement si la date de renouvellement est dans le futur")
    void checkAndRenewSubscriptions_shouldNotRenewIfRenewalDateIsInFuture() throws StripeException {
        // GIVEN: Un abonnement dont la prochaine date de renouvellement est dans le
        // futur.
        User user = new User();
        user.setId(1L);

        Subscription subscription = new Subscription();
        subscription.setId(101L);
        subscription.setAutoRenew(true);
        subscription.setNextRenewalDate(fixedNow.plusDays(5)); // Date de renouvellement dans le futur
        subscription.setUser(user);

        List<Subscription> allSubscriptions = Collections.singletonList(subscription);

        // Configure le service d'abonnement.
        when(subscriptionService.getAllSubscriptions(anyInt(), anyInt())).thenReturn(allSubscriptions);

        // WHEN: La tâche de renouvellement est exécutée.
        renewalTask.checkAndRenewSubscriptions();

        // THEN:
        // 1. Vérifie que `getAllSubscriptions` a été appelé.
        verify(subscriptionService, times(1)).getAllSubscriptions(0, Integer.MAX_VALUE);
        // 2. Vérifie qu'aucune interaction avec Stripe n'a eu lieu.
        verify(stripePaymentService, never()).createStripeSubscription(any(Subscription.class));
        // 3. Vérifie qu'aucune mise à jour d'abonnement n'a eu lieu.
        verify(subscriptionService, never()).updateSubscription(anyLong(), any(Subscription.class));
    }

    @Test
    @DisplayName("Ne devrait PAS renouveler un abonnement si nextRenewalDate est null")
    void checkAndRenewSubscriptions_shouldNotRenewIfNextRenewalDateIsNull() throws StripeException {
        // GIVEN: Un abonnement avec un `nextRenewalDate` null.
        User user = new User();
        user.setId(1L);

        Subscription subscription = new Subscription();
        subscription.setId(101L);
        subscription.setAutoRenew(true);
        subscription.setNextRenewalDate(null); // Date de renouvellement nulle
        subscription.setUser(user);

        List<Subscription> allSubscriptions = Collections.singletonList(subscription);

        // Configure le service d'abonnement.
        when(subscriptionService.getAllSubscriptions(anyInt(), anyInt())).thenReturn(allSubscriptions);

        // WHEN: La tâche de renouvellement est exécutée.
        renewalTask.checkAndRenewSubscriptions();

        // THEN:
        // 1. Vérifie que `getAllSubscriptions` a été appelé.
        verify(subscriptionService, times(1)).getAllSubscriptions(0, Integer.MAX_VALUE);
        // 2. Vérifie qu'aucune interaction avec Stripe n'a eu lieu.
        verify(stripePaymentService, never()).createStripeSubscription(any(Subscription.class));
        // 3. Vérifie qu'aucune mise à jour d'abonnement n'a eu lieu.
        verify(subscriptionService, never()).updateSubscription(anyLong(), any(Subscription.class));
    }

    @Test
    @DisplayName("Devrait gérer une exception Stripe pendant le renouvellement et continuer")
    void checkAndRenewSubscriptions_shouldHandleStripeExceptionAndContinue() throws StripeException {
        // GIVEN: Un abonnement éligible, mais Stripe lève une exception.
        User user1 = new User();
        user1.setId(1L);
        User user2 = new User();
        user2.setId(2L);

        Subscription sub1 = new Subscription();
        sub1.setId(101L);
        sub1.setAutoRenew(true);
        sub1.setNextRenewalDate(fixedNow.minusDays(1));
        sub1.setUser(user1);

        Subscription sub2 = new Subscription(); // Un autre abonnement qui devrait se renouveler avec succès
        sub2.setId(102L);
        sub2.setAutoRenew(true);
        sub2.setNextRenewalDate(fixedNow.minusDays(1));
        sub2.setUser(user2);

        List<Subscription> allSubscriptions = Arrays.asList(sub1, sub2);

        // Configure le service d'abonnement pour retourner les deux abonnements.
        when(subscriptionService.getAllSubscriptions(anyInt(), anyInt())).thenReturn(allSubscriptions);

        // Simule une exception Stripe lors de la tentative de renouvellement du premier
        // abonnement.
        doThrow(new ApiException("Erreur Stripe simulée", "code", "param", null, null))
                .when(stripePaymentService).createStripeSubscription(sub1);

        // Pour le deuxième abonnement, simule un renouvellement réussi.
        Subscription renewedSub2 = new Subscription();
        renewedSub2.setId(102L);
        renewedSub2.setStripeSubscriptionId("new_stripe_id_2");
        renewedSub2.setNextRenewalDate(fixedNow.plusMonths(1));
        renewedSub2.setUser(user2);
        when(stripePaymentService.createStripeSubscription(sub2))
                .thenReturn(renewedSub2);
        doNothing().when(subscriptionService).updateSubscription(sub2.getId(), any(Subscription.class));

        // WHEN: La tâche est exécutée.
        renewalTask.checkAndRenewSubscriptions();

        // THEN:
        // 1. Vérifie que `getAllSubscriptions` a été appelé.
        verify(subscriptionService, times(1)).getAllSubscriptions(0, Integer.MAX_VALUE);

        // 2. Vérifie que `createStripeSubscription` a été appelé pour les deux
        // abonnements.
        verify(stripePaymentService, times(1)).createStripeSubscription(sub1); // Échec ici
        verify(stripePaymentService, times(1)).createStripeSubscription(sub2); // Succès ici

        // 3. Vérifie que `updateSubscription` n'a PAS été appelé pour le premier
        // abonnement (échec).
        verify(subscriptionService, never()).updateSubscription(sub1.getId(), any(Subscription.class));
        // 4. Vérifie que `updateSubscription` a été appelé pour le deuxième abonnement
        // (succès).
        verify(subscriptionService, times(1)).updateSubscription(sub2.getId(),
                argThat(arg -> arg.getStripeSubscriptionId().equals("new_stripe_id_2") &&
                        arg.getNextRenewalDate().equals(fixedNow.plusMonths(1))));
    }

    @Test
    @DisplayName("Devrait gérer le cas où il n'y a aucun abonnement à traiter")
    void checkAndRenewSubscriptions_shouldHandleNoSubscriptions() throws StripeException {
        // GIVEN: Le service d'abonnement ne retourne aucun abonnement.
        when(subscriptionService.getAllSubscriptions(anyInt(), anyInt())).thenReturn(Collections.emptyList());

        // WHEN: La tâche de renouvellement est exécutée.
        renewalTask.checkAndRenewSubscriptions();

        // THEN:
        // 1. Vérifie que `getAllSubscriptions` a été appelé.
        verify(subscriptionService, times(1)).getAllSubscriptions(0, Integer.MAX_VALUE);
        // 2. Vérifie qu'aucune autre interaction avec Stripe ou le service d'abonnement
        // n'a eu lieu.
        verify(stripePaymentService, never()).createStripeSubscription(any(Subscription.class));
        verify(subscriptionService, never()).updateSubscription(anyLong(), any(Subscription.class));
    }
}