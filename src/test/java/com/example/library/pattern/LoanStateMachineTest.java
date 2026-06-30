package com.example.library.pattern;

import com.example.library.entity.Book;
import com.example.library.entity.BookCopy;
import com.example.library.entity.Loan;
import com.example.library.entity.LoanStatus;
import com.example.library.entity.Member;
import com.example.library.entity.MembershipTier;
import com.example.library.pattern.state.ActiveLoanState;
import com.example.library.pattern.state.LoanContext;
import com.example.library.pattern.state.LoanStateMachine;
import com.example.library.pattern.state.LostLoanState;
import com.example.library.pattern.state.OverdueLoanState;
import com.example.library.pattern.state.ReturnedLoanState;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class LoanStateMachineTest {

    @InjectMocks
    private LoanStateMachine loanStateMachine;

    @Mock
    private ActiveLoanState activeLoanState;

    @Mock
    private OverdueLoanState overdueLoanState;

    @Mock
    private ReturnedLoanState returnedLoanState;

    @Mock
    private LostLoanState lostLoanState;

    private Loan buildLoan(LoanStatus status) {
        Member member = Member.builder()
                .id(1L)
                .membershipTier(MembershipTier.STANDARD)
                .active(true)
                .expiryDate(LocalDate.now().plusYears(1))
                .fineBalance(BigDecimal.ZERO)
                .build();

        Book book = Book.builder().id(10L).title("Test Book").isbn("978-0-000-00000-0").build();
        BookCopy bookCopy = BookCopy.builder().id(100L).book(book).build();

        return Loan.builder()
                .id(1L)
                .member(member)
                .bookCopy(bookCopy)
                .dueDate(LocalDateTime.now().plusDays(7))
                .checkoutDate(LocalDateTime.now().minusDays(7))
                .renewalCount(0)
                .status(status)
                .build();
    }

    @Test
    void createContext_activeLoan_returnsActiveContext() {
        when(activeLoanState.getStatus()).thenReturn(LoanStatus.ACTIVE);
        Loan loan = buildLoan(LoanStatus.ACTIVE);

        LoanContext ctx = loanStateMachine.createContext(loan);

        assertThat(ctx).isNotNull();
        assertThat(ctx.getLoan()).isEqualTo(loan);
        assertThat(loan.getStatus()).isEqualTo(LoanStatus.ACTIVE);
        verify(activeLoanState).getStatus();
    }

    @Test
    void createContext_overdueLoan_returnsOverdueContext() {
        when(overdueLoanState.getStatus()).thenReturn(LoanStatus.OVERDUE);
        Loan loan = buildLoan(LoanStatus.OVERDUE);

        LoanContext ctx = loanStateMachine.createContext(loan);

        assertThat(ctx).isNotNull();
        assertThat(loan.getStatus()).isEqualTo(LoanStatus.OVERDUE);
    }

    @Test
    void createContext_returnedLoan_returnsReturnedContext() {
        when(returnedLoanState.getStatus()).thenReturn(LoanStatus.RETURNED);
        Loan loan = buildLoan(LoanStatus.RETURNED);

        LoanContext ctx = loanStateMachine.createContext(loan);

        assertThat(ctx).isNotNull();
        assertThat(loan.getStatus()).isEqualTo(LoanStatus.RETURNED);
    }

    @Test
    void createContext_lostLoan_returnsLostContext() {
        when(lostLoanState.getStatus()).thenReturn(LoanStatus.LOST);
        Loan loan = buildLoan(LoanStatus.LOST);

        LoanContext ctx = loanStateMachine.createContext(loan);

        assertThat(ctx).isNotNull();
        assertThat(loan.getStatus()).isEqualTo(LoanStatus.LOST);
    }

    @Test
    void createContext_unknownStatus_throwsIllegalArgumentException() {
        Loan loan = buildLoan(LoanStatus.ACTIVE);
        loan.setStatus(null); // simulate unknown/null status

        assertThrows(IllegalArgumentException.class, () -> loanStateMachine.createContext(loan));
    }

    @Test
    void activeToOverdue_transitionSucceeds() {
        when(activeLoanState.getStatus()).thenReturn(LoanStatus.ACTIVE);
        when(overdueLoanState.getStatus()).thenReturn(LoanStatus.OVERDUE);
        Loan loan = buildLoan(LoanStatus.ACTIVE);
        LoanContext ctx = loanStateMachine.createContext(loan);

        // Simulate markOverdue transitioning state
        doAnswer(inv -> {
            ctx.setState(overdueLoanState);
            return null;
        }).when(activeLoanState).markOverdue(ctx);

        ctx.markOverdue();

        verify(activeLoanState).markOverdue(ctx);
        assertThat(loan.getStatus()).isEqualTo(LoanStatus.OVERDUE);
    }

    @Test
    void activeToReturned_transitionSucceeds() {
        when(activeLoanState.getStatus()).thenReturn(LoanStatus.ACTIVE);
        when(returnedLoanState.getStatus()).thenReturn(LoanStatus.RETURNED);
        Loan loan = buildLoan(LoanStatus.ACTIVE);
        LoanContext ctx = loanStateMachine.createContext(loan);

        doAnswer(inv -> {
            loan.setReturnDate(LocalDateTime.now());
            ctx.setState(returnedLoanState);
            return null;
        }).when(activeLoanState).returnBook(ctx);

        ctx.returnBook();

        verify(activeLoanState).returnBook(ctx);
        assertThat(loan.getStatus()).isEqualTo(LoanStatus.RETURNED);
        assertThat(loan.getReturnDate()).isNotNull();
    }

    @Test
    void overdueToReturned_transitionSucceeds() {
        when(overdueLoanState.getStatus()).thenReturn(LoanStatus.OVERDUE);
        when(returnedLoanState.getStatus()).thenReturn(LoanStatus.RETURNED);
        Loan loan = buildLoan(LoanStatus.OVERDUE);
        LoanContext ctx = loanStateMachine.createContext(loan);

        doAnswer(inv -> {
            loan.setReturnDate(LocalDateTime.now());
            ctx.setState(returnedLoanState);
            return null;
        }).when(overdueLoanState).returnBook(ctx);

        ctx.returnBook();

        verify(overdueLoanState).returnBook(ctx);
        assertThat(loan.getStatus()).isEqualTo(LoanStatus.RETURNED);
    }

    @Test
    void lostToReturned_transitionSucceeds() {
        when(lostLoanState.getStatus()).thenReturn(LoanStatus.LOST);
        when(returnedLoanState.getStatus()).thenReturn(LoanStatus.RETURNED);
        Loan loan = buildLoan(LoanStatus.LOST);
        LoanContext ctx = loanStateMachine.createContext(loan);

        doAnswer(inv -> {
            loan.setReturnDate(LocalDateTime.now());
            ctx.setState(returnedLoanState);
            return null;
        }).when(lostLoanState).returnBook(ctx);

        ctx.returnBook();

        verify(lostLoanState).returnBook(ctx);
        assertThat(loan.getStatus()).isEqualTo(LoanStatus.RETURNED);
    }

    @Test
    void returnedLoan_markOverdue_throwsIllegalStateException() {
        when(returnedLoanState.getStatus()).thenReturn(LoanStatus.RETURNED);
        Loan loan = buildLoan(LoanStatus.RETURNED);
        LoanContext ctx = loanStateMachine.createContext(loan);

        doThrow(new IllegalStateException("Loan is already returned - terminal state"))
                .when(returnedLoanState).markOverdue(ctx);

        assertThrows(IllegalStateException.class, ctx::markOverdue);
    }

    @Test
    void returnedLoan_markLost_throwsIllegalStateException() {
        when(returnedLoanState.getStatus()).thenReturn(LoanStatus.RETURNED);
        Loan loan = buildLoan(LoanStatus.RETURNED);
        LoanContext ctx = loanStateMachine.createContext(loan);

        doThrow(new IllegalStateException("Loan is already returned - terminal state"))
                .when(returnedLoanState).markLost(ctx);

        assertThrows(IllegalStateException.class, ctx::markLost);
    }

    @Test
    void activeLoan_returnBook_setsStatusReturned() {
        // Use real states to verify end-to-end behavior
        ActiveLoanState realActive = new ActiveLoanState();
        ReturnedLoanState realReturned = new ReturnedLoanState();

        // Wire them via reflection to avoid Spring context
        try {
            java.lang.reflect.Field f = ActiveLoanState.class.getDeclaredField("returnedLoanState");
            f.setAccessible(true);
            f.set(realActive, realReturned);
        } catch (Exception e) {
            fail("Reflection failed: " + e.getMessage());
        }

        Loan loan = buildLoan(LoanStatus.ACTIVE);
        LoanContext ctx = new LoanContext(loan, realActive);

        ctx.returnBook();

        assertThat(loan.getStatus()).isEqualTo(LoanStatus.RETURNED);
        assertThat(loan.getReturnDate()).isNotNull();
    }

    @Test
    void overdueLoan_returnBook_setsStatusReturned() {
        OverdueLoanState realOverdue = new OverdueLoanState();
        ReturnedLoanState realReturned = new ReturnedLoanState();

        try {
            java.lang.reflect.Field f = OverdueLoanState.class.getDeclaredField("returnedLoanState");
            f.setAccessible(true);
            f.set(realOverdue, realReturned);
        } catch (Exception e) {
            fail("Reflection failed: " + e.getMessage());
        }

        Loan loan = buildLoan(LoanStatus.OVERDUE);
        LoanContext ctx = new LoanContext(loan, realOverdue);

        ctx.returnBook();

        assertThat(loan.getStatus()).isEqualTo(LoanStatus.RETURNED);
        assertThat(loan.getReturnDate()).isNotNull();
    }

    @Test
    void activeLoan_markLost_throwsIllegalStateException() {
        when(activeLoanState.getStatus()).thenReturn(LoanStatus.ACTIVE);
        Loan loan = buildLoan(LoanStatus.ACTIVE);
        LoanContext ctx = loanStateMachine.createContext(loan);

        doThrow(new IllegalStateException("Loan must be overdue before marking as lost"))
                .when(activeLoanState).markLost(ctx);

        assertThrows(IllegalStateException.class, ctx::markLost);
    }
}
