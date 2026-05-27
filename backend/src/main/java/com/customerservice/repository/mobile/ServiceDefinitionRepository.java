package com.customerservice.repository.mobile;

import com.customerservice.entity.mobile.ServiceDefinitionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServiceDefinitionRepository extends JpaRepository<ServiceDefinitionEntity, String> {
}
