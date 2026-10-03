package com.m2ibank.account.controller;

import com.m2ibank.account.dto.AccountRequest;
import com.m2ibank.account.dto.AccountResponse;
import com.m2ibank.account.service.AccountService;
import com.m2ibank.common.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Accounts", description = "Account management API")
@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @Operation(summary = "Create a new account")
    @PostMapping
    public ResponseEntity<ApiResponse<AccountResponse>> createAccount(@Valid @RequestBody AccountRequest request) {
        AccountResponse response = accountService.createAccount(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Account created successfully", response));
    }

    @Operation(summary = "Get account by ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AccountResponse>> getAccountById(@PathVariable Long id) {
        AccountResponse response = accountService.getAccountById(id);
        return ResponseEntity.ok(ApiResponse.success("Account retrieved successfully", response));
    }

    @Operation(summary = "Get account by CUSTOMER ID")
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<ApiResponse<List<AccountResponse>>> getAccountsByCustomerId(@PathVariable Long customerId) {
        List<AccountResponse> response = accountService.getAccountsByCustomerId(customerId);
        return ResponseEntity.ok(ApiResponse.success("Accounts retrieved successfully", response));
    }
}
