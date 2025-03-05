package com.example.users.entities;

import jakarta.persistence.*;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "secteurs_activites")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ActivityType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code", nullable = false, unique = false)
    private String code;

    @Column(name = "libelle", nullable = false)
    private String libelle;

}
