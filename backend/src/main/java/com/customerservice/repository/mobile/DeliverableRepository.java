package com.customerservice.repository.mobile;

import com.customerservice.entity.mobile.DeliverableEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeliverableRepository extends JpaRepository<DeliverableEntity, UUID> {

    Optional<DeliverableEntity> findFirstByImplementationItemId(UUID implementationItemId);

    List<DeliverableEntity> findBySubscriptionId(UUID subscriptionId);
}
