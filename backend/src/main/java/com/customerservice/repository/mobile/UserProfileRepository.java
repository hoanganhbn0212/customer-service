package com.customerservice.repository.mobile;

import com.customerservice.entity.mobile.UserProfileEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserProfileRepository extends JpaRepository<UserProfileEntity, String> {
}
