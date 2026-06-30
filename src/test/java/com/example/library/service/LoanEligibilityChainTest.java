package com.example.library.service;

import com.example.library.TestDataFactory;
import com.example.library.entity.Loan;
import com.example.library.entity.LoanStatus;
import com.example.library.entity.Member;
import com.example.library.entity.BookCopy;
import com.example.library.entity.CopyStatus;
import com.example.library.entity.MembershipTier;
import com.example.library.pattern.chain.*;
import com.example.library.repository.LoanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link LoanEligibilityChain}.
 *
 * The chain is assembled via @PostConstruct in the production bean, so here we
 * instantiate each handler directly, inject the LoanRepository mock into
 * MaxLoansHandler via ReflectionTestUtils, wire up all handlers into the chain,
 * and then call chain.init() to link them in order.
 */
@ExtendWith(MockitoExtension.class)
class LoanEligibilityChainTest {

    @Mock
    private LoanRepository loanRepository;

    private MembershipActiveHandler membershipActiveHandler;
    private MaxLoansHandler maxLoansHandler;
    private UnpaidFinesHandler unpaidFinesHandler;
    private CopyAvailableHandler copyAvailableHandler;
    private BranchAccessHandler branchAccessHandler;
    private AgeRestrictionHandler ageRestrictionHandler;
    private LoanEligibilityChain chain;

    @BeforeEach
    void setUp() {
        membershipActiveHandler = new MembershipActiveHandler();

        maxLoansHandler = new MaxLoansHandler();
        ReflectionTestUtils.setField(maxLoansHandler, "loanRepository", loanRepository);

        unpaidFinesHandler = new UnpaidFinesHandler();
        copyAvailableHandler = new CopyAvailableHandler();
        branchAccessHandler = new BranchAccessHandler();
        ageRestrictionHandler = new AgeRestrictionHandler();

        chain = new LoanEligibilityChain();
        ReflectionTestUtils.setField(chain, "membershipActiveHandler", membershipActiveHandler);
        ReflectionTestUtils.setField(chain, "maxLoansHandler", maxLoansHandler);
        ReflectionTestUtils.setField(chain, "unpaidFinesHandler", unpaidFinesHandler);
        ReflectionTestUtils.setField(chain, "copyAvailableHandler", copyAvailableHandler);
        ReflectionTestUtils.setField(chain, "branchAccessHandler", branchAccessHandler);
        ReflectionTestUtils.setField(chain, "ageRestrictionHandler", ageRestrictionHandler);

        chain.init();
    }

    // ------------------------------------------------------------------
    // Helper
    // ------------------------------------------------------------------

    private CheckoutRequest request(Member member, BookCopy copy) {
        return new CheckoutRequest(member.getId(), copy.getId(), 1L);
    }

    // ------------------------------------------------------------------
    // Tests
    // ------------------------------------------------------------------

    @Test
    void membershipExpired_failsEarly() {
        Member member = TestDataFactory.createMember();
        member.setExpiryDate(LocalDate.now().minusDays(1)); // expired yesterday
        BookCopy copy = TestDataFactory.createBookCopy();

        ValidationResult result = chain.validate(request(member, copy), member, copy);

        assertThat(result.isValid()).isFalse();
        assertThat(result.getFailedHandler()).isEqualTo("MembershipActiveHandler");
        assertThat(result.getFailureReason()).contains("expired");
    }

    @Test
    void maxLoansReached_failsWithCount() {
        Member member = TestDataFactory.createMember(); // STANDARD tier, limit = 5
        BookCopy copy = TestDataFactory.createBookCopy();

        // 5 active loans puts the member at the limit
        List<Loan> activeLoans = buildLoans(5, member);
        when(loanRepository.findByMember_IdAndStatus(member.getId(), LoanStatus.ACTIVE))
                .thenReturn(activeLoans);
        when(loanRepository.findByMember_IdAndStatus(member.getId(), LoanStatus.OVERDUE))
                .thenReturn(Collections.emptyList());

        ValidationResult result = chain.validate(request(member, copy), member, copy);

        assertThat(result.isValid()).isFalse();
        assertThat(result.getFailedHandler()).isEqualTo("MaxLoansHandler");
        assertThat(result.getFailureReason()).contains("STANDARD");
    }

