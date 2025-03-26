package com.videoflix.Users.entities;

import org.junit.jupiter.api.Test;

import com.videoflix.users_microservice.entities.LoginAttempt;

import jakarta.persistence.*;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class LoginAttemptTest {

    @Test
    void loginAttempt_ShouldCreateEntityWithCorrectAttributes() {
        // Création d'une instance de LoginAttempt
        LoginAttempt loginAttempt = new LoginAttempt();

        // Définition des attributs de l'instance
        loginAttempt.setUsername("testUser");
        loginAttempt.setAttemptTime(LocalDateTime.now());

        // Vérification que les attributs sont correctement définis
        assertNotNull(loginAttempt.getAttemptTime()); // Vérification que attemptTime n'est pas null
        assertEquals("testUser", loginAttempt.getUsername()); // Vérification que le nom d'utilisateur est correct

        // Vérification que l'ID est null avant la persistance (car
        // GenerationType.IDENTITY est utilisé)
        assertEquals(null, loginAttempt.getId());
    }

    @Test
    void loginAttempt_ShouldHaveCorrectColumnMappings() throws NoSuchFieldException {
        // Teste que les annotations @Column sont correctement configurées

        // Vérification de l'annotation @Column pour le champ username
        Column usernameColumn = LoginAttempt.class.getDeclaredField("username").getAnnotation(Column.class);
        assertNotNull(usernameColumn);
        assertEquals("username", usernameColumn.name()); // Vérification du nom de la colonne

        // Vérification de l'annotation @Column pour le champ attemptTime
        Column attemptTimeColumn = LoginAttempt.class.getDeclaredField("attemptTime").getAnnotation(Column.class);
        assertNotNull(attemptTimeColumn);
        assertEquals("attempt_time", attemptTimeColumn.name()); // Vérification du nom de la colonne
    }

    @Test
    void loginAttempt_ShouldHaveCorrectTableMapping() {
        // Teste que l'annotation @Table est correctement configurée
        Table table = LoginAttempt.class.getAnnotation(Table.class);
        assertNotNull(table);
        assertEquals("login_attempts", table.name()); // Vérification du nom de la table
    }

    @Test
    void loginAttempt_ShouldHaveCorrectIdMapping() throws NoSuchFieldException {
        // Teste que l'annotation @Id et @GeneratedValue sont correctement configurées
        Id id = LoginAttempt.class.getDeclaredField("id").getAnnotation(Id.class);
        GeneratedValue generatedValue = LoginAttempt.class.getDeclaredField("id").getAnnotation(GeneratedValue.class);

        assertNotNull(id); // Vérification que le champ id est annoté avec @Id
        assertNotNull(generatedValue); // Vérification que le champ id est annoté avec @GeneratedValue
        assertEquals(GenerationType.IDENTITY, generatedValue.strategy()); // Vérification de la stratégie de génération
                                                                          // de l'ID
    }
}