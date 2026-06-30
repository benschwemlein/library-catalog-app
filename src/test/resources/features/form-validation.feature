Feature: Form Validation

  As a library system
  I want to reject requests with missing required fields
  So that data integrity is maintained and callers receive clear error feedback

  Background:
    Given I authenticate as "alice@citylibrary.org" with password "password123"

  Scenario: Checkout is rejected when memberId is missing
    When I send a POST request to "/loans/checkout" with body:
      """
      { "bookCopyId": 1 }
      """
    Then the response status should be 400
    And the response body should contain "message"

  Scenario: Checkout is rejected when bookCopyId is missing
    When I send a POST request to "/loans/checkout" with body:
      """
      { "memberId": 1 }
      """
    Then the response status should be 400
    And the response body should contain "message"

  Scenario: Place hold is rejected when memberId is missing
    When I send a POST request to "/holds" with body:
      """
      { "bookId": 1, "pickupBranchId": 1 }
      """
    Then the response status should be 400
    And the response body should contain "message"

  Scenario: Place hold is rejected when bookId is missing
    When I send a POST request to "/holds" with body:
      """
      { "memberId": 1, "pickupBranchId": 1 }
      """
    Then the response status should be 400
    And the response body should contain "message"

  Scenario: Waive fine is rejected when reason is missing
    When I send a POST request to "/fines/999/waive" with body:
      """
      { "waivedBy": "admin" }
      """
    Then the response status should be 400
    And the response body should contain "message"

  Scenario: Create event is rejected when title is missing
    When I send a POST request to "/events" with body:
      """
      {
        "description": "A great event",
        "branchId": 1,
        "startDateTime": "2027-01-01T10:00:00",
        "endDateTime": "2027-01-01T12:00:00",
        "capacity": 50,
        "eventType": "WORKSHOP"
      }
      """
    Then the response status should be 400
    And the response body should contain "message"

  Scenario: Create event is rejected when startDateTime is missing
    When I send a POST request to "/events" with body:
      """
      {
        "title": "Library Workshop",
        "branchId": 1,
        "endDateTime": "2027-01-01T12:00:00",
        "capacity": 50,
        "eventType": "WORKSHOP"
      }
      """
    Then the response status should be 400
    And the response body should contain "message"

  Scenario: Registration is rejected when email is blank
    When I send a POST request to "/v1/auth/register" with body:
      """
      { "firstname": "Test", "lastname": "User", "password": "pass123", "role": "USER" }
      """
    Then the response status should be 400
    And the response body should contain "message"

  Scenario: Registration is rejected when password is blank
    When I send a POST request to "/v1/auth/register" with body:
      """
      { "firstname": "Test", "lastname": "User", "email": "newuser@example.com", "role": "USER" }
      """
    Then the response status should be 400
    And the response body should contain "message"

  Scenario: Book review is rejected when rating is missing
    When I send a POST request to "/books/1/reviews" with body:
      """
      { "memberId": 1, "reviewText": "Great book, highly recommend it!" }
      """
    Then the response status should be 400
    And the response body should contain "message"

  Scenario: Book review is rejected when reviewText is missing
    When I send a POST request to "/books/1/reviews" with body:
      """
      { "memberId": 1, "rating": 4 }
      """
    Then the response status should be 400
    And the response body should contain "message"

  Scenario: Book review is rejected when memberId is missing
    When I send a POST request to "/books/1/reviews" with body:
      """
      { "rating": 4, "reviewText": "Great book!" }
      """
    Then the response status should be 400
    And the response body should contain "message"

  Scenario: Reading list creation is rejected when name is missing
    When I send a POST request to "/library/reading-lists" with body:
      """
      { "memberId": 1, "visibility": "PRIVATE" }
      """
    Then the response status should be 400
    And the response body should contain "message"

  Scenario: Reading list creation is rejected when memberId is missing
    When I send a POST request to "/library/reading-lists" with body:
      """
      { "name": "My List", "visibility": "PRIVATE" }
      """
    Then the response status should be 400
    And the response body should contain "message"

  Scenario: Event registration is rejected when memberId is missing
    When I send a POST request to "/events/1/register" with body:
      """
      {}
      """
    Then the response status should be 400
    And the response body should contain "message"

  @auth
  Scenario: Login response contains an access token with roles claim
    When I send a POST request to "/v1/auth/authenticate" with body:
      """
      { "username": "alice@citylibrary.org", "password": "password123" }
      """
    Then the response status should be 200
    And the response body should contain "access_token"
    And the JWT access_token contains a roles claim

  @auth
  Scenario: Admin JWT contains ADMIN role
    When I send a POST request to "/v1/auth/authenticate" with body:
      """
      { "username": "alice@citylibrary.org", "password": "password123" }
      """
    Then the response status should be 200
    And the JWT roles claim contains "ADMIN"

  @auth
  Scenario: Regular user JWT contains USER role
    When I send a POST request to "/v1/auth/authenticate" with body:
      """
      { "username": "carol@example.com", "password": "password123" }
      """
    Then the response status should be 200
    And the JWT roles claim contains "USER"

