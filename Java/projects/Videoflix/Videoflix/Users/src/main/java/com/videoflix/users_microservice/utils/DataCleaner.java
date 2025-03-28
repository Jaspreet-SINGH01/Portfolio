package com.videoflix.users_microservice.utils;

import org.owasp.encoder.Encode;

public class DataCleaner {

    /**
     * Constructeur privé pour empêcher l'instanciation de la classe DataCleaner.
     * Cette classe contient uniquement des méthodes statiques utilitaires.
     */
    private DataCleaner() {
    }

    /**
     * Nettoie une chaîne de caractères pour l'affichage HTML en échappant les
     * caractères spéciaux.
     * Utilise la bibliothèque OWASP Encoder pour prévenir les attaques XSS.
     *
     * @param input La chaîne de caractères à nettoyer.
     * @return La chaîne de caractères nettoyée, ou null si l'entrée est null.
     */
    public static String cleanHtml(String input) {
        if (input == null) {
            return null;
        }
        return Encode.forHtml(input);
    }

    /**
     * Nettoie une chaîne de caractères pour une utilisation dans une URL en
     * encodant les caractères spéciaux.
     * Utilise la bibliothèque OWASP Encoder pour prévenir les attaques d'injection
     * dans les URLs.
     *
     * @param input La chaîne de caractères à nettoyer.
     * @return La chaîne de caractères nettoyée, ou null si l'entrée est null.
     */
    public static String cleanUrl(String input) {
        if (input == null) {
            return null;
        }
        return Encode.forUriComponent(input);
    }

    /**
     * Nettoie une chaîne de caractères pour une utilisation dans une requête SQL en
     * supprimant les caractères potentiellement dangereux.
     * **Attention :** Cette méthode effectue un nettoyage de base. L'utilisation de
     * paramètres préparés est toujours la meilleure pratique pour prévenir les
     * attaques d'injection SQL.
     *
     * @param input La chaîne de caractères à nettoyer.
     * @return La chaîne de caractères nettoyée, ou null si l'entrée est null.
     */
    public static String cleanSql(String input) {
        if (input == null) {
            return null;
        }
        // Nettoyage de base, mais les paramètres préparés sont toujours préférables
        return input.replaceAll("[\\';\"]", "");
    }

    // Ajouter d'autres méthodes de nettoyage si besoin
}