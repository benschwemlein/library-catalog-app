package com.example.library.pattern.strategy;

import com.example.library.entity.Member;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Delivers notifications via push notification (e.g., FCM or APNs).
 * In a production system this would call a push notification gateway.
 * Delivery is simulated via structured logging.
 */
@Component
@Slf4j
public class PushNotificationDeliveryStrategy implements NotificationDeliveryStrategy {

    @Override
    public boolean deliver(Member member, String subject, String body) {
        try {
            String deviceToken = resolveDeviceToken(member);

            if (deviceToken == null) {
                log.warn("No device token registered for member={}; skipping push notification",
                        member.getId());
                return false;
            }

            String preview = body != null && body.length() > 100 ? body.substring(0, 100) + "..." : body;

            log.info("Sending push notification to member={} device='{}': title='{}' body='{}'",
                    member.getId(), deviceToken, subject, preview);

            // In production: pushGateway.send(deviceToken, subject, body);
            return true;
        } catch (Exception e) {
            log.error("Failed to send push notification to member={}: {}",
                    member.getId(), e.getMessage(), e);
            return false;
        }
    }

    /**
     * Resolve the device push token for the member.
     * In a real implementation this would look up a device-token table.
     * Here we derive a placeholder token from the member ID so logs are identifiable.
     */
    private String resolveDeviceToken(Member member) {
        // Placeholder: in production, look up a DeviceToken entity for this member
        return "device-token-member-" + member.getId();
    }
}
