package com.customerservice.service.mobile;

import com.customerservice.entity.mobile.ContentReviewEntity;
import com.customerservice.entity.mobile.DeliverableEntity;
import com.customerservice.entity.mobile.ImplementationItemEntity;
import com.customerservice.entity.mobile.ScheduleTaskEntity;
import com.customerservice.entity.mobile.ServicePackageEntity;
import com.customerservice.entity.mobile.SubscriptionProgressEntity;
import com.customerservice.entity.mobile.SubscriptionServiceProgressEntity;
import com.customerservice.entity.mobile.SubscriptionServiceProgressId;
import com.customerservice.entity.mobile.UserSubscriptionEntity;
import com.customerservice.model.AdminAssignSubscriptionRequest;
import com.customerservice.model.SubscriptionSummary;
import com.customerservice.repository.mobile.ContentReviewRepository;
import com.customerservice.repository.mobile.DeliverableRepository;
import com.customerservice.repository.mobile.ImplementationItemRepository;
import com.customerservice.repository.mobile.ScheduleTaskRepository;
import com.customerservice.repository.mobile.ServicePackageRepository;
import com.customerservice.repository.mobile.SubscriptionProgressRepository;
import com.customerservice.repository.mobile.SubscriptionServiceProgressRepository;
import com.customerservice.repository.mobile.UserSubscriptionRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class SubscriptionProvisioningService {

    private final UserSubscriptionRepository userSubscriptionRepository;
    private final ServicePackageRepository servicePackageRepository;
    private final SubscriptionProgressRepository subscriptionProgressRepository;
    private final SubscriptionServiceProgressRepository subscriptionServiceProgressRepository;
    private final ImplementationItemRepository implementationItemRepository;
    private final DeliverableRepository deliverableRepository;
    private final ContentReviewRepository contentReviewRepository;
    private final ScheduleTaskRepository scheduleTaskRepository;

    public SubscriptionProvisioningService(
            UserSubscriptionRepository userSubscriptionRepository,
            ServicePackageRepository servicePackageRepository,
            SubscriptionProgressRepository subscriptionProgressRepository,
            SubscriptionServiceProgressRepository subscriptionServiceProgressRepository,
            ImplementationItemRepository implementationItemRepository,
            DeliverableRepository deliverableRepository,
            ContentReviewRepository contentReviewRepository,
            ScheduleTaskRepository scheduleTaskRepository
    ) {
        this.userSubscriptionRepository = userSubscriptionRepository;
        this.servicePackageRepository = servicePackageRepository;
        this.subscriptionProgressRepository = subscriptionProgressRepository;
        this.subscriptionServiceProgressRepository = subscriptionServiceProgressRepository;
        this.implementationItemRepository = implementationItemRepository;
        this.deliverableRepository = deliverableRepository;
        this.contentReviewRepository = contentReviewRepository;
        this.scheduleTaskRepository = scheduleTaskRepository;
    }

    @Transactional
    public UserSubscriptionEntity assignDefaultForNewUser(String userId) {
        LocalDate start = LocalDate.now();
        LocalDate end = start.plusMonths(3);
        return assignPackage(
                userId,
                "BASIC_15",
                null,
                start,
                end,
                true
        );
    }

    @Transactional
    public SubscriptionSummary assignForAdmin(
            String userId,
            AdminAssignSubscriptionRequest request
    ) {
        ServicePackageEntity pkg = servicePackageRepository.findById(request.getPackageCode())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "INVALID_PACKAGE"));
        if (!pkg.isActive()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "INACTIVE_PACKAGE");
        }

        LocalDate start = request.getStartDate() != null ? request.getStartDate() : LocalDate.now();
        LocalDate end = request.getEndDate() != null ? request.getEndDate() : start.plusMonths(3);
        String title = request.getDisplayTitle() != null && !request.getDisplayTitle().isBlank()
                ? request.getDisplayTitle()
                : pkg.getLabel();

        boolean fullDemo = "PRO".equals(pkg.getTier());
        UserSubscriptionEntity sub = assignPackage(userId, pkg.getCode(), title, start, end, fullDemo);
        return MobileDtoMapper.toSubscriptionSummary(sub);
    }

    @Transactional
    public UserSubscriptionEntity assignProDemoForAdmin(String userId) {
        return assignPackage(
                userId,
                "PRO_15",
                null,
                LocalDate.of(2024, 5, 1),
                LocalDate.of(2024, 8, 1),
                true
        );
    }

    @Transactional
    public UserSubscriptionEntity assignPackage(
            String userId,
            String packageCode,
            String displayTitle,
            LocalDate startDate,
            LocalDate endDate,
            boolean fullDemo
    ) {
        expireActiveSubscriptions(userId);

        ServicePackageEntity pkg = servicePackageRepository.findById(packageCode)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "INVALID_PACKAGE"));
        if (!pkg.isActive()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "INACTIVE_PACKAGE");
        }

        UserSubscriptionEntity sub = new UserSubscriptionEntity();
        sub.setUserId(userId);
        sub.setPackageCode(packageCode);
        sub.setStatus("ACTIVE");
        sub.setStartDate(startDate);
        sub.setEndDate(endDate);
        sub.setDisplayTitle(displayTitle != null && !displayTitle.isBlank() ? displayTitle : pkg.getLabel());
        sub.setServicePackage(pkg);
        UserSubscriptionEntity saved = userSubscriptionRepository.save(sub);
        saved.setServicePackage(pkg);

        UUID subId = saved.getId();
        seedProgress(subId, pkg, fullDemo);
        seedImplementation(subId, userId, pkg.getTier(), fullDemo);
        if (fullDemo) {
            seedDemoSchedule(subId);
        }
        return saved;
    }

    private void expireActiveSubscriptions(String userId) {
        List<UserSubscriptionEntity> active = userSubscriptionRepository.findActiveByUserId(userId);
        for (UserSubscriptionEntity existing : active) {
            existing.setStatus("EXPIRED");
            userSubscriptionRepository.save(existing);
        }
    }

    private void seedProgress(UUID subId, ServicePackageEntity pkg, boolean fullDemo) {
        SubscriptionProgressEntity progress = new SubscriptionProgressEntity();
        progress.setSubscriptionId(subId);
        if (fullDemo) {
            progress.setCompletedPosts(13);
            progress.setCompletedImages(10);
            progress.setCompletedVideos(4);
        } else {
            progress.setCompletedPosts(0);
            progress.setCompletedImages(0);
            progress.setCompletedVideos(0);
        }
        subscriptionProgressRepository.save(progress);

        if ("PRO".equals(pkg.getTier())) {
            saveServiceProgress(subId, "fanpage", fullDemo ? 100 : 0);
            saveServiceProgress(subId, "content", fullDemo ? 70 : 0);
            saveServiceProgress(subId, "ads", fullDemo ? 60 : 0);
            saveServiceProgress(subId, "report", fullDemo ? 100 : 0);
            saveServiceProgress(subId, "cover", fullDemo ? 50 : 0);
            saveServiceProgress(subId, "like", fullDemo ? 40 : 0);
        }
    }

    private void seedImplementation(UUID subId, String userId, String tier, boolean fullDemo) {
        if ("PRO".equals(tier) && fullDemo) {
            ImplementationItemEntity designPost = saveImpl(subId, "design_post", "content", "Thiết kế bài đăng", 12, 24, "approved", LocalDate.now().minusDays(1), 1);
            ImplementationItemEntity seo = saveImpl(subId, "seo_content", "content", "Lên nội dung SEO", 8, 16, "in_progress", LocalDate.now().minusDays(2), 2);
            saveImpl(subId, "run_ads", "ads", "Chạy quảng cáo", 2, 5, "waiting_feedback", LocalDate.now().minusDays(3), 3);
            saveImpl(subId, "weekly_report", "report", "Báo cáo tuần", 23, 24, "approved", LocalDate.now().minusDays(4), 4);
            DeliverableEntity designDeliverable = saveDeliverable(subId, designPost, "1256",
                    "https://images.unsplash.com/photo-1556228578-0d85b1a4d571?w=160&h=160&fit=crop",
                    "8.5", "9.0", "responded",
                    LocalDate.of(2026, 6, 5),
                    "Giới thiệu dịch vụ chăm sóc da",
                    "Nêu vấn đề da khô, sau đó giới thiệu liệu trình chăm sóc da chuyên sâu",
                    "Nội dung chi tiết bài viết",
                    "completed",
                    "https://example.com/content/skincare-post.pdf",
                    LocalDate.of(2026, 6, 5),
                    "Banner ưu đãi tháng 6",
                    "image",
                    "https://images.unsplash.com/photo-1556228578-0d85b1a4d571?w=900&h=600&fit=crop",
                    "Màu sắc đẹp, bố cục rõ ràng",
                    "Có thể làm nổi bật nút ưu đãi hơn");
            saveSubmittedReview(designDeliverable.getId(), userId, 8,
                    "Nội dung ổn, cần thêm CTA mạnh hơn",
                    "Thêm lời kêu gọi đặt lịch và ưu đãi cuối bài");
            saveDeliverable(subId, seo, "1240",
                    "https://images.unsplash.com/photo-1570172619644-dfdcef6e0e0c?w=160&h=160&fit=crop",
                    "8.0", "8.5", "responded",
                    LocalDate.of(2026, 6, 8),
                    "Tối ưu nội dung SEO cho fanpage",
                    "Tập trung từ khóa dịch vụ chính, mở bài bằng pain point và kết thúc bằng CTA",
                    "Bài viết SEO nháp đang được hoàn thiện.",
                    "doing",
                    null,
                    LocalDate.of(2026, 6, 8),
                    "Video giới thiệu liệu trình",
                    "video",
                    null,
                    null,
                    null);
        } else if ("PRO".equals(tier)) {
            saveImpl(subId, "design_post", "content", "Thiết kế bài đăng", 0, 15, "in_progress", LocalDate.now(), 1);
            saveImpl(subId, "run_ads", "ads", "Chạy quảng cáo", 0, 5, "in_progress", LocalDate.now(), 2);
        } else {
            ImplementationItemEntity write = saveImpl(subId, "write_posts", "content", "Viết bài đăng", fullDemo ? 10 : 0, 15, "in_progress", LocalDate.now(), 1);
            saveImpl(subId, "design_images", "content", "Thiết kế hình ảnh", fullDemo ? 12 : 0, 15, "approved", LocalDate.now().minusDays(1), 2);
            if (fullDemo) {
                saveDeliverable(subId, write, "1180",
                        "https://images.unsplash.com/photo-1612817288484-6f933f271cf2?w=160&h=160&fit=crop",
                        "7.5", "8.0", "pending",
                        LocalDate.of(2026, 6, 5),
                        "Giới thiệu dịch vụ chăm sóc da",
                        "Nêu vấn đề da khô, sau đó giới thiệu liệu trình chăm sóc da chuyên sâu",
                        "Nội dung chi tiết bài viết",
                        "waiting_customer",
                        null,
                        LocalDate.of(2026, 6, 5),
                        "Banner ưu đãi tháng 6",
                        "image",
                        null,
                        null,
                        null);
            }
        }
    }

    private void seedDemoSchedule(UUID subId) {
        saveSchedule(subId, LocalDate.now(), "Viết bài đăng", "09:00", 1);
        saveSchedule(subId, LocalDate.now(), "Duyệt nội dung", "15:30", 2);
        saveSchedule(subId, LocalDate.now().plusDays(1), "Tối ưu quảng cáo", "10:00", 1);
    }

    private void saveServiceProgress(UUID subId, String serviceId, int percent) {
        SubscriptionServiceProgressEntity row = new SubscriptionServiceProgressEntity();
        row.setId(new SubscriptionServiceProgressId(subId, serviceId));
        row.setPercent(percent);
        subscriptionServiceProgressRepository.save(row);
    }

    private ImplementationItemEntity saveImpl(
            UUID subId,
            String code,
            String category,
            String title,
            int current,
            int target,
            String status,
            LocalDate updated,
            int sort
    ) {
        ImplementationItemEntity item = new ImplementationItemEntity();
        item.setSubscriptionId(subId);
        item.setCode(code);
        item.setCategory(category);
        item.setTitle(title);
        item.setCurrentCount(current);
        item.setTargetCount(target);
        item.setStatus(status);
        item.setUpdatedOn(updated);
        item.setSortOrder(sort);
        return implementationItemRepository.save(item);
    }

    private DeliverableEntity saveDeliverable(
            UUID subId,
            ImplementationItemEntity item,
            String postNumber,
            String thumb,
            String contentScore,
            String designScore,
            String responseStatus,
            LocalDate plannedPublishDate,
            String topic,
            String ideaFrame,
            String postContent,
            String contentStatus,
            String attachmentUrl,
            LocalDate completedOn,
            String mediaName,
            String mediaType,
            String previewUrl,
            String designCustomerComment,
            String designImprovementSuggestion
    ) {
        DeliverableEntity d = new DeliverableEntity();
        d.setSubscriptionId(subId);
        d.setImplementationItemId(item.getId());
        d.setPostNumber(postNumber);
        d.setPlannedPublishDate(plannedPublishDate);
        d.setTopic(topic);
        d.setIdeaFrame(ideaFrame);
        d.setPostContent(postContent);
        d.setContentStatus(contentStatus);
        d.setAttachmentUrl(attachmentUrl);
        d.setCompletedOn(completedOn);
        d.setMediaName(mediaName);
        d.setMediaType(mediaType);
        d.setPreviewUrl(previewUrl);
        d.setDesignCustomerComment(designCustomerComment);
        d.setDesignImprovementSuggestion(designImprovementSuggestion);
        d.setThumbnailUrl(thumb);
        d.setTeamContentScore(new BigDecimal(contentScore));
        d.setTeamDesignScore(new BigDecimal(designScore));
        d.setCompanyResponseStatus(responseStatus);
        return deliverableRepository.save(d);
    }

    private void saveSubmittedReview(
            UUID deliverableId,
            String userId,
            int score,
            String comments,
            String suggestions
    ) {
        if (contentReviewRepository.findByDeliverableIdAndUserIdAndStatus(deliverableId, userId, "SUBMITTED").isPresent()) {
            return;
        }
        ContentReviewEntity review = new ContentReviewEntity();
        review.setDeliverableId(deliverableId);
        review.setUserId(userId);
        review.setReviewType("CONTENT");
        review.setQualityScore(score);
        review.setComments(comments);
        review.setSuggestions(suggestions);
        review.setStatus("SUBMITTED");
        review.setSubmittedAt(java.time.LocalDateTime.now());
        contentReviewRepository.save(review);
    }

    private void saveSchedule(UUID subId, LocalDate date, String title, String time, int sort) {
        ScheduleTaskEntity task = new ScheduleTaskEntity();
        task.setSubscriptionId(subId);
        task.setTaskDate(date);
        task.setTitle(title);
        task.setScheduledTime(time);
        task.setSortOrder(sort);
        scheduleTaskRepository.save(task);
    }
}
