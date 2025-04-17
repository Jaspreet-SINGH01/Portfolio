package com.videoflix.subscriptions_microservice.controllers;

import com.videoflix.subscriptions_microservice.dtos.AdminUpdateSubscriptionRequest;
import com.videoflix.subscriptions_microservice.entities.Subscription;
import com.videoflix.subscriptions_microservice.services.SubscriptionService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin/subscriptions")
@PreAuthorize("hasRole('ADMIN')")
public class AdminSubscriptionController {

    private final SubscriptionService subscriptionService;

    public AdminSubscriptionController(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    // Méthodes pour lister et rechercher les abonnements

    @GetMapping("/list")
    public ResponseEntity<List<Subscription>> listAllSubscriptions(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        List<Subscription> subscriptions = subscriptionService.getAllSubscriptions(page, size);
        return new ResponseEntity<>(subscriptions, HttpStatus.OK);
    }

    @GetMapping("/search")
    public ResponseEntity<List<Subscription>> searchSubscriptions(
            @RequestParam(value = "userId", required = false) Long userId,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "subscriptionType", required = false) String subscriptionType,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        List<Subscription> subscriptions = subscriptionService.getAllSubscriptionsByFilters(userId, status,
                subscriptionType, page, size);
        return new ResponseEntity<>(subscriptions, HttpStatus.OK);
    }

    // Méthodes pour modifier les abonnements

    @PutMapping("/{id}")
    public ResponseEntity<Subscription> updateSubscription(
            @PathVariable Long id,
            @Valid @RequestBody AdminUpdateSubscriptionRequest updateRequest) {
        Subscription updatedSubscription = subscriptionService.updateSubscriptionByAdmin(id, updateRequest);
        return new ResponseEntity<>(updatedSubscription, HttpStatus.OK);
    }
}