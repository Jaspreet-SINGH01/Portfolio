package com.videoflix.subscriptions_microservice.validations;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Arrays;
import java.util.List;

public class SubscriptionTypeValidator implements ConstraintValidator<ValidSubscriptionLevel, String> {

    private List<String> allowedTypes;

    @Override
    public void initialize(ValidSubscriptionLevel constraintAnnotation) {
        allowedTypes = Arrays.asList("Basic", "Premium", "Ultra"); // Exemple de types valides
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return value == null || allowedTypes.contains(value);
    }
}