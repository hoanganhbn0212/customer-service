package com.customerservice.repository.mobile;

import com.customerservice.entity.mobile.ServicePackageEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServicePackageRepository extends JpaRepository<ServicePackageEntity, String> {
}
