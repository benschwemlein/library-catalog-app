package com.example.cucumber.steps;

import com.example.cucumber.TestContext;
import com.example.library.repository.LoanRepository;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.restassured.response.Response;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

public class LoanSteps {

    @Autowired
    private TestContext ctx;

    @Autowired
    private LoanRepository loanRepository;

    @Given("an active loan exists for member {int} on book copy {int}")
    public void anActiveLoanExistsForMemberOnBookCopy(int memberId, int copyId) {
        String body = String.format("{\"memberId\": %d, \"bookCopyId\": %d}", memberId, copyId);
        Response response = given()
                .header("Authorization", "Bearer " + ctx.getAuthToken())
                .contentType("application/json")
                .body(body)
                .when()
                .post("/loans/checkout");
        response.then().statusCode(201);
        ctx.setLastResponse(response);
        Long loanId = response.jsonPath().getLong("id");
        ctx.setVariable("loanId", String.valueOf(loanId));
    }

    @Given("an overdue loan exists for member {int} on book copy {int}")
    public void anOverdueLoanExistsForMemberOnBookCopy(int memberId, int copyId) {
        String body = String.format("{\"memberId\": %d, \"bookCopyId\": %d}", memberId, copyId);
        Response response = given()
                .header("Authorization", "Bearer " + ctx.getAuthToken())
                .contentType("application/json")
                .body(body)
                .when()
                .post("/loans/checkout");
        response.then().statusCode(201);
        ctx.setLastResponse(response);
        Long loanId = response.jsonPath().getLong("id");
        ctx.setVariable("loanId", String.valueOf(loanId));
        // Backdate the due date so the loan appears in overdue query
        loanRepository.findById(loanId).ifPresent(loan -> {
            loan.setDueDate(LocalDateTime.now().minusDays(30));
            loanRepository.save(loan);
        });
    }

    @Given("member {int} has already reached the maximum number of active loans")
    public void memberHasAlreadyReachedMaxActiveLoans(int memberId) {
        // Suspend the member so the next checkout returns 403 (blocked)
        given()
                .header("Authorization", "Bearer " + ctx.getAuthToken())
                .contentType("application/json")
                .body("{\"reason\": \"Cucumber test suspension\"}")
                .when()
                .post("/members/" + memberId + "/suspend")
                .then()
                .statusCode(200);
    }

    @And("I save the loan id as {string}")
    public void iSaveTheLoanIdAs(String varName) {
        Long id = ctx.getLastResponse().jsonPath().getLong("id");
        assertThat(id).as("loan id should be present in response").isNotNull();
        ctx.setVariable(varName, String.valueOf(id));
    }

    @And("I save the loan due date as {string}")
    public void iSaveTheLoanDueDateAs(String varName) {
        String dueDate = ctx.getLastResponse().jsonPath().getString("dueDate");
        assertThat(dueDate).as("dueDate should be present in response").isNotNull();
        ctx.setVariable(varName, dueDate);
    }

    @Then("the response body field {string} should be later than {string}")
    public void theResponseBodyFieldShouldBeLaterThan(String field, String savedVarName) {
        String actualStr = ctx.getLastResponse().jsonPath().getString(field);
        String savedStr = ctx.getVariable(savedVarName);
        assertThat(actualStr).as("field '%s' should be present", field).isNotNull();
        assertThat(savedStr).as("saved variable '%s' should exist", savedVarName).isNotNull();
        LocalDateTime actual = LocalDateTime.parse(actualStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        LocalDateTime saved = LocalDateTime.parse(savedStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        assertThat(actual).as("'%s' should be after '%s'", field, savedVarName).isAfter(saved);
    }
}
