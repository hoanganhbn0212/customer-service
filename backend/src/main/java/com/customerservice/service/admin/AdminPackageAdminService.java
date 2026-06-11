package com.customerservice.service.admin;

import com.customerservice.entity.mobile.NotificationEntity;
import com.customerservice.entity.mobile.PackageUpgradeRequestEntity;
import com.customerservice.entity.mobile.ServicePackageEntity;
import com.customerservice.model.AdminAssignSubscriptionRequest;
import com.customerservice.model.AdminUpsertPackageRequest;
import com.customerservice.model.PackageCatalogItem;
import com.customerservice.model.PackageUpgradeRequestAdminDto;
import com.customerservice.model.ReviewPackageUpgradeRequest;
import com.customerservice.repository.AppUserRepository;
import com.customerservice.repository.mobile.NotificationRepository;
import com.customerservice.repository.mobile.PackageUpgradeRequestRepository;
import com.customerservice.repository.mobile.ServicePackageRepository;
import com.customerservice.repository.mobile.UserSubscriptionRepository;
import com.customerservice.security.AuthContext;
import com.customerservice.service.mobile.MobileDtoMapper;
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
    private final UserSubscriptionRepository userSubscriptionRepository;

    public AdminPackageAdminService(
            ServicePackageRepository servicePackageRepository,
            PackageUpgradeRequestRepository upgradeRequestRepository,
            AppUserRepository appUserRepository,
            SubscriptionProvisioningService subscriptionProvisioningService,
            NotificationRepository notificationRepository,
            UserSubscriptionRepository userSubscriptionRepository
    ) {
        this.servicePackageRepository = servicePackageRepository;
        this.upgradeRequestRepository = upgradeRequestRepository;
        this.appUserRepository = appUserRepository;
        this.subscriptionProvisioningService = subscriptionProvisioningService;
        this.notificationRepository = notificationRepository;
        this.userSubscriptionRepository = userSubscriptionRepository;
    }

    @Transactional(readOnly = true)
    public List<PackageCatalogItem> listPackages() {
        AuthContext.requireAdmin();
        return servicePackageRepository.findAll().stream()
                .filter(ServicePackageEntity::isActive)
                .sorted((a, b) -> Integer.compare(a.getSortOrder(), b.getSortOrder()))
                .map(MobileDtoMapper::toPackageCatalogItem)
                .toList();
    }

    @Transactional
    public PackageCatalogItem createPackage(AdminUpsertPackageRequest request) {
        AuthContext.requireAdmin();
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "PACKAGE_REQUEST_REQUIRED");
        }
        String code = clean(request.getCode());
        if (code == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "PACKAGE_CODE_REQUIRED");
        }
        if (servicePackageRepository.existsById(code)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "PACKAGE_ALREADY_EXISTS");
        }

        ServicePackageEntity entity = new ServicePackageEntity();
        entity.setCode(code);
        applyPackageRequest(entity, request, true);
        return MobileDtoMapper.toPackageCatalogItem(servicePackageRepository.save(entity));
    }

    @Transactional
    public PackageCatalogItem updatePackage(String code, AdminUpsertPackageRequest request) {
        AuthContext.requireAdmin();
        ServicePackageEntity entity = servicePackageRepository.findById(code)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "PACKAGE_NOT_FOUND"));
        applyPackageRequest(entity, request, false);
        return MobileDtoMapper.toPackageCatalogItem(servicePackageRepository.save(entity));
    }

    @Transactional
    public void deactivatePackage(String code) {
        AuthContext.requireAdmin();
        ServicePackageEntity entity = servicePackageRepository.findById(code)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "PACKAGE_NOT_FOUND"));
        if (!userSubscriptionRepository.findAllActiveByPackageCode(code).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "PACKAGE_HAS_ACTIVE_SUBSCRIPTIONS");
        }
        entity.setActive(false);
        servicePackageRepository.save(entity);
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

    private void applyPackageRequest(ServicePackageEntity entity, AdminUpsertPackageRequest request, boolean creating) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "PACKAGE_REQUEST_REQUIRED");
        }
        String tier = clean(request.getTier());
        String label = clean(request.getLabel());
        if (creating && tier == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "PACKAGE_TIER_REQUIRED");
        }
        if (creating && label == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "PACKAGE_LABEL_REQUIRED");
        }
        if (tier != null) {
            String normalizedTier = tier.toUpperCase();
            if (!"BASIC".equals(normalizedTier) && !"PRO".equals(normalizedTier)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "INVALID_PACKAGE_TIER");
            }
            entity.setTier(normalizedTier);
        }
        if (label != null) {
            entity.setLabel(label);
        }
        if (request.getQuotaPosts() != null) {
            entity.setQuotaPosts(nonNegative(request.getQuotaPosts(), "INVALID_QUOTA_POSTS"));
        }
        if (request.getQuotaImages() != null) {
            entity.setQuotaImages(nonNegative(request.getQuotaImages(), "INVALID_QUOTA_IMAGES"));
        }
        if (request.getQuotaVideos() != null) {
            entity.setQuotaVideos(nonNegative(request.getQuotaVideos(), "INVALID_QUOTA_VIDEOS"));
        }
        if (request.getSortOrder() != null) {
            entity.setSortOrder(request.getSortOrder());
        }
        if (request.getActive() != null) {
            entity.setActive(request.getActive());
        } else if (creating) {
            entity.setActive(true);
        }
    }

    private int nonNegative(Integer value, String errorCode) {
        if (value < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, errorCode);
        }
        return value;
    }

    private String clean(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
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
