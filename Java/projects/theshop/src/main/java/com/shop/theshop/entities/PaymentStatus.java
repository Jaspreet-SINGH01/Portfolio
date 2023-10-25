package com.shop.theshop.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "payment_status")

public class PaymentStatus {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;

    // Getters
    public Long getId() {
        return id;
    }
    public String getName() {
        return name;
    }

    // Setters
    public void setId(Long id) {
        this.id = id;
    }
    public void setName(String name) {
        this.name = name;
    }
}
