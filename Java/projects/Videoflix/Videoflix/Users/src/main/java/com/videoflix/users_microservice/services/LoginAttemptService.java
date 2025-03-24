package com.videoflix.users_microservice.services;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.videoflix.users_microservice.entities.LoginAttempt;
import com.videoflix.users_microservice.repositories.LoginAttemptRepository;

@Service
public class LoginAttemptService {
    private static final int MAX_ATTEMPTS = 5;
    private static final int BLOCKING_TIME = 5;
    private final LoginAttemptRepository loginAttemptRepository;

    public LoginAttemptService(LoginAttemptRepository loginAttemptRepository) {
        this.loginAttemptRepository = loginAttemptRepository;
    }

    @Transactional
    public void recordFailedLogin(String username) {
        LoginAttempt attempt = new LoginAttempt();
        attempt.setUsername(username);
        attempt.setAttemptTime(LocalDateTime.now());
        loginAttemptRepository.save(attempt);
    }

    @Transactional
    public void resetLoginAttempts(String username) {
        LocalDateTime blockingTime = LocalDateTime.now().minusMinutes(BLOCKING_TIME);
        List<LoginAttempt> attempts = loginAttemptRepository.findByUsernameAndAttemptTimeAfter(username, blockingTime);
        loginAttemptRepository.deleteAll(attempts);
    }

    @Transactional(readOnly = true)
    public boolean isBlocked(String username) {
        LocalDateTime blockingTime = LocalDateTime.now().minusMinutes(BLOCKING_TIME);
        List<LoginAttempt> attempts = loginAttemptRepository.findByUsernameAndAttemptTimeAfter(username, blockingTime);
        return attempts.size() >= MAX_ATTEMPTS;
    }
}