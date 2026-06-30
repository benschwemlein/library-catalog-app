package com.example.library.service;

import com.example.library.TestDataFactory;
import com.example.library.dto.CheckoutRequest;
import com.example.library.entity.*;
import com.example.library.exception.*;
import com.example.library.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class LoanServiceTest {

    @Mock
    private LoanRepository loanRepository;

    @Mock
    private BookCopyRepository bookCopyRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private LibraryBranchRepository libraryBranchRepository;

    @Mock
    private FineService fineService;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private LoanService loanService;

    private Member activeMember;
    private BookCopy availableCopy;
    private LibraryBranch branch;
    private CheckoutRequest checkoutRequest;

    @BeforeEach
    void setUp() {
        activeMember = TestDataFactory.createMember();
        availableCopy = TestDataFactory.createBookCopy();
        branch = TestDataFactory.createBranch();

        checkoutRequest = new CheckoutRequest();
        checkoutRequest.setMemberId(activeMember.getId());
        checkoutRequest.setCopyId(availableCopy.getId());
        checkoutRequest.setBranchId(branch.getId());
    }

    // -------------------------------------------------------------------------
    // checkout tests
    // -------------------------------------------------------------------------

    @Test
    void checkout_happyPath_createsLoan() {
        when(memberRepository.findById(activeMember.getId())).thenReturn(Optional.of(activeMember));
        when(fineService.getTotalUnpaidFines(activeMember.getId())).thenReturn(BigDecimal.ZERO);
        when(bookCopyRepository.findById(availableCopy.getId())).thenReturn(Optional.of(availableCopy));
        when(libraryBranchRepository.findById(branch.getId())).thenReturn(Optional.of(branch));

        Loan savedLoan = TestDataFactory.createLoan();
        when(loanRepository.save(any(Loan.class))).thenReturn(savedLoan);

        Loan result = loanService.checkout(checkoutRequest);

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(LoanStatus.ACTIVE);
        verify(bookCopyRepository).save(availableCopy);
        verify(loanRepository).save(any(Loan.class));
        verify(notificationService).sendNotification(
                eq(activeMember.getId()), any(), anyString(), any());
    }

    @Test
    void checkout_memberNotFound_throwsMemberNotFoundException() {
        when(memberRepository.findById(99L)).thenReturn(Optional.empty());
        checkoutRequest.setMemberId(99L);

        assertThrows(MemberNotFoundException.class, () -> loanService.checkout(checkoutRequest));
    }

    @Test
    void checkout_membershipExpired_throwsMembershipExpiredException() {
        // Member with past expiry date
        Member expiredMember = Member.builder()
                .id(5L)
                .membershipNumber("MEM-EXP-001")
                .membershipTier(MembershipTier.STANDARD)
                .joinDate(LocalDate.now().minusYears(2))
                .expiryDate(LocalDate.now().minusDays(30))
                .fineBalance(BigDecimal.ZERO)
                .loans(new ArrayList<>())
                .holds(new ArrayList<>())
                .active(true)
                .build();

        checkoutRequest.setMemberId(expiredMember.getId());
        when(memberRepository.findById(expiredMember.getId())).thenReturn(Optional.of(expiredMember));

        assertThrows(MembershipExpiredException.class, () -> loanService.checkout(checkoutRequest));
        verify(bookCopyRepository, never()).findById(anyLong());
    }

    @Test
    void checkout_memberInactive_throwsMembershipExpiredException() {
        Member inactiveMember = Member.builder()
                .id(6L)
                .membershipNumber("MEM-INACT-001")
                .membershipTier(MembershipTier.STANDARD)
                .joinDate(LocalDate.now().minusYears(1))
                .expiryDate(LocalDate.now().plusYears(1))
                .fineBalance(BigDecimal.ZERO)
                .loans(new ArrayList<>())
                .holds(new ArrayList<>())
                .active(false)
                .build();

        checkoutRequest.setMemberId(inactiveMember.getId());
        when(memberRepository.findById(inactiveMember.getId())).thenReturn(Optional.of(inactiveMember));

        assertThrows(MembershipExpiredException.class, () -> loanService.checkout(checkoutRequest));
    }

    @Test
    void checkout_unpaidFinesExceedThreshold_throwsUnpaidFinesException() {
        when(memberRepository.findById(activeMember.getId())).thenReturn(Optional.of(activeMember));
        when(fineService.getTotalUnpaidFines(activeMember.getId()))
                .thenReturn(new BigDecimal("15.00"));

        assertThrows(UnpaidFinesException.class, () -> loanService.checkout(checkoutRequest));
        verify(bookCopyRepository, never()).findById(anyLong());
    }

    @Test
    void checkout_copyNotAvailable_throwsCopyNotAvailableException() {
        BookCopy checkedOutCopy = TestDataFactory.createCheckedOutCopy();
        checkoutRequest.setCopyId(checkedOutCopy.getId());

        when(memberRepository.findById(activeMember.getId())).thenReturn(Optional.of(activeMember));
        when(fineService.getTotalUnpaidFines(activeMember.getId())).thenReturn(BigDecimal.ZERO);
        when(bookCopyRepository.findById(checkedOutCopy.getId())).thenReturn(Optional.of(checkedOutCopy));

        assertThrows(CopyNotAvailableException.class, () -> loanService.checkout(checkoutRequest));
        verify(loanRepository, never()).save(any());
    }

    @Test
    void checkout_copyNotFound_throwsCopyNotAvailableException() {
        checkoutRequest.setCopyId(999L);
        when(memberRepository.findById(activeMember.getId())).thenReturn(Optional.of(activeMember));
        when(fineService.getTotalUnpaidFines(activeMember.getId())).thenReturn(BigDecimal.ZERO);
        when(bookCopyRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(CopyNotAvailableException.class, () -> loanService.checkout(checkoutRequest));
    }

    // -------------------------------------------------------------------------
    // returnBook tests
    // -------------------------------------------------------------------------

    @Test
    void returnBook_onTime_updatesStatusAndNoFine() {
        Loan activeLoan = TestDataFactory.createLoan();
        // Due date is in the future, so no fine
        activeLoan.setDueDate(LocalDateTime.now().plusDays(7));

        when(loanRepository.findById(activeLoan.getId())).thenReturn(Optional.of(activeLoan));
        when(loanRepository.save(any(Loan.class))).thenReturn(activeLoan);

        Loan result = loanService.returnBook(activeLoan.getId());

        assertThat(result.getStatus()).isEqualTo(LoanStatus.RETURNED);
        assertThat(result.getReturnDate()).isNotNull();
        verify(fineService, never()).issueFine(any());
        verify(bookCopyRepository).save(any(BookCopy.class));
    }

    @Test
    void returnBook_overdue_triggersFineIssuance() {
        Loan overdueLoan = TestDataFactory.createLoan();
        // Due date in the past triggers fine
        overdueLoan.setDueDate(LocalDateTime.now().minusDays(5));

        when(loanRepository.findById(overdueLoan.getId())).thenReturn(Optional.of(overdueLoan));
        when(loanRepository.save(any(Loan.class))).thenReturn(overdueLoan);

        loanService.returnBook(overdueLoan.getId());

        verify(fineService).issueFine(overdueLoan);
    }

    @Test
    void returnBook_loanNotFound_throwsLoanNotFoundException() {
        when(loanRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(LoanNotFoundException.class, () -> loanService.returnBook(999L));
    }

    // -------------------------------------------------------------------------
    // renewLoan tests
    // -------------------------------------------------------------------------

    @Test
    void renewLoan_happyPath_incrementsRenewalCount() {
        Loan loan = TestDataFactory.createLoan();
        loan.setRenewalCount(0);
        LocalDateTime originalDueDate = loan.getDueDate();

        when(loanRepository.findById(loan.getId())).thenReturn(Optional.of(loan));
        when(loanRepository.save(any(Loan.class))).thenAnswer(inv -> inv.getArgument(0));

        Loan result = loanService.renewLoan(loan.getId());

        assertThat(result.getRenewalCount()).isEqualTo(1);
        assertThat(result.getDueDate()).isAfter(originalDueDate);
    }

    @Test
    void renewLoan_maxRenewalsExceeded_throwsMaxRenewalsExceededException() {
        // STANDARD tier allows 3 max renewals (from LoanService.getMaxRenewalsForTier)
        Loan loan = TestDataFactory.createLoan();
        loan.setRenewalCount(3);  // already at max for STANDARD

        when(loanRepository.findById(loan.getId())).thenReturn(Optional.of(loan));

        assertThrows(MaxRenewalsExceededException.class, () -> loanService.renewLoan(loan.getId()));
        verify(loanRepository, never()).save(any());
    }

    @Test
    void renewLoan_premiumMember_allowsMoreRenewals() {
        Member premiumMember = TestDataFactory.createPremiumMember();
        Loan loan = TestDataFactory.createLoan();
        loan.setMember(premiumMember);
        loan.setRenewalCount(4);  // PREMIUM allows 5

        when(loanRepository.findById(loan.getId())).thenReturn(Optional.of(loan));
        when(loanRepository.save(any(Loan.class))).thenAnswer(inv -> inv.getArgument(0));

        Loan result = loanService.renewLoan(loan.getId());

        assertThat(result.getRenewalCount()).isEqualTo(5);
    }

    @Test
    void renewLoan_loanNotFound_throwsLoanNotFoundException() {
        when(loanRepository.findById(404L)).thenReturn(Optional.empty());

        assertThrows(LoanNotFoundException.class, () -> loanService.renewLoan(404L));
    }

    // -------------------------------------------------------------------------
    // getOverdueLoans tests
    // -------------------------------------------------------------------------

    @Test
    void getOverdueLoans_returnsOnlyOverdueLoans() {
        Loan overdue1 = TestDataFactory.createOverdueLoan();
        Loan overdue2 = TestDataFactory.createOverdueLoan();
        overdue2.setId(10L);

        when(loanRepository.findOverdueLoans(any(LocalDateTime.class)))
                .thenReturn(List.of(overdue1, overdue2));

        List<Loan> result = loanService.getOverdueLoans();

        assertThat(result).hasSize(2);
        assertThat(result).allSatisfy(loan ->
                assertThat(loan.getDueDate()).isBefore(LocalDateTime.now()));
    }

    // -------------------------------------------------------------------------
    // getLoanHistory tests
    // -------------------------------------------------------------------------

    @Test
    void getLoanHistory_returnsAllLoansForMember() {
        Loan active = TestDataFactory.createLoan();
        Loan returned = TestDataFactory.createReturnedLoan();

        when(loanRepository.findByMember_Id(activeMember.getId()))
                .thenReturn(List.of(active, returned));

        List<Loan> result = loanService.getLoanHistory(activeMember.getId());

        assertThat(result).hasSize(2);
        assertThat(result).extracting(Loan::getStatus)
                .containsExactlyInAnyOrder(LoanStatus.ACTIVE, LoanStatus.RETURNED);
    }

    // -------------------------------------------------------------------------
    // findById tests
    // -------------------------------------------------------------------------

    @Test
    void findById_found_returnsLoan() {
        Loan loan = TestDataFactory.createLoan();
        when(loanRepository.findById(loan.getId())).thenReturn(Optional.of(loan));

        Loan result = loanService.findById(loan.getId());

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(loan.getId());
    }

    @Test
    void findById_notFound_throwsLoanNotFoundException() {
        when(loanRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> loanService.findById(999L))
                .isInstanceOf(LoanNotFoundException.class)
                .hasMessageContaining("999");
    }
}
