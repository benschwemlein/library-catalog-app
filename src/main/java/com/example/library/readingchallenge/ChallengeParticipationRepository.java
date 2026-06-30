package com.example.library.readingchallenge;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChallengeParticipationRepository extends JpaRepository<ChallengeParticipation, Long> {
    Optional<ChallengeParticipation> findByChallengeIdAndMemberId(Long challengeId, Long memberId);
    List<ChallengeParticipation> findByChallengeId(Long challengeId);
    List<ChallengeParticipation> findByMemberId(Long memberId);
    List<ChallengeParticipation> findByChallengeIdOrderByCompletedBooksDesc(Long challengeId);
}
