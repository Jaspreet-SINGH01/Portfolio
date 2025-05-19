package com.videoflix.subscriptions_microservice.services;

import com.stripe.Stripe;
import com.stripe.exception.AuthenticationException;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StripePaymentServiceTest {

    private static final String TEST_SECRET_KEY = "sk_test_xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx";

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private StripePaymentService stripePaymentService;

    @SuppressWarnings("unchecked")
    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(stripePaymentService, "secretKey", TEST_SECRET_KEY);
        Stripe.apiKey = TEST_SECRET_KEY;
        reset(subscriptionRepository, paymentRepository);
    }

    @Test
    void createStripeCustomer_shouldCreateCustomerAndReturnId() throws StripeException {
        String email = "test@example.com";
        String name = "Test User";
        String customerId = "cus_TEST";

        try (MockedStatic<Customer> mockedCustomer = Mockito.mockStatic(Customer.class)) {
            Customer mockCustomer = mock(Customer.class);
            when(mockCustomer.getId()).thenReturn(customerId);
            mockedCustomer.when(() -> Customer.create(any(CustomerCreateParams.class))).thenReturn(mockCustomer);

            String actualCustomerId = stripePaymentService.createStripeCustomer(email, name);

            ArgumentCaptor<CustomerCreateParams> paramsCaptor = ArgumentCaptor.forClass(CustomerCreateParams.class);
            mockedCustomer.verify(() -> Customer.create(paramsCaptor.capture()), times(1));

            assertEquals(email, paramsCaptor.getValue().getEmail());
            assertEquals(name, paramsCaptor.getValue().getName());
            assertEquals(customerId, actualCustomerId);
        }
    }

    @Test
    void createStripeSubscription_shouldCreateSubscriptionAndPaymentOnSuccess() throws StripeException {
        com.videoflix.subscriptions_microservice.entities.Subscription subscription = new com.videoflix.subscriptions_microservice.entities.Subscription();
        subscription.setCustomerId("cus_TEST");
        subscription.setPriceId("price_TEST");
        String stripeSubscriptionId = "sub_TEST";

        try (MockedStatic<Subscription> mockedStripeSubscription = Mockito.mockStatic(Subscription.class)) {
            Subscription mockStripeSub = mock(Subscription.class);
            when(mockStripeSub.getId()).thenReturn(stripeSubscriptionId);
            mockedStripeSubscription.when(() -> Subscription.create(any(SubscriptionCreateParams.class))).thenReturn(mockStripeSub);

            when(subscriptionRepository.save(any())).thenReturn(subscription);
            when(paymentRepository.save(any())).thenReturn(new Payment());

            com.videoflix.subscriptions_microservice.entities.Subscription actualSubscription =
                    stripePaymentService.createStripeSubscription(subscription);

            ArgumentCaptor<SubscriptionCreateParams> paramsCaptor = ArgumentCaptor.forClass(SubscriptionCreateParams.class);
            mockedStripeSubscription.verify(() -> Subscription.create(paramsCaptor.capture()), times(1));

            assertEquals(subscription.getCustomerId(), paramsCaptor.getValue().getCustomer());
            assertEquals(subscription.getPriceId(), paramsCaptor.getValue().getItems().get(0).getPrice());
            assertEquals(stripeSubscriptionId, actualSubscription.getStripeSubscriptionId());

            verify(subscriptionRepository).save(subscription);

            ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);
            verify(paymentRepository).save(paymentCaptor.capture());

            assertEquals(Payment.PaymentStatus.SUCCESS, paymentCaptor.getValue().getStatus());
            assertEquals(stripeSubscriptionId, paymentCaptor.getValue().getPaymentId());
            assertEquals(subscription, paymentCaptor.getValue().getSubscription());
        }
    }

    @Test
    void createStripeSubscription_shouldHandleStripeExceptionAndSaveError() throws StripeException {
        com.videoflix.subscriptions_microservice.entities.Subscription subscription = new com.videoflix.subscriptions_microservice.entities.Subscription();
        subscription.setCustomerId("cus_TEST");
        subscription.setPriceId("price_TEST");
        String errorMessage = "Stripe error occurred";

        try (MockedStatic<Subscription> mockedStripeSubscription = Mockito.mockStatic(Subscription.class)) {
            mockedStripeSubscription.when(() -> Subscription.create(any(SubscriptionCreateParams.class)))
                    .thenThrow(new AuthenticationException(errorMessage, errorMessage, errorMessage, null, null));

            when(subscriptionRepository.save(any())).thenReturn(subscription);
            when(paymentRepository.save(any())).thenReturn(new Payment());

            com.videoflix.subscriptions_microservice.entities.Subscription actualSubscription =
                    stripePaymentService.createStripeSubscription(subscription);

            assertEquals(errorMessage, actualSubscription.getLastPaymentError());
            verify(subscriptionRepository).save(subscription);

            ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);
            verify(paymentRepository).save(paymentCaptor.capture());

            assertEquals(Payment.PaymentStatus.FAILED, paymentCaptor.getValue().getStatus());
            assertEquals(errorMessage, paymentCaptor.getValue().getErrorMessage());
        }
    }

    @Test
    void refundSubscription_shouldCreateRefundAndUpdateSubscription() throws StripeException {
        com.videoflix.subscriptions_microservice.entities.Subscription subscription = new com.videoflix.subscriptions_microservice.entities.Subscription();
        subscription.setPaymentId("pi_TEST");
        double amount = 5.0;
        String reason = "requested_by_customer";

        try (MockedStatic<Refund> mockedRefund = Mockito.mockStatic(Refund.class)) {
            Refund mockRefund = mock(Refund.class);
            mockedRefund.when(() -> Refund.create(any(RefundCreateParams.class))).thenReturn(mockRefund);
            when(subscriptionRepository.save(any())).thenReturn(subscription);

            com.videoflix.subscriptions_microservice.entities.Subscription actualSubscription =
                    stripePaymentService.refundSubscription(subscription, amount, reason);

            ArgumentCaptor<RefundCreateParams> paramsCaptor = ArgumentCaptor.forClass(RefundCreateParams.class);
            mockedRefund.verify(() -> Refund.create(paramsCaptor.capture()), times(1));

            assertEquals(subscription.getPaymentId(), paramsCaptor.getValue().getPaymentIntent());
            assertEquals((long) (amount * 100), paramsCaptor.getValue().getAmount());
            assertEquals(RefundCreateParams.Reason.valueOf(reason.toUpperCase()), paramsCaptor.getValue().getReason());

            assertNotNull(actualSubscription.getRefundDate());
            verify(subscriptionRepository).save(subscription);
        }
    }

