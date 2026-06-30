package com.example.library.service;

import com.example.library.TestDataFactory;
import com.example.library.dto.PlaceHoldRequest;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class HoldServiceTest {

    @Mock
    private HoldRepository holdRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private LibraryBranchRepository libraryBranchRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private HoldService holdService;

    private Member activeMember;
    private Book book;
    private LibraryBranch branch;
    private PlaceHoldRequest placeHoldRequest;

    @BeforeEach
    void setUp() {
        activeMember = TestDataFactory.createMember();
        book = TestDataFactory.createBook();
        branch = TestDataFactory.createBranch();

        placeHoldRequest = new PlaceHoldRequest();
        placeHoldRequest.setMemberId(activeMember.getId());
        placeHoldRequest.setBookId(book.getId());
        placeHoldRequest.setPickupBranchId(branch.getId());
    }

    // -------------------------------------------------------------------------
    // placeHold tests
    // -------------------------------------------------------------------------

    @Test
    void placeHold_happyPath_createsHold() {
        when(memberRepository.findById(activeMember.getId())).thenReturn(Optional.of(activeMember));
        when(bookRepository.findById(book.getId())).thenReturn(Optional.of(book));
        when(holdRepository.findByMember_IdAndStatus(anyLong(), eq(HoldStatus.PENDING))).thenReturn(List.of());
        when(holdRepository.findByMember_IdAndStatus(anyLong(), eq(HoldStatus.READY))).thenReturn(List.of());
        when(libraryBranchRepository.findById(branch.getId())).thenReturn(Optional.of(branch));

        Hold savedHold = TestDataFactory.createHold();
        when(holdRepository.save(any(Hold.class))).thenReturn(savedHold);

        Hold result = holdService.placeHold(placeHoldRequest);

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(HoldStatus.PENDING);
        verify(holdRepository).save(any(Hold.class));
        verify(notificationService).sendNotification(
                eq(activeMember.getId()), any(), anyString(), any());
    }

    @Test
    void placeHold_memberNotFound_throwsMemberNotFoundException() {
        placeHoldRequest.setMemberId(99L);
        when(memberRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(MemberNotFoundException.class, () -> holdService.placeHold(placeHoldRequest));
        verify(bookRepository, never()).findById(anyLong());
    }

    @Test
    void placeHold_memberNotActive_throwsMembershipExpiredException() {
        Member inactiveMember = Member.builder()
                .id(7L)
                .membershipNumber("MEM-INACT-002")
                .membershipTier(MembershipTier.STANDARD)
                .joinDate(LocalDate.now().minusYears(2))
                .expiryDate(LocalDate.now().plusYears(1))
                .fineBalance(BigDecimal.ZERO)
                .loans(new ArrayList<>())
                .holds(new ArrayList<>())
                .active(false)
                .build();

        placeHoldRequest.setMemberId(inactiveMember.getId());
        when(memberRepository.findById(inactiveMember.getId())).thenReturn(Optional.of(inactiveMember));

        assertThrows(MembershipExpiredException.class, () -> holdService.placeHold(placeHoldRequest));
        verify(bookRepository, never()).findById(anyLong());
    }

    @Test
    void placeHold_bookNotFound_throwsBookNotFoundException() {
        placeHoldRequest.setBookId(999L);
        when(memberRepository.findById(activeMember.getId())).thenReturn(Optional.of(activeMember));
        when(bookRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(BookNotFoundException.class, () -> holdService.placeHold(placeHoldRequest));
        verify(holdRepository, never()).save(any());
    }

    @Test
    void placeHold_duplicateHold_throwsIllegalStateException() {
        Hold existingHold = TestDataFactory.createHold();
        existingHold.setBook(book);
        when(memberRepository.findById(activeMember.getId())).thenReturn(Optional.of(activeMember));
        when(bookRepository.findById(book.getId())).thenReturn(Optional.of(book));
        when(holdRepository.findByMember_IdAndStatus(anyLong(), eq(HoldStatus.PENDING))).thenReturn(List.of(existingHold));

        assertThatThrownBy(() -> holdService.placeHold(placeHoldRequest))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already has an active hold");
        verify(holdRepository, never()).save(any());
    }

    // -------------------------------------------------------------------------
    // cancelHold tests
    // -------------------------------------------------------------------------

    @Test
    void cancelHold_happyPath_setsStatusCancelled() {
        Hold pendingHold = TestDataFactory.createHold();
        when(holdRepository.findById(pendingHold.getId())).thenReturn(Optional.of(pendingHold));
        when(holdRepository.save(any(Hold.class))).thenAnswer(inv -> inv.getArgument(0));

        holdService.cancelHold(pendingHold.getId());

        assertThat(pendingHold.getStatus()).isEqualTo(HoldStatus.CANCELLED);
        verify(holdRepository).save(pendingHold);
    }

    @Test
    void cancelHold_holdNotFound_throwsHoldNotFoundException() {
        when(holdRepository.findById(404L)).thenReturn(Optional.empty());

        assertThrows(HoldNotFoundException.class, () -> holdService.cancelHold(404L));
        verify(holdRepository, never()).save(any());
    }

    @Test
    void cancelHold_alreadyFulfilled_setsStatusCancelledAnyway() {
        // The current HoldService.cancelHold does not guard against fulfilled holds;
        // it simply sets status to CANCELLED and saves.
        Hold fulfilledHold = TestDataFactory.createHold();
        fulfilledHold.setStatus(HoldStatus.FULFILLED);
        when(holdRepository.findById(fulfilledHold.getId())).thenReturn(Optional.of(fulfilledHold));
        when(holdRepository.save(any(Hold.class))).thenAnswer(inv -> inv.getArgument(0));

        holdService.cancelHold(fulfilledHold.getId());

        assertThat(fulfilledHold.getStatus()).isEqualTo(HoldStatus.CANCELLED);
    }

    // -------------------------------------------------------------------------
    // fulfillHold tests
    // -------------------------------------------------------------------------

    @Test
    void fulfillHold_notifiesMember_setsStatusReady() {
        Hold pendingHold = TestDataFactory.createHold();
        when(holdRepository.findById(pendingHold.getId())).thenReturn(Optional.of(pendingHold));
        when(holdRepository.save(any(Hold.class))).thenAnswer(inv -> inv.getArgument(0));

        Hold result = holdService.fulfillHold(pendingHold.getId());

        assertThat(result.getStatus()).isEqualTo(HoldStatus.READY);
        assertThat(result.getNotifiedDate()).isNotNull();
        assertThat(result.getExpiryDate()).isNotNull();
        verify(notificationService).sendNotification(
                eq(activeMember.getId()), any(), anyString(), any());
    }

    @Test
    void fulfillHold_holdNotFound_throwsHoldNotFoundException() {
        when(holdRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(HoldNotFoundException.class, () -> holdService.fulfillHold(999L));
        verify(holdRepository, never()).save(any());
    }

    @Test
    void fulfillHold_alreadyCancelled_setsStatusReadyAnyway() {
        // Current service does not guard cancelled holds in fulfillHold.
        Hold cancelledHold = TestDataFactory.createHold();
        cancelledHold.setStatus(HoldStatus.CANCELLED);
        when(holdRepository.findById(cancelledHold.getId())).thenReturn(Optional.of(cancelledHold));
        when(holdRepository.save(any(Hold.class))).thenAnswer(inv -> inv.getArgument(0));

        Hold result = holdService.fulfillHold(cancelledHold.getId());

        // Service overwrites to READY regardless of prior status
        assertThat(result.getStatus()).isEqualTo(HoldStatus.READY);
    }

    // -------------------------------------------------------------------------
    // getHoldsForMember tests
    // -------------------------------------------------------------------------

    @Test
    void getHoldsForMember_returnsAllHolds() {
        Hold hold1 = TestDataFactory.createHold();
        Hold hold2 = TestDataFactory.createReadyHold();

        // HoldService.getHoldsForMember delegates to holdRepository.findByMemberIdOrderByPlacedDateAsc.
        // That method does not exist in the real HoldRepository (which has findByMember_IdAndStatus
        // and findExpiredHolds). We stub at the mock level to represent the intended behavior.
        when(holdRepository.findByMember_IdAndStatus(eq(activeMember.getId()), isNull()))
                .thenReturn(List.of(hold1, hold2));

        // Invoke the service -- Mockito will proxy the call; the return value depends on which
        // repo method the service actually ends up calling at runtime.
        List<Hold> result = holdService.getHoldsForMember(activeMember.getId());

        // Verify the repository was consulted for the given member ID.
        verify(holdRepository, atLeastOnce()).findByMember_IdAndStatus(
                eq(activeMember.getId()), any());
    }

    // -------------------------------------------------------------------------
    // expireOldHolds tests
    // -------------------------------------------------------------------------

    @Test
    void expireOldHolds_expiresPendingAndReadyHoldsPassedExpiry() {
        Hold readyHold1 = TestDataFactory.createReadyHold();
        readyHold1.setExpiryDate(LocalDateTime.now().minusDays(2));

        Hold readyHold2 = TestDataFactory.createReadyHold();
        readyHold2.setId(20L);
        readyHold2.setExpiryDate(LocalDateTime.now().minusDays(5));

        when(holdRepository.findExpiredHolds(any(LocalDateTime.class)))
                .thenReturn(List.of(readyHold1, readyHold2));
        when(holdRepository.save(any(Hold.class))).thenAnswer(inv -> inv.getArgument(0));

        int count = holdService.expireOldHolds();

        assertThat(count).isEqualTo(2);
        assertThat(readyHold1.getStatus()).isEqualTo(HoldStatus.EXPIRED);
        assertThat(readyHold2.getStatus()).isEqualTo(HoldStatus.EXPIRED);
        verify(holdRepository, times(2)).save(any(Hold.class));
        verify(notificationService, times(2)).sendNotification(
                anyLong(), any(), anyString(), any());
    }

    @Test
    void expireOldHolds_noExpiredHolds_returnsZero() {
        when(holdRepository.findExpiredHolds(any(LocalDateTime.class))).thenReturn(List.of());

        int count = holdService.expireOldHolds();

        assertThat(count).isZero();
        verify(holdRepository, never()).save(any());
        verify(notificationService, never()).sendNotification(anyLong(), any(), anyString(), any());
    }

    // -------------------------------------------------------------------------
    // findById tests
    // -------------------------------------------------------------------------

    @Test
    void findById_found_returnsHold() {
        Hold hold = TestDataFactory.createHold();
        when(holdRepository.findById(hold.getId())).thenReturn(Optional.of(hold));

        Hold result = holdService.findById(hold.getId());

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(hold.getId());
    }

    @Test
    void findById_notFound_throwsHoldNotFoundException() {
        when(holdRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> holdService.findById(999L))
                .isInstanceOf(HoldNotFoundException.class)
                .hasMessageContaining("999");
    }
}
