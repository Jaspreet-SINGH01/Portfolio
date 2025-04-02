package com.videoflix.subscriptions_microservice.controllers;

import com.videoflix.subscriptions_microservice.entities.Permission;
import com.videoflix.subscriptions_microservice.entities.Role;
import com.videoflix.subscriptions_microservice.services.RoleService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/roles")
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @GetMapping
    public ResponseEntity<List<Role>> getAllRoles() {
        List<Role> roles = roleService.getAllRoles();
        return ResponseEntity.ok(roles);
    }

    @GetMapping("/{roleName}/permissions")
    public ResponseEntity<List<Permission>> getPermissionsForRole(@PathVariable String roleName) {
        List<Permission> permissions = roleService.getPermissionsForRole(roleName);
        if (permissions == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(permissions);
    }

    @GetMapping("/users/{userId}/roles")
    public ResponseEntity<List<Role>> getRolesForUser(@PathVariable Long userId) {
        List<Role> roles = roleService.getRolesForUser(userId);
        return ResponseEntity.ok(roles);
    }

    @PostMapping("/users/{userId}/roles")
    public ResponseEntity<String> assignRoleToUser(@PathVariable Long userId, @RequestParam String roleName) {
        roleService.assignRoleToUser(userId, roleName);
        return ResponseEntity.status(HttpStatus.CREATED).body("Role assigned to user.");
    }

    // Ajoutez d'autres points de terminaison pour gérer les rôles et les
    // permissions
}