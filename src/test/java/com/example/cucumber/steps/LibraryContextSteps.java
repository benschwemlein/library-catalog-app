package com.example.cucumber.steps;

import com.example.cucumber.TestContext;
import com.example.library.entity.Fine;
import com.example.library.entity.Loan;
import com.example.library.entity.LoanStatus;
import com.example.library.repository.FineRepository;
import com.example.library.repository.LoanRepository;
import com.example.library.service.FineService;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.restassured.response.Response;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

public class LibraryContextSteps {

    @Autowired
    private TestContext ctx;

    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private FineRepository fineRepository;

    @Autowired
    private FineService fineService;

    // ── Member lookup helper ────────────────────────────────────────────────

    private Long findMemberIdByEmail(String email) {
        Response response = given()
                .header("Authorization", "Bearer " + ctx.getAuthToken())
                .when()
                .get("/members");
        response.then().statusCode(200);
        List<Map<String, Object>> members = response.jsonPath().getList("$");
        return members.stream()
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
    }

    private Long findAvailableCopyByBookTitle(String title) {
        Response booksResponse = given()
                .header("Authorization", "Bearer " + ctx.getAuthToken())
                .queryParam("title", title)
                .when()
                .get("/books");
        booksResponse.then().statusCode(200);
        List<Map<String, Object>> books = booksResponse.jsonPath().getList("$");
        Map<String, Object> match = books.stream()
                .filter(b -> title.equals(b.get("title")))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No book found with title: " + title));
        Long bookId = ((Number) match.get("id")).longValue();
        return findAvailableCopyForBook(bookId, title);
    }

    private Long findAvailableCopyForBook(Long bookId, String context) {
        Response copiesResponse = given()
                .header("Authorization", "Bearer " + ctx.getAuthToken())
                .when()
                .get("/books/" + bookId + "/copies");
        copiesResponse.then().statusCode(200);
        List<Map<String, Object>> copies = copiesResponse.jsonPath().getList("$");
        return copies.stream()
                .filter(c -> "AVAILABLE".equals(c.get("status")))
                .map(c -> ((Number) c.get("id")).longValue())
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No available copy of: " + context));
    }

    private Long findBookIdByTitle(String title) {
        Response response = given()
                .header("Authorization", "Bearer " + ctx.getAuthToken())
                .queryParam("title", title)
                .when()
                .get("/books");
        response.then().statusCode(200);
        List<Map<String, Object>> books = response.jsonPath().getList("$");
        return books.stream()
                .filter(b -> title.equals(b.get("title")))
                .map(b -> ((Number) b.get("id")).longValue())
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No book found with title: " + title));
    }

    private Long checkoutCopy(Long memberId, Long copyId) {
        String body = String.format("{\"memberId\": %d, \"bookCopyId\": %d}", memberId, copyId);
        Response response = given()
                .header("Authorization", "Bearer " + ctx.getAuthToken())
                .contentType("application/json")
                .body(body)
                .when()
                .post("/loans/checkout");
        response.then().statusCode(201);
        return response.jsonPath().getLong("id");
    }

    // ── Loan setup steps ────────────────────────────────────────────────────

    @Given("the member {string} has an available copy of {string} ready to check out")
    public void theMemberHasAvailableCopyReadyToCheckOut(String email, String bookTitle) {
        Long memberId = findMemberIdByEmail(email);
        Long copyId = findAvailableCopyByBookTitle(bookTitle);
        ctx.setVariable("memberId", String.valueOf(memberId));
        ctx.setVariable("copyId", String.valueOf(copyId));
    }

    @Given("an active loan exists where {string} has borrowed {string}")
    public void anActiveLoanExistsWhereHasBorrowed(String email, String bookTitle) {
        Long memberId = findMemberIdByEmail(email);
        Long copyId = findAvailableCopyByBookTitle(bookTitle);
        String body = String.format("{\"memberId\": %d, \"bookCopyId\": %d}", memberId, copyId);
        Response response = given()
                .header("Authorization", "Bearer " + ctx.getAuthToken())
                .contentType("application/json")
                .body(body)
                .when()
                .post("/loans/checkout");
        response.then().statusCode(201);
        ctx.setLastResponse(response);
        ctx.setVariable("loanId", String.valueOf(response.jsonPath().getLong("id")));
        ctx.setVariable("memberId", String.valueOf(memberId));
    }

