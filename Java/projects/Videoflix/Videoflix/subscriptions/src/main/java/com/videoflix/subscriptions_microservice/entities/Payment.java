package com.videoflix.subscriptions_microservice.entities;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Data
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "subscription_id", nullable = false)
    private Subscription subscription; // Abonnement associé

    @Column(name = "payment_date", nullable = false)
    private LocalDateTime paymentDate; // Date du paiement

    @Column(name = "amount", nullable = false)
    private double amount; // Montant du paiement

    @Column(name = "payment_id")
    private String paymentId; // Identifiant du paiement (Stripe, etc.)

    @Column(name = "payment_method")
    private String paymentMethod; // Méthode de paiement (PayPal,...)

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private PaymentStatus status; // Statut du paiement

    @Column(name = "error_message")
    private String errorMessage; // Message d'erreur en cas d'échec

    public enum PaymentStatus {
        SUCCESS,
        FAILED,
        PENDING
    }
}