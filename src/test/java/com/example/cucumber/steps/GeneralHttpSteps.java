package com.example.cucumber.steps;

import com.example.cucumber.TestContext;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

public class GeneralHttpSteps {

    @Autowired
    private TestContext ctx;

    // ── Precondition no-ops ─────────────────────────────────────────────────

    @Given("the application is running")
    public void theApplicationIsRunning() {
        given().when().get("/books").then().statusCode(200);
    }

    @Given("the authentication endpoint is available at {string}")
    public void theAuthenticationEndpointIsAvailable(String ignored) {
        // documented precondition — endpoint existence verified by other tests
    }

    @Given("{string} is a registered user with the USER role")
    public void isARegisteredUserWithRole(String email) {
        // documented precondition — user is seeded by DataSeeder
    }

    @Given("no user exists with the email {string}")
    public void noUserExistsWithEmail(String email) {
        // documented precondition — unknown@example.com is not seeded
    }

    // ── HTTP request steps ──────────────────────────────────────────────────

    @When("I submit a login request with username {string} and password {string}")
    public void iSubmitALoginRequestWithUsernameAndPassword(String username, String password) {
        Response response = given()
                .contentType("application/json")
                .body(Map.of("username", username, "password", password))
                .when()
                .post("/v1/auth/authenticate");
        ctx.setLastResponse(response);
    }

    @When("I send a GET request to {string}")
    public void iSendAGetRequestTo(String urlTemplate) {
        String url = ctx.resolveUrl(urlTemplate);
        Response response = given()
                .header("Authorization", "Bearer " + ctx.getAuthToken())
                .when()
                .get(url);
        ctx.setLastResponse(response);
    }

    @When("I send a POST request to {string}")
    public void iSendAPostRequestTo(String urlTemplate) {
        String url = ctx.resolveUrl(urlTemplate);
        Response response = given()
                .header("Authorization", "Bearer " + ctx.getAuthToken())
                .contentType("application/json")
                .when()
                .post(url);
        ctx.setLastResponse(response);
    }

    @When("I send a POST request to {string} with body:")
    public void iSendAPostRequestToWithBody(String urlTemplate, String body) {
        String url = ctx.resolveUrl(urlTemplate);
        String resolvedBody = ctx.resolveUrl(body);
        Response response = given()
                .header("Authorization", "Bearer " + ctx.getAuthToken())
                .contentType("application/json")
                .body(resolvedBody)
                .when()
                .post(url);
        ctx.setLastResponse(response);
    }

    @When("I refresh my access token")
    public void iRefreshMyAccessToken() {
        String token = ctx.getRefreshToken() != null ? ctx.getRefreshToken() : ctx.getAuthToken();
        Response response = given()
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .when()
                .post("/v1/auth/refresh-token");
        ctx.setLastResponse(response);
    }

    @When("I send a DELETE request to {string}")
    public void iSendADeleteRequestTo(String urlTemplate) {
        String url = ctx.resolveUrl(urlTemplate);
        Response response = given()
                .header("Authorization", "Bearer " + ctx.getAuthToken())
                .when()
                .delete(url);
        ctx.setLastResponse(response);
    }

    // ── Response assertion steps ────────────────────────────────────────────

    @Then("the response body should contain an {string}")
    public void theResponseBodyShouldContainAn(String field) {
        Object value = ctx.getLastResponse().jsonPath().get(field);
        assertThat(value).as("field '%s' should be present and non-null", field).isNotNull();
    }

    @Then("the response body should not contain an {string}")
    public void theResponseBodyShouldNotContainAn(String field) {
        Object value = ctx.getLastResponse().jsonPath().get(field);
        assertThat(value).as("field '%s' should be absent or null", field).isNull();
    }

    @And("the response body should contain {string} and {string}")
    public void theResponseBodyShouldContainAnd(String field1, String field2) {
        Object val1 = ctx.getLastResponse().jsonPath().get(field1);
        Object val2 = ctx.getLastResponse().jsonPath().get(field2);
        assertThat(val1).as("field '%s' should be present", field1).isNotNull();
        assertThat(val2).as("field '%s' should be present", field2).isNotNull();
    }

    @And("the response body should contain {string}")
    public void theResponseBodyShouldContain(String field) {
        try {
            Object value = ctx.getLastResponse().jsonPath().get(field);
            assertThat(value).as("field '%s' should be present and non-null", field).isNotNull();
        } catch (Exception e) {
            // Multi-word or special-char string — not a valid GPath expression; check raw body
            String body = ctx.getLastResponse().getBody().asString();
            assertThat(body).as("response body should contain '%s'", field).contains(field);
        }
    }

    @And("the response body should contain {string} with value {string}")
    public void theResponseBodyShouldContainWithStringValue(String field, String expected) {
        Object actual = ctx.getLastResponse().jsonPath().get(field);
        assertThat(String.valueOf(actual))
                .as("field '%s' should equal '%s'", field, expected)
                .isEqualTo(expected);
    }

