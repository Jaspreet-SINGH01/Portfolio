package com.videoflix.Users.config;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecurityException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import static org.junit.jupiter.api.Assertions.*;

import com.videoflix.users_microservice.config.JwtAuthenticationFilter;
import com.videoflix.users_microservice.config.JwtConfig;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) // Active les annotations Mockito
class JwtAuthenticationFilterTest {

    @Mock
    private JwtConfig jwtConfig; // Mock pour JwtConfig

    @Mock
    private HttpServletRequest request; // Mock pour HttpServletRequest

    @Mock
    private HttpServletResponse response; // Mock pour HttpServletResponse

    @Mock
    private FilterChain filterChain; // Mock pour FilterChain

    @Mock
    private Claims claims; // Mock pour Claims

    @Mock
    private Jws<Claims> jwsClaims; // Mock pour Jws<Claims>

    @Mock
    private PrintWriter writer; // Mock pour PrintWriter

    private JwtAuthenticationFilter filter; // Instance du filtre à tester

    private String secretKey;
    private String validToken;
    private String invalidToken;

    @BeforeEach
    void setUp() throws IOException {
        secretKey = "secretsecretsecretsecretsecretsecretsecretsecret";
        validToken = Jwts.builder()
                .setSubject("123")
                .signWith(Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8)))
                .compact();
        invalidToken = "invalid.token";

        when(jwtConfig.getSecret()).thenReturn(secretKey);
        when(response.getWriter()).thenReturn(writer);

        filter = new JwtAuthenticationFilter(jwtConfig);
    }

    @Test
    void doFilterInternal_ShouldSetAuthentication_WhenValidToken() throws ServletException, IOException {
        // Configuration du mock pour simuler un token valide
        when(request.getHeader("Authorization")).thenReturn("Bearer " + validToken);
        when(Jwts.parserBuilder().setSigningKey(Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8))).build()
                .parseClaimsJws(anyString())).thenReturn(jwsClaims);
        when(jwsClaims.getBody()).thenReturn(claims);
        when(claims.getSubject()).thenReturn("123");

        // Appel de la méthode à tester
        filter.doFilter(request, response, filterChain);

        // Vérification que l'authentification est définie dans le SecurityContextHolder
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());

        // Vérification que le filtre suivant est appelé
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_ShouldNotSetAuthentication_WhenInvalidToken() throws ServletException, IOException {
        // Configuration du mock pour simuler un token invalide
        when(request.getHeader("Authorization")).thenReturn("Bearer " + invalidToken);
        when(Jwts.parserBuilder().setSigningKey(Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8))).build()
                .parseClaimsJws(anyString())).thenThrow(new MalformedJwtException("Invalid token"));

        // Appel de la méthode à tester
        filter.doFilter(request, response, filterChain);

        // Vérification que l'authentification n'est pas définie dans le
        // SecurityContextHolder
        assertNull(SecurityContextHolder.getContext().getAuthentication());

        // Vérification que le filtre suivant est appelé
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_ShouldSendErrorResponse_WhenExpiredToken() throws ServletException, IOException {
        // Configuration du mock pour simuler un token expiré
        when(request.getHeader("Authorization")).thenReturn("Bearer " + invalidToken);
        when(Jwts.parserBuilder().setSigningKey(Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8))).build()
                .parseClaimsJws(anyString())).thenThrow(new ExpiredJwtException(null, null, "Expired token"));

        // Appel de la méthode à tester
        filter.doFilter(request, response, filterChain);

        // Vérification que la réponse d'erreur est envoyée
        verify(response).setStatus(HttpStatus.UNAUTHORIZED.value());
        verify(writer).write("Token JWT expiré.");
    }

    @Test
    void doFilterInternal_ShouldSendErrorResponse_WhenMalformedToken() throws ServletException, IOException {
        // Configuration du mock pour simuler un token malformé
        when(request.getHeader("Authorization")).thenReturn("Bearer " + invalidToken);
        when(Jwts.parserBuilder().setSigningKey(Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8))).build()
                .parseClaimsJws(anyString())).thenThrow(new MalformedJwtException("Malformed token"));

        // Appel de la méthode à tester
        filter.doFilter(request, response, filterChain);

        // Vérification que la réponse d'erreur est envoyée
        verify(response).setStatus(HttpStatus.UNAUTHORIZED.value());
        verify(writer).write("Token JWT malformé.");
    }

    @Test
    void doFilterInternal_ShouldSendErrorResponse_WhenInvalidSignature() throws ServletException, IOException {
        // Configuration du mock pour simuler une signature invalide
        when(request.getHeader("Authorization")).thenReturn("Bearer " + invalidToken);
        when(Jwts.parserBuilder().setSigningKey(Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8))).build()
                .parseClaimsJws(anyString())).thenThrow(new SecurityException("Invalid signature"));

        // Appel de la méthode à tester
        filter.doFilter(request, response, filterChain);

        // Vérification que la réponse d'erreur est envoyée
        verify(response).setStatus(HttpStatus.UNAUTHORIZED.value());
        verify(writer).write("Signature JWT invalide.");
    }

    @Test
    void doFilterInternal_ShouldSendErrorResponse_WhenUnsupportedToken() throws ServletException, IOException {
        // Configuration du mock pour simuler un token non supporté
        when(request.getHeader("Authorization")).thenReturn("Bearer " + invalidToken);
        when(Jwts.parserBuilder().setSigningKey(Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8))).build()
                .parseClaimsJws(anyString())).thenThrow(new UnsupportedJwtException("Unsupported token"));

        // Appel de la méthode à tester
        filter.doFilter(request, response, filterChain);

        // Vérification que la réponse d'erreur est envoyée
        verify(response).setStatus(HttpStatus.UNAUTHORIZED.value());
        verify(writer).write("Type de token JWT non supporté.");
    }

    @Test
    void doFilterInternal_ShouldSendErrorResponse_WhenIllegalArgument() throws ServletException, IOException {
        // Configuration du mock pour simuler un token vide ou nul
        when(request.getHeader("Authorization")).thenReturn("Bearer " + invalidToken);
        when(Jwts.parserBuilder().setSigningKey(Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8))).build()
                .parseClaimsJws(anyString())).thenThrow(new IllegalArgumentException("Empty or null token"));

        // Appel de la méthode à tester
        filter.doFilter(request, response, filterChain);

        // Vérification que la réponse d'erreur est envoyée
        verify(response).setStatus(HttpStatus.UNAUTHORIZED.value());
        verify(writer).write("Token JWT vide ou nul.");
    }

    @Test
    void doFilterInternal_ShouldSendErrorResponse_WhenGenericException() throws ServletException, IOException {
        // Configuration du mock pour simuler une exception générique
        when(request.getHeader("Authorization")).thenReturn("Bearer " + invalidToken);
        when(Jwts.parserBuilder().setSigningKey(Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8))).build()
                .parseClaimsJws(anyString())).thenThrow(new RuntimeException("Generic exception"));

        // Appel de la méthode à tester
        filter.doFilter(request, response, filterChain);

        // Vérification que la réponse d'erreur est envoyée
        verify(response).setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        verify(writer).write("Erreur lors de la validation du token JWT.");
    }
}