package com.videoflix.subscriptions_microservice.config;

import com.videoflix.subscriptions_microservice.entities.Permission;
import com.videoflix.subscriptions_microservice.entities.Role;
import com.videoflix.subscriptions_microservice.repositories.PermissionRepository;
import com.videoflix.subscriptions_microservice.repositories.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class RolePermissionInitializer implements CommandLineRunner {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @Override
    public void run(String... args) throws Exception {
        // Créer les permissions
        Permission freeContent = createPermission("Accéder au contenu gratuit");
        Permission premiumContent = createPermission("Accéder au contenu Premium");
        Permission downloadVideos = createPermission("Télécharger des vidéos");
        Permission exclusiveContent = createPermission("Accéder au contenu exclusif");
        Permission manageUsers = createPermission("Gérer les utilisateurs");
        Permission manageSubscriptions = createPermission("Gérer les abonnements");
        Permission manageCatalog = createPermission("Gérer le catalogue");
        Permission accessStats = createPermission("Accéder aux statistiques");

        // Créer les rôles
        Role basicUser = createRole("Utilisateur Basic", Arrays.asList(freeContent));
        Role premiumUser = createRole("Abonné Premium", Arrays.asList(premiumContent, downloadVideos));
        Role ultraUser = createRole("Abonné Ultra", Arrays.asList(premiumContent, downloadVideos, exclusiveContent));
        Role admin = createRole("Administrateur", Arrays.asList(manageUsers, manageSubscriptions, manageCatalog, accessStats));
    }

    private Permission createPermission(String name) {
        Permission permission = new Permission();
        permission.setName(name);
        return permissionRepository.save(permission);
    }

    private Role createRole(String name, List<Permission> permissions) {
        Role role = new Role();
        role.setName(name);
        role.setPermissions(permissions);
        return roleRepository.save(role);
    }
}