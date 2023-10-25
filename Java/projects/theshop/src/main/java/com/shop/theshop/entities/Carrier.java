package com.shop.theshop.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "carriers")
public class Carrier {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String trackingUrl;

    // Getters
    public Long getId() {
        return id;
    }
    public String getName() {
        return name;
    }
    public String getTrackingUrl() {
        return trackingUrl;
    }

    // Setters
    public void setId(Long id) {
        this.id = id;
    }
    public void setName(String name) {
        this.name = name;
    }
    public void setTrackingUrl(String trackingUrl) {
        this.trackingUrl = trackingUrl;
    }

}
