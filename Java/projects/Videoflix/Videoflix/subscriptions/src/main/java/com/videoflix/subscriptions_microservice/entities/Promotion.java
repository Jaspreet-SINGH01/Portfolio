package com.videoflix.subscriptions_microservice.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "promotions")
@Data
public class Promotion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "promotion_code", unique = true)
    private String promotionCode; // Code promo

    @Column(name = "discount_percentage")
    private int discountPercentage; // Pourcentage de réduction

    @Column(name = "start_date")
    private LocalDateTime startDate; // Date de début de la promotion

    @Column(name = "end_date")
    private LocalDateTime endDate; // Date de fin de la promotion

    @Column(name = "active")
    private boolean active; // Indique si la promotion est active

    @Column(name = "description")
    private String description; // Description de la promotion
}