package com.shop.theshop.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "payments")
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;
    private String paymentMethod;
    private Double amount;

    // Getters
    public Long getId() {
        return id;
    }
    public Order getOrder() {
        return order;
    }
    public String getPaymentMethod() {
        return paymentMethod;
    }
    public Double getAmount() {
        return amount;
    }

    // Setters
    public void setId(Long id) {
        this.id = id;
    }
    public void setOrder(Order order) {
        this.order = order;
    }
    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
    public void setAmount(Double amount) {
        this.amount = amount;
    }
}
