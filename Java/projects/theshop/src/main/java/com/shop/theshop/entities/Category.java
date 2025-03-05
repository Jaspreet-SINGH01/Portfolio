package com.shop.theshop.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "categories")
public class Category {
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
    public void setName(String id) {
        this.name = name;
    }
}
