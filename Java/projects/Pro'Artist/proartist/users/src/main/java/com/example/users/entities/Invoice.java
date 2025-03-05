package com.example.users.entities;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import ch.qos.logback.core.net.server.Client;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "invoices")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero_facture", nullable = false, unique = true)
    private String numeroFacture;

    @Column(name = "date_facture", nullable = false)
    private Date dateFacture;

    @Column(name = "client_id", nullable = false)
    private Long clientId;

    @ManyToOne // Optional: @ FetchType(FetchType.EAGER) for eager fetching
    @JoinColumn(name = "client_id", insertable = false, updatable = false)
    private Client client;

    @Column(name = "montant_total", nullable = false)
    private BigDecimal montantTotal;

    @Column(name = "taxe", nullable = false)
    private BigDecimal taxe;

    // Optional: Use isReglee if you prefer following Java Bean naming conventions
    @Column(name = "reglee", nullable = false)
    private boolean reglee;

    @OneToMany(mappedBy = "facture")
    private List<InvoiceLine> invoiceLines;

}
