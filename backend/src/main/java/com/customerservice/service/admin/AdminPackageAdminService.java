package com.customerservice.service.admin;

import com.customerservice.entity.mobile.NotificationEntity;
import com.customerservice.entity.mobile.PackageUpgradeRequestEntity;
import com.customerservice.entity.mobile.ServicePackageEntity;
import com.customerservice.model.AdminAssignSubscriptionRequest;
import com.customerservice.model.PackageCatalogItem;
import com.customerservice.model.PackageUpgradeRequestAdminDto;
import com.customerservice.model.ReviewPackageUpgradeRequest;
import com.customerservice.repository.AppUserRepository;
import com.customerservice.repository.mobile.NotificationRepository;
import com.customerservice.repository.mobile.PackageUpgradeRequestRepository;
import com.customerservice.repository.mobile.ServicePackageRepository;
import com.customerservice.security.AuthContext;
import com.customerservice.service.mobile.SubscriptionProvisioningService;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AdminPackageAdminService {

    private final ServicePackageRepository servicePackageRepository;
    private final PackageUpgradeRequestRepository upgradeRequestRepository;
    private final AppUserRepository appUserRepository;
    private final SubscriptionProvisioningService subscriptionProvisioningService;
    private final NotificationRepository notificationRepository;

    public AdminPackageAdminService(
            ServicePackageRepository servicePackageRepository,
            PackageUpgradeRequestRepository upgradeRequestRepository,
            AppUserRepository appUserRepository,
            SubscriptionProvisioningService subscriptionProvisioningService,
            NotificationRepository notificationRepository
    ) {
        this.servicePackageRepository = servicePackageRepository;
        this.upgradeRequestRepository = upgradeRequestRepository;
        this.appUserRepository = appUserRepository;
        this.subscriptionProvisioningService = subscriptionProvisioningService;
        this.notificationRepository = notificationRepository;
    }

    @Transactional(readOnly = true)
    public List<PackageCatalogItem> listPackages() {
        AuthContext.requireAdmin();
        return servicePackageRepository.findAll().stream()
                .filter(ServicePackageEntity::isActive)
                .sorted((a, b) -> Integer.compare(a.getSortOrder(), b.getSortOrder()))
                .map(pkg -> {
                    PackageCatalogItem item = new PackageCatalogItem();
                    item.setCode(pkg.getCode());
                    item.setTier(PackageCatalogItem.TierEnum.fromValue(pkg.getTier()));
                    item.setLabel(pkg.getLabel());
                    item.setQuotaPosts(pkg.getQuotaPosts());
                    item.setQuotaImages(pkg.getQuotaImages());
                    item.setQuotaVideos(pkg.getQuotaVideos());
                    return item;
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PackageUpgradeRequestAdminDto> listUpgradeRequests(String status) {
        AuthContext.requireAdmin();
        List<PackageUpgradeRequestEntity> rows;
        if (status == null || status.isBlank()) {
            rows = upgradeRequestRepository.findAllByOrderByCreatedAtDesc();
        } else {
            rows = upgradeRequestRepository.findByStatusOrderByCreatedAtDesc(status.trim().toUpperCase());
        }
        return rows.stream().map(this::toAdminDto).toList();
    }

    @Transactional
    public PackageUpgradeRequestAdminDto reviewUpgradeRequest(UUID id, ReviewPackageUpgradeRequest request) {
        AuthContext.requireAdmin();

        PackageUpgradeRequestEntity entity = upgradeRequestRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "UPGRADE_REQUEST_NOT_FOUND"));

        if (!"PENDING".equals(entity.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "UPGRADE_REQUEST_NOT_PENDING");
        }

        String decision = request.getStatus().getValue();
        if (!"APPROVED".equals(decision) && !"REJECTED".equals(decision)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "INVALID_STATUS");
        }

        entity.setStatus(decision);
        entity.setAdminNote(request.getAdminNote());
        entity.setReviewedAt(LocalDateTime.now());
        upgradeRequestRepository.save(entity);

        String userName = appUserRepository.findById(entity.getUserId())
                .map(u -> u.getUsername())
                .orElse(entity.getUserId());

        if ("APPROVED".equals(decision)) {
            ServicePackageEntity targetPkg = servicePackageRepository.findById(entity.getToPackageCode())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "INVALID_PACKAGE"));

            AdminAssignSubscriptionRequest assign = new AdminAssignSubscriptionRequest();
            assign.setPackageCode(entity.getToPackageCode());
            assign.setDisplayTitle(targetPkg.getLabel());
            subscriptionProvisioningService.assignForAdmin(entity.getUserId(), assign);

            notifyUser(
                    entity.getUserId(),
                    "PROMOTION",
                    "Yêu cầu nâng cấp đã được duyệt",
                    "Gói của bạn đã được nâng cấp lên " + targetPkg.getLabel() + "."
            );
        } else {
            notifyUser(
                    entity.getUserId(),
                    "FEEDBACK_REPLY",
                    "Yêu cầu nâng cấp chưa được chấp nhận",
                    request.getAdminNote() != null && !request.getAdminNote().isBlank()
                            ? request.getAdminNote()
                            : "Yêu cầu nâng cấp lên " + entity.getToPackageCode() + " đã bị từ chối."
            );
        }

        PackageUpgradeRequestAdminDto dto = toAdminDto(entity);
        dto.setUserName(userName);
        return dto;
    }

    private void notifyUser(String userId, String type, String title, String body) {
        NotificationEntity n = new NotificationEntity();
        n.setUserId(userId);
        n.setType(type);
        n.setTitle(title);
        n.setBody(body);
        notificationRepository.save(n);
    }

    private PackageUpgradeRequestAdminDto toAdminDto(PackageUpgradeRequestEntity entity) {
        PackageUpgradeRequestAdminDto dto = new PackageUpgradeRequestAdminDto();
        dto.setId(entity.getId());
        dto.setUserId(entity.getUserId());
        dto.setUserName(
                appUserRepository.findById(entity.getUserId())
                        .map(u -> u.getUsername())
                        .orElse("—")
        );
        dto.setFromPackageCode(entity.getFromPackageCode());
        dto.setToPackageCode(entity.getToPackageCode());
        dto.setStatus(PackageUpgradeRequestAdminDto.StatusEnum.fromValue(entity.getStatus()));
        dto.setNote(entity.getNote());
        dto.setAdminNote(entity.getAdminNote());
        dto.setCreatedAt(entity.getCreatedAt().atOffset(ZoneOffset.UTC));
        if (entity.getReviewedAt() != null) {
            dto.setReviewedAt(entity.getReviewedAt().atOffset(ZoneOffset.UTC));
        }
        return dto;
    }
}
