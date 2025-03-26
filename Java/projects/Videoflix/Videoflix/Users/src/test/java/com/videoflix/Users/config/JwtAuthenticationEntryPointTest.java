package com.videoflix.Users.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;

import com.videoflix.users_microservice.config.JwtAuthenticationEntryPoint;

import java.io.IOException;
import java.io.PrintWriter;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) // Active les annotations Mockito
class JwtAuthenticationEntryPointTest {

    @Mock
    private HttpServletRequest request; // Mock pour HttpServletRequest

    @Mock
    private HttpServletResponse response; // Mock pour HttpServletResponse

    @Mock
    private AuthenticationException authException; // Mock pour AuthenticationException

    @Mock
    private PrintWriter writer; // Mock pour PrintWriter

    private JwtAuthenticationEntryPoint entryPoint; // Instance du point d'entrée à tester

    @BeforeEach
    void setUp() throws IOException {
        entryPoint = new JwtAuthenticationEntryPoint(); // Initialisation du point d'entrée
        when(response.getWriter()).thenReturn(writer); // Configuration du mock pour simuler l'obtention du PrintWriter
    }

    @Test
    void commence_ShouldSetUnauthorizedStatusAndWriteMessage() throws IOException, ServletException {
        // Appel de la méthode à tester
        entryPoint.commence(request, response, authException);

        // Vérification que le code de statut HTTP est 401 (UNAUTHORIZED)
        verify(response).setStatus(HttpStatus.UNAUTHORIZED.value());

        // Vérification que le message "Token JWT invalide ou absent." est écrit dans la
        // réponse
        verify(writer).write("Token JWT invalide ou absent.");
    }

    @Test
    void commence_ShouldHandleIOException() throws IOException, ServletException {
        // Configuration du mock pour lancer une IOException lors de l'obtention du
        // PrintWriter
        when(response.getWriter()).thenThrow(new IOException("Test IOException"));

        // Appel de la méthode à tester et vérification qu'elle ne lance pas d'exception
        // (ou gère l'exception)
        entryPoint.commence(request, response, authException);

        // Vérification que le code de statut HTTP est 401 (UNAUTHORIZED)
        verify(response).setStatus(HttpStatus.UNAUTHORIZED.value());

        // Vérification qu'aucun message n'est écrit (puisque l'IOException est lancée
        // avant)
        verify(writer, never()).write(anyString());
    }
}