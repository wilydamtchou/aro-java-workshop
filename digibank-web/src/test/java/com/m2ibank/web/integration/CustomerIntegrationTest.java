package com.m2ibank.web.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.m2ibank.customer.dto.CustomerRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CustomerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Should create a customer through REST API")
    void shouldCreateCustomerThroughApi() throws Exception {
        CustomerRequest request = new CustomerRequest();
        request.setFullName("Integration User");
        request.setEmail("integration@m2ibank.com");
        request.setPhoneNumber("+237611111111");
        request.setNationalId("CNI-INTEG-001");

        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.fullName").value("Integration User"))
                .andExpect(jsonPath("$.data.email").value("integration@m2ibank.com"));
    }
}
