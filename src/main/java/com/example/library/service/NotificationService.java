package com.example.library.service;

import com.example.library.entity.*;
import com.example.library.exception.MemberNotFoundException;
import com.example.library.repository.MemberRepository;
import com.example.library.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private MemberRepository memberRepository;

    public Notification sendNotification(Long memberId, NotificationType type, String message, NotificationChannel channel) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException("Member not found with id: " + memberId));

        Notification notification = new Notification();
        notification.setMember(member);
        notification.setType(type);
        notification.setMessage(message);
        notification.setChannel(channel);
        notification.setSentDate(LocalDateTime.now());

        return notificationRepository.save(notification);
    }

    public Notification markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found with id: " + notificationId));
        notification.setReadDate(LocalDateTime.now());
        return notificationRepository.save(notification);
    }

    public void markAllAsRead(Long memberId) {
        List<Notification> unread = notificationRepository.findByMember_IdAndReadDateIsNull(memberId);
        LocalDateTime now = LocalDateTime.now();
        for (Notification n : unread) {
            n.setReadDate(now);
        }
        notificationRepository.saveAll(unread);
    }

    @Transactional(readOnly = true)
    public List<Notification> getUnreadNotifications(Long memberId) {
        return notificationRepository.findByMember_IdAndReadDateIsNull(memberId);
    }

    @Transactional(readOnly = true)
    public List<Notification> getAllNotifications(Long memberId) {
        return notificationRepository.findByMember_IdOrderBySentDateDesc(memberId);
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(Long memberId) {
        return notificationRepository.countByMember_IdAndReadDateIsNull(memberId);
    }
}
