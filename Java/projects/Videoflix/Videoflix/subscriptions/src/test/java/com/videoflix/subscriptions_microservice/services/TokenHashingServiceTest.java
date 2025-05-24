package com.videoflix.subscriptions_microservice.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder; // Importez BCryptPasswordEncoder si nécessaire pour les tests de granularité
import org.springframework.security.crypto.password.PasswordEncoder; // Importez PasswordEncoder si nécessaire pour vérifier l'implémentation
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy; // Utilisation de @Spy pour espionner l'instance réelle de PasswordEncoder

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify; // Pour vérifier les appels sur l'objet espionné

@DisplayName("Tests pour TokenHashingService")
class TokenHashingServiceTest {

    // @InjectMocks injecte les dépendances mockées ou espionnées dans l'instance de
    // la classe testée.
    // Ici, il injectera l'instance de PasswordEncoder (créée par @Spy ou par le
    // constructeur)
    // dans `tokenHashingService`.
    @InjectMocks
    private TokenHashingService tokenHashingService;

    // @Spy crée un espion de l'instance réelle de BCryptPasswordEncoder.
    // Cela nous permet d'appeler les méthodes réelles de BCryptPasswordEncoder
    // tout en pouvant vérifier les interactions si nécessaire (bien que pour ce
    // service,
    // ce ne soit pas strictement nécessaire car nous testons le comportement
    // global).
    // Si nous utilisions @Mock, nous devrions stubber `encode` et `matches`.
    // Avec @Spy, nous testons la logique réelle des méthodes de hachage.
    @Spy
    private PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * Configuration initiale avant chaque test.
     * Appelle MockitoAnnotations.openMocks(this) pour initialiser les champs
     * annotés avec @Mock, @Spy ou @InjectMocks.
     */
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // --- Tests de la méthode hashToken ---

    @Test
    @DisplayName("Devrait hasher un token avec succès")
    void hashToken_shouldHashSuccessfully() {
        // GIVEN: Un token en clair
        String rawToken = "mySecretPushToken123";

        // WHEN: On hashe le token
        String hashedPassword = tokenHashingService.hashToken(rawToken);

        // THEN: On vérifie que le hash n'est ni null ni vide et qu'il est différent du
        // token original.
        // BCrypt génère des hashes d'une longueur spécifique (généralement 60
        // caractères pour $2a$, $2b$, $2y$).
        assertNotNull(hashedPassword, "Le hash ne devrait pas être null.");
        assertFalse(hashedPassword.isEmpty(), "Le hash ne devrait pas être vide.");
        assertNotEquals(rawToken, hashedPassword, "Le hash ne devrait pas être identique au token original.");

        // On peut aussi vérifier que le hash commence par le préfixe BCrypt standard.
        assertTrue(
                hashedPassword.startsWith("$2a$") || hashedPassword.startsWith("$2b$")
                        || hashedPassword.startsWith("$2y$"),
                "Le hash devrait commencer par le préfixe BCrypt standard.");

        // On peut vérifier que la méthode `encode` de `passwordEncoder` a bien été
        // appelée.
        verify(passwordEncoder).encode(rawToken);
    }

    @Test
    @DisplayName("Le hachage du même token devrait produire des hashes différents (car le sel est aléatoire)")
    void hashToken_shouldProduceDifferentHashesForSameInput() {
        // GIVEN: Le même token en clair
        String rawToken = "anotherTokenABC";

        // WHEN: On hashe le token deux fois de suite
        String hashedPassword1 = tokenHashingService.hashToken(rawToken);
        String hashedPassword2 = tokenHashingService.hashToken(rawToken);

        // THEN: Les deux hashes devraient être différents car BCrypt utilise un sel
        // aléatoire.
        assertNotEquals(hashedPassword1, hashedPassword2,
                "Deux hachages du même token devraient produire des hashes différents.");
    }

    // --- Tests de la méthode verifyToken ---

    @Test
    @DisplayName("Devrait vérifier un token avec succès (correspondance)")
    void verifyToken_shouldReturnTrueForMatchingTokens() {
        // GIVEN: Un token en clair et son version hashée (que l'on génère pour le test)
        String rawToken = "userToken123";
        // Nous hashons le token avec l'encoder réel pour obtenir un hash valide à
        // vérifier.
        String hashedTokenFromDatabase = passwordEncoder.encode(rawToken);

        // WHEN: On vérifie la correspondance
        boolean isVerified = tokenHashingService.verifyToken(rawToken, hashedTokenFromDatabase);

        // THEN: La vérification devrait retourner true
        assertTrue(isVerified, "Le token en clair devrait correspondre au hash.");

        // On peut vérifier que la méthode `matches` de `passwordEncoder` a bien été
        // appelée.
        verify(passwordEncoder).matches(rawToken, hashedTokenFromDatabase);
    }

    @Test
    @DisplayName("Devrait échouer la vérification pour des tokens non correspondants")
    void verifyToken_shouldReturnFalseForNonMatchingTokens() {
        // GIVEN: Un token en clair et un hash qui ne correspond pas
        String rawToken = "wrongToken";
        String hashedTokenFromDatabase = passwordEncoder.encode("correctToken"); // Hash d'un token différent

        // WHEN: On vérifie la non-correspondance
        boolean isVerified = tokenHashingService.verifyToken(rawToken, hashedTokenFromDatabase);

        // THEN: La vérification devrait retourner false
        assertFalse(isVerified, "Le token en clair ne devrait PAS correspondre au hash.");

        // On peut vérifier que la méthode `matches` a bien été appelée.
        verify(passwordEncoder).matches(rawToken, hashedTokenFromDatabase);
    }

    @Test
    @DisplayName("Devrait échouer la vérification si le token hashé est nul ou vide")
    void verifyToken_shouldReturnFalseIfHashedTokenIsInvalid() {
        String rawToken = "someToken";

        // Test avec un hash null
        assertFalse(tokenHashingService.verifyToken(rawToken, null),
                "La vérification devrait échouer si le hash est null.");

        // Test avec un hash vide
        assertFalse(tokenHashingService.verifyToken(rawToken, ""),
                "La vérification devrait échouer si le hash est vide.");
    }
}