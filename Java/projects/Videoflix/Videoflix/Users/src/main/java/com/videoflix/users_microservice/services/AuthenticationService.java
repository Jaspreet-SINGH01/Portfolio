package com.videoflix.users_microservice.services;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.videoflix.users_microservice.config.JwtConfig;
import com.videoflix.users_microservice.entities.User;
import com.videoflix.users_microservice.exceptions.AuthenticationException;
import com.videoflix.users_microservice.repositories.UserRepository;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtConfig jwtConfig;

    /**
     * Constructeur pour AuthenticationService.
     * Injecte les dépendances nécessaires : UserRepository, PasswordEncoder et
     * JwtConfig.
     *
     * @param userRepository  Le repository pour accéder aux données des
     *                        utilisateurs.
     * @param passwordEncoder L'encodeur de mot de passe pour vérifier les mots de
     *                        passe.
     * @param jwtConfig       La configuration JWT pour générer les tokens.
     */
    public AuthenticationService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtConfig jwtConfig) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtConfig = jwtConfig;
    }

    /**
     * Authentifie un utilisateur en vérifiant les informations de connexion et
     * génère un token JWT.
     *
     * @param username Le nom d'utilisateur de l'utilisateur.
     * @param password Le mot de passe de l'utilisateur.
     * @return Le token JWT généré en cas d'authentification réussie.
     * @throws AuthenticationException Si le nom d'utilisateur ou le mot de passe
     *                                 est incorrect.
     */
    public String authenticate(String username, String password) {
        // Recherche l'utilisateur par nom d'utilisateur.
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AuthenticationException("Nom d'utilisateur ou mot de passe incorrect."));

        // Vérifie si le mot de passe fourni correspond au mot de passe haché stocké.
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new AuthenticationException("Nom d'utilisateur ou mot de passe incorrect.");
        }

        // Génère et renvoie le token JWT pour l'utilisateur authentifié.
        return generateToken(user);
    }

    /**
     * Génère un token JWT pour un utilisateur donné.
     *
     * @param user L'utilisateur pour lequel générer le token.
     * @return Le token JWT généré.
     */
    private String generateToken(User user) {
        // Crée une date pour l'heure actuelle.
        Date now = new Date();
        // Crée une date pour l'expiration du token.
        Date expiryDate = new Date(now.getTime() + jwtConfig.getExpiration());
        // Crée une clé secrète à partir de la clé secrète configurée.
        SecretKey key = Keys.hmacShaKeyFor(jwtConfig.getSecret().getBytes(StandardCharsets.UTF_8));

        // Construit et signe le token JWT.
        return Jwts.builder()
                .setSubject(user.getId().toString()) // Définit l'ID de l'utilisateur comme sujet du token.
                .setIssuedAt(now) // Définit la date d'émission du token.
                .setExpiration(expiryDate) // Définit la date d'expiration du token.
                .signWith(key) // Signe le token avec la clé secrète.
                .compact(); // Compacte le token en une chaîne JWT.
    }
}