package com.videoflix.users_microservice.repositories;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.videoflix.users_microservice.entities.LoginAttempt;

public interface LoginAttemptRepository extends JpaRepository<LoginAttempt, Long> {
    List<LoginAttempt> findByUsernameAndAttemptTimeAfter(String username, LocalDateTime time);
}