    @Test
    void unpaidFinesExceedThreshold_blocked() {
        Member member = TestDataFactory.createMember();
        member.setFineBalance(new BigDecimal("15.00")); // over $10 threshold
        BookCopy copy = TestDataFactory.createBookCopy();

        // No loans so MaxLoansHandler passes through
        when(loanRepository.findByMember_IdAndStatus(member.getId(), LoanStatus.ACTIVE))
                .thenReturn(Collections.emptyList());
        when(loanRepository.findByMember_IdAndStatus(member.getId(), LoanStatus.OVERDUE))
                .thenReturn(Collections.emptyList());

        ValidationResult result = chain.validate(request(member, copy), member, copy);

        assertThat(result.isValid()).isFalse();
        assertThat(result.getFailedHandler()).isEqualTo("UnpaidFinesHandler");
        assertThat(result.getFailureReason()).contains("10.00");
    }

    @Test
    void copyNotAvailable_blocked() {
        Member member = TestDataFactory.createMember();
        BookCopy copy = TestDataFactory.createCheckedOutCopy(); // status = CHECKED_OUT

        when(loanRepository.findByMember_IdAndStatus(member.getId(), LoanStatus.ACTIVE))
                .thenReturn(Collections.emptyList());
        when(loanRepository.findByMember_IdAndStatus(member.getId(), LoanStatus.OVERDUE))
                .thenReturn(Collections.emptyList());

        ValidationResult result = chain.validate(request(member, copy), member, copy);

        assertThat(result.isValid()).isFalse();
        assertThat(result.getFailedHandler()).isEqualTo("CopyAvailableHandler");
        assertThat(result.getFailureReason()).contains("not available");
    }

    @Test
    void allHandlersPass_eligible() {
        Member member = TestDataFactory.createMember();
        BookCopy copy = TestDataFactory.createBookCopy();

        when(loanRepository.findByMember_IdAndStatus(member.getId(), LoanStatus.ACTIVE))
                .thenReturn(Collections.emptyList());
        when(loanRepository.findByMember_IdAndStatus(member.getId(), LoanStatus.OVERDUE))
                .thenReturn(Collections.emptyList());

        ValidationResult result = chain.validate(request(member, copy), member, copy);

        assertThat(result.isValid()).isTrue();
        assertThat(result.getFailureReason()).isNull();
        assertThat(result.getFailedHandler()).isNull();
    }

    @Test
    void premiumMember_higherLoanLimit() {
        // PREMIUM limit = 8; a member with 7 loans should still be allowed
        Member member = TestDataFactory.createPremiumMember();
        BookCopy copy = TestDataFactory.createBookCopy();

        List<Loan> activeLoans = buildLoans(7, member);
        when(loanRepository.findByMember_IdAndStatus(member.getId(), LoanStatus.ACTIVE))
                .thenReturn(activeLoans);
        when(loanRepository.findByMember_IdAndStatus(member.getId(), LoanStatus.OVERDUE))
                .thenReturn(Collections.emptyList());

        ValidationResult result = chain.validate(request(member, copy), member, copy);

        assertThat(result.isValid()).isTrue();
    }

    @Test
    void studentMember_lowerLoanLimit() {
        // STUDENT limit = 3; a member already at 3 loans should be blocked
        Member member = TestDataFactory.createStudentMember();
        BookCopy copy = TestDataFactory.createBookCopy();

        List<Loan> activeLoans = buildLoans(3, member);
        when(loanRepository.findByMember_IdAndStatus(member.getId(), LoanStatus.ACTIVE))
                .thenReturn(activeLoans);
        when(loanRepository.findByMember_IdAndStatus(member.getId(), LoanStatus.OVERDUE))
                .thenReturn(Collections.emptyList());

        ValidationResult result = chain.validate(request(member, copy), member, copy);

        assertThat(result.isValid()).isFalse();
        assertThat(result.getFailedHandler()).isEqualTo("MaxLoansHandler");
        assertThat(result.getFailureReason()).contains("STUDENT");
    }

    // ------------------------------------------------------------------
    // Private helpers
    // ------------------------------------------------------------------

    private List<Loan> buildLoans(int count, Member member) {
        List<Loan> loans = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Loan loan = Loan.builder()
                    .id((long) (i + 100))
                    .member(member)
                    .bookCopy(TestDataFactory.createBookCopy())
                    .branch(TestDataFactory.createBranch())
                    .checkoutDate(java.time.LocalDateTime.now().minusDays(7))
                    .dueDate(java.time.LocalDateTime.now().plusDays(14))
                    .status(LoanStatus.ACTIVE)
                    .build();
            loans.add(loan);
        }
        return loans;
    }
}