// Méthode utilitaire pour capturer et vérifier un paiement sauvegardé
private void verifySavedPayment(Payment.PaymentStatus expectedStatus, String expectedPaymentId,
                                String expectedErrorMessage, Subscription expectedSubscription, double expectedAmount) {
    ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);
    verify(paymentRepository, times(1)).save(paymentCaptor.capture());
    Payment savedPayment = paymentCaptor.getValue();
    assertEquals(expectedStatus, savedPayment.getStatus());
    assertEquals(expectedPaymentId, savedPayment.getPaymentId());
    assertEquals(expectedSubscription, savedPayment.getSubscription());
    if (expectedErrorMessage != null) {
        assertEquals(expectedErrorMessage, savedPayment.getErrorMessage());
    }
    assertEquals(expectedAmount, savedPayment.getAmount());
}

// createStripeSubscriptionWithAutoRenew - succès
@Test
void createStripeSubscriptionWithAutoRenew_shouldCreateSubscriptionAndPaymentOnSuccess() throws StripeException {
    Subscription subscription = new Subscription();
    subscription.setCustomer("cus_AUTO_SUCCESS");
    subscription.setId("price_AUTO");
    String stripeSubscriptionId = "sub_AUTO";

    try (MockedStatic<Subscription> mockedStripeSubscription = Mockito.mockStatic(Subscription.class)) {
        Subscription mockStripeSub = mock(Subscription.class);
        when(mockStripeSub.getId()).thenReturn(stripeSubscriptionId);
        mockedStripeSubscription.when(() -> Subscription.create(any(SubscriptionCreateParams.class)))
                .thenReturn(mockStripeSub);

        when(subscriptionRepository.save(any(Subscription.class))).thenReturn(subscription);
        when(paymentRepository.save(any(Payment.class))).thenReturn(new Payment());

        Subscription actual = stripePaymentService.createStripeSubscriptionWithAutoRenew(subscription);

        ArgumentCaptor<SubscriptionCreateParams> paramsCaptor = ArgumentCaptor.forClass(SubscriptionCreateParams.class);
        mockedStripeSubscription.verify(() -> Subscription.create(paramsCaptor.capture()), times(1));
        assertEquals(subscription.getCustomer(), paramsCaptor.getValue().getCustomer());
        assertEquals(subscription.getId(), paramsCaptor.getValue().getItems().get(0).getPrice());
        assertEquals(SubscriptionCreateParams.CollectionMethod.CHARGE_AUTOMATICALLY, paramsCaptor.getValue().getCollectionMethod());

        assertEquals(stripeSubscriptionId, actual.getStripeSubscriptionId());
        verify(subscriptionRepository, times(1)).save(subscription);
        verifySavedPayment(Payment.PaymentStatus.SUCCESS, stripeSubscriptionId, null, subscription, 0.0);
    }
}

