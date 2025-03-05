package com.example.users.entities;

import jakarta.persistence.*;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "artist")
@AllArgsConstructor
@NoArgsConstructor
public class Artist extends User {

    @OneToMany(mappedBy = "artist")
    private List<SponsoringContract> sponsoringContracts;

}