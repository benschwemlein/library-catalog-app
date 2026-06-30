package com.example.library.pattern;

import com.example.library.entity.Book;
import com.example.library.entity.BookCopy;
import com.example.library.entity.Loan;
import com.example.library.entity.Member;
import com.example.library.entity.MembershipTier;
import com.example.library.pattern.strategy.FineCalculationStrategy;
import com.example.library.pattern.strategy.OverdueFineContext;
import com.example.library.pattern.strategy.PremiumFineStrategy;
import com.example.library.pattern.strategy.ReferenceFineStrategy;
import com.example.library.pattern.strategy.StandardFineStrategy;
import com.example.library.pattern.strategy.StudentFineStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;

class OverdueFineContextTest {

    private OverdueFineContext context;
    private StandardFineStrategy standardStrategy;
    private PremiumFineStrategy premiumStrategy;
    private StudentFineStrategy studentStrategy;

    @BeforeEach
    void setUp() {
        standardStrategy = new StandardFineStrategy();
        premiumStrategy = new PremiumFineStrategy();
        studentStrategy = new StudentFineStrategy();
        context = new OverdueFineContext(standardStrategy, premiumStrategy, studentStrategy);
    }

    private Loan buildLoan(MembershipTier tier, LocalDateTime dueDate) {
        Member member = Member.builder()
                .id(1L)
                .membershipTier(tier)
                .active(true)
                .expiryDate(LocalDate.now().plusYears(1))
                .fineBalance(BigDecimal.ZERO)
                .build();

        Book book = Book.builder()
                .id(10L)
                .title("Test Book")
                .isbn("978-0-000000-00-0")
                .build();

        BookCopy bookCopy = BookCopy.builder()
                .id(100L)
                .book(book)
                .build();

        return Loan.builder()
                .id(1000L)
                .member(member)
                .bookCopy(bookCopy)
                .dueDate(dueDate)
                .checkoutDate(dueDate.minusDays(21))
                .renewalCount(0)
                .build();
    }

