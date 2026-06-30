package com.example.library.service;

import com.example.library.TestDataFactory;
import com.example.library.entity.Book;
import com.example.library.entity.Member;
import com.example.library.readingchallenge.*;
import com.example.library.repository.BookRepository;
import com.example.library.repository.MemberRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReadingChallengeServiceTest {

    @Mock
    private ReadingChallengeRepository challengeRepository;

    @Mock
    private ChallengeParticipationRepository participationRepository;

    @Mock
    private ChallengeProgressRepository progressRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private ReadingChallengeService readingChallengeService;

    // -------------------------------------------------------------------------
    // createChallenge tests
    // -------------------------------------------------------------------------

    @Test
    void createChallenge_validDates_saves() {
        LocalDate start = LocalDate.now();
        LocalDate end = start.plusMonths(1);

        ReadingChallenge savedChallenge = ReadingChallenge.builder()
                .id(1L)
                .name("Summer Reading Challenge")
                .description("Read as many books as you can this summer!")
                .startDate(start)
                .endDate(end)
                .targetBooks(5)
                .targetGenreNames("Fiction,Mystery")
                .badge("Summer Reader")
                .active(true)
                .build();

        when(challengeRepository.save(any(ReadingChallenge.class))).thenReturn(savedChallenge);

        ChallengeDTO result = readingChallengeService.createChallenge(
                "Summer Reading Challenge",
                "Read as many books as you can this summer!",
                start, end, 5, "Fiction,Mystery", "Summer Reader");

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Summer Reading Challenge");
        assertThat(result.getTargetBooks()).isEqualTo(5);
        assertThat(result.isActive()).isTrue();
        verify(challengeRepository).save(any(ReadingChallenge.class));
    }

    @Test
    void createChallenge_endBeforeStart_throws() {
        LocalDate start = LocalDate.now();
        LocalDate end = start.minusDays(1);

        assertThatThrownBy(() -> readingChallengeService.createChallenge(
                "Bad Challenge", "Desc", start, end, 3, null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Start date must be before end date");

        verify(challengeRepository, never()).save(any());
    }

    // -------------------------------------------------------------------------
    // enrollMember tests
    // -------------------------------------------------------------------------

    @Test
    void enroll_happyPath_setsEnrollDate() {
        Member member = TestDataFactory.createMember();
        ReadingChallenge challenge = ReadingChallenge.builder()
                .id(10L)
                .name("Winter Challenge")
                .startDate(LocalDate.now().minusDays(5))
                .endDate(LocalDate.now().plusMonths(2))
                .targetBooks(4)
                .active(true)
                .build();

        ChallengeParticipation savedParticipation = ChallengeParticipation.builder()
                .id(100L)
                .challenge(challenge)
                .member(member)
                .enrollDate(LocalDate.now())
                .completedBooks(0)
                .badgeEarned(false)
                .build();

        when(challengeRepository.findById(10L)).thenReturn(Optional.of(challenge));
        when(participationRepository.findByChallengeIdAndMemberId(10L, member.getId()))
                .thenReturn(Optional.empty());
        when(memberRepository.findById(member.getId())).thenReturn(Optional.of(member));
        when(participationRepository.save(any(ChallengeParticipation.class))).thenReturn(savedParticipation);

        ParticipationDTO result = readingChallengeService.enrollMember(10L, member.getId());

        assertThat(result).isNotNull();
        assertThat(result.getChallengeId()).isEqualTo(10L);
        assertThat(result.getMemberId()).isEqualTo(member.getId());
        assertThat(result.getEnrollDate()).isNotNull();

        ArgumentCaptor<ChallengeParticipation> captor = ArgumentCaptor.forClass(ChallengeParticipation.class);
        verify(participationRepository).save(captor.capture());
        assertThat(captor.getValue().getEnrollDate()).isEqualTo(LocalDate.now());
    }

    @Test
    void enroll_duplicateEnrollment_throws() {
        ReadingChallenge challenge = ReadingChallenge.builder()
                .id(10L)
                .name("Winter Challenge")
                .startDate(LocalDate.now().minusDays(5))
                .endDate(LocalDate.now().plusMonths(2))
                .targetBooks(4)
                .active(true)
                .build();

        ChallengeParticipation existing = ChallengeParticipation.builder()
                .id(99L)
                .challenge(challenge)
                .enrollDate(LocalDate.now().minusDays(2))
                .completedBooks(1)
                .badgeEarned(false)
                .build();

        when(challengeRepository.findById(10L)).thenReturn(Optional.of(challenge));
        when(participationRepository.findByChallengeIdAndMemberId(10L, 1L))
                .thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> readingChallengeService.enrollMember(10L, 1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already enrolled");

        verify(participationRepository, never()).save(any());
    }

    @Test
    void enroll_challengeEnded_throws() {
        ReadingChallenge challenge = ReadingChallenge.builder()
                .id(10L)
                .name("Ended Challenge")
                .startDate(LocalDate.now().minusMonths(3))
                .endDate(LocalDate.now().minusDays(1))
                .targetBooks(4)
                .active(true)
                .build();

        when(challengeRepository.findById(10L)).thenReturn(Optional.of(challenge));

        assertThatThrownBy(() -> readingChallengeService.enrollMember(10L, 1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not active");

        verify(participationRepository, never()).save(any());
    }

    // -------------------------------------------------------------------------
    // logProgress tests
    // -------------------------------------------------------------------------

    @Test
    void logProgress_partialProgress_updatesCount() {
        Member member = TestDataFactory.createMember();
        Book book = TestDataFactory.createBook();

        ReadingChallenge challenge = ReadingChallenge.builder()
                .id(10L)
                .name("3-Book Challenge")
                .startDate(LocalDate.now().minusMonths(1))
                .endDate(LocalDate.now().plusMonths(1))
                .targetBooks(3)
                .active(true)
                .build();

        ChallengeParticipation participation = ChallengeParticipation.builder()
                .id(100L)
                .challenge(challenge)
                .member(member)
                .enrollDate(LocalDate.now().minusDays(10))
                .completedBooks(0)
                .badgeEarned(false)
                .build();

        ChallengeParticipation savedParticipation = ChallengeParticipation.builder()
                .id(100L)
                .challenge(challenge)
                .member(member)
                .enrollDate(LocalDate.now().minusDays(10))
                .completedBooks(1)
                .badgeEarned(false)
                .build();

        when(participationRepository.findByChallengeIdAndMemberId(10L, member.getId()))
                .thenReturn(Optional.of(participation));
        when(bookRepository.findById(book.getId())).thenReturn(Optional.of(book));
        when(progressRepository.save(any(ChallengeProgress.class))).thenAnswer(inv -> inv.getArgument(0));
        when(participationRepository.save(any(ChallengeParticipation.class))).thenReturn(savedParticipation);

        ParticipationDTO result = readingChallengeService.logProgress(
                10L, member.getId(), book.getId(), book.getTitle(), LocalDate.now(), "Great read");

        assertThat(result).isNotNull();
        assertThat(result.getCompletedBooks()).isEqualTo(1);
        assertThat(result.isBadgeEarned()).isFalse();
        assertThat(result.getCompletedDate()).isNull();
    }

    @Test
    void logProgress_reachesTarget_awardsBadge() {
        Member member = TestDataFactory.createMember();
        Book book = TestDataFactory.createBook();

        ReadingChallenge challenge = ReadingChallenge.builder()
                .id(10L)
                .name("3-Book Challenge")
                .startDate(LocalDate.now().minusMonths(1))
                .endDate(LocalDate.now().plusMonths(1))
                .targetBooks(3)
                .active(true)
                .build();

        // completedBooks is 2, logging 1 more will reach target of 3
        ChallengeParticipation participation = ChallengeParticipation.builder()
                .id(100L)
                .challenge(challenge)
                .member(member)
                .enrollDate(LocalDate.now().minusDays(10))
                .completedBooks(2)
                .badgeEarned(false)
                .build();

        when(participationRepository.findByChallengeIdAndMemberId(10L, member.getId()))
                .thenReturn(Optional.of(participation));
        when(bookRepository.findById(book.getId())).thenReturn(Optional.of(book));
        when(progressRepository.save(any(ChallengeProgress.class))).thenAnswer(inv -> inv.getArgument(0));
        when(participationRepository.save(any(ChallengeParticipation.class))).thenAnswer(inv -> inv.getArgument(0));

        ParticipationDTO result = readingChallengeService.logProgress(
                10L, member.getId(), book.getId(), book.getTitle(), LocalDate.now(), "Finished the challenge!");

        assertThat(result).isNotNull();
        assertThat(result.getCompletedBooks()).isEqualTo(3);
        assertThat(result.isBadgeEarned()).isTrue();
        assertThat(result.getCompletedDate()).isNotNull();
    }

    // -------------------------------------------------------------------------
    // getLeaderboard tests
    // -------------------------------------------------------------------------

    @Test
    void getLeaderboard_sortedByCompletedBooks() {
        Member member1 = TestDataFactory.createMember();
        Member member2 = TestDataFactory.createPremiumMember();
        Member member3 = TestDataFactory.createStudentMember();

        ReadingChallenge challenge = ReadingChallenge.builder()
                .id(10L)
                .name("Leaderboard Challenge")
                .startDate(LocalDate.now().minusMonths(1))
                .endDate(LocalDate.now().plusMonths(1))
                .targetBooks(5)
                .active(true)
                .build();

        ChallengeParticipation p1 = ChallengeParticipation.builder()
                .id(1L)
                .challenge(challenge)
                .member(member1)
                .enrollDate(LocalDate.now().minusDays(20))
                .completedBooks(5)
                .badgeEarned(true)
                .completedDate(LocalDate.now().minusDays(2))
                .build();

        ChallengeParticipation p2 = ChallengeParticipation.builder()
                .id(2L)
                .challenge(challenge)
                .member(member2)
                .enrollDate(LocalDate.now().minusDays(18))
                .completedBooks(3)
                .badgeEarned(false)
                .build();

        ChallengeParticipation p3 = ChallengeParticipation.builder()
                .id(3L)
                .challenge(challenge)
                .member(member3)
                .enrollDate(LocalDate.now().minusDays(15))
                .completedBooks(1)
                .badgeEarned(false)
                .build();

        // Repository returns already sorted by completedBooks desc
        when(participationRepository.findByChallengeIdOrderByCompletedBooksDesc(10L))
                .thenReturn(List.of(p1, p2, p3));

        List<LeaderboardEntryDTO> leaderboard = readingChallengeService.getLeaderboard(10L);

        assertThat(leaderboard).hasSize(3);
        assertThat(leaderboard.get(0).getRank()).isEqualTo(1);
        assertThat(leaderboard.get(0).getCompletedBooks()).isEqualTo(5);
        assertThat(leaderboard.get(0).isBadgeEarned()).isTrue();
        assertThat(leaderboard.get(1).getRank()).isEqualTo(2);
        assertThat(leaderboard.get(1).getCompletedBooks()).isEqualTo(3);
        assertThat(leaderboard.get(2).getRank()).isEqualTo(3);
        assertThat(leaderboard.get(2).getCompletedBooks()).isEqualTo(1);
    }
}
