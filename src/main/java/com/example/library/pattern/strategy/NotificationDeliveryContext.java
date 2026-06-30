package com.example.library.pattern.strategy;

import com.example.library.entity.Member;
import com.example.library.entity.NotificationChannel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Context class that selects and delegates to the appropriate
 * {@link NotificationDeliveryStrategy} based on the requested {@link NotificationChannel}.
 * Can also broadcast a notification across all channels.
 */
@Component
@Slf4j
public class NotificationDeliveryContext {

    private final EmailDeliveryStrategy emailStrategy;
    private final SmsDeliveryStrategy smsStrategy;
    private final InAppDeliveryStrategy inAppStrategy;
    private final PushNotificationDeliveryStrategy pushStrategy;

    public NotificationDeliveryContext(EmailDeliveryStrategy emailStrategy,
                                       SmsDeliveryStrategy smsStrategy,
                                       InAppDeliveryStrategy inAppStrategy,
                                       PushNotificationDeliveryStrategy pushStrategy) {
        this.emailStrategy = emailStrategy;
        this.smsStrategy = smsStrategy;
        this.inAppStrategy = inAppStrategy;
        this.pushStrategy = pushStrategy;
    }

    /**
     * Deliver a notification to a member via the specified channel.
     *
     * @param member  the target member
     * @param subject the notification subject / title
     * @param body    the full notification body
     * @param channel the desired delivery channel
     * @return true if delivery succeeded
     */
    public boolean deliver(Member member, String subject, String body, NotificationChannel channel) {
        log.debug("Delivering notification to member={} via channel={}", member.getId(), channel);
        NotificationDeliveryStrategy strategy = resolveStrategy(channel);
        boolean result = strategy.deliver(member, subject, body);
        log.info("Notification delivery member={} channel={} success={}", member.getId(), channel, result);
        return result;
    }

    /**
     * Deliver a notification to a member via all available channels.
     * Collects per-channel results; a failure in one channel does not stop the others.
     *
     * @param member  the target member
     * @param subject the notification subject / title
     * @param body    the full notification body
     * @return list of per-channel results in channel enum order
     */
    public List<Boolean> deliverToAllChannels(Member member, String subject, String body) {
        log.info("Broadcasting notification to all channels for member={}", member.getId());
        List<Boolean> results = new ArrayList<>();

        for (NotificationChannel channel : NotificationChannel.values()) {
            try {
                boolean success = deliver(member, subject, body, channel);
                results.add(success);
            } catch (Exception e) {
                log.error("Error delivering via channel={} to member={}: {}",
                        channel, member.getId(), e.getMessage(), e);
                results.add(false);
            }
        }

        long successCount = results.stream().filter(Boolean::booleanValue).count();
        log.info("Broadcast complete for member={}: {}/{} channels succeeded",
                member.getId(), successCount, results.size());
        return results;
    }

    private NotificationDeliveryStrategy resolveStrategy(NotificationChannel channel) {
        return switch (channel) {
            case EMAIL -> emailStrategy;
            case SMS -> smsStrategy;
            case IN_APP -> inAppStrategy;
        };
    }
}
