package com.customerservice.service.mobile;

import com.customerservice.entity.mobile.NotificationEntity;
import com.customerservice.model.NotificationDto;
import com.customerservice.model.NotificationListResponse;
import com.customerservice.model.UnreadCountResponse;
import com.customerservice.repository.mobile.NotificationRepository;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class MobileNotificationService {

    private final MobileSubscriptionAccess subscriptionAccess;
    private final NotificationRepository notificationRepository;

    public MobileNotificationService(
            MobileSubscriptionAccess subscriptionAccess,
            NotificationRepository notificationRepository
    ) {
        this.subscriptionAccess = subscriptionAccess;
        this.notificationRepository = notificationRepository;
    }

    @Transactional(readOnly = true)
    public NotificationListResponse listNotifications(Integer page, Integer size, Boolean unreadOnly) {
        String userId = subscriptionAccess.currentUserId();
        int p = page == null ? 0 : page;
        int s = size == null ? 20 : size;
        PageRequest pageable = PageRequest.of(p, s);

        Page<NotificationEntity> result = Boolean.TRUE.equals(unreadOnly)
                ? notificationRepository.findByUserIdAndReadAtIsNullOrderByCreatedAtDesc(userId, pageable)
                : notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);

        NotificationListResponse response = new NotificationListResponse();
        response.setItems(result.getContent().stream().map(MobileDtoMapper::toNotificationDto).toList());
        response.setTotalElements((int) result.getTotalElements());
        response.setTotalPages(result.getTotalPages());
        return response;
    }

    @Transactional(readOnly = true)
    public UnreadCountResponse getUnreadCount() {
        String userId = subscriptionAccess.currentUserId();
        UnreadCountResponse response = new UnreadCountResponse();
        response.setCount((int) notificationRepository.countByUserIdAndReadAtIsNull(userId));
        return response;
    }

    @Transactional
    public void markRead(UUID notificationId) {
        String userId = subscriptionAccess.currentUserId();
        NotificationEntity entity = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "NOTIFICATION_NOT_FOUND"));
        if (!userId.equals(entity.getUserId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "FORBIDDEN");
        }
        if (entity.getReadAt() == null) {
            entity.setReadAt(LocalDateTime.now());
            notificationRepository.save(entity);
        }
    }
}
