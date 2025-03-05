package com.example.users.entities;

import jakarta.persistence.*;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "lignes_factures")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class InvoiceLine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero_ligne", nullable = false)
    private int numeroLigne;

    @Column(name = "designation", nullable = false)
    private String designation;

    @Column(name = "quantite", nullable = false)
    private int quantite;

    @Column(name = "prix_unitaire", nullable = false)
    private BigDecimal prixUnitaire;

    @Column(name = "taxe", nullable = false)
    private BigDecimal taxe;

    @Column(name = "montant_total", nullable = false)
    private BigDecimal montantTotal;

    @ManyToOne
    @JoinColumn(name = "facture_id", nullable = false)
    private Invoice invoice;

}

