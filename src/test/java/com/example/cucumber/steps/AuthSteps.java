package com.example.cucumber.steps;

import com.example.cucumber.TestContext;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

public class AuthSteps {

    private final TestContext ctx;

    public AuthSteps(TestContext ctx) {
        this.ctx = ctx;
    }

    /**
     * Helper step: authenticates as the given user and stores the token.
     * Expects a fixture password matching the username, or uses a default.
     * Usage: Given I am authenticated as "admin@example.com"
     */
    @Given("I am authenticated as {string}")
    public void iAmAuthenticatedAs(String email) {
        String token = authenticate(email, "password");
        ctx.setAuthToken(token);
    }

    /**
     * Full-form authentication step that stores the token for reuse.
     * Usage: Given I authenticate as "user@example.com" with password "secret"
     */
    @Given("I authenticate as {string} with password {string}")
    public void iAuthenticateAsWithPassword(String email, String password) {
        String token = authenticate(email, password);
        ctx.setAuthToken(token);
    }

    /**
     * Sends a login request and stores the raw response for later assertion steps.
     * Usage: When I send a login request with email "user@example.com" and password "wrong"
     */
    @When("I register with email {string} first name {string} and password {string}")
    public void iRegisterWithEmailFirstNameAndPassword(String email, String firstName, String password) {
        Response response = given()
                .contentType("application/json")
                .body(Map.of(
                        "firstname", firstName,
                        "lastname", "User",
                        "email", email,
                        "password", password,
                        "role", "USER"
                ))
                .when()
                .post("/v1/auth/register");
        ctx.setLastResponse(response);
    }

    @When("I send a login request with email {string} and password {string}")
    public void iSendALoginRequestWithEmailAndPassword(String email, String password) {
        Response response = given()
                .contentType("application/json")
                .body(Map.of("username", email, "password", password))
                .when()
                .post("/v1/auth/authenticate");
        ctx.setLastResponse(response);
    }

    /**
     * Asserts that the most recently captured response has the expected HTTP status code.
     * Usage: Then the response status should be 200
     */
    @Then("the response status should be {int}")
    public void theResponseStatusShouldBe(int expectedStatus) {
        assertThat(ctx.getLastResponse())
                .as("last response must not be null")
                .isNotNull();
        ctx.getLastResponse().then().statusCode(expectedStatus);
    }

    /**
     * Asserts that the most recently captured response contains an access_token and stores it.
     * Usage: And I receive an access token
     */
    @And("I receive an access token")
    public void iReceiveAnAccessToken() {
        assertThat(ctx.getLastResponse())
                .as("last response must not be null")
                .isNotNull();

        String token = ctx.getLastResponse()
                .then()
                .statusCode(200)
                .extract()
                .path("access_token");

        assertThat(token)
                .as("access_token should be present in the response body")
                .isNotBlank();

        ctx.setAuthToken(token);
    }

    /**
     * Asserts that the most recently captured response body contains an error indicator.
     * Usage: And the response should contain an error
     */
    @And("the response should contain an error")
    public void theResponseShouldContainAnError() {
        assertThat(ctx.getLastResponse())
                .as("last response must not be null")
                .isNotNull();

        Response response = ctx.getLastResponse();

        // Accept either a top-level "error" field or a "message" field (Spring error bodies)
        String body = response.getBody().asString();
        assertThat(body)
                .as("response body should contain an error indicator")
                .satisfiesAnyOf(
                        b -> assertThat(b).containsIgnoringCase("error"),
                        b -> assertThat(b).containsIgnoringCase("message")
                );
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    /**
     * Performs a login request and returns the extracted access_token string.
     * Throws an AssertionError (via RestAssured) if the status is not 200.
     */
    private String authenticate(String email, String password) {
        io.restassured.response.Response response = given()
                .contentType("application/json")
                .body(Map.of("username", email, "password", password))
                .when()
                .post("/v1/auth/authenticate")
                .then()
                .statusCode(200)
                .extract()
                .response();
        String refresh = response.jsonPath().getString("refresh_token");
        if (refresh != null) {
            ctx.setRefreshToken(refresh);
        }
        return response.jsonPath().getString("access_token");
    }
}
