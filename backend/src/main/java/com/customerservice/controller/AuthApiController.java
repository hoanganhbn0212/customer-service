package com.customerservice.controller;

import com.customerservice.api.AuthApi;
import com.customerservice.entity.AppUser;
import com.customerservice.model.LoginRequest;
import com.customerservice.model.LoginResponse;
import com.customerservice.model.RegisterRequest;
import com.customerservice.model.RegisterResponse;
import com.customerservice.model.UserProfile;
import com.customerservice.repository.AppUserRepository;
import com.customerservice.security.AuthContext;
import com.customerservice.service.AuthService;
import com.customerservice.service.JwtTokenService;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthApiController implements AuthApi {

    private final AuthService authService;
    private final JwtTokenService jwtTokenService;
    private final AppUserRepository appUserRepository;

    public AuthApiController(
            AuthService authService,
            JwtTokenService jwtTokenService,
            AppUserRepository appUserRepository
    ) {
        this.authService = authService;
        this.jwtTokenService = jwtTokenService;
        this.appUserRepository = appUserRepository;
    }

    @Override
    public ResponseEntity<RegisterResponse> register(RegisterRequest registerRequest) {
        AppUser user = authService.register(registerRequest.getUserName(), registerRequest.getPassword());
        RegisterResponse body = new RegisterResponse();
        body.setId(UUID.fromString(user.getId()));
        body.setUserName(user.getUsername());
        body.setEnabled(user.getEnabled());
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    @Override
    public ResponseEntity<LoginResponse> login(LoginRequest loginRequest) {
        AppUser user = authService.authenticate(loginRequest.getUserName(), loginRequest.getPassword());
        LoginResponse body = new LoginResponse();
        body.setAccessToken(jwtTokenService.createAccessToken(user));
        body.setTokenType("Bearer");
        body.setExpiresIn(jwtTokenService.getExpirationSeconds());
        body.setUserName(user.getUsername());
        body.setRole(LoginResponse.RoleEnum.fromValue(user.getRole()));
        return ResponseEntity.ok(body);
    }

    @Override
    public ResponseEntity<UserProfile> getCurrentUser() {
        var authUser = AuthContext.requireAuthenticated();
        AppUser user = appUserRepository.findById(authUser.id())
                .orElseThrow(() -> new IllegalStateException("User not found"));
        UserProfile profile = new UserProfile();
        profile.setId(user.getId());
        profile.setUserName(user.getUsername());
        profile.setRole(UserProfile.RoleEnum.fromValue(user.getRole()));
        profile.setEnabled(UserProfile.EnabledEnum.fromValue(user.getEnabled()));
        return ResponseEntity.ok(profile);
    }
}
