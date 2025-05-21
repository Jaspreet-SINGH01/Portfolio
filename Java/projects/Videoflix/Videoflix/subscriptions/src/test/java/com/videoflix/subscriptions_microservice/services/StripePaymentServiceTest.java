package com.videoflix.subscriptions_microservice.services;

import com.stripe.Stripe;
import com.stripe.exception.ApiException;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.Refund;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.RefundCreateParams;
import com.stripe.param.SubscriptionCreateParams;
import com.videoflix.subscriptions_microservice.entities.Payment;
import com.videoflix.subscriptions_microservice.repositories.PaymentRepository;
import com.videoflix.subscriptions_microservice.repositories.SubscriptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

// @ExtendWith(MockitoExtension.class) permet d'utiliser les fonctionnalités de Mockito dans ce test.
@ExtendWith(MockitoExtension.class)
class StripePaymentServiceTest {

        // Clé secrète Stripe de test
        private static final String TEST_SECRET_KEY = "vdfx_test_xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx";

        // @Mock pour le repository des abonnements.
        @Mock
        private SubscriptionRepository subscriptionRepository;

        // @Mock pour le repository des paiements.
        @Mock
        private PaymentRepository paymentRepository;

        // @InjectMocks crée une instance de StripePaymentService et injecte les mocks.
        @InjectMocks
        private StripePaymentService stripePaymentService;

        // Configuration exécutée avant chaque test pour initialiser la clé API Stripe.
        @SuppressWarnings("unchecked")
        @BeforeEach
        void setUp() {
                ReflectionTestUtils.setField(stripePaymentService, "secretKey", TEST_SECRET_KEY);
                assertEquals(TEST_SECRET_KEY, Stripe.apiKey);
                reset(subscriptionRepository, paymentRepository);
        }

        // Test pour vérifier la création d'un client Stripe.
        @Test
        void createStripeCustomer_shouldCreateCustomerAndReturnId() throws StripeException {
                // GIVEN : Un email et un nom de client.
                String email = "test@example.com";
                String name = "Test User";
                String customerId = "cus_TEST";

                // Mock de la classe Customer de Stripe.
                try (MockedStatic<Customer> mockedCustomer = Mockito.mockStatic(Customer.class)) {
                        Customer mockCustomer = mock(Customer.class);
                        when(mockCustomer.getId()).thenReturn(customerId);
                        when(Customer.create(any(CustomerCreateParams.class))).thenReturn(mockCustomer);

                        // WHEN : Appel de la méthode createStripeCustomer.
                        String actualCustomerId = stripePaymentService.createStripeCustomer(email, name);

                        // THEN : Vérification que Customer.create a été appelé avec les bons
                        // paramètres.
                        ArgumentCaptor<CustomerCreateParams> paramsCaptor = ArgumentCaptor
                                        .forClass(CustomerCreateParams.class);
                        mockedCustomer.verify(() -> Customer.create(paramsCaptor.capture()), times(1));
                        assertEquals(email, paramsCaptor.getValue().getEmail());
                        assertEquals(name, paramsCaptor.getValue().getName());
                        // Vérification que l'ID de client retourné est correct.
                        assertEquals(customerId, actualCustomerId);
                }
        }

