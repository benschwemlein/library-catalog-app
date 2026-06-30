Feature: Loan Management

  As a library system user
  I want to manage loans (checkouts, returns, and renewals) for book copies
  So that members can borrow books and staff can track lending activity

  Background:
    Given I authenticate as "alice@citylibrary.org" with password "password123"

  Scenario: Staff checks out an available copy of a book to a member
    Given the member "carol@example.com" has an available copy of "Effective Java" ready to check out
    When I send a POST request to "/loans/checkout" with body:
      """
      {
        "memberId": "{memberId}",
        "bookCopyId": "{copyId}"
      }
      """
    Then the response status should be 201
    And the response body should contain "id"
    And the response body should contain "status" with value "ACTIVE"
    And the response body should contain "dueDate"

  Scenario: Checkout fails when the requested copy is already checked out
    Given I obtain the member ID for "carol@example.com" from "/members"
    And I obtain the copy ID for barcode "BC-005"
    When I send a POST request to "/loans/checkout" with body:
      """
      {
        "memberId": "{memberId}",
        "bookCopyId": "{copyId}"
      }
      """
    Then the response status should be 409

  Scenario: Checkout fails when the member is suspended
    Given the member "frank@example.com" is suspended
    And the member "frank@example.com" has an available copy of "Clean Code" ready to check out
    When I send a POST request to "/loans/checkout" with body:
      """
      {
        "memberId": "{memberId}",
        "bookCopyId": "{copyId}"
      }
      """
    Then the response status should be 403

  Scenario: Checkout is rejected when a member has reached their concurrent loan limit
    Given the member "grace@example.com" has reached their concurrent loan limit
    And the member "grace@example.com" has an available copy of "The Two Towers" ready to check out
    When I send a POST request to "/loans/checkout" with body:
      """
      {
        "memberId": "{memberId}",
        "bookCopyId": "{copyId}"
      }
      """
    Then the response status should be 422

  Scenario: Staff returns a borrowed book and the loan status becomes RETURNED
    Given an active loan exists where "carol@example.com" has borrowed "Animal Farm"
    And I save the loan id as "loanId"
    When I send a POST request to "/loans/{loanId}/return"
    Then the response status should be 200
    And the response body should contain "status" with value "RETURNED"
    And the response body should contain "returnedAt"

  Scenario: Member renews an active loan and the due date is extended
    Given an active loan exists where "david@example.com" has borrowed "Design Patterns"
    And I save the loan id as "loanId"
    And I save the loan due date as "originalDueDate"
    When I send a POST request to "/loans/{loanId}/renew"
    Then the response status should be 200
    And the response body should contain "status" with value "ACTIVE"
    And the response body field "dueDate" should be later than "originalDueDate"

  Scenario: Overdue loans appear in the overdue loans report
    Given an overdue loan exists where "ivan@example.com" has borrowed "Refactoring"
    When I send a GET request to "/loans/overdue"
    Then the response status should be 200
    And the response body should be a non-empty array

  Scenario: Staff can view all active loans across the library
    Given an active loan exists where "carol@example.com" has borrowed "Sapiens: A Brief History of Humankind"
    When I send a GET request to "/loans"
    Then the response status should be 200
    And the response body should be a non-empty array
    And each item in the response array should contain "id"
    And each item in the response array should contain "status"
