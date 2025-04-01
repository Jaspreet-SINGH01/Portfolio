package com.videoflix.subscriptions_microservice.services;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.Refund;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.RefundCreateParams;
import com.stripe.param.SubscriptionCreateParams;
import com.videoflix.subscriptions_microservice.entities.Payment;
import com.videoflix.subscriptions_microservice.entities.Subscription;
import com.videoflix.subscriptions_microservice.repositories.PaymentRepository;
import com.videoflix.subscriptions_microservice.repositories.SubscriptionRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class StripePaymentService {

    private final SubscriptionRepository subscriptionRepository;
    private final PaymentRepository paymentRepository;

    public StripePaymentService(@Value("${stripe.api.secretKey}") String secretKey,
            SubscriptionRepository subscriptionRepository, PaymentRepository paymentRepository) {
        Stripe.apiKey = secretKey;
        this.subscriptionRepository = subscriptionRepository;
        this.paymentRepository = paymentRepository;
    }

    public String createStripeCustomer(String email, String name) throws StripeException {
        CustomerCreateParams createParams = new CustomerCreateParams.Builder()
                .setEmail(email)
                .setName(name)
                .build();
        Customer customer = Customer.create(createParams);
        return customer.getId();
    }

    public Subscription createStripeSubscription(Subscription subscription) throws StripeException {
        try {
            SubscriptionCreateParams createParams = new SubscriptionCreateParams.Builder()
                    .setCustomer(subscription.getCustomerId())
                    .addItem(SubscriptionCreateParams.Item.builder()
                            .setPrice(subscription.getPriceId())
                            .build())
                    .build();
            com.stripe.model.Subscription stripeSubscription = com.stripe.model.Subscription.create(createParams);
            subscription.setStripeSubscriptionId(stripeSubscription.getId());

            // Créer un enregistrement Payment
            Payment payment = new Payment();
            payment.setSubscription(subscription);
            payment.setPaymentDate(LocalDateTime.now());
            payment.setAmount(10.0); // Remplacez par le montant réel
            payment.setPaymentId(stripeSubscription.getId());
            payment.setStatus(Payment.PaymentStatus.SUCCESS);
            paymentRepository.save(payment);

            return subscriptionRepository.save(subscription);
        } catch (StripeException e) {
            subscription.setLastPaymentError(e.getMessage());

            // Créer un enregistrement Payment avec le statut FAILED
            Payment payment = new Payment();
            payment.setSubscription(subscription);
            payment.setPaymentDate(LocalDateTime.now());
            payment.setAmount(10.0); // Remplacez par le montant réel
            payment.setPaymentId(null);
            payment.setStatus(Payment.PaymentStatus.FAILED);
            payment.setErrorMessage(e.getMessage());
            paymentRepository.save(payment);

            return subscriptionRepository.save(subscription);
        }
    }

    public Subscription createStripeSubscriptionWithAutoRenew(Subscription subscription) {
        try {
            SubscriptionCreateParams createParams = new SubscriptionCreateParams.Builder()
                    .setCustomer(subscription.getCustomerId())
                    .addItem(SubscriptionCreateParams.Item.builder()
                            .setPrice(subscription.getPriceId())
                            .build())
                    .setCollectionMethod(SubscriptionCreateParams.CollectionMethod.CHARGE_AUTOMATICALLY)
                    .setBillingCycleAnchor(System.currentTimeMillis() / 1000)
                    .build();
            com.stripe.model.Subscription stripeSubscription = com.stripe.model.Subscription.create(createParams);
            subscription.setStripeSubscriptionId(stripeSubscription.getId());

            // Créer un enregistrement Payment
            Payment payment = new Payment();
            payment.setSubscription(subscription);
            payment.setPaymentDate(LocalDateTime.now());
            payment.setAmount(10.0); // Remplacez par le montant réel
            payment.setPaymentId(stripeSubscription.getId());
            payment.setStatus(Payment.PaymentStatus.SUCCESS);
            paymentRepository.save(payment);

            return subscriptionRepository.save(subscription);
        } catch (StripeException e) {
            subscription.setLastPaymentError(e.getMessage());

            // Créer un enregistrement Payment avec le statut FAILED
            Payment payment = new Payment();
            payment.setSubscription(subscription);
            payment.setPaymentDate(LocalDateTime.now());
            payment.setAmount(10.0); // Remplacez par le montant réel
            payment.setPaymentId(null);
            payment.setStatus(Payment.PaymentStatus.FAILED);
            payment.setErrorMessage(e.getMessage());
            paymentRepository.save(payment);

            return subscriptionRepository.save(subscription);
        }
    }

    public Subscription createStripeSubscriptionWithTrial(Subscription subscription, long trialPeriodDays) {
        try {
            SubscriptionCreateParams createParams = new SubscriptionCreateParams.Builder()
                    .setCustomer(subscription.getCustomerId())
                    .addItem(SubscriptionCreateParams.Item.builder()
                            .setPrice(subscription.getPriceId())
                            .build())
                    .setTrialPeriodDays(trialPeriodDays) // Définit la durée de l'essai gratuit
                    .build();
            com.stripe.model.Subscription stripeSubscription = com.stripe.model.Subscription.create(createParams);
            subscription.setStripeSubscriptionId(stripeSubscription.getId());
            subscription.setTrialStartDate(LocalDateTime.now());
            subscription.setTrialEndDate(LocalDateTime.now().plusDays(trialPeriodDays));

            // Créer un enregistrement Payment (pour l'essai gratuit)
            Payment payment = new Payment();
            payment.setSubscription(subscription);
            payment.setPaymentDate(LocalDateTime.now());
            payment.setAmount(0.0); // Montant de l'essai gratuit
            payment.setPaymentId(stripeSubscription.getId());
            payment.setStatus(Payment.PaymentStatus.SUCCESS);
            paymentRepository.save(payment);

            return subscriptionRepository.save(subscription);
        } catch (StripeException e) {
            subscription.setLastPaymentError(e.getMessage());

            // Créer un enregistrement Payment avec le statut FAILED
            Payment payment = new Payment();
            payment.setSubscription(subscription);
            payment.setPaymentDate(LocalDateTime.now());
            payment.setAmount(0.0); // Montant de l'essai gratuit
            payment.setPaymentId(null);
            payment.setStatus(Payment.PaymentStatus.FAILED);
            payment.setErrorMessage(e.getMessage());
            paymentRepository.save(payment);

            return subscriptionRepository.save(subscription);
        }
    }

    public Subscription refundSubscription(Subscription subscription, double amount, String reason) throws StripeException {
        RefundCreateParams createParams = new RefundCreateParams.Builder()
                .setPaymentIntent(subscription.getPaymentId()) // Identifiant du paiement à rembourser
                .setAmount((long) (amount * 100)) // Montant en centimes
                .setReason(RefundCreateParams.Reason.valueOf(reason)) // Raison du remboursement
                .build();
        Refund.create(createParams);

        subscription.setRefundDate(LocalDateTime.now());
        return subscriptionRepository.save(subscription);
    }
}