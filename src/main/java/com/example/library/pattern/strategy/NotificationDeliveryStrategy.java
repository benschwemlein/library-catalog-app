package com.example.library.pattern.strategy;

import com.example.library.entity.Member;

public interface NotificationDeliveryStrategy {

    /**
     * Deliver a notification to the given member.
     *
     * @param member  the member to notify
     * @param subject the subject or title of the notification
     * @param body    the full body text of the notification
     * @return true if delivery succeeded, false otherwise
     */
    boolean deliver(Member member, String subject, String body);
}
