package com.videoflix.subscriptions_microservice.events;

import com.videoflix.subscriptions_microservice.entities.Subscription;
import com.videoflix.subscriptions_microservice.entities.User;
import org.springframework.context.ApplicationEvent;

public class SubscriptionLevelChangedEvent extends ApplicationEvent {
    private final User user;
    private final transient Subscription subscription;
    private final String oldLevel; // Niveau d'abonnement précédent
    private final String newLevel; // Nouveau niveau d'abonnement

    public SubscriptionLevelChangedEvent(Object source, User user, Subscription subscription, String oldLevel,
            String newLevel) {
        super(source);
        this.user = user;
        this.subscription = subscription;
        this.oldLevel = oldLevel;
        this.newLevel = newLevel;
    }

    public User getUser() {
        return user;
    }

    public Subscription getSubscription() {
        return subscription;
    }

    public String getOldLevel() {
        return oldLevel;
    }

    public String getNewLevel() {
        return newLevel;
    }
}