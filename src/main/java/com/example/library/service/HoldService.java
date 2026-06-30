package com.example.library.service;

import com.example.library.dto.PlaceHoldRequest;
import com.example.library.entity.*;
import com.example.library.exception.*;
import com.example.library.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class HoldService {

    private static final int HOLD_EXPIRY_DAYS = 7;

    @Autowired
    private HoldRepository holdRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private LibraryBranchRepository libraryBranchRepository;

    @Autowired
    private NotificationService notificationService;

    public Hold place(PlaceHoldRequest req) {
        return placeHold(req);
    }

    public Hold placeHold(PlaceHoldRequest req) {
        Member member = memberRepository.findById(req.getMemberId())
                .orElseThrow(() -> new MemberNotFoundException("Member not found: " + req.getMemberId()));

        if (!member.isActive()) {
            throw new MembershipExpiredException("Member account is not active.");
        }

        Book book = bookRepository.findById(req.getBookId())
                .orElseThrow(() -> new BookNotFoundException("Book not found: " + req.getBookId()));

        // Check if member already has active hold for this book
        List<Hold> existingHolds = holdRepository.findByMember_IdAndStatus(member.getId(), HoldStatus.PENDING);
        boolean alreadyHasHold = existingHolds.stream()
                .anyMatch(h -> h.getBook().getId().equals(book.getId()));
        if (!alreadyHasHold) {
            List<Hold> readyHolds = holdRepository.findByMember_IdAndStatus(member.getId(), HoldStatus.READY);
            alreadyHasHold = readyHolds.stream()
                    .anyMatch(h -> h.getBook().getId().equals(book.getId()));
        }
        if (alreadyHasHold) {
            throw new IllegalStateException("Member already has an active hold on this book.");
        }

        Hold hold = new Hold();
        hold.setMember(member);
        hold.setBook(book);
        hold.setStatus(HoldStatus.PENDING);
        hold.setRequestDate(LocalDateTime.now());

        if (req.getPickupBranchId() != null) {
            libraryBranchRepository.findById(req.getPickupBranchId())
                    .ifPresent(hold::setPickupBranch);
        }

        Hold savedHold = holdRepository.save(hold);

        notificationService.sendNotification(
                member.getId(),
                NotificationType.HOLD_READY,
                "Your hold has been placed for: " + book.getTitle(),
                NotificationChannel.EMAIL
        );

        return savedHold;
    }

    public void cancel(Long holdId) {
        cancelHold(holdId);
    }

    public void cancelHold(Long holdId) {
        Hold hold = findById(holdId);
        hold.setStatus(HoldStatus.CANCELLED);
        holdRepository.save(hold);
    }

    public Hold fulfill(Long holdId) {
        return fulfillHold(holdId);
    }

    public Hold fulfillHold(Long holdId) {
        Hold hold = findById(holdId);
        hold.setStatus(HoldStatus.READY);
        hold.setNotifiedDate(LocalDateTime.now());
        hold.setExpiryDate(LocalDateTime.now().plusDays(HOLD_EXPIRY_DAYS));
        Hold savedHold = holdRepository.save(hold);

        notificationService.sendNotification(
                hold.getMember().getId(),
                NotificationType.HOLD_READY,
                "Your hold for '" + hold.getBook().getTitle() + "' is ready for pickup. Please pick it up within " + HOLD_EXPIRY_DAYS + " days.",
                NotificationChannel.EMAIL
        );

        return savedHold;
    }

    @Transactional(readOnly = true)
    public List<Hold> getHoldsForMember(Long memberId) {
        return holdRepository.findByMember_IdAndStatus(memberId, HoldStatus.PENDING);
    }

    public int expireOldHolds() {
        List<Hold> expiredHolds = holdRepository.findExpiredHolds(LocalDateTime.now());

        for (Hold hold : expiredHolds) {
            hold.setStatus(HoldStatus.EXPIRED);
            holdRepository.save(hold);

            notificationService.sendNotification(
                    hold.getMember().getId(),
                    NotificationType.HOLD_READY,
                    "Your hold for '" + hold.getBook().getTitle() + "' has expired.",
                    NotificationChannel.EMAIL
            );
        }

        return expiredHolds.size();
    }

    @Transactional(readOnly = true)
    public Hold findById(Long id) {
        return holdRepository.findById(id)
                .orElseThrow(() -> new HoldNotFoundException("Hold not found with id: " + id));
    }
}
