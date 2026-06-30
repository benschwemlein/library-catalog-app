package com.example.library.circulation;

import com.example.library.TestDataFactory;
import com.example.library.entity.*;
import com.example.library.repository.LoanRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CirculationRulesEngineTest {

    @Mock
    private CirculationRuleRepository ruleRepository;

    @Mock
    private LoanRepository loanRepository;

    @InjectMocks
    private CirculationRulesEngine engine;

    // -------------------------------------------------------------------------
    // getApplicableRule
    // -------------------------------------------------------------------------

    @Test
    void getApplicableRule_branchSpecificBeatsGlobal() {
        Member member = TestDataFactory.createMember();
        BookCopy copy = TestDataFactory.createBookCopy();
        LibraryBranch branch = TestDataFactory.createBranch();

        CirculationRule branchSpecificRule = CirculationRule.builder()
                .id(1L)
                .membershipTier(MembershipTier.STANDARD)
                .itemType(ItemType.BOOK)
                .branchId(branch.getId())
                .loanPeriodDays(14)
                .maxRenewals(1)
                .fineRatePerDay(new BigDecimal("0.50"))
                .maxFineAmount(new BigDecimal("10.00"))
                .maxLoansAllowed(5)
                .reservationHoldDays(3)
                .minAgeRequired(0)
                .active(true)
                .build();

        CirculationRule globalRule = CirculationRule.builder()
                .id(2L)
                .membershipTier(MembershipTier.STANDARD)
                .itemType(ItemType.BOOK)
                .branchId(null)
                .loanPeriodDays(21)
                .maxRenewals(2)
                .fineRatePerDay(new BigDecimal("0.25"))
                .maxFineAmount(new BigDecimal("25.00"))
                .maxLoansAllowed(8)
                .reservationHoldDays(7)
                .minAgeRequired(0)
                .active(true)
                .build();

        when(ruleRepository.findApplicableRules(
                eq(MembershipTier.STANDARD), eq(ItemType.BOOK), eq(branch.getId())))
                .thenReturn(List.of(branchSpecificRule, globalRule));

        CirculationRule result = engine.getApplicableRule(member, copy, branch);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getBranchId()).isEqualTo(branch.getId());
    }

    @Test
    void getApplicableRule_fallsBackToGlobal() {
        Member member = TestDataFactory.createMember();
        BookCopy copy = TestDataFactory.createBookCopy();
        LibraryBranch branch = TestDataFactory.createBranch();

        when(ruleRepository.findApplicableRules(any(), any(), any()))
                .thenReturn(List.of());

        CirculationRule result = engine.getApplicableRule(member, copy, branch);

        assertThat(result.getLoanPeriodDays()).isEqualTo(21);
        assertThat(result.getMaxLoansAllowed()).isEqualTo(8);
    }

    // -------------------------------------------------------------------------
    // calculateDueDate
    // -------------------------------------------------------------------------

    @Test
    void calculateDueDate_standardMember_21Days() {
        Member member = TestDataFactory.createMember();
        BookCopy copy = TestDataFactory.createBookCopy();
        LibraryBranch branch = TestDataFactory.createBranch();
        LocalDate checkoutDate = LocalDate.of(2025, 1, 1);

        CirculationRule rule = CirculationRule.builder()
                .loanPeriodDays(21)
                .maxRenewals(2)
                .fineRatePerDay(new BigDecimal("0.25"))
                .maxFineAmount(new BigDecimal("25.00"))
                .maxLoansAllowed(8)
                .reservationHoldDays(7)
                .minAgeRequired(0)
                .active(true)
                .build();

        when(ruleRepository.findApplicableRules(
                eq(MembershipTier.STANDARD), eq(ItemType.BOOK), eq(branch.getId())))
                .thenReturn(List.of(rule));

        LocalDate dueDate = engine.calculateDueDate(member, copy, branch, checkoutDate);

        assertThat(dueDate).isEqualTo(LocalDate.of(2025, 1, 22));
    }

    @Test
    void calculateDueDate_premiumMember_28Days() {
        Member member = TestDataFactory.createPremiumMember();
        BookCopy copy = TestDataFactory.createBookCopy();
        LibraryBranch branch = TestDataFactory.createBranch();
        LocalDate checkoutDate = LocalDate.of(2025, 3, 1);

        CirculationRule rule = CirculationRule.builder()
                .loanPeriodDays(28)
                .maxRenewals(3)
                .fineRatePerDay(new BigDecimal("0.25"))
                .maxFineAmount(new BigDecimal("25.00"))
                .maxLoansAllowed(12)
                .reservationHoldDays(7)
                .minAgeRequired(0)
                .active(true)
                .build();

        when(ruleRepository.findApplicableRules(
                eq(MembershipTier.PREMIUM), eq(ItemType.BOOK), eq(branch.getId())))
                .thenReturn(List.of(rule));

        LocalDate dueDate = engine.calculateDueDate(member, copy, branch, checkoutDate);

        assertThat(dueDate).isEqualTo(LocalDate.of(2025, 3, 29));
    }

    @Test
    void calculateDueDate_studentMember_14Days() {
        Member member = TestDataFactory.createStudentMember();
        BookCopy copy = TestDataFactory.createBookCopy();
        LibraryBranch branch = TestDataFactory.createBranch();
        LocalDate checkoutDate = LocalDate.of(2025, 9, 1);

        CirculationRule rule = CirculationRule.builder()
                .loanPeriodDays(14)
                .maxRenewals(1)
                .fineRatePerDay(new BigDecimal("0.10"))
                .maxFineAmount(new BigDecimal("15.00"))
                .maxLoansAllowed(5)
                .reservationHoldDays(5)
                .minAgeRequired(0)
                .active(true)
                .build();

        when(ruleRepository.findApplicableRules(
                eq(MembershipTier.STUDENT), eq(ItemType.BOOK), eq(branch.getId())))
                .thenReturn(List.of(rule));

        LocalDate dueDate = engine.calculateDueDate(member, copy, branch, checkoutDate);

        assertThat(dueDate).isEqualTo(LocalDate.of(2025, 9, 15));
    }

    // -------------------------------------------------------------------------
    // calculateFine
    // -------------------------------------------------------------------------

    @Test
    void calculateFine_standardRate_perDay() {
        Loan loan = TestDataFactory.createLoan();
        // Set due date to 5 days ago
        LocalDate dueLocalDate = LocalDate.now().minusDays(5);
        loan.setDueDate(dueLocalDate.atStartOfDay());

        CirculationRule rule = CirculationRule.builder()
                .loanPeriodDays(21)
                .maxRenewals(2)
                .fineRatePerDay(new BigDecimal("0.25"))
                .maxFineAmount(new BigDecimal("25.00"))
                .maxLoansAllowed(8)
                .reservationHoldDays(7)
                .minAgeRequired(0)
                .active(true)
                .build();

        when(ruleRepository.findApplicableRules(
                eq(MembershipTier.STANDARD), eq(ItemType.BOOK), any()))
                .thenReturn(List.of(rule));

        BigDecimal fine = engine.calculateFine(loan, LocalDate.now());

        assertThat(fine).isEqualByComparingTo(new BigDecimal("1.25"));
    }

    @Test
    void calculateFine_reachesCapAndStops() {
        Loan loan = TestDataFactory.createLoan();
        // 100 days overdue at $1.00/day = $100, but capped at $25
        LocalDate dueLocalDate = LocalDate.now().minusDays(100);
        loan.setDueDate(dueLocalDate.atStartOfDay());

        CirculationRule rule = CirculationRule.builder()
                .loanPeriodDays(21)
                .maxRenewals(2)
                .fineRatePerDay(new BigDecimal("1.00"))
                .maxFineAmount(new BigDecimal("25.00"))
                .maxLoansAllowed(8)
                .reservationHoldDays(7)
                .minAgeRequired(0)
                .active(true)
                .build();

        when(ruleRepository.findApplicableRules(
                eq(MembershipTier.STANDARD), eq(ItemType.BOOK), any()))
                .thenReturn(List.of(rule));

        BigDecimal fine = engine.calculateFine(loan, LocalDate.now());

        assertThat(fine).isEqualByComparingTo(new BigDecimal("25.00"));
    }

    @Test
    void calculateFine_studentGracePeriod_noFineOnDay1() {
        Loan loan = TestDataFactory.createLoan();
        // Return on the due date exactly — not overdue
        LocalDate dueLocalDate = LocalDate.now();
        loan.setDueDate(dueLocalDate.atStartOfDay());

        BigDecimal fine = engine.calculateFine(loan, LocalDate.now());

        assertThat(fine).isEqualByComparingTo(BigDecimal.ZERO);
    }

    // -------------------------------------------------------------------------
    // getMaxLoans
    // -------------------------------------------------------------------------

    @Test
    void getMaxLoans_perTier() {
        Member member = TestDataFactory.createMember();
        LibraryBranch branch = TestDataFactory.createBranch();

        CirculationRule rule = CirculationRule.builder()
                .loanPeriodDays(21)
                .maxRenewals(2)
                .fineRatePerDay(new BigDecimal("0.25"))
                .maxFineAmount(new BigDecimal("25.00"))
                .maxLoansAllowed(5)
                .reservationHoldDays(7)
                .minAgeRequired(0)
                .active(true)
                .build();

        when(ruleRepository.findApplicableRules(
                eq(MembershipTier.STANDARD), eq(ItemType.BOOK), eq(branch.getId())))
                .thenReturn(List.of(rule));

        int maxLoans = engine.getMaxLoans(member, branch);

        assertThat(maxLoans).isEqualTo(5);
    }

    // -------------------------------------------------------------------------
    // isEligibleToCheckout
    // -------------------------------------------------------------------------

    @Test
    void isEligibleToCheckout_allConditionsMet() {
        Member member = TestDataFactory.createMember();
        BookCopy copy = TestDataFactory.createBookCopy();
        LibraryBranch branch = TestDataFactory.createBranch();

        CirculationRule rule = CirculationRule.builder()
                .loanPeriodDays(21)
                .maxRenewals(2)
                .fineRatePerDay(new BigDecimal("0.25"))
                .maxFineAmount(new BigDecimal("25.00"))
                .maxLoansAllowed(8)
                .reservationHoldDays(7)
                .minAgeRequired(0)
                .active(true)
                .build();

        when(ruleRepository.findApplicableRules(
                eq(MembershipTier.STANDARD), eq(ItemType.BOOK), eq(branch.getId())))
                .thenReturn(List.of(rule));
        when(loanRepository.findByMember_IdAndStatus(member.getId(), LoanStatus.ACTIVE))
                .thenReturn(new ArrayList<>());

        EligibilityResult result = engine.isEligibleToCheckout(member, copy, branch);

        assertThat(result.isEligible()).isTrue();
        assertThat(result.getReasons()).isEmpty();
    }

    @Test
    void isEligibleToCheckout_membershipExpired_notEligible() {
        Member expiredMember = Member.builder()
                .id(99L)
                .user(null)
                .membershipNumber("MEM-EXP-001")
                .membershipTier(MembershipTier.STANDARD)
                .joinDate(LocalDate.now().minusYears(2))
                .expiryDate(LocalDate.now().minusDays(10))
                .fineBalance(BigDecimal.ZERO)
                .loans(new ArrayList<>())
                .holds(new ArrayList<>())
                .active(true)
                .build();

        BookCopy copy = TestDataFactory.createBookCopy();
        LibraryBranch branch = TestDataFactory.createBranch();

        CirculationRule rule = CirculationRule.builder()
                .loanPeriodDays(21)
                .maxRenewals(2)
                .fineRatePerDay(new BigDecimal("0.25"))
                .maxFineAmount(new BigDecimal("25.00"))
                .maxLoansAllowed(8)
                .reservationHoldDays(7)
                .minAgeRequired(0)
                .active(true)
                .build();

        when(ruleRepository.findApplicableRules(
                eq(MembershipTier.STANDARD), eq(ItemType.BOOK), eq(branch.getId())))
                .thenReturn(List.of(rule));
        when(loanRepository.findByMember_IdAndStatus(expiredMember.getId(), LoanStatus.ACTIVE))
                .thenReturn(new ArrayList<>());

        EligibilityResult result = engine.isEligibleToCheckout(expiredMember, copy, branch);

        assertThat(result.isEligible()).isFalse();
        assertThat(result.getReasons()).anyMatch(r -> r.toLowerCase().contains("expired"));
    }
}
