package com.customerservice.controller;

import com.customerservice.service.admin.AdminProgressService;
import com.customerservice.service.admin.AdminProgressService.AdminSubscriptionProgressDto;
import com.customerservice.service.admin.AdminProgressService.UpdateProgressRequest;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Tiến độ triển khai gói — frontend: {@code adminApi.js} → ProgressAdminView.
 */
@RestController
@RequestMapping("/api/v1/admin/subscription-progress")
public class AdminProgressController {

    private final AdminProgressService adminProgressService;

    public AdminProgressController(AdminProgressService adminProgressService) {
        this.adminProgressService = adminProgressService;
    }

    /** GET /api/v1/admin/subscription-progress — Liệt kê tiến độ (lọc packageCode). */
    @GetMapping
    public ResponseEntity<List<AdminSubscriptionProgressDto>> listProgress(
            @RequestParam(value = "packageCode", required = false) String packageCode
    ) {
        return ResponseEntity.ok(adminProgressService.listProgress(packageCode));
    }

    /** PATCH /api/v1/admin/subscription-progress/{id} — Cập nhật số liệu (đồng bộ mobile/home). */
    @PatchMapping("/{subscriptionId}")
    public ResponseEntity<AdminSubscriptionProgressDto> updateProgress(
            @PathVariable("subscriptionId") UUID subscriptionId,
            @RequestBody UpdateProgressRequest request
    ) {
        return ResponseEntity.ok(adminProgressService.updateProgress(subscriptionId, request));
    }
}

