package com.m2ibank.common.api;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ApiResponseTest {

    @Test
    void shouldCreateApiResponseWithDefaultConstructor() {
        ApiResponse<String> response = new ApiResponse<>();

        assertNotNull(response);
        assertNotNull(response.getTimestamp());
    }

    @Test
    void shouldCreateApiResponseWithParameterizedConstructor() {
        ApiResponse<String> response = new ApiResponse<>(true, "Success message", "test data");

        assertTrue(response.isSuccess());
        assertEquals("Success message", response.getMessage());
        assertEquals("test data", response.getData());
        assertNotNull(response.getTimestamp());
    }

    @Test
    void shouldCreateSuccessResponse() {
        ApiResponse<String> response = ApiResponse.success("Operation successful", "result");

        assertTrue(response.isSuccess());
        assertEquals("Operation successful", response.getMessage());
        assertEquals("result", response.getData());
        assertNotNull(response.getTimestamp());
    }

    @Test
    void shouldCreateFailureResponse() {
        ApiResponse<String> response = ApiResponse.failure("Operation failed", null);

        assertFalse(response.isSuccess());
        assertEquals("Operation failed", response.getMessage());
        assertNull(response.getData());
        assertNotNull(response.getTimestamp());
    }

    @Test
    void shouldSetAndGetSuccess() {
        ApiResponse<String> response = new ApiResponse<>();
        response.setSuccess(true);

        assertTrue(response.isSuccess());
    }

    @Test
    void shouldSetAndGetMessage() {
        ApiResponse<String> response = new ApiResponse<>();
        response.setMessage("Test message");

        assertEquals("Test message", response.getMessage());
    }

    @Test
    void shouldSetAndGetData() {
        ApiResponse<Integer> response = new ApiResponse<>();
        response.setData(123);

        assertEquals(123, response.getData());
    }

    @Test
    void shouldWorkWithDifferentDataTypes() {
        ApiResponse<Integer> intResponse = ApiResponse.success("Integer data", 42);
        assertEquals(42, intResponse.getData());

        ApiResponse<Double> doubleResponse = ApiResponse.success("Double data", 3.14);
        assertEquals(3.14, doubleResponse.getData());

        ApiResponse<Boolean> boolResponse = ApiResponse.success("Boolean data", true);
        assertTrue(boolResponse.getData());
    }
}
