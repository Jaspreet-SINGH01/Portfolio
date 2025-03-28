package com.videoflix.users_microservice.config;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    /**
     * Cette méthode est appelée lorsque l'authentification d'une requête échoue.
     * Elle renvoie une réponse HTTP 401 (Non autorisé) avec un message indiquant
     * que le token JWT est invalide ou absent.
     *
     * @param request       La requête HTTP entrante.
     * @param response      La réponse HTTP à envoyer.
     * @param authException L'exception d'authentification levée.
     * @throws IOException      Si une erreur d'entrée/sortie se produit lors de
     *                          l'écriture de la réponse.
     * @throws ServletException Si une erreur de servlet se produit.
     */

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException authException) throws IOException, ServletException {

        // Définit le code de statut de la réponse HTTP à 401 (Non autorisé).
        response.setStatus(HttpStatus.UNAUTHORIZED.value());

        // Écrit le message d'erreur dans le corps de la réponse.
        response.getWriter().write("Token JWT invalide ou absent.");
    }
}