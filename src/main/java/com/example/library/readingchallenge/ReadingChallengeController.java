package com.example.library.readingchallenge;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/library/challenges")
@Slf4j
@RequiredArgsConstructor
public class ReadingChallengeController {

    private final ReadingChallengeService challengeService;

    @GetMapping
    public ResponseEntity<List<ChallengeDTO>> getActiveChallenges() {
        List<ChallengeDTO> challenges = challengeService.getActiveChallenges();
        return ResponseEntity.ok(challenges);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ChallengeDTO> getChallenge(@PathVariable Long id) {
        ChallengeDTO challenge = challengeService.getChallenge(id);
        return ResponseEntity.ok(challenge);
    }

    @PostMapping
    public ResponseEntity<ChallengeDTO> createChallenge(@RequestBody CreateChallengeRequest request) {
        ChallengeDTO created = challengeService.createChallenge(
                request.getName(),
                request.getDescription(),
                request.getStartDate(),
                request.getEndDate(),
                request.getTargetBooks(),
                request.getTargetGenres(),
                request.getBadge()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PostMapping("/{id}/enroll")
    public ResponseEntity<ParticipationDTO> enrollMember(@PathVariable Long id,
                                                         @RequestBody EnrollRequest request) {
        ParticipationDTO participation = challengeService.enrollMember(id, request.getMemberId());
        return ResponseEntity.status(HttpStatus.CREATED).body(participation);
    }

    @PostMapping("/{id}/progress")
    public ResponseEntity<ParticipationDTO> logProgress(@PathVariable Long id,
                                                        @RequestBody LogProgressRequest request) {
        ParticipationDTO participation = challengeService.logProgress(
                id,
                request.getMemberId(),
                request.getBookId(),
                request.getBookTitle(),
                request.getCompletedDate(),
                request.getNotes()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(participation);
    }

    @GetMapping("/{id}/leaderboard")
    public ResponseEntity<List<LeaderboardEntryDTO>> getLeaderboard(@PathVariable Long id) {
        List<LeaderboardEntryDTO> leaderboard = challengeService.getLeaderboard(id);
        return ResponseEntity.ok(leaderboard);
    }

    @GetMapping("/member/{memberId}/participations")
    public ResponseEntity<List<ParticipationDTO>> getMemberParticipations(@PathVariable Long memberId) {
        List<ParticipationDTO> participations = challengeService.getMemberParticipations(memberId);
        return ResponseEntity.ok(participations);
    }

    @GetMapping("/active")
    public ResponseEntity<List<ChallengeDTO>> getActiveChallengesExplicit() {
        return ResponseEntity.ok(challengeService.getActiveChallenges());
    }

    @PostMapping("/{id}/enroll/{memberId}")
    public ResponseEntity<ParticipationDTO> enrollMemberByPath(@PathVariable Long id,
                                                               @PathVariable Long memberId) {
        ParticipationDTO participation = challengeService.enrollMember(id, memberId);
        return ResponseEntity.status(HttpStatus.CREATED).body(participation);
    }

    @GetMapping("/member/{memberId}")
    public ResponseEntity<List<ParticipationDTO>> getMemberParticipationsShort(@PathVariable Long memberId) {
        List<ParticipationDTO> participations = challengeService.getMemberParticipations(memberId);
        return ResponseEntity.ok(participations);
    }

    // -------------------------------------------------------------------------
    // Inner request classes
    // -------------------------------------------------------------------------

    @Data
    static class CreateChallengeRequest {
        private String name;
        private String description;
        private LocalDate startDate;
        private LocalDate endDate;
        private int targetBooks;
        private String targetGenres;
        private String badge;
    }

    @Data
    static class EnrollRequest {
        private Long memberId;
    }

    @Data
    static class LogProgressRequest {
        private Long memberId;
        private Long bookId;
        private String bookTitle;
        private LocalDate completedDate;
        private String notes;
    }
}
