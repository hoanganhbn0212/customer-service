package com.customerservice.controller;

import com.customerservice.api.ContentApi;
import com.customerservice.model.LoginThemeResponse;
import com.customerservice.model.SaveLoginThemeRequest;
import com.customerservice.service.LoginThemeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ContentApiController implements ContentApi {

    private final LoginThemeService loginThemeService;

    public ContentApiController(LoginThemeService loginThemeService) {
        this.loginThemeService = loginThemeService;
    }

    @Override
    public ResponseEntity<LoginThemeResponse> getLoginTheme() {
        return ResponseEntity.ok(loginThemeService.getLoginTheme());
    }

    @Override
    public ResponseEntity<LoginThemeResponse> saveLoginTheme(SaveLoginThemeRequest saveLoginThemeRequest) {
        return ResponseEntity.ok(loginThemeService.saveLoginTheme(saveLoginThemeRequest));
    }
}
