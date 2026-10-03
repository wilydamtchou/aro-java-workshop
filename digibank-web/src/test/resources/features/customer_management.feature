Feature: Customer management

  Scenario: Create a new customer successfully
    Given the customer payload is valid
    When the client submits the customer creation request
    Then the response status should be 201
    And the response should indicate a successful customer creation
