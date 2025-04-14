package com.videoflix.subscriptions_microservice.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
public class PaymentServiceUnavailableException extends CommunicationException {

    public PaymentServiceUnavailableException(String message) {
        super(message);
    }

    public PaymentServiceUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }

    public PaymentServiceUnavailableException(String message, String remoteServiceUrl, String method) {
        super(message, remoteServiceUrl, method);
    }

    public PaymentServiceUnavailableException(String message, Throwable cause, String remoteServiceUrl, String method) {
        super(message, cause, remoteServiceUrl, method);
    }
}