package com.videoflix.subscriptions_microservice.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "failed_emails")
@Data
public class FailedEmail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String recipientEmail;

    @Column(nullable = false)
    private String subject;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String body;

    @Column(nullable = false)
    private int attemptCount;

    @Column(nullable = false)
    private String failureReason;

    @Column(nullable = false)
    private LocalDateTime creationTimestamp;

    public FailedEmail(String recipientEmail, String subject, String body, int attemptCount, String failureReason,
            LocalDateTime creationTimestamp) {
        this.recipientEmail = recipientEmail;
        this.subject = subject;
        this.body = body;
        this.attemptCount = attemptCount;
        this.failureReason = failureReason;
        this.creationTimestamp = creationTimestamp;
    }

    @Override
    public String toString() {
        return "FailedEmail{" +
                "id=" + id +
                ", recipientEmail='" + recipientEmail + '\'' +
                ", subject='" + subject + '\'' +
                ", body='" + body + '\'' +
                ", attemptCount=" + attemptCount +
                ", failureReason='" + failureReason + '\'' +
                ", creationTimestamp=" + creationTimestamp +
                '}';
    }
}