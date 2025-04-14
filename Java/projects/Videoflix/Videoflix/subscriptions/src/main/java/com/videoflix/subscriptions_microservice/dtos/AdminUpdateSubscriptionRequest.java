package com.videoflix.subscriptions_microservice.dtos;

import com.videoflix.subscriptions_microservice.entities.Subscription.SubscriptionStatus;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AdminUpdateSubscriptionRequest {
    private SubscriptionStatus status;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Long subscriptionLevelId;
}