    @Given("an overdue loan exists where {string} has borrowed {string}")
    public void anOverdueLoanExistsWhereHasBorrowed(String email, String bookTitle) {
        anActiveLoanExistsWhereHasBorrowed(email, bookTitle);
        Long loanId = Long.parseLong(ctx.getVariable("loanId"));
        loanRepository.findById(loanId).ifPresent(loan -> {
            loan.setDueDate(LocalDateTime.now().minusDays(120));
            loanRepository.save(loan);
        });
    }

    @Given("the member {string} has reached their concurrent loan limit")
    public void theMemberHasReachedTheirConcurrentLoanLimit(String email) {
        Long memberId = findMemberIdByEmail(email);
        ctx.setVariable("memberId", String.valueOf(memberId));
        String[] books = {
                "Effective Java", "Clean Code", "Refactoring",
                "Spring in Action", "1984", "Animal Farm"
        };
        for (String book : books) {
            try {
                Long copyId = findAvailableCopyByBookTitle(book);
                String body = String.format("{\"memberId\": %d, \"bookCopyId\": %d}", memberId, copyId);
                Response response = given()
                        .header("Authorization", "Bearer " + ctx.getAuthToken())
                        .contentType("application/json")
                        .body(body)
                        .when()
                        .post("/loans/checkout");
                if (response.statusCode() != 201) {
                    break; // hit the concurrent loan limit
                }
            } catch (IllegalStateException e) {
                // no available copy of this book, try next
            }
        }
    }

    // ── Hold setup steps ────────────────────────────────────────────────────

    @Given("a hold exists for member {string} on book {string}")
    public void aHoldExistsForMemberOnBook(String email, String bookTitle) {
        Long memberId = findMemberIdByEmail(email);
        Long bookId = findBookIdByTitle(bookTitle);
        Response branchResponse = given()
                .when()
                .get("/branches");
        branchResponse.then().statusCode(200);
        Long branchId = ((Number) branchResponse.jsonPath()
                .getList("$", Map.class).get(0).get("id")).longValue();

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
        ctx.setVariable("holdId", String.valueOf(response.jsonPath().getLong("id")));
        ctx.setVariable("memberId", String.valueOf(memberId));
    }

    // ── Member state steps ──────────────────────────────────────────────────

    @Given("the member {string} is suspended")
    public void theMemberIsSuspended(String email) {
        Long memberId = findMemberIdByEmail(email);
        given()
                .header("Authorization", "Bearer " + ctx.getAuthToken())
                .contentType("application/json")
                .body("{\"reason\": \"Cucumber test suspension\"}")
                .when()
                .post("/members/" + memberId + "/suspend")
                .then()
                .statusCode(200);
        ctx.setVariable("memberId", String.valueOf(memberId));
    }

    // ── Fine setup steps ────────────────────────────────────────────────────