    @And("the response body should contain {string} with value {int}")
    public void theResponseBodyShouldContainWithIntValue(String field, int expected) {
        Number actual = ctx.getLastResponse().jsonPath().get(field);
        assertThat(actual).as("field '%s' should equal %d", field, expected)
                .isNotNull();
        assertThat(actual.intValue()).isEqualTo(expected);
    }

    @And("the response body should contain field {string} with value {string}")
    public void theResponseBodyShouldContainFieldWithValue(String field, String expected) {
        Object actual = ctx.getLastResponse().jsonPath().get(field);
        assertThat(String.valueOf(actual))
                .as("field '%s' should equal '%s'", field, expected)
                .isEqualTo(expected);
    }

    @And("the response body should be a non-empty array")
    public void theResponseBodyShouldBeANonEmptyArray() {
        List<?> list = ctx.getLastResponse().jsonPath().getList("$");
        assertThat(list).isNotEmpty();
    }

    @And("the response body should be a valid list")
    public void theResponseBodyShouldBeAValidList() {
        String body = ctx.getLastResponse().getBody().asString();
        assertThat(body).startsWith("[");
    }

    @And("the response body should contain a non-empty list of members")
    public void theResponseBodyShouldContainANonEmptyListOfMembers() {
        List<?> members = ctx.getLastResponse().jsonPath().getList("$");
        assertThat(members).isNotEmpty();
    }

    @And("the response body should contain a non-empty list of books")
    public void theResponseBodyShouldContainANonEmptyListOfBooks() {
        List<?> books = ctx.getLastResponse().jsonPath().getList("$");
        assertThat(books).isNotEmpty();
    }

    @And("the response body should contain a non-empty list of authors")
    public void theResponseBodyShouldContainANonEmptyListOfAuthors() {
        List<?> authors = ctx.getLastResponse().jsonPath().getList("$");
        assertThat(authors).isNotEmpty();
    }

    @And("the response body should contain an empty list of books")
    public void theResponseBodyShouldContainAnEmptyListOfBooks() {
        List<?> books = ctx.getLastResponse().jsonPath().getList("$");
        assertThat(books).isEmpty();
    }

    @And("the response body should contain exactly {int} branches")
    public void theResponseBodyShouldContainExactlyBranches(int expected) {
        List<?> list = ctx.getLastResponse().jsonPath().getList("$");
        assertThat(list).hasSize(expected);
    }

    @And("the branch names should include {string}")
    public void theBranchNamesShouldInclude(String name) {
        List<String> names = ctx.getLastResponse().jsonPath().getList("name");
        assertThat(names).contains(name);
    }

    @And("each item in the response array should contain {string}")
    public void eachItemInTheResponseArrayShouldContain(String field) {
        List<Map<String, Object>> items = ctx.getLastResponse().jsonPath().getList("$");
        assertThat(items).isNotEmpty();
        for (Map<String, Object> item : items) {
            assertThat(item).containsKey(field);
        }
    }

    @And("each item in the response array should contain {string} with value {string}")
    public void eachItemInTheResponseArrayShouldContainWithValue(String field, String expected) {
        List<Map<String, Object>> items = ctx.getLastResponse().jsonPath().getList("$");
        assertThat(items).isNotEmpty();
        for (Map<String, Object> item : items) {
            assertThat(String.valueOf(item.get(field))).isEqualTo(expected);
        }
    }

    @And("the response body should indicate the member is suspended")
    public void theResponseBodyShouldIndicateTheMemberIsSuspended() {
        Boolean active = ctx.getLastResponse().jsonPath().getBoolean("active");
        assertThat(active).isFalse();
    }

    @And("the response body should indicate the member is active")
    public void theResponseBodyShouldIndicateTheMemberIsActive() {
        Boolean active = ctx.getLastResponse().jsonPath().getBoolean("active");
        assertThat(active).isTrue();
    }

    @And("the results should include a member matching {string}")
    public void theResultsShouldIncludeAMemberMatching(String term) {
        String body = ctx.getLastResponse().getBody().asString().toLowerCase();
        assertThat(body).contains(term.toLowerCase());
    }

    @And("the JWT access_token contains a roles claim")
    public void theJwtAccessTokenContainsARolesClaim() {
        String token = ctx.getLastResponse().jsonPath().getString("access_token");
        assertThat(token).as("access_token must be present").isNotBlank();
        String payload = decodeJwtPayload(token);
        assertThat(payload).as("JWT payload must contain 'roles' claim").contains("\"roles\"");
    }

    @And("the JWT roles claim contains {string}")
    public void theJwtRolesClaimContains(String expectedRole) {
        String token = ctx.getLastResponse().jsonPath().getString("access_token");
        assertThat(token).as("access_token must be present").isNotBlank();
        String payload = decodeJwtPayload(token);
        assertThat(payload).as("JWT payload must contain role " + expectedRole).contains(expectedRole);
    }

