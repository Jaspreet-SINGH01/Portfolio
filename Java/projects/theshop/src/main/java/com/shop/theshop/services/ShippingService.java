// Package dans src/main/java
package com.shop.theshop.services;

import com.shop.theshop.entities.Shipping;
import com.shop.theshop.repositories.ShippingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ShippingService {

    private final ShippingRepository shippingRepository;

    @Autowired
    public ShippingService(ShippingRepository shippingRepository) {
        this.shippingRepository = shippingRepository;
    }

    public List<Shipping> getAllShipping() {
        return shippingRepository.findAll();
    }

    public Shipping getShippingById(Long id) {
        Optional<Shipping> shippingOptional = shippingRepository.findById(id);
        return shippingOptional.orElse(null);
    }

    public Shipping createShipping(Shipping shipping) {
        return shippingRepository.save(shipping);
    }

    public Shipping updateShipping(Long id, Shipping updatedShipping) {
        Optional<Shipping> existingShippingOptional = shippingRepository.findById(id);

        if (existingShippingOptional.isPresent()) {
            Shipping existingShipping = existingShippingOptional.get();

            existingShipping.setTrackingNumber(updatedShipping.getTrackingNumber());
            existingShipping.setShippingStatus(updatedShipping.getShippingStatus());

            return shippingRepository.save(existingShipping);
        } else {
            return null;
        }
    }

    public boolean deleteShipping(Long id) {
        if (shippingRepository.existsById(id)) {
            shippingRepository.deleteById(id);
            return true;
        } else {
            return false;
        }
    }
}
