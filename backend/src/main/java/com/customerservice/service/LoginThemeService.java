package com.customerservice.service;

import com.customerservice.entity.AppBackground;
import com.customerservice.model.LoginThemeResponse;
import com.customerservice.model.SaveLoginThemeRequest;
import com.customerservice.repository.AppBackgroundRepository;
import com.customerservice.security.AuthContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LoginThemeService {

    private final AppBackgroundRepository appBackgroundRepository;

    public LoginThemeService(AppBackgroundRepository appBackgroundRepository) {
        this.appBackgroundRepository = appBackgroundRepository;
    }

    @Transactional(readOnly = true)
    public LoginThemeResponse getLoginTheme() {
        return toResponse(
                findOrEmpty(AppBackground.KEY_LOGIN_HEADER),
                findOrEmpty(AppBackground.KEY_LOGIN_BODY)
        );
    }

    @Transactional
    public LoginThemeResponse saveLoginTheme(SaveLoginThemeRequest request) {
        AuthContext.requirePageEditor();

        String headerUrl = request.getHeaderBackgroundUrl() != null
                ? request.getHeaderBackgroundUrl().trim()
                : "";
        String bodyUrl = request.getBodyBackgroundUrl() != null
                ? request.getBodyBackgroundUrl().trim()
                : "";

        upsert(AppBackground.KEY_LOGIN_HEADER, headerUrl);
        upsert(AppBackground.KEY_LOGIN_BODY, bodyUrl);

        return toResponse(headerUrl, bodyUrl);
    }

    private String findOrEmpty(String key) {
        return appBackgroundRepository.findById(key)
                .map(AppBackground::getImageUrl)
                .orElse("");
    }

    private void upsert(String key, String imageUrl) {
        AppBackground row = appBackgroundRepository.findById(key).orElseGet(() -> {
            AppBackground created = new AppBackground();
            created.setBackgroundKey(key);
            return created;
        });
        row.setImageUrl(imageUrl);
        appBackgroundRepository.save(row);
    }

    private LoginThemeResponse toResponse(String headerUrl, String bodyUrl) {
        LoginThemeResponse response = new LoginThemeResponse();
        response.setHeaderBackgroundUrl(headerUrl);
        response.setBodyBackgroundUrl(bodyUrl);
        return response;
    }
}
