package com.customerservice.service.mobile;

import com.customerservice.entity.mobile.ContentReviewEntity;
import com.customerservice.entity.mobile.DeliverableEntity;
import com.customerservice.entity.mobile.ImplementationItemEntity;
import com.customerservice.entity.mobile.ScheduleTaskEntity;
import com.customerservice.entity.mobile.ServiceDefinitionEntity;
import com.customerservice.entity.mobile.ServicePackageEntity;
import com.customerservice.entity.mobile.SubscriptionProgressEntity;
import com.customerservice.entity.mobile.SubscriptionServiceProgressEntity;
import com.customerservice.entity.mobile.UserSubscriptionEntity;
import com.customerservice.model.ContentReviewDto;
import com.customerservice.model.DeliverableDto;
import com.customerservice.model.DeliverableReviewResponse;
import com.customerservice.model.ImplementationItem;
import com.customerservice.model.MobileHomeResponse;
import com.customerservice.model.MobileHomeSchedule;
import com.customerservice.model.MobileServicesResponse;
import com.customerservice.model.PackageServiceInfo;
import com.customerservice.model.ProgressSummary;
import com.customerservice.model.ScheduleDay;
import com.customerservice.model.ScheduleTask;
import com.customerservice.model.ServiceProgressItem;
import com.customerservice.entity.mobile.NotificationEntity;
import com.customerservice.entity.mobile.ServicePackageEntity;
import com.customerservice.entity.mobile.UserVoucherEntity;
import com.customerservice.model.NotificationDto;
import com.customerservice.model.PackageCatalogItem;
import com.customerservice.model.SubscriptionSummary;
import com.customerservice.model.UserVoucherDto;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

public final class MobileDtoMapper {

    private MobileDtoMapper() {
    }

    public static SubscriptionSummary toSubscriptionSummary(UserSubscriptionEntity sub) {
        ServicePackageEntity pkg = sub.getServicePackage();
        String packageCode = sub.getPackageCode();
        String fallbackTier = packageCode != null && packageCode.toUpperCase().startsWith("PRO") ? "PRO" : "BASIC";
        SubscriptionSummary dto = new SubscriptionSummary();
        dto.setId(sub.getId());
        dto.setPackageCode(packageCode);
        dto.setTier(SubscriptionSummary.TierEnum.fromValue(pkg != null ? pkg.getTier() : fallbackTier));
        dto.setDisplayTitle(
                sub.getDisplayTitle() != null && !sub.getDisplayTitle().isBlank()
                        ? sub.getDisplayTitle()
                        : pkg != null ? pkg.getLabel() : packageCode
        );
        dto.setStartDate(sub.getStartDate());
        dto.setEndDate(sub.getEndDate());
        dto.setStatus(SubscriptionSummary.StatusEnum.fromValue(sub.getStatus()));
        dto.setDeploymentStatus(SubscriptionSummary.DeploymentStatusEnum.fromValue(sub.getDeploymentStatus()));
        return dto;
    }

    public static ProgressSummary toProgressSummary(
            ServicePackageEntity pkg,
            SubscriptionProgressEntity progress
    ) {
        int quotaTotal = pkg.getQuotaPosts() + pkg.getQuotaImages() + pkg.getQuotaVideos();
        int completed = Math.min(progress.getCompletedPosts(), pkg.getQuotaPosts())
                + Math.min(progress.getCompletedImages(), pkg.getQuotaImages())
                + Math.min(progress.getCompletedVideos(), pkg.getQuotaVideos());
        int percent = quotaTotal == 0 ? 0 : Math.round((completed * 100f) / quotaTotal);

        ProgressSummary dto = new ProgressSummary();
        dto.setOverallPercent(percent);
        dto.setCompletedPosts(progress.getCompletedPosts());
        dto.setCompletedImages(progress.getCompletedImages());
        dto.setCompletedVideos(progress.getCompletedVideos());
        dto.setQuotaPosts(pkg.getQuotaPosts());
        dto.setQuotaImages(pkg.getQuotaImages());
        dto.setQuotaVideos(pkg.getQuotaVideos());
        dto.setStatus(percent >= 100
                ? ProgressSummary.StatusEnum.DONE
                : percent > 0 ? ProgressSummary.StatusEnum.PROGRESS : ProgressSummary.StatusEnum.PENDING);
        return dto;
    }

    public static ServiceProgressItem toServiceProgressItem(
            ServiceDefinitionEntity def,
            int percent
    ) {
        ServiceProgressItem item = new ServiceProgressItem();
        item.setId(def.getId());
        item.setName(def.getName());
        item.setIcon(def.getIcon());
        item.setPercent(percent);
        item.setStatus(percent >= 100
                ? ServiceProgressItem.StatusEnum.DONE
                : percent > 0 ? ServiceProgressItem.StatusEnum.PROGRESS : ServiceProgressItem.StatusEnum.PENDING);
        return item;
    }

