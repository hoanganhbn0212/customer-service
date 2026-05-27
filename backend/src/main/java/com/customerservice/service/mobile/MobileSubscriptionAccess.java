package com.customerservice.service.mobile;

import com.customerservice.entity.mobile.UserSubscriptionEntity;
import com.customerservice.exception.UnauthorizedException;
import com.customerservice.repository.mobile.UserSubscriptionRepository;
import com.customerservice.security.AuthContext;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class MobileSubscriptionAccess {

    private final UserSubscriptionRepository userSubscriptionRepository;

    public MobileSubscriptionAccess(UserSubscriptionRepository userSubscriptionRepository) {
        this.userSubscriptionRepository = userSubscriptionRepository;
    }

    public UserSubscriptionEntity requireActiveSubscription() {
        String userId = AuthContext.requireAuthenticated().id();
        List<UserSubscriptionEntity> active = userSubscriptionRepository.findActiveByUserId(userId);
        if (active.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "NO_ACTIVE_SUBSCRIPTION");
        }
        return active.get(0);
    }

    public String currentUserId() {
        try {
            return AuthContext.requireAuthenticated().id();
        } catch (UnauthorizedException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED");
        }
    }
}
