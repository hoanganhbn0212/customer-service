package com.customerservice.repository.mobile;

import com.customerservice.entity.mobile.UserSubscriptionEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserSubscriptionRepository extends JpaRepository<UserSubscriptionEntity, UUID> {

    @Query("""
            SELECT s FROM UserSubscriptionEntity s
            JOIN FETCH s.servicePackage
            WHERE s.userId = :userId AND s.status = 'ACTIVE'
            ORDER BY s.createdAt DESC
            """)
    java.util.List<UserSubscriptionEntity> findActiveByUserId(@Param("userId") String userId);
}
