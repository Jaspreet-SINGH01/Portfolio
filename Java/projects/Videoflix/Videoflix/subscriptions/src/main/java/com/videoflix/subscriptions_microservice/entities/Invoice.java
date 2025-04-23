package com.videoflix.subscriptions_microservice.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "invoices")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "subscription_id", nullable = false)
    private Subscription subscription;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "issue_date", nullable = false)
    private LocalDateTime issueDate;

    @Column(name = "billing_start_date", nullable = false)
    private LocalDateTime billingStartDate;

    @Column(name = "billing_end_date", nullable = false)
    private LocalDateTime billingEndDate;

    @Column(name = "amount", nullable = false)
    private Double amount;

    @Column(name = "currency", nullable = false)
    private String currency;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private InvoiceStatus status;

    @Column(name = "stripe_invoice_id", unique = true)
    private String stripeInvoiceId; // Facultatif: si vous liez vos factures à Stripe

    // Ajoutez d'autres champs pertinents pour votre gestion des factures
    // par exemple: date de paiement, méthode de paiement, etc.

    public enum InvoiceStatus {
        DRAFT,
        FAILED,
        PAID,
        PENDING,
        VOIDED
    }
}