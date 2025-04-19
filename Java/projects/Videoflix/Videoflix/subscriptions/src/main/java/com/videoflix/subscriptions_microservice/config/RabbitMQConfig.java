package com.videoflix.subscriptions_microservice.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // Échange et Queue pour les nouveaux abonnements et l'e-mail de bienvenue
    public static final String NEW_SUBSCRIPTION_EXCHANGE = "videoflix.subscriptions.new";
    public static final String WELCOME_EMAIL_QUEUE = "videoflix.subscriptions.welcome-email";
    public static final String WELCOME_EMAIL_ROUTING_KEY = "welcome.email";

    // Échange et Queue pour les changements de niveau d'abonnement
    public static final String SUBSCRIPTION_LEVEL_CHANGED_EXCHANGE = "subscription.events";
    public static final String SUBSCRIPTION_LEVEL_CHANGED_QUEUE = "subscription.level-changed";
    public static final String SUBSCRIPTION_LEVEL_CHANGED_ROUTING_KEY = "subscription.level.changed";

    // Échange et Queue pour l'annulation d'abonnement
    public static final String SUBSCRIPTION_CANCELLED_EXCHANGE = "subscription.events";
    public static final String SUBSCRIPTION_CANCELLED_QUEUE = "subscription.cancelled";
    public static final String SUBSCRIPTION_CANCELLED_ROUTING_KEY = "subscription.cancelled";

    // Échange et Queue pour le renouvellement d'abonnement
    public static final String SUBSCRIPTION_RENEWED_EXCHANGE = "subscription.events";
    public static final String SUBSCRIPTION_RENEWED_QUEUE = "subscription.renewed";
    public static final String SUBSCRIPTION_RENEWED_ROUTING_KEY = "subscription.renewed";

    // Échange et Queue pour la mise à jour de la méthode de paiement
    public static final String PAYMENT_METHOD_UPDATED_EXCHANGE = "user.events";
    public static final String PAYMENT_METHOD_UPDATED_QUEUE = "user.payment-method-updated";
    public static final String PAYMENT_METHOD_UPDATED_ROUTING_KEY = "user.payment.method.updated";

    // Échange et Queue pour l'échec de paiement
    public static final String PAYMENT_FAILED_EXCHANGE = "billing.events";
    public static final String PAYMENT_FAILED_QUEUE = "billing.payment-failed";
    public static final String PAYMENT_FAILED_ROUTING_KEY = "billing.payment.failed";

    // Échange et Queue pour la réactivation d'abonnement
    public static final String SUBSCRIPTION_REACTIVATED_EXCHANGE = "subscription.events";
    public static final String SUBSCRIPTION_REACTIVATED_QUEUE = "subscription.reactivated";
    public static final String SUBSCRIPTION_REACTIVATED_ROUTING_KEY = "subscription.reactivated";

    @Bean
    public DirectExchange newSubscriptionExchange() {
        return new DirectExchange(NEW_SUBSCRIPTION_EXCHANGE);
    }

    @Bean
    public Queue welcomeEmailQueue() {
        return new Queue(WELCOME_EMAIL_QUEUE, true);
    }

    @Bean
    public Binding welcomeEmailBinding(Queue welcomeEmailQueue, DirectExchange newSubscriptionExchange) {
        return BindingBuilder.bind(welcomeEmailQueue).to(newSubscriptionExchange).with(WELCOME_EMAIL_ROUTING_KEY);
    }

    @Bean
    public DirectExchange subscriptionLevelChangedExchange() {
        return new DirectExchange(SUBSCRIPTION_LEVEL_CHANGED_EXCHANGE);
    }

    @Bean
    public Queue subscriptionLevelChangedQueue() {
        return new Queue(SUBSCRIPTION_LEVEL_CHANGED_QUEUE, true);
    }

    @Bean
    public Binding subscriptionLevelChangedBinding(Queue subscriptionLevelChangedQueue,
            DirectExchange subscriptionLevelChangedExchange) {
        return BindingBuilder.bind(subscriptionLevelChangedQueue).to(subscriptionLevelChangedExchange)
                .with(SUBSCRIPTION_LEVEL_CHANGED_ROUTING_KEY);
    }

    @Bean
    public DirectExchange subscriptionCancelledExchange() {
        return new DirectExchange(SUBSCRIPTION_CANCELLED_EXCHANGE);
    }

    @Bean
    public Queue subscriptionCancelledQueue() {
        return new Queue(SUBSCRIPTION_CANCELLED_QUEUE, true);
    }

    @Bean
    public Binding subscriptionCancelledBinding(Queue subscriptionCancelledQueue,
            DirectExchange subscriptionCancelledExchange) {
        return BindingBuilder.bind(subscriptionCancelledQueue).to(subscriptionCancelledExchange)
                .with(SUBSCRIPTION_CANCELLED_ROUTING_KEY);
    }

    @Bean
    public DirectExchange subscriptionRenewedExchange() {
        return new DirectExchange(SUBSCRIPTION_RENEWED_EXCHANGE);
    }

    @Bean
    public Queue subscriptionRenewedQueue() {
        return new Queue(SUBSCRIPTION_RENEWED_QUEUE, true);
    }

    @Bean
    public Binding subscriptionRenewedBinding(Queue subscriptionRenewedQueue,
            DirectExchange subscriptionRenewedExchange) {
        return BindingBuilder.bind(subscriptionRenewedQueue).to(subscriptionRenewedExchange)
                .with(SUBSCRIPTION_RENEWED_ROUTING_KEY);
    }

    @Bean
    public DirectExchange paymentMethodUpdatedExchange() {
        return new DirectExchange(PAYMENT_METHOD_UPDATED_EXCHANGE);
    }

    @Bean
    public Queue paymentMethodUpdatedQueue() {
        return new Queue(PAYMENT_METHOD_UPDATED_QUEUE, true);
    }

    @Bean
    public Binding paymentMethodUpdatedBinding(Queue paymentMethodUpdatedQueue,
            DirectExchange paymentMethodUpdatedExchange) {
        return BindingBuilder.bind(paymentMethodUpdatedQueue).to(paymentMethodUpdatedExchange)
                .with(PAYMENT_METHOD_UPDATED_ROUTING_KEY);
    }

    @Bean
    public DirectExchange paymentFailedExchange() {
        return new DirectExchange(PAYMENT_FAILED_EXCHANGE);
    }

    @Bean
    public Queue paymentFailedQueue() {
        return new Queue(PAYMENT_FAILED_QUEUE, true);
    }

    @Bean
    public Binding paymentFailedBinding(Queue paymentFailedQueue, DirectExchange paymentFailedExchange) {
        return BindingBuilder.bind(paymentFailedQueue).to(paymentFailedExchange).with(PAYMENT_FAILED_ROUTING_KEY);
    }

    @Bean
    public DirectExchange subscriptionReactivatedExchange() {
        return new DirectExchange(SUBSCRIPTION_REACTIVATED_EXCHANGE);
    }

    @Bean
    public Queue subscriptionReactivatedQueue() {
        return new Queue(SUBSCRIPTION_REACTIVATED_QUEUE, true);
    }

    @Bean
    public Binding subscriptionReactivatedBinding(Queue subscriptionReactivatedQueue,
            DirectExchange subscriptionReactivatedExchange) {
        return BindingBuilder.bind(subscriptionReactivatedQueue).to(subscriptionReactivatedExchange)
                .with(SUBSCRIPTION_REACTIVATED_ROUTING_KEY);
    }
}