package com.customerservice.entity.mobile;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "service_packages")
public class ServicePackageEntity {

    @Id
    @Column(length = 32)
    private String code;

    @Column(nullable = false, length = 10)
    private String tier;

    @Column(nullable = false, length = 120)
    private String label;

    @Column(name = "quota_posts", nullable = false)
    private int quotaPosts;

    @Column(name = "quota_images", nullable = false)
    private int quotaImages;

    @Column(name = "quota_videos", nullable = false)
    private int quotaVideos;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getTier() {
        return tier;
    }

    public void setTier(String tier) {
        this.tier = tier;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public int getQuotaPosts() {
        return quotaPosts;
    }

    public void setQuotaPosts(int quotaPosts) {
        this.quotaPosts = quotaPosts;
    }

    public int getQuotaImages() {
        return quotaImages;
    }

    public void setQuotaImages(int quotaImages) {
        this.quotaImages = quotaImages;
    }

    public int getQuotaVideos() {
        return quotaVideos;
    }

    public void setQuotaVideos(int quotaVideos) {
        this.quotaVideos = quotaVideos;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }
}
