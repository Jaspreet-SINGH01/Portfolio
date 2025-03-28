package com.videoflix.users_microservice.config;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecurityException;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtConfig jwtConfig;

    public JwtAuthenticationFilter(JwtConfig jwtConfig) {
        this.jwtConfig = jwtConfig;
    }

    /**
     * Cette méthode est appelée pour chaque requête entrante. Elle extrait le token
     * JWT
     * de l'en-tête "Authorization", le valide et, s'il est valide, définit
     * l'authentification
     * dans le contexte de sécurité de Spring Security.
     *
     * @param request     La requête HTTP entrante.
     * @param response    La réponse HTTP à envoyer.
     * @param filterChain La chaîne de filtres à poursuivre.
     * @throws ServletException Si une erreur de servlet se produit.
     * @throws IOException      Si une erreur d'entrée/sortie se produit.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        // Récupère l'en-tête "Authorization" de la requête.
        String authorizationHeader = request.getHeader("Authorization");

        // Vérifie si l'en-tête "Authorization" est présent et commence par "Bearer ".
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            // Extrait le token JWT de l'en-tête.
            String token = authorizationHeader.substring(7);
            // Valide le token JWT.
            validateToken(token, response);
        }

        // Poursuit la chaîne de filtres.
        filterChain.doFilter(request, response);
    }

    /**
     * Valide le token JWT et, s'il est valide, définit l'authentification dans le
     * contexte de
     * sécurité de Spring Security. En cas d'erreur de validation, une réponse
     * d'erreur est envoyée.
     *
     * @param token    Le token JWT à valider.
     * @param response La réponse HTTP à envoyer en cas d'erreur.
     * @throws IOException Si une erreur d'entrée/sortie se produit lors de l'envoi
     *                     de la réponse d'erreur.
     */
    private void validateToken(String token, HttpServletResponse response) throws IOException {
        try {
            // Parse le token JWT et récupère les claims (informations contenues dans le
            // token).
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(Keys.hmacShaKeyFor(jwtConfig.getSecret().getBytes(StandardCharsets.UTF_8)))
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            // Récupère l'ID de l'utilisateur (subject) du token.
            String userId = claims.getSubject();
            // Si l'ID de l'utilisateur est présent, crée un token d'authentification et le
            // définit dans le contexte de sécurité.
            if (userId != null) {
                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                        userId, null, new ArrayList<>());
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            }
        } catch (ExpiredJwtException e) {
            // Gère l'exception si le token est expiré.
            sendErrorResponse(response, HttpStatus.UNAUTHORIZED, "Token JWT expiré.");
        } catch (MalformedJwtException e) {
            // Gère l'exception si le token est malformé.
            sendErrorResponse(response, HttpStatus.UNAUTHORIZED, "Token JWT malformé.");
        } catch (SecurityException e) {
            // Gère l'exception si la signature du token est invalide.
            sendErrorResponse(response, HttpStatus.UNAUTHORIZED, "Signature JWT invalide.");
        } catch (UnsupportedJwtException e) {
            // Gère l'exception si le type de token n'est pas supporté.
            sendErrorResponse(response, HttpStatus.UNAUTHORIZED, "Type de token JWT non supporté.");
        } catch (IllegalArgumentException e) {
            // Gère l'exception si le token est vide ou nul.
            sendErrorResponse(response, HttpStatus.UNAUTHORIZED, "Token JWT vide ou nul.");
        } catch (Exception e) {
            // Gère toutes les autres exceptions.
            sendErrorResponse(response, HttpStatus.INTERNAL_SERVER_ERROR, "Erreur lors de la validation du token JWT.");
        }
    }

    /**
     * Envoie une réponse d'erreur avec le code de statut et le message spécifiés.
     *
     * @param response La réponse HTTP à envoyer.
     * @param status   Le code de statut HTTP à définir.
     * @param message  Le message d'erreur à inclure dans la réponse.
     * @throws IOException Si une erreur d'entrée/sortie se produit lors de
     *                     l'écriture de la réponse.
     */
    private void sendErrorResponse(HttpServletResponse response, HttpStatus status, String message) throws IOException {
        response.setStatus(status.value());
        response.getWriter().write(message);
    }
}