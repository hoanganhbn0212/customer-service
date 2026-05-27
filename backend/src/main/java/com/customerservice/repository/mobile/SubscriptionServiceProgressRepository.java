package com.customerservice.repository.mobile;

import com.customerservice.entity.mobile.SubscriptionServiceProgressEntity;
import com.customerservice.entity.mobile.SubscriptionServiceProgressId;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SubscriptionServiceProgressRepository
        extends JpaRepository<SubscriptionServiceProgressEntity, SubscriptionServiceProgressId> {

    @Query("SELECT p FROM SubscriptionServiceProgressEntity p WHERE p.id.subscriptionId = :subscriptionId")
    List<SubscriptionServiceProgressEntity> findBySubscriptionId(@Param("subscriptionId") UUID subscriptionId);
}