    public static List<ScheduleDay> buildWeekDays(LocalDate selectedDate) {
        int dow = selectedDate.getDayOfWeek().getValue();
        final LocalDate monday = selectedDate.minusDays(dow - 1L);

        return java.util.stream.IntStream.range(0, 7)
                .mapToObj(i -> {
                    LocalDate d = monday.plusDays(i);
                    ScheduleDay day = new ScheduleDay();
                    day.setDate(d);
                    day.setDayOfMonth(d.getDayOfMonth());
                    day.setDowIndex(i);
                    day.setSelected(d.equals(selectedDate));
                    return day;
                })
                .toList();
    }

    public static ScheduleTask toScheduleTask(ScheduleTaskEntity entity) {
        ScheduleTask task = new ScheduleTask();
        task.setId(entity.getId());
        task.setTitle(entity.getTitle());
        task.setScheduledTime(entity.getScheduledTime());
        return task;
    }

    public static ImplementationItem toImplementationItem(
            ImplementationItemEntity entity,
            UUID deliverableId,
            boolean reviewable,
            DeliverableEntity deliverable,
            ContentReviewEntity contentReview,
            ContentReviewEntity designReview
    ) {
        ImplementationItem dto = new ImplementationItem();
        dto.setId(entity.getId());
        dto.setCode(entity.getCode());
        dto.setTitle(entity.getTitle());
        dto.setCategory(ImplementationItem.CategoryEnum.fromValue(entity.getCategory()));
        dto.setCurrentCount(entity.getCurrentCount());
        dto.setTargetCount(entity.getTargetCount());
        dto.setStatus(ImplementationItem.StatusEnum.fromValue(entity.getStatus()));
        dto.setUpdatedOn(entity.getUpdatedOn());
        dto.setDeliverableId(deliverableId);
        dto.setReviewable(reviewable);
        if (deliverable != null) {
            dto.setPlannedPublishDate(deliverable.getPlannedPublishDate());
            dto.setTopic(firstText(deliverable.getTopic(), entity.getTitle()));
            dto.setIdeaFrame(deliverable.getIdeaFrame());
            dto.setPostContent(deliverable.getPostContent());
            dto.setContentStatus(ImplementationItem.ContentStatusEnum.fromValue(
                    firstText(deliverable.getContentStatus(), contentStatusFromImplementation(entity.getStatus()))
            ));
            dto.setAttachmentUrl(firstText(deliverable.getAttachmentUrl(), deliverable.getThumbnailUrl()));
            dto.setCompletedOn(deliverable.getCompletedOn());
            dto.setMediaName(firstText(deliverable.getMediaName(), entity.getTitle()));
            if (deliverable.getMediaType() != null && !deliverable.getMediaType().isBlank()) {
                dto.setMediaType(ImplementationItem.MediaTypeEnum.fromValue(deliverable.getMediaType()));
            }
            dto.setPreviewUrl(firstText(deliverable.getPreviewUrl(), deliverable.getThumbnailUrl()));
            if (deliverable.getTeamContentScore() != null) {
                dto.setContentScore(deliverable.getTeamContentScore().doubleValue());
            }
            if (deliverable.getTeamDesignScore() != null) {
                dto.setDesignScore(deliverable.getTeamDesignScore().doubleValue());
            }
            dto.setDesignCustomerComment(deliverable.getDesignCustomerComment());
            dto.setDesignImprovementSuggestion(deliverable.getDesignImprovementSuggestion());
        } else {
            dto.setTopic(entity.getTitle());
            dto.setContentStatus(ImplementationItem.ContentStatusEnum.fromValue(
                    contentStatusFromImplementation(entity.getStatus())
            ));
        }
        if (contentReview != null) {
            dto.setCustomerComment(contentReview.getComments());
            dto.setImprovementSuggestion(contentReview.getSuggestions());
            if (contentReview.getQualityScore() != null) {
                dto.setContentScore(contentReview.getQualityScore().doubleValue());
            }
        }
        if (designReview != null) {
            dto.setDesignCustomerComment(designReview.getComments());
            dto.setDesignImprovementSuggestion(designReview.getSuggestions());
            if (designReview.getQualityScore() != null) {
                dto.setDesignScore(designReview.getQualityScore().doubleValue());
            }
        }
        return dto;
    }

    public static ImplementationItem toImplementationItem(
            ImplementationItemEntity entity,
            UUID deliverableId,
            boolean reviewable,
            DeliverableEntity deliverable,
            ContentReviewEntity review
    ) {
        return toImplementationItem(entity, deliverableId, reviewable, deliverable, review, null);
    }

    public static ImplementationItem toImplementationItem(
            ImplementationItemEntity entity,
            UUID deliverableId,
            boolean reviewable
    ) {
        return toImplementationItem(entity, deliverableId, reviewable, null, null, null);
    }

    private static String firstText(String primary, String fallback) {
        return primary != null && !primary.isBlank() ? primary : fallback;
    }

