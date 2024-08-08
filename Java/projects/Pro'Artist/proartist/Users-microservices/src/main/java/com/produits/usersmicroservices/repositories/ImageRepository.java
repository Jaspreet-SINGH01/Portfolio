package com.produits.usersmicroservices.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.produits.usersmicroservices.entities.Image;

public interface ImageRepository extends JpaRepository<Image, Long> {

}
