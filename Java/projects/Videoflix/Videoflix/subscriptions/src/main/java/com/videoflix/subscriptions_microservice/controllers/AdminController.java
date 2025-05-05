package com.videoflix.subscriptions_microservice.controllers;

import com.videoflix.subscriptions_microservice.dtos.AdminUpdateRequest;
import com.videoflix.subscriptions_microservice.entities.Role;
import com.videoflix.subscriptions_microservice.entities.User;
import com.videoflix.subscriptions_microservice.services.RoleService;

import jakarta.validation.Valid;

import com.videoflix.subscriptions_microservice.repositories.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMINISTRATEUR')") // Sécurité au niveau du contrôleur
public class AdminController {

    private final UserRepository userRepository;
    private final RoleService roleService;

    public AdminController(UserRepository userRepository, RoleService roleService) {
        this.userRepository = userRepository;
        this.roleService = roleService;
    }

    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userRepository.findAll();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<User> getUserById(@PathVariable Long userId) {
        Optional<User> user = userRepository.findById(userId);
        return user.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/users/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId) {
        if (userRepository.existsById(userId)) {
            userRepository.deleteById(userId);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/roles")
    public ResponseEntity<List<Role>> getAllRoles() {
        return ResponseEntity.ok(roleService.getAllRoles());
    }

    @PostMapping("/users/{userId}/roles")
    public ResponseEntity<String> assignRoleToUser(@PathVariable Long userId, @RequestParam String roleName) {
        try {
            roleService.assignRoleToUser(userId, roleName);
            return ResponseEntity.ok("Role '" + roleName + "' assigned to user " + userId);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @GetMapping("/users/{userId}/roles")
    public ResponseEntity<List<Role>> getRolesForUser(@PathVariable Long userId) {
        try {
            List<Role> roles = roleService.getRolesForUser(userId);
            return ResponseEntity.ok(roles);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Collections.emptyList());
        }
    }

    @PostMapping("/users/{id}")
    public ResponseEntity<String> updateUser(@PathVariable Long id,
            @Valid @RequestBody AdminUpdateRequest updateRequest) {
        // Si la validation réussit, updateRequest contient des données valides
        return ResponseEntity.ok("Utilisateur mis à jour.");
    }
}