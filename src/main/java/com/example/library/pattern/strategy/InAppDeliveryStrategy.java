package com.example.library.pattern.strategy;

import com.example.library.entity.Member;
import com.example.library.entity.Notification;
import com.example.library.entity.NotificationChannel;
import com.example.library.entity.NotificationType;
import com.example.library.repository.NotificationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Delivers notifications by persisting a {@link Notification} entity in the database,
 * making the notification available in the member's in-app notification feed.
 */
@Component
@Slf4j
public class InAppDeliveryStrategy implements NotificationDeliveryStrategy {

    @Autowired
    private NotificationRepository notificationRepository;

    @Override
    public boolean deliver(Member member, String subject, String body) {
        try {
            NotificationType type = resolveNotificationType(subject);

            Notification notification = Notification.builder()
                    .member(member)
                    .type(type)
                    .message(body)
                    .sentDate(LocalDateTime.now())
                    .channel(NotificationChannel.IN_APP)
                    .build();

            Notification saved = notificationRepository.save(notification);

            log.info("In-app notification saved: id={} member={} type={} subject='{}'",
                    saved.getId(), member.getId(), type, subject);
            return true;
        } catch (Exception e) {
            log.error("Failed to persist in-app notification for member={}: {}",
                    member.getId(), e.getMessage(), e);
            return false;
        }
    }

    /**
     * Infer the {@link NotificationType} from the subject line.
     * Falls back to OVERDUE when the subject does not match a known pattern.
     */
    private NotificationType resolveNotificationType(String subject) {
        if (subject == null) {
            return NotificationType.OVERDUE;
        }
        String lower = subject.toLowerCase();
        if (lower.contains("hold") || lower.contains("ready") || lower.contains("pickup")) {
            return NotificationType.HOLD_READY;
        }
        if (lower.contains("overdue") || lower.contains("past due")) {
            return NotificationType.OVERDUE;
        }
        if (lower.contains("due soon") || lower.contains("reminder")) {
            return NotificationType.DUE_SOON;
        }
        if (lower.contains("fine") || lower.contains("fee") || lower.contains("charge")) {
            return NotificationType.FINE_ISSUED;
        }
        if (lower.contains("membership") || lower.contains("expir")) {
            return NotificationType.MEMBERSHIP_EXPIRING;
        }
        return NotificationType.OVERDUE;
    }
}
