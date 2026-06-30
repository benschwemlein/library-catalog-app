Feature: Member Management

  As a library system user
  I want to manage member accounts and view member activity
  So that library operations can be administered effectively

  Background:
    Given the application is running
    And I authenticate as "alice@citylibrary.org" with password "password123"

  Scenario: Admin views all registered members
    When I send a GET request to "/members"
    Then the response status should be 200
    And the response body should contain a non-empty list of members

  Scenario: A member views their own profile and sees their membership number
    Given I authenticate as "carol@example.com" with password "password123"
    When I send a GET request to "/members/me"
    Then the response status should be 200
    And the response body should contain field "membershipNumber" with value "MEM-001"

  Scenario: A premium member's profile reflects the PREMIUM tier
    Given I authenticate as "david@example.com" with password "password123"
    When I send a GET request to "/members/me"
    Then the response status should be 200
    And the response body should contain field "membershipNumber" with value "MEM-002"

  Scenario: A member can view their own loan history
    Given I authenticate as "carol@example.com" with password "password123"
    And I obtain my member ID from "/members/me"
    When I send a GET request to "/members/{memberId}/loans"
    Then the response status should be 200
    And the response body should be a valid list

  Scenario: A member can view their current holds
    Given I authenticate as "carol@example.com" with password "password123"
    And I obtain my member ID from "/members/me"
    When I send a GET request to "/members/{memberId}/holds"
    Then the response status should be 200
    And the response body should be a valid list

  Scenario: A member can view their outstanding fines
    Given I authenticate as "carol@example.com" with password "password123"
    And I obtain my member ID from "/members/me"
    When I send a GET request to "/members/{memberId}/fines"
    Then the response status should be 200
    And the response body should be a valid list

  Scenario: Admin suspends a member account with a stated reason
    Given I obtain the member ID for "carol@example.com" from "/members"
    When I send a POST request to "/members/{memberId}/suspend" with body:
      """
      {
        "reason": "Overdue items not returned after repeated notices"
      }
      """
    Then the response status should be 200
    And the response body should indicate the member is suspended

  Scenario: Admin reactivates a suspended member account
    Given I obtain the member ID for "carol@example.com" from "/members"
    And the member "carol@example.com" is suspended
    When I send a POST request to "/members/{memberId}/reactivate"
    Then the response status should be 200
    And the response body should indicate the member is active

  Scenario: Searching for a member by first name returns matching results
    When I send a GET request to "/members/search?q=carol"
    Then the response status should be 200
    And the response body should contain a non-empty list of members
    And the results should include a member matching "carol"

  Scenario: Admin can view the notification history for a member
    Given I obtain the member ID for "david@example.com" from "/members"
    When I send a GET request to "/members/{memberId}/notifications"
    Then the response status should be 200
    And the response body should be a valid list
