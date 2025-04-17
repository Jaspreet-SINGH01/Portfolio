package com.videoflix.subscriptions_microservice.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String NEW_SUBSCRIPTION_EXCHANGE = "videoflix.subscriptions.new";
    public static final String WELCOME_EMAIL_QUEUE = "videoflix.subscriptions.welcome-email";
    public static final String WELCOME_EMAIL_ROUTING_KEY = "welcome.email";

    @Bean
    public DirectExchange newSubscriptionExchange() {
        return new DirectExchange(NEW_SUBSCRIPTION_EXCHANGE);
    }

    @Bean
    public Queue welcomeEmailQueue() {
        return new Queue(WELCOME_EMAIL_QUEUE, true); // durable: la queue survit au redémarrage du broker
    }

    @Bean
    public Binding welcomeEmailBinding(Queue welcomeEmailQueue, DirectExchange newSubscriptionExchange) {
        return BindingBuilder.bind(welcomeEmailQueue).to(newSubscriptionExchange).with(WELCOME_EMAIL_ROUTING_KEY);
    }
}