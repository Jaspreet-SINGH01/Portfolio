package com.videoflix.subscriptions_microservice.config;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class RabbitMQConfigTest {

    @Autowired
    private ApplicationContext context; // Injection du contexte de l'application Spring pour accéder aux beans

    @Test
    void testNewSubscriptionExchangeBean() {
        // Teste la création du bean DirectExchange pour les nouveaux abonnements
        DirectExchange exchange = context.getBean("newSubscriptionExchange", DirectExchange.class);
        assertNotNull(exchange); // Vérifie que l'instance de l'exchange n'est pas nulle
        assertEquals(RabbitMQConfig.NEW_SUBSCRIPTION_EXCHANGE, exchange.getName()); // Vérifie que le nom de l'exchange
                                                                                    // correspond à la constante
        assertTrue(exchange.isDurable()); // Vérifie que l'exchange est configuré pour être durable (survit aux
                                          // redémarrages du broker)
        assertFalse(exchange.isAutoDelete()); // Vérifie que l'exchange n'est pas auto-supprimé lorsqu'il n'est plus
                                              // utilisé
    }

    @Test
    void testWelcomeEmailQueueBean() {
        // Teste la création du bean Queue pour la file d'attente des e-mails de
        // bienvenue
        Queue queue = context.getBean("welcomeEmailQueue", Queue.class);
        assertNotNull(queue); // Vérifie que l'instance de la queue n'est pas nulle
        assertEquals(RabbitMQConfig.WELCOME_EMAIL_QUEUE, queue.getName()); // Vérifie que le nom de la queue correspond
                                                                           // à la constante
        assertTrue(queue.isDurable()); // Vérifie que la queue est configurée pour être durable
        assertFalse(queue.isExclusive()); // Vérifie que la queue n'est pas exclusive à une seule connexion
        assertFalse(queue.isAutoDelete()); // Vérifie que la queue n'est pas auto-supprimée
    }

    @Test
    void testWelcomeEmailBindingBean() {
        // Teste la création du bean Binding pour lier la queue des e-mails de bienvenue
        // à l'exchange des nouveaux abonnements
        Binding binding = context.getBean("welcomeEmailBinding", Binding.class);
        assertNotNull(binding); // Vérifie que l'instance du binding n'est pas nulle
        assertEquals(RabbitMQConfig.WELCOME_EMAIL_QUEUE, binding.getDestination()); // Vérifie que la destination du
                                                                                    // binding est la queue attendue
        assertEquals(RabbitMQConfig.NEW_SUBSCRIPTION_EXCHANGE, binding.getExchange()); // Vérifie que l'échange du
                                                                                       // binding est celui attendu
        assertEquals(RabbitMQConfig.WELCOME_EMAIL_ROUTING_KEY, binding.getRoutingKey()); // Vérifie que la clé de
                                                                                         // routage du binding est celle
                                                                                         // attendue
    }

    @Test
    void testSubscriptionLevelChangedExchangeBean() {
        // Teste la création du bean DirectExchange pour les changements de niveau
        // d'abonnement
        DirectExchange exchange = context.getBean("subscriptionLevelChangedExchange", DirectExchange.class);
        assertNotNull(exchange);
        assertEquals(RabbitMQConfig.SUBSCRIPTION_LEVEL_CHANGED_EXCHANGE, exchange.getName());
        assertTrue(exchange.isDurable());
        assertFalse(exchange.isAutoDelete());
    }

    @Test
    void testSubscriptionLevelChangedQueueBean() {
        // Teste la création du bean Queue pour la file d'attente des changements de
        // niveau d'abonnement
        Queue queue = context.getBean("subscriptionLevelChangedQueue", Queue.class);
        assertNotNull(queue);
        assertEquals(RabbitMQConfig.SUBSCRIPTION_LEVEL_CHANGED_QUEUE, queue.getName());
        assertTrue(queue.isDurable());
        assertFalse(queue.isExclusive());
        assertFalse(queue.isAutoDelete());
    }

    @Test
    void testSubscriptionLevelChangedBindingBean() {
        // Teste la création du bean Binding pour lier la queue des changements de
        // niveau d'abonnement à l'exchange correspondant
        Binding binding = context.getBean("subscriptionLevelChangedBinding", Binding.class);
        assertNotNull(binding);
        assertEquals(RabbitMQConfig.SUBSCRIPTION_LEVEL_CHANGED_QUEUE, binding.getDestination());
        assertEquals(RabbitMQConfig.SUBSCRIPTION_LEVEL_CHANGED_EXCHANGE, binding.getExchange());
        assertEquals(RabbitMQConfig.SUBSCRIPTION_LEVEL_CHANGED_ROUTING_KEY, binding.getRoutingKey());
    }

    @Test
    void testSubscriptionCancelledExchangeBean() {
        // Teste la création du bean DirectExchange pour l'annulation d'abonnement
        DirectExchange exchange = context.getBean("subscriptionCancelledExchange", DirectExchange.class);
        assertNotNull(exchange);
        assertEquals(RabbitMQConfig.SUBSCRIPTION_CANCELLED_EXCHANGE, exchange.getName());
        assertTrue(exchange.isDurable());
        assertFalse(exchange.isAutoDelete());
    }

    @Test
    void testSubscriptionCancelledQueueBean() {
        // Teste la création du bean Queue pour la file d'attente des annulations
        // d'abonnement
        Queue queue = context.getBean("subscriptionCancelledQueue", Queue.class);
        assertNotNull(queue);
        assertEquals(RabbitMQConfig.SUBSCRIPTION_CANCELLED_QUEUE, queue.getName());
        assertTrue(queue.isDurable());
        assertFalse(queue.isExclusive());
        assertFalse(queue.isAutoDelete());
    }

    @Test
    void testSubscriptionCancelledBindingBean() {
        // Teste la création du bean Binding pour lier la queue des annulations
        // d'abonnement à l'exchange correspondant
        Binding binding = context.getBean("subscriptionCancelledBinding", Binding.class);
        assertNotNull(binding);
        assertEquals(RabbitMQConfig.SUBSCRIPTION_CANCELLED_QUEUE, binding.getDestination());
        assertEquals(RabbitMQConfig.SUBSCRIPTION_CANCELLED_EXCHANGE, binding.getExchange());
        assertEquals(RabbitMQConfig.SUBSCRIPTION_CANCELLED_ROUTING_KEY, binding.getRoutingKey());
    }

    @Test
    void testSubscriptionRenewedExchangeBean() {
        // Teste la création du bean DirectExchange pour le renouvellement d'abonnement
        DirectExchange exchange = context.getBean("subscriptionRenewedExchange", DirectExchange.class);
        assertNotNull(exchange);
        assertEquals(RabbitMQConfig.SUBSCRIPTION_RENEWED_EXCHANGE, exchange.getName());
        assertTrue(exchange.isDurable());
        assertFalse(exchange.isAutoDelete());
    }

    @Test
    void testSubscriptionRenewedQueueBean() {
        // Teste la création du bean Queue pour la file d'attente des renouvellements
        // d'abonnement
        Queue queue = context.getBean("subscriptionRenewedQueue", Queue.class);
        assertNotNull(queue);
        assertEquals(RabbitMQConfig.SUBSCRIPTION_RENEWED_QUEUE, queue.getName());
        assertTrue(queue.isDurable());
        assertFalse(queue.isExclusive());
        assertFalse(queue.isAutoDelete());
    }

    @Test
    void testSubscriptionRenewedBindingBean() {
        // Teste la création du bean Binding pour lier la queue des renouvellements
        // d'abonnement à l'exchange correspondant
        Binding binding = context.getBean("subscriptionRenewedBinding", Binding.class);
        assertNotNull(binding);
        assertEquals(RabbitMQConfig.SUBSCRIPTION_RENEWED_QUEUE, binding.getDestination());
        assertEquals(RabbitMQConfig.SUBSCRIPTION_RENEWED_EXCHANGE, binding.getExchange());
        assertEquals(RabbitMQConfig.SUBSCRIPTION_RENEWED_ROUTING_KEY, binding.getRoutingKey());
    }

    @Test
    void testPaymentMethodUpdatedExchangeBean() {
        // Teste la création du bean DirectExchange pour la mise à jour de la méthode de
        // paiement
        DirectExchange exchange = context.getBean("paymentMethodUpdatedExchange", DirectExchange.class);
        assertNotNull(exchange);
        assertEquals(RabbitMQConfig.PAYMENT_METHOD_UPDATED_EXCHANGE, exchange.getName());
        assertTrue(exchange.isDurable());
        assertFalse(exchange.isAutoDelete());
    }

    @Test
    void testPaymentMethodUpdatedQueueBean() {
        // Teste la création du bean Queue pour la file d'attente des mises à jour de la
        // méthode de paiement
        Queue queue = context.getBean("paymentMethodUpdatedQueue", Queue.class);
        assertNotNull(queue);
        assertEquals(RabbitMQConfig.PAYMENT_METHOD_UPDATED_QUEUE, queue.getName());
        assertTrue(queue.isDurable());
        assertFalse(queue.isExclusive());
        assertFalse(queue.isAutoDelete());
    }

    @Test
    void testPaymentMethodUpdatedBindingBean() {
        // Teste la création du bean Binding pour lier la queue des mises à jour de la
        // méthode de paiement à l'exchange correspondant
        Binding binding = context.getBean("paymentMethodUpdatedBinding", Binding.class);
        assertNotNull(binding);
        assertEquals(RabbitMQConfig.PAYMENT_METHOD_UPDATED_QUEUE, binding.getDestination());
        assertEquals(RabbitMQConfig.PAYMENT_METHOD_UPDATED_EXCHANGE, binding.getExchange());
        assertEquals(RabbitMQConfig.PAYMENT_METHOD_UPDATED_ROUTING_KEY, binding.getRoutingKey());
    }

    @Test
    void testPaymentFailedExchangeBean() {
        // Teste la création du bean DirectExchange pour l'échec de paiement
        DirectExchange exchange = context.getBean("paymentFailedExchange", DirectExchange.class);
        assertNotNull(exchange);
        assertEquals(RabbitMQConfig.PAYMENT_FAILED_EXCHANGE, exchange.getName());
        assertTrue(exchange.isDurable());
        assertFalse(exchange.isAutoDelete());
    }

    @Test
    void testPaymentFailedQueueBean() {
        // Teste la création du bean Queue pour la file d'attente des échecs de paiement
        Queue queue = context.getBean("paymentFailedQueue", Queue.class);
        assertNotNull(queue);
        assertEquals(RabbitMQConfig.PAYMENT_FAILED_QUEUE, queue.getName());
        assertTrue(queue.isDurable());
        assertFalse(queue.isExclusive());
        assertFalse(queue.isAutoDelete());
    }

    @Test
    void testPaymentFailedBindingBean() {
        // Teste la création du bean Binding pour lier la queue des échecs de paiement à
        // l'exchange correspondant
        Binding binding = context.getBean("paymentFailedBinding", Binding.class);
        assertNotNull(binding);
        assertEquals(RabbitMQConfig.PAYMENT_FAILED_QUEUE, binding.getDestination());
        assertEquals(RabbitMQConfig.PAYMENT_FAILED_EXCHANGE, binding.getExchange());
        assertEquals(RabbitMQConfig.PAYMENT_FAILED_ROUTING_KEY, binding.getRoutingKey());
    }

    @Test
    void testSubscriptionReactivatedExchangeBean() {
        // Teste la création du bean DirectExchange pour la réactivation d'abonnement
        DirectExchange exchange = context.getBean("subscriptionReactivatedExchange", DirectExchange.class);
        assertNotNull(exchange);
        assertEquals(RabbitMQConfig.SUBSCRIPTION_REACTIVATED_EXCHANGE, exchange.getName());
        assertTrue(exchange.isDurable());
        assertFalse(exchange.isAutoDelete());
    }

    @Test
    void testSubscriptionReactivatedQueueBean() {
        // Teste la création du bean Queue pour la file d'attente des réactivations
        // d'abonnement
        Queue queue = context.getBean("subscriptionReactivatedQueue", Queue.class);
        assertNotNull(queue);
        assertEquals(RabbitMQConfig.SUBSCRIPTION_REACTIVATED_QUEUE, queue.getName());
        assertTrue(queue.isDurable());
        assertFalse(queue.isExclusive());
        assertFalse(queue.isAutoDelete());
    }

    @Test
    void testSubscriptionReactivatedBindingBean() {
        // Teste la création du bean Binding pour lier la queue des réactivations
        // d'abonnement à l'exchange correspondant
        Binding binding = context.getBean("subscriptionReactivatedBinding", Binding.class);
        assertNotNull(binding);
        assertEquals(RabbitMQConfig.SUBSCRIPTION_REACTIVATED_QUEUE, binding.getDestination());
        assertEquals(RabbitMQConfig.SUBSCRIPTION_REACTIVATED_EXCHANGE, binding.getExchange());
        assertEquals(RabbitMQConfig.SUBSCRIPTION_REACTIVATED_ROUTING_KEY, binding.getRoutingKey());
    }
}