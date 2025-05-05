package com.videoflix.subscriptions_microservice.events;

import com.videoflix.subscriptions_microservice.entities.Subscription;
import com.videoflix.subscriptions_microservice.entities.User;

import org.springframework.context.ApplicationEvent;

public class NewSubscriptionCreatedEvent extends ApplicationEvent {
    private final User user;
    private final transient Subscription subscription;

    public NewSubscriptionCreatedEvent(Object source, User user, Subscription subscription) {
        super(source);
        this.user = user;
        this.subscription = subscription;
    }

    public User getUser() {
        return user;
    }

    public Subscription getSubscription() {
        return subscription;
    }
}