    @Test
    void calculateFine_standardMember_noOverdue_returnsZero() {
        LocalDateTime dueDate = LocalDateTime.now().minusDays(0);
        Loan loan = buildLoan(MembershipTier.STANDARD, dueDate);

        BigDecimal fine = context.calculateFine(loan, LocalDate.now());

        assertThat(fine).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void calculateFine_standardMember_5DaysOverdue_returns1_25() {
        LocalDateTime dueDate = LocalDateTime.now().minusDays(5);
        Loan loan = buildLoan(MembershipTier.STANDARD, dueDate);

        BigDecimal fine = context.calculateFine(loan, LocalDate.now());

        assertThat(fine).isEqualByComparingTo(new BigDecimal("1.25"));
    }

    @Test
    void calculateFine_standardMember_exceeds25_capApplied() {
        LocalDateTime dueDate = LocalDateTime.now().minusDays(200);
        Loan loan = buildLoan(MembershipTier.STANDARD, dueDate);

        BigDecimal fine = context.calculateFine(loan, LocalDate.now());

        assertThat(fine).isEqualByComparingTo(new BigDecimal("25.00"));
    }

    @Test
    void calculateFine_premiumMember_5DaysOverdue_returns0_50() {
        LocalDateTime dueDate = LocalDateTime.now().minusDays(5);
        Loan loan = buildLoan(MembershipTier.PREMIUM, dueDate);

        BigDecimal fine = context.calculateFine(loan, LocalDate.now());

        assertThat(fine).isEqualByComparingTo(new BigDecimal("0.50"));
    }

    @Test
    void calculateFine_premiumMember_exceeds10_capApplied() {
        LocalDateTime dueDate = LocalDateTime.now().minusDays(200);
        Loan loan = buildLoan(MembershipTier.PREMIUM, dueDate);

        BigDecimal fine = context.calculateFine(loan, LocalDate.now());

        assertThat(fine).isEqualByComparingTo(new BigDecimal("10.00"));
    }

    @Test
    void calculateFine_studentMember_withinGracePeriod_returnsZero() {
        // 1 day overdue - still within grace period
        LocalDateTime dueDate = LocalDateTime.now().minusDays(1);
        Loan loan = buildLoan(MembershipTier.STUDENT, dueDate);

        BigDecimal fine = context.calculateFine(loan, LocalDate.now());

        assertThat(fine).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void calculateFine_studentMember_beyondGracePeriod_calculatesCorrectly() {
        // 3 days overdue: grace period is 1 day, so 3 days from due date counts (0.15 * 3)
        LocalDateTime dueDate = LocalDateTime.now().minusDays(3);
        Loan loan = buildLoan(MembershipTier.STUDENT, dueDate);

        BigDecimal fine = context.calculateFine(loan, LocalDate.now());

        assertThat(fine).isEqualByComparingTo(new BigDecimal("0.45"));
    }

    @Test
    void calculateFine_studentMember_exceeds15_capApplied() {
        LocalDateTime dueDate = LocalDateTime.now().minusDays(200);
        Loan loan = buildLoan(MembershipTier.STUDENT, dueDate);

        BigDecimal fine = context.calculateFine(loan, LocalDate.now());

        assertThat(fine).isEqualByComparingTo(new BigDecimal("15.00"));
    }

    @Test
    void calculateFine_referenceMember_noCap_uncapped() {
        // ReferenceFineStrategy is not registered in OverdueFineContext, so test it directly
        ReferenceFineStrategy refStrategy = new ReferenceFineStrategy();
        LocalDateTime dueDate = LocalDateTime.now().minusDays(200);
        Loan loan = buildLoan(MembershipTier.STANDARD, dueDate); // tier doesn't matter here

        BigDecimal fine = refStrategy.calculateFine(loan, LocalDate.now());

        // $1.00/day * 200 days = $200.00 - no cap
        assertThat(fine).isEqualByComparingTo(new BigDecimal("200.00"));
    }

    @Test
    void calculateFine_unknownTier_throwsIllegalStateException() {
        // Override strategies map to be empty by using setStrategy to remove coverage,
        // then calculate on a tier that has no registered strategy.
        // We test with a STUDENT member but first we remove the STUDENT strategy by setting
        // a null-equivalent: we test via a strategy that covers it still.
        // Best approach: verify exception via overriding all strategies, create an anonymous strategy for another tier.
        // Since we can't truly remove a key, we verify the exception message for a tier not in the map.
        // We create a fresh context with only standard strategy, then use a PREMIUM loan.
        OverdueFineContext partialContext = new OverdueFineContext(standardStrategy, premiumStrategy, studentStrategy) {
            // This is a real context - we instead create context missing STUDENT
        };
        // Create an empty context using a workaround: map a fresh OverdueFineContext but override STUDENT to null
        // The simplest way: replace the PREMIUM strategy with standard and test on a tier that doesn't exist
        // Reflection approach for real test:
        // Actually the cleanest: since setStrategy allows overrides, create a loan with a tier that has no strategy
        // by testing the ReferenceFineStrategy path on a loan whose member.getMembershipTier() is not in the map.
        // We can add STUDENT manually and test with a freshly-constructed context that's missing PREMIUM.
        // For simplicity, verify that if an unregistered tier were present it throws:
        LocalDateTime dueDate = LocalDateTime.now().minusDays(5);
        Loan loan = buildLoan(MembershipTier.PREMIUM, dueDate);
        // Remove PREMIUM from a custom context by setting something weird
        // We cannot since setStrategy only adds. So test via reflection:
        try {
            java.lang.reflect.Field field = OverdueFineContext.class.getDeclaredField("strategies");
            field.setAccessible(true);
            @SuppressWarnings("unchecked")
            java.util.Map<MembershipTier, FineCalculationStrategy> map =
                    (java.util.Map<MembershipTier, FineCalculationStrategy>) field.get(context);
            map.remove(MembershipTier.PREMIUM);
        } catch (Exception e) {
            fail("Could not access strategies field: " + e.getMessage());
        }

        assertThrows(IllegalStateException.class, () -> context.calculateFine(loan, LocalDate.now()));
    }

    @Test
    void setStrategy_overridesExistingStrategy() {
        // Override STANDARD with a premium strategy (lower rate)
        context.setStrategy(MembershipTier.STANDARD, premiumStrategy);

        LocalDateTime dueDate = LocalDateTime.now().minusDays(5);
        Loan loan = buildLoan(MembershipTier.STANDARD, dueDate);

        BigDecimal fine = context.calculateFine(loan, LocalDate.now());

        // Now using premium rate $0.10/day * 5 = $0.50
        assertThat(fine).isEqualByComparingTo(new BigDecimal("0.50"));
    }

    @Test
    void getStrategies_returnsUnmodifiableMap() {
        Map<MembershipTier, FineCalculationStrategy> strategies = context.getStrategies();

        assertThat(strategies).hasSize(3);
        assertThat(strategies).containsKey(MembershipTier.STANDARD);
        assertThat(strategies).containsKey(MembershipTier.PREMIUM);
        assertThat(strategies).containsKey(MembershipTier.STUDENT);

        assertThrows(UnsupportedOperationException.class,
                () -> strategies.put(MembershipTier.STANDARD, premiumStrategy));
    }

    @Test
    void calculateFine_referenceFineStrategy_noOverdue_returnsZero() {
        ReferenceFineStrategy refStrategy = new ReferenceFineStrategy();
        LocalDateTime dueDate = LocalDateTime.now();
        Loan loan = buildLoan(MembershipTier.STANDARD, dueDate);

        BigDecimal fine = refStrategy.calculateFine(loan, LocalDate.now());

        assertThat(fine).isEqualByComparingTo(BigDecimal.ZERO);
    }
}
