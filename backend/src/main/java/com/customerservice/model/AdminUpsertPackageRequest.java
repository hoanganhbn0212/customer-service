package com.customerservice.model;

public class AdminUpsertPackageRequest {

    private String code;
    private String tier;
    private String label;
    private Integer quotaPosts;
    private Integer quotaImages;
    private Integer quotaVideos;
    private Boolean active;
    private Integer sortOrder;

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

    public Integer getQuotaPosts() {
        return quotaPosts;
    }

    public void setQuotaPosts(Integer quotaPosts) {
        this.quotaPosts = quotaPosts;
    }

    public Integer getQuotaImages() {
        return quotaImages;
    }

    public void setQuotaImages(Integer quotaImages) {
        this.quotaImages = quotaImages;
    }

    public Integer getQuotaVideos() {
        return quotaVideos;
    }

    public void setQuotaVideos(Integer quotaVideos) {
        this.quotaVideos = quotaVideos;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }
}
