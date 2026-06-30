package com.example.cucumber.steps;

import com.example.cucumber.TestContext;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

public class MiscSteps {

    @Autowired
    private TestContext testContext;

    // ---------------------------------------------------------------------------
    // Fine steps
    // ---------------------------------------------------------------------------

    @When("I view all fines")
    public void iViewAllFines() {
        Response response = given()
                .header("Authorization", "Bearer " + testContext.getAuthToken())
                .when()
                .get("/fines");
        testContext.setLastResponse(response);
    }

    @When("I view unpaid fines")
    public void iViewUnpaidFines() {
        Response response = given()
                .header("Authorization", "Bearer " + testContext.getAuthToken())
                .when()
                .get("/fines/unpaid");
        testContext.setLastResponse(response);
    }

    @When("I view fines for member {int}")
    public void iViewFinesForMember(int memberId) {
        Response response = given()
                .header("Authorization", "Bearer " + testContext.getAuthToken())
                .when()
                .get("/fines/member/" + memberId);
        testContext.setLastResponse(response);
    }

    @When("I pay fine {int}")
    public void iPayFine(int fineId) {
        Response response = given()
                .header("Authorization", "Bearer " + testContext.getAuthToken())
                .when()
                .post("/fines/" + fineId + "/pay");
        testContext.setLastResponse(response);
    }

    @When("I waive fine {int} with reason {string}")
    public void iWaiveFineWithReason(int fineId, String reason) {
        Map<String, String> body = new HashMap<>();
        body.put("reason", reason);
        Response response = given()
                .header("Authorization", "Bearer " + testContext.getAuthToken())
                .contentType("application/json")
                .body(body)
                .when()
                .post("/fines/" + fineId + "/waive");
        testContext.setLastResponse(response);
    }

    // ---------------------------------------------------------------------------
    // Event steps
    // ---------------------------------------------------------------------------

    @When("I view all events")
    public void iViewAllEvents() {
        Response response = given()
                .when()
                .get("/events");
        testContext.setLastResponse(response);
    }

    @Then("the events list is not empty")
    public void theEventsListIsNotEmpty() {
        List<?> events = testContext.getLastResponse().jsonPath().getList("$");
        assertThat(events).hasSizeGreaterThanOrEqualTo(1);
    }

    @When("I get event with ID {int}")
    public void iGetEventWithId(int eventId) {
        Response response = given()
                .when()
                .get("/events/" + eventId);
        testContext.setLastResponse(response);
    }

    @When("I create an event with name {string}")
    public void iCreateAnEventWithName(String name) {
        Map<String, Object> body = new HashMap<>();
        body.put("name", name);
        Response response = given()
                .contentType("application/json")
                .body(body)
                .when()
                .post("/events");
        testContext.setLastResponse(response);
        assertThat(response.statusCode()).isIn(200, 201);
    }

    // ---------------------------------------------------------------------------
    // Reading Challenge steps
    // ---------------------------------------------------------------------------

    @When("I view all reading challenges")
    public void iViewAllReadingChallenges() {
        Response response = given()
                .when()
                .get("/reading-challenges");
        testContext.setLastResponse(response);
    }

    @Then("the challenges list is not empty")
    public void theChallengesListIsNotEmpty() {
        List<?> challenges = testContext.getLastResponse().jsonPath().getList("$");
        assertThat(challenges).hasSizeGreaterThanOrEqualTo(1);
    }

    @When("I get challenge with ID {int}")
    public void iGetChallengeWithId(int challengeId) {
        Response response = given()
                .when()
                .get("/reading-challenges/" + challengeId);
        testContext.setLastResponse(response);
    }

    // ---------------------------------------------------------------------------
    // Book Club steps
    // ---------------------------------------------------------------------------

    @When("I view all book clubs")
    public void iViewAllBookClubs() {
        Response response = given()
                .when()
                .get("/book-clubs");
        testContext.setLastResponse(response);
    }

    @Then("the book clubs list is not empty")
    public void theBookClubsListIsNotEmpty() {
        List<?> clubs = testContext.getLastResponse().jsonPath().getList("$");
        assertThat(clubs).hasSizeGreaterThanOrEqualTo(1);
    }

