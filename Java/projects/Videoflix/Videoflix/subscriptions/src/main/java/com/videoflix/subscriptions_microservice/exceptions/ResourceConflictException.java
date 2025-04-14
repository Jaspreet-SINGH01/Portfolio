package com.videoflix.subscriptions_microservice.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class ResourceConflictException extends RuntimeException {

    private final String resourceId;
    private final String conflictingField;
    private final transient Object conflictingValue;

    public ResourceConflictException(String message) {
        super(message);
        this.resourceId = "";
        this.conflictingField = "";
        this.conflictingValue = new Object();
    }

    public ResourceConflictException(String message, Throwable cause) {
        super(message, cause);
        this.resourceId = "";
        this.conflictingField = "";
        this.conflictingValue = new Object();
    }

    public ResourceConflictException(String message, String resourceId, String conflictingField,
            Object conflictingValue) {
        super(message);
        this.resourceId = resourceId;
        this.conflictingField = conflictingField;
        this.conflictingValue = conflictingValue;
    }

    public ResourceConflictException(String message, Throwable cause, String resourceId, String conflictingField,
            Object conflictingValue) {
        super(message, cause);
        this.resourceId = resourceId;
        this.conflictingField = conflictingField;
        this.conflictingValue = conflictingValue;
    }

    public String getResourceId() {
        return resourceId;
    }

    public String getConflictingField() {
        return conflictingField;
    }

    public Object getConflictingValue() {
        return conflictingValue;
    }
}