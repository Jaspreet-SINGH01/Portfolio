package com.videoflix.users_microservice.config;

import com.videoflix.users_microservice.services.LoginAttemptService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final LoginAttemptService loginAttemptService;

    /**
     * Constructeur pour SecurityConfig.
     * Injecte les dépendances nécessaires : JwtAuthenticationEntryPoint,
     * JwtAuthenticationFilter et LoginAttemptService.
     *
     * @param jwtAuthenticationEntryPoint Le point d'entrée pour la gestion des
     *                                    erreurs d'authentification JWT.
     * @param jwtAuthenticationFilter     Le filtre pour l'authentification JWT.
     * @param loginAttemptService         Le service pour gérer les tentatives de
     *                                    connexion.
     */
    public SecurityConfig(JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint,
            JwtAuthenticationFilter jwtAuthenticationFilter,
            LoginAttemptService loginAttemptService) {
        this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.loginAttemptService = loginAttemptService;
    }

    /**
     * Crée un bean PasswordEncoder pour encoder les mots de passe.
     *
     * @return Un BCryptPasswordEncoder.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Crée un bean AuthenticationManager pour gérer l'authentification.
     *
     * @param userDetailsService Le service pour charger les détails de
     *                           l'utilisateur.
     * @param passwordEncoder    L'encodeur de mot de passe.
     * @return Un ProviderManager configuré avec un DaoAuthenticationProvider.
     */
    @Bean
    public AuthenticationManager authenticationManager(UserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setUserDetailsService(userDetailsService);
        authenticationProvider.setPasswordEncoder(passwordEncoder);
        return new ProviderManager(Arrays.asList(authenticationProvider));
    }

    /**
     * Configure la chaîne de filtres de sécurité.
     *
     * @param http HttpSecurity pour configurer la sécurité HTTP.
     * @return Un SecurityFilterChain configuré.
     * @throws Exception Si une erreur se produit lors de la configuration.
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Désactive la protection CSRF.
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/users").permitAll() // Autorise l'accès à /users sans authentification.
                        .requestMatchers(HttpMethod.GET, "/users/**").hasRole("ADMIN") // Autorise l'accès GET à
                                                                                       // /users/** uniquement aux
                                                                                       // administrateurs.
                        .anyRequest().authenticated()) // Toutes les autres requêtes nécessitent une authentification.
                .requiresChannel(channel -> channel.anyRequest().requiresSecure()) // Force HTTPS pour toutes les
                                                                                   // requêtes.
                .oauth2Login(oauth2 -> oauth2.defaultSuccessUrl("/oauth2/success")) // Configure l'authentification
                                                                                    // OAuth2.
                .httpBasic(basic -> {
                }) // Configure l'authentification HTTP Basic (vide, peut être personnalisé).
                .exceptionHandling(exceptionHandling -> exceptionHandling
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint) // Configure le point d'entrée pour les
                                                                               // erreurs d'authentification.
                        .accessDeniedHandler(accessDeniedHandler())) // Configure le gestionnaire pour les accès
                                                                     // refusés.
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class) // Ajoute le
                                                                                                      // filtre JWT
                                                                                                      // avant le filtre
                                                                                                      // d'authentification
                                                                                                      // par nom
                                                                                                      // d'utilisateur/mot
                                                                                                      // de passe.
                .formLogin(form -> form
                        .successHandler((request, response, authentication) -> { // Configure le gestionnaire de succès
                                                                                 // de connexion.
                            loginAttemptService.resetLoginAttempts(authentication.getName()); // Réinitialise les
                                                                                              // tentatives de connexion
                                                                                              // après un succès.
                            response.sendRedirect("/"); // Redirige vers la page d'accueil.
                        })
                        .failureHandler((request, response, exception) -> { // Configure le gestionnaire d'échec de
                                                                            // connexion.
                            loginAttemptService.recordFailedLogin(request.getParameter("username")); // Enregistre
                                                                                                     // l'échec de
                                                                                                     // connexion.
                            response.sendRedirect("/login?error"); // Redirige vers la page de connexion avec une
                                                                   // erreur.
                        }));

        return http.build();
    }

    /**
     * Crée un bean AccessDeniedHandler pour gérer les accès refusés.
     *
     * @return Un AccessDeniedHandler qui renvoie une réponse 403 (FORBIDDEN).
     */
    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return (request, response, accessDeniedException) -> {
            response.setStatus(HttpStatus.FORBIDDEN.value());
            response.getWriter().write("Accès refusé.");
        };
    }

    /**
     * Injecte la valeur de la propriété security.maxAttempts.
     */
    @Value("${security.maxAttempts:5}")
    private int maxAttempts;

    /**
     * Obtient le nombre maximal de tentatives de connexion autorisées.
     *
     * @return Le nombre maximal de tentatives de connexion.
     */
    public int getMaxAttempts() {
        return maxAttempts;
    }

    /**
     * Injecte la valeur de la propriété security.blockingTime.
     */
    @Value("${security.blockingTime:5}")
    public int blockingTime;
}