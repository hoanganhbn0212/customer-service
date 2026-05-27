package com.customerservice.service;

import com.customerservice.entity.AppUser;
import com.customerservice.exception.ForbiddenException;
import com.customerservice.exception.UsernameAlreadyExistsException;
import com.customerservice.model.AdminCreateUserRequest;
import com.customerservice.model.AdminUpdateUserRequest;
import com.customerservice.model.UserAccountResponse;
import com.customerservice.model.UserAccountResponse.EnabledEnum;
import com.customerservice.model.UserAccountResponse.RoleEnum;
import com.customerservice.repository.AppUserRepository;
import com.customerservice.security.AppRoles;
import com.customerservice.security.AuthContext;
import com.customerservice.security.AuthUser;
import java.util.List;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminUserService {

    public static final String STATUS_ACTIVE = "ACTIVE";
    public static final String STATUS_INACTIVE = "INACTIVE";
    public static final String ROLE_ADMIN = AppRoles.ADMIN;
    public static final String ROLE_DEVELOP = AppRoles.DEVELOP;
    public static final String ROLE_USER = AppRoles.USER;

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminUserService(AppUserRepository appUserRepository, PasswordEncoder passwordEncoder) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<UserAccountResponse> listUsers() {
        AuthContext.requireAdmin();
        return appUserRepository.findAllByOrderByUsernameAsc().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public UserAccountResponse createUser(AdminCreateUserRequest request) {
        AuthContext.requireAdmin();
        String userName = request.getUserName().trim();
        if (appUserRepository.existsByUsernameIgnoreCase(userName)) {
            throw new UsernameAlreadyExistsException(userName);
        }

        AppUser user = new AppUser();
        user.setUsername(userName);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(normalizeRole(request.getRole().getValue()));
        user.setEnabled(normalizeStatus(request.getEnabled().getValue()));
        return toResponse(appUserRepository.save(user));
    }

    @Transactional
    public UserAccountResponse updateUser(String id, AdminUpdateUserRequest request) {
        AuthUser current = AuthContext.requireAdmin();
        AppUser user = appUserRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (current.id().equals(id)) {
            if (request.getRole() != null && !ROLE_ADMIN.equals(normalizeRole(request.getRole().getValue()))) {
                throw new ForbiddenException();
            }
            if (request.getEnabled() != null && STATUS_INACTIVE.equals(normalizeStatus(request.getEnabled().getValue()))) {
                throw new ForbiddenException();
            }
        }

        if (request.getRole() != null) {
            user.setRole(normalizeRole(request.getRole().getValue()));
        }
        if (request.getEnabled() != null) {
            user.setEnabled(normalizeStatus(request.getEnabled().getValue()));
        }
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        }

        return toResponse(appUserRepository.save(user));
    }

    private UserAccountResponse toResponse(AppUser user) {
        UserAccountResponse response = new UserAccountResponse();
        response.setId(user.getId());
        response.setUserName(user.getUsername());
        response.setRole(RoleEnum.fromValue(user.getRole()));
        response.setEnabled(EnabledEnum.fromValue(user.getEnabled()));
        return response;
    }

    private String normalizeRole(String role) {
        return AppRoles.normalize(role);
    }

    private String normalizeStatus(String enabled) {
        if (enabled == null) {
            return STATUS_ACTIVE;
        }
        String value = enabled.trim().toUpperCase();
        if (!STATUS_ACTIVE.equals(value) && !STATUS_INACTIVE.equals(value)) {
            throw new IllegalArgumentException("Invalid status: " + enabled);
        }
        return value;
    }
}
