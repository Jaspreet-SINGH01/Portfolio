package com.shop.theshop.services;

import com.shop.theshop.entities.User;
import com.shop.theshop.repositories.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User createUser(String email, String password, String name, String firstname) {
        User user = new User();
        user.setEmail(email);
        user.setPassword(password);
        user.setName(name);
        user.setFirstname(firstname);

        userRepository.save(user);

        return user;
    }
}

