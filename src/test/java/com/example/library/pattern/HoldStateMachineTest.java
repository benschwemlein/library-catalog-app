package com.example.library.pattern;

import com.example.library.entity.Book;
import com.example.library.entity.Hold;
import com.example.library.entity.HoldStatus;
import com.example.library.entity.Member;
import com.example.library.entity.MembershipTier;
import com.example.library.pattern.state.CancelledHoldState;
import com.example.library.pattern.state.ExpiredHoldState;
import com.example.library.pattern.state.FulfilledHoldState;
import com.example.library.pattern.state.HoldContext;
import com.example.library.pattern.state.HoldStateMachine;
import com.example.library.pattern.state.PendingHoldState;
import com.example.library.pattern.state.ReadyHoldState;
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
class HoldStateMachineTest {

    @InjectMocks
    private HoldStateMachine holdStateMachine;

    @Mock
    private PendingHoldState pendingHoldState;

    @Mock
    private ReadyHoldState readyHoldState;

    @Mock
    private FulfilledHoldState fulfilledHoldState;

    @Mock
    private CancelledHoldState cancelledHoldState;

    @Mock
    private ExpiredHoldState expiredHoldState;

    private Hold buildHold(HoldStatus status) {
        Member member = Member.builder()
                .id(1L)
                .membershipTier(MembershipTier.STANDARD)
                .active(true)
                .expiryDate(LocalDate.now().plusYears(1))
                .fineBalance(BigDecimal.ZERO)
                .build();

        Book book = Book.builder()
                .id(10L)
                .title("Test Book")
                .isbn("978-0-000-00000-0")
                .build();

        return Hold.builder()
                .id(1L)
                .book(book)
                .member(member)
                .requestDate(LocalDateTime.now().minusDays(1))
                .status(status)
                .build();
    }

    @Test
    void createContext_pendingHold_returnsCorrectContext() {
        when(pendingHoldState.getStatus()).thenReturn(HoldStatus.PENDING);
        Hold hold = buildHold(HoldStatus.PENDING);

        HoldContext ctx = holdStateMachine.createContext(hold);

        assertThat(ctx).isNotNull();
        assertThat(ctx.getHold()).isEqualTo(hold);
        assertThat(hold.getStatus()).isEqualTo(HoldStatus.PENDING);
    }

    @Test
    void createContext_readyHold_returnsCorrectContext() {
        when(readyHoldState.getStatus()).thenReturn(HoldStatus.READY);
        Hold hold = buildHold(HoldStatus.READY);

        HoldContext ctx = holdStateMachine.createContext(hold);

        assertThat(ctx).isNotNull();
        assertThat(hold.getStatus()).isEqualTo(HoldStatus.READY);
    }

    @Test
    void createContext_unknownStatus_throwsIllegalArgumentException() {
        Hold hold = buildHold(HoldStatus.PENDING);
        hold.setStatus(null); // simulate unknown status

        assertThrows(IllegalArgumentException.class, () -> holdStateMachine.createContext(hold));
    }

    @Test
    void pendingToReady_transitionSucceeds() {
        when(pendingHoldState.getStatus()).thenReturn(HoldStatus.PENDING);
        when(readyHoldState.getStatus()).thenReturn(HoldStatus.READY);
        Hold hold = buildHold(HoldStatus.PENDING);
        HoldContext ctx = holdStateMachine.createContext(hold);

        doAnswer(inv -> {
            hold.setNotifiedDate(LocalDateTime.now());
            ctx.setState(readyHoldState);
            return null;
        }).when(pendingHoldState).activate(ctx);

        ctx.activate();

        verify(pendingHoldState).activate(ctx);
        assertThat(hold.getStatus()).isEqualTo(HoldStatus.READY);
        assertThat(hold.getNotifiedDate()).isNotNull();
    }

    @Test
    void pendingToCancelled_transitionSucceeds() {
        when(pendingHoldState.getStatus()).thenReturn(HoldStatus.PENDING);
        when(cancelledHoldState.getStatus()).thenReturn(HoldStatus.CANCELLED);
        Hold hold = buildHold(HoldStatus.PENDING);
        HoldContext ctx = holdStateMachine.createContext(hold);

        doAnswer(inv -> {
            ctx.setState(cancelledHoldState);
            return null;
        }).when(pendingHoldState).cancel(ctx);

        ctx.cancel();

        verify(pendingHoldState).cancel(ctx);
        assertThat(hold.getStatus()).isEqualTo(HoldStatus.CANCELLED);
    }

    @Test
    void readyToFulfilled_transitionSucceeds() {
        when(readyHoldState.getStatus()).thenReturn(HoldStatus.READY);
        when(fulfilledHoldState.getStatus()).thenReturn(HoldStatus.FULFILLED);
        Hold hold = buildHold(HoldStatus.READY);
        HoldContext ctx = holdStateMachine.createContext(hold);

        doAnswer(inv -> {
            ctx.setState(fulfilledHoldState);
            return null;
        }).when(readyHoldState).fulfill(ctx);

        ctx.fulfill();

        verify(readyHoldState).fulfill(ctx);
        assertThat(hold.getStatus()).isEqualTo(HoldStatus.FULFILLED);
    }

