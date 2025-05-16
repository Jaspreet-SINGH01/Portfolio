package com.videoflix.subscriptions_microservice.services;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.Refund;
import com.stripe.model.Subscription;
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

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

// @ExtendWith(MockitoExtension.class) permet d'utiliser les fonctionnalités de Mockito dans ce test.
@ExtendWith(MockitoExtension.class)
class StripePaymentServiceTest {

    // Clé secrète Stripe de test
    private static final String TEST_SECRET_KEY = "sk_test_xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx";

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
            ArgumentCaptor<CustomerCreateParams> paramsCaptor = ArgumentCaptor.forClass(CustomerCreateParams.class);
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
        Subscription subscription = new Subscription();
        subscription.setCustomer("cus_TEST");
        subscription.setId("price_TEST");
        String stripeSubscriptionId = "sub_TEST";

        // Mock de la classe com.stripe.model.Subscription.
        try (MockedStatic<StripeSubscription> mockedStripeSubscription = Mockito.mockStatic(StripeSubscription.class)) {
            StripeSubscription mockStripeSub = mock(StripeSubscription.class);
            when(mockStripeSub.getId()).thenReturn(stripeSubscriptionId);
            when(StripeSubscription.create(any(SubscriptionCreateParams.class))).thenReturn(mockStripeSub);
            when(subscriptionRepository.save(any(Subscription.class))).thenReturn(subscription);
            when(paymentRepository.save(any(Payment.class))).thenReturn(new Payment());

            // WHEN : Appel de la méthode createStripeSubscription.
            Subscription actualSubscription = stripePaymentService.createStripeSubscription(subscription);

            // THEN : Vérification que StripeSubscription.create a été appelé avec les bons
            // paramètres.
            ArgumentCaptor<SubscriptionCreateParams> paramsCaptor = ArgumentCaptor
                    .forClass(SubscriptionCreateParams.class);
            mockedStripeSubscription.verify(() -> StripeSubscription.create(paramsCaptor.capture()), times(1));
            assertEquals(subscription.getCustomer(), paramsCaptor.getValue().getCustomer());
            assertEquals(subscription.getId(), paramsCaptor.getValue().getItems().get(0).getPrice());
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
        Subscription subscription = new Subscription();
        subscription.setCustomer("cus_TEST");
        subscription.setId("price_TEST");
        String errorMessage = "Stripe error occurred";

        // Mock de la classe com.stripe.model.Subscription pour simuler une exception.
        try (MockedStatic<StripeSubscription> mockedStripeSubscription = Mockito.mockStatic(StripeSubscription.class)) {
            when(StripeSubscription.create(any(SubscriptionCreateParams.class)))
                    .thenThrow(new StripeException(errorMessage));
            when(subscriptionRepository.save(any(Subscription.class))).thenReturn(subscription);
            when(paymentRepository.save(any(Payment.class))).thenReturn(new Payment());

            // WHEN : Appel de la méthode createStripeSubscription qui devrait lancer une
            // exception.
            Subscription actualSubscription = stripePaymentService.createStripeSubscription(subscription);

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
        Subscription subscription = new Subscription();
        subscription.setPaymentId("pi_TEST");
        double amount = 5.0;
        String reason = "requested_by_customer";

        // Mock de la classe Refund de Stripe.
        try (MockedStatic<Refund> mockedRefund = Mockito.mockStatic(Refund.class)) {
            Refund mockRefund = mock(Refund.class);
            when(Refund.create(any(RefundCreateParams.class))).thenReturn(mockRefund);
            when(subscriptionRepository.save(any(Subscription.class))).thenReturn(subscription);

            // WHEN : Appel de la méthode refundSubscription.
            Subscription actualSubscription = stripePaymentService.refundSubscription(subscription, amount, reason);

            // THEN : Vérification que Refund.create a été appelé avec les bons paramètres.
            ArgumentCaptor<RefundCreateParams> paramsCaptor = ArgumentCaptor.forClass(RefundCreateParams.class);
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
    void createStripeSubscriptionWithAutoRenew_shouldCreateSubscriptionAndPaymentOnSuccess() throws StripeException {
        // GIVEN : Un objet Subscription local.
        Subscription subscription = new Subscription();
        subscription.setCustomerId("cus_AUTO_RENEW");
        subscription.setPriceId("price_AUTO_RENEW");
        String stripeSubscriptionId = "sub_AUTO_RENEW";

        // Mock de la classe com.stripe.model.Subscription.
        try (MockedStatic<StripeSubscription> mockedStripeSubscription = Mockito.mockStatic(StripeSubscription.class)) {
            StripeSubscription mockStripeSub = mock(StripeSubscription.class);
            when(mockStripeSub.getId()).thenReturn(stripeSubscriptionId);
            when(StripeSubscription.create(any(SubscriptionCreateParams.class))).thenReturn(mockStripeSub);
            when(subscriptionRepository.save(any(Subscription.class))).thenReturn(subscription);
            when(paymentRepository.save(any(Payment.class))).thenReturn(new Payment());

            // WHEN : Appel de la méthode createStripeSubscriptionWithAutoRenew.
            Subscription actualSubscription = stripePaymentService.createStripeSubscriptionWithAutoRenew(subscription);

            // THEN : Vérification que StripeSubscription.create a été appelé avec les bons
            // paramètres pour le renouvellement automatique.
            ArgumentCaptor<SubscriptionCreateParams> paramsCaptor = ArgumentCaptor
                    .forClass(SubscriptionCreateParams.class);
            mockedStripeSubscription.verify(() -> StripeSubscription.create(paramsCaptor.capture()), times(1));
            assertEquals(subscription.getCustomer(), paramsCaptor.getValue().getCustomer());
            assertEquals(subscription.getId(), paramsCaptor.getValue().getItems().get(0).getPrice());
            assertEquals(SubscriptionCreateParams.CollectionMethod.CHARGE_AUTOMATICALLY,
                    paramsCaptor.getValue().getCollectionMethod());
            assertNotNull(paramsCaptor.getValue().getBillingCycleAnchor());
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

    // Test pour vérifier la création d'un abonnement Stripe avec essai gratuit.
    @Test
    void createStripeSubscriptionWithTrial_shouldCreateSubscriptionAndPaymentOnSuccess() throws StripeException {

        // GIVEN : Un objet Subscription local et une durée d'essai.
        Subscription subscription = new Subscription();
        subscription.setCustomer("cus_TRIAL");
        subscription.setId("price_TRIAL");
        long trialPeriodDays = 14L;
        String stripeSubscriptionId = "sub_TRIAL";

        // Mock de la classe com.stripe.model.Subscription.
        try (MockedStatic<StripeSubscription> mockedStripeSubscription = Mockito.mockStatic(StripeSubscription.class)) {
            StripeSubscription mockStripeSub = mock(StripeSubscription.class);
            when(mockStripeSub.getId()).thenReturn(stripeSubscriptionId);
            when(StripeSubscription.create(any(SubscriptionCreateParams.class))).thenReturn(mockStripeSub);
            when(subscriptionRepository.save(any(Subscription.class))).thenReturn(subscription);
            when(paymentRepository.save(any(Payment.class))).thenReturn(new Payment());

            // WHEN : Appel de la méthode createStripeSubscriptionWithTrial.
            Subscription actualSubscription = stripePaymentService.createStripeSubscriptionWithTrial(subscription,
                    trialPeriodDays);

            // THEN : Vérification que StripeSubscription.create a été appelé avec les bons
            // paramètres pour l'essai gratuit.
            ArgumentCaptor<SubscriptionCreateParams> paramsCaptor = ArgumentCaptor
                    .forClass(SubscriptionCreateParams.class);
            mockedStripeSubscription.verify(() -> StripeSubscription.create(paramsCaptor.capture()), times(1));
            assertEquals(subscription.getCustomer(), paramsCaptor.getValue().getCustomer());
            assertEquals(subscription.getId(), paramsCaptor.getValue().getItems().get(0).getPrice());
            assertEquals(trialPeriodDays, paramsCaptor.getValue().getTrialPeriodDays());

            // Vérification que l'ID d'abonnement Stripe et les dates d'essai ont été mis à
            // jour.
            assertEquals(stripeSubscriptionId, actualSubscription.getStripeSubscriptionId());
            assertNotNull(actualSubscription.getTrialStartDate());
            assertNotNull(actualSubscription.getTrialEndDate());

            // Vérification que subscriptionRepository.save a été appelé.
            verify(subscriptionRepository, times(1)).save(subscription);

            // Vérification que paymentRepository.save a été appelé avec un Payment réussi
            // (montant nul pour l'essai).
            ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);
            verify(paymentRepository, times(1)).save(paymentCaptor.capture());
            assertEquals(Payment.PaymentStatus.SUCCESS, paymentCaptor.getValue().getStatus());
            assertEquals(stripeSubscriptionId, paymentCaptor.getValue().getPaymentId());
            assertEquals(0.0, paymentCaptor.getValue().getAmount());
            assertEquals(subscription, paymentCaptor.getValue().getSubscription());
        }
    }

    // Test pour vérifier la gestion d'une exception lors de la création d'un
    // abonnement avec renouvellement automatique.
    @Test
    void createStripeSubscriptionWithAutoRenew_shouldHandleStripeExceptionAndSaveError() throws StripeException {
        // GIVEN : Un objet Subscription local et un message d'erreur.
        Subscription subscription = new Subscription();
        subscription.setCustomer("cus_AUTO_RENEW_FAIL");
        subscription.setId("price_AUTO_RENEW_FAIL");
        String errorMessage = "Failed to create auto-renew subscription";

        // Mock de la classe com.stripe.model.Subscription pour simuler une exception.
        try (MockedStatic<StripeSubscription> mockedStripeSubscription = Mockito.mockStatic(StripeSubscription.class)) {
            when(StripeSubscription.create(any(SubscriptionCreateParams.class)))
                    .thenThrow(new StripeException(errorMessage));
            when(subscriptionRepository.save(any(Subscription.class))).thenReturn(subscription);
            when(paymentRepository.save(any(Payment.class))).thenReturn(new Payment());

            // WHEN : Appel de la méthode createStripeSubscriptionWithAutoRenew qui devrait
            // lancer une exception.
            Subscription actualSubscription = stripePaymentService.createStripeSubscriptionWithAutoRenew(subscription);

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

    // Test pour vérifier la gestion d'une exception lors de la création d'un
    // abonnement avec essai gratuit.
    @Test
    void createStripeSubscriptionWithTrial_shouldHandleStripeExceptionAndSaveError() throws StripeException {
        // GIVEN : Un objet Subscription local, une durée d'essai et un message
        // d'erreur.
        Subscription subscription = new Subscription();
        subscription.setCustomerId("cus_TRIAL_FAIL");
        subscription.setPriceId("price_TRIAL_FAIL");
        long trialPeriodDays = 7L;
        String errorMessage = "Failed to create trial subscription";

        // Mock de la classe com.stripe.model.Subscription pour simuler une exception.
        try (MockedStatic<StripeSubscription> mockedStripeSubscription = Mockito.mockStatic(StripeSubscription.class)) {
            when(StripeSubscription.create(any(SubscriptionCreateParams.class)))
                    .thenThrow(new StripeException(errorMessage));
            when(subscriptionRepository.save(any(Subscription.class))).thenReturn(subscription);
            when(paymentRepository.save(any(Payment.class))).thenReturn(new Payment());

            // WHEN : Appel de la méthode createStripeSubscriptionWithTrial qui devrait
            // lancer une exception.
            Subscription actualSubscription = stripePaymentService.createStripeSubscriptionWithTrial(subscription,
                    trialPeriodDays);

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
}