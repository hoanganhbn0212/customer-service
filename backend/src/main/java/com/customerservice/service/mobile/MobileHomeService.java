package com.customerservice.service.mobile;

import com.customerservice.entity.mobile.ScheduleTaskEntity;
import com.customerservice.entity.mobile.ServiceDefinitionEntity;
import com.customerservice.entity.mobile.ServicePackageEntity;
import com.customerservice.entity.mobile.SubscriptionProgressEntity;
import com.customerservice.entity.mobile.SubscriptionServiceProgressEntity;
import com.customerservice.entity.mobile.UserSubscriptionEntity;
import com.customerservice.model.MobileHomeResponse;
import com.customerservice.model.MobileHomeSchedule;
import com.customerservice.model.ServiceProgressItem;
import com.customerservice.repository.mobile.PackageServiceItemRepository;
import com.customerservice.repository.mobile.ScheduleTaskRepository;
import com.customerservice.repository.mobile.ServiceDefinitionRepository;
import com.customerservice.repository.mobile.SubscriptionProgressRepository;
import com.customerservice.repository.mobile.SubscriptionServiceProgressRepository;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MobileHomeService {

    private final MobileSubscriptionAccess subscriptionAccess;
    private final SubscriptionProgressRepository progressRepository;
    private final SubscriptionServiceProgressRepository serviceProgressRepository;
    private final PackageServiceItemRepository packageServiceItemRepository;
    private final ServiceDefinitionRepository serviceDefinitionRepository;
    private final ScheduleTaskRepository scheduleTaskRepository;

    public MobileHomeService(
            MobileSubscriptionAccess subscriptionAccess,
            SubscriptionProgressRepository progressRepository,
            SubscriptionServiceProgressRepository serviceProgressRepository,
            PackageServiceItemRepository packageServiceItemRepository,
            ServiceDefinitionRepository serviceDefinitionRepository,
            ScheduleTaskRepository scheduleTaskRepository
    ) {
        this.subscriptionAccess = subscriptionAccess;
        this.progressRepository = progressRepository;
        this.serviceProgressRepository = serviceProgressRepository;
        this.packageServiceItemRepository = packageServiceItemRepository;
        this.serviceDefinitionRepository = serviceDefinitionRepository;
        this.scheduleTaskRepository = scheduleTaskRepository;
    }

    @Transactional(readOnly = true)
    public MobileHomeResponse getHome(LocalDate selectedDate) {
        UserSubscriptionEntity sub = subscriptionAccess.requireActiveSubscription();
        ServicePackageEntity pkg = sub.getServicePackage();
        UUID subscriptionId = sub.getId();

        SubscriptionProgressEntity progress = progressRepository.findById(subscriptionId)
                .orElseGet(() -> emptyProgress(subscriptionId));

        Map<String, Integer> percentByService = new HashMap<>();
        for (SubscriptionServiceProgressEntity row : serviceProgressRepository.findBySubscriptionId(subscriptionId)) {
            percentByService.put(row.getId().getServiceId(), row.getPercent());
        }

        List<ServiceProgressItem> services = packageServiceItemRepository.findByPackageCode(pkg.getCode()).stream()
                .map(item -> serviceDefinitionRepository.findById(item.getId().getServiceId()).orElseThrow())
                .map(def -> MobileDtoMapper.toServiceProgressItem(def, percentByService.getOrDefault(def.getId(), 0)))
                .toList();

        LocalDate day = selectedDate != null ? selectedDate : LocalDate.now();
        List<ScheduleTaskEntity> tasks = scheduleTaskRepository.findBySubscriptionIdAndTaskDateOrderBySortOrderAsc(
                subscriptionId,
                day
        );

        MobileHomeSchedule schedule = new MobileHomeSchedule();
        schedule.setMonth(day.getMonthValue());
        schedule.setYear(day.getYear());
        schedule.setWeekDays(MobileDtoMapper.buildWeekDays(day));
        schedule.setTasks(tasks.stream().map(MobileDtoMapper::toScheduleTask).toList());

        MobileHomeResponse response = new MobileHomeResponse();
        response.setSubscription(MobileDtoMapper.toSubscriptionSummary(sub));
        response.setProgress(MobileDtoMapper.toProgressSummary(pkg, progress));
        response.setServices(services);
        response.setSchedule(schedule);
        return response;
    }

    private SubscriptionProgressEntity emptyProgress(UUID subscriptionId) {
        SubscriptionProgressEntity progress = new SubscriptionProgressEntity();
        progress.setSubscriptionId(subscriptionId);
        return progress;
    }
}
