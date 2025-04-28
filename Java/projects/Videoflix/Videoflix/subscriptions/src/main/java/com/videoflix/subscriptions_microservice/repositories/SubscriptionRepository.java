package com.videoflix.subscriptions_microservice.repositories;

import com.stripe.model.StripeObject;
import com.videoflix.subscriptions_microservice.entities.Subscription;
import com.videoflix.subscriptions_microservice.entities.Subscription.SubscriptionStatus;
import com.videoflix.subscriptions_microservice.entities.User;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface SubscriptionRepository
                extends JpaRepository<Subscription, Long>, JpaSpecificationExecutor<Subscription> {
        List<Subscription> findByUser(User user);

        long countByStatus(SubscriptionStatus active);

        long countByStartDateBetween(LocalDateTime startDate, LocalDateTime endDate);

        List<Subscription> findByStatusAndStartDateLessThanEqualAndEndDateGreaterThanEqual(SubscriptionStatus active,
                        LocalDateTime endDateTime, LocalDateTime startDateTime);

        long countByStatusAndEndDateGreaterThanEqual(SubscriptionStatus active, LocalDateTime endDateTime);

        long countByStatusAndStartDateLessThan(SubscriptionStatus active, LocalDateTime startDateTime);

        List<Subscription> findByEndDate(LocalDate today);

        List<Subscription> findByEndDateBeforeAndStatusNot(LocalDate today, SubscriptionStatus expired);

        List<Subscription> findByTrialEndDate(LocalDate today);

        List<Subscription> findByTrialEndDateBeforeAndStatus(LocalDate today, SubscriptionStatus trial);

        List<Subscription> findByStatusAndCancellationDateBefore(SubscriptionStatus cancelled,
                        LocalDate archiveThreshold);

        List<Subscription> findInactiveBefore(LocalDate deletionThreshold);

        long countByCreationTimestampBetween(LocalDateTime startOfYesterday, LocalDateTime endOfYesterday);

        long countByStatusAndCancellationDateBetween(SubscriptionStatus cancelled, LocalDate localDate,
                        LocalDate localDate2);

        List<Subscription> findByStatusAndLastPaymentDateBetween(SubscriptionStatus active, LocalDate minusDays,
                        LocalDate localDate);

        List<Subscription> findSubscriptionsUpdatedSince(LocalDateTime lastSyncTimestamp, int i, int batchSize);

        List<Subscription> findByStatusAndLastActivityBefore(Subscription.SubscriptionStatus status,
                        LocalDateTime lastActivityBefore, Pageable pageable);

        List<Subscription> findByNextBillingDate(LocalDate reminderDate);

        Optional<StripeObject> findByStripeSubscriptionId(String stripeSubscriptionId);

        Object findByEndDateBeforeAndStatusIn(LocalDateTime eq, List<Object> anyList);
}