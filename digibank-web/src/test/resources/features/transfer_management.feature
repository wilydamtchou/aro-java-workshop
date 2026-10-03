Feature: Transfer management

  Scenario: Reject transfer when source and destination accounts are identical
    Given a transfer payload with the same source and destination account
    When the client submits the transfer request
    Then the transfer response status should be 400
    And the transfer response should contain the business error message
