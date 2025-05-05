package com.videoflix.subscriptions_microservice.exceptions;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler exceptionHandler = new GlobalExceptionHandler();

    // Test pour vérifier la gestion de IllegalArgumentException
    @Test
    void handleIllegalArgumentException_shouldReturnBadRequestWithErrorMessage() {
        // GIVEN : Création d'une exception IllegalArgumentException avec un message
        String errorMessage = "Argument invalide fourni.";
        IllegalArgumentException exception = new IllegalArgumentException(errorMessage);

        // WHEN : Appel de la méthode handleIllegalArgumentException
        ResponseEntity<String> response = exceptionHandler.handleIllegalArgumentException(exception);

        // THEN : Vérification que la réponse a le statut BAD_REQUEST et contient le
        // message d'erreur
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode(),
                "Le statut de la réponse doit être BAD_REQUEST.");
        assertEquals(errorMessage, response.getBody(), "Le corps de la réponse doit contenir le message d'erreur.");
    }

    // Test pour vérifier la gestion de MethodArgumentNotValidException (erreurs de
    // validation)
    @Test
    void handleValidationExceptions_shouldReturnBadRequestWithValidationErrors() {
        // GIVEN : Création d'une exception MethodArgumentNotValidException mockée
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        when(exception.getBindingResult()).thenReturn(bindingResult);

        // Création d'une liste d'erreurs de champ mockées
        FieldError fieldError1 = new FieldError("user", "username", "Le nom d'utilisateur est requis.");
        FieldError fieldError2 = new FieldError("subscription", "levelId", "L'ID du niveau est requis.");
        when(bindingResult.getFieldErrors())
                .thenReturn(Collections.unmodifiableList(Arrays.asList(fieldError1, fieldError2)));

        // WHEN : Appel de la méthode handleValidationExceptions
        ResponseEntity<Map<String, String>> response = exceptionHandler.handleValidationExceptions(exception);

        // THEN : Vérification que la réponse a le statut BAD_REQUEST et contient les
        // erreurs de validation
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode(),
                "Le statut de la réponse doit être BAD_REQUEST.");
        Map<String, String> errors = response.getBody();
        assertNotNull(errors, "Le corps de la réponse ne doit pas être null.");
        assertEquals(2, errors.size(), "Le nombre d'erreurs de validation doit être correct.");
        assertEquals("Le nom d'utilisateur est requis.", errors.get("username"),
                "Le message d'erreur pour 'username' doit être correct.");
        assertEquals("L'ID du niveau est requis.", errors.get("levelId"),
                "Le message d'erreur pour 'levelId' doit être correct.");
    }

    // Test pour vérifier la gestion de SubscriptionNotFoundException
    @Test
    void handleSubscriptionNotFoundException_shouldReturnNotFoundWithErrorMessage() {
        // GIVEN : Création d'une exception SubscriptionNotFoundException avec un
        // message
        String errorMessage = "Abonnement non trouvé avec l'ID spécifié.";
        SubscriptionNotFoundException exception = new SubscriptionNotFoundException(errorMessage);

        // WHEN : Appel de la méthode handleSubscriptionNotFoundException
        ResponseEntity<String> response = exceptionHandler.handleSubscriptionNotFoundException(exception);

        // THEN : Vérification que la réponse a le statut NOT_FOUND et contient le
        // message d'erreur
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode(), "Le statut de la réponse doit être NOT_FOUND.");
        assertEquals(errorMessage, response.getBody(), "Le corps de la réponse doit contenir le message d'erreur.");
    }

    // Test pour vérifier la gestion des exceptions génériques (Exception.class)
    @Test
    void handleGenericException_shouldReturnInternalServerErrorWithMessageAndLog() {
        // GIVEN : Création d'une exception générique
        Exception exception = new RuntimeException("Une erreur inattendue s'est produite.");

        // WHEN : Appel de la méthode handleGenericException
        ResponseEntity<String> response = exceptionHandler.handleGenericException(exception);

        // THEN : Vérification que la réponse a le statut INTERNAL_SERVER_ERROR et
        // contient un message générique
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode(),
                "Le statut de la réponse doit être INTERNAL_SERVER_ERROR.");
        assertEquals("Une erreur interne est survenue.", response.getBody(),
                "Le corps de la réponse doit contenir le message générique.");
        // Note : On ne peut pas facilement tester l'appel à ex.printStackTrace() dans
        // un test unitaire.
        // En pratique, il faudrait vérifier les logs pour s'assurer que l'erreur est
        // bien enregistrée.
    }
}