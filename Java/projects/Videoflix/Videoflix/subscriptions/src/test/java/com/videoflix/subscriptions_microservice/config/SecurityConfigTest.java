package com.videoflix.subscriptions_microservice.config;

import com.videoflix.subscriptions_microservice.services.CustomUserDetailsService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class SecurityConfigTest {

    @InjectMocks
    private SecurityConfig securityConfig; // L'instance de la classe de configuration de sécurité à tester

    @Mock
    private CustomUserDetailsService userDetailsService; // Mock du service utilisateur personnalisé

    @Mock
    private AuthenticationConfiguration authConfig; // Mock de la configuration d'authentification

    @Test
    void passwordEncoder_shouldReturnBCryptPasswordEncoder() {
        // Teste la création du bean PasswordEncoder
        PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();
        assertNotNull(passwordEncoder); // Vérifie que l'encodeur n'est pas nul
        assertTrue(passwordEncoder instanceof BCryptPasswordEncoder); // Vérifie que l'encodeur est une instance de
                                                                      // BCryptPasswordEncoder
    }

    @Test
    void authenticationProvider_shouldConfigureDaoAuthenticationProviderCorrectly() {
        // WHEN : La méthode authenticationProvider est appelée
        DaoAuthenticationProvider authProvider = securityConfig.authenticationProvider();

        // THEN : Vérification que les setters ont été appelés avec les mocks attendus
        verify(authProvider, times(1)).setUserDetailsService(userDetailsService);
        // Pour vérifier l'encodeur de mot de passe, s'assurer qu'un
        // PasswordEncoder a été créé
        // et potentiellement vérifier son type si nécessaire (comme nous le faisons
        // dans un autre test).
        // Il n'y a pas de getter direct pour le PasswordEncoder dans
        // DaoAuthenticationProvider.
        // On pourrait tester indirectement en tentant une authentification (ce qui
        // serait plus un test d'intégration).
        // Pour un test unitaire, s'assurer que le setter implicite via
        // passwordEncoder() a été utilisé.
        PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();
        verify(authProvider, times(1)).setPasswordEncoder(passwordEncoder);
        assertTrue(passwordEncoder instanceof BCryptPasswordEncoder);
    }

    @Test
    void authenticationManager_shouldReturnAuthenticationManagerFromConfiguration() throws Exception {
        // Teste la récupération de l'AuthenticationManager depuis la configuration
        AuthenticationManager mockAuthenticationManager = mock(AuthenticationManager.class);
        when(authConfig.getAuthenticationManager()).thenReturn(mockAuthenticationManager); // Configure le mock pour
                                                                                           // retourner un
                                                                                           // AuthenticationManager

        AuthenticationManager authenticationManager = securityConfig.authenticationManager(authConfig);
        assertNotNull(authenticationManager); // Vérifie que le manager n'est pas nul
        assertEquals(mockAuthenticationManager, authenticationManager); // Vérifie que le manager retourné est celui de
                                                                        // la configuration
    }

    // Note : Tester securityFilterChain nécessite une configuration Spring Security
    // plus complexe
    // et impliquerait de vérifier les règles d'autorisation des requêtes HTTP.
    // Cela dépasse la portée d'un simple test unitaire et nécessiterait
    // probablement
    // des tests d'intégration avec @SpringBootTest et @AutoConfigureMockMvc.
    // Nous pouvons nous concentrer sur la vérification des beans individuels pour
    // un test unitaire.

    // La méthode configureGlobal est plus difficile à tester en isolation car elle
    // interagit
    // directement avec un AuthenticationManagerBuilder qui est généralement géré
    // par Spring Security.
    // Tester la configuration en mémoire nécessiterait également un contexte Spring
    // Security complet.
}