package com.example.customerservice.service;

import com.example.customerservice.model.CreateCustomerRequest;
import com.example.customerservice.model.Customer;
import com.example.customerservice.model.UpdateCustomerRequest;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class CustomerService {

    private final Map<Long, Customer> store = new ConcurrentHashMap<>();
    private final AtomicLong idSequence = new AtomicLong(1);

    public List<Customer> findAll() {
        return new ArrayList<>(store.values());
    }

    public Optional<Customer> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    public Customer create(CreateCustomerRequest request) {
        Customer customer = new Customer();
        customer.setId(idSequence.getAndIncrement());
        customer.setName(request.getName());
        customer.setEmail(request.getEmail());
        store.put(customer.getId(), customer);
        return customer;
    }

    public Optional<Customer> update(Long id, UpdateCustomerRequest request) {
        Customer existing = store.get(id);
        if (existing == null) {
            return Optional.empty();
        }
        existing.setName(request.getName());
        existing.setEmail(request.getEmail());
        return Optional.of(existing);
    }

    public boolean delete(Long id) {
        return store.remove(id) != null;
    }
}
