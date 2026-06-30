package com.example.library.repository;

import com.example.library.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByMember_IdAndReadDateIsNull(Long memberId);

    List<Notification> findByMember_IdOrderBySentDateDesc(Long memberId);

    List<Notification> findByMember_IdAndType(Long memberId, String type);

    long countByMember_IdAndReadDateIsNull(Long memberId);
}
