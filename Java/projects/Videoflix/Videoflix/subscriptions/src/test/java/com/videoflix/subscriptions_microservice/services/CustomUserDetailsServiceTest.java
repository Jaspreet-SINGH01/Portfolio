package com.videoflix.subscriptions_microservice.services;

import com.videoflix.subscriptions_microservice.entities.Permission;
import com.videoflix.subscriptions_microservice.entities.Role;
import com.videoflix.subscriptions_microservice.entities.User;
import com.videoflix.subscriptions_microservice.entities.UserRole;
import com.videoflix.subscriptions_microservice.repositories.RoleRepository;
import com.videoflix.subscriptions_microservice.repositories.UserRepository;
import com.videoflix.subscriptions_microservice.repositories.UserRoleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

// @ExtendWith(MockitoExtension.class) permet d'utiliser les fonctionnalités de Mockito dans ce test.
@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    // @Mock crée un mock du UserRepository.
    @Mock
    private UserRepository userRepository;

    // @Mock crée un mock du RoleRepository.
    @Mock
    private RoleRepository roleRepository;

    // @Mock crée un mock du UserRoleRepository.
    @Mock
    private UserRoleRepository userRoleRepository;

    // @InjectMocks crée une instance de CustomUserDetailsService et injecte les
    // mocks annotés avec @Mock.
    @InjectMocks
    private CustomUserDetailsService userDetailsService;

    // Test pour vérifier que loadUserByUsername charge correctement l'utilisateur,
    // ses rôles et ses permissions.
    @Test
    void loadUserByUsername_shouldLoadUserWithRolesAndPermissions() {
        // GIVEN : Un nom d'utilisateur existant dans la base de données.
        String username = "testuser";

        // Création d'un utilisateur mocké.
        User user = new User();
        user.setUsername(username);
        user.setPassword("password");

        // Création d'un rôle mocké.
        Role role = new Role();
        role.setName("ADMIN");

        // Création d'une permission mockée associée au rôle.
        Permission permission1 = new Permission();
        permission1.setName("READ_MOVIES");
        Permission permission2 = new Permission();
        permission2.setName("EDIT_USERS");
        role.setPermissions(List.of(permission1, permission2));

        // Création d'un UserRole mocké liant l'utilisateur au rôle.
        UserRole userRole = new UserRole();
        userRole.setUser(user);
        userRole.setRole(role);

        // Configuration des mocks pour retourner les données simulées.
        when(userRepository.findByUsername(username)).thenReturn(user);
        when(userRoleRepository.findByUser(user)).thenReturn(List.of(userRole));

        // WHEN : Appel de la méthode loadUserByUsername.
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        // THEN : Vérification que l'objet UserDetails retourné est correct.
        assertEquals(username, userDetails.getUsername());
        assertEquals("password", userDetails.getPassword());
        assertNotNull(userDetails.getAuthorities());
        assertEquals(3, userDetails.getAuthorities().size()); // 2 permissions + 1 rôle

        // Vérification que les autorités contiennent les permissions et le rôle.
        List<String> authorities = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
        assertTrue(authorities.contains("READ_MOVIES"));
        assertTrue(authorities.contains("EDIT_USERS"));
        assertTrue(authorities.contains("ROLE_ADMIN"));
    }

    // Test pour vérifier que loadUserByUsername lève une UsernameNotFoundException
    // si l'utilisateur n'est pas trouvé.
    @Test
    void loadUserByUsername_shouldThrowExceptionIfUserNotFound() {
        // GIVEN : Un nom d'utilisateur qui n'existe pas dans la base de données.
        String username = "nonexistentuser";

        // Configuration du mock userRepository pour retourner null (utilisateur non
        // trouvé).
        when(userRepository.findByUsername(username)).thenReturn(null);

        // WHEN : Tentative d'appeler loadUserByUsername.
        // THEN : Vérification qu'une UsernameNotFoundException est levée.
        assertThrows(UsernameNotFoundException.class, () -> userDetailsService.loadUserByUsername(username));
    }

    // Test pour vérifier le cas où l'utilisateur n'a aucun rôle associé.
    @Test
    void loadUserByUsername_shouldHandleUserWithNoRoles() {
        // GIVEN : Un utilisateur existant mais sans rôles associés.
        String username = "userwithoutroles";
        User user = new User();
        user.setUsername(username);
        user.setPassword("password");

        // Configuration du mock userRepository pour retourner l'utilisateur.
        when(userRepository.findByUsername(username)).thenReturn(user);
        // Configuration du mock userRoleRepository pour retourner une liste vide (aucun
        // rôle).
        when(userRoleRepository.findByUser(user)).thenReturn(Collections.emptyList());

        // WHEN : Appel de la méthode loadUserByUsername.
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        // THEN : Vérification que l'objet UserDetails est créé avec les informations de
        // l'utilisateur mais sans autorités (permissions ou rôles).
        assertEquals(username, userDetails.getUsername());
        assertEquals("password", userDetails.getPassword());
        assertNotNull(userDetails.getAuthorities());
        assertTrue(userDetails.getAuthorities().isEmpty());
    }

    // Test pour vérifier le cas où un rôle n'a aucune permission associée.
    @Test
    void loadUserByUsername_shouldHandleRoleWithNoPermissions() {
        // GIVEN : Un utilisateur avec un rôle mais ce rôle n'a aucune permission.
        String username = "userwithnorolepermissions";
        User user = new User();
        user.setUsername(username);
        user.setPassword("password");

        Role role = new Role();
        role.setName("VIEWER");
        role.setPermissions(Collections.emptyList()); // Le rôle n'a pas de permissions.

        UserRole userRole = new UserRole();
        userRole.setUser(user);
        userRole.setRole(role);

        // Configuration des mocks.
        when(userRepository.findByUsername(username)).thenReturn(user);
        when(userRoleRepository.findByUser(user)).thenReturn(List.of(userRole));

        // WHEN : Appel de loadUserByUsername.
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        // THEN : Vérification que l'utilisateur est chargé et a seulement l'autorité
        // basée sur le rôle.
        assertEquals(username, userDetails.getUsername());
        assertEquals("password", userDetails.getPassword());
        assertNotNull(userDetails.getAuthorities());
        assertEquals(1, userDetails.getAuthorities().size());
        assertEquals("ROLE_VIEWER", userDetails.getAuthorities().iterator().next().getAuthority());
    }
}