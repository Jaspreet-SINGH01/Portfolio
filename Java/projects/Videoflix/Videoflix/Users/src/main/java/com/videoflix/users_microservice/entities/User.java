package com.videoflix.users_microservice.entities;

import java.time.LocalDateTime;
import java.util.Set;

import com.videoflix.users_microservice.validation.annotations.UniqueUsername;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Entity
@Table(name = "users")
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "username", nullable = false)
    @UniqueUsername
    private String username;

    @Column(name = "email", nullable = false)
    @Email
    private String email;

    @Column(name = "password", nullable = false)
    @Pattern(regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d).{8,}$")
    private String password;

    @Column(name = "status")
    private String status;

    @Column(name = "role")
    private String role;

    @Column(name = "subscription_id")
    private Long subscriptionId;

    @Column(name = "reset_token")
    private String resetToken;

    @Column(name = "secret_key")
    private String secretKey;

    @Column(name = "is_2fa_enabled")
    private boolean is2faEnabled;

    @Column(name = "failed_login_attempts")
    private int failedLoginAttempts;

    @Column(name = "last_failed_login")
    private LocalDateTime lastFailedLogin;

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    @Column(name = "last_login_ip")
    private String lastLoginIp;

    @ElementCollection(targetClass = Permission.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "user_permissions", joinColumns = @JoinColumn(name = "user_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "permission")
    private Set<Permission> permissions;

    public Set<Permission> getPermissions() {
        return Role.valueOf(role).getPermissions();
    }

    @Column(name = "email_notifications_enabled")
    private boolean emailNotificationsEnabled;

    public boolean isEmailNotificationsEnabled() {
        return emailNotificationsEnabled;
    }

    public void setEmailNotificationsEnabled(boolean emailNotificationsEnabled) {
        this.emailNotificationsEnabled = emailNotificationsEnabled;
    }
}