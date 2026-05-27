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
}
