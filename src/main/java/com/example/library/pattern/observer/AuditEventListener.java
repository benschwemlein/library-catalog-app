package com.example.library.pattern.observer;

import com.example.library.entity.AuditLog;
import com.example.library.repository.AuditLogRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Persists an {@link AuditLog} record for every {@link LibraryEvent} published in the application.
 * Runs asynchronously so it does not block the publishing thread.
 */
@Component
@Slf4j
public class AuditEventListener {

    @Autowired
    private AuditLogRepository auditLogRepository;

    /**
     * Catch-all listener that creates an audit entry for any LibraryEvent subclass.
     */
    @Async
    @EventListener
    public void onLibraryEvent(LibraryEvent event) {
        try {
            String eventName = event.getEventName();
            log.debug("Auditing event: {} correlationId={}", eventName, event.getCorrelationId());

            String description = buildEventDescription(event);

            AuditLog auditLog = AuditLog.builder()
                    .action(eventName)
                    .entityType(resolveEntityType(event))
                    .entityId(resolveEntityId(event))
                    .timestamp(LocalDateTime.now())
                    .newValue(description)
                    .build();

            auditLogRepository.save(auditLog);

            log.debug("Audit log saved for event={} correlationId={}", eventName, event.getCorrelationId());
        } catch (Exception e) {
            log.error("Failed to audit event {}: {}", event.getEventName(), e.getMessage(), e);
        }
    }

    private String resolveEntityType(LibraryEvent event) {
        if (event instanceof BookCheckedOutEvent || event instanceof BookReturnedEvent || event instanceof BookAddedEvent) {
            return "Book";
        }
        if (event instanceof HoldReadyEvent) {
            return "Hold";
        }
        if (event instanceof FineIssuedEvent) {
            return "Fine";
        }
        if (event instanceof MemberRegisteredEvent || event instanceof MembershipExpiredEvent) {
            return "Member";
        }
        if (event instanceof OverdueNoticeEvent) {
            return "Loan";
        }
        return event.getClass().getSimpleName();
    }

    private Long resolveEntityId(LibraryEvent event) {
        if (event instanceof BookCheckedOutEvent e) {
            return e.getLoanId();
        }
        if (event instanceof BookReturnedEvent e) {
            return e.getLoanId();
        }
        if (event instanceof HoldReadyEvent e) {
            return e.getHoldId();
        }
        if (event instanceof FineIssuedEvent e) {
            return e.getFineId();
        }
        if (event instanceof MemberRegisteredEvent e) {
            return e.getMemberId();
        }
        if (event instanceof MembershipExpiredEvent e) {
            return e.getMemberId();
        }
        if (event instanceof BookAddedEvent e) {
            return e.getBookId();
        }
        if (event instanceof OverdueNoticeEvent e) {
            return e.getLoanId();
        }
        return null;
    }

    private String buildEventDescription(LibraryEvent event) {
        StringBuilder sb = new StringBuilder();
        sb.append("correlationId=").append(event.getCorrelationId());
        sb.append(" timestamp=").append(event.getTimestamp());

        if (event instanceof BookCheckedOutEvent e) {
            sb.append(" loanId=").append(e.getLoanId())
              .append(" memberId=").append(e.getMemberId())
              .append(" bookId=").append(e.getBookId())
              .append(" dueDate=").append(e.getDueDate());
        } else if (event instanceof BookReturnedEvent e) {
            sb.append(" loanId=").append(e.getLoanId())
              .append(" memberId=").append(e.getMemberId())
              .append(" wasOverdue=").append(e.isWasOverdue())
              .append(" fineIssued=").append(e.isFineIssued())
              .append(" fineAmount=").append(e.getFineAmount());
        } else if (event instanceof HoldReadyEvent e) {
            sb.append(" holdId=").append(e.getHoldId())
              .append(" memberId=").append(e.getMemberId())
              .append(" bookId=").append(e.getBookId());
        } else if (event instanceof FineIssuedEvent e) {
            sb.append(" fineId=").append(e.getFineId())
              .append(" memberId=").append(e.getMemberId())
              .append(" amount=").append(e.getAmount())
              .append(" reason=").append(e.getReason());
        } else if (event instanceof MemberRegisteredEvent e) {
            sb.append(" memberId=").append(e.getMemberId())
              .append(" membershipNumber=").append(e.getMembershipNumber());
        } else if (event instanceof MembershipExpiredEvent e) {
            sb.append(" memberId=").append(e.getMemberId())
              .append(" expiryDate=").append(e.getExpiryDate());
        } else if (event instanceof BookAddedEvent e) {
            sb.append(" bookId=").append(e.getBookId())
              .append(" isbn=").append(e.getIsbn())
              .append(" title=").append(e.getTitle());
        } else if (event instanceof OverdueNoticeEvent e) {
            sb.append(" loanId=").append(e.getLoanId())
              .append(" memberId=").append(e.getMemberId())
              .append(" daysOverdue=").append(e.getDaysOverdue());
        }

        return sb.toString();
    }
}
