package com.customerservice.repository.mobile;

import com.customerservice.entity.mobile.NotificationEntity;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<NotificationEntity, UUID> {

    Page<NotificationEntity> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);

    Page<NotificationEntity> findByUserIdAndReadAtIsNullOrderByCreatedAtDesc(
            String userId,
            Pageable pageable
    );

    long countByUserIdAndReadAtIsNull(String userId);
}
