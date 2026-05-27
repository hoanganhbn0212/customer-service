package com.customerservice.service.mobile;

import com.customerservice.entity.AppUser;
import com.customerservice.entity.mobile.PackageUpgradeRequestEntity;
import com.customerservice.entity.mobile.ServicePackageEntity;
import com.customerservice.entity.mobile.UserProfileEntity;
import com.customerservice.entity.mobile.UserSubscriptionEntity;
import com.customerservice.entity.mobile.UserVoucherEntity;
import com.customerservice.model.MobileAccountResponse;
import com.customerservice.model.PackageCatalogItem;
import com.customerservice.model.PackageUpgradeRequest;
import com.customerservice.model.PackageUpgradeResponse;
import com.customerservice.model.UpdateAccountRequest;
import com.customerservice.model.UserVoucherDto;
import com.customerservice.repository.AppUserRepository;
import com.customerservice.repository.mobile.PackageUpgradeRequestRepository;
import com.customerservice.repository.mobile.ServicePackageRepository;
import com.customerservice.repository.mobile.UserProfileRepository;
import com.customerservice.repository.mobile.UserSubscriptionRepository;
import com.customerservice.repository.mobile.UserVoucherRepository;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class MobileAccountService {

    private final AppUserRepository appUserRepository;
    private final UserProfileRepository userProfileRepository;
    private final UserSubscriptionRepository userSubscriptionRepository;
    private final ServicePackageRepository servicePackageRepository;
    private final PackageUpgradeRequestRepository packageUpgradeRequestRepository;
    private final UserVoucherRepository userVoucherRepository;
    private final MobileSubscriptionAccess subscriptionAccess;

    public MobileAccountService(
            AppUserRepository appUserRepository,
            UserProfileRepository userProfileRepository,
            UserSubscriptionRepository userSubscriptionRepository,
            ServicePackageRepository servicePackageRepository,
            PackageUpgradeRequestRepository packageUpgradeRequestRepository,
            UserVoucherRepository userVoucherRepository,
            MobileSubscriptionAccess subscriptionAccess
    ) {
        this.appUserRepository = appUserRepository;
        this.userProfileRepository = userProfileRepository;
        this.userSubscriptionRepository = userSubscriptionRepository;
        this.servicePackageRepository = servicePackageRepository;
        this.packageUpgradeRequestRepository = packageUpgradeRequestRepository;
        this.userVoucherRepository = userVoucherRepository;
        this.subscriptionAccess = subscriptionAccess;
    }

    @Transactional(readOnly = true)
    public MobileAccountResponse getAccount() {
        String userId = subscriptionAccess.currentUserId();
        AppUser user = appUserRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND"));

        MobileAccountResponse response = new MobileAccountResponse();
        response.setUserName(user.getUsername());

        userProfileRepository.findById(userId).ifPresent(profile -> {
            response.setFullName(profile.getFullName());
            response.setPhone(profile.getPhone());
            response.setEmail(profile.getEmail());
            response.setAvatarUrl(profile.getAvatarUrl());
        });

        List<UserSubscriptionEntity> active = userSubscriptionRepository.findActiveByUserId(userId);
        if (!active.isEmpty()) {
            response.setSubscription(MobileDtoMapper.toSubscriptionSummary(active.get(0)));
        }
        return response;
    }

    @Transactional
    public MobileAccountResponse updateAccount(UpdateAccountRequest request) {
        String userId = subscriptionAccess.currentUserId();
        UserProfileEntity profile = userProfileRepository.findById(userId).orElseGet(() -> {
            UserProfileEntity created = new UserProfileEntity();
            created.setUserId(userId);
            return created;
        });

        if (request.getFullName() != null) {
            profile.setFullName(request.getFullName());
        }
        if (request.getPhone() != null) {
            profile.setPhone(request.getPhone());
        }
        if (request.getEmail() != null) {
            profile.setEmail(request.getEmail());
        }
        if (request.getAvatarUrl() != null) {
            profile.setAvatarUrl(request.getAvatarUrl());
        }
        userProfileRepository.save(profile);
        return getAccount();
    }

    @Transactional(readOnly = true)
    public List<PackageCatalogItem> listPackages() {
        return servicePackageRepository.findAll().stream()
                .filter(ServicePackageEntity::isActive)
                .sorted((a, b) -> Integer.compare(a.getSortOrder(), b.getSortOrder()))
                .map(MobileDtoMapper::toPackageCatalogItem)
                .toList();
    }

    @Transactional
    public PackageUpgradeResponse requestUpgrade(PackageUpgradeRequest request) {
        String userId = subscriptionAccess.currentUserId();
        servicePackageRepository.findById(request.getToPackageCode())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "INVALID_PACKAGE"));

        UserSubscriptionEntity current = subscriptionAccess.requireActiveSubscription();

        PackageUpgradeRequestEntity entity = new PackageUpgradeRequestEntity();
        entity.setUserId(userId);
        entity.setFromPackageCode(current.getPackageCode());
        entity.setToPackageCode(request.getToPackageCode());
        entity.setNote(request.getNote());
        entity.setStatus("PENDING");
        packageUpgradeRequestRepository.save(entity);

        PackageUpgradeResponse response = new PackageUpgradeResponse();
        response.setId(entity.getId());
        response.setStatus(PackageUpgradeResponse.StatusEnum.PENDING);
        return response;
    }

    @Transactional(readOnly = true)
    public List<UserVoucherDto> listVouchers() {
        String userId = subscriptionAccess.currentUserId();
        return userVoucherRepository.findByUserIdOrderByExpiresAtDesc(userId).stream()
                .map(MobileDtoMapper::toUserVoucherDto)
                .toList();
    }
}
