package com.customerservice.entity.mobile;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
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

    @Column(name = "planned_publish_date")
    private LocalDate plannedPublishDate;

    @Column(length = 240)
    private String topic;

    @Column(name = "idea_frame", columnDefinition = "TEXT")
    private String ideaFrame;

    @Column(name = "post_content", columnDefinition = "TEXT")
    private String postContent;

    @Column(name = "content_status", length = 32)
    private String contentStatus;

    @Column(name = "attachment_url", columnDefinition = "TEXT")
    private String attachmentUrl;

    @Column(name = "completed_on")
    private LocalDate completedOn;

    @Column(name = "media_name", length = 240)
    private String mediaName;

    @Column(name = "media_type", length = 16)
    private String mediaType;

    @Column(name = "preview_url", columnDefinition = "TEXT")
    private String previewUrl;

    @Column(name = "design_customer_comment", columnDefinition = "TEXT")
    private String designCustomerComment;

    @Column(name = "design_improvement_suggestion", columnDefinition = "TEXT")
    private String designImprovementSuggestion;

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

    public LocalDate getPlannedPublishDate() {
        return plannedPublishDate;
    }

    public void setPlannedPublishDate(LocalDate plannedPublishDate) {
        this.plannedPublishDate = plannedPublishDate;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getIdeaFrame() {
        return ideaFrame;
    }

    public void setIdeaFrame(String ideaFrame) {
        this.ideaFrame = ideaFrame;
    }

    public String getPostContent() {
        return postContent;
    }

    public void setPostContent(String postContent) {
        this.postContent = postContent;
    }

    public String getContentStatus() {
        return contentStatus;
    }

    public void setContentStatus(String contentStatus) {
        this.contentStatus = contentStatus;
    }

    public String getAttachmentUrl() {
        return attachmentUrl;
    }

    public void setAttachmentUrl(String attachmentUrl) {
        this.attachmentUrl = attachmentUrl;
    }

    public LocalDate getCompletedOn() {
        return completedOn;
    }

    public void setCompletedOn(LocalDate completedOn) {
        this.completedOn = completedOn;
    }

    public String getMediaName() {
        return mediaName;
    }

    public void setMediaName(String mediaName) {
        this.mediaName = mediaName;
    }

    public String getMediaType() {
        return mediaType;
    }

    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }

    public String getPreviewUrl() {
        return previewUrl;
    }

    public void setPreviewUrl(String previewUrl) {
        this.previewUrl = previewUrl;
    }

    public String getDesignCustomerComment() {
        return designCustomerComment;
    }

    public void setDesignCustomerComment(String designCustomerComment) {
        this.designCustomerComment = designCustomerComment;
    }

    public String getDesignImprovementSuggestion() {
        return designImprovementSuggestion;
    }

    public void setDesignImprovementSuggestion(String designImprovementSuggestion) {
        this.designImprovementSuggestion = designImprovementSuggestion;
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
