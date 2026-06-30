package com.example.library.pattern.strategy;

import com.example.library.entity.Member;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Delivers notifications via SMS.
 * SMS messages are limited to 160 characters; longer bodies are truncated.
 */
@Component
@Slf4j
public class SmsDeliveryStrategy implements NotificationDeliveryStrategy {

    private static final int SMS_MAX_LENGTH = 160;

    @Override
    public boolean deliver(Member member, String subject, String body) {
        try {
            String phone = member.getUser() != null ? member.getUser().getUsername() : "unknown";

            // Build the SMS body from the subject + body, truncated to 160 chars
            String smsText = buildSmsText(subject, body);

            log.info("Sending SMS to member={} ({}): '{}'",
                    member.getId(), phone, smsText);

            // In production: smsClient.send(phone, smsText);
            return true;
        } catch (Exception e) {
            log.error("Failed to send SMS notification to member={}: {}",
                    member.getId(), e.getMessage(), e);
            return false;
        }
    }

    private String buildSmsText(String subject, String body) {
        String combined = (subject != null ? subject + ": " : "") + (body != null ? body : "");
        if (combined.length() > SMS_MAX_LENGTH) {
            String truncated = combined.substring(0, SMS_MAX_LENGTH - 3) + "...";
            log.debug("SMS body truncated from {} to {} characters", combined.length(), SMS_MAX_LENGTH);
            return truncated;
        }
        return combined;
    }
}
