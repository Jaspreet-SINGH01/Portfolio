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

    @Column(name = "name", unique = true)
    private String name; // Nom du niveau (Basic, Premium, Ultra)

    @Column(name = "price")
    private double price; // Prix de l'abonnement

    @Column(name = "features")
    private String features; // Caractéristiques de l'abonnement (liste de fonctionnalités)
}