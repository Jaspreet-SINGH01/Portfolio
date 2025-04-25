package com.videoflix.subscriptions_microservice.exceptions;

public class StripeIntegrationException extends RuntimeException {
    public StripeIntegrationException(String message, Throwable cause) {
        super(message, cause);
    }

    public StripeIntegrationException(String message) {
        super(message);
    }
}