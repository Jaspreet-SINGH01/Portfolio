package com.videoflix.subscriptions_microservice.events;

import com.videoflix.subscriptions_microservice.entities.Subscription;
import com.videoflix.subscriptions_microservice.entities.User;
import org.springframework.context.ApplicationEvent;

public class PaymentInfoUpdatedEvent extends ApplicationEvent {
    private final User user;
    private final transient Subscription subscription; // L'abonnement concerné par la mise à jour des infos de paiement

    // Inclure des détails spécifiques sur les informations mises à
    // jour si nécessaire
    private final String updatedPaymentMethod;
    private final String lastFourDigits;

    public PaymentInfoUpdatedEvent(Object source, User user, Subscription subscription) {
        super(source);
        this.user = user;
        this.subscription = subscription;
        this.updatedPaymentMethod = "";
        this.lastFourDigits = "";
    }

    public User getUser() {
        return user;
    }

    public Subscription getSubscription() {
        return subscription;
    }

    public String getUpdatedPaymentMethod() {
        return updatedPaymentMethod;
    }

    public String getLastFourDigits() {
        return lastFourDigits;
    }
}