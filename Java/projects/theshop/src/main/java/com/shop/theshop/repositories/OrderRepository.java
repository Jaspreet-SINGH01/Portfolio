package com.shop.theshop.repositories;

import com.shop.theshop.entities.Order;
import com.shop.theshop.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findById(User user1);
}
