package com.example.library.service;

import com.example.library.entity.Member;
import com.example.library.entity.MembershipTier;
import com.example.library.entity.NotificationChannel;
import com.example.library.entity.NotificationType;
import com.example.library.exception.MemberNotFoundException;
import com.example.library.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class MembershipService {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private NotificationService notificationService;

    public Member renewMembership(Long memberId, int months) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException("Member not found: " + memberId));

        LocalDate currentExpiry = member.getExpiryDate();
        LocalDate baseDate = (currentExpiry != null && currentExpiry.isAfter(LocalDate.now()))
                ? currentExpiry
                : LocalDate.now();
        member.setExpiryDate(baseDate.plusMonths(months));
        member.setActive(true);

        Member saved = memberRepository.save(member);

        notificationService.sendNotification(
                memberId,
                NotificationType.MEMBERSHIP_EXPIRING,
                "Your membership has been renewed until " + saved.getExpiryDate(),
                NotificationChannel.EMAIL
        );

        return saved;
    }

    public Member upgradeTier(Long memberId, String newTier) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException("Member not found: " + memberId));

        MembershipTier tier;
        try {
            tier = MembershipTier.valueOf(newTier.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid membership tier: " + newTier);
        }

        member.setMembershipTier(tier);
        Member saved = memberRepository.save(member);

        notificationService.sendNotification(
                memberId,
                NotificationType.MEMBERSHIP_EXPIRING,
                "Your membership has been upgraded to " + tier.name(),
                NotificationChannel.EMAIL
        );

        return saved;
    }

    @Transactional(readOnly = true)
    public List<Member> checkExpiringMemberships(int daysAhead) {
        LocalDate cutoffDate = LocalDate.now().plusDays(daysAhead);
        // Use findByExpiryDateBefore and filter active ones
        return memberRepository.findByExpiryDateBefore(cutoffDate).stream()
                .filter(m -> m.isActive() && (m.getExpiryDate() == null || m.getExpiryDate().isAfter(LocalDate.now())))
                .collect(Collectors.toList());
    }

    public Member suspendMembership(Long memberId, String reason) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException("Member not found: " + memberId));

        member.setActive(false);

        Member saved = memberRepository.save(member);

        notificationService.sendNotification(
                memberId,
                NotificationType.FINE_ISSUED,
                "Your membership has been suspended. Reason: " + reason,
                NotificationChannel.EMAIL
        );

        return saved;
    }
}
