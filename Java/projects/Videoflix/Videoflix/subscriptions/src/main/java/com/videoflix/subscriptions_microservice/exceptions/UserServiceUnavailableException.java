package com.videoflix.subscriptions_microservice.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
public class UserServiceUnavailableException extends CommunicationException {

    public UserServiceUnavailableException(String message) {
        super(message);
    }

    public UserServiceUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }

    public UserServiceUnavailableException(String message, String remoteServiceUrl, String method) {
        super(message, remoteServiceUrl, method);
    }

    public UserServiceUnavailableException(String message, Throwable cause, String remoteServiceUrl, String method) {
        super(message, cause, remoteServiceUrl, method);
    }
}