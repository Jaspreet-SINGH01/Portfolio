package com.videoflix.subscriptions_microservice.repositories;

import com.videoflix.subscriptions_microservice.entities.SubscriptionLevel;
import com.videoflix.subscriptions_microservice.entities.SubscriptionLevel.Level;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SubscriptionLevelRepository extends JpaRepository<SubscriptionLevel, Long> {

    Optional<SubscriptionLevel> findByLevel(Level levelEnum);
}