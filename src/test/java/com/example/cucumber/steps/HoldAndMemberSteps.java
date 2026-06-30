package com.example.cucumber.steps;

import com.example.cucumber.TestContext;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.restassured.response.Response;
import org.springframework.beans.factory.annotation.Autowired;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

public class HoldAndMemberSteps {

    @Autowired
    private TestContext ctx;

    @Given("a hold exists for member {int} on book {int} at branch {int}")
    public void aHoldExistsForMemberOnBookAtBranch(int memberId, int bookId, int branchId) {
        String body = String.format(
                "{\"memberId\": %d, \"bookId\": %d, \"pickupBranchId\": %d}",
                memberId, bookId, branchId);
        Response response = given()
                .header("Authorization", "Bearer " + ctx.getAuthToken())
                .contentType("application/json")
                .body(body)
                .when()
                .post("/holds");
        response.then().statusCode(201);
        ctx.setLastResponse(response);
        Long holdId = response.jsonPath().getLong("id");
        ctx.setVariable("holdId", String.valueOf(holdId));
    }

    @Given("the member is currently suspended")
    public void theMemberIsCurrentlySuspended() {
        String memberId = ctx.getVariable("memberId");
        assertThat(memberId).as("memberId variable should be set").isNotNull();
        given()
                .header("Authorization", "Bearer " + ctx.getAuthToken())
                .contentType("application/json")
                .body("{\"reason\": \"Cucumber test suspension\"}")
                .when()
                .post("/members/" + memberId + "/suspend")
                .then()
                .statusCode(200);
    }
}
