package com.example.library.pattern.observer;

import com.example.library.entity.Member;
import com.example.library.entity.NotificationChannel;
import com.example.library.entity.NotificationType;
import com.example.library.repository.MemberRepository;
import com.example.library.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Listens for library events and dispatches member notifications in response.
 * All handlers run asynchronously to avoid blocking the publishing thread.
 */
@Component
@Slf4j
public class NotificationEventListener {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private MemberRepository memberRepository;

    /**
     * Notify a member when their hold is ready for pickup.
     */
    @Async
    @EventListener
    public void onHoldReady(HoldReadyEvent event) {
        log.info("HoldReadyEvent received: holdId={} memberId={} bookId={} correlationId={}",
                event.getHoldId(), event.getMemberId(), event.getBookId(), event.getCorrelationId());

        Optional<Member> memberOpt = memberRepository.findById(event.getMemberId());
        if (memberOpt.isEmpty()) {
            log.warn("Cannot send hold-ready notification: member {} not found", event.getMemberId());
            return;
        }

        Member member = memberOpt.get();
        String message = String.format(
                "Your hold (id=%d) is ready for pickup. The item will expire on %s. " +
                "Please collect it from branch id=%d.",
                event.getHoldId(),
                event.getExpiryDate() != null ? event.getExpiryDate().toLocalDate().toString() : "N/A",
                event.getPickupBranchId()
        );

        notificationService.sendNotification(
                member.getId(),
                NotificationType.HOLD_READY,
                message,
                NotificationChannel.IN_APP
        );

        log.debug("Hold-ready notification sent to member={} for holdId={}",
                member.getId(), event.getHoldId());
    }

    /**
     * Notify a member when their loan is overdue.
     */
    @Async
    @EventListener
    public void onOverdueNotice(OverdueNoticeEvent event) {
        log.info("OverdueNoticeEvent received: loanId={} memberId={} daysOverdue={} correlationId={}",
                event.getLoanId(), event.getMemberId(), event.getDaysOverdue(), event.getCorrelationId());

        Optional<Member> memberOpt = memberRepository.findById(event.getMemberId());
        if (memberOpt.isEmpty()) {
            log.warn("Cannot send overdue notification: member {} not found", event.getMemberId());
            return;
        }

        Member member = memberOpt.get();
        String message = String.format(
                "Your loan (id=%d) is %d day(s) overdue. Please return the item as soon as possible " +
                "to avoid additional fines. Fines accrue at your membership rate per day.",
                event.getLoanId(),
                event.getDaysOverdue()
        );

        notificationService.sendNotification(
                member.getId(),
                NotificationType.OVERDUE,
                message,
                NotificationChannel.IN_APP
        );

        log.debug("Overdue notification sent to member={} for loanId={} ({} days overdue)",
                member.getId(), event.getLoanId(), event.getDaysOverdue());
    }

    /**
     * Notify a member when a fine has been issued against their account.
     */
    @Async
    @EventListener
    public void onFineIssued(FineIssuedEvent event) {
        log.info("FineIssuedEvent received: fineId={} memberId={} amount={} correlationId={}",
                event.getFineId(), event.getMemberId(), event.getAmount(), event.getCorrelationId());

        Optional<Member> memberOpt = memberRepository.findById(event.getMemberId());
        if (memberOpt.isEmpty()) {
            log.warn("Cannot send fine notification: member {} not found", event.getMemberId());
            return;
        }

        Member member = memberOpt.get();
        String message = String.format(
                "A fine of $%s has been issued to your account (fine id=%d) for loan id=%d. Reason: %s. " +
                "Your current fine balance is $%s.",
                event.getAmount(),
                event.getFineId(),
                event.getLoanId(),
                event.getReason() != null ? event.getReason() : "overdue return",
                member.getFineBalance()
        );

        notificationService.sendNotification(
                member.getId(),
                NotificationType.FINE_ISSUED,
                message,
                NotificationChannel.IN_APP
        );

        log.debug("Fine notification sent to member={} for fineId={} amount={}",
                member.getId(), event.getFineId(), event.getAmount());
    }
}
