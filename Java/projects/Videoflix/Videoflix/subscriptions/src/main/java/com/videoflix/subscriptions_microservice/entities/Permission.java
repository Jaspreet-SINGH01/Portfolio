package com.videoflix.subscriptions_microservice.entities;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "permissions")
@Data
public class Permission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", unique = true, nullable = false)
    private String name; // Nom de la permission (par exemple, "READ_VIDEOS", "WRITE_COMMENTS")
}