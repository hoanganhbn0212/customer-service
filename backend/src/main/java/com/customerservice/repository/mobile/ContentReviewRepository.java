package com.customerservice.repository.mobile;

import com.customerservice.entity.mobile.ContentReviewEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContentReviewRepository extends JpaRepository<ContentReviewEntity, UUID> {

    Optional<ContentReviewEntity> findByDeliverableIdAndUserIdAndStatus(
            UUID deliverableId,
            String userId,
            String status
    );

    Optional<ContentReviewEntity> findFirstByDeliverableIdAndUserIdOrderByUpdatedAtDesc(
            UUID deliverableId,
            String userId
    );
}
