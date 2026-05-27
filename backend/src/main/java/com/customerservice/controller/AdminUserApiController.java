package com.customerservice.controller;

import com.customerservice.api.AdminApi;
import com.customerservice.model.AdminAssignSubscriptionRequest;
import com.customerservice.model.AdminCreateUserRequest;
import com.customerservice.model.AdminUpdateUserRequest;
import com.customerservice.model.SubscriptionSummary;
import com.customerservice.model.UserAccountResponse;
import com.customerservice.model.PackageCatalogItem;
import com.customerservice.model.PackageUpgradeRequestAdminDto;
import com.customerservice.model.ReviewPackageUpgradeRequest;
import com.customerservice.service.AdminUserService;
import com.customerservice.service.admin.AdminPackageAdminService;
import com.customerservice.service.mobile.SubscriptionProvisioningService;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AdminUserApiController implements AdminApi {

    private final AdminUserService adminUserService;
    private final SubscriptionProvisioningService subscriptionProvisioningService;
    private final AdminPackageAdminService adminPackageAdminService;

    public AdminUserApiController(
            AdminUserService adminUserService,
            SubscriptionProvisioningService subscriptionProvisioningService,
            AdminPackageAdminService adminPackageAdminService
    ) {
        this.adminUserService = adminUserService;
        this.subscriptionProvisioningService = subscriptionProvisioningService;
        this.adminPackageAdminService = adminPackageAdminService;
    }

    @Override
    public ResponseEntity<List<UserAccountResponse>> listUsers() {
        return ResponseEntity.ok(adminUserService.listUsers());
    }

    @Override
    public ResponseEntity<UserAccountResponse> createUser(AdminCreateUserRequest adminCreateUserRequest) {
        UserAccountResponse created = adminUserService.createUser(adminCreateUserRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Override
    public ResponseEntity<UserAccountResponse> updateUser(
            String id,
            AdminUpdateUserRequest adminUpdateUserRequest
    ) {
        return ResponseEntity.ok(adminUserService.updateUser(id, adminUpdateUserRequest));
    }

    @Override
    public ResponseEntity<SubscriptionSummary> assignUserSubscription(
            String id,
            AdminAssignSubscriptionRequest adminAssignSubscriptionRequest
    ) {
        SubscriptionSummary created = subscriptionProvisioningService.assignForAdmin(
                id,
                adminAssignSubscriptionRequest
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Override
    public ResponseEntity<List<PackageCatalogItem>> listAdminPackages() {
        return ResponseEntity.ok(adminPackageAdminService.listPackages());
    }

    @Override
    public ResponseEntity<List<PackageUpgradeRequestAdminDto>> listPackageUpgradeRequests(String status) {
        return ResponseEntity.ok(adminPackageAdminService.listUpgradeRequests(status));
    }

    @Override
    public ResponseEntity<PackageUpgradeRequestAdminDto> reviewPackageUpgradeRequest(
            UUID id,
            ReviewPackageUpgradeRequest reviewPackageUpgradeRequest
    ) {
        return ResponseEntity.ok(
                adminPackageAdminService.reviewUpgradeRequest(id, reviewPackageUpgradeRequest)
        );
    }
}
