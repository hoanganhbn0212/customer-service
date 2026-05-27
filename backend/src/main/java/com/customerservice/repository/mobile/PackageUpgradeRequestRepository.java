package com.customerservice.repository.mobile;

import com.customerservice.entity.mobile.PackageUpgradeRequestEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PackageUpgradeRequestRepository extends JpaRepository<PackageUpgradeRequestEntity, UUID> {

    List<PackageUpgradeRequestEntity> findByStatusOrderByCreatedAtDesc(String status);

    List<PackageUpgradeRequestEntity> findAllByOrderByCreatedAtDesc();
}
