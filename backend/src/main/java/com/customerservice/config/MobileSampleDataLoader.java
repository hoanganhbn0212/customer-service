package com.customerservice.config;

import com.customerservice.entity.mobile.NotificationEntity;
import com.customerservice.entity.mobile.PackageServiceItemEntity;
import com.customerservice.entity.mobile.PackageServiceItemId;
import com.customerservice.entity.mobile.ServiceDefinitionEntity;
import com.customerservice.entity.mobile.ServicePackageEntity;
import com.customerservice.entity.mobile.UserVoucherEntity;
import com.customerservice.entity.mobile.PackageUpgradeRequestEntity;
import com.customerservice.repository.mobile.NotificationRepository;
import com.customerservice.repository.mobile.PackageServiceItemRepository;
import com.customerservice.repository.mobile.PackageUpgradeRequestRepository;
import com.customerservice.repository.mobile.ServiceDefinitionRepository;
import com.customerservice.repository.mobile.ServicePackageRepository;
import com.customerservice.repository.mobile.UserSubscriptionRepository;
import com.customerservice.repository.mobile.UserVoucherRepository;
import com.customerservice.service.mobile.SubscriptionProvisioningService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Order(100)
public class MobileSampleDataLoader implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(MobileSampleDataLoader.class);
    private static final String ADMIN_USER_ID = "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11";
    private final ServicePackageRepository servicePackageRepository;
    private final ServiceDefinitionRepository serviceDefinitionRepository;
    private final PackageServiceItemRepository packageServiceItemRepository;
    private final UserSubscriptionRepository userSubscriptionRepository;
    private final SubscriptionProvisioningService subscriptionProvisioningService;
    private final NotificationRepository notificationRepository;
    private final UserVoucherRepository userVoucherRepository;
    private final PackageUpgradeRequestRepository packageUpgradeRequestRepository;

    public MobileSampleDataLoader(
            ServicePackageRepository servicePackageRepository,
            ServiceDefinitionRepository serviceDefinitionRepository,
            PackageServiceItemRepository packageServiceItemRepository,
            UserSubscriptionRepository userSubscriptionRepository,
            SubscriptionProvisioningService subscriptionProvisioningService,
            NotificationRepository notificationRepository,
            UserVoucherRepository userVoucherRepository,
            PackageUpgradeRequestRepository packageUpgradeRequestRepository
    ) {
        this.servicePackageRepository = servicePackageRepository;
        this.serviceDefinitionRepository = serviceDefinitionRepository;
        this.packageServiceItemRepository = packageServiceItemRepository;
        this.userSubscriptionRepository = userSubscriptionRepository;
        this.subscriptionProvisioningService = subscriptionProvisioningService;
        this.notificationRepository = notificationRepository;
        this.userVoucherRepository = userVoucherRepository;
        this.packageUpgradeRequestRepository = packageUpgradeRequestRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        seedPackages();
        seedDefinitions();
        seedPackageLinks();
        if (userSubscriptionRepository.findActiveByUserId(ADMIN_USER_ID).isEmpty()) {
            log.info("Seeding demo mobile subscription for admin user");
            subscriptionProvisioningService.assignProDemoForAdmin(ADMIN_USER_ID);
        }
        seedAdminNotifications();
        seedAdminVouchers();
        seedDemoUpgradeRequest();
    }

    private void seedPackages() {
        if (servicePackageRepository.count() > 0) {
            return;
        }
        savePackage("BASIC_15", "BASIC", "Gói Basic 15 bài", 15, 15, 0, 1);
        savePackage("PRO_15", "PRO", "Gói Pro 15 bài", 15, 10, 5, 2);
        savePackage("BASIC_30", "BASIC", "Gói Basic 30 bài", 30, 30, 0, 3);
        savePackage("PRO_30", "PRO", "Gói Pro 30 bài", 30, 20, 10, 4);
    }

    private void savePackage(String code, String tier, String label, int posts, int images, int videos, int sort) {
        ServicePackageEntity entity = new ServicePackageEntity();
        entity.setCode(code);
        entity.setTier(tier);
        entity.setLabel(label);
        entity.setQuotaPosts(posts);
        entity.setQuotaImages(images);
        entity.setQuotaVideos(videos);
        entity.setSortOrder(sort);
        servicePackageRepository.save(entity);
    }

    private void seedDefinitions() {
        if (serviceDefinitionRepository.count() > 0) {
            return;
        }
        saveDef("posts", "edit", "Viết bài đăng", "Viết bài theo kế hoạch nội dung.", "BASIC", 1);
        saveDef("design", "image", "Thiết kế hình ảnh", "Thiết kế hình ảnh cho bài đăng.", "BASIC", 2);
        saveDef("fanpage", "doc", "Chăm sóc Fanpage", "Quản trị và duy trì fanpage.", "PRO", 3);
        saveDef("content", "edit", "Sáng tạo nội dung", "Lên ý tưởng và nội dung đăng bài.", "PRO", 4);
        saveDef("ads", "ads", "Quảng cáo", "Tối ưu chiến dịch Facebook Ads.", "PRO", 5);
        saveDef("report", "chart", "Báo cáo", "Báo cáo hiệu suất định kỳ.", "PRO", 6);
        saveDef("cover", "image", "Ảnh bìa / Avatar", "Thiết kế ảnh bìa và avatar.", "PRO", 7);
        saveDef("like", "heart", "Like / Follow", "Tăng tương tác có kiểm soát.", "PRO", 8);
    }

    private void saveDef(String id, String icon, String name, String desc, String tier, int sort) {
        ServiceDefinitionEntity entity = new ServiceDefinitionEntity();
        entity.setId(id);
        entity.setIcon(icon);
        entity.setName(name);
        entity.setDescription(desc);
        entity.setTierScope(tier);
        entity.setSortOrder(sort);
        serviceDefinitionRepository.save(entity);
    }

    private void seedPackageLinks() {
        if (packageServiceItemRepository.count() > 0) {
            return;
        }
        link("PRO_15", "fanpage", "content", "ads", "report", "cover", "like");
        link("PRO_30", "fanpage", "content", "ads", "report", "cover", "like");
        link("BASIC_15", "posts", "design");
        link("BASIC_30", "posts", "design");
    }

    private void link(String pkg, String... serviceIds) {
        for (String serviceId : serviceIds) {
            PackageServiceItemEntity item = new PackageServiceItemEntity();
            item.setId(new PackageServiceItemId(pkg, serviceId));
            packageServiceItemRepository.save(item);
        }
    }

    private void seedAdminNotifications() {
        if (notificationRepository.count() > 0) {
            return;
        }
        saveNotification("FEEDBACK_REPLY", "Phản hồi từ Layla", "Team đã trả lời đánh giá bài viết #1256 của bạn.", false);
        saveNotification("PROMOTION", "Ưu đãi nâng cấp Pro", "Giảm 15% khi nâng cấp gói Pro trong tháng này.", false);
        saveNotification("SCHEDULE", "Nhắc lịch hôm nay", "Bạn có 2 công việc được lên lịch hôm nay.", true);
        saveNotification("FEEDBACK_REPLY", "Nội dung đã duyệt", "3 bài viết tuần này đã được duyệt.", true);
    }

    private void saveNotification(String type, String title, String body, boolean read) {
        NotificationEntity n = new NotificationEntity();
        n.setUserId(ADMIN_USER_ID);
        n.setType(type);
        n.setTitle(title);
        n.setBody(body);
        if (read) {
            n.setReadAt(LocalDateTime.now().minusHours(2));
        }
        notificationRepository.save(n);
    }

    private void seedAdminVouchers() {
        if (userVoucherRepository.count() > 0) {
            return;
        }
        UserVoucherEntity birthday = new UserVoucherEntity();
        birthday.setUserId(ADMIN_USER_ID);
        birthday.setCode("BDAY-2024");
        birthday.setTitle("Quà sinh nhật từ Layla");
        birthday.setDescription("Giảm 20% phí dịch vụ tháng tiếp theo.");
        birthday.setExpiresAt(LocalDateTime.now().plusDays(30));
        userVoucherRepository.save(birthday);

        UserVoucherEntity promo = new UserVoucherEntity();
        promo.setUserId(ADMIN_USER_ID);
        promo.setCode("PRO-UPGRADE");
        promo.setTitle("Voucher nâng cấp Pro");
        promo.setDescription("Áp dụng khi nâng cấp từ Basic lên Pro.");
        promo.setExpiresAt(LocalDateTime.now().plusDays(60));
        userVoucherRepository.save(promo);
    }

    private void seedDemoUpgradeRequest() {
        if (packageUpgradeRequestRepository.count() > 0) {
            return;
        }
        PackageUpgradeRequestEntity req = new PackageUpgradeRequestEntity();
        req.setUserId(ADMIN_USER_ID);
        req.setFromPackageCode("PRO_15");
        req.setToPackageCode("PRO_30");
        req.setStatus("PENDING");
        req.setNote("Muốn nâng lên gói 30 bài để tăng sản lượng nội dung.");
        packageUpgradeRequestRepository.save(req);
    }
}
