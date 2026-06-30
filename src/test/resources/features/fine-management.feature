Feature: Fine Management

  As a library system administrator or staff member
  I want to manage fines on overdue loans
  So that the library can track and resolve outstanding member obligations

  Background:
    Given I authenticate as "alice@citylibrary.org" with password "password123"

  Scenario: Admin can view the list of all fines in the system
    When I send a GET request to "/fines"
    Then the response status should be 200
    And the response body should be a valid list

  Scenario: Admin can view only unpaid fines
    When I send a GET request to "/fines/unpaid"
    Then the response status should be 200
    And the response body should be a valid list

  Scenario: A member can view their own fines
    Given I authenticate as "carol@example.com" with password "password123"
    And I obtain my member ID from "/members/me"
    When I send a GET request to "/fines/member/{memberId}"
    Then the response status should be 200
    And the response body should be a valid list

  Scenario: Admin pays a fine on behalf of a member and the fine status becomes PAID
    Given an overdue loan exists where "carol@example.com" has borrowed "1984"
    And a fine exists for member "carol@example.com"
    And I save the fine id as "fineId"
    When I send a POST request to "/fines/{fineId}/pay"
    Then the response status should be 200
    And the response body should contain "status" with value "PAID"

  Scenario: Staff waives a fine with a reason and the status becomes WAIVED
    Given an overdue loan exists where "david@example.com" has borrowed "Animal Farm"
    And a fine exists for member "david@example.com"
    And I save the fine id as "fineId"
    When I send a POST request to "/fines/{fineId}/waive" with body:
      """
      {
        "reason": "Waived as a courtesy for a long-standing member in good standing"
      }
      """
    Then the response status should be 200
    And the response body should contain "status" with value "WAIVED"

  Scenario: Checkout is blocked when a member has unpaid fines exceeding the $10 threshold
    Given an overdue loan exists where "eva@example.com" has borrowed "1984"
    And a fine exists for member "eva@example.com"
    And the member "eva@example.com" has an available copy of "Clean Code" ready to check out
    When I send a POST request to "/loans/checkout" with body:
      """
      {
        "memberId": "{memberId}",
        "bookCopyId": "{copyId}"
      }
      """
    Then the response status should be 403
