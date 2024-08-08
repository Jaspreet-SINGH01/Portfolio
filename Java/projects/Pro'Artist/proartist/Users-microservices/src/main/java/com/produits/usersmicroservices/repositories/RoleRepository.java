package com.produits.usersmicroservices.repositories;

import com.produits.usersmicroservices.entities.Role;
import org.springframework.data.jpa.repository.JpaRepository;
public interface RoleRepository extends JpaRepository<Role, Long> {
    Role findByRole(String role);
}

