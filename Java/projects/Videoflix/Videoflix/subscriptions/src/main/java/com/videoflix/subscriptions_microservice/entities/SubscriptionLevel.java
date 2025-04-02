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

    public enum Level {
        BASIC,
        PREMIUM,
        ULTRA
    }
}