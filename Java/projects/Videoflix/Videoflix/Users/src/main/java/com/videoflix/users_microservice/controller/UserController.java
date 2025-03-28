package com.videoflix.users_microservice.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import com.videoflix.users_microservice.dto.UpdateUserDTO;
import com.videoflix.users_microservice.dto.UserDTO;
import com.videoflix.users_microservice.entities.Role;
import com.videoflix.users_microservice.entities.User;
import com.videoflix.users_microservice.services.UserService;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequestMapping("/users")
@Tag(name = "Utilisateurs", description = "Opérations liées aux utilisateurs")
public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // Méthode GET pour récupérer le profil de l'utilisateur connecté
    @GetMapping("/profile")
    public ResponseEntity<UserDTO> getUserProfile(@AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());

        logger.info("Récupération du profil pour l'utilisateur ID : {}", userId);

        User user = userService.getUserById(userId);
        UserDTO userDTO = convertToDTO(user);

        return ResponseEntity.ok(userDTO);
    }

    @PostMapping
    public ResponseEntity<UserDTO> createUser(@Valid @RequestBody UserDTO userDTO) {
        logger.info("Tentative de création d'un nouvel utilisateur avec l'email : {}", userDTO.getEmail());

        User user = convertToEntity(userDTO);
        User createdUser = userService.createUser(user, Role.USER);

        UserDTO createdUserDTO = convertToDTO(createdUser);
        logger.info("Utilisateur créé avec succès. ID : {}", createdUser.getId());

        return ResponseEntity.status(HttpStatus.CREATED).body(createdUserDTO);
    }

    @PutMapping("/profile")
    public ResponseEntity<UserDTO> updateUser(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UpdateUserDTO updateUserDto) {

        Long userId = Long.parseLong(userDetails.getUsername());
        Role userRole = Role.valueOf(userDetails.getAuthorities().iterator().next().getAuthority());

        User updatedUser = userService.updateUser(
                userId,
                updateUserDto.getName(),
                updateUserDto.getEmail(),
                userRole);

        return ResponseEntity.ok(convertToDTO(updatedUser));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(
            @PathVariable Long id,
            @RequestParam(value = "confirm", required = false) Boolean confirm,
            @AuthenticationPrincipal UserDetails userDetails) {

        Long currentUserId = Long.parseLong(userDetails.getUsername());

        // Vérifier que l'utilisateur ne supprime que son propre compte
        if (confirm == null || !confirm || !currentUserId.equals(id)) {
            logger.warn("Tentative de suppression non autorisée. ID demandé : {}, ID connecté : {}",
                    id, currentUserId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        userService.deleteUser(id, (User) userDetails);
        logger.info("Utilisateur supprimé avec succès. ID : {}", id);

        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/subscription")
    public ResponseEntity<User> updateUserSubscription(@PathVariable Long id, @RequestBody Long subscriptionId) {
        User updatedUser = userService.updateUserSubscription(id, subscriptionId);
        return ResponseEntity.ok(updatedUser);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Void> forgotPassword(@RequestParam String email) {
        if (!StringUtils.hasText(email)) {
            return ResponseEntity.badRequest().build();
        }

        logger.info("Demande de réinitialisation de mot de passe pour l'email : {}", email);
        userService.generateResetToken(email);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(
            @RequestParam String token,
            @RequestParam String newPassword) {

        if (!StringUtils.hasText(token) || !StringUtils.hasText(newPassword)) {
            logger.warn("Tentative de réinitialisation de mot de passe avec des paramètres invalides");
            return ResponseEntity.badRequest().build();
        }

        userService.resetPassword(token, newPassword);
        logger.info("Mot de passe réinitialisé avec succès");

        return ResponseEntity.ok().build();
    }

    // Méthodes 2FA

    // Méthode GET pour vérifier si 2FA est activé
    @GetMapping("/2fa/status")
    public ResponseEntity<Boolean> get2FAStatus(@AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());

        logger.info("Vérification du statut 2FA pour l'utilisateur ID : {}", userId);

        boolean is2faEnabled = userService.is2faEnabled(userId);

        return ResponseEntity.ok(is2faEnabled);
    }

    @PostMapping("/2fa/generate")
    public ResponseEntity<String> generate2FA(@AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        String secretKey = userService.generate2FASecretKey(userId);

        return ResponseEntity.ok(userService.get2FAQRCodeUrl(secretKey, userDetails.getUsername()));
    }

    @PostMapping("/2fa/verify")
    public ResponseEntity<Boolean> verify2FA(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam String code) {

        Long userId = Long.parseLong(userDetails.getUsername());
        return ResponseEntity.ok(userService.verify2FACode(userId, code));
    }

    @PostMapping("/2fa/enable")
    public ResponseEntity<Void> enable2FA(@AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        userService.enable2FA(userId);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/2fa/disable")
    public ResponseEntity<Void> disable2FA(@AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        userService.disable2FA(userId);

        return ResponseEntity.ok().build();
    }

    // Méthodes de conversion
    private UserDTO convertToDTO(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }

        UserDTO userDTO = new UserDTO();
        userDTO.setId(user.getId());
        userDTO.setUsername(user.getName());
        userDTO.setEmail(user.getEmail());

        return userDTO;
    }

    private User convertToEntity(UserDTO userDTO) {
        if (userDTO == null) {
            throw new IllegalArgumentException("UserDTO cannot be null");
        }

        User user = new User();
        user.setName(userDTO.getUsername());
        user.setEmail(userDTO.getEmail());
        user.setPassword(userDTO.getPassword());

        return user;
    }
}