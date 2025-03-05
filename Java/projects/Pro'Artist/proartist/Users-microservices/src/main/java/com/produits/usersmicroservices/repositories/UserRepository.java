package com.produits.usersmicroservices.repositories;

import com.produits.usersmicroservices.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);
}
