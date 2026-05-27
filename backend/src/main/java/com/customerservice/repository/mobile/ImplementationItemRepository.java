package com.customerservice.repository.mobile;

import com.customerservice.entity.mobile.ImplementationItemEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ImplementationItemRepository extends JpaRepository<ImplementationItemEntity, UUID> {

    List<ImplementationItemEntity> findBySubscriptionIdOrderBySortOrderAsc(UUID subscriptionId);
}
