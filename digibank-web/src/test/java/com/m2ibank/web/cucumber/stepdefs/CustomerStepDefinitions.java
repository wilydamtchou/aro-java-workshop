package com.m2ibank.web.cucumber.stepdefs;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.m2ibank.customer.dto.CustomerRequest;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;

import static org.junit.jupiter.api.Assertions.*;

public class CustomerStepDefinitions {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private CustomerRequest request;
    private ResponseEntity<String> response;

    @Given("the customer payload is valid")
    public void theCustomerPayloadIsValid() {
        request = new CustomerRequest();
        request.setFullName("Cucumber User");
        request.setEmail("cucumber@m2ibank.com");
        request.setPhoneNumber("+237622222222");
        request.setNationalId("CNI-CUC-001");
    }

    @When("the client submits the customer creation request")
    public void theClientSubmitsTheCustomerCreationRequest() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<>(
                objectMapper.writeValueAsString(request),
                headers
        );

        response = restTemplate.postForEntity(
                "http://localhost:" + port + "/api/customers",
                entity,
                String.class
        );
    }

    @Then("the response status should be 201")
    public void theResponseStatusShouldBe201() {
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
    }

    @And("the response should indicate a successful customer creation")
    public void theResponseShouldIndicateASuccessfulCustomerCreation() {
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("\"success\":true"));
        assertTrue(response.getBody().contains("Customer created successfully"));
    }
}
