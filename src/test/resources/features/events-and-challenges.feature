Feature: Library Events, Reading Challenges, and Book Clubs

  As a library system user
  I want to browse and manage library events, reading challenges, and book clubs
  So that members can participate in community programs and track their reading goals

  Background:
    Given the application is running
    And I authenticate as "alice@citylibrary.org" with password "password123"

  Scenario: The events list returns all seeded library events
    When I send a GET request to "/events"
    Then the response status should be 200
    And the response body should be a non-empty array
    And the response body should contain "Java & Spring Workshop"
    And the response body should contain "Kids Story Hour"
    And the response body should contain "Mystery Book Club"

  Scenario: Retrieving a specific event by ID returns the event details
    Given an event exists in the catalog
    When I send a GET request to "/events/{eventId}"
    Then the response status should be 200
    And the response body should contain "id"
    And the response body should contain "title"

  Scenario: Admin creates a new library event
    Given I obtain the branch ID for "City Library Main Branch" from "/branches"
    When I send a POST request to "/events" with body:
      """
      {
        "title": "Poetry Night",
        "description": "An evening of poetry readings and open mic performances",
        "branchId": "{branchId}",
        "startDateTime": "2026-08-15T18:00:00",
        "endDateTime": "2026-08-15T20:00:00",
        "capacity": 50,
        "eventType": "OTHER"
      }
      """
    Then the response status should be 201
    And the response body should contain "id"
    And the response body should contain "title" with value "Poetry Night"

  Scenario: The reading challenges list includes the active summer challenge
    When I send a GET request to "/library/challenges"
    Then the response status should be 200
    And the response body should be a non-empty array
    And the response body should contain "Summer Reading Challenge 2026"

  Scenario: Retrieving a specific reading challenge by ID returns the challenge details
    Given a reading challenge exists
    When I send a GET request to "/library/challenges/{challengeId}"
    Then the response status should be 200
    And the response body should contain "id"
    And the response body should contain "name"

  Scenario: The book clubs list includes the Classic Fiction Society
    When I send a GET request to "/library/book-clubs"
    Then the response status should be 200
    And the response body should be a non-empty array
    And the response body should contain "Classic Fiction Society"

  Scenario: Retrieving a specific book club by ID returns the club details
    Given a book club exists
    When I send a GET request to "/library/book-clubs/{clubId}"
    Then the response status should be 200
    And the response body should contain "id"
    And the response body should contain "name"
