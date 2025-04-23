package com.videoflix.subscriptions_microservice.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.envers.Audited;

import com.videoflix.subscriptions_microservice.validations.ValidSubscriptionLevel;

@Entity
@Table(name = "subscriptions", indexes = {
        @Index(name = "idx_status", columnList = "status"),
        @Index(name = "idx_customer_id", columnList = "customer_id")
})
@Data
@Audited
public class Subscription {

    /** Identifiant unique de l'abonnement */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // L'entité User liée à cet abonnement
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** Niveau d'abonnement associé (ex: Basic, Premium, Ultra) */
    @ManyToOne
    @JoinColumn(name = "level_id", nullable = false)
    @NotNull(message = "Le type d'abonnement est obligatoire")
    @ValidSubscriptionLevel
    private SubscriptionLevel subscriptionLevel;

    /** Promotion */
    @ManyToOne
    @JoinColumn(name = "promotion_code_id")
    private Promotion promotion;

    /** Date de début de l'abonnement */
    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    /** Date de fin de l'abonnement */
    @Column(name = "end_date")
    private LocalDateTime endDate;

    /** Statut actuel de l'abonnement (ACTIVE, CANCELLED, etc.) */
    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private SubscriptionStatus status;

    /** Indique si l'abonnement se renouvelle automatiquement */
    @Column(name = "auto_renew", nullable = false)
    private boolean autoRenew;

    /** Date du dernier paiement effectué */
    @Column(name = "payment_date")
    private LocalDateTime paymentDate;

    /** Identifiant du paiement dans le système de paiement */
    @Column(name = "payment_id")
    private String paymentId;

    /** Date prévue pour le prochain renouvellement */
    @Column(name = "next_renewal_date")
    private LocalDateTime nextRenewalDate;

    /** Date prévue pour la prochaine facturation */
    @Column(name = "next_billing_date")
    private LocalDateTime nextBillingDate;

    /** Date prévue pour le prochain essai */
    @Column(name = "next_retry_date")
    private LocalDateTime nextRetryDate;

    /** Identifiant du client dans la passerelle de paiement (Stripe) */
    @Column(name = "customer_id", nullable = false)
    private String customerId;

    /** Identifiant de l'abonnement dans Stripe */
    @Column(name = "subscription_id")
    private String stripeSubscriptionId;

    /** Identifiant du prix appliqué dans Stripe */
    @Column(name = "price_id")
    private String priceId;

    /** Message d'erreur du dernier paiement */
    @Column(name = "last_payment_error")
    private String lastPaymentError;

    @Column(name = "trial_start_date")
    private LocalDateTime trialStartDate; // Date de début de l'essai gratuit

    @Column(name = "trial_end_date")
    private LocalDateTime trialEndDate; // Date de fin de l'essai gratuit

    @Column(name = "refund_date")
    private LocalDateTime refundDate; // Date du remboursement

    @Column(name = "refund_amount")
    private double refundAmount; // Montant du remboursement

    @OneToMany(mappedBy = "subscription", cascade = CascadeType.ALL)
    private List<Payment> payments; // Liste des paiements associés à l'abonnement

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "stripe_charge_id")
    private String stripeChargeId;

    public enum SubscriptionStatus {
        ACTIVE, // Abonnement actif et en cours
        ARCHIVED, // Abonnement archivé
        CANCELLED, // Abonnement annulé mais encore valide jusqu'à sa date de fin
        EXPIRED, // Abonnement expiré
        INACTIVE, // Abonnement inactif
        PENDING, // Paiement en attente ou en cours de traitement
        PAYMENT_FAILED, // Paiement échoué
        TRIAL, // Période d'essai
        TRIAL_ENDED // Fin de la période d'essai
    }

    /**
     * Retourne la date du prochain paiement
     * 
     * @return La date du prochain paiement ou null si l'abonnement n'est pas actif
     */
    public LocalDateTime getNextBillingDate() {
        // Si l'abonnement n'est pas actif ou si le renouvellement automatique est
        // désactivé
        if (status != SubscriptionStatus.ACTIVE && status != SubscriptionStatus.TRIAL || !autoRenew) {
            return null;
        }

        // Utiliser la date de prochain renouvellement si elle est déjà définie
        if (nextRenewalDate != null) {
            return nextRenewalDate;
        }

        // Si l'abonnement est en période d'essai, retourner la date de fin d'essai
        if (status == SubscriptionStatus.TRIAL && trialEndDate != null) {
            return trialEndDate;
        }

        // Par défaut, retourner null
        return null;
    }
}