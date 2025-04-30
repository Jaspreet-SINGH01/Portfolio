package com.videoflix.subscriptions_microservice.config;

import com.videoflix.subscriptions_microservice.entities.Permission;
import com.videoflix.subscriptions_microservice.entities.Role;
import com.videoflix.subscriptions_microservice.repositories.PermissionRepository;
import com.videoflix.subscriptions_microservice.repositories.RoleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class RolePermissionInitializerTest {

    @InjectMocks
    private RolePermissionInitializer initializer; // L'instance de la classe à tester

    @Mock
    private RoleRepository roleRepository; // Mock du repository pour les rôles

    @Mock
    private PermissionRepository permissionRepository; // Mock du repository pour les permissions

    @Test
    void run_shouldCreateAndSaveDefaultRolesAndPermissions() throws Exception {
        // GIVEN : Les mocks des repositories sont configurés pour simuler la sauvegarde
        // et le retour d'entités
        // Captureurs d'arguments pour vérifier les entités sauvegardées
        ArgumentCaptor<Permission> permissionCaptor = ArgumentCaptor.forClass(Permission.class);
        ArgumentCaptor<Role> roleCaptor = ArgumentCaptor.forClass(Role.class);

        // Configuration du comportement des mocks lors de la sauvegarde des permissions
        when(permissionRepository.save(any(Permission.class))).thenAnswer(invocation -> {
            Permission permission = invocation.getArgument(0);
            permission.setId(System.currentTimeMillis()); // Simule l'attribution d'un ID lors de la sauvegarde
            return permission;
        });

        // Configuration du comportement des mocks lors de la sauvegarde des rôles
        when(roleRepository.save(any(Role.class))).thenAnswer(invocation -> {
            Role role = invocation.getArgument(0);
            role.setId(System.currentTimeMillis()); // Simule l'attribution d'un ID lors de la sauvegarde
            return role;
        });

        // WHEN : La méthode run de l'initializer est appelée
        initializer.run();

        // THEN : Vérification que les permissions attendues ont été créées et
        // sauvegardées
        verify(permissionRepository, times(8)).save(permissionCaptor.capture());
        List<Permission> savedPermissions = permissionCaptor.getAllValues();
        assertEquals(8, savedPermissions.size());
        assertTrue(savedPermissions.stream().anyMatch(p -> p.getName().equals("Accéder au contenu gratuit")));
        assertTrue(savedPermissions.stream().anyMatch(p -> p.getName().equals("Accéder au contenu Premium")));
        assertTrue(savedPermissions.stream().anyMatch(p -> p.getName().equals("Télécharger des vidéos")));
        assertTrue(savedPermissions.stream().anyMatch(p -> p.getName().equals("Accéder au contenu exclusif")));
        assertTrue(savedPermissions.stream().anyMatch(p -> p.getName().equals("Gérer les utilisateurs")));
        assertTrue(savedPermissions.stream().anyMatch(p -> p.getName().equals("Gérer les abonnements")));
        assertTrue(savedPermissions.stream().anyMatch(p -> p.getName().equals("Gérer le catalogue")));
        assertTrue(savedPermissions.stream().anyMatch(p -> p.getName().equals("Accéder aux statistiques")));

        // THEN : Vérification que les rôles attendus ont été créés et sauvegardés avec
        // les bonnes permissions
        verify(roleRepository, times(4)).save(roleCaptor.capture());
        List<Role> savedRoles = roleCaptor.getAllValues();
        assertEquals(4, savedRoles.size());

        // Vérification du rôle "Utilisateur Basic"
        assertTrue(savedRoles.stream().anyMatch(r -> r.getName().equals("Utilisateur Basic") &&
                r.getPermissions().stream().anyMatch(p -> p.getName().equals("Accéder au contenu gratuit"))));

        // Vérification du rôle "Abonné Premium"
        assertTrue(savedRoles.stream().anyMatch(r -> r.getName().equals("Abonné Premium") &&
                r.getPermissions().stream().anyMatch(p -> p.getName().equals("Accéder au contenu Premium")) &&
                r.getPermissions().stream().anyMatch(p -> p.getName().equals("Télécharger des vidéos"))));

        // Vérification du rôle "Abonné Ultra"
        assertTrue(savedRoles.stream().anyMatch(r -> r.getName().equals("Abonné Ultra") &&
                r.getPermissions().stream().anyMatch(p -> p.getName().equals("Accéder au contenu Premium")) &&
                r.getPermissions().stream().anyMatch(p -> p.getName().equals("Télécharger des vidéos")) &&
                r.getPermissions().stream().anyMatch(p -> p.getName().equals("Accéder au contenu exclusif"))));

        // Vérification du rôle "Administrateur"
        assertTrue(savedRoles.stream().anyMatch(r -> r.getName().equals("Administrateur") &&
                r.getPermissions().stream().anyMatch(p -> p.getName().equals("Gérer les utilisateurs")) &&
                r.getPermissions().stream().anyMatch(p -> p.getName().equals("Gérer les abonnements")) &&
                r.getPermissions().stream().anyMatch(p -> p.getName().equals("Gérer le catalogue")) &&
                r.getPermissions().stream().anyMatch(p -> p.getName().equals("Accéder aux statistiques"))));
    }
}