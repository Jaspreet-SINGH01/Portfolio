package com.videoflix.subscriptions_microservice.repositories;

import com.videoflix.subscriptions_microservice.entities.FailedEmail;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FailedEmailRepository extends JpaRepository<FailedEmail, Long> {

    List<FailedEmail> findByRecipientEmail(String recipientEmail);

    List<FailedEmail> findByCreationTimestampBefore(LocalDateTime dateTime);
}