    private static String contentStatusFromImplementation(String status) {
        if ("approved".equals(status)) {
            return "completed";
        }
        if ("waiting_feedback".equals(status)) {
            return "waiting_customer";
        }
        if ("in_progress".equals(status)) {
            return "doing";
        }
        return "not_started";
    }

    public static PackageServiceInfo toPackageServiceInfo(ServiceDefinitionEntity def) {
        PackageServiceInfo info = new PackageServiceInfo();
        info.setId(def.getId());
        info.setName(def.getName());
        info.setIcon(def.getIcon());
        info.setDescription(def.getDescription());
        return info;
    }

    public static PackageServiceInfo toPackageServiceInfo(
            ServiceDefinitionEntity def,
            ServiceProgressItem progress
    ) {
        PackageServiceInfo info = toPackageServiceInfo(def);
        info.setTrackMode(PackageServiceInfo.TrackModeEnum.fromValue(progress.getTrackMode().getValue()));
        info.setCompletedCount(progress.getCompletedCount());
        info.setTotalCount(progress.getTotalCount());
        info.setPercent(progress.getPercent());
        info.setStatus(PackageServiceInfo.StatusEnum.fromValue(progress.getStatus().getValue()));
        return info;
    }

    public static DeliverableDto toDeliverableDto(DeliverableEntity d, String taskTitle) {
        DeliverableDto dto = new DeliverableDto();
        dto.setId(d.getId());
        dto.setPostNumber(d.getPostNumber());
        dto.setThumbnailUrl(d.getThumbnailUrl());
        dto.setTaskTitle(taskTitle);
        dto.setCompanyResponseStatus(
                DeliverableDto.CompanyResponseStatusEnum.fromValue(d.getCompanyResponseStatus())
        );
        if (d.getTeamContentScore() != null) {
            dto.setTeamContentScore(d.getTeamContentScore().doubleValue());
        }
        if (d.getTeamDesignScore() != null) {
            dto.setTeamDesignScore(d.getTeamDesignScore().doubleValue());
        }
        return dto;
    }

    public static ContentReviewDto toContentReviewDto(ContentReviewEntity review) {
        ContentReviewDto dto = new ContentReviewDto();
        dto.setId(review.getId());
        dto.setQualityScore(review.getQualityScore());
        dto.setReviewType(ContentReviewDto.ReviewTypeEnum.fromValue(review.getReviewType()));
        dto.setComments(review.getComments());
        dto.setSuggestions(review.getSuggestions());
        dto.setStatus(ContentReviewDto.StatusEnum.fromValue(review.getStatus()));
        if (review.getSubmittedAt() != null) {
            dto.setSubmittedAt(review.getSubmittedAt().atOffset(ZoneOffset.UTC));
        }
        return dto;
    }

    public static DeliverableReviewResponse toDeliverableReviewResponse(
            DeliverableEntity deliverable,
            String taskTitle,
            ContentReviewEntity review
    ) {
        DeliverableReviewResponse response = new DeliverableReviewResponse();
        response.setDeliverable(toDeliverableDto(deliverable, taskTitle));
        if (review != null) {
            response.setReview(toContentReviewDto(review));
        }
        return response;
    }

    public static NotificationDto toNotificationDto(NotificationEntity entity) {
        NotificationDto dto = new NotificationDto();
        dto.setId(entity.getId());
        dto.setType(NotificationDto.TypeEnum.fromValue(entity.getType()));
        dto.setTitle(entity.getTitle());
        dto.setBody(entity.getBody());
        dto.setRead(entity.getReadAt() != null);
        dto.setCreatedAt(entity.getCreatedAt().atOffset(ZoneOffset.UTC));
        dto.setReferenceType(entity.getReferenceType());
        dto.setReferenceId(entity.getReferenceId());
        return dto;
    }

    public static PackageCatalogItem toPackageCatalogItem(ServicePackageEntity pkg) {
        PackageCatalogItem item = new PackageCatalogItem();
        item.setCode(pkg.getCode());
        item.setTier(PackageCatalogItem.TierEnum.fromValue(pkg.getTier()));
        item.setLabel(pkg.getLabel());
        item.setQuotaPosts(pkg.getQuotaPosts());
        item.setQuotaImages(pkg.getQuotaImages());
        item.setQuotaVideos(pkg.getQuotaVideos());
        return item;
    }

    public static UserVoucherDto toUserVoucherDto(UserVoucherEntity entity) {
        UserVoucherDto dto = new UserVoucherDto();
        dto.setId(entity.getId());
        dto.setCode(entity.getCode());
        dto.setTitle(entity.getTitle());
        dto.setDescription(entity.getDescription());
        dto.setExpiresAt(entity.getExpiresAt().atOffset(ZoneOffset.UTC));
        if (entity.getUsedAt() != null) {
            dto.setUsedAt(entity.getUsedAt().atOffset(ZoneOffset.UTC));
        }
        return dto;
    }
}
