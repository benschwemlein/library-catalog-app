Feature: Hold Management

  As a library system user
  I want to manage holds (reservations) on books
  So that members can reserve books that are currently unavailable and pick them up when ready

  Background:
    Given I authenticate as "alice@citylibrary.org" with password "password123"

  Scenario: Member places a hold on a book at their preferred branch
    Given I obtain the member ID for "carol@example.com" from "/members"
    And I obtain the branch ID for "City Library Main Branch" from "/branches"
    And I obtain the book ID for "Murder on the Orient Express"
    When I send a POST request to "/holds" with body:
      """
      {
        "memberId": "{memberId}",
        "bookId": "{bookId}",
        "pickupBranchId": "{branchId}"
      }
      """
    Then the response status should be 201
    And the response body should contain "id"
    And the response body should contain "status" with value "PENDING"

  Scenario: The holds list is non-empty after a hold has been placed
    Given a hold exists for member "carol@example.com" on book "Refactoring"
    When I send a GET request to "/holds"
    Then the response status should be 200
    And the response body should be a non-empty array

  Scenario: Staff fulfills a pending hold and the status changes to READY
    Given a hold exists for member "carol@example.com" on book "A Brief History of Time"
    And I save the hold id as "holdId"
    When I send a POST request to "/holds/{holdId}/fulfill"
    Then the response status should be 200
    And the response body should contain "status" with value "READY"

  Scenario: Member cancels their hold and receives a 204 No Content response
    Given a hold exists for member "david@example.com" on book "Sapiens: A Brief History of Humankind"
    And I save the hold id as "holdId"
    When I send a DELETE request to "/holds/{holdId}"
    Then the response status should be 204

  Scenario: Staff can retrieve a specific hold by its ID
    Given a hold exists for member "eva@example.com" on book "To Kill a Mockingbird"
    And I save the hold id as "holdId"
    When I send a GET request to "/holds/{holdId}"
    Then the response status should be 200
    And the response body should contain "id"
    And the response body should contain "status"
