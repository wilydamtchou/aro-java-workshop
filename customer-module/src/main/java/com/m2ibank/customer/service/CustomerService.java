package com.m2ibank.customer.service;

import com.m2ibank.common.exception.BusinessException;
import com.m2ibank.common.exception.ResourceNotFoundException;
import com.m2ibank.customer.dto.CustomerRequest;
import com.m2ibank.customer.dto.CustomerResponse;
import com.m2ibank.customer.entity.Customer;
import com.m2ibank.customer.repository.CustomerRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public CustomerResponse createCustomer(CustomerRequest request) {
        customerRepository.findByEmail(request.getEmail()).ifPresent(c -> {
            throw new BusinessException("A customer with this email already exists");
        });

        customerRepository.findByPhoneNumber(request.getPhoneNumber()).ifPresent(c -> {
            throw new BusinessException("A customer with this phone number already exists");
        });

        Customer customer = new Customer(
                request.getFullName(),
                request.getEmail(),
                request.getPhoneNumber(),
                request.getNationalId()
        );

        Customer saved = customerRepository.save(customer);
        return mapToResponse(saved);
    }

    public CustomerResponse getCustomerById(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id " + id));
        return mapToResponse(customer);
    }

    public List<CustomerResponse> getAllCustomers() {
        return customerRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public Customer getCustomerEntityById(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id " + id));
    }

    private CustomerResponse mapToResponse(Customer customer) {
        return new CustomerResponse(
                customer.getId(),
                customer.getFullName(),
                customer.getEmail(),
                customer.getPhoneNumber(),
                customer.getNationalId(),
                customer.getCreatedAt()
        );
    }
}
