package com.customerservice.service;

import com.customerservice.entity.CustomerEntity;
import com.customerservice.model.CreateCustomerRequest;
import com.customerservice.model.Customer;
import com.customerservice.model.UpdateCustomerRequest;
import com.customerservice.repository.CustomerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Transactional(readOnly = true)
    public List<Customer> findAll() {
        return customerRepository.findAll().stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public Optional<Customer> findById(Long id) {
        return customerRepository.findById(id).map(this::toDto);
    }

    public Customer create(CreateCustomerRequest request) {
        CustomerEntity entity = new CustomerEntity();
        entity.setName(request.getName());
        entity.setEmail(request.getEmail());
        return toDto(customerRepository.save(entity));
    }

    public Optional<Customer> update(Long id, UpdateCustomerRequest request) {
        return customerRepository.findById(id).map(entity -> {
            entity.setName(request.getName());
            entity.setEmail(request.getEmail());
            return toDto(customerRepository.save(entity));
        });
    }

    public boolean delete(Long id) {
        if (!customerRepository.existsById(id)) {
            return false;
        }
        customerRepository.deleteById(id);
        return true;
    }

    private Customer toDto(CustomerEntity entity) {
        Customer customer = new Customer();
        customer.setId(entity.getId());
        customer.setName(entity.getName());
        customer.setEmail(entity.getEmail());
        return customer;
    }
}
