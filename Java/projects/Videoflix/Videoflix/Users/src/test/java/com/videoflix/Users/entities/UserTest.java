package com.videoflix.Users.entities;

import org.junit.jupiter.api.Test;

import com.videoflix.users_microservice.entities.Role;
import com.videoflix.users_microservice.entities.User;

import jakarta.persistence.*;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    void user_ShouldCreateEntityWithCorrectAttributes() {
        // Crée une instance de User
        User user = new User();

        // Définit les attributs de l'instance
        user.setName("John Doe");
        user.setUsername("johndoe");
        user.setEmail("john.doe@example.com");
        user.setPassword("Password123");
        user.setStatus("ACTIVE");
        user.setRole("USER");
        user.setResetToken("resetToken123");
        user.setSecretKey("secretKey123");
        user.set2faEnabled(true);
        user.setFailedLoginAttempts(3);
        user.setLastFailedLogin(LocalDateTime.now().minusHours(1));
        user.setLastLogin(LocalDateTime.now());
        user.setLastLoginIp("192.168.1.1");
        user.setEmailNotificationsEnabled(true);

        // Vérifie que les attributs sont correctement définis
        assertEquals("John Doe", user.getName());
        assertEquals("johndoe", user.getUsername());
        assertEquals("john.doe@example.com", user.getEmail());
        assertEquals("Password123", user.getPassword());
        assertEquals("ACTIVE", user.getStatus());
        assertEquals("USER", user.getRole());
        assertEquals("resetToken123", user.getResetToken());
        assertEquals("secretKey123", user.getSecretKey());
        assertTrue(user.is2faEnabled());
        assertEquals(3, user.getFailedLoginAttempts());
        assertNotNull(user.getLastFailedLogin());
        assertNotNull(user.getLastLogin());
        assertEquals("192.168.1.1", user.getLastLoginIp());
        assertTrue(user.isEmailNotificationsEnabled());

        // Vérifie que l'ID est null avant la persistance
        assertNull(user.getId());
    }

    @Test
    void user_ShouldHaveCorrectColumnMappings() throws NoSuchFieldException {
        // Teste que les annotations @Column sont correctement configurées

        // Vérification de l'annotation @Column pour le champ username
        assertEquals("username", User.class.getDeclaredField("username").getAnnotation(Column.class).name());
        assertFalse(User.class.getDeclaredField("username").getAnnotation(Column.class).nullable());

        // Vérification de l'annotation @Column pour le champ email
        assertEquals("email", User.class.getDeclaredField("email").getAnnotation(Column.class).name());
        assertFalse(User.class.getDeclaredField("email").getAnnotation(Column.class).nullable());

        // Vérification de l'annotation @Column pour le champ password
        assertEquals("password", User.class.getDeclaredField("password").getAnnotation(Column.class).name());
        assertFalse(User.class.getDeclaredField("password").getAnnotation(Column.class).nullable());

        // Vérification de l'annotation @Column pour le champ name
        assertEquals("name", User.class.getDeclaredField("name").getAnnotation(Column.class).name());

        // Vérification de l'annotation @Column pour le champ status
        assertEquals("status", User.class.getDeclaredField("status").getAnnotation(Column.class).name());

        // Vérification de l'annotation @Column pour le champ role
        assertEquals("role", User.class.getDeclaredField("role").getAnnotation(Column.class).name());

        // Vérification de l'annotation @Column pour le champ resetToken
        assertEquals("reset_token", User.class.getDeclaredField("resetToken").getAnnotation(Column.class).name());

        // Vérification de l'annotation @Column pour le champ secretKey
        assertEquals("secret_key", User.class.getDeclaredField("secretKey").getAnnotation(Column.class).name());

        // Vérification de l'annotation @Column pour le champ is2faEnabled
        assertEquals("is_2fa_enabled", User.class.getDeclaredField("is2faEnabled").getAnnotation(Column.class).name());

        // Vérification de l'annotation @Column pour le champ failedLoginAttempts
        assertEquals("failed_login_attempts",
                User.class.getDeclaredField("failedLoginAttempts").getAnnotation(Column.class).name());

        // Vérification de l'annotation @Column pour le champ lastFailedLogin
        assertEquals("last_failed_login",
                User.class.getDeclaredField("lastFailedLogin").getAnnotation(Column.class).name());

        // Vérification de l'annotation @Column pour le champ lastLogin
        assertEquals("last_login", User.class.getDeclaredField("lastLogin").getAnnotation(Column.class).name());

        // Vérification de l'annotation @Column pour le champ lastLoginIp
        assertEquals("last_login_ip", User.class.getDeclaredField("lastLoginIp").getAnnotation(Column.class).name());

        // Vérification de l'annotation @Column pour le champ emailNotificationsEnabled
        assertEquals("email_notifications_enabled",
                User.class.getDeclaredField("emailNotificationsEnabled").getAnnotation(Column.class).name());
    }

    @Test
    void user_ShouldHaveCorrectTableMapping() {
        // Teste que l'annotation @Table est correctement configurée
        assertEquals("users", User.class.getAnnotation(Table.class).name());
    }

    @Test
    void user_ShouldHaveCorrectIdMapping() throws NoSuchFieldException {
        // Teste que l'annotation @Id et @GeneratedValue sont correctement configurées
        assertNotNull(User.class.getDeclaredField("id").getAnnotation(Id.class));
        assertEquals(GenerationType.IDENTITY,
                User.class.getDeclaredField("id").getAnnotation(GeneratedValue.class).strategy());
    }

    @Test
    void user_ShouldHaveCorrectPermissionsMapping() throws NoSuchFieldException {
        // Teste que les annotations @ElementCollection, @CollectionTable, @Enumerated,
        // @Column sont correctes pour permissions
        assertNotNull(User.class.getDeclaredField("permissions").getAnnotation(ElementCollection.class));
        assertEquals("user_permissions", User.class.getAnnotation(Table.class).name());
        assertNotNull(User.class.getDeclaredField("permissions").getAnnotation(Enumerated.class));
        assertEquals(EnumType.STRING,
                User.class.getDeclaredField("permissions").getAnnotation(Enumerated.class).value());
        assertEquals("permission", User.class.getDeclaredField("permissions").getAnnotation(Column.class).name());
    }

    @Test
    void user_ShouldReturnCorrectPermissionsFromRole() {
        // Teste que la méthode getPermissions() renvoie les permissions correctes en
        // fonction du rôle
        User user = new User();
        user.setRole("USER");
        assertEquals(Role.USER.getPermissions(), user.getPermissions());

        user.setRole("ADMIN");
        assertEquals(Role.ADMIN.getPermissions(), user.getPermissions());
    }

    @Test
    void user_ShouldSetAndGetEmailNotificationsEnabled() {
        // Teste que les méthodes setEmailNotificationsEnabled() et
        // isEmailNotificationsEnabled() fonctionnent correctement
        User user = new User();
        user.setEmailNotificationsEnabled(true);
        assertTrue(user.isEmailNotificationsEnabled());

        user.setEmailNotificationsEnabled(false);
        assertFalse(user.isEmailNotificationsEnabled());
    }
}