package com.videoflix.users_microservice.exceptions;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class ValidationExceptionHandler {

    /**
     * Gestionnaire d'exceptions pour les erreurs de validation
     * (MethodArgumentNotValidException).
     * Cette classe est annotée avec @ControllerAdvice, ce qui signifie qu'elle
     * intercepte les exceptions
     * lancées par les contrôleurs de l'application.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        // Crée une Map pour stocker les erreurs de validation.
        Map<String, String> errors = new HashMap<>();

        // Parcourt toutes les erreurs de champ trouvées dans l'exception.
        ex.getBindingResult().getFieldErrors().forEach(error ->
        // Ajoute chaque erreur à la Map, avec le nom du champ comme clé et le message
        // d'erreur par défaut comme valeur.
        errors.put(error.getField(), error.getDefaultMessage()));

        // Renvoie une réponse HTTP 400 (Bad Request) avec la Map des erreurs de
        // validation dans le corps de la réponse.
        return ResponseEntity.badRequest().body(errors);
    }
}