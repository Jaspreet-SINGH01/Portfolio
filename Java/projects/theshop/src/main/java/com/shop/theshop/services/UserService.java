package com.shop.theshop.services;

import com.shop.theshop.entities.User;
import com.shop.theshop.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User findById(Long userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        return userOptional.orElse(null);
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

    public User updateUser(Long userId, User updatedUser) {
        Optional<User> userOptional = userRepository.findById(userId);

        if (userOptional.isPresent()) {
            User existingUser = userOptional.get();

            existingUser.setEmail(updatedUser.getEmail());
            existingUser.setPassword(updatedUser.getPassword());
            existingUser.setName(updatedUser.getName());
            existingUser.setFirstname(updatedUser.getFirstname());

            return userRepository.save(existingUser);
        } else {
            return null;
        }
    }

    public void deleteUser(Long userId) {
        userRepository.deleteById(userId);
    }

    public User createUser(User newUser) {
        return newUser;
    }
}
