package com.m2ibank.customer.dto;

import java.time.LocalDateTime;

public class CustomerResponse {

    private Long id;
    private String fullName;
    private String email;
    private String phoneNumber;
    private String nationalId;
    private LocalDateTime createdAt;

    public CustomerResponse() {
    }

    public CustomerResponse(Long id, String fullName, String email, String phoneNumber, String nationalId, LocalDateTime createdAt) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.nationalId = nationalId;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getNationalId() {
        return nationalId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
