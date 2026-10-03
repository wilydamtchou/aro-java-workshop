package com.m2ibank.customer.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "customers")
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String fullName;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(nullable = false, unique = true, length = 30)
    private String phoneNumber;

    @Column(nullable = false, length = 30)
    private String nationalId;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public Customer() {
        this.createdAt = LocalDateTime.now();
    }

    public Customer(String fullName, String email, String phoneNumber, String nationalId) {
        this.fullName = fullName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.nationalId = nationalId;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getNationalId() {
        return nationalId;
    }

    public void setNationalId(String nationalId) {
        this.nationalId = nationalId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
