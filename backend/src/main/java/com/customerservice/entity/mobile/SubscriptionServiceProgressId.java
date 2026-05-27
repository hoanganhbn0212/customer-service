package com.customerservice.entity.mobile;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class SubscriptionServiceProgressId implements Serializable {

    @Column(name = "subscription_id")
    private UUID subscriptionId;

    @Column(name = "service_id", length = 32)
    private String serviceId;

    public SubscriptionServiceProgressId() {
    }

    public SubscriptionServiceProgressId(UUID subscriptionId, String serviceId) {
        this.subscriptionId = subscriptionId;
        this.serviceId = serviceId;
    }

    public UUID getSubscriptionId() {
        return subscriptionId;
    }

    public void setSubscriptionId(UUID subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SubscriptionServiceProgressId that = (SubscriptionServiceProgressId) o;
        return Objects.equals(subscriptionId, that.subscriptionId) && Objects.equals(serviceId, that.serviceId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(subscriptionId, serviceId);
    }
}
