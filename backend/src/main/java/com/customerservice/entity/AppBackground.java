package com.customerservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "app_backgrounds")
public class AppBackground {

    public static final String KEY_LOGIN_HEADER = "LOGIN_HEADER";
    public static final String KEY_LOGIN_BODY = "LOGIN_BODY";

    @Id
    @Column(name = "background_key", length = 50, nullable = false, updatable = false)
    private String backgroundKey;

    @Column(name = "image_url", nullable = false, columnDefinition = "TEXT")
    private String imageUrl = "";

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    void touchUpdatedAt() {
        updatedAt = LocalDateTime.now();
    }

    public String getBackgroundKey() {
        return backgroundKey;
    }

    public void setBackgroundKey(String backgroundKey) {
        this.backgroundKey = backgroundKey;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl != null ? imageUrl : "";
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