    private String decodeJwtPayload(String token) {
        String[] parts = token.split("\\.");
        assertThat(parts).as("JWT must have 3 parts").hasSize(3);
        byte[] decoded = java.util.Base64.getUrlDecoder().decode(parts[1]);
        return new String(decoded, java.nio.charset.StandardCharsets.UTF_8);
    }

    @And("the results should include a book matching {string}")
    public void theResultsShouldIncludeABookMatching(String term) {
        String body = ctx.getLastResponse().getBody().asString().toLowerCase();
        assertThat(body).contains(term.toLowerCase());
    }

    // ── Variable-capture steps ──────────────────────────────────────────────

    @Given("I obtain my member ID from {string}")
    public void iObtainMyMemberIdFrom(String url) {
        Response response = given()
                .header("Authorization", "Bearer " + ctx.getAuthToken())
                .when()
                .get(url);
        response.then().statusCode(200);
        Long id = response.jsonPath().getLong("id");
        ctx.setVariable("memberId", String.valueOf(id));
    }

    @Given("I obtain the member ID for {string} from {string}")
    public void iObtainTheMemberIdForFrom(String email, String url) {
        Response response = given()
                .header("Authorization", "Bearer " + ctx.getAuthToken())
                .when()
                .get(url);
        response.then().statusCode(200);
        List<Map<String, Object>> members = response.jsonPath().getList("$");
        Long id = members.stream()
                .filter(m -> {
                    Object user = m.get("user");
                    if (user instanceof Map) {
                        return email.equals(((Map<?, ?>) user).get("email"));
                    }
                    return false;
                })
                .map(m -> ((Number) m.get("id")).longValue())
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No member found with email: " + email));
        ctx.setVariable("memberId", String.valueOf(id));
    }

    @Given("I obtain the branch ID for {string} from {string}")
    public void iObtainTheBranchIdForFrom(String name, String url) {
        Response response = given()
                .when()
                .get(url);
        response.then().statusCode(200);
        List<Map<String, Object>> branches = response.jsonPath().getList("$");
        Long id = branches.stream()
                .filter(b -> name.equals(b.get("name")))
                .map(b -> ((Number) b.get("id")).longValue())
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No branch found with name: " + name));
        ctx.setVariable("branchId", String.valueOf(id));
    }

    @Given("I obtain the book ID for {string}")
    public void iObtainTheBookIdFor(String title) {
        Response response = given()
                .header("Authorization", "Bearer " + ctx.getAuthToken())
                .queryParam("title", title)
                .when()
                .get("/books");
        response.then().statusCode(200);
        List<Map<String, Object>> books = response.jsonPath().getList("$");
        Long id = books.stream()
                .filter(b -> title.equals(b.get("title")))
                .map(b -> ((Number) b.get("id")).longValue())
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No book found with title: " + title));
        ctx.setVariable("bookId", String.valueOf(id));
    }

    @Given("I obtain the copy ID for barcode {string}")
    public void iObtainTheCopyIdForBarcode(String barcode) {
        Response response = given()
                .header("Authorization", "Bearer " + ctx.getAuthToken())
                .when()
                .get("/books");
        response.then().statusCode(200);
        List<Map<String, Object>> books = response.jsonPath().getList("$");
        for (Map<String, Object> book : books) {
            Long bookId = ((Number) book.get("id")).longValue();
            Response copiesResp = given()
                    .header("Authorization", "Bearer " + ctx.getAuthToken())
                    .when()
                    .get("/books/" + bookId + "/copies");
            if (copiesResp.statusCode() != 200) continue;
            List<Map<String, Object>> copies = copiesResp.jsonPath().getList("$");
            for (Map<String, Object> copy : copies) {
                if (barcode.equals(copy.get("barcode"))) {
                    ctx.setVariable("copyId", String.valueOf(((Number) copy.get("id")).longValue()));
                    return;
                }
            }
        }
        throw new IllegalStateException("No copy found with barcode: " + barcode);
    }

    @Given("I obtain the author ID for {string} from {string}")
    public void iObtainTheAuthorIdForFrom(String name, String url) {
        Response response = given()
                .header("Authorization", "Bearer " + ctx.getAuthToken())
                .when()
                .get(url);
        response.then().statusCode(200);
        List<Map<String, Object>> authors = response.jsonPath().getList("$");
        // Authors have firstName + lastName, not a combined name field
        String[] parts = name.split(" ", 2);
        String firstName = parts[0];
        String lastName = parts.length > 1 ? parts[1] : "";
        Long id = authors.stream()
                .filter(a -> firstName.equals(a.get("firstName")) && lastName.equals(a.get("lastName")))
                .map(a -> ((Number) a.get("id")).longValue())
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No author found with name: " + name));
        ctx.setVariable("authorId", String.valueOf(id));
    }
}
