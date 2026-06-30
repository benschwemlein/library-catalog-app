Feature: Registration creates a member profile

  As a new user who registers
  I want my own member profile to be created automatically
  So that I see my own data and can check out books

  Scenario: Registered user gets their own member profile
    When I register with email "neweval@example.com" first name "Eval" and password "pass123"
    Then the response status should be 200
    And I authenticate as "neweval@example.com" with password "pass123"
    And I send a GET request to "/members/me"
    Then the response status should be 200
    And the response body should contain "user"

  Scenario: Registered user member profile has correct name
    When I register with email "evaltest2@example.com" first name "EvalTwo" and password "pass123"
    And I authenticate as "evaltest2@example.com" with password "pass123"
    And I send a GET request to "/members/me"
    Then the response status should be 200
    And the response body should contain "user.firstName" with value "EvalTwo"

  Scenario: Registered user has their own membership number
    When I register with email "evaltest3@example.com" first name "EvalThree" and password "pass123"
    And I authenticate as "evaltest3@example.com" with password "pass123"
    And I send a GET request to "/members/me"
    Then the response status should be 200
    And the response body should contain "membershipNumber"
