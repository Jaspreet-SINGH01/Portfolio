package com.videoflix.Users.exceptions;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.videoflix.users_microservice.exceptions.UserAlreadyExistsException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserAlreadyExistsExceptionTest {

    @Test
    void userAlreadyExistsException_ShouldCreateExceptionWithCorrectMessage() {
        // Teste que l'exception UserAlreadyExistsException est créée avec le message
        // correct

        // Message de test
        String errorMessage = "Utilisateur déjà existant test";

        // Création de l'exception
        UserAlreadyExistsException exception = new UserAlreadyExistsException(errorMessage);

        // Vérification que le message de l'exception est correct
        assertEquals(errorMessage, exception.getMessage());
    }

    @Test
    void userAlreadyExistsException_ShouldBeRuntimeException() {
        // Teste que UserAlreadyExistsException est une sous-classe de RuntimeException

        // Vérification que UserAlreadyExistsException est une sous-classe de
        // RuntimeException
        assertTrue(RuntimeException.class.isAssignableFrom(UserAlreadyExistsException.class));
    }

    @Test
    void userAlreadyExistsException_ShouldBeThrownAndCaught() {
        // Teste que UserAlreadyExistsException peut être lancée et capturée

        // Message de test
        String errorMessage = "Utilisateur déjà existant test";

        // Vérification que l'exception est lancée et capturée correctement
        UserAlreadyExistsException exception = assertThrows(UserAlreadyExistsException.class, () -> {
            throw new UserAlreadyExistsException(errorMessage);
        });

        // Vérification que le message de l'exception capturée est correct
        assertEquals(errorMessage, exception.getMessage());
    }

    @Test
    void userAlreadyExistsException_ShouldHaveCorrectResponseStatus() {
        // Teste que l'exception UserAlreadyExistsException a le code de statut HTTP
        // correct

        // Récupération de l'annotation ResponseStatus de la classe
        // UserAlreadyExistsException
        ResponseStatus responseStatus = UserAlreadyExistsException.class.getAnnotation(ResponseStatus.class);

        // Vérification que l'annotation ResponseStatus est présente
        assertNotNull(responseStatus);

        // Vérification que le code de statut HTTP est CONFLICT
        assertEquals(HttpStatus.CONFLICT, responseStatus.value());
    }
}