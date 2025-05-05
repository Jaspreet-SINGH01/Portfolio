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

    // Champs supplémentaires pour la gestion des factures
    @Column(name = "payment_date")
    private LocalDateTime paymentDate; // Date à laquelle le paiement a été effectué

    @Column(name = "payment_method")
    private String paymentMethod; // Méthode de paiement utilisée (e.g., Carte de crédit, PayPal)

    @Column(name = "transaction_id")
    private String transactionId; // Identifiant de la transaction de paiement

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes; // Notes ou informations supplémentaires concernant la facture

    public enum InvoiceStatus {
        DRAFT,
        FAILED,
        PAID,
        PENDING,
        VOIDED
    }
}