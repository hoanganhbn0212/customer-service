package com.customerservice.entity.mobile;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "subscription_progress")
public class SubscriptionProgressEntity {

    @Id
    @Column(name = "subscription_id")
    private UUID subscriptionId;

    @Column(name = "completed_posts", nullable = false)
    private int completedPosts;

    @Column(name = "completed_images", nullable = false)
    private int completedImages;

    @Column(name = "completed_videos", nullable = false)
    private int completedVideos;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PreUpdate
    void touchUpdatedAt() {
        updatedAt = LocalDateTime.now();
    }

    public UUID getSubscriptionId() {
        return subscriptionId;
    }

    public void setSubscriptionId(UUID subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    public int getCompletedPosts() {
        return completedPosts;
    }

    public void setCompletedPosts(int completedPosts) {
        this.completedPosts = completedPosts;
    }

    public int getCompletedImages() {
        return completedImages;
    }

    public void setCompletedImages(int completedImages) {
        this.completedImages = completedImages;
    }

    public int getCompletedVideos() {
        return completedVideos;
    }

    public void setCompletedVideos(int completedVideos) {
        this.completedVideos = completedVideos;
    }
}
