package com.shop.theshop.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "Adresses")

public class Adress {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String street;
    private String city;
    private String state;
    private String zipcode;
    private String country;

    public Adress(String updatedStreet, String updatedCity, String s) {
    }

    // Getters
    public Long getId() {
        return id;
    }
    public String getStreet() {
        return street;
    }
    public String getCity() {
        return city;
    }
    public String getState() {
        return state;
    }
    public String getZipcode() {
        return zipcode;
    }
    public String getCountry() {
        return country;
    }

    // Setters
    public void setId(Long id) {
        this.id = id;
    }
    public void setStreet(String street) {
        this.street = street;
    }
    public void setCity(String city) {
        this.city = city;
    }
    public void setState(String state) {
        this.state = state;
    }
    public void setZipcode(String zipcode) {
        this.zipcode = zipcode;
    }
    public void setCountry(String country) {
        this.country = country;
    }
}

