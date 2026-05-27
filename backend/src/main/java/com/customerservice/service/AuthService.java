package com.customerservice.service;

import com.customerservice.entity.AppUser;
import com.customerservice.exception.InvalidCredentialsException;
import com.customerservice.exception.UsernameAlreadyExistsException;
import com.customerservice.repository.AppUserRepository;
import com.customerservice.service.mobile.SubscriptionProvisioningService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private static final String STATUS_ACTIVE = "ACTIVE";
    private static final String ROLE_USER = "USER";

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final SubscriptionProvisioningService subscriptionProvisioningService;

    public AuthService(
            AppUserRepository appUserRepository,
            PasswordEncoder passwordEncoder,
            SubscriptionProvisioningService subscriptionProvisioningService
    ) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.subscriptionProvisioningService = subscriptionProvisioningService;
    }

    @Transactional
    public AppUser register(String username, String rawPassword) {
        String key = username == null ? "" : username.trim();
        if (key.isEmpty()) {
            throw new IllegalArgumentException("userName must not be blank");
        }

        if (appUserRepository.existsByUsernameIgnoreCase(key)) {
            throw new UsernameAlreadyExistsException(key);
        }

        AppUser user = new AppUser();
        user.setUsername(key);
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setEnabled(STATUS_ACTIVE);
        user.setRole(ROLE_USER);
        AppUser saved = appUserRepository.save(user);
        subscriptionProvisioningService.assignDefaultForNewUser(saved.getId());
        return saved;
    }

    @Transactional(readOnly = true)
    public AppUser authenticate(String username, String rawPassword) {
        String key = username == null ? "" : username.trim();

        AppUser user = appUserRepository
                .findFirstByUsernameIgnoreCaseAndEnabled(key, STATUS_ACTIVE)
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        return user;
    }
}
