package com.example.library.readingchallenge;

import com.example.catalog.model.User;
import com.example.library.entity.Book;
import com.example.library.entity.Member;
import com.example.library.repository.BookRepository;
import com.example.library.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class ReadingChallengeService {

    private final ReadingChallengeRepository challengeRepository;
    private final ChallengeParticipationRepository participationRepository;
    private final ChallengeProgressRepository progressRepository;
    private final MemberRepository memberRepository;
    private final BookRepository bookRepository;

    @Transactional
    public ChallengeDTO createChallenge(String name, String description, LocalDate start, LocalDate end,
                                        int targetBooks, String targetGenres, String badge) {
        if (start == null || end == null || !start.isBefore(end)) {
            throw new IllegalArgumentException("Start date must be before end date");
        }
        if (targetBooks <= 0) {
            throw new IllegalArgumentException("Target books must be positive");
        }

        ReadingChallenge challenge = ReadingChallenge.builder()
                .name(name)
                .description(description)
                .startDate(start)
                .endDate(end)
                .targetBooks(targetBooks)
                .targetGenreNames(targetGenres)
                .badge(badge)
                .active(true)
                .build();

        ReadingChallenge saved = challengeRepository.save(challenge);
        log.info("Created reading challenge '{}' with id {}", saved.getName(), saved.getId());
        return toDTO(saved, 0, 0);
    }

    @Transactional(readOnly = true)
    public List<ChallengeDTO> getActiveChallenges() {
        List<ReadingChallenge> challenges = challengeRepository.findByActiveTrueAndEndDateAfter(LocalDate.now());
        List<ChallengeDTO> result = new ArrayList<>();
        for (ReadingChallenge challenge : challenges) {
            List<ChallengeParticipation> participations = participationRepository.findByChallengeId(challenge.getId());
            int enrolledCount = participations.size();
            int completedCount = (int) participations.stream()
                    .filter(p -> p.getCompletedDate() != null)
                    .count();
            result.add(toDTO(challenge, enrolledCount, completedCount));
        }
        return result;
    }

    @Transactional(readOnly = true)
    public ChallengeDTO getChallenge(Long challengeId) {
        ReadingChallenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new RuntimeException("Challenge not found: " + challengeId));
        List<ChallengeParticipation> participations = participationRepository.findByChallengeId(challengeId);
        int enrolledCount = participations.size();
        int completedCount = (int) participations.stream()
                .filter(p -> p.getCompletedDate() != null)
                .count();
        return toDTO(challenge, enrolledCount, completedCount);
    }

    @Transactional
    public ParticipationDTO enrollMember(Long challengeId, Long memberId) {
        ReadingChallenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new RuntimeException("Challenge not found: " + challengeId));

        if (!challenge.isActive() || challenge.getEndDate().isBefore(LocalDate.now())) {
            throw new IllegalStateException("Challenge is not active");
        }

        participationRepository.findByChallengeIdAndMemberId(challengeId, memberId).ifPresent(p -> {
            throw new IllegalStateException("Member already enrolled");
        });

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Member not found: " + memberId));

        ChallengeParticipation participation = ChallengeParticipation.builder()
                .challenge(challenge)
                .member(member)
                .enrollDate(LocalDate.now())
                .completedBooks(0)
                .badgeEarned(false)
                .build();

        ChallengeParticipation saved = participationRepository.save(participation);
        log.info("Member {} enrolled in challenge {}", memberId, challengeId);
        return toParticipationDTO(saved);
    }

    @Transactional
    public ParticipationDTO logProgress(Long challengeId, Long memberId, Long bookId,
                                        String bookTitle, LocalDate completedDate, String notes) {
        ChallengeParticipation participation = participationRepository
                .findByChallengeIdAndMemberId(challengeId, memberId)
                .orElseThrow(() -> new RuntimeException(
                        "Participation not found for challenge " + challengeId + " and member " + memberId));

        Book book = null;
        if (bookId != null) {
            Optional<Book> found = bookRepository.findById(bookId);
            book = found.orElse(null);
        }

        String resolvedTitle = (book != null) ? book.getTitle() : bookTitle;

        LocalDate effectiveDate = (completedDate != null) ? completedDate : LocalDate.now();

        ChallengeProgress progress = ChallengeProgress.builder()
                .participation(participation)
                .book(book)
                .bookTitle(resolvedTitle)
                .completedDate(effectiveDate)
                .notes(notes)
                .verified(false)
                .build();

        progressRepository.save(progress);

        participation.setCompletedBooks(participation.getCompletedBooks() + 1);

        if (participation.getCompletedBooks() >= participation.getChallenge().getTargetBooks()
                && participation.getCompletedDate() == null) {
            participation.setCompletedDate(LocalDate.now());
            participation.setBadgeEarned(true);
            log.info("Member {} completed challenge {}", memberId, challengeId);
        }

        ChallengeParticipation saved = participationRepository.save(participation);
        return toParticipationDTO(saved);
    }

    @Transactional(readOnly = true)
    public List<LeaderboardEntryDTO> getLeaderboard(Long challengeId) {
        List<ChallengeParticipation> participations =
                participationRepository.findByChallengeIdOrderByCompletedBooksDesc(challengeId);

        List<LeaderboardEntryDTO> leaderboard = new ArrayList<>();
        int rank = 1;
        for (ChallengeParticipation p : participations) {
            Member member = p.getMember();
            leaderboard.add(LeaderboardEntryDTO.builder()
                    .rank(rank++)
                    .memberId(member.getId())
                    .memberName(getMemberName(member))
                    .completedBooks(p.getCompletedBooks())
                    .badgeEarned(p.isBadgeEarned())
                    .completedDate(p.getCompletedDate())
                    .build());
        }
        return leaderboard;
    }

    @Transactional
    public void awardBadge(Long participationId) {
        ChallengeParticipation participation = participationRepository.findById(participationId)
                .orElseThrow(() -> new RuntimeException("Participation not found: " + participationId));
        participation.setBadgeEarned(true);
        participationRepository.save(participation);
        log.info("Badge awarded for participation {}", participationId);
    }

    @Transactional(readOnly = true)
    public List<ParticipationDTO> getMemberParticipations(Long memberId) {
        List<ChallengeParticipation> participations = participationRepository.findByMemberId(memberId);
        List<ParticipationDTO> result = new ArrayList<>();
        for (ChallengeParticipation p : participations) {
            result.add(toParticipationDTO(p));
        }
        return result;
    }

    private ChallengeDTO toDTO(ReadingChallenge challenge, int enrolledCount, int completedCount) {
        return ChallengeDTO.builder()
                .id(challenge.getId())
                .name(challenge.getName())
                .description(challenge.getDescription())
                .startDate(challenge.getStartDate())
                .endDate(challenge.getEndDate())
                .targetBooks(challenge.getTargetBooks())
                .targetGenreNames(challenge.getTargetGenreNames())
                .badge(challenge.getBadge())
                .active(challenge.isActive())
                .enrolledCount(enrolledCount)
                .completedCount(completedCount)
                .createdAt(challenge.getCreatedAt())
                .build();
    }

    private ParticipationDTO toParticipationDTO(ChallengeParticipation p) {
        Member member = p.getMember();
        ReadingChallenge challenge = p.getChallenge();

        String memberName;
        User user = member.getUser();
        if (user != null) {
            memberName = (user.getFirstName() + " " + user.getLastName()).trim();
        } else {
            memberName = "Member #" + member.getId();
        }

        double progressPercentage = Math.min(100.0,
                (p.getCompletedBooks() * 100.0) / challenge.getTargetBooks());

        return ParticipationDTO.builder()
                .participationId(p.getId())
                .challengeId(challenge.getId())
                .challengeName(challenge.getName())
                .memberId(member.getId())
                .memberName(memberName)
                .enrollDate(p.getEnrollDate())
                .completedBooks(p.getCompletedBooks())
                .targetBooks(challenge.getTargetBooks())
                .badgeEarned(p.isBadgeEarned())
                .completedDate(p.getCompletedDate())
                .progressPercentage(progressPercentage)
                .build();
    }

    private String getMemberName(Member m) {
        User user = m.getUser();
        if (user != null) {
            return (user.getFirstName() + " " + user.getLastName()).trim();
        }
        return "Member #" + m.getId();
    }
}
