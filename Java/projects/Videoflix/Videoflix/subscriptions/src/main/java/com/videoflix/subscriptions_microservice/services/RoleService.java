package com.videoflix.subscriptions_microservice.services;

import com.videoflix.subscriptions_microservice.entities.Permission;
import com.videoflix.subscriptions_microservice.entities.Role;
import com.videoflix.subscriptions_microservice.entities.UserRole;
import com.videoflix.subscriptions_microservice.repositories.PermissionRepository;
import com.videoflix.subscriptions_microservice.repositories.RoleRepository;
import com.videoflix.subscriptions_microservice.repositories.UserRoleRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RoleService {

    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private static final Logger logger = LoggerFactory.getLogger(RoleService.class);

    public RoleService(PermissionRepository permissionRepository, RoleRepository roleRepository,
            UserRoleRepository userRoleRepository) {
        this.permissionRepository = permissionRepository;
        this.roleRepository = roleRepository;
        this.userRoleRepository = userRoleRepository;
    }

    /**
     * Assigne un rôle à un utilisateur.
     *
     * @param userId   L'identifiant de l'utilisateur.
     * @param roleName Le nom du rôle à assigner.
     */
    @Transactional
    public void assignRoleToUser(Long userId, String roleName) {
        Role role = roleRepository.findByName(roleName);

        if (role != null) {
            UserRole userRole = new UserRole();
            userRole.setUserId(userId);
            userRole.setRoleId(role.getId());
            userRoleRepository.save(userRole);
        } else {
            // Utilisation d'un logger au lieu de System.err
            logger.warn("Role with name '{}' not found.", roleName);
            // Vous pouvez également lancer une exception personnalisée ici
        }
    }

    /**
     * Assigne un rôle à un utilisateur en fonction de son niveau d'abonnement.
     *
     * @param userId            L'identifiant de l'utilisateur.
     * @param subscriptionLevel Le niveau d'abonnement de l'utilisateur.
     */
    @Transactional
    public void assignRoleBySubscriptionLevel(Long userId, String subscriptionLevel) {
        Role role = null;
        switch (subscriptionLevel) {
            case "Gratuit":
                role = roleRepository.findByName("Utilisateur gratuit");
                break;
            case "Premium":
                role = roleRepository.findByName("Abonné Premium");
                break;
            case "Ultra":
                role = roleRepository.findByName("Abonné Ultra");
                break;
            case "Administrateur":
                role = roleRepository.findByName("Administrateur");
                break;
            default:
                logger.warn("Unknown subscription level: '{}'", subscriptionLevel);
                break;
        }

        if (role != null) {
            UserRole userRole = new UserRole();
            userRole.setUserId(userId);
            userRole.setRoleId(role.getId());
            userRoleRepository.save(userRole);
        } else {
            logger.warn("No role found for subscription level: '{}'", subscriptionLevel);
        }
    }

    @Transactional
    public Permission createPermission(String permissionName) {
        Permission permission = new Permission();
        permission.setName(permissionName);
        return permissionRepository.save(permission);
    }

    @Transactional
    public void deletePermission(String permissionName) {
        Permission permission = permissionRepository.findByName(permissionName);
        if (permission != null) {
            permissionRepository.delete(permission);
        }
    }

    public List<Permission> getPermissionsForRole(String roleName) {
        Role role = roleRepository.findByName(roleName);
        if (role != null) {
            return role.getPermissions();
        }
        return null; // ou lancez une exception
    }

    @Transactional
    public Role createRole(String roleName, List<String> permissionNames) {
        Role role = new Role();
        role.setName(roleName);
        List<Permission> permissions = new ArrayList<>();
        for (String permissionName : permissionNames) {
            Permission permission = permissionRepository.findByName(permissionName);
            if (permission != null) {
                permissions.add(permission);
            }
        }
        role.setPermissions(permissions);
        return roleRepository.save(role);
    }

    @Transactional
    public void deleteRole(String roleName) {
        Role role = roleRepository.findByName(roleName);
        if (role != null) {
            roleRepository.delete(role);
        }
    }

    @Transactional
    public void addPermissionsToRole(String roleName, List<String> permissionNames) {
        Role role = roleRepository.findByName(roleName);
        List<Permission> permissions = new ArrayList<>();
        for (String permissionName : permissionNames) {
            Permission permission = permissionRepository.findByName(permissionName);
            if (permission != null) {
                permissions.add(permission);
            }
        }
        if (role != null) {
            role.getPermissions().addAll(permissions);
            roleRepository.save(role);
        }
    }

    @Transactional
    public void removePermissionsFromRole(String roleName, List<String> permissionNames) {
        Role role = roleRepository.findByName(roleName);
        List<Permission> permissions = new ArrayList<>();
        for (String permissionName : permissionNames) {
            Permission permission = permissionRepository.findByName(permissionName);
            if (permission != null) {
                permissions.add(permission);
            }
        }
        if (role != null) {
            role.getPermissions().removeAll(permissions);
            roleRepository.save(role);
        }
    }

    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }

    public List<Role> getRolesForUser(Long userId) {
        // Utiliser findByUserId au lieu de findById
        List<UserRole> userRoles = userRoleRepository.findByUserId(userId);

        return userRoles.stream()
                .map(userRole -> roleRepository.findById(userRole.getRoleId()).orElse(null))
                .filter(Objects::nonNull) // Filtrer les valeurs nulles
                .collect(Collectors.toList());
    }
}