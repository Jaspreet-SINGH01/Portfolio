package com.videoflix.subscriptions_microservice.entities;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "subscription_levels")
@Data
public class SubscriptionLevel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "level", unique = true)
    @Enumerated(EnumType.STRING)
    private Level level; // Niveau d'abonnement (BASIC, PREMIUM, ULTRA)

    @Column(name = "price")
    private double price; // Prix de l'abonnement

    @Column(name = "features")
    private String features; // Caractéristiques de l'abonnement (liste de fonctionnalités)

    @Column(name = "billing_frequency", nullable = false)
    @Enumerated(EnumType.STRING)
    private BillingFrequency billingFrequency; // Fréquence de facture

    @Column(name = "stripe_price_id")
    private String stripePriceId;

    public enum Level {
        BASIC,
        PREMIUM,
        ULTRA;

        public static Level fromString(String text) {
            for (Level l : Level.values()) {
                if (l.name().equalsIgnoreCase(text)) {
                    return l;
                }
            }
            throw new IllegalArgumentException("Niveau d'abonnement inconnu : " + text);
        }
    }

    public enum BillingFrequency {
        MONTHLY,
        QUARTERLY,
        YEARLY
    }

    @Override
    public String toString() {
        return level != null ? level.name() : null;
    }
}