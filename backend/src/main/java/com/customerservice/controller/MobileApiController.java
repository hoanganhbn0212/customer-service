package com.customerservice.controller;

import com.customerservice.api.MobileApi;
import com.customerservice.model.ContentReviewDto;
import com.customerservice.model.DeliverableReviewResponse;
import com.customerservice.model.MobileAccountResponse;
import com.customerservice.model.MobileHomeResponse;
import com.customerservice.model.MobileServicesResponse;
import com.customerservice.model.NotificationListResponse;
import com.customerservice.model.PackageCatalogItem;
import com.customerservice.model.PackageUpgradeRequest;
import com.customerservice.model.PackageUpgradeResponse;
import com.customerservice.model.SaveReviewDraftRequest;
import com.customerservice.model.SubmitReviewRequest;
import com.customerservice.model.UnreadCountResponse;
import com.customerservice.model.UpdateAccountRequest;
import com.customerservice.model.UserVoucherDto;
import com.customerservice.service.mobile.MobileAccountService;
import com.customerservice.service.mobile.MobileHomeService;
import com.customerservice.service.mobile.MobileNotificationService;
import com.customerservice.service.mobile.MobileReviewService;
import com.customerservice.service.mobile.MobileServicesService;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MobileApiController implements MobileApi {

    private final MobileHomeService mobileHomeService;
    private final MobileServicesService mobileServicesService;
    private final MobileReviewService mobileReviewService;
    private final MobileNotificationService mobileNotificationService;
    private final MobileAccountService mobileAccountService;

    public MobileApiController(
            MobileHomeService mobileHomeService,
            MobileServicesService mobileServicesService,
            MobileReviewService mobileReviewService,
            MobileNotificationService mobileNotificationService,
            MobileAccountService mobileAccountService
    ) {
        this.mobileHomeService = mobileHomeService;
        this.mobileServicesService = mobileServicesService;
        this.mobileReviewService = mobileReviewService;
        this.mobileNotificationService = mobileNotificationService;
        this.mobileAccountService = mobileAccountService;
    }

    @Override
    public ResponseEntity<MobileHomeResponse> getMobileHome(LocalDate selectedDate) {
        return ResponseEntity.ok(mobileHomeService.getHome(selectedDate));
    }

    @Override
    public ResponseEntity<MobileServicesResponse> getMobileServices(String category) {
        return ResponseEntity.ok(mobileServicesService.getServices(category));
    }

    @Override
    public ResponseEntity<DeliverableReviewResponse> getDeliverableReview(UUID deliverableId) {
        return ResponseEntity.ok(mobileReviewService.getReview(deliverableId));
    }

    @Override
    public ResponseEntity<ContentReviewDto> saveReviewDraft(UUID deliverableId, SaveReviewDraftRequest request) {
        return ResponseEntity.ok(mobileReviewService.saveDraft(deliverableId, request));
    }

    @Override
    public ResponseEntity<ContentReviewDto> submitReview(UUID deliverableId, SubmitReviewRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(mobileReviewService.submitReview(deliverableId, request));
    }

    @Override
    public ResponseEntity<NotificationListResponse> listNotifications(
            Integer page,
            Integer size,
            Boolean unreadOnly
    ) {
        return ResponseEntity.ok(mobileNotificationService.listNotifications(page, size, unreadOnly));
    }

    @Override
    public ResponseEntity<UnreadCountResponse> getUnreadNotificationCount() {
        return ResponseEntity.ok(mobileNotificationService.getUnreadCount());
    }

    @Override
    public ResponseEntity<Void> markNotificationRead(UUID id) {
        mobileNotificationService.markRead(id);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<MobileAccountResponse> getMobileAccount() {
        return ResponseEntity.ok(mobileAccountService.getAccount());
    }

    @Override
    public ResponseEntity<MobileAccountResponse> updateMobileAccount(UpdateAccountRequest updateAccountRequest) {
        return ResponseEntity.ok(mobileAccountService.updateAccount(updateAccountRequest));
    }

    @Override
    public ResponseEntity<List<PackageCatalogItem>> listAvailablePackages() {
        return ResponseEntity.ok(mobileAccountService.listPackages());
    }

    @Override
    public ResponseEntity<PackageUpgradeResponse> requestPackageUpgrade(PackageUpgradeRequest packageUpgradeRequest) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(mobileAccountService.requestUpgrade(packageUpgradeRequest));
    }

    @Override
    public ResponseEntity<List<UserVoucherDto>> listMyVouchers() {
        return ResponseEntity.ok(mobileAccountService.listVouchers());
    }
}
