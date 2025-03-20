package com.videoflix.users_microservice.validation.annotations;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

import com.videoflix.users_microservice.validation.validators.UniqueUsernameValidator;


@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = UniqueUsernameValidator.class)
public @interface UniqueUsername {
    String message() default "Nom d'utilisateur déjà utilisé";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}