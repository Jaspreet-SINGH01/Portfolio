package com.videoflix.subscriptions_microservice.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE) // Indique que le service externe est temporairement indisponible
public class CommunicationException extends RuntimeException {

    private final String remoteServiceUrl;
    private final String method;

    public CommunicationException(String message) {
        super(message);
        this.remoteServiceUrl = "";
        this.method = "";
    }

    public CommunicationException(String message, Throwable cause) {
        super(message, cause);
        this.remoteServiceUrl = "";
        this.method = "";
    }

    public CommunicationException(String message, String remoteServiceUrl, String method) {
        super(message);
        this.remoteServiceUrl = remoteServiceUrl;
        this.method = method;
    }

    public CommunicationException(String message, Throwable cause, String remoteServiceUrl, String method) {
        super(message, cause);
        this.remoteServiceUrl = remoteServiceUrl;
        this.method = method;
    }

    public String getRemoteServiceUrl() {
        return remoteServiceUrl;
    }

    public String getMethod() {
        return method;
    }
}