Feature: Digital Resources

  As a library system user
  I want to access the digital resource catalog
  So that members can browse and use eBooks, audiobooks, and reference databases

  Background:
    Given I authenticate as "alice@citylibrary.org" with password "password123"

  Scenario: The digital resource catalog returns a non-empty list
    When I send a GET request to "/library/digital"
    Then the response status should be 200
    And the response body should be a non-empty array

  Scenario: Each digital resource entry includes its type and format
    When I send a GET request to "/library/digital"
    Then the response status should be 200
    And each item in the response array should contain "title"
    And each item in the response array should contain "resourceType"
    And each item in the response array should contain "format"

  Scenario: Retrieving a specific eBook resource by ID returns correct details
    Given a digital resource of type "EBOOK" exists
    When I send a GET request to "/library/digital/{resourceId}"
    Then the response status should be 200
    And the response body should contain "title" with value "Effective Java (eBook)"
    And the response body should contain "resourceType" with value "EBOOK"
    And the response body should contain "format" with value "EPUB"

  Scenario: Retrieving an audiobook resource returns the correct audio format
    Given a digital resource of type "AUDIOBOOK" exists
    When I send a GET request to "/library/digital/{resourceId}"
    Then the response status should be 200
    And the response body should contain "title" with value "1984 (Audiobook)"
    And the response body should contain "resourceType" with value "AUDIOBOOK"
    And the response body should contain "format" with value "MP3"

  Scenario: Retrieving a database resource returns HTML format
    Given a digital resource of type "DATABASE" exists
    When I send a GET request to "/library/digital/{resourceId}"
    Then the response status should be 200
    And the response body should contain "title" with value "Oxford Reference Online"
    And the response body should contain "resourceType" with value "DATABASE"
    And the response body should contain "format" with value "HTML"

  Scenario: A regular member can browse the digital catalog
    Given I authenticate as "carol@example.com" with password "password123"
    When I send a GET request to "/library/digital"
    Then the response status should be 200
    And the response body should be a non-empty array
