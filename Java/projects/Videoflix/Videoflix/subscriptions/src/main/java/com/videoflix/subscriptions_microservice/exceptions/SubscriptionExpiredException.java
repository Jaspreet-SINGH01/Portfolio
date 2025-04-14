package com.videoflix.subscriptions_microservice.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.time.LocalDate;

@ResponseStatus(HttpStatus.FORBIDDEN) // L'opération n'est pas autorisée car l'abonnement est expiré
public class SubscriptionExpiredException extends RuntimeException {

    private final LocalDate expirationDate;

    public SubscriptionExpiredException(String message) {
        super(message);
        this.expirationDate = null;
    }

    public SubscriptionExpiredException(String message, Throwable cause) {
        super(message, cause);
        this.expirationDate = null;
    }

    public SubscriptionExpiredException(String message, LocalDate expirationDate) {
        super(message);
        this.expirationDate = expirationDate;
    }

    public SubscriptionExpiredException(String message, Throwable cause, LocalDate expirationDate) {
        super(message, cause);
        this.expirationDate = expirationDate;
    }

    public LocalDate getExpirationDate() {
        return expirationDate;
    }
}