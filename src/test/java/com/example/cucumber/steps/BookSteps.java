package com.example.cucumber.steps;

import com.example.cucumber.TestContext;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import org.springframework.beans.factory.annotation.Autowired;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

public class BookSteps {

    @Autowired
    private TestContext ctx;

    @Given("the book catalog contains books")
    public void theBookCatalogContainsBooks() {
        Response response = given()
                .when()
                .get("/books")
                .then()
                .extract().response();

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.jsonPath().getList("$")).isNotEmpty();
    }

    @When("I search for books with query {string}")
    public void iSearchForBooksWithQuery(String query) {
        Response response = given()
                .queryParam("q", query)
                .when()
                .get("/search/books")
                .then()
                .extract().response();

        ctx.setLastResponse(response);
    }

    @When("I get the book with ID {int}")
    public void iGetTheBookWithId(int id) {
        Response response = given()
                .when()
                .get("/books/{id}", id)
                .then()
                .extract().response();

        ctx.setLastResponse(response);
    }

    @When("I get the book with ISBN {string}")
    public void iGetTheBookWithIsbn(String isbn) {
        Response response = given()
                .when()
                .get("/books/isbn/{isbn}", isbn)
                .then()
                .extract().response();

        ctx.setLastResponse(response);
    }

    @Then("the response should contain a book with title {string}")
    public void theResponseShouldContainABookWithTitle(String title) {
        Response response = ctx.getLastResponse();
        assertThat(response).isNotNull();

        String body = response.getBody().asString();
        assertThat(body).contains(title);
    }

    @Then("the response should contain at least {int} books")
    public void theResponseShouldContainAtLeastBooks(int minCount) {
        Response response = ctx.getLastResponse();
        assertThat(response).isNotNull();

        java.util.List<?> books = response.jsonPath().getList("$");
        assertThat(books).hasSizeGreaterThanOrEqualTo(minCount);
    }

    @When("I create a book with title {string} and ISBN {string}")
    public void iCreateABookWithTitleAndIsbn(String title, String isbn) {
        String body = String.format(
                "{\"title\": \"%s\", \"isbn\": \"%s\"}", title, isbn);

        Response response = given()
                .header("Authorization", "Bearer " + ctx.getAuthToken())
                .contentType("application/json")
                .body(body)
                .when()
                .post("/books")
                .then()
                .extract().response();

        ctx.setLastResponse(response);
    }

    @When("I request new arrivals")
    public void iRequestNewArrivals() {
        Response response = given()
                .when()
                .get("/books/new-arrivals")
                .then()
                .extract().response();

        ctx.setLastResponse(response);
    }

    @When("I get copies of book {int}")
    public void iGetCopiesOfBook(int bookId) {
        Response response = given()
                .when()
                .get("/books/{id}/copies", bookId)
                .then()
                .extract().response();

        ctx.setLastResponse(response);
    }
}
