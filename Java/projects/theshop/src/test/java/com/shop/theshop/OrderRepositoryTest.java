package com.shop.theshop;

import com.shop.theshop.entities.Order;
import com.shop.theshop.entities.OrderStatus;
import com.shop.theshop.entities.User;
import com.shop.theshop.repositories.OrderRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class OrderRepositoryTest {

    @Autowired
    private OrderRepository orderRepository;

    @Test
    public void shouldSaveAndRetrieveOrder() {
        // Given
        User user = new User();
        user.setEmail("user@example.com");

        Order order = new Order();
        order.setUser(user);
        order.getStatus(OrderStatus.PENDING);

        // When
        orderRepository.save(order);

        // Then
        Order retrievedOrder = orderRepository.findById(order.getId()).orElse(null);
        assertThat(retrievedOrder).isNotNull();
        assertThat(retrievedOrder.getUser()).isEqualTo(user);
        assertThat(retrievedOrder.getStatus()).isEqualTo(OrderStatus.PENDING);
    }

    @Test
    public void shouldFindOrdersByUser() {
        // Given
        User user1 = new User();
        user1.setEmail("user1@example.com");

        User user2 = new User();
        user2.setEmail("user2@example.com");

        Order order1 = new Order();
        order1.setUser(user1);
        order1.setOrderStatus(OrderStatus.PENDING);

        Order order2 = new Order();
        order2.setUser(user2);
        order2.setOrderStatus(OrderStatus.SHIPPED);

        orderRepository.saveAll(List.of(order1, order2));

        // When
        List<Order> ordersByUser1 = orderRepository.findByUser(user1);
        List<Order> ordersByUser2 = orderRepository.findByUser(user2);

        // Then
        assertThat(ordersByUser1).hasSize(1);
        assertThat(ordersByUser1.get(0).getUser()).isEqualTo(user1);
        assertThat(ordersByUser1.get(0).getStatus()).isEqualTo(OrderStatus.PENDING);

        assertThat(ordersByUser2).hasSize(1);
        assertThat(ordersByUser2.get(0).getUser()).isEqualTo(user2);
        assertThat(ordersByUser2.get(0).getStatus()).isEqualTo(OrderStatus.SHIPPED);
    }
}