// createStripeSubscriptionWithTrial - succès
@Test
void createStripeSubscriptionWithTrial_shouldCreateSubscriptionAndPaymentOnSuccess() throws StripeException {
    Subscription subscription = new Subscription();
    subscription.setCustomer("cus_TRIAL");
    subscription.setId("price_TRIAL");
    long trialPeriodDays = 7L;
    String stripeSubscriptionId = "sub_TRIAL";

    try (MockedStatic<Subscription> mockedStripeSubscription = Mockito.mockStatic(Subscription.class)) {
        Subscription mockStripeSub = mock(Subscription.class);
        when(mockStripeSub.getId()).thenReturn(stripeSubscriptionId);
        mockedStripeSubscription.when(() -> Subscription.create(any(SubscriptionCreateParams.class)))
                .thenReturn(mockStripeSub);

        when(subscriptionRepository.save(any(Subscription.class))).thenReturn(subscription);
        when(paymentRepository.save(any(Payment.class))).thenReturn(new Payment());

        Subscription actual = stripePaymentService.createStripeSubscriptionWithTrial(subscription, trialPeriodDays);

        ArgumentCaptor<SubscriptionCreateParams> paramsCaptor = ArgumentCaptor.forClass(SubscriptionCreateParams.class);
        mockedStripeSubscription.verify(() -> Subscription.create(paramsCaptor.capture()), times(1));
        assertEquals(subscription.getCustomer(), paramsCaptor.getValue().getCustomer());
        assertEquals(subscription.getId(), paramsCaptor.getValue().getItems().get(0).getPrice());
        assertEquals(trialPeriodDays, paramsCaptor.getValue().getTrialPeriodDays());

        assertEquals(stripeSubscriptionId, actual.getStripeSubscriptionId());
        assertNotNull(actual.getTrialStart());
        assertNotNull(actual.getTrialEnd());

        verify(subscriptionRepository, times(1)).save(subscription);
        verifySavedPayment(Payment.PaymentStatus.SUCCESS, stripeSubscriptionId, null, subscription, 0.0);
    }
}

// createStripeSubscriptionWithAutoRenew - échec
@Test
void createStripeSubscriptionWithAutoRenew_shouldHandleStripeExceptionAndSaveError() throws StripeException {
    Subscription subscription = new Subscription();
    subscription.setCustomer("cus_FAIL");
    subscription.setId("price_FAIL");
    String errorMessage = "Stripe auto-renew error";

    try (MockedStatic<Subscription> mockedStripeSubscription = Mockito.mockStatic(Subscription.class)) {
        mockedStripeSubscription.when(() -> Subscription.create(any(SubscriptionCreateParams.class)))
                .thenThrow(new StripeException(errorMessage, null, null, 400, null, null));

        when(subscriptionRepository.save(any(Subscription.class))).thenReturn(subscription);
        when(paymentRepository.save(any(Payment.class))).thenReturn(new Payment());

        Subscription actual = stripePaymentService.createStripeSubscriptionWithAutoRenew(subscription);

        assertEquals(errorMessage, actual.getLastPriceMigrationError());
        verify(subscriptionRepository, times(1)).save(subscription);
        verifySavedPayment(Payment.PaymentStatus.FAILED, null, errorMessage, subscription, 0.0);
    }
}

// createStripeSubscriptionWithTrial - échec
@Test
void createStripeSubscriptionWithTrial_shouldHandleStripeExceptionAndSaveError() throws StripeException {
    Subscription subscription = new Subscription();
    subscription.setCustomer("cus_TRIAL_FAIL");
    subscription.setId("price_TRIAL_FAIL");
    long trialDays = 10L;
    String errorMessage = "Stripe trial creation error";

    try (MockedStatic<Subscription> mockedStripeSubscription = Mockito.mockStatic(Subscription.class)) {
        mockedStripeSubscription.when(() -> Subscription.create(any(SubscriptionCreateParams.class)))
                .thenThrow(new StripeException(errorMessage, null, null, 400, null, null));

        when(subscriptionRepository.save(any())).thenReturn(subscription);
        when(paymentRepository.save(any())).thenReturn(new Payment());

        Subscription actual = stripePaymentService.createStripeSubscriptionWithTrial(subscription, trialDays);

        assertEquals(errorMessage, actual.getLastPriceMigrationError());
        verify(subscriptionRepository, times(1)).save(null);
        verifySavedPayment(Payment.PaymentStatus.FAILED, null, errorMessage, subscription, 0.0);
    }
}

}
