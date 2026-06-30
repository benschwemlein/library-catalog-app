package com.example.library.pattern.command;

import com.example.library.entity.Fine;
import com.example.library.entity.Member;
import com.example.library.repository.FineRepository;
import com.example.library.repository.MemberRepository;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Command that records a fine payment, marking the fine as paid and reducing
 * the member's outstanding fine balance.
 *
 * <p>Prototype-scoped; set {@code fineId} and {@code memberId} before calling
 * {@code execute()}. The previous balance is captured automatically during execution
 * so that {@code undo()} can restore it exactly.</p>
 */
@Component
@Scope("prototype")
@Slf4j
public class PayFineCommand implements LibraryCommand {

    @Autowired
    private FineRepository fineRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Setter private Long fineId;
    @Setter private Long memberId;

    /** Captured during execute() so undo() can restore the exact prior balance. */
    private BigDecimal previousBalance;

    @Override
    public CommandResult execute() {
        if (fineId == null || memberId == null) {
            return CommandResult.failure("Cannot execute: fineId and memberId are required");
        }
        try {
            Fine fine = fineRepository.findById(fineId)
                    .orElseThrow(() -> new IllegalArgumentException("Fine not found: " + fineId));

            if (fine.getPaidDate() != null) {
                return CommandResult.failure("Fine " + fineId + " has already been paid", fineId);
            }
            if (fine.isWaived()) {
                return CommandResult.failure("Fine " + fineId + " has been waived and cannot be paid", fineId);
            }

            Member member = memberRepository.findById(memberId)
                    .orElseThrow(() -> new IllegalArgumentException("Member not found: " + memberId));

            // Capture prior state for undo
            this.previousBalance = member.getFineBalance();

            // Record payment
            fine.setPaidDate(LocalDateTime.now());
            fineRepository.save(fine);

            BigDecimal newBalance = previousBalance.subtract(fine.getAmount());
            if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
                newBalance = BigDecimal.ZERO;
            }
            member.setFineBalance(newBalance);
            memberRepository.save(member);

            log.info("PayFineCommand executed: fineId={} memberId={} amount={} previousBalance={} newBalance={}",
                    fineId, memberId, fine.getAmount(), previousBalance, newBalance);

            return CommandResult.success(
                    String.format("Fine of $%.2f paid. New balance: $%.2f", fine.getAmount(), newBalance),
                    fineId
            );
        } catch (Exception e) {
            log.error("PayFineCommand failed: fineId={} memberId={} error={}", fineId, memberId, e.getMessage(), e);
            return CommandResult.failure("Pay fine failed: " + e.getMessage(), fineId);
        }
    }

    @Override
    public CommandResult undo() {
        if (fineId == null || previousBalance == null) {
            return CommandResult.failure("Cannot undo: no prior state recorded (was execute() called?)");
        }
        try {
            Fine fine = fineRepository.findById(fineId)
                    .orElseThrow(() -> new IllegalArgumentException("Fine not found: " + fineId));

            // Reverse the payment
            fine.setPaidDate(null);
            fineRepository.save(fine);

            Member member = memberRepository.findById(memberId)
                    .orElseThrow(() -> new IllegalArgumentException("Member not found: " + memberId));

            member.setFineBalance(previousBalance);
            memberRepository.save(member);

            log.info("PayFineCommand undone: fineId={} memberId={} balanceRestored={}",
                    fineId, memberId, previousBalance);

            return CommandResult.success(
                    "Fine payment reversed. Balance restored to: $" + previousBalance,
                    fineId
            );
        } catch (Exception e) {
            log.error("PayFineCommand undo failed: fineId={} error={}", fineId, e.getMessage(), e);
            return CommandResult.failure("Undo fine payment failed: " + e.getMessage(), fineId);
        }
    }

    @Override
    public String getDescription() {
        return String.format("PayFine fineId=%d for memberId=%d (previousBalance=%s)",
                fineId, memberId, previousBalance);
    }
}
