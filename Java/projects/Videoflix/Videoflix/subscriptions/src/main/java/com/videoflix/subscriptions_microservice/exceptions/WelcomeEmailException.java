package com.videoflix.subscriptions_microservice.exceptions;

public class WelcomeEmailException extends RuntimeException {
    public WelcomeEmailException(String message, Throwable cause) {
        super(message, cause);
    }
}