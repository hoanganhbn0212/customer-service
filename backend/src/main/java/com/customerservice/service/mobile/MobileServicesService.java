package com.customerservice.service.mobile;

import com.customerservice.entity.mobile.ContentReviewEntity;
import com.customerservice.entity.mobile.DeliverableEntity;
import com.customerservice.entity.mobile.ImplementationItemEntity;
import com.customerservice.entity.mobile.ServicePackageEntity;
import com.customerservice.entity.mobile.SubscriptionProgressEntity;
import com.customerservice.entity.mobile.SubscriptionServiceProgressEntity;
import com.customerservice.entity.mobile.ServiceDefinitionEntity;
import com.customerservice.entity.mobile.UserSubscriptionEntity;
import com.customerservice.model.ImplementationItem;
import com.customerservice.model.MobileServicesResponse;
import com.customerservice.model.PackageServiceInfo;
import com.customerservice.repository.mobile.ContentReviewRepository;
import com.customerservice.repository.mobile.DeliverableRepository;
import com.customerservice.repository.mobile.ImplementationItemRepository;
import com.customerservice.repository.mobile.PackageServiceItemRepository;
import com.customerservice.repository.mobile.ServiceDefinitionRepository;
import com.customerservice.repository.mobile.SubscriptionProgressRepository;
import com.customerservice.repository.mobile.SubscriptionServiceProgressRepository;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MobileServicesService {

    private final MobileSubscriptionAccess subscriptionAccess;
    private final ImplementationItemRepository implementationItemRepository;
    private final DeliverableRepository deliverableRepository;
    private final ContentReviewRepository contentReviewRepository;
    private final PackageServiceItemRepository packageServiceItemRepository;
    private final ServiceDefinitionRepository serviceDefinitionRepository;
    private final SubscriptionProgressRepository subscriptionProgressRepository;
    private final SubscriptionServiceProgressRepository subscriptionServiceProgressRepository;

    public MobileServicesService(
            MobileSubscriptionAccess subscriptionAccess,
            ImplementationItemRepository implementationItemRepository,
            DeliverableRepository deliverableRepository,
            ContentReviewRepository contentReviewRepository,
            PackageServiceItemRepository packageServiceItemRepository,
            ServiceDefinitionRepository serviceDefinitionRepository,
            SubscriptionProgressRepository subscriptionProgressRepository,
            SubscriptionServiceProgressRepository subscriptionServiceProgressRepository
    ) {
        this.subscriptionAccess = subscriptionAccess;
        this.implementationItemRepository = implementationItemRepository;
        this.deliverableRepository = deliverableRepository;
        this.contentReviewRepository = contentReviewRepository;
        this.packageServiceItemRepository = packageServiceItemRepository;
        this.serviceDefinitionRepository = serviceDefinitionRepository;
        this.subscriptionProgressRepository = subscriptionProgressRepository;
        this.subscriptionServiceProgressRepository = subscriptionServiceProgressRepository;
    }

    @Transactional(readOnly = true)
    public MobileServicesResponse getServices(String category) {
        UserSubscriptionEntity sub = subscriptionAccess.requireActiveSubscription();
        String userId = subscriptionAccess.currentUserId();
        UUID subscriptionId = sub.getId();
        ServicePackageEntity pkg = sub.getServicePackage();

        List<ImplementationItemEntity> items =
                implementationItemRepository.findBySubscriptionIdOrderBySortOrderAsc(subscriptionId);

        String filter = category == null || category.isBlank() ? "all" : category;
        List<ImplementationItem> mapped = items.stream()
                .filter(item -> "all".equals(filter) || filter.equals(item.getCategory()))
                .map(item -> {
                    DeliverableEntity deliverable = deliverableRepository
                            .findFirstByImplementationItemId(item.getId())
                            .orElse(null);
                    UUID deliverableId = deliverable != null ? deliverable.getId() : null;
                    boolean reviewable = deliverable != null && "content".equals(item.getCategory());
                    ContentReviewEntity contentReview = deliverable == null
                            ? null
                            : contentReviewRepository
                                    .findFirstByDeliverableIdAndUserIdAndReviewTypeOrderByUpdatedAtDesc(deliverable.getId(), userId, "CONTENT")
                                    .orElse(null);
                    ContentReviewEntity designReview = deliverable == null
                            ? null
                            : contentReviewRepository
                                    .findFirstByDeliverableIdAndUserIdAndReviewTypeOrderByUpdatedAtDesc(deliverable.getId(), userId, "DESIGN_VIDEO")
                                    .orElse(null);
                    return MobileDtoMapper.toImplementationItem(item, deliverableId, reviewable, deliverable, contentReview, designReview);
                })
                .toList();

        SubscriptionProgressEntity overallProgress = subscriptionProgressRepository.findById(subscriptionId)
                .orElseGet(() -> {
                    SubscriptionProgressEntity empty = new SubscriptionProgressEntity();
                    empty.setSubscriptionId(subscriptionId);
                    return empty;
                });

        Map<String, SubscriptionServiceProgressEntity> progressByService = new HashMap<>();
        for (SubscriptionServiceProgressEntity row : subscriptionServiceProgressRepository.findBySubscriptionId(subscriptionId)) {
            progressByService.put(row.getId().getServiceId(), row);
        }

        List<PackageServiceInfo> packageServices = packageServiceItemRepository
                .findByPackageCode(sub.getPackageCode())
                .stream()
                .map(link -> serviceDefinitionRepository.findById(link.getId().getServiceId()).orElseThrow())
                .map(def -> MobileDtoMapper.toPackageServiceInfo(
                        def,
                        MobileProgressCalculator.computeServiceItem(
                                def,
                                pkg,
                                overallProgress,
                                progressByService.get(def.getId())
                        )
                ))
                .toList();

        MobileServicesResponse response = new MobileServicesResponse();
        response.setActiveSubscription(MobileDtoMapper.toSubscriptionSummary(sub));
        response.setImplementationItems(mapped);
        response.setPackageServices(packageServices);
        return response;
    }
}
