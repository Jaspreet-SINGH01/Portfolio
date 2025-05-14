package com.videoflix.subscriptions_microservice.services;

import com.videoflix.subscriptions_microservice.entities.User;
import com.videoflix.subscriptions_microservice.repositories.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User findByStripeCustomerId(String customerId) {
        return userRepository.findByStripeCustomerId(customerId).orElse(null);
    }
}
