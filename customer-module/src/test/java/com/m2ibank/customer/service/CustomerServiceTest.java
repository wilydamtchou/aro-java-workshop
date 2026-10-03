package com.m2ibank.customer.service;

import com.m2ibank.common.exception.BusinessException;
import com.m2ibank.common.exception.ResourceNotFoundException;
import com.m2ibank.customer.dto.CustomerRequest;
import com.m2ibank.customer.dto.CustomerResponse;
import com.m2ibank.customer.entity.Customer;
import com.m2ibank.customer.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CustomerService customerService;

    private CustomerRequest request;
    private Customer customer;

    @BeforeEach
    void setUp() {
        request = new CustomerRequest();
        request.setFullName("Alice Ndzi");
        request.setEmail("alice@m2ibank.com");
        request.setPhoneNumber("+237600000001");
        request.setNationalId("CNI000001");

        customer = new Customer(
                "Alice Ndzi",
                "alice@m2ibank.com",
                "+237600000001",
                "CNI000001"
        );
    }

    @Test
    void shouldCreateCustomerSuccessfully() {
        when(customerRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());
        when(customerRepository.findByPhoneNumber(request.getPhoneNumber())).thenReturn(Optional.empty());
        when(customerRepository.save(any(Customer.class))).thenAnswer(invocation -> {
            Customer saved = invocation.getArgument(0);
            java.lang.reflect.Field idField = Customer.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(saved, 1L);
            return saved;
        });

        CustomerResponse response = customerService.createCustomer(request);

        assertNotNull(response);
        assertEquals("Alice Ndzi", response.getFullName());
        assertEquals("alice@m2ibank.com", response.getEmail());
        verify(customerRepository, times(1)).save(any(Customer.class));
    }

    @Test
    void shouldThrowExceptionWhenEmailAlreadyExists() {
        when(customerRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(customer));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> customerService.createCustomer(request));

        assertEquals("A customer with this email already exists", exception.getMessage());
        verify(customerRepository, never()).save(any(Customer.class));
    }

    @Test
    void shouldThrowExceptionWhenPhoneNumberAlreadyExists() {
        when(customerRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());
        when(customerRepository.findByPhoneNumber(request.getPhoneNumber())).thenReturn(Optional.of(customer));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> customerService.createCustomer(request));

        assertEquals("A customer with this phone number already exists", exception.getMessage());
        verify(customerRepository, never()).save(any(Customer.class));
    }

    @Test
    void shouldReturnCustomerById() throws Exception {
        java.lang.reflect.Field idField = Customer.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(customer, 1L);

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));

        CustomerResponse response = customerService.getCustomerById(1L);

        assertEquals(1L, response.getId());
        assertEquals("Alice Ndzi", response.getFullName());
    }

    @Test
    void shouldThrowExceptionWhenCustomerNotFound() {
        when(customerRepository.findById(99L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> customerService.getCustomerById(99L));

        assertTrue(exception.getMessage().contains("Customer not found"));
    }

    @Test
    void shouldReturnAllCustomers() throws Exception {
        java.lang.reflect.Field idField = Customer.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(customer, 1L);

        when(customerRepository.findAll()).thenReturn(List.of(customer));

        List<CustomerResponse> customers = customerService.getAllCustomers();

        assertEquals(1, customers.size());
        assertEquals("Alice Ndzi", customers.get(0).getFullName());
    }
}
