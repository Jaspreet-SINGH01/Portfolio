package com.shop.theshop;

import com.shop.theshop.entities.Order;
import com.shop.theshop.entities.OrderItem;
import com.shop.theshop.entities.OrderStatus;
import com.shop.theshop.entities.User;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class OrderTest {

    @Test
    public void shouldCreateOrder() {
        Long userId = 1L;
        User user = new User();
        user.setId(userId);

        List<OrderItem> orderItems = new ArrayList<>();
        OrderItem orderItem1 = new OrderItem();
        OrderItem orderItem2 = new OrderItem();
        orderItems.add(orderItem1);
        orderItems.add(orderItem2);

        OrderStatus status = OrderStatus.PENDING;

        Order order = new Order();
        order.setUser(user);
        order.setOrderItems(orderItems);
        order.setStatus(status);

        assertThat(order).isNotNull();
        assertThat(order.getUser()).isEqualTo(user);
        assertThat(order.getOrderItems()).isEqualTo(orderItems);
        assertThat(order.getStatus()).isEqualTo(status);
    }

    @Test
    public void shouldUpdateOrder() {
        Long orderId = 1L;
        OrderStatus initialStatus = OrderStatus.PENDING;
        OrderStatus updatedStatus = OrderStatus.SHIPPED;

        Order order = new Order();
        order.setId(orderId);
        order.setStatus(initialStatus);

        order.setStatus(updatedStatus);

        assertThat(order.getStatus()).isEqualTo(updatedStatus);
    }
}
