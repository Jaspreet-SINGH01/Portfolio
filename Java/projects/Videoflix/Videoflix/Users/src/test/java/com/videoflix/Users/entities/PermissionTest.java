package com.videoflix.Users.entities;

import org.junit.jupiter.api.Test;

import com.videoflix.users_microservice.entities.Permission;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class PermissionTest {

    @Test
    void permissionEnum_ShouldHaveCorrectValues() {
        // Teste que l'énumération Permission a les valeurs attendues

        // Vérification de la présence de chaque valeur de l'énumération
        assertNotNull(Permission.VIDEO_VIEW);
        assertNotNull(Permission.VIDEO_LIKE_DISLIKE);
        assertNotNull(Permission.PLAYLIST_ADD_VIDEO);
        assertNotNull(Permission.VIDEO_ADD);
        assertNotNull(Permission.VIDEO_UPDATE);
        assertNotNull(Permission.VIDEO_DELETE);
        assertNotNull(Permission.VIDEO_ADD_DESCRIPTION);

        // Vérification des noms des valeurs de l'énumération
        assertEquals("VIDEO_VIEW", Permission.VIDEO_VIEW.name());
        assertEquals("VIDEO_LIKE_DISLIKE", Permission.VIDEO_LIKE_DISLIKE.name());
        assertEquals("PLAYLIST_ADD_VIDEO", Permission.PLAYLIST_ADD_VIDEO.name());
        assertEquals("VIDEO_ADD", Permission.VIDEO_ADD.name());
        assertEquals("VIDEO_UPDATE", Permission.VIDEO_UPDATE.name());
        assertEquals("VIDEO_DELETE", Permission.VIDEO_DELETE.name());
        assertEquals("VIDEO_ADD_DESCRIPTION", Permission.VIDEO_ADD_DESCRIPTION.name());

        // Vérification de la longueur de l'énumération
        assertEquals(7, Permission.values().length);
    }

    @Test
    void permissionEnum_ShouldHaveCorrectOrdinalValues() {
        // Teste que les valeurs ordinales de l'énumération sont correctes

        // Vérification des valeurs ordinales (index) de chaque valeur de l'énumération
        assertEquals(0, Permission.VIDEO_VIEW.ordinal());
        assertEquals(1, Permission.VIDEO_LIKE_DISLIKE.ordinal());
        assertEquals(2, Permission.PLAYLIST_ADD_VIDEO.ordinal());
        assertEquals(3, Permission.VIDEO_ADD.ordinal());
        assertEquals(4, Permission.VIDEO_UPDATE.ordinal());
        assertEquals(5, Permission.VIDEO_DELETE.ordinal());
        assertEquals(6, Permission.VIDEO_ADD_DESCRIPTION.ordinal());
    }

    @Test
    void permissionEnum_ShouldReturnCorrectValuesFromString() {
        // Teste que la méthode valueOf() renvoie les valeurs correctes à partir de
        // chaînes de caractères

        // Vérification que valueOf() renvoie les valeurs correctes pour chaque nom de
        // valeur
        assertEquals(Permission.VIDEO_VIEW, Permission.valueOf("VIDEO_VIEW"));
        assertEquals(Permission.VIDEO_LIKE_DISLIKE, Permission.valueOf("VIDEO_LIKE_DISLIKE"));
        assertEquals(Permission.PLAYLIST_ADD_VIDEO, Permission.valueOf("PLAYLIST_ADD_VIDEO"));
        assertEquals(Permission.VIDEO_ADD, Permission.valueOf("VIDEO_ADD"));
        assertEquals(Permission.VIDEO_UPDATE, Permission.valueOf("VIDEO_UPDATE"));
        assertEquals(Permission.VIDEO_DELETE, Permission.valueOf("VIDEO_DELETE"));
        assertEquals(Permission.VIDEO_ADD_DESCRIPTION, Permission.valueOf("VIDEO_ADD_DESCRIPTION"));
    }
}