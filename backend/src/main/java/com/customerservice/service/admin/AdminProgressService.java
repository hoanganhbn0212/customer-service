package com.customerservice.service.admin;

import com.customerservice.entity.AppUser;
import com.customerservice.entity.mobile.PackageServiceItemEntity;
import com.customerservice.entity.mobile.ServiceDefinitionEntity;
import com.customerservice.entity.mobile.SubscriptionProgressEntity;
import com.customerservice.entity.mobile.SubscriptionServiceProgressEntity;
import com.customerservice.entity.mobile.SubscriptionServiceProgressId;
import com.customerservice.entity.mobile.UserSubscriptionEntity;
import com.customerservice.repository.AppUserRepository;
import com.customerservice.repository.mobile.PackageServiceItemRepository;
import com.customerservice.repository.mobile.ServiceDefinitionRepository;
import com.customerservice.repository.mobile.SubscriptionProgressRepository;
import com.customerservice.repository.mobile.SubscriptionServiceProgressRepository;
import com.customerservice.repository.mobile.UserSubscriptionRepository;
import com.customerservice.security.AuthContext;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AdminProgressService {

    private final UserSubscriptionRepository userSubscriptionRepository;
    private final AppUserRepository appUserRepository;
    private final SubscriptionProgressRepository subscriptionProgressRepository;
    private final SubscriptionServiceProgressRepository subscriptionServiceProgressRepository;
    private final PackageServiceItemRepository packageServiceItemRepository;
    private final ServiceDefinitionRepository serviceDefinitionRepository;

    public AdminProgressService(
            UserSubscriptionRepository userSubscriptionRepository,
            AppUserRepository appUserRepository,
            SubscriptionProgressRepository subscriptionProgressRepository,
            SubscriptionServiceProgressRepository subscriptionServiceProgressRepository,
            PackageServiceItemRepository packageServiceItemRepository,
            ServiceDefinitionRepository serviceDefinitionRepository
    ) {
        this.userSubscriptionRepository = userSubscriptionRepository;
        this.appUserRepository = appUserRepository;
        this.subscriptionProgressRepository = subscriptionProgressRepository;
        this.subscriptionServiceProgressRepository = subscriptionServiceProgressRepository;
        this.packageServiceItemRepository = packageServiceItemRepository;
        this.serviceDefinitionRepository = serviceDefinitionRepository;
    }

    @Transactional(readOnly = true)
    public List<AdminSubscriptionProgressDto> listProgress(String packageCode) {
        AuthContext.requirePageEditor();
        List<UserSubscriptionEntity> subscriptions = (packageCode == null || packageCode.isBlank())
                ? userSubscriptionRepository.findAllActive()
                : userSubscriptionRepository.findAllActiveByPackageCode(packageCode.trim().toUpperCase());

        return subscriptions.stream().map(this::toDto).toList();
    }

    @Transactional
    public AdminSubscriptionProgressDto updateProgress(UUID subscriptionId, UpdateProgressRequest request) {
        AuthContext.requirePageEditor();
        UserSubscriptionEntity sub = userSubscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "SUBSCRIPTION_NOT_FOUND"));

        SubscriptionProgressEntity progress = subscriptionProgressRepository.findById(subscriptionId)
                .orElseGet(() -> {
                    SubscriptionProgressEntity created = new SubscriptionProgressEntity();
                    created.setSubscriptionId(subscriptionId);
                    return created;
                });
        progress.setCompletedPosts(Math.max(0, request.completedPosts()));
        progress.setCompletedImages(Math.max(0, request.completedImages()));
        progress.setCompletedVideos(Math.max(0, request.completedVideos()));
        subscriptionProgressRepository.save(progress);

        if (request.serviceProgress() != null && !request.serviceProgress().isEmpty()) {
            Set<String> allowedServices = packageServiceItemRepository.findByPackageCode(sub.getPackageCode()).stream()
                    .map(item -> item.getId().getServiceId())
                    .collect(Collectors.toSet());
            for (UpdateServiceProgressItem serviceUpdate : request.serviceProgress()) {
                if (!allowedServices.contains(serviceUpdate.serviceId())) {
                    continue;
                }
                SubscriptionServiceProgressId id = new SubscriptionServiceProgressId(subscriptionId, serviceUpdate.serviceId());
                SubscriptionServiceProgressEntity row = subscriptionServiceProgressRepository.findById(id)
                        .orElseGet(() -> {
                            SubscriptionServiceProgressEntity created = new SubscriptionServiceProgressEntity();
                            created.setId(id);
                            return created;
                        });
                row.setPercent(clampPercent(serviceUpdate.percent()));
                subscriptionServiceProgressRepository.save(row);
            }
        }

        return toDto(sub);
    }

    private int clampPercent(Integer value) {
        if (value == null) {
            return 0;
        }
        return Math.min(100, Math.max(0, value));
    }

    private AdminSubscriptionProgressDto toDto(UserSubscriptionEntity sub) {
        SubscriptionProgressEntity overall = subscriptionProgressRepository.findById(sub.getId())
                .orElseGet(() -> {
                    SubscriptionProgressEntity empty = new SubscriptionProgressEntity();
                    empty.setSubscriptionId(sub.getId());
                    return empty;
                });

        Map<String, Integer> servicePercent = new HashMap<>();
        for (SubscriptionServiceProgressEntity row : subscriptionServiceProgressRepository.findBySubscriptionId(sub.getId())) {
            servicePercent.put(row.getId().getServiceId(), row.getPercent());
        }

        Map<String, ServiceDefinitionEntity> defsById = new HashMap<>();
        List<PackageServiceItemEntity> serviceLinks = packageServiceItemRepository.findByPackageCode(sub.getPackageCode());
        for (String serviceId : serviceLinks.stream().map(s -> s.getId().getServiceId()).toList()) {
            serviceDefinitionRepository.findById(serviceId).ifPresent(def -> defsById.put(serviceId, def));
        }

        List<AdminServiceProgressDto> services = new ArrayList<>();
        for (PackageServiceItemEntity link : serviceLinks) {
            String serviceId = link.getId().getServiceId();
            ServiceDefinitionEntity def = defsById.get(serviceId);
            services.add(new AdminServiceProgressDto(
                    serviceId,
                    def != null ? def.getName() : serviceId,
                    def != null ? def.getProgressMode() : "STATUS",
                    def != null ? def.getQuotaKey() : null,
                    servicePercent.getOrDefault(serviceId, 0)
            ));
        }

        String userName = appUserRepository.findById(sub.getUserId()).map(AppUser::getUsername).orElse(sub.getUserId());
        return new AdminSubscriptionProgressDto(
                sub.getId(),
                sub.getUserId(),
                userName,
                sub.getPackageCode(),
                sub.getServicePackage() != null ? sub.getServicePackage().getLabel() : sub.getPackageCode(),
                overall.getCompletedPosts(),
                overall.getCompletedImages(),
                overall.getCompletedVideos(),
                services
        );
    }

    public record AdminSubscriptionProgressDto(
            UUID subscriptionId,
            String userId,
            String userName,
            String packageCode,
            String packageLabel,
            int completedPosts,
            int completedImages,
            int completedVideos,
            List<AdminServiceProgressDto> services
    ) {
    }

    public record AdminServiceProgressDto(
            String serviceId,
            String serviceName,
            String progressMode,
            String quotaKey,
            int percent
    ) {
    }

    public record UpdateProgressRequest(
            int completedPosts,
            int completedImages,
            int completedVideos,
            List<UpdateServiceProgressItem> serviceProgress
    ) {
    }

    public record UpdateServiceProgressItem(String serviceId, Integer percent) {
    }
}

