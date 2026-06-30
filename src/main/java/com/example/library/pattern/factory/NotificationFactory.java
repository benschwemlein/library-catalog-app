package com.example.library.pattern.factory;

import com.example.library.entity.Fine;
import com.example.library.entity.Hold;
import com.example.library.entity.Loan;
import com.example.library.entity.Member;
import com.example.library.entity.Notification;
import com.example.library.entity.NotificationChannel;
import com.example.library.entity.NotificationType;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Factory for constructing {@link Notification} entity instances.
 *
 * <p>All methods return a new, unsaved {@link Notification}. The caller is responsible
 * for persisting the notification via {@code NotificationRepository.save()}.</p>
 */
@Component
public class NotificationFactory {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("MMM d, yyyy");

    /**
     * Build a hold-ready notification informing the member their held book is available.
     *
     * @param hold the hold that has become ready for pickup
     * @return an unsaved Notification entity
     */
    public Notification createHoldReadyNotification(Hold hold) {
        String bookTitle = hold.getBook() != null ? hold.getBook().getTitle() : "Unknown Title";
        String branchName = hold.getPickupBranch() != null ? hold.getPickupBranch().getName() : "your library";
        String expiryStr = hold.getExpiryDate() != null
                ? hold.getExpiryDate().format(DATE_FMT) : "soon";

        String message = String.format(
                "Your hold for '%s' is ready for pickup at %s. Expires: %s. " +
                "Please collect your item before the expiry date to avoid cancellation.",
                bookTitle, branchName, expiryStr
        );

        return Notification.builder()
                .member(hold.getMember())
                .type(NotificationType.HOLD_READY)
                .message(message)
                .sentDate(LocalDateTime.now())
                .channel(NotificationChannel.IN_APP)
                .build();
    }

    /**
     * Build an overdue notification for a loan that has passed its due date.
     *
     * @param loan       the overdue loan
     * @param daysOverdue the number of days past the due date
     * @return an unsaved Notification entity
     */
    public Notification createOverdueNotification(Loan loan, int daysOverdue) {
        String bookTitle = (loan.getBookCopy() != null && loan.getBookCopy().getBook() != null)
                ? loan.getBookCopy().getBook().getTitle() : "Unknown Title";
        String dueDateStr = loan.getDueDate() != null
                ? loan.getDueDate().format(DATE_FMT) : "your due date";

        String message = String.format(
                "Your loan for '%s' is %d day(s) overdue (due: %s). " +
                "Fines are accruing at your membership rate. Please return the item as soon as possible.",
                bookTitle, daysOverdue, dueDateStr
        );

        return Notification.builder()
                .member(loan.getMember())
                .type(NotificationType.OVERDUE)
                .message(message)
                .sentDate(LocalDateTime.now())
                .channel(NotificationChannel.IN_APP)
                .build();
    }

    /**
     * Build a fine-issued notification informing a member of a new fine on their account.
     *
     * @param fine the fine that was created
     * @return an unsaved Notification entity
     */
    public Notification createFineNotification(Fine fine) {
        String bookTitle = (fine.getLoan() != null
                && fine.getLoan().getBookCopy() != null
                && fine.getLoan().getBookCopy().getBook() != null)
                ? fine.getLoan().getBookCopy().getBook().getTitle() : "a borrowed item";

        String message = String.format(
                "A fine of $%.2f has been added to your account for the overdue return of '%s'. " +
                "Reason: %s. You can pay your fines from the Fines section of your account.",
                fine.getAmount(),
                bookTitle,
                fine.getReason() != null ? fine.getReason() : "overdue return"
        );

        return Notification.builder()
                .member(fine.getMember())
                .type(NotificationType.FINE_ISSUED)
                .message(message)
                .sentDate(LocalDateTime.now())
                .channel(NotificationChannel.IN_APP)
                .build();
    }

    /**
     * Build a welcome notification for a newly registered member.
     *
     * @param member the newly created member
     * @return an unsaved Notification entity
     */
    public Notification createWelcomeNotification(Member member) {
        String firstName = (member.getUser() != null && member.getUser().getFirstName() != null)
                ? member.getUser().getFirstName() : "Member";

        String message = String.format(
                "Welcome to the library, %s! Your membership number is %s and your membership tier is %s. " +
                "You can start borrowing books right away. Enjoy your membership!",
                firstName,
                member.getMembershipNumber(),
                member.getMembershipTier() != null ? member.getMembershipTier().name() : "STANDARD"
        );

        return Notification.builder()
                .member(member)
                .type(NotificationType.HOLD_READY)  // Use closest type; WELCOME would be ideal in a richer enum
                .message(message)
                .sentDate(LocalDateTime.now())
                .channel(NotificationChannel.IN_APP)
                .build();
    }

    /**
     * Build a membership-expiry warning notification.
     *
     * @param member         the member whose membership is expiring
     * @param daysUntilExpiry number of days until the membership expires
     * @return an unsaved Notification entity
     */
    public Notification createMembershipExpiryNotification(Member member, int daysUntilExpiry) {
        String expiryStr = member.getExpiryDate() != null
                ? member.getExpiryDate().format(DateTimeFormatter.ofPattern("MMMM d, yyyy")) : "soon";

        String urgencyPrefix = daysUntilExpiry <= 7
                ? "URGENT: "
                : daysUntilExpiry <= 30 ? "Reminder: " : "";

        String message = String.format(
                "%sYour library membership (number: %s) expires in %d day(s) on %s. " +
                "Renew your membership to continue borrowing books without interruption.",
                urgencyPrefix,
                member.getMembershipNumber(),
                daysUntilExpiry,
                expiryStr
        );

        return Notification.builder()
                .member(member)
                .type(NotificationType.MEMBERSHIP_EXPIRING)
                .message(message)
                .sentDate(LocalDateTime.now())
                .channel(NotificationChannel.IN_APP)
                .build();
    }
}
