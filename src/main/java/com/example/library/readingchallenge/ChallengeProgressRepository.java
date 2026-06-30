package com.example.library.readingchallenge;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChallengeProgressRepository extends JpaRepository<ChallengeProgress, Long> {
    List<ChallengeProgress> findByParticipationId(Long participationId);
    long countByParticipationId(Long participationId);
}
