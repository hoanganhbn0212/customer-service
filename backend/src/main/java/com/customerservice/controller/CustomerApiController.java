package com.customerservice.controller;

import com.customerservice.api.CustomerApi;
import com.customerservice.model.CreateCustomerRequest;
import com.customerservice.model.Customer;
import com.customerservice.model.UpdateCustomerRequest;
import com.customerservice.service.CustomerService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** CRUD khách demo — frontend: {@code CustomerService} → DashboardView (/customers). */
@RestController
public class CustomerApiController implements CustomerApi {

    private final CustomerService customerService;

    public CustomerApiController(CustomerService customerService) {
        this.customerService = customerService;
    }

    /** GET /api/customers */
    @Override
    public ResponseEntity<List<Customer>> listCustomers() {
        return ResponseEntity.ok(customerService.findAll());
    }

    /** POST /api/customers */
    @Override
    public ResponseEntity<Customer> createCustomer(CreateCustomerRequest createCustomerRequest) {
        Customer created = customerService.create(createCustomerRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /** GET /api/customers/{id} */
    @Override
    public ResponseEntity<Customer> getCustomerById(Long id) {
        return customerService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /** PUT /api/customers/{id} */
    @Override
    public ResponseEntity<Customer> updateCustomer(Long id, UpdateCustomerRequest updateCustomerRequest) {
        return customerService.update(id, updateCustomerRequest)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /** DELETE /api/customers/{id} */
    @Override
    public ResponseEntity<Void> deleteCustomer(Long id) {
        if (customerService.delete(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
