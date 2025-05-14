package com.videoflix.subscriptions_microservice.repositories;

import com.videoflix.subscriptions_microservice.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    User findByUsername(String username);

    User findByEmail(String email);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.userRoles WHERE u.id = :userId")
    Optional<User> findByIdWithRoles(@Param("userId") Long userId);

    Optional<User> findByStripeCustomerId(String stripeCustomerId);
}