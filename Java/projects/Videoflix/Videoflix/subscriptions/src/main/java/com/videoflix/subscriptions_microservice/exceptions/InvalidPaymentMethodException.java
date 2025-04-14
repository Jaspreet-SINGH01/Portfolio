package com.videoflix.subscriptions_microservice.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST) // Le statut BAD_REQUEST indique que la requête du client était invalide
public class InvalidPaymentMethodException extends RuntimeException {

    private final String providedPaymentMethod;

    public InvalidPaymentMethodException(String message) {
        super(message);
        this.providedPaymentMethod = "";
    }

    public InvalidPaymentMethodException(String message, Throwable cause) {
        super(message, cause);
        this.providedPaymentMethod = "";
    }

    public InvalidPaymentMethodException(String message, String providedPaymentMethod) {
        super(message);
        this.providedPaymentMethod = providedPaymentMethod;
    }

    public InvalidPaymentMethodException(String message, Throwable cause, String providedPaymentMethod) {
        super(message, cause);
        this.providedPaymentMethod = providedPaymentMethod;
    }

    public String getProvidedPaymentMethod() {
        return providedPaymentMethod;
    }
}