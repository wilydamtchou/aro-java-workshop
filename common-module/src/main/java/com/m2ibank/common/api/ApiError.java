package com.m2ibank.common.api;

import java.time.LocalDateTime;
import java.util.List;

public class ApiError {

    private boolean success;
    private String message;
    private List<String> details;
    private LocalDateTime timestamp;

    public ApiError() {
        this.timestamp = LocalDateTime.now();
        this.success = false;
    }

    public ApiError(String message, List<String> details) {
        this();
        this.message = message;
        this.details = details;
    }

    public static ApiError of(String message, List<String> details) {
        return new ApiError(message, details);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public List<String> getDetails() {
        return details;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}
