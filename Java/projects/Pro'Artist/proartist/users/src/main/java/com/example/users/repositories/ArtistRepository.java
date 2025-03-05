package com.example.users.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.users.entities.Artist;

public interface ArtistRepository extends JpaRepository<Artist, Long> {

    // List<Artist> findByNomArtisteContaining(String nomArtist);

    // List<Artist> findByStyleArtistique(String styleArtistique);

    List<Artist> findByJob(String job);

    // Je me demande s'il ne faudrait pas supprimer l'entité...

}

