package com.videoflix.Users.utils;

import org.junit.jupiter.api.Test;

import com.videoflix.users_microservice.utils.DataCleaner;

import static org.junit.jupiter.api.Assertions.*;

class DataCleanerTest {

    @Test
    void testCleanHtml_NormalInput() {
        // Test de nettoyage HTML avec des caractères spéciaux
        String input = "<script>alert('XSS');</script>";
        String expected = "&lt;script&gt;alert(&#39;XSS&#39;);&lt;/script&gt;";

        assertEquals(expected, DataCleaner.cleanHtml(input),
                "Le nettoyage HTML doit encoder les caractères spéciaux");
    }

    @Test
    void testCleanHtml_NullInput() {
        // Test avec une entrée null
        assertNull(DataCleaner.cleanHtml(null),
                "L'entrée null doit retourner null");
    }

    @Test
    void testCleanHtml_EmptyInput() {
        // Test avec une chaîne vide
        assertEquals("", DataCleaner.cleanHtml(""),
                "Une chaîne vide doit rester vide");
    }

    @Test
    void testCleanUrl_NormalInput() {
        // Test de nettoyage d'URL avec des caractères spéciaux
        String input = "https://example.com/path?param=test value";
        String expected = "https://example.com/path?param=test%20value";

        assertEquals(expected, DataCleaner.cleanUrl(input),
                "Le nettoyage URL doit encoder correctement les espaces et caractères spéciaux");
    }

    @Test
    void testCleanUrl_NullInput() {
        // Test avec une entrée null
        assertNull(DataCleaner.cleanUrl(null),
                "L'entrée null doit retourner null");
    }

    @Test
    void testCleanSql_NormalInput() {
        // Test de nettoyage SQL avec des caractères potentiellement dangereux
        String input = "user'; DROP TABLE Users; --";
        String expected = "user DROP TABLE Users ";

        assertEquals(expected, DataCleaner.cleanSql(input),
                "Le nettoyage SQL doit supprimer les caractères potentiellement dangereux");
    }

    @Test
    void testCleanSql_NullInput() {
        // Test avec une entrée null
        assertNull(DataCleaner.cleanSql(null),
                "L'entrée null doit retourner null");
    }

    @Test
    void testCleanSql_SafeInput() {
        // Test avec une entrée sans caractères spéciaux
        String input = "Normal user input";

        assertEquals(input, DataCleaner.cleanSql(input),
                "Une entrée sûre doit rester inchangée");
    }

    @Test
    void testPrivateConstructor() {
        // Test du constructeur privé pour la couverture de code
        try {
            // Utilisation de la réflexion pour instancier le constructeur privé
            java.lang.reflect.Constructor<DataCleaner> constructor = DataCleaner.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            constructor.newInstance();
        } catch (Exception e) {
            fail("La création d'une instance ne devrait pas lever d'exception");
        }
    }
}