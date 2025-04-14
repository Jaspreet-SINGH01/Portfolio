package com.videoflix.subscriptions_microservice.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST) // Indique que la requête est invalide en raison de l'état de l'abonnement
public class InvalidSubscriptionStateException extends RuntimeException {

    private final String currentState;
    private final String attemptedOperation;

    public InvalidSubscriptionStateException(String message) {
        super(message);
        this.currentState = "";
        this.attemptedOperation = "";
    }

    public InvalidSubscriptionStateException(String message, Throwable cause) {
        super(message, cause);
        this.currentState = "";
        this.attemptedOperation = "";
    }

    public InvalidSubscriptionStateException(String message, String currentState, String attemptedOperation) {
        super(message);
        this.currentState = currentState;
        this.attemptedOperation = attemptedOperation;
    }

    public InvalidSubscriptionStateException(String message, Throwable cause, String currentState,
            String attemptedOperation) {
        super(message, cause);
        this.currentState = currentState;
        this.attemptedOperation = attemptedOperation;
    }

    public String getCurrentState() {
        return currentState;
    }

    public String getAttemptedOperation() {
        return attemptedOperation;
    }
}