Feature: Branches, Authors, and Catalog Search

  As a library system user
  I want to browse branches and authors and search the catalog
  So that I can find books and library locations effectively

  Background:
    Given the application is running
    And I authenticate as "alice@citylibrary.org" with password "password123"

  Scenario: The system has exactly three branches
    When I send a GET request to "/branches"
    Then the response status should be 200
    And the response body should contain exactly 3 branches
    And the branch names should include "City Library Main Branch"
    And the branch names should include "Westside Branch"
    And the branch names should include "North End Library"

  Scenario: Retrieving a branch by ID returns the correct branch name
    Given I obtain the branch ID for "Westside Branch" from "/branches"
    When I send a GET request to "/branches/{branchId}"
    Then the response status should be 200
    And the response body should contain field "name" with value "Westside Branch"

  Scenario: The authors list is non-empty and each entry includes name fields
    When I send a GET request to "/authors"
    Then the response status should be 200
    And the response body should contain a non-empty list of authors

  Scenario: Retrieving an author by ID returns the correct author details
    Given I obtain the author ID for "George Orwell" from "/authors"
    When I send a GET request to "/authors/{authorId}"
    Then the response status should be 200
    And the response body should contain field "lastName" with value "Orwell"

  Scenario: Retrieving the books for a specific author returns their catalog entries
    Given I obtain the author ID for "Harper Lee" from "/authors"
    When I send a GET request to "/authors/{authorId}/books"
    Then the response status should be 200
    And the response body should be a valid list

  Scenario: Searching the catalog by author last name returns matching books
    When I send a GET request to "/books?author=Orwell"
    Then the response status should be 200
    And the response body should contain a non-empty list of books
    And the results should include a book matching "Orwell"

  Scenario: Searching the catalog by book title returns the matching book
    When I send a GET request to "/books?title=1984"
    Then the response status should be 200
    And the response body should contain a non-empty list of books
    And the results should include a book matching "1984"

  Scenario: Searching the catalog with a nonsense query returns an empty result set
    When I send a GET request to "/books?title=xzqwjfkpvmblorg"
    Then the response status should be 200
    And the response body should contain an empty list of books
