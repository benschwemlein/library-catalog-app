Feature: Library Authentication

  As a library system user
  I want to authenticate with my credentials
  So that I can access library resources based on my role

  Scenario: Member logs in with valid credentials and receives tokens
    Given "carol@example.com" is a registered user with the USER role
    When I submit a login request with username "carol@example.com" and password "password123"
    Then the response status should be 200
    And the response body should contain an "access_token"
    And the response body should not contain an "error"
    And the response body should contain "first_name" and "last_name"

  Scenario: Admin logs in with valid credentials and receives tokens
    Given "alice@citylibrary.org" is a registered user with the USER role
    When I submit a login request with username "alice@citylibrary.org" and password "password123"
    Then the response status should be 200
    And the response body should contain an "access_token"

  Scenario: Login fails when the password is incorrect
    Given "carol@example.com" is a registered user with the USER role
    When I submit a login request with username "carol@example.com" and password "wrongpassword"
    Then the response status should be 401
    And the response body should not contain an "access_token"

  Scenario: Login fails for an email address that is not registered
    Given no user exists with the email "nobody@notlibrary.org"
    When I submit a login request with username "nobody@notlibrary.org" and password "password123"
    Then the response status should be 401
    And the response body should not contain an "access_token"

  Scenario: Valid refresh token produces a new access token
    Given I authenticate as "carol@example.com" with password "password123"
    When I refresh my access token
    Then the response status should be 200
    And the response body should contain an "access_token"
