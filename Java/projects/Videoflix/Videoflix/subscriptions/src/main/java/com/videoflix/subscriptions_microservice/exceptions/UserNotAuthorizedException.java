package com.videoflix.subscriptions_microservice.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class UserNotAuthorizedException extends RuntimeException {

    private final Long userId;
    private final Long resourceId;
    private final String operation;

    public UserNotAuthorizedException(String message) {
        super(message);
        this.userId = null;
        this.resourceId = null;
        this.operation = "";
    }

    public UserNotAuthorizedException(String message, Throwable cause) {
        super(message, cause);
        this.userId = null;
        this.resourceId = null;
        this.operation = "";
    }

    public UserNotAuthorizedException(String message, Long userId, Long resourceId, String operation) {
        super(message);
        this.userId = userId;
        this.resourceId = resourceId;
        this.operation = operation;
    }

    public UserNotAuthorizedException(String message, Throwable cause, Long userId, Long resourceId, String operation) {
        super(message, cause);
        this.userId = userId;
        this.resourceId = resourceId;
        this.operation = operation;
    }

    public Long getUserId() {
        return userId;
    }

    public Long getResourceId() {
        return resourceId;
    }

    public String getOperation() {
        return operation;
    }
}