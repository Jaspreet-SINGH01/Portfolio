package com.videoflix.subscriptions_microservice.entities;

import java.util.List;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "users")
@Data
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username", unique = true, nullable = false)
    private String username;

    @Column(name = "email", unique = true, nullable = false)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "first_name", nullable = false)
    private String firstname;

    @Column(name = "last_name", nullable = false)
    private String lastname;

    @Column(name = "push_token")
    private String pushToken;

    @Column(name = "stripe_customer_id")
    private String stripeCustomerId;

    @OneToMany(mappedBy = "user")
    private List<UserRole> userRoles;
}