    @Given("a fine exists for member {string}")
    public void aFineExistsForMemberByEmail(String email) {
        Long memberId = findMemberIdByEmail(email);
        // Prefer issuing a fine against the specific loan set up in this scenario
        String loanIdStr = ctx.getVariable("loanId");
        if (loanIdStr != null) {
            Long loanId = Long.parseLong(loanIdStr);
            List<Fine> loanFines = fineRepository.findByLoan_Id(loanId);
            Fine unpaidLoanFine = loanFines.stream()
                    .filter(f -> f.getPaidDate() == null)
                    .findFirst().orElse(null);
            if (unpaidLoanFine != null) {
                ctx.setVariable("fineId", String.valueOf(unpaidLoanFine.getId()));
                return;
            }
            Loan loan = loanRepository.findById(loanId).orElse(null);
            if (loan != null) {
                Fine fine = fineService.issueFine(loan);
                if (fine != null) {
                    ctx.setVariable("fineId", String.valueOf(fine.getId()));
                    return;
                }
            }
        }
        // Fallback: find any existing unpaid fine for the member
        List<Fine> existing = fineRepository.findByMember_Id(memberId).stream()
                .filter(f -> f.getPaidDate() == null)
                .toList();
        if (!existing.isEmpty()) {
            ctx.setVariable("fineId", String.valueOf(existing.get(0).getId()));
            return;
        }
        // Last resort: find any overdue loan and issue a fine
        List<Loan> overdueLoans = loanRepository.findAll().stream()
                .filter(l -> l.getMember() != null && memberId.equals(l.getMember().getId()))
                .filter(l -> LoanStatus.ACTIVE.equals(l.getStatus()))
                .filter(l -> l.getDueDate() != null && l.getDueDate().isBefore(LocalDateTime.now()))
                .toList();
        if (overdueLoans.isEmpty()) {
            throw new IllegalStateException(
                    "No overdue loan found for " + email +
                    ". Run 'an overdue loan exists where...' before this step.");
        }
        Fine fine = fineService.issueFine(overdueLoans.get(0));
        assertThat(fine).as("issued fine should not be null (loan must be overdue by enough days)").isNotNull();
        ctx.setVariable("fineId", String.valueOf(fine.getId()));
    }

    @And("I save the hold id as {string}")
    public void iSaveTheHoldIdAs(String varName) {
        Long id = ctx.getLastResponse().jsonPath().getLong("id");
        assertThat(id).as("hold id should be present in response").isNotNull();
        ctx.setVariable(varName, String.valueOf(id));
    }

    // ── Catalog entity lookup steps ─────────────────────────────────────────

    @Given("an event exists in the catalog")
    public void anEventExistsInTheCatalog() {
        Response response = given()
                .header("Authorization", "Bearer " + ctx.getAuthToken())
                .when()
                .get("/events");
        response.then().statusCode(200);
        List<Map<String, Object>> events = response.jsonPath().getList("$");
        assertThat(events).as("events list should not be empty").isNotEmpty();
        ctx.setVariable("eventId", String.valueOf(((Number) events.get(0).get("id")).longValue()));
    }

    @Given("a reading challenge exists")
    public void aReadingChallengeExists() {
        Response response = given()
                .header("Authorization", "Bearer " + ctx.getAuthToken())
                .when()
                .get("/library/challenges");
        response.then().statusCode(200);
        List<Map<String, Object>> challenges = response.jsonPath().getList("$");
        assertThat(challenges).as("challenges list should not be empty").isNotEmpty();
        ctx.setVariable("challengeId", String.valueOf(((Number) challenges.get(0).get("id")).longValue()));
    }

    @Given("a book club exists")
    public void aBookClubExists() {
        Response response = given()
                .header("Authorization", "Bearer " + ctx.getAuthToken())
                .when()
                .get("/library/book-clubs");
        response.then().statusCode(200);
        List<Map<String, Object>> clubs = response.jsonPath().getList("$");
        assertThat(clubs).as("book clubs list should not be empty").isNotEmpty();
        ctx.setVariable("clubId", String.valueOf(((Number) clubs.get(0).get("id")).longValue()));
    }

    @Given("a digital resource of type {string} exists")
    public void aDigitalResourceOfTypeExists(String resourceType) {
        Response response = given()
                .header("Authorization", "Bearer " + ctx.getAuthToken())
                .when()
                .get("/library/digital");
        response.then().statusCode(200);
        List<Map<String, Object>> resources = response.jsonPath().getList("$");
        Map<String, Object> resource = resources.stream()
                .filter(r -> resourceType.equalsIgnoreCase(String.valueOf(r.get("resourceType"))))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "No digital resource of type: " + resourceType));
        ctx.setVariable("resourceId", String.valueOf(((Number) resource.get("id")).longValue()));
    }
}
