package com.example.library.scheduler;

import com.example.library.entity.Member;
import com.example.library.entity.NotificationChannel;
import com.example.library.entity.NotificationType;
import com.example.library.repository.MemberRepository;
import com.example.library.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
public class MembershipExpiryScheduler {

    private static final Logger log = LoggerFactory.getLogger(MembershipExpiryScheduler.class);

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private NotificationService notificationService;

    /**
     * Runs at 10:00 AM every Monday to notify members whose memberships expire within 30 days.
     */
    @Scheduled(cron = "0 0 10 * * MON")
    public void notifyExpiringMemberships() {
        log.info("Checking for expiring memberships at {}", LocalDateTime.now());

        LocalDate now = LocalDate.now();
        LocalDate thirtyDaysFromNow = now.plusDays(30);

        List<Member> expiringMembers = memberRepository.findByExpiryDateBefore(thirtyDaysFromNow);
        // Further filter to only those not yet expired
        int notificationsSent = 0;

        for (Member member : expiringMembers) {
            if (member.getExpiryDate() == null || member.getExpiryDate().isBefore(now)) {
                continue;
            }
            try {
                long daysUntilExpiry = ChronoUnit.DAYS.between(now, member.getExpiryDate());
                notificationService.sendNotification(
                        member.getId(),
                        NotificationType.MEMBERSHIP_EXPIRING,
                        "Your library membership expires in " + daysUntilExpiry + " day(s) on "
                                + member.getExpiryDate()
                                + ". Please renew to continue borrowing books.",
                        NotificationChannel.EMAIL
                );
                notificationsSent++;
            } catch (Exception e) {
                log.error("Error notifying member id={} about expiring membership: {}", member.getId(), e.getMessage(), e);
            }
        }

        log.info("Membership expiry notifications complete. Notifications sent: {}", notificationsSent);
    }
}
