package com.videoflix.users_microservice.services;

import java.util.Base64;
import java.util.UUID;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.videoflix.users_microservice.entities.Role;
import com.videoflix.users_microservice.entities.User;
import com.videoflix.users_microservice.exceptions.AuthorizationException;
import com.videoflix.users_microservice.exceptions.InvalidEmailException;
import com.videoflix.users_microservice.exceptions.InvalidPasswordException;
import com.videoflix.users_microservice.exceptions.UserAlreadyExistsException;
import com.videoflix.users_microservice.exceptions.UserNotFoundException;
import com.videoflix.users_microservice.repositories.UserRepository;
import com.videoflix.users_microservice.services.async.NotificationServiceAsync;
import com.videoflix.users_microservice.utils.DataCleaner;

import jakarta.servlet.http.HttpServletRequest;

import com.videoflix.users_microservice.exceptions.EmailSendingException;

@Service
public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final HttpServletRequest request;

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
    private static final String PASSWORD_REGEX = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d).{8,}$";
    private static final String USER_NOT_FOUND_MESSAGE = "Utilisateur non trouvé avec l'ID : ";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;
    private final LoginAttemptService loginAttemptService;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder,
            JavaMailSender mailSender, LoginAttemptService loginAttemptService,
            HttpServletRequest request) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.mailSender = mailSender;
        this.loginAttemptService = loginAttemptService;
        this.request = request;
    }

    @Transactional(readOnly = true)
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND_MESSAGE + id));
    }

    // Création d'un utilisateur
    // Vérification de la validité du mot de passe
    // Vérification de la validité de l'email
    // Vérification de l'unicité du nom d'utilisateur et de l'email
    // Nettoyage des données entrantes
    // Création d'un nouvel utilisateur avec les données nettoyées
    // Envoi d'un email de bienvenue
    // Retour de l'utilisateur créé
    @Transactional
    public User createUser(User user, Role role) {
        if (!isValidPassword(user.getPassword())) {
            throw new InvalidPasswordException(
                    "Mot de passe invalide : doit contenir au moins 8 caractères, une majuscule, une minuscule et un chiffre.");
        }

        if (!isValidEmail(user.getEmail())) {
            throw new InvalidEmailException("Email invalide.");
        }

        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            throw new UserAlreadyExistsException("Nom d'utilisateur déjà utilisé : " + user.getUsername());
        }
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new UserAlreadyExistsException("Email déjà utilisé : " + user.getEmail());
        }

        // Nettoyer les données entrantes
        String safeUsername = DataCleaner.cleanHtml(user.getUsername());
        String safeEmail = DataCleaner.cleanHtml(user.getEmail());
        String safePassword = user.getPassword(); // Le mot de passe est haché, pas besoin de nettoyage HTML

        // Créer un nouvel utilisateur avec les données nettoyées
        User safeUser = new User();
        safeUser.setUsername(safeUsername);
        safeUser.setEmail(safeEmail);
        safeUser.setPassword(passwordEncoder.encode(safePassword));
        safeUser.setRole(role.name());

        User createdUser = userRepository.save(safeUser);

        logger.info("Utilisateur créé : ID={}, nom={}, email={}, rôle={}", createdUser.getId(),
                createdUser.getUsername(), createdUser.getEmail(), role);

        NotificationServiceAsync.sendWelcomeEmailAsync(user.getEmail(), user.getUsername());
        return createdUser;
    }

    // Mise à jour d'un utilisateur
    // Vérification de l'existence de l'utilisateur
    // Mise à jour des données de l'utilisateur
    // Enregistrement des modifications
    // Logging des modifications
    // Retour de l'utilisateur mis à jour
    @Transactional
    public User updateUser(Long id, String name, String email, Role role) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND_MESSAGE + id));

        user.setName(name);
        user.setEmail(email);
        user.setRole(role.name());
        user.setEmailNotificationsEnabled(false);
        User updatedUser = userRepository.save(user);

        logger.info("Utilisateur mis à jour : ID={}, nom={}, email={}, rôle={}", id, name, email, role);
        return updatedUser;
    }

    
    @Transactional
    public User updateUserSubscription(Long userId, Long subscriptionId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user != null) {
            user.setSubscriptionId(subscriptionId);
            return userRepository.save(user);
        }
        return null;
    }

    // Suppression d'un utilisateur
    // Vérification de l'existence de l'utilisateur
    // Vérification des autorisations
    // Suppression de l'utilisateur
    // Logging de la suppression
    @Transactional
    public void deleteUser(Long id, User currentUser) {
        User userToDelete = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND_MESSAGE + id));

        if (!currentUser.getRole().equals(Role.ADMIN.name()) && !currentUser.equals(userToDelete)) {
            throw new AuthorizationException(
                    "Vous n'avez pas les autorisations nécessaires pour supprimer cet utilisateur.");
        }

        userRepository.delete(userToDelete);
        logger.info("Utilisateur supprimé : ID={}, par ID={}", id, currentUser.getId());
    }

    // Vérification de la validité du mot de passe
    // Retour de true si le mot de passe est valide, false sinon
    private boolean isValidPassword(String password) {
        return password != null && password.matches(PASSWORD_REGEX);
    }

    private boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

    // Génération d'un jeton de réinitialisation
    // Recherche de l'utilisateur par email
    // Génération d'un jeton de réinitialisation sécurisé
    // Enregistrement du jeton dans la base de données
    // Envoi d'un email de réinitialisation
    // Logging de la génération du jeton
    // Exception si l'utilisateur n'est pas trouvé
    @Transactional
    public void generateResetToken(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("Utilisateur non trouvé avec l'email : " + email));

        String resetToken = generateSecureToken();
        user.setResetToken(resetToken);
        userRepository.save(user);

        sendResetEmail(user);
        logger.info("Token de réinitialisation généré pour l'utilisateur ID={}", user.getId());
    }

    private String generateSecureToken() {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(UUID.randomUUID().toString().getBytes());
    }

    private void sendResetEmail(User user) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(user.getEmail());
            message.setSubject("Réinitialisation de votre mot de passe");
            message.setText(
                    "Cliquez sur le lien suivant pour réinitialiser votre mot de passe : http://videoflix.com/reset-password?token="
                            + user.getResetToken());

            mailSender.send(message);
            logger.info("Email de réinitialisation envoyé à {}", user.getEmail());
        } catch (Exception e) {
            throw new EmailSendingException("Impossible d'envoyer l'email de réinitialisation à " + user.getEmail(), e);
        }
    }

    // Réinitialisation du mot de passe
    // Recherche de l'utilisateur par jeton de réinitialisation
    // Mise à jour du mot de passe
    // Suppression du jeton
    // Enregistrement des modifications
    // Logging de la réinitialisation
    @Transactional
    public void resetPassword(String resetToken, String newPassword) {
        User user = userRepository.findByResetToken(resetToken)
                .orElseThrow(() -> new UserNotFoundException("Jeton de réinitialisation invalide"));

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetToken(null); // Supprimer le jeton après utilisation
        userRepository.save(user);
        logger.info("Mot de passe réinitialisé pour l'utilisateur ID={}", user.getId());
    }

    // Authentification d'un utilisateur
    // Vérification si l'utilisateur est bloqué
    // Recherche de l'utilisateur par nom d'utilisateur
    // Vérification de la validité du mot de passe
    // Réinitialisation des essais de connexion
    // Retour de l'utilisateur authentifié
    @Transactional
    public User authenticate(String username, String password) {
        if (loginAttemptService.isBlocked(username)) {
            throw new AuthorizationException("Compte bloqué. Veuillez réessayer dans 5 minutes.");
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("Utilisateur non trouvé"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            loginAttemptService.recordFailedLogin(username);
            throw new AuthorizationException("Mot de passe incorrect");
        }

        // Enregistre l'adresse IP de la dernière connexion
        user.setLastLoginIp(request.getRemoteAddr());
        userRepository.save(user);

        loginAttemptService.resetLoginAttempts(username);
        return user;
    }

    // 2FA

    // Activation de la 2FA
    // Recherche de l'utilisateur par ID
    // Vérification de l'existence de l'utilisateur
    // Activation de la 2FA
    // Enregistrement des modifications
    // Logging de l'activation de la 2FA
    @Transactional
    public void enable2FA(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND_MESSAGE + userId));

        user.set2faEnabled(true);
        userRepository.save(user);
        logger.info("2FA activé pour l'utilisateur ID={}", userId);
    }

    @Transactional(readOnly = true)
    public boolean is2faEnabled(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND_MESSAGE + userId));
        return user.is2faEnabled();
    }

    // Désactivation de la 2FA
    // Recherche de l'utilisateur par ID
    // Vérification de l'existence de l'utilisateur
    // Désactivation de la 2FA
    // Enregistrement des modifications
    // Logging de la désactivation de la 2FA
    @Transactional
    public void disable2FA(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND_MESSAGE + userId));

        user.set2faEnabled(false);
        userRepository.save(user);
        logger.info("2FA désactivé pour l'utilisateur ID={}", userId);
    }

    // Génération d'une clé secrète pour la 2FA
    // Recherche de l'utilisateur par ID
    // Vérification de l'existence de l'utilisateur
    // Génération d'une clé secrète sécurisée
    // Enregistrement de la clé secrète dans la base de données
    // Retour de la clé secrète
    @Transactional
    public String generate2FASecretKey(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND_MESSAGE + userId));

        String secretKey = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(UUID.randomUUID().toString().getBytes());
        user.setSecretKey(secretKey);
        userRepository.save(user);

        return secretKey;
    }

    public String get2FAQRCodeUrl(String secretKey, String username) {
        return String.format("otpauth://totp/%s:%s?secret=%s&issuer=Videoflix",
                "Videoflix", username, secretKey);
    }

    public boolean verify2FACode(Long userId, String code) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND_MESSAGE + userId));

        if (!user.is2faEnabled()) {
            return true;
        }

        try {
            byte[] keyBytes = Base64.getUrlDecoder().decode(user.getSecretKey());
            long currentTime = System.currentTimeMillis() / 1000L;
            long timeStep = 30L;
            long t = currentTime / timeStep;

            for (int i = -1; i <= 1; i++) {
                String expectedCode = generateTOTP(keyBytes, t + i);
                if (expectedCode.equals(code)) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    private String generateTOTP(byte[] key, long t) {
        try {
            byte[] timeBytes = longToBytes(t);
            javax.crypto.Mac mac = javax.crypto.Mac.getInstance("HmacSHA1");
            mac.init(new javax.crypto.spec.SecretKeySpec(key, "HmacSHA1"));
            byte[] hash = mac.doFinal(timeBytes);
            int offset = hash[hash.length - 1] & 0xf;
            int binary = ((hash[offset] & 0x7f) << 24) |
                    ((hash[offset + 1] & 0xff) << 16) |
                    ((hash[offset + 2] & 0xff) << 8) |
                    (hash[offset + 3] & 0xff);
            int otp = binary % 1000000;
            return String.format("%06d", otp);
        } catch (Exception e) {
            return "";
        }
    }

    private byte[] longToBytes(long l) {
        byte[] result = new byte[8];
        for (int i = 7; i >= 0; i--) {
            result[i] = (byte) (l & 0xFF);
            l >>= 8;
        }
        return result;
    }

}