    // ---------------------------------------------------------------------------
    // Digital Resource steps
    // ---------------------------------------------------------------------------

    @When("I view all digital resources")
    public void iViewAllDigitalResources() {
        Response response = given()
                .when()
                .get("/digital-resources");
        testContext.setLastResponse(response);
    }

    @Then("the digital resources list has at least {int} items")
    public void theDigitalResourcesListHasAtLeastItems(int minCount) {
        List<?> resources = testContext.getLastResponse().jsonPath().getList("$");
        assertThat(resources).hasSizeGreaterThanOrEqualTo(minCount);
    }

    @When("I get digital resource with ID {int}")
    public void iGetDigitalResourceWithId(int resourceId) {
        Response response = given()
                .when()
                .get("/digital-resources/" + resourceId);
        testContext.setLastResponse(response);
    }

    @Then("the resource has type {string}")
    public void theResourceHasType(String expectedType) {
        String actualType = testContext.getLastResponse().jsonPath().getString("resourceType");
        assertThat(actualType).isEqualTo(expectedType);
    }

    // ---------------------------------------------------------------------------
    // Branch and Author steps
    // ---------------------------------------------------------------------------

    @When("I view all branches")
    public void iViewAllBranches() {
        Response response = given()
                .when()
                .get("/branches");
        testContext.setLastResponse(response);
    }

    @Then("the branches list has {int} branches")
    public void theBranchesListHasBranches(int expectedCount) {
        List<?> branches = testContext.getLastResponse().jsonPath().getList("$");
        assertThat(branches).hasSize(expectedCount);
    }

    @When("I view all authors")
    public void iViewAllAuthors() {
        Response response = given()
                .when()
                .get("/authors");
        testContext.setLastResponse(response);
    }

    @When("I get books by author {int}")
    public void iGetBooksByAuthor(int authorId) {
        Response response = given()
                .when()
                .get("/authors/" + authorId + "/books");
        testContext.setLastResponse(response);
    }

    @When("I search for {string}")
    public void iSearchFor(String query) {
        Response response = given()
                .queryParam("q", query)
                .when()
                .get("/search/books");
        testContext.setLastResponse(response);
    }

    @Then("the search results are empty")
    public void theSearchResultsAreEmpty() {
        List<?> results = testContext.getLastResponse().jsonPath().getList("$");
        assertThat(results).isEmpty();
    }

    // ---------------------------------------------------------------------------
    // Shared assertion steps
    // ---------------------------------------------------------------------------

    @Then("the response is not empty")
    public void theResponseIsNotEmpty() {
        List<?> items = testContext.getLastResponse().jsonPath().getList("$");
        assertThat(items).hasSizeGreaterThan(0);
    }

    // ---------------------------------------------------------------------------
    // Fine setup steps
    // ---------------------------------------------------------------------------

    @Given("a fine exists for member {int}")
    public void aFineExistsForMember(int memberId) {
        Response response = given()
                .header("Authorization", "Bearer " + testContext.getAuthToken())
                .when()
                .get("/fines");
        response.then().statusCode(200);
        List<Map<String, Object>> fines = response.jsonPath().getList("$");
        Map<String, Object> fine = fines.stream()
                .filter(f -> {
                    Object member = f.get("member");
                    if (member instanceof Map) {
                        Object id = ((Map<?, ?>) member).get("id");
                        return id != null && ((Number) id).intValue() == memberId;
                    }
                    return false;
                })
                .filter(f -> !"PAID".equals(f.get("status")) && !"WAIVED".equals(f.get("status")))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "No unpaid fine found for member " + memberId));
        testContext.setLastResponse(response);
        testContext.setVariable("fineId", String.valueOf(((Number) fine.get("id")).longValue()));
    }

    @And("I save the fine id as {string}")
    public void iSaveTheFineIdAs(String varName) {
        String fineId = testContext.getVariable("fineId");
        assertThat(fineId).as("fineId should have been stored by 'a fine exists for member' step").isNotNull();
        testContext.setVariable(varName, fineId);
    }
}
