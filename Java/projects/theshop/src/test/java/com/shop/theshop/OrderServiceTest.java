package com.shop.theshop;

import com.shop.theshop.entities.Order;
import com.shop.theshop.repositories.OrderRepository;
import com.shop.theshop.services.OrderService;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@SpringBootTest
public class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderService orderService;

    @Test
    public void shouldGetAllOrders() {
        Order order1 = new Order();
        Order order2 = new Order();
        List<Order> orderList = Arrays.asList(order1, order2);

        when(orderRepository.findAll()).thenReturn(orderList);

        List<Order> result = orderService.getAllOrders();

        assertThat(result).hasSize(2);
        assertThat(result).contains(order1, order2);
    }

    @Test
    public void shouldGetOrderById() {
        Long orderId = 1L;
        Order order = new Order();
        order.setId(orderId);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        Order result = orderService.getOrderById(orderId);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(orderId);
    }

    @Test
    public void shouldCreateOrder() {
        Order order = new Order();

        when(orderRepository.save(order)).thenReturn(order);

        Order result = orderService.createOrder(order);

        assertThat(result).isNotNull();
    }

    @Test
    public void shouldUpdateOrder() {
        Long orderId = 1L;
        Order existingOrder = new Order();
        existingOrder.setId(orderId);

        Order updatedOrder = new Order();
        updatedOrder.setTotalAmount(BigDecimal.valueOf(100.00));

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(existingOrder));
        when(orderRepository.save(existingOrder)).thenReturn(existingOrder);

        Order result = orderService.updateOrder(orderId, updatedOrder);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(orderId);
        assertThat(result.getTotalAmount()).isEqualTo(100.00);
    }

    @Test
    public void shouldDeleteOrder() {
        Long orderId = 1L;

        orderService.deleteOrder(orderId);

        verify(orderRepository, times(1)).deleteById(orderId);
    }
}