package com.customerservice.service;

import com.customerservice.entity.AppUser;
import com.customerservice.exception.InvalidCredentialsException;
import com.customerservice.exception.UsernameAlreadyExistsException;
import com.customerservice.repository.AppUserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private static final String STATUS_ACTIVE = "ACTIVE";

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(AppUserRepository appUserRepository, PasswordEncoder passwordEncoder) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
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
        return appUserRepository.save(user);
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
