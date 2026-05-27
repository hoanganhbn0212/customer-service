package com.customerservice.entity.mobile;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "service_definitions")
public class ServiceDefinitionEntity {

    @Id
    @Column(length = 32)
    private String id;

    @Column(length = 32)
    private String icon;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "tier_scope", nullable = false, length = 10)
    private String tierScope;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    /** QUANTITY = completed/total; STATUS = pending/progress/done only */
    @Column(name = "progress_mode", nullable = false, length = 16)
    private String progressMode = "STATUS";

    /** POSTS | IMAGES | VIDEOS when progressMode is QUANTITY */
    @Column(name = "quota_key", length = 16)
    private String quotaKey;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTierScope() {
        return tierScope;
    }

    public void setTierScope(String tierScope) {
        this.tierScope = tierScope;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }

    public String getProgressMode() {
        return progressMode;
    }

    public void setProgressMode(String progressMode) {
        this.progressMode = progressMode;
    }

    public String getQuotaKey() {
        return quotaKey;
    }

    public void setQuotaKey(String quotaKey) {
        this.quotaKey = quotaKey;
    }
}
