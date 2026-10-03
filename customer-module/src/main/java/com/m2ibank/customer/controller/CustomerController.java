package com.m2ibank.customer.controller;

import com.m2ibank.common.api.ApiResponse;
import com.m2ibank.customer.dto.CustomerRequest;
import com.m2ibank.customer.dto.CustomerResponse;
import com.m2ibank.customer.service.CustomerService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@Tag(
        name = "Customers",
        description = "Customer management API"
)
@Validated
@RestController
@RequestMapping("/api/customers")
public class CustomerController {
    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }


    @Operation(
            summary = "Create a new customer",
            description = "Create a new customer account"
    )
    @PostMapping
    public ResponseEntity<ApiResponse<CustomerResponse>> createCustomer(@Valid @RequestBody CustomerRequest request) {
        CustomerResponse response = customerService.createCustomer(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        "Customer created successfully",
                        response
                ));
    }

    @Operation(
            summary = "Get customer by ID",
            description = "Retrieve a customer using its unique identifier"
    )
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CustomerResponse>> getCustomerById(
            @Parameter(
                    name = "id",
                    description = "Customer database identifier",
                    required = true,
                    schema = @Schema(
                            type = "integer",
                            format = "int64",
                            example = "1"
                    )
            )
            @PathVariable("id") Long id
    ) {
        CustomerResponse response = customerService.getCustomerById(id);

        return ResponseEntity.ok(
                ApiResponse.success("Customer retrieved successfully", response)
        );
    }

    @Operation(
            summary = "Get all customers",
            description = "Retrieve a list of all customers"
    )
    @GetMapping
    public ResponseEntity<ApiResponse<List<CustomerResponse>>> getAllCustomers() {
        List<CustomerResponse> response = customerService.getAllCustomers();

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Customers retrieved successfully",
                        response
                )
        );
    }
}