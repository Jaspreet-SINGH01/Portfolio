package com.videoflix.subscriptions_microservice.controllers;

import com.videoflix.subscriptions_microservice.services.StatsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/admin/stats/subscriptions")
public class AdminStatsController {

    private final StatsService statsService;

    public AdminStatsController(StatsService statsService) {
        this.statsService = statsService;
    }

    @GetMapping("/active-count")
    public ResponseEntity<Long> getActiveSubscriberCount() {
        return ResponseEntity.ok(statsService.getTotalActiveSubscribers());
    }

    @GetMapping("/new-count")
    public ResponseEntity<Long> getNewSubscriptionCount(
            @RequestParam(value = "startDate", required = false) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) LocalDate endDate) {
        if (startDate == null || endDate == null) {
            return ResponseEntity.badRequest().body(0L); // Ou une autre gestion d'erreur
        }
        return ResponseEntity.ok(statsService.getNewSubscriptionsCount(startDate, endDate));
    }

    @GetMapping("/by-type")
    public ResponseEntity<Map<Object, Long>> getSubscribersByType() {
        return ResponseEntity.ok(statsService.getSubscribersByType());
    }

    @GetMapping("/revenue")
    public ResponseEntity<Double> getTotalRevenue(
            @RequestParam(value = "startDate", required = false) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) LocalDate endDate) {
        if (startDate == null || endDate == null) {
            return ResponseEntity.badRequest().body(0.0); // Ou une autre gestion d'erreur
        }
        return ResponseEntity.ok(statsService.getTotalRevenue(startDate, endDate));
    }

    @GetMapping("/retention-rate")
    public ResponseEntity<Double> getRetentionRate(
            @RequestParam(value = "startDate", required = false) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) LocalDate endDate) {
        if (startDate == null || endDate == null) {
            return ResponseEntity.badRequest().body(0.0); // Ou une autre gestion d'erreur
        }
        return ResponseEntity.ok(statsService.getRetentionRate(startDate, endDate));
    }

    @GetMapping("/failed-payments-count")
    public ResponseEntity<Long> getFailedPaymentCount() {
        return ResponseEntity.ok(statsService.getFailedPaymentCount());
    }
}