package com.example.users.entities;

import jakarta.persistence.*;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "remises")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Discount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "type_remise", nullable = false)
    private String typeRemise; // Taux ou Montant

    @Column(name = "valeur_remise", nullable = false)
    private BigDecimal valeurRemise;

    @ManyToOne
    @JoinColumn(name = "facture_id", nullable = false)
    private Invoice facture;

}

