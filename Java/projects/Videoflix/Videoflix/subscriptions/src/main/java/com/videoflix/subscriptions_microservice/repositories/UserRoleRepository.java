package com.videoflix.subscriptions_microservice.repositories;

import com.videoflix.subscriptions_microservice.entities.User;
import com.videoflix.subscriptions_microservice.entities.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, Long> {
    List<UserRole> findByUserId(Long userId);

    List<UserRole> findByUser(User user);
}