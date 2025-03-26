package com.videoflix.Users.config;

import com.videoflix.users_microservice.config.JwtAuthenticationEntryPoint;
import com.videoflix.users_microservice.config.JwtAuthenticationFilter;
import com.videoflix.users_microservice.config.SecurityConfig;
import com.videoflix.users_microservice.services.LoginAttemptService;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.DefaultSecurityFilterChain;

import java.io.IOException;
import java.io.PrintWriter;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class) // Active les annotations Mockito
class SecurityConfigTest {

    @Mock
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint; // Mock pour JwtAuthenticationEntryPoint

    @Mock
    private JwtAuthenticationFilter jwtAuthenticationFilter; // Mock pour JwtAuthenticationFilter

    @Mock
    private LoginAttemptService loginAttemptService; // Mock pour LoginAttemptService

    @Mock
    private UserDetailsService userDetailsService; // Mock pour UserDetailsService

    @Mock
    private HttpServletResponse response; // Mock pour HttpServletResponse

    @Mock
    private Authentication authentication; // Mock pour Authentication

    @Mock
    private HttpSecurity http; // mock for HttpSecurity

    @Mock
    private PrintWriter writer; // mock for PrintWriter

    private SecurityConfig securityConfig; // Instance de SecurityConfig à tester

    @BeforeEach
    void setUp() throws IOException {
        securityConfig = new SecurityConfig(jwtAuthenticationEntryPoint, jwtAuthenticationFilter, loginAttemptService); // Initialisation
                                                                                                                        // de
                                                                                                                        // SecurityConfig
        when(response.getWriter()).thenReturn(writer);
    }

    @Test
    void passwordEncoder_ShouldReturnBCryptPasswordEncoder() {
        // Teste que la méthode passwordEncoder() renvoie une instance de
        // BCryptPasswordEncoder
        PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();
        assertTrue(passwordEncoder instanceof BCryptPasswordEncoder);
    }

    @Test
    void authenticationManager_ShouldReturnProviderManager() {
        // Teste que la méthode authenticationManager() renvoie une instance de
        // ProviderManager
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        AuthenticationManager authenticationManager = securityConfig.authenticationManager(userDetailsService,
                passwordEncoder);
        assertTrue(authenticationManager instanceof ProviderManager);
    }

    @Test
    void filterChain_ShouldConfigureHttpSecurity() throws Exception {
        // Teste que la méthode filterChain() configure correctement HttpSecurity
        when(http.csrf(any())).thenReturn(http);
        when(http.authorizeHttpRequests(any())).thenReturn(http);
        when(http.requiresChannel(any())).thenReturn(http);
        when(http.oauth2Login(any())).thenReturn(http);
        when(http.httpBasic(any())).thenReturn(http);
        when(http.exceptionHandling(any())).thenReturn(http);
        when(http.addFilterBefore(any(), any())).thenReturn(http);
        when(http.formLogin(any())).thenReturn(http);
        when(http.build()).thenReturn(mock(DefaultSecurityFilterChain.class));

        SecurityFilterChain filterChain = securityConfig.filterChain(http);

        assertNotNull(filterChain);

        verify(http).csrf(any());
        verify(http).authorizeHttpRequests(any());
        verify(http).requiresChannel(any());
        verify(http).oauth2Login(any());
        verify(http).httpBasic(any());
        verify(http).exceptionHandling(any());
        verify(http).addFilterBefore(any(), eq(UsernamePasswordAuthenticationFilter.class));
        verify(http).formLogin(any());

    }

    @Test
    void accessDeniedHandler_ShouldReturnAccessDeniedHandler() throws IOException, ServletException {
        // Teste que la méthode accessDeniedHandler() renvoie une instance de
        // AccessDeniedHandler
        AccessDeniedHandler handler = securityConfig.accessDeniedHandler();

        handler.handle(null, response, null);
        verify(response).setStatus(HttpStatus.FORBIDDEN.value());
        verify(writer).write("Accès refusé.");
    }

    @Test
    void getMaxAttempts_ShouldReturnMaxAttempts() {
        // Teste que la méthode getMaxAttempts() renvoie la valeur de maxAttempts
        int maxAttempts = securityConfig.getMaxAttempts();
        assertEquals(5, maxAttempts);
    }

    @Test
    void getBlockingTime_ShouldReturnBlockingTime() {
        // Teste que la méthode getBlockingTime() renvoie la valeur de blockingTime
        int blockingTime = securityConfig.blockingTime;
        assertEquals(5, blockingTime);
    }
}