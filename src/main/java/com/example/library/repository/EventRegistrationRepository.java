package com.example.library.repository;

import com.example.library.entity.EventRegistration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EventRegistrationRepository extends JpaRepository<EventRegistration, Long> {

    Optional<EventRegistration> findByEvent_IdAndMember_Id(Long eventId, Long memberId);

    long countByEvent_Id(Long eventId);

    List<EventRegistration> findByMember_Id(Long memberId);

    List<EventRegistration> findByEvent_Id(Long eventId);

    boolean existsByEvent_IdAndMember_Id(Long eventId, Long memberId);
}
