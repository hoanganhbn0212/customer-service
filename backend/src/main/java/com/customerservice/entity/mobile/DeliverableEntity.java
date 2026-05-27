package com.customerservice.entity.mobile;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "deliverables")
public class DeliverableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "implementation_item_id")
    private UUID implementationItemId;

    @Column(name = "subscription_id", nullable = false)
    private UUID subscriptionId;

    @Column(name = "post_number", nullable = false, length = 32)
    private String postNumber;

    @Column(name = "thumbnail_url", columnDefinition = "TEXT")
    private String thumbnailUrl;

    @Column(name = "team_content_score", precision = 3, scale = 1)
    private BigDecimal teamContentScore;

    @Column(name = "team_design_score", precision = 3, scale = 1)
    private BigDecimal teamDesignScore;

    @Column(name = "company_response_status", nullable = false, length = 20)
    private String companyResponseStatus = "pending";

    public UUID getId() {
        return id;
    }

    public UUID getImplementationItemId() {
        return implementationItemId;
    }

    public void setImplementationItemId(UUID implementationItemId) {
        this.implementationItemId = implementationItemId;
    }

    public UUID getSubscriptionId() {
        return subscriptionId;
    }

    public void setSubscriptionId(UUID subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    public String getPostNumber() {
        return postNumber;
    }

    public void setPostNumber(String postNumber) {
        this.postNumber = postNumber;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public BigDecimal getTeamContentScore() {
        return teamContentScore;
    }

    public void setTeamContentScore(BigDecimal teamContentScore) {
        this.teamContentScore = teamContentScore;
    }

    public BigDecimal getTeamDesignScore() {
        return teamDesignScore;
    }

    public void setTeamDesignScore(BigDecimal teamDesignScore) {
        this.teamDesignScore = teamDesignScore;
    }

    public String getCompanyResponseStatus() {
        return companyResponseStatus;
    }

    public void setCompanyResponseStatus(String companyResponseStatus) {
        this.companyResponseStatus = companyResponseStatus;
    }
}
