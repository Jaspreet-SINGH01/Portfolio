package com.shop.theshop.services;

import com.shop.theshop.entities.Order;
import com.shop.theshop.repositories.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class OrderService {

    private final OrderRepository orderRepository;

    @Autowired
    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public Order getOrderById(Long id) {
        Optional<Order> optionalOrder = orderRepository.findById(id);
        return optionalOrder.orElse(null);
    }

    public Order createOrder(Order order) {
        // Ajoutez une logique de validation ou de traitement si nécessaire
        return orderRepository.save(order);
    }

    public Order updateOrder(Long id, Order updatedOrder) {
        // Vérifiez si la commande existe avant de la mettre à jour
        Optional<Order> optionalOrder = orderRepository.findById(id);
        if (optionalOrder.isPresent()) {
            Order existingOrder = optionalOrder.get();
            // Mettez à jour les champs nécessaires
            existingOrder.setTotalAmount(updatedOrder.getTotalAmount());
            // Ajoutez d'autres mises à jour si nécessaire
            return orderRepository.save(existingOrder);
        } else {
            // La commande n'existe pas, vous pouvez choisir de lever une exception ou de créer une nouvelle commande
            return null;
        }
    }

    public void deleteOrder(Long id) {
        orderRepository.deleteById(id);
    }
}
