package com.videoflix.users_microservice.utils;

import org.owasp.encoder.Encode;

public class DataCleaner {

    private DataCleaner() {
    }

    public static String cleanHtml(String input) {
        if (input == null) {
            return null;
        }
        return Encode.forHtml(input);
    }

    public static String cleanUrl(String input) {
        if (input == null) {
            return null;
        }
        return Encode.forUriComponent(input);
    }

    public static String cleanSql(String input) {
        if (input == null) {
            return null;
        }
        // Nettoyage de base, mais les paramètres préparés sont toujours préférables
        return input.replaceAll("[\\';\"]", "");
    }

    // Ajoutez d'autres méthodes de nettoyage au besoin
}