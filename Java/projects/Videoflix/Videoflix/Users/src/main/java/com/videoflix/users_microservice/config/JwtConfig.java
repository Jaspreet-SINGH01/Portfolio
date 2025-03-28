package com.videoflix.users_microservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Configuration
@ConfigurationProperties(prefix = "application.jwt")
@Validated
public class JwtConfig {
    private String secret;
    private long expiration;

    /**
     * Obtient la clé secrète utilisée pour signer les tokens JWT.
     *
     * @return La clé secrète.
     */
    public String getSecret() {
        return secret;
    }

    /**
     * Définit la clé secrète utilisée pour signer les tokens JWT.
     *
     * @param secret La clé secrète à définir.
     */
    public void setSecret(String secret) {
        this.secret = secret;
    }

    /**
     * Obtient la durée d'expiration des tokens JWT en millisecondes.
     *
     * @return La durée d'expiration en millisecondes.
     */
    public long getExpiration() {
        return expiration;
    }

    /**
     * Définit la durée d'expiration des tokens JWT en millisecondes.
     *
     * @param expiration La durée d'expiration à définir en millisecondes.
     */
    public void setExpiration(long expiration) {
        this.expiration = expiration;
    }
}