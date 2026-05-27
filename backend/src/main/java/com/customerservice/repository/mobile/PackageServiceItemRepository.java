package com.customerservice.repository.mobile;

import com.customerservice.entity.mobile.PackageServiceItemEntity;
import com.customerservice.entity.mobile.PackageServiceItemId;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PackageServiceItemRepository extends JpaRepository<PackageServiceItemEntity, PackageServiceItemId> {

    @Query("SELECT p FROM PackageServiceItemEntity p WHERE p.id.packageCode = :packageCode ORDER BY p.id.serviceId")
    List<PackageServiceItemEntity> findByPackageCode(@Param("packageCode") String packageCode);
}
