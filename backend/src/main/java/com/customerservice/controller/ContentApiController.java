package com.customerservice.controller;

import com.customerservice.api.ContentApi;
import com.customerservice.model.LoginThemeResponse;
import com.customerservice.model.SaveLoginThemeRequest;
import com.customerservice.service.LoginThemeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

/** Theme màn login — frontend: {@code ContentService} → LoginView, LoginThemeAdminView. */
@RestController
public class ContentApiController implements ContentApi {

    private final LoginThemeService loginThemeService;

    public ContentApiController(LoginThemeService loginThemeService) {
        this.loginThemeService = loginThemeService;
    }

    /** GET /api/v1/content — Ảnh nền header/body login. */
    @Override
    public ResponseEntity<LoginThemeResponse> getLoginTheme() {
        return ResponseEntity.ok(loginThemeService.getLoginTheme());
    }

    /** POST /api/v1/content — Lưu theme (admin). */
    @Override
    public ResponseEntity<LoginThemeResponse> saveLoginTheme(SaveLoginThemeRequest saveLoginThemeRequest) {
        return ResponseEntity.ok(loginThemeService.saveLoginTheme(saveLoginThemeRequest));
    }
}
