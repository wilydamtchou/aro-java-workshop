package com.m2ibank.web.cucumber.stepdefs;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.m2ibank.transfer.dto.TransferRequest;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

public class TransferStepDefinitions {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private TransferRequest request;
    private ResponseEntity<String> response;

    @Given("a transfer payload with the same source and destination account")
    public void a_transfer_payload_with_the_same_source_and_destination_account() {
        request = new TransferRequest();
        request.setSourceAccountId(100L);
        request.setDestinationAccountId(100L);
        request.setAmount(BigDecimal.valueOf(500));
        request.setDescription("Test identical accounts");
    }

    @When("the client submits the transfer request")
    public void the_client_submits_the_transfer_request() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<>(
                objectMapper.writeValueAsString(request),
                headers
        );

        response = restTemplate.postForEntity(
                "http://localhost:" + port + "/api/transfers",
                entity,
                String.class
        );
    }

    @Then("the transfer response status should be {int}")
    public void the_transfer_response_status_should_be(Integer statusCode) {
        assertEquals(statusCode, response.getStatusCode().value());
    }

    @And("the transfer response should contain the business error message")
    public void the_transfer_response_should_contain_the_business_error_message() {
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("Source and destination accounts must be different"));
    }
}
