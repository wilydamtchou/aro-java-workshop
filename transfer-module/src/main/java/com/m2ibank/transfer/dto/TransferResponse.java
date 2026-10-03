package com.m2ibank.transfer.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TransferResponse {

    private Long id;
    private Long sourceAccountId;
    private Long destinationAccountId;
    private BigDecimal amount;
    private String description;
    private LocalDateTime createdAt;

    public TransferResponse(Long id, Long sourceAccountId, Long destinationAccountId, BigDecimal amount, String description, LocalDateTime createdAt) {
        this.id = id;
        this.sourceAccountId = sourceAccountId;
        this.destinationAccountId = destinationAccountId;
        this.amount = amount;
        this.description = description;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public Long getSourceAccountId() {
        return sourceAccountId;
    }

    public Long getDestinationAccountId() {
        return destinationAccountId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getDescription() {
        return description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
