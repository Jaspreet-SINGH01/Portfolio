package com.produits.usersmicroservices.repositories;

import com.produits.usersmicroservices.entities.Categorie;
import org.springframework.data.jpa.repository.JpaRepository;

@RepositoryRestResource(path = "cat")
public interface CategorieRepository extends JpaRepository<Categorie, Long> {

}
