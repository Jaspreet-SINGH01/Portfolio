package com.videoflix.Users.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.videoflix.users_microservice.entities.Role;
import com.videoflix.users_microservice.entities.User;
import com.videoflix.users_microservice.exceptions.AuthorizationException;
import com.videoflix.users_microservice.exceptions.InvalidEmailException;
import com.videoflix.users_microservice.exceptions.InvalidPasswordException;
import com.videoflix.users_microservice.exceptions.UserAlreadyExistsException;
import com.videoflix.users_microservice.exceptions.UserNotFoundException;
import com.videoflix.users_microservice.repositories.UserRepository;
import com.videoflix.users_microservice.services.LoginAttemptService;
import com.videoflix.users_microservice.services.UserService;
import com.videoflix.users_microservice.services.async.NotificationServiceAsync;

import jakarta.servlet.http.HttpServletRequest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private LoginAttemptService loginAttemptService;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("ValidPassword123");
        testUser.setRole(Role.USER.name());
    }

    @Test
    void testCreateUser_Success() {
        // Vérifie la création réussie d'un nouvel utilisateur
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        User createdUser = userService.createUser(testUser, Role.USER);

        assertNotNull(createdUser);
        verify(userRepository).save(any(User.class));
        try (MockedStatic<NotificationServiceAsync> mockedStatic = mockStatic(NotificationServiceAsync.class)) {
            mockedStatic.verify(
                    () -> NotificationServiceAsync.sendWelcomeEmailAsync(testUser.getEmail(), testUser.getUsername()));
        }
    }

    @Test
    void testCreateUser_InvalidPassword() {
        // Vérifie le rejet d'un utilisateur avec un mot de passe invalide
        User invalidUser = new User();
        invalidUser.setPassword("short");

        assertThrows(InvalidPasswordException.class, () -> {
            userService.createUser(invalidUser, Role.USER);
        });
    }

    @Test
    void testCreateUser_InvalidEmail() {
        // Vérifie le rejet d'un utilisateur avec un email invalide
        User invalidUser = new User();
        invalidUser.setUsername("testuser");
        invalidUser.setPassword("ValidPassword123");
        invalidUser.setEmail("invalidemail");

        assertThrows(InvalidEmailException.class, () -> {
            userService.createUser(invalidUser, Role.USER);
        });
    }

    @Test
    void testCreateUser_UsernameAlreadyExists() {
        // Vérifie le rejet de la création d'un utilisateur avec un nom d'utilisateur
        // existant
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(testUser));

        assertThrows(UserAlreadyExistsException.class, () -> {
            userService.createUser(testUser, Role.USER);
        });
    }

    @Test
    void testUpdateUser_Success() {
        // Vérifie la mise à jour réussie d'un utilisateur
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        User updatedUser = userService.updateUser(1L, "New Name", "new@example.com", Role.USER);

        assertNotNull(updatedUser);
        assertEquals("New Name", updatedUser.getName());
        assertEquals("new@example.com", updatedUser.getEmail());
    }

    @Test
    void testUpdateUser_UserNotFound() {
        // Vérifie le comportement lors de la tentative de mise à jour d'un utilisateur
        // inexistant
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> {
            userService.updateUser(1L, "New Name", "new@example.com", Role.USER);
        });
    }

    @Test
    void testDeleteUser_Success() {
        // Vérifie la suppression réussie d'un utilisateur par un administrateur
        User adminUser = new User();
        adminUser.setRole(Role.ADMIN.name());
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));

        userService.deleteUser(1L, adminUser);

        verify(userRepository).delete(testUser);
    }

    @Test
    void testDeleteUser_Unauthorized() {
        // Vérifie l'interdiction de suppression pour un utilisateur non administrateur
        User nonAdminUser = new User();
        nonAdminUser.setRole(Role.USER.name());
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));

        assertThrows(AuthorizationException.class, () -> {
            userService.deleteUser(1L, nonAdminUser);
        });
    }

    @Test
    void testAuthenticate_Success() {
        // Vérifie l'authentification réussie d'un utilisateur
        when(loginAttemptService.isBlocked(anyString())).thenReturn(false);
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");

        User authenticatedUser = userService.authenticate("testuser", "ValidPassword123");

        assertNotNull(authenticatedUser);
        verify(loginAttemptService).resetLoginAttempts(anyString());
    }

    @Test
    void testAuthenticate_Blocked() {
        // Vérifie le rejet de l'authentification pour un compte bloqué
        when(loginAttemptService.isBlocked(anyString())).thenReturn(true);

        assertThrows(AuthorizationException.class, () -> {
            userService.authenticate("testuser", "password");
        });
    }

    @Test
    void testResetPassword_Success() {
        // Vérifie la réinitialisation réussie du mot de passe
        String resetToken = "validResetToken";
        testUser.setResetToken(resetToken);
        when(userRepository.findByResetToken(anyString())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.encode(anyString())).thenReturn("newEncodedPassword");

        userService.resetPassword(resetToken, "NewValidPassword123");

        verify(userRepository).save(testUser);
        assertNull(testUser.getResetToken());
    }

    @Test
    void testResetPassword_InvalidToken() {
        // Vérifie le rejet de la réinitialisation avec un jeton invalide
        when(userRepository.findByResetToken(anyString())).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> {
            userService.resetPassword("invalidToken", "NewValidPassword123");
        });
    }

    @Test
    void testEnable2FA() {
        // Vérifie l'activation de l'authentification à deux facteurs
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));

        userService.enable2FA(1L);

        assertTrue(testUser.is2faEnabled());
        verify(userRepository).save(testUser);
    }

    @Test
    void testDisable2FA() {
        // Vérifie la désactivation de l'authentification à deux facteurs
        testUser.set2faEnabled(true);
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));

        userService.disable2FA(1L);

        assertFalse(testUser.is2faEnabled());
        verify(userRepository).save(testUser);
    }
}