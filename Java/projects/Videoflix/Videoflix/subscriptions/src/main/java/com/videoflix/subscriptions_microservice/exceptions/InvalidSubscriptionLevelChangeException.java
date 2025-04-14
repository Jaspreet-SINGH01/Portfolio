package com.videoflix.subscriptions_microservice.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST) // Indique que la requête du client n'est pas valide en raison de la règle
                                        // métier
public class InvalidSubscriptionLevelChangeException extends RuntimeException {

    private final String currentLevel;
    private final String requestedLevel;
    private final String reason;

    public InvalidSubscriptionLevelChangeException(String message) {
        super(message);
        this.currentLevel = "";
        this.requestedLevel = "";
        this.reason = "";
    }

    public InvalidSubscriptionLevelChangeException(String message, Throwable cause) {
        super(message, cause);
        this.currentLevel = "";
        this.requestedLevel = "";
        this.reason = "";
    }

    public InvalidSubscriptionLevelChangeException(String message, String currentLevel, String requestedLevel) {
        super(message);
        this.currentLevel = currentLevel;
        this.requestedLevel = requestedLevel;
        this.reason = "";
    }

    public InvalidSubscriptionLevelChangeException(String message, Throwable cause, String currentLevel,
            String requestedLevel) {
        super(message, cause);
        this.currentLevel = currentLevel;
        this.requestedLevel = requestedLevel;
        this.reason = "";
    }

    public InvalidSubscriptionLevelChangeException(String message, String currentLevel, String requestedLevel,
            String reason) {
        super(message);
        this.currentLevel = currentLevel;
        this.requestedLevel = requestedLevel;
        this.reason = reason;
    }

    public InvalidSubscriptionLevelChangeException(String message, Throwable cause, String currentLevel,
            String requestedLevel, String reason) {
        super(message, cause);
        this.currentLevel = currentLevel;
        this.requestedLevel = requestedLevel;
        this.reason = reason;
    }

    public String getCurrentLevel() {
        return currentLevel;
    }

    public String getRequestedLevel() {
        return requestedLevel;
    }

    public String getReason() {
        return reason;
    }
}