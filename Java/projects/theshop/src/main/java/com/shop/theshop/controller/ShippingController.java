package com.shop.theshop.controller;

import com.shop.theshop.entities.Shipping;
import com.shop.theshop.services.ShippingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/shipping")
public class ShippingController {

    private final ShippingService shippingService;

    @Autowired
    public ShippingController(ShippingService shippingService) {
        this.shippingService = shippingService;
    }

    @GetMapping
    public ResponseEntity<List<Shipping>> getAllShipping() {
        List<Shipping> shippingList = shippingService.getAllShipping();
        return new ResponseEntity<>(shippingList, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Shipping> getShippingById(@PathVariable Long id) {
        Shipping shipping = shippingService.getShippingById(id);
        if (shipping != null) {
            return new ResponseEntity<>(shipping, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping
    public ResponseEntity<Shipping> createShipping(@RequestBody Shipping shipping) {
        Shipping createdShipping = shippingService.createShipping(shipping);
        return new ResponseEntity<>(createdShipping, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Shipping> updateShipping(@PathVariable Long id, @RequestBody Shipping updatedShipping) {
        Shipping shipping = shippingService.updateShipping(id, updatedShipping);
        if (shipping != null) {
            return new ResponseEntity<>(shipping, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteShipping(@PathVariable Long id) {
        boolean deleted = shippingService.deleteShipping(id);
        if (deleted) {
            return new ResponseEntity<>(HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}
