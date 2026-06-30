package com.example.library.controller;

import com.example.library.entity.Notification;
import com.example.library.repository.NotificationRepository;
import com.example.library.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private NotificationRepository notificationRepository;

    @GetMapping
    public ResponseEntity<List<Notification>> getAllNotifications() {
        return ResponseEntity.ok(notificationRepository.findAll());
    }

    @GetMapping("/member/{memberId}")
    public ResponseEntity<List<Notification>> getNotificationsByMember(@PathVariable Long memberId) {
        return ResponseEntity.ok(notificationService.getAllNotifications(memberId));
    }

    @GetMapping("/me")
    public ResponseEntity<List<Notification>> getMyNotifications(@RequestParam Long memberId) {
        return ResponseEntity.ok(notificationService.getAllNotifications(memberId));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<Notification> markAsRead(@PathVariable Long id) {
        return ResponseEntity.ok(notificationService.markAsRead(id));
    }

    @PutMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead(@RequestParam Long memberId) {
        notificationService.markAllAsRead(memberId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/member/{memberId}/unread-count")
    public ResponseEntity<java.util.Map<String, Long>> getUnreadCount(@PathVariable Long memberId) {
        long count = notificationRepository.findAll().stream()
                .filter(n -> n.getMember() != null && memberId.equals(n.getMember().getId()) && n.getReadDate() == null)
                .count();
        return ResponseEntity.ok(java.util.Map.of("count", count));
    }
}