    @Test
    void readyToExpired_transitionSucceeds() {
        when(readyHoldState.getStatus()).thenReturn(HoldStatus.READY);
        when(expiredHoldState.getStatus()).thenReturn(HoldStatus.EXPIRED);
        Hold hold = buildHold(HoldStatus.READY);
        HoldContext ctx = holdStateMachine.createContext(hold);

        doAnswer(inv -> {
            ctx.setState(expiredHoldState);
            return null;
        }).when(readyHoldState).expire(ctx);

        ctx.expire();

        verify(readyHoldState).expire(ctx);
        assertThat(hold.getStatus()).isEqualTo(HoldStatus.EXPIRED);
    }

    @Test
    void readyToCancelled_transitionSucceeds() {
        when(readyHoldState.getStatus()).thenReturn(HoldStatus.READY);
        when(cancelledHoldState.getStatus()).thenReturn(HoldStatus.CANCELLED);
        Hold hold = buildHold(HoldStatus.READY);
        HoldContext ctx = holdStateMachine.createContext(hold);

        doAnswer(inv -> {
            ctx.setState(cancelledHoldState);
            return null;
        }).when(readyHoldState).cancel(ctx);

        ctx.cancel();

        verify(readyHoldState).cancel(ctx);
        assertThat(hold.getStatus()).isEqualTo(HoldStatus.CANCELLED);
    }

    @Test
    void fulfilledHold_cancel_throwsIllegalStateException() {
        when(fulfilledHoldState.getStatus()).thenReturn(HoldStatus.FULFILLED);
        Hold hold = buildHold(HoldStatus.FULFILLED);
        HoldContext ctx = holdStateMachine.createContext(hold);

        doThrow(new IllegalStateException("Hold is already fulfilled - terminal state"))
                .when(fulfilledHoldState).cancel(ctx);

        assertThrows(IllegalStateException.class, ctx::cancel);
    }

    @Test
    void cancelledHold_markReady_throwsIllegalStateException() {
        when(cancelledHoldState.getStatus()).thenReturn(HoldStatus.CANCELLED);
        Hold hold = buildHold(HoldStatus.CANCELLED);
        HoldContext ctx = holdStateMachine.createContext(hold);

        doThrow(new IllegalStateException("Hold is already cancelled - terminal state"))
                .when(cancelledHoldState).activate(ctx);

        assertThrows(IllegalStateException.class, ctx::activate);
    }

    @Test
    void expiredHold_fulfil_throwsIllegalStateException() {
        when(expiredHoldState.getStatus()).thenReturn(HoldStatus.EXPIRED);
        Hold hold = buildHold(HoldStatus.EXPIRED);
        HoldContext ctx = holdStateMachine.createContext(hold);

        doThrow(new IllegalStateException("Hold is already expired - terminal state"))
                .when(expiredHoldState).fulfill(ctx);

        assertThrows(IllegalStateException.class, ctx::fulfill);
    }

    @Test
    void pendingHold_fulfill_throwsIllegalStateException() {
        // Pending → can't fulfill directly, must activate first
        when(pendingHoldState.getStatus()).thenReturn(HoldStatus.PENDING);
        Hold hold = buildHold(HoldStatus.PENDING);
        HoldContext ctx = holdStateMachine.createContext(hold);

        doThrow(new IllegalStateException("Cannot fulfill pending hold - hold must be ready first"))
                .when(pendingHoldState).fulfill(ctx);

        assertThrows(IllegalStateException.class, ctx::fulfill);
    }

    @Test
    void pendingToReady_realStates_transitionSucceeds() {
        // Integration-style test using real state objects wired via reflection
        PendingHoldState realPending = new PendingHoldState();
        ReadyHoldState realReady = new ReadyHoldState();
        CancelledHoldState realCancelled = new CancelledHoldState();

        try {
            java.lang.reflect.Field readyField = PendingHoldState.class.getDeclaredField("readyHoldState");
            readyField.setAccessible(true);
            readyField.set(realPending, realReady);

            java.lang.reflect.Field cancelledField = PendingHoldState.class.getDeclaredField("cancelledHoldState");
            cancelledField.setAccessible(true);
            cancelledField.set(realPending, realCancelled);
        } catch (Exception e) {
            fail("Reflection failed: " + e.getMessage());
        }

        Hold hold = buildHold(HoldStatus.PENDING);
        HoldContext ctx = new HoldContext(hold, realPending);

        ctx.activate();

        assertThat(hold.getStatus()).isEqualTo(HoldStatus.READY);
        assertThat(hold.getNotifiedDate()).isNotNull();
    }

    @Test
    void fulfilledHold_isTerminalState_allOperationsThrow() {
        FulfilledHoldState realFulfilled = new FulfilledHoldState();
        Hold hold = buildHold(HoldStatus.FULFILLED);
        HoldContext ctx = new HoldContext(hold, realFulfilled);

        assertThrows(IllegalStateException.class, ctx::activate);
        assertThrows(IllegalStateException.class, ctx::fulfill);
        assertThrows(IllegalStateException.class, ctx::cancel);
        assertThrows(IllegalStateException.class, ctx::expire);
    }
}
