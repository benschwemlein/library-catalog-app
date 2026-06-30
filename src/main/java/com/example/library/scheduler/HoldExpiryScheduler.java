package com.example.library.scheduler;

import com.example.library.entity.Hold;
import com.example.library.entity.HoldStatus;
import com.example.library.entity.NotificationChannel;
import com.example.library.entity.NotificationType;
import com.example.library.repository.HoldRepository;
import com.example.library.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class HoldExpiryScheduler {

    private static final Logger log = LoggerFactory.getLogger(HoldExpiryScheduler.class);

    @Autowired
    private HoldRepository holdRepository;

    @Autowired
    private NotificationService notificationService;

    /**
     * Runs at 9:00 AM every day to expire holds that are past their expiry date.
     */
    @Scheduled(cron = "0 0 9 * * *")
    public void expireOldHolds() {
        log.info("Starting hold expiry processing at {}", LocalDateTime.now());

        List<Hold> expirableHolds = holdRepository.findExpiredHolds(LocalDateTime.now());

        int expiredCount = 0;

        for (Hold hold : expirableHolds) {
            try {
                hold.setStatus(HoldStatus.EXPIRED);
                holdRepository.save(hold);

                notificationService.sendNotification(
                        hold.getMember().getId(),
                        NotificationType.HOLD_READY,
                        "Your hold for \"" + hold.getBook().getTitle() + "\" has expired.",
                        NotificationChannel.EMAIL
                );
                expiredCount++;
            } catch (Exception e) {
                log.error("Error expiring hold id={}: {}", hold.getId(), e.getMessage(), e);
            }
        }

        log.info("Hold expiry processing complete. Holds expired: {}", expiredCount);
    }
}
