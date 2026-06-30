package com.example.library.service;

import com.example.library.TestDataFactory;
import com.example.library.entity.Loan;
import com.example.library.entity.LoanStatus;
import com.example.library.entity.NotificationChannel;
import com.example.library.entity.NotificationType;
import com.example.library.repository.LoanRepository;
import com.example.library.scheduler.OverdueLoanScheduler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link OverdueLoanScheduler}.
 *
 * Fields are injected via ReflectionTestUtils so the test does not depend
 * on a Spring application context.
 */
@ExtendWith(MockitoExtension.class)
class OverdueLoanSchedulerTest {

    @Mock
    private LoanRepository loanRepository;

    @Mock
    private FineService fineService;

    @Mock
    private NotificationService notificationService;

    private OverdueLoanScheduler scheduler;

    @BeforeEach
    void setUp() {
        scheduler = new OverdueLoanScheduler();
        ReflectionTestUtils.setField(scheduler, "loanRepository", loanRepository);
        ReflectionTestUtils.setField(scheduler, "fineService", fineService);
        ReflectionTestUtils.setField(scheduler, "notificationService", notificationService);
    }

    // ------------------------------------------------------------------
    // processOverdueLoans
    // ------------------------------------------------------------------

    @Test
    void processOverdueLoans_issuesFinesForEach() {
        Loan loan1 = TestDataFactory.createOverdueLoan();
        Loan loan2 = buildOverdueLoan(20L);

        when(loanRepository.findOverdueLoans(any(LocalDateTime.class)))
                .thenReturn(List.of(loan1, loan2));
        when(fineService.issueFine(loan1)).thenReturn(TestDataFactory.createFine());
        when(fineService.issueFine(loan2)).thenReturn(TestDataFactory.createFine());
        lenient().when(notificationService.sendNotification(
                anyLong(), eq(NotificationType.OVERDUE), anyString(), any(NotificationChannel.class)))
                .thenReturn(null);

        scheduler.processOverdueLoans();

        verify(fineService).issueFine(loan1);
        verify(fineService).issueFine(loan2);
        verify(notificationService, times(2)).sendNotification(
                anyLong(), eq(NotificationType.OVERDUE), anyString(), any(NotificationChannel.class));
    }

    @Test
    void processOverdueLoans_skipsAlreadyFined() {
        // issueFine returns null when fine amount <= 0 (already fined or no overdue)
        Loan loan = TestDataFactory.createOverdueLoan();

        when(loanRepository.findOverdueLoans(any(LocalDateTime.class)))
                .thenReturn(List.of(loan));
        when(fineService.issueFine(loan)).thenReturn(null); // null means no fine was created

        lenient().when(notificationService.sendNotification(
                anyLong(), eq(NotificationType.OVERDUE), anyString(), any(NotificationChannel.class)))
                .thenReturn(null);

        scheduler.processOverdueLoans();

        // issueFine was called but returned null — no exception should occur
        verify(fineService).issueFine(loan);
        verify(notificationService).sendNotification(
                anyLong(), eq(NotificationType.OVERDUE), anyString(), any(NotificationChannel.class));
    }

    // ------------------------------------------------------------------
    // sendDueSoonReminders
    // ------------------------------------------------------------------

    @Test
    void sendDueSoonReminders_notifiesMembersWithin2Days() {
        Loan loan1 = TestDataFactory.createLoan();
        Loan loan2 = buildDueSoonLoan(30L);

        when(loanRepository.findByDueDateBeforeAndStatus(any(LocalDateTime.class), eq(LoanStatus.ACTIVE)))
                .thenReturn(List.of(loan1, loan2));
        lenient().when(notificationService.sendNotification(
                anyLong(), eq(NotificationType.DUE_SOON), anyString(), any(NotificationChannel.class)))
                .thenReturn(null);

        scheduler.sendDueSoonReminders();

        verify(notificationService, times(2)).sendNotification(
                anyLong(), eq(NotificationType.DUE_SOON), anyString(), any(NotificationChannel.class));
    }

    @Test
    void sendDueSoonReminders_skipsAlreadyReturned() {
        Loan returnedLoan = TestDataFactory.createReturnedLoan();
        // returnDate is already set by TestDataFactory — scheduler skips these

        when(loanRepository.findByDueDateBeforeAndStatus(any(LocalDateTime.class), eq(LoanStatus.ACTIVE)))
                .thenReturn(List.of(returnedLoan));

        scheduler.sendDueSoonReminders();

        verify(notificationService, never()).sendNotification(
                anyLong(), any(), anyString(), any());
    }

    // ------------------------------------------------------------------
    // Private helpers
    // ------------------------------------------------------------------

    private Loan buildOverdueLoan(Long id) {
        Loan base = TestDataFactory.createOverdueLoan();
        return Loan.builder()
                .id(id)
                .bookCopy(base.getBookCopy())
                .member(base.getMember())
                .branch(base.getBranch())
                .checkoutDate(base.getCheckoutDate())
                .dueDate(base.getDueDate())
                .returnDate(null)
                .renewalCount(0)
                .status(base.getStatus())
                .build();
    }

    private Loan buildDueSoonLoan(Long id) {
        Loan base = TestDataFactory.createLoan();
        return Loan.builder()
                .id(id)
                .bookCopy(base.getBookCopy())
                .member(base.getMember())
                .branch(base.getBranch())
                .checkoutDate(LocalDateTime.now().minusDays(5))
                .dueDate(LocalDateTime.now().plusDays(1))
                .returnDate(null)
                .renewalCount(0)
                .status(LoanStatus.ACTIVE)
                .build();
    }
}
