package com.customerservice.repository.mobile;

import com.customerservice.entity.mobile.UserVoucherEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserVoucherRepository extends JpaRepository<UserVoucherEntity, UUID> {

    List<UserVoucherEntity> findByUserIdOrderByExpiresAtDesc(String userId);
}
