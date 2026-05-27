package com.customerservice.controller;

import com.customerservice.api.HealthApi;
import com.customerservice.model.HealthResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

/** Health check — frontend: {@code HealthService} → DashboardView. */
@RestController
public class HealthApiController implements HealthApi {

    /** GET /api/health */
    @Override
    public ResponseEntity<HealthResponse> getHealth() {
        HealthResponse response = new HealthResponse();
        response.setStatus("UP");
        response.setService("customer-service-backend");
        return ResponseEntity.ok(response);
    }
}
