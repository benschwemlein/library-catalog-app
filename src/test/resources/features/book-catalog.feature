Feature: Book Catalog

  As a library user or administrator
  I want to browse, search, and manage the book catalog
  So that I can find books and keep the catalog up to date

  Background:
    Given I authenticate as "alice@citylibrary.org" with password "password123"

  Scenario: Browsing the catalog returns a non-empty list of books
    When I send a GET request to "/books"
    Then the response status should be 200
    And the response body should contain a non-empty list of books

  Scenario: Each book in the catalog includes title and author information
    When I send a GET request to "/books"
    Then the response status should be 200
    And each item in the response array should contain "title"
    And each item in the response array should contain "authorNames"

  Scenario: Searching by title finds the matching book
    When I send a GET request to "/books?title=1984"
    Then the response status should be 200
    And the response body should contain a non-empty list of books
    And the results should include a book matching "1984"

  Scenario: Searching by author name finds their books
    When I send a GET request to "/books?author=Orwell"
    Then the response status should be 200
    And the response body should contain a non-empty list of books
    And the results should include a book matching "Orwell"

  Scenario: Searching by a title that does not exist returns an empty list
    When I send a GET request to "/books?title=xzqwjfkpvmblorg"
    Then the response status should be 200
    And the response body should contain an empty list of books

  Scenario: A member can view the available copies of a book
    Given I authenticate as "carol@example.com" with password "password123"
    When I send a GET request to "/books?title=Effective Java"
    Then the response status should be 200
    And the response body should contain a non-empty list of books

  Scenario: Admin creates a new book entry in the catalog
    Given I authenticate as "alice@citylibrary.org" with password "password123"
    When I send a POST request to "/books" with body:
      """
      {
        "title": "The Pragmatic Programmer",
        "isbn": "9780135957059",
        "publishedYear": 2019,
        "genre": "TECHNOLOGY",
        "author": {
          "firstName": "David",
          "lastName": "Thomas"
        }
      }
      """
    Then the response status should be 201
    And the response body should contain "id"
    And the response body should contain "title" with value "The Pragmatic Programmer"

  Scenario: Manager can also add a new book to the catalog
    Given I authenticate as "bob@citylibrary.org" with password "password123"
    When I send a POST request to "/books" with body:
      """
      {
        "title": "Domain-Driven Design",
        "isbn": "9780321125217",
        "publishedYear": 2003,
        "genre": "TECHNOLOGY",
        "author": {
          "firstName": "Eric",
          "lastName": "Evans"
        }
      }
      """
    Then the response status should be 201
    And the response body should contain "id"
    And the response body should contain "title" with value "Domain-Driven Design"
