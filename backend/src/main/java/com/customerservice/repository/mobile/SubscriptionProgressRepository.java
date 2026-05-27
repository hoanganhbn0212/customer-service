package com.customerservice.repository.mobile;

import com.customerservice.entity.mobile.SubscriptionProgressEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubscriptionProgressRepository extends JpaRepository<SubscriptionProgressEntity, UUID> {
}
