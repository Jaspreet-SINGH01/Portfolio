package com.videoflix.subscriptions_microservice.entities;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "subscriptions")
@Data
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "level_id")
    private SubscriptionLevel subscriptionLevel; // Niveau d'abonnement
    
    @ManyToOne
    @JoinColumn(name = "promotion_code_id")
    private Promotion promotion; // Niveau d'abonnement

    @Column(name = "start_date")
    private java.time.LocalDateTime startDate;

    @Column(name = "end_date")
    private java.time.LocalDateTime endDate;

    // Ajout d'autres champs pertinents pour l'abonnement
    @Column(name = "status")
    private String status; // Par exemple, "Active", "Cancelled", "Expired"

    @Column(name = "auto_renew")
    private boolean autoRenew;

    // Ajouter d'autres champs comme la date de paiement, l'identifiant de paiement,
}