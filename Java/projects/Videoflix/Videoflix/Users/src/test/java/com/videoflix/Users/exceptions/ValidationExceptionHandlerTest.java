package com.videoflix.Users.exceptions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import com.videoflix.users_microservice.exceptions.ValidationExceptionHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

class ValidationExceptionHandlerTest {

    private ValidationExceptionHandler validationExceptionHandler; // Instance de ValidationExceptionHandler à tester

    @Mock
    private MethodArgumentNotValidException methodArgumentNotValidException; // Mock pour
                                                                             // MethodArgumentNotValidException

    @Mock
    private BindingResult bindingResult; // Mock pour BindingResult

    @BeforeEach
    void setUp() {
        // Initialisation des mocks et de l'instance de ValidationExceptionHandler avant
        // chaque test
        MockitoAnnotations.openMocks(this);
        validationExceptionHandler = new ValidationExceptionHandler();
    }

    @Test
    void handleValidationExceptions_ShouldReturnBadRequestWithErrors() {
        // Teste que la méthode handleValidationExceptions() renvoie une réponse
        // BadRequest avec les erreurs de validation

        // Création d'une liste de FieldError pour simuler les erreurs de validation
        List<FieldError> fieldErrors = new ArrayList<>();
        fieldErrors.add(new FieldError("userDTO", "username", "Le nom d'utilisateur est requis"));
        fieldErrors.add(new FieldError("userDTO", "email", "L'email est invalide"));

        // Configuration des mocks pour simuler les erreurs de validation
        when(methodArgumentNotValidException.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(fieldErrors);

        // Appel de la méthode handleValidationExceptions()
        ResponseEntity<Map<String, String>> responseEntity = validationExceptionHandler
                .handleValidationExceptions(methodArgumentNotValidException);

        // Vérification que le code de statut est BAD_REQUEST
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());

        // Vérification que le corps de la réponse contient les erreurs de validation
        assertNotNull(responseEntity.getBody());
        assertEquals(2, responseEntity.getBody().size());
        assertEquals("Le nom d'utilisateur est requis", responseEntity.getBody().get("username"));
        assertEquals("L'email est invalide", responseEntity.getBody().get("email"));
    }

    @Test
    void handleValidationExceptions_ShouldReturnEmptyErrors_WhenNoErrors() {
        // Teste que la méthode handleValidationExceptions() renvoie une réponse
        // BadRequest avec un corps vide lorsqu'il n'y a pas d'erreurs

        // Configuration des mocks pour simuler l'absence d'erreurs de validation
        when(methodArgumentNotValidException.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(new ArrayList<>());

        // Appel de la méthode handleValidationExceptions()
        ResponseEntity<Map<String, String>> responseEntity = validationExceptionHandler
                .handleValidationExceptions(methodArgumentNotValidException);

        // Vérification que le code de statut est BAD_REQUEST
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());

        // Vérification que le corps de la réponse est vide
        assertNotNull(responseEntity.getBody());
        assertEquals(0, responseEntity.getBody().size());
    }
}