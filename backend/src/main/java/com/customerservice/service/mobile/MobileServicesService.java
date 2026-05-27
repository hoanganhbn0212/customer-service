package com.customerservice.service.mobile;

import com.customerservice.entity.mobile.DeliverableEntity;
import com.customerservice.entity.mobile.ImplementationItemEntity;
import com.customerservice.entity.mobile.ServiceDefinitionEntity;
import com.customerservice.entity.mobile.UserSubscriptionEntity;
import com.customerservice.model.ImplementationItem;
import com.customerservice.model.MobileServicesResponse;
import com.customerservice.model.PackageServiceInfo;
import com.customerservice.repository.mobile.DeliverableRepository;
import com.customerservice.repository.mobile.ImplementationItemRepository;
import com.customerservice.repository.mobile.PackageServiceItemRepository;
import com.customerservice.repository.mobile.ServiceDefinitionRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MobileServicesService {

    private final MobileSubscriptionAccess subscriptionAccess;
    private final ImplementationItemRepository implementationItemRepository;
    private final DeliverableRepository deliverableRepository;
    private final PackageServiceItemRepository packageServiceItemRepository;
    private final ServiceDefinitionRepository serviceDefinitionRepository;

    public MobileServicesService(
            MobileSubscriptionAccess subscriptionAccess,
            ImplementationItemRepository implementationItemRepository,
            DeliverableRepository deliverableRepository,
            PackageServiceItemRepository packageServiceItemRepository,
            ServiceDefinitionRepository serviceDefinitionRepository
    ) {
        this.subscriptionAccess = subscriptionAccess;
        this.implementationItemRepository = implementationItemRepository;
        this.deliverableRepository = deliverableRepository;
        this.packageServiceItemRepository = packageServiceItemRepository;
        this.serviceDefinitionRepository = serviceDefinitionRepository;
    }

    @Transactional(readOnly = true)
    public MobileServicesResponse getServices(String category) {
        UserSubscriptionEntity sub = subscriptionAccess.requireActiveSubscription();
        UUID subscriptionId = sub.getId();

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
                    return MobileDtoMapper.toImplementationItem(item, deliverableId, reviewable);
                })
                .toList();

        List<PackageServiceInfo> packageServices = packageServiceItemRepository
                .findByPackageCode(sub.getPackageCode())
                .stream()
                .map(link -> serviceDefinitionRepository.findById(link.getId().getServiceId()).orElseThrow())
                .map(MobileDtoMapper::toPackageServiceInfo)
                .toList();

        MobileServicesResponse response = new MobileServicesResponse();
        response.setActiveSubscription(MobileDtoMapper.toSubscriptionSummary(sub));
        response.setImplementationItems(mapped);
        response.setPackageServices(packageServices);
        return response;
    }
}
