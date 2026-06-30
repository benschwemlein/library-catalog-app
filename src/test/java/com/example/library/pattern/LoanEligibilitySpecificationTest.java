package com.example.library.pattern;

import com.example.library.entity.Loan;
import com.example.library.entity.LoanStatus;
import com.example.library.entity.Member;
import com.example.library.entity.MembershipTier;
import com.example.library.pattern.specification.AndSpecification;
import com.example.library.pattern.specification.HasNoPendingFinesSpecification;
import com.example.library.pattern.specification.IsActiveMemberSpecification;
import com.example.library.pattern.specification.IsEligibleToBorrowSpecification;
import com.example.library.pattern.specification.NotSpecification;
import com.example.library.pattern.specification.OrSpecification;
import com.example.library.pattern.specification.Specification;
import com.example.library.repository.LoanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoanEligibilitySpecificationTest {

    private IsActiveMemberSpecification isActiveMemberSpec;
    private HasNoPendingFinesSpecification hasNoPendingFinesSpec;

    @InjectMocks
    private IsEligibleToBorrowSpecification isEligibleToBorrowSpec;

    @Mock
    private LoanRepository loanRepository;

    @BeforeEach
    void setUp() {
        isActiveMemberSpec = new IsActiveMemberSpecification();
        hasNoPendingFinesSpec = new HasNoPendingFinesSpecification();
    }

    private Member activeMemberWithBalance(BigDecimal balance) {
        return Member.builder()
                .id(1L)
                .membershipTier(MembershipTier.STANDARD)
                .active(true)
                .expiryDate(LocalDate.now().plusYears(1))
                .fineBalance(balance)
                .build();
    }

    private Member inactiveMember() {
        return Member.builder()
                .id(2L)
                .membershipTier(MembershipTier.STANDARD)
                .active(false)
                .expiryDate(LocalDate.now().plusYears(1))
                .fineBalance(BigDecimal.ZERO)
                .build();
    }

    private Member expiredMember() {
        return Member.builder()
                .id(3L)
                .membershipTier(MembershipTier.STANDARD)
                .active(true)
                .expiryDate(LocalDate.now().minusDays(1))
                .fineBalance(BigDecimal.ZERO)
                .build();
    }

    @Test
    void isActiveMember_activeMember_returnsTrue() {
        Member member = activeMemberWithBalance(BigDecimal.ZERO);

        assertThat(isActiveMemberSpec.isSatisfiedBy(member)).isTrue();
    }

    @Test
    void isActiveMember_inactiveMember_returnsFalse() {
        Member member = inactiveMember();

        assertThat(isActiveMemberSpec.isSatisfiedBy(member)).isFalse();
    }

    @Test
    void isActiveMember_expiredMembership_returnsFalse() {
        Member member = expiredMember();

        assertThat(isActiveMemberSpec.isSatisfiedBy(member)).isFalse();
    }

    @Test
    void hasNoPendingFines_zeroBalance_returnsTrue() {
        Member member = activeMemberWithBalance(BigDecimal.ZERO);

        assertThat(hasNoPendingFinesSpec.isSatisfiedBy(member)).isTrue();
    }

    @Test
    void hasNoPendingFines_nonZeroBalance_returnsFalse() {
        Member member = activeMemberWithBalance(new BigDecimal("5.00"));

        assertThat(hasNoPendingFinesSpec.isSatisfiedBy(member)).isFalse();
    }

    @Test
    void hasNoPendingFines_nullBalance_returnsFalse() {
        Member member = Member.builder()
                .id(4L)
                .membershipTier(MembershipTier.STANDARD)
                .active(true)
                .expiryDate(LocalDate.now().plusYears(1))
                .fineBalance(null)
                .build();

        assertThat(hasNoPendingFinesSpec.isSatisfiedBy(member)).isFalse();
    }

    @Test
    void andSpecification_bothTrue_returnsTrue() {
        Member member = activeMemberWithBalance(BigDecimal.ZERO);
        AndSpecification<Member> andSpec = new AndSpecification<>(isActiveMemberSpec, hasNoPendingFinesSpec);

        assertThat(andSpec.isSatisfiedBy(member)).isTrue();
    }

    @Test
    void andSpecification_oneFalse_returnsFalse() {
        Member member = activeMemberWithBalance(new BigDecimal("5.00"));
        // isActive = true, hasNoPendingFines = false
        AndSpecification<Member> andSpec = new AndSpecification<>(isActiveMemberSpec, hasNoPendingFinesSpec);

        assertThat(andSpec.isSatisfiedBy(member)).isFalse();
    }

    @Test
    void andSpecification_bothFalse_returnsFalse() {
        Member member = inactiveMember();
        member.setFineBalance(new BigDecimal("10.00"));
        AndSpecification<Member> andSpec = new AndSpecification<>(isActiveMemberSpec, hasNoPendingFinesSpec);

        assertThat(andSpec.isSatisfiedBy(member)).isFalse();
    }

    @Test
    void orSpecification_oneTrue_returnsTrue() {
        Member member = activeMemberWithBalance(new BigDecimal("5.00"));
        // isActive = true, hasNoPendingFines = false; OR → true
        OrSpecification<Member> orSpec = new OrSpecification<>(isActiveMemberSpec, hasNoPendingFinesSpec);

        assertThat(orSpec.isSatisfiedBy(member)).isTrue();
    }

    @Test
    void orSpecification_bothFalse_returnsFalse() {
        // inactive + has fines → both false
        Member member = Member.builder()
                .id(5L)
                .membershipTier(MembershipTier.STANDARD)
                .active(false)
                .expiryDate(LocalDate.now().plusYears(1))
                .fineBalance(new BigDecimal("5.00"))
                .build();
        OrSpecification<Member> orSpec = new OrSpecification<>(isActiveMemberSpec, hasNoPendingFinesSpec);

        assertThat(orSpec.isSatisfiedBy(member)).isFalse();
    }

    @Test
    void notSpecification_negatesTrue_returnsFalse() {
        Member member = activeMemberWithBalance(BigDecimal.ZERO);
        NotSpecification<Member> notSpec = new NotSpecification<>(isActiveMemberSpec);

        assertThat(notSpec.isSatisfiedBy(member)).isFalse();
    }

    @Test
    void notSpecification_negatesFalse_returnsTrue() {
        Member member = inactiveMember();
        NotSpecification<Member> notSpec = new NotSpecification<>(isActiveMemberSpec);

        assertThat(notSpec.isSatisfiedBy(member)).isTrue();
    }

    @Test
    void isEligibleToBorrow_activeMemberNoFinesUnderLimit_returnsTrue() {
        Member member = activeMemberWithBalance(BigDecimal.ZERO);
        when(loanRepository.findByMember_IdAndStatus(member.getId(), LoanStatus.ACTIVE))
                .thenReturn(Collections.emptyList());
        when(loanRepository.findByMember_IdAndStatus(member.getId(), LoanStatus.OVERDUE))
                .thenReturn(Collections.emptyList());

        assertThat(isEligibleToBorrowSpec.isSatisfiedBy(member)).isTrue();
    }

    @Test
    void isEligibleToBorrow_activeMemberHasFines_returnsFalse() {
        // Fine balance over $10 threshold
        Member member = activeMemberWithBalance(new BigDecimal("15.00"));

        // No need to mock repo since fine check short-circuits
        assertThat(isEligibleToBorrowSpec.isSatisfiedBy(member)).isFalse();
    }

    @Test
    void isEligibleToBorrow_inactiveMember_returnsFalse() {
        Member member = inactiveMember();

        assertThat(isEligibleToBorrowSpec.isSatisfiedBy(member)).isFalse();
    }

    @Test
    void isEligibleToBorrow_standardMemberAtLoanLimit_returnsFalse() {
        Member member = activeMemberWithBalance(BigDecimal.ZERO);
        // STANDARD limit is 5; return 5 active loans
        List<Loan> activeLoans = List.of(
                Loan.builder().id(1L).build(),
                Loan.builder().id(2L).build(),
                Loan.builder().id(3L).build(),
                Loan.builder().id(4L).build(),
                Loan.builder().id(5L).build()
        );
        when(loanRepository.findByMember_IdAndStatus(member.getId(), LoanStatus.ACTIVE))
                .thenReturn(activeLoans);
        when(loanRepository.findByMember_IdAndStatus(member.getId(), LoanStatus.OVERDUE))
                .thenReturn(Collections.emptyList());

        assertThat(isEligibleToBorrowSpec.isSatisfiedBy(member)).isFalse();
    }
}
