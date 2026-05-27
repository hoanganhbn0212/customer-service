package com.customerservice.repository;

import com.customerservice.entity.AppUser;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppUserRepository extends JpaRepository<AppUser, String> {

    Optional<AppUser> findFirstByUsernameIgnoreCaseAndEnabled(String username, String enabled);

    boolean existsByUsernameIgnoreCase(String username);

    List<AppUser> findAllByOrderByUsernameAsc();
}