        // Test pour vérifier la création d'un abonnement Stripe avec succès.
        @Test
        void createStripeSubscription_shouldCreateSubscriptionAndPaymentOnSuccess() throws StripeException {
                // GIVEN : Un objet Subscription local.
                com.videoflix.subscriptions_microservice.entities.Subscription subscription = new com.videoflix.subscriptions_microservice.entities.Subscription();
                subscription.setCustomerId("cus_TEST");
                subscription.setPriceId("price_TEST"); // Assurez-vous que c'est priceId et non id
                String stripeSubscriptionId = "sub_TEST";

                // Mock de la classe com.stripe.model.Subscription.
                try (MockedStatic<com.stripe.model.Subscription> mockedStripeSubscription = Mockito
                                .mockStatic(com.stripe.model.Subscription.class)) {
                        com.stripe.model.Subscription mockStripeSub = mock(com.stripe.model.Subscription.class);
                        when(mockStripeSub.getId()).thenReturn(stripeSubscriptionId);
                        when(com.stripe.model.Subscription.create(any(SubscriptionCreateParams.class)))
                                        .thenReturn(mockStripeSub);
                        when(subscriptionRepository.save(
                                        any(com.videoflix.subscriptions_microservice.entities.Subscription.class)))
                                        .thenReturn(subscription);
                        when(paymentRepository.save(any(Payment.class))).thenReturn(new Payment());

                        // WHEN : Appel de la méthode createStripeSubscription.
                        com.videoflix.subscriptions_microservice.entities.Subscription actualSubscription = stripePaymentService
                                        .createStripeSubscription(subscription);

                        // THEN : Vérification que StripeSubscription.create a été appelé avec les bons
                        // paramètres.
                        ArgumentCaptor<SubscriptionCreateParams> paramsCaptor = ArgumentCaptor
                                        .forClass(SubscriptionCreateParams.class);
                        mockedStripeSubscription.verify(
                                        () -> com.stripe.model.Subscription.create(paramsCaptor.capture()),
                                        times(1));
                        assertEquals(subscription.getCustomerId(), paramsCaptor.getValue().getCustomer());
                        assertEquals(subscription.getPriceId(), paramsCaptor.getValue().getItems().get(0).getPrice());
                        // Vérification que l'ID d'abonnement Stripe a été mis à jour.
                        assertEquals(stripeSubscriptionId, actualSubscription.getStripeSubscriptionId());
                        // Vérification que subscriptionRepository.save a été appelé.
                        verify(subscriptionRepository, times(1)).save(subscription);
                        // Vérification que paymentRepository.save a été appelé avec un Payment réussi.
                        ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);
                        verify(paymentRepository, times(1)).save(paymentCaptor.capture());
                        assertEquals(Payment.PaymentStatus.SUCCESS, paymentCaptor.getValue().getStatus());
                        assertEquals(stripeSubscriptionId, paymentCaptor.getValue().getPaymentId());
                        assertEquals(subscription, paymentCaptor.getValue().getSubscription());
                }
        }

        // Test pour vérifier la création d'un abonnement Stripe avec échec.
        @Test
        void createStripeSubscription_shouldHandleStripeExceptionAndSaveError() throws StripeException {
                // GIVEN : Un objet Subscription local.
                com.videoflix.subscriptions_microservice.entities.Subscription subscription = new com.videoflix.subscriptions_microservice.entities.Subscription();
                subscription.setCustomerId("cus_TEST");
                subscription.setPriceId("price_TEST"); // Assurez-vous que c'est priceId et non id
                String errorMessage = "Stripe error occurred";

                // Mock de la classe com.stripe.model.Subscription pour simuler une exception.
                try (MockedStatic<com.stripe.model.Subscription> mockedStripeSubscription = Mockito
                                .mockStatic(com.stripe.model.Subscription.class)) {
                        when(com.stripe.model.Subscription.create(any(SubscriptionCreateParams.class)))
                                        .thenThrow(new ApiException(errorMessage, errorMessage, errorMessage, null,
                                                        null));
                        when(subscriptionRepository.save(
                                        any(com.videoflix.subscriptions_microservice.entities.Subscription.class)))
                                        .thenReturn(subscription);
                        when(paymentRepository.save(any(Payment.class))).thenReturn(new Payment());

                        // WHEN : Appel de la méthode createStripeSubscription qui devrait lancer une
                        // exception.
                        com.videoflix.subscriptions_microservice.entities.Subscription actualSubscription = stripePaymentService
                                        .createStripeSubscription(subscription);

                        // THEN : Vérification que l'erreur a été enregistrée dans l'abonnement.
                        assertEquals(errorMessage, actualSubscription.getLastPaymentError());
                        // Vérification que subscriptionRepository.save a été appelé.
                        verify(subscriptionRepository, times(1)).save(subscription);
                        // Vérification que paymentRepository.save a été appelé avec un Payment en
                        // échec.
                        ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);
                        verify(paymentRepository, times(1)).save(paymentCaptor.capture());
                        assertEquals(Payment.PaymentStatus.FAILED, paymentCaptor.getValue().getStatus());
                        assertEquals(errorMessage, paymentCaptor.getValue().getErrorMessage());
                        assertEquals(subscription, paymentCaptor.getValue().getSubscription());
                }
        }

        // Test pour vérifier le remboursement d'un abonnement Stripe.
        @Test
        void refundSubscription_shouldCreateRefundAndUpdateSubscription() throws StripeException {
                // GIVEN : Un objet Subscription local et des informations de remboursement.
                com.videoflix.subscriptions_microservice.entities.Subscription subscription = new com.videoflix.subscriptions_microservice.entities.Subscription();
                subscription.setPaymentId("pi_TEST");
                double amount = 5.0;
                String reason = "requested_by_customer";

                // Mock de la classe Refund de Stripe.
                try (MockedStatic<Refund> mockedRefund = Mockito.mockStatic(Refund.class)) {
                        Refund mockRefund = mock(Refund.class);
                        when(Refund.create(any(RefundCreateParams.class))).thenReturn(mockRefund);
                        when(subscriptionRepository.save(
                                        any(com.videoflix.subscriptions_microservice.entities.Subscription.class)))
                                        .thenReturn(subscription);

                        // WHEN : Appel de la méthode refundSubscription.
                        com.videoflix.subscriptions_microservice.entities.Subscription actualSubscription = stripePaymentService
                                        .refundSubscription(subscription, amount, reason);

                        // THEN : Vérification que Refund.create a été appelé avec les bons paramètres.
                        ArgumentCaptor<RefundCreateParams> paramsCaptor = ArgumentCaptor
                                        .forClass(RefundCreateParams.class);
                        mockedRefund.verify(() -> Refund.create(paramsCaptor.capture()), times(1));
                        assertEquals(subscription.getPaymentId(), paramsCaptor.getValue().getPaymentIntent());
                        assertEquals((long) (amount * 100), paramsCaptor.getValue().getAmount());
                        assertEquals(RefundCreateParams.Reason.valueOf(reason), paramsCaptor.getValue().getReason());
                        // Vérification que la date de remboursement a été mise à jour dans
                        // l'abonnement.
                        assertNotNull(actualSubscription.getRefundDate());
                        // Vérification que subscriptionRepository.save a été appelé.
                        verify(subscriptionRepository, times(1)).save(subscription);
                }
        }

        // Test pour vérifier la création d'un abonnement Stripe avec renouvellement
        // automatique.
        @Test
        void createStripeSubscriptionWithAutoRenew_shouldCreateSubscriptionAndPaymentOnSuccess() {
                // GIVEN : Un objet Subscription local.
                com.videoflix.subscriptions_microservice.entities.Subscription subscription = new com.videoflix.subscriptions_microservice.entities.Subscription();
                subscription.setCustomerId("cus_AUTO_SUCCESS");
                subscription.setPriceId("price_AUTO");
                String stripeSubscriptionId = "sub_AUTO";

                // WHEN & THEN (avec Mockito.mockStatic pour les méthodes statiques de Stripe):
                try (MockedStatic<com.stripe.model.Subscription> mockedStripeSubscription = Mockito
                                .mockStatic(com.stripe.model.Subscription.class)) {
                        com.stripe.model.Subscription mockStripeSub = mock(com.stripe.model.Subscription.class);
                        when(mockStripeSub.getId()).thenReturn(stripeSubscriptionId);

                        mockedStripeSubscription
                                        .when(() -> com.stripe.model.Subscription
                                                        .create(any(SubscriptionCreateParams.class)))
                                        .thenReturn(mockStripeSub);
                        when(subscriptionRepository.save(
                                        any(com.videoflix.subscriptions_microservice.entities.Subscription.class)))
                                        .thenReturn(subscription);
                        when(paymentRepository.save(any(Payment.class))).thenReturn(new Payment());

                        com.videoflix.subscriptions_microservice.entities.Subscription actualSubscription = stripePaymentService
                                        .createStripeSubscriptionWithAutoRenew(subscription);

                        // THEN: Vérifications des interactions et des résultats.

                        // Capture les paramètres passés à la méthode statique `Subscription.create()`.
                        ArgumentCaptor<SubscriptionCreateParams> paramsCaptor = ArgumentCaptor
                                        .forClass(SubscriptionCreateParams.class);
                        // Vérifie que `Subscription.create()` a été appelé exactement une fois avec les
                        // paramètres capturés.
                        mockedStripeSubscription.verify(
                                        () -> com.stripe.model.Subscription.create(paramsCaptor.capture()),
                                        times(1));

                        // Vérifie que les paramètres passés à Stripe étaient corrects.
                        assertEquals(subscription.getCustomerId(), paramsCaptor.getValue().getCustomer(),
                                        "Le Customer ID passé à Stripe doit correspondre à celui de l'abonnement.");
                        assertEquals(subscription.getPriceId(), paramsCaptor.getValue().getItems().get(0).getPrice(),
                                        "Le Price ID passé à Stripe doit correspondre à celui de l'abonnement.");
                        assertEquals(SubscriptionCreateParams.CollectionMethod.CHARGE_AUTOMATICALLY,
                                        paramsCaptor.getValue().getCollectionMethod(),
                                        "La méthode de collection doit être CHARGE_AUTOMATICALLY pour le renouvellement automatique.");
                        assertNotNull(paramsCaptor.getValue().getBillingCycleAnchor(),
                                        "Le Billing Cycle Anchor doit être défini pour le renouvellement automatique.");

                        // Vérifie que l'ID d'abonnement Stripe a été correctement enregistré dans
                        // l'entité locale.
                        assertEquals(stripeSubscriptionId, actualSubscription.getStripeSubscriptionId(),
                                        "L'ID Stripe de l'abonnement local doit correspondre à l'ID retourné par Stripe.");

                        // Vérifie que la méthode `save()` du `subscriptionRepository` a été appelée
                        // exactement une fois avec l'objet abonnement.
                        verify(subscriptionRepository, times(1))
                                        .save(any(com.videoflix.subscriptions_microservice.entities.Subscription.class));

                        // Capture l'objet Payment qui a été sauvegardé.
                        ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);
                        verify(paymentRepository, times(1)).save(paymentCaptor.capture());

                        // Vérifie les propriétés du `Payment` sauvegardé.
                        assertEquals(Payment.PaymentStatus.SUCCESS, paymentCaptor.getValue().getStatus(),
                                        "Le statut du paiement doit être SUCCÈS.");
                        assertEquals(stripeSubscriptionId, paymentCaptor.getValue().getPaymentId(),
                                        "L'ID de paiement doit être l'ID de l'abonnement Stripe.");
                        assertEquals(subscription, paymentCaptor.getValue().getSubscription(),
                                        "Le paiement doit être associé à l'abonnement correct.");
                        assertEquals(10.0, paymentCaptor.getValue().getAmount(),
                                        "Le montant du paiement doit être de 10.0 (selon la logique du service).");
                        assertNotNull(paymentCaptor.getValue().getPaymentDate(),
                                        "La date de paiement ne doit pas être nulle.");
                }
        }

        // Test pour vérifier la gestion d'une exception lors de la création d'un
        // abonnement avec renouvellement automatique.
        @Test
        void createStripeSubscriptionWithAutoRenew_shouldHandleStripeExceptionAndSaveError() throws StripeException {
                // GIVEN : Un objet Subscription local et un message d'erreur.
                com.videoflix.subscriptions_microservice.entities.Subscription subscription = new com.videoflix.subscriptions_microservice.entities.Subscription();
                subscription.setCustomerId("cus_AUTO_RENEW_FAIL");
                subscription.setPriceId("price_AUTO_RENEW_FAIL"); // Correction ici
                String errorMessage = "Failed to create auto-renew subscription";

                // Mock de la classe com.stripe.model.Subscription pour simuler une exception.
                try (MockedStatic<com.stripe.model.Subscription> mockedStripeSubscription = Mockito
                                .mockStatic(com.stripe.model.Subscription.class)) {
                        when(com.stripe.model.Subscription.create(any(SubscriptionCreateParams.class)))
                                        .thenThrow(new ApiException(errorMessage, errorMessage, errorMessage, null,
                                                        null));
                        when(subscriptionRepository.save(
                                        any(com.videoflix.subscriptions_microservice.entities.Subscription.class)))
                                        .thenReturn(subscription);
                        when(paymentRepository.save(any(Payment.class))).thenReturn(new Payment());

                        // WHEN : Appel de la méthode createStripeSubscriptionWithAutoRenew qui devrait
                        // lancer une exception.
                        com.videoflix.subscriptions_microservice.entities.Subscription actualSubscription = stripePaymentService
                                        .createStripeSubscriptionWithAutoRenew(subscription);

                        // THEN : Vérification que l'erreur a été enregistrée dans l'abonnement.
                        assertEquals(errorMessage, actualSubscription.getLastPaymentError());
                        // Vérification que subscriptionRepository.save a été appelé.
                        verify(subscriptionRepository, times(1)).save(subscription);
                        // Vérification que paymentRepository.save a été appelé avec un Payment en
                        // échec.
                        ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);
                        verify(paymentRepository, times(1)).save(paymentCaptor.capture());
                        assertEquals(Payment.PaymentStatus.FAILED, paymentCaptor.getValue().getStatus());
                        assertEquals(errorMessage, paymentCaptor.getValue().getErrorMessage());
                        assertEquals(subscription, paymentCaptor.getValue().getSubscription());
                }
        }

        // Test pour vérifier la création d'un abonnement Stripe avec essai gratuit.
        @Test
        void createStripeSubscriptionWithTrial_shouldCreateSubscriptionAndPaymentOnSuccess() throws StripeException {

                // GIVEN : Un objet Subscription local et une durée d'essai.
                com.videoflix.subscriptions_microservice.entities.Subscription subscription = new com.videoflix.subscriptions_microservice.entities.Subscription();
                subscription.setCustomerId("cus_TRIAL");
                subscription.setPriceId("price_TRIAL"); // Correction ici
                long trialPeriodDays = 14L;
                String stripeSubscriptionId = "sub_TRIAL";

                // Mock de la classe com.stripe.model.Subscription.
                try (MockedStatic<com.stripe.model.Subscription> mockedStripeSubscription = Mockito
                                .mockStatic(com.stripe.model.Subscription.class)) {
                        com.stripe.model.Subscription mockStripeSub = mock(com.stripe.model.Subscription.class);
                        when(mockStripeSub.getId()).thenReturn(stripeSubscriptionId);
                        when(com.stripe.model.Subscription.create(any(SubscriptionCreateParams.class)))
                                        .thenReturn(mockStripeSub);
                        when(subscriptionRepository.save(
                                        any(com.videoflix.subscriptions_microservice.entities.Subscription.class)))
                                        .thenReturn(subscription);
                        when(paymentRepository.save(any(Payment.class))).thenReturn(new Payment());

                        // WHEN : Appel de la méthode createStripeSubscriptionWithTrial.
                        com.videoflix.subscriptions_microservice.entities.Subscription actualSubscription = stripePaymentService
                                        .createStripeSubscriptionWithTrial(subscription,
                                                        trialPeriodDays);

                        // THEN : Vérification que StripeSubscription.create a été appelé avec les bons
                        // paramètres pour l'essai gratuit.
                        ArgumentCaptor<SubscriptionCreateParams> paramsCaptor = ArgumentCaptor
                                        .forClass(SubscriptionCreateParams.class);
                        mockedStripeSubscription.verify(
                                        () -> com.stripe.model.Subscription.create(paramsCaptor.capture()),
                                        times(1));
                        assertEquals(subscription.getCustomerId(), paramsCaptor.getValue().getCustomer());
                        assertEquals(subscription.getPriceId(), paramsCaptor.getValue().getItems().get(0).getPrice());
                        assertEquals(trialPeriodDays, paramsCaptor.getValue().getTrialPeriodDays());

                        // Vérification que l'ID d'abonnement Stripe et les dates d'essai ont été mis à
                        // jour.
                        assertEquals(stripeSubscriptionId, actualSubscription.getStripeSubscriptionId(),
                                        "L'ID Stripe de l'abonnement local doit correspondre.");
                        assertNotNull(actualSubscription.getTrialStartDate(),
                                        "La date de début d'essai doit être définie.");
                        assertNotNull(actualSubscription.getTrialEndDate(),
                                        "La date de fin d'essai doit être définie.");

                        // Vérification que subscriptionRepository.save a été appelé.
                        verify(subscriptionRepository, times(1)).save(subscription);

                        // Vérification que paymentRepository.save a été appelé avec un Payment réussi
                        // (montant nul pour l'essai).
                        ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);
                        verify(paymentRepository, times(1)).save(paymentCaptor.capture());
                        assertEquals(Payment.PaymentStatus.SUCCESS, paymentCaptor.getValue().getStatus(),
                                        "Le statut du paiement doit être SUCCÈS.");
                        assertEquals(stripeSubscriptionId, paymentCaptor.getValue().getPaymentId(),
                                        "L'ID de paiement doit être l'ID de l'abonnement Stripe.");
                        assertEquals(0.0, paymentCaptor.getValue().getAmount(),
                                        "Le montant du paiement doit être de 0.0 pour un essai gratuit.");
                        assertEquals(subscription, paymentCaptor.getValue().getSubscription(),
                                        "Le paiement doit être associé à l'abonnement correct.");
                        assertNotNull(paymentCaptor.getValue().getPaymentDate(),
                                        "La date de paiement ne doit pas être nulle.");
                }
        }

        // Test pour vérifier la gestion d'une exception lors de la création d'un
        // abonnement avec essai gratuit.
        @Test
        void createStripeSubscriptionWithTrial_shouldHandleStripeExceptionAndSaveError() throws StripeException {
                // GIVEN : Un objet Subscription local, une durée d'essai et un message
                // d'erreur.
                com.videoflix.subscriptions_microservice.entities.Subscription subscription = new com.videoflix.subscriptions_microservice.entities.Subscription();
                subscription.setCustomerId("cus_TRIAL_FAIL");
                subscription.setPriceId("price_TRIAL_FAIL"); // Correction ici
                long trialPeriodDays = 7L;
                String errorMessage = "Failed to create trial subscription";

                // Mock de la classe com.stripe.model.Subscription pour simuler une exception.
                try (MockedStatic<com.stripe.model.Subscription> mockedStripeSubscription = Mockito
                                .mockStatic(com.stripe.model.Subscription.class)) {
                        when(com.stripe.model.Subscription.create(any(SubscriptionCreateParams.class)))
                                        .thenThrow(new ApiException(errorMessage, errorMessage, errorMessage, null,
                                                        null));
                        when(subscriptionRepository.save(
                                        any(com.videoflix.subscriptions_microservice.entities.Subscription.class)))
                                        .thenReturn(subscription);
                        when(paymentRepository.save(any(Payment.class))).thenReturn(new Payment());

                        // WHEN : Appel de la méthode createStripeSubscriptionWithTrial qui devrait
                        // lancer une exception.
                        com.videoflix.subscriptions_microservice.entities.Subscription actualSubscription = stripePaymentService
                                        .createStripeSubscriptionWithTrial(subscription,
                                                        trialPeriodDays);

                        // THEN : Vérification que l'erreur a été enregistrée dans l'abonnement.
                        assertEquals(errorMessage, actualSubscription.getLastPaymentError(),
                                        "L'erreur de paiement doit être enregistrée.");
                        // Vérification que subscriptionRepository.save a été appelé.
                        verify(subscriptionRepository, times(1)).save(subscription);
                        // Vérification que paymentRepository.save a été appelé avec un Payment en
                        // échec.
                        ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);
                        verify(paymentRepository, times(1)).save(paymentCaptor.capture());
                        assertEquals(Payment.PaymentStatus.FAILED, paymentCaptor.getValue().getStatus(),
                                        "Le statut du paiement doit être ÉCHEC.");
                        assertEquals(errorMessage, paymentCaptor.getValue().getErrorMessage(),
                                        "Le message d'erreur du paiement doit correspondre.");
                        assertEquals(subscription, paymentCaptor.getValue().getSubscription(),
                                        "Le paiement doit être associé à l'abonnement correct.");
                        assertNull(actualSubscription.getTrialStartDate(),
                                        "La date de début d'essai ne doit pas être définie en cas d'échec.");
                        assertNull(actualSubscription.getTrialEndDate(),
                                        "La date de fin d'essai ne doit pas être définie en cas d'échec.");
                }
        }
}