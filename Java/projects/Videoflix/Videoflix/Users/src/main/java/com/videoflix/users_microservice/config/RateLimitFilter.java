package com.videoflix.users_microservice.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.time.Duration;

@Component
public class RateLimitFilter implements Filter {

    private final Bucket bucket;

    /**
     * Constructeur pour RateLimitFilter.
     * Initialise un Bucket Bucket4j avec une limite de 10 requêtes par minute.
     */
    public RateLimitFilter() {
        // Définit la limite de bande passante : 10 requêtes par minute.
        Bandwidth limit = Bandwidth.builder()
                .capacity(10) // Capacité totale du bucket : 10 jetons.
                .refillGreedy(10, Duration.ofMinutes(1)) // Remplissage : 10 jetons toutes les minutes.
                .build();

        // Crée un Bucket avec la limite définie.
        this.bucket = Bucket.builder().addLimit(limit).build();
    }

    /**
     * Filtre les requêtes entrantes pour appliquer une limitation de débit.
     * Si le bucket a suffisamment de jetons, la requête est autorisée à passer.
     * Sinon, une réponse HTTP 429 (Trop de requêtes) est envoyée.
     *
     * @param servletRequest  La requête servlet.
     * @param servletResponse La réponse servlet.
     * @param filterChain     La chaîne de filtres.
     * @throws IOException      Si une erreur d'entrée/sortie se produit.
     * @throws ServletException Si une erreur de servlet se produit.
     */
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        // Tente de consommer un jeton du bucket.
        if (bucket.tryConsume(1)) {
            // Si la consommation réussit (il y a un jeton disponible), la requête est
            // autorisée à passer.
            filterChain.doFilter(servletRequest, servletResponse);
        } else {
            // Si la consommation échoue (il n'y a pas de jeton disponible), la requête est
            // rejetée.
            HttpServletResponse httpResponse = (HttpServletResponse) servletResponse;
            // Définit le code de statut de la réponse à 429 (Trop de requêtes).
            httpResponse.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            // Écrit le message d'erreur dans le corps de la réponse.
            httpResponse.getWriter().write("Too many requests");
        }
    }
}