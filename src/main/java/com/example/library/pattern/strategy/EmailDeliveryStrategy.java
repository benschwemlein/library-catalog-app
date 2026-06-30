package com.example.library.pattern.strategy;

import com.example.library.entity.Member;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Delivers notifications via email.
 * In a production system this would integrate with an SMTP client or an email-sending service.
 * Here the delivery is simulated via structured logging.
 */
@Component
@Slf4j
public class EmailDeliveryStrategy implements NotificationDeliveryStrategy {

    @Override
    public boolean deliver(Member member, String subject, String body) {
        try {
            String email = member.getUser().getEmail();
            String preview = body != null && body.length() > 50 ? body.substring(0, 50) : body;

            log.info("Sending email to {}: subject='{}', body preview='{}'",
                    email, subject, preview);

            // In production: emailClient.send(email, subject, body);
            return true;
        } catch (Exception e) {
            log.error("Failed to send email notification to member={}: {}",
                    member.getId(), e.getMessage(), e);
            return false;
        }
    }
}
