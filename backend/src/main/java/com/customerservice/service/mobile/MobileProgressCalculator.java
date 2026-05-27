package com.customerservice.service.mobile;

import com.customerservice.entity.mobile.ServiceDefinitionEntity;
import com.customerservice.entity.mobile.ServicePackageEntity;
import com.customerservice.entity.mobile.SubscriptionProgressEntity;
import com.customerservice.model.ProgressSummary;
import com.customerservice.model.ServiceProgressItem;

/**
 * Tiến độ tổng thể = tổng hạng mục đã hoàn thành / tổng hạng mục cần làm (chỉ dịch vụ QUANTITY).
 */
public final class MobileProgressCalculator {

    public static final String MODE_QUANTITY = "QUANTITY";
    public static final String MODE_STATUS = "STATUS";

    private MobileProgressCalculator() {
    }

    public static ProgressSummary computeOverall(
            ServicePackageEntity pkg,
            SubscriptionProgressEntity progress
    ) {
        int completedPosts = capped(progress.getCompletedPosts(), pkg.getQuotaPosts());
        int completedImages = capped(progress.getCompletedImages(), pkg.getQuotaImages());
        int completedVideos = capped(progress.getCompletedVideos(), pkg.getQuotaVideos());

        int completedItems = completedPosts + completedImages + completedVideos;
        int totalItems = pkg.getQuotaPosts() + pkg.getQuotaImages() + pkg.getQuotaVideos();
        int overallPercent = totalItems == 0 ? 0 : Math.round((completedItems * 100f) / totalItems);

        ProgressSummary dto = new ProgressSummary();
        dto.setOverallPercent(overallPercent);
        dto.setCompletedItems(completedItems);
        dto.setTotalItems(totalItems);
        dto.setCompletedPosts(completedPosts);
        dto.setCompletedImages(completedImages);
        dto.setCompletedVideos(completedVideos);
        dto.setQuotaPosts(pkg.getQuotaPosts());
        dto.setQuotaImages(pkg.getQuotaImages());
        dto.setQuotaVideos(pkg.getQuotaVideos());
        dto.setStatus(statusFromPercent(overallPercent));
        return dto;
    }

    public static ServiceProgressItem computeServiceItem(
            ServiceDefinitionEntity def,
            ServicePackageEntity pkg,
            SubscriptionProgressEntity progress,
            int statusPercent
    ) {
        ServiceProgressItem item = new ServiceProgressItem();
        item.setId(def.getId());
        item.setName(def.getName());
        item.setIcon(def.getIcon());

        if (MODE_QUANTITY.equals(def.getProgressMode())) {
            int completed = quotaCompleted(def.getQuotaKey(), pkg, progress);
            int total = quotaTotal(def.getQuotaKey(), pkg);
            int percent = total == 0 ? 0 : Math.round((completed * 100f) / total);

            item.setTrackMode(ServiceProgressItem.TrackModeEnum.QUANTITY);
            item.setCompletedCount(completed);
            item.setTotalCount(total);
            item.setPercent(percent);
            item.setStatus(serviceStatusFromPercent(percent));
        } else {
            item.setTrackMode(ServiceProgressItem.TrackModeEnum.STATUS);
            item.setPercent(statusPercent);
            item.setStatus(serviceStatusFromPercent(statusPercent));
        }
        return item;
    }

    private static int capped(int completed, int quota) {
        if (quota <= 0) {
            return 0;
        }
        return Math.min(completed, quota);
    }

    private static int quotaCompleted(String quotaKey, ServicePackageEntity pkg, SubscriptionProgressEntity progress) {
        if (quotaKey == null) {
            return 0;
        }
        return switch (quotaKey) {
            case "POSTS" -> capped(progress.getCompletedPosts(), pkg.getQuotaPosts());
            case "IMAGES" -> capped(progress.getCompletedImages(), pkg.getQuotaImages());
            case "VIDEOS" -> capped(progress.getCompletedVideos(), pkg.getQuotaVideos());
            default -> 0;
        };
    }

    private static int quotaTotal(String quotaKey, ServicePackageEntity pkg) {
        if (quotaKey == null) {
            return 0;
        }
        return switch (quotaKey) {
            case "POSTS" -> pkg.getQuotaPosts();
            case "IMAGES" -> pkg.getQuotaImages();
            case "VIDEOS" -> pkg.getQuotaVideos();
            default -> 0;
        };
    }

    private static ProgressSummary.StatusEnum statusFromPercent(int percent) {
        if (percent >= 100) {
            return ProgressSummary.StatusEnum.DONE;
        }
        if (percent > 0) {
            return ProgressSummary.StatusEnum.PROGRESS;
        }
        return ProgressSummary.StatusEnum.PENDING;
    }

    private static ServiceProgressItem.StatusEnum serviceStatusFromPercent(int percent) {
        if (percent >= 100) {
            return ServiceProgressItem.StatusEnum.DONE;
        }
        if (percent > 0) {
            return ServiceProgressItem.StatusEnum.PROGRESS;
        }
        return ServiceProgressItem.StatusEnum.PENDING;
    }
}
