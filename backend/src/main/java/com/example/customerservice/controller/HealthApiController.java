package com.example.customerservice.controller;

import com.example.customerservice.api.HealthApi;
import com.example.customerservice.model.HealthResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthApiController implements HealthApi {

    @Override
    public ResponseEntity<HealthResponse> getHealth() {
        HealthResponse response = new HealthResponse();
        response.setStatus("UP");
        response.setService("customer-service-backend");
        return ResponseEntity.ok(response);
    }
}
