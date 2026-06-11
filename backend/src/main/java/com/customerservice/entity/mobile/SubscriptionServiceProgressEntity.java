package com.customerservice.entity.mobile;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "subscription_service_progress")
public class SubscriptionServiceProgressEntity {

    @EmbeddedId
    private SubscriptionServiceProgressId id;

    @Column(nullable = false)
    private int percent;

    @Column(name = "completed_count")
    private Integer completedCount;

    @Column(name = "target_count")
    private Integer targetCount;

    public SubscriptionServiceProgressId getId() {
        return id;
    }

    public void setId(SubscriptionServiceProgressId id) {
        this.id = id;
    }

    public int getPercent() {
        return percent;
    }

    public void setPercent(int percent) {
        this.percent = percent;
    }

    public Integer getCompletedCount() {
        return completedCount;
    }

    public void setCompletedCount(Integer completedCount) {
        this.completedCount = completedCount;
    }

    public Integer getTargetCount() {
        return targetCount;
    }

    public void setTargetCount(Integer targetCount) {
        this.targetCount = targetCount;
    }
}
