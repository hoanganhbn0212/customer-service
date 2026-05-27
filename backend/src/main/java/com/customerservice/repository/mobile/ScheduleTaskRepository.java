package com.customerservice.repository.mobile;

import com.customerservice.entity.mobile.ScheduleTaskEntity;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScheduleTaskRepository extends JpaRepository<ScheduleTaskEntity, UUID> {

    List<ScheduleTaskEntity> findBySubscriptionIdAndTaskDateOrderBySortOrderAsc(
            UUID subscriptionId,
            LocalDate taskDate
    );
}
