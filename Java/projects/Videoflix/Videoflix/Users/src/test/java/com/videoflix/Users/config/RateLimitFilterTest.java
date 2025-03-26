package com.videoflix.Users.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import com.videoflix.users_microservice.config.RateLimitFilter;

import java.io.IOException;
import java.io.PrintWriter;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) // Active les annotations Mockito
class RateLimitFilterTest {

    @Mock
    private ServletRequest servletRequest; // Mock pour ServletRequest

    @Mock
    private HttpServletResponse servletResponse; // Mock pour HttpServletResponse

    @Mock
    private FilterChain filterChain; // Mock pour FilterChain

    @Mock
    private PrintWriter writer; // Mock pour PrintWriter

    private RateLimitFilter rateLimitFilter; // Instance du filtre à tester

    @BeforeEach
    void setUp() throws IOException {
        rateLimitFilter = new RateLimitFilter(); // Initialisation du filtre
        when(servletResponse.getWriter()).thenReturn(writer); // Configuration du mock pour simuler l'obtention du
                                                              // PrintWriter
    }

    @Test
    void doFilter_ShouldAllowRequest_WhenWithinLimit() throws ServletException, IOException {
        // Simule les 10 premières requêtes (dans la limite)
        for (int i = 0; i < 10; i++) {
            rateLimitFilter.doFilter(servletRequest, servletResponse, filterChain);
        }

        // Vérification que le filtre suivant est appelé 10 fois
        verify(filterChain, times(10)).doFilter(servletRequest, servletResponse);

        // Vérification qu'aucun message d'erreur n'est envoyé
        verify(servletResponse, never()).setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        verify(writer, never()).write(anyString());
    }

    @Test
    void doFilter_ShouldRejectRequest_WhenExceedsLimit() throws ServletException, IOException {
        // Simule 11 requêtes (dépasse la limite)
        for (int i = 0; i < 11; i++) {
            rateLimitFilter.doFilter(servletRequest, servletResponse, filterChain);
        }

        // Vérification que le filtre suivant est appelé 10 fois
        verify(filterChain, times(10)).doFilter(servletRequest, servletResponse);

        // Vérification que la réponse d'erreur est envoyée pour la 11ème requête
        verify(servletResponse).setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        verify(writer).write("Too many requests");
    }

    @Test
    void doFilter_ShouldHandleIOException() throws ServletException, IOException {
        // Configuration du mock pour lancer une IOException lors de l'obtention du
        // PrintWriter
        when(servletResponse.getWriter()).thenThrow(new IOException("Test IOException"));

        // Simule 11 requêtes (dépasse la limite)
        for (int i = 0; i < 11; i++) {
            rateLimitFilter.doFilter(servletRequest, servletResponse, filterChain);
        }

        // Vérification que le filtre suivant est appelé 10 fois
        verify(filterChain, times(10)).doFilter(servletRequest, servletResponse);

        // Vérification que le code de statut est toujours défini
        verify(servletResponse).setStatus(HttpStatus.TOO_MANY_REQUESTS.value());

        // Vérification qu'aucun message n'est écrit (puisque l'IOException est levée
        // avant)
        verify(writer, never()).write(anyString());
    }
}