package com.example.library.service;

import com.example.library.TestDataFactory;
import com.example.library.bookclub.*;
import com.example.library.entity.Book;
import com.example.library.entity.LibraryBranch;
import com.example.library.entity.Member;
import com.example.library.repository.BookRepository;
import com.example.library.repository.LibraryBranchRepository;
import com.example.library.repository.MemberRepository;
import com.example.library.repository.StaffMemberRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class BookClubServiceTest {

    @Mock
    private BookClubRepository clubRepository;

    @Mock
    private BookClubMembershipRepository membershipRepository;

    @Mock
    private BookClubMeetingRepository meetingRepository;

    @Mock
    private BookClubDiscussionRepository discussionRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private StaffMemberRepository staffMemberRepository;

    @Mock
    private LibraryBranchRepository branchRepository;

    @InjectMocks
    private BookClubService bookClubService;

    // -------------------------------------------------------------------------
    // createClub tests
    // -------------------------------------------------------------------------

    @Test
    void create_savesClub() {
        LibraryBranch branch = TestDataFactory.createBranch();

        BookClub savedClub = BookClub.builder()
                .id(1L)
                .name("Mystery Lovers")
                .description("A club for fans of mystery novels")
                .branch(branch)
                .maxMembers(15)
                .meetingSchedule("First Tuesday of the month")
                .status(BookClubStatus.ACTIVE)
                .build();

        when(branchRepository.findById(branch.getId())).thenReturn(Optional.of(branch));
        when(clubRepository.save(any(BookClub.class))).thenReturn(savedClub);
        when(membershipRepository.countByClubIdAndActiveTrue(savedClub.getId())).thenReturn(0L);

        BookClubDTO result = bookClubService.createClub(
                "Mystery Lovers",
                "A club for fans of mystery novels",
                branch.getId(),
                null,
                15,
                "First Tuesday of the month");

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Mystery Lovers");
        assertThat(result.getBranchId()).isEqualTo(branch.getId());
        assertThat(result.getMaxMembers()).isEqualTo(15);
        assertThat(result.getStatus()).isEqualTo(BookClubStatus.ACTIVE);
        verify(clubRepository).save(any(BookClub.class));
    }

    // -------------------------------------------------------------------------
    // joinClub tests
    // -------------------------------------------------------------------------

    @Test
    void join_happyPath_createsMembership() {
        LibraryBranch branch = TestDataFactory.createBranch();
        Member member = TestDataFactory.createMember();

        BookClub club = BookClub.builder()
                .id(5L)
                .name("Sci-Fi Circle")
                .branch(branch)
                .maxMembers(20)
                .status(BookClubStatus.ACTIVE)
                .build();

        BookClubMembership savedMembership = BookClubMembership.builder()
                .id(100L)
                .club(club)
                .member(member)
                .joinDate(LocalDate.now())
                .role(BookClubMemberRole.MEMBER)
                .active(true)
                .build();

        when(clubRepository.findById(5L)).thenReturn(Optional.of(club));
        when(membershipRepository.countByClubIdAndActiveTrue(5L)).thenReturn(10L);
        when(membershipRepository.findByClubIdAndMemberId(5L, member.getId())).thenReturn(Optional.empty());
        when(memberRepository.findById(member.getId())).thenReturn(Optional.of(member));
        when(membershipRepository.save(any(BookClubMembership.class))).thenReturn(savedMembership);

        BookClubMembership result = bookClubService.joinClub(5L, member.getId());

        assertThat(result).isNotNull();
        assertThat(result.isActive()).isTrue();
        assertThat(result.getRole()).isEqualTo(BookClubMemberRole.MEMBER);
        assertThat(result.getClub().getId()).isEqualTo(5L);
        verify(membershipRepository).save(any(BookClubMembership.class));
    }

    @Test
    void join_alreadyMember_throws() {
        LibraryBranch branch = TestDataFactory.createBranch();
        Member member = TestDataFactory.createMember();

        BookClub club = BookClub.builder()
                .id(5L)
                .name("Sci-Fi Circle")
                .branch(branch)
                .maxMembers(20)
                .status(BookClubStatus.ACTIVE)
                .build();

        BookClubMembership existingMembership = BookClubMembership.builder()
                .id(99L)
                .club(club)
                .member(member)
                .joinDate(LocalDate.now().minusMonths(1))
                .role(BookClubMemberRole.MEMBER)
                .active(true)
                .build();

        when(clubRepository.findById(5L)).thenReturn(Optional.of(club));
        when(membershipRepository.countByClubIdAndActiveTrue(5L)).thenReturn(5L);
        when(membershipRepository.findByClubIdAndMemberId(5L, member.getId()))
                .thenReturn(Optional.of(existingMembership));

        assertThatThrownBy(() -> bookClubService.joinClub(5L, member.getId()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("already");

        verify(membershipRepository, never()).save(any());
    }

    @Test
    void join_clubFull_throws() {
        LibraryBranch branch = TestDataFactory.createBranch();

        BookClub club = BookClub.builder()
                .id(5L)
                .name("Tiny Book Club")
                .branch(branch)
                .maxMembers(10)
                .status(BookClubStatus.ACTIVE)
                .build();

        when(clubRepository.findById(5L)).thenReturn(Optional.of(club));
        // Current count equals maxMembers
        when(membershipRepository.countByClubIdAndActiveTrue(5L)).thenReturn(10L);

        assertThatThrownBy(() -> bookClubService.joinClub(5L, 1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("capacity");

        verify(membershipRepository, never()).save(any());
    }

    // -------------------------------------------------------------------------
    // leaveClub tests
    // -------------------------------------------------------------------------

    @Test
    void leave_activeMembership_deactivates() {
        LibraryBranch branch = TestDataFactory.createBranch();
        Member member = TestDataFactory.createMember();

        BookClub club = BookClub.builder()
                .id(5L)
                .name("Sci-Fi Circle")
                .branch(branch)
                .maxMembers(20)
                .status(BookClubStatus.ACTIVE)
                .build();

        BookClubMembership membership = BookClubMembership.builder()
                .id(100L)
                .club(club)
                .member(member)
                .joinDate(LocalDate.now().minusMonths(2))
                .role(BookClubMemberRole.MEMBER)
                .active(true)
                .build();

        when(membershipRepository.findByClubIdAndMemberId(5L, member.getId()))
                .thenReturn(Optional.of(membership));
        when(membershipRepository.save(any(BookClubMembership.class))).thenAnswer(inv -> inv.getArgument(0));

        bookClubService.leaveClub(5L, member.getId());

        ArgumentCaptor<BookClubMembership> captor = ArgumentCaptor.forClass(BookClubMembership.class);
        verify(membershipRepository).save(captor.capture());
        assertThat(captor.getValue().isActive()).isFalse();
    }

    // -------------------------------------------------------------------------
    // postDiscussion tests
    // -------------------------------------------------------------------------

    @Test
    void postDiscussion_savesPost() {
        LibraryBranch branch = TestDataFactory.createBranch();
        Member member = TestDataFactory.createMember();

        BookClub club = BookClub.builder()
                .id(5L)
                .name("Sci-Fi Circle")
                .branch(branch)
                .maxMembers(20)
                .status(BookClubStatus.ACTIVE)
                .build();

        BookClubDiscussion savedDiscussion = BookClubDiscussion.builder()
                .id(200L)
                .club(club)
                .poster(member)
                .content("Has anyone read the latest Dune sequel?")
                .edited(false)
                .build();

        when(clubRepository.findById(5L)).thenReturn(Optional.of(club));
        when(memberRepository.findById(member.getId())).thenReturn(Optional.of(member));
        when(discussionRepository.save(any(BookClubDiscussion.class))).thenReturn(savedDiscussion);

        DiscussionPostDTO result = bookClubService.postDiscussion(
                5L, member.getId(), "Has anyone read the latest Dune sequel?", null);

        assertThat(result).isNotNull();
        assertThat(result.getClubId()).isEqualTo(5L);
        assertThat(result.getContent()).isEqualTo("Has anyone read the latest Dune sequel?");
        assertThat(result.getParentDiscussionId()).isNull();
        assertThat(result.isEdited()).isFalse();
        verify(discussionRepository).save(any(BookClubDiscussion.class));
    }

    @Test
    void replyToDiscussion_setsParent() {
        LibraryBranch branch = TestDataFactory.createBranch();
        Member member = TestDataFactory.createMember();

        BookClub club = BookClub.builder()
                .id(5L)
                .name("Sci-Fi Circle")
                .branch(branch)
                .maxMembers(20)
                .status(BookClubStatus.ACTIVE)
                .build();

        BookClubDiscussion parentDiscussion = BookClubDiscussion.builder()
                .id(200L)
                .club(club)
                .poster(member)
                .content("Has anyone read the latest Dune sequel?")
                .edited(false)
                .build();

        BookClubDiscussion savedReply = BookClubDiscussion.builder()
                .id(201L)
                .club(club)
                .poster(member)
                .content("Yes, it was excellent!")
                .parentDiscussion(parentDiscussion)
                .edited(false)
                .build();

        when(clubRepository.findById(5L)).thenReturn(Optional.of(club));
        when(memberRepository.findById(member.getId())).thenReturn(Optional.of(member));
        when(discussionRepository.findById(200L)).thenReturn(Optional.of(parentDiscussion));
        when(discussionRepository.save(any(BookClubDiscussion.class))).thenReturn(savedReply);

        DiscussionPostDTO result = bookClubService.postDiscussion(
                5L, member.getId(), "Yes, it was excellent!", 200L);

        assertThat(result).isNotNull();
        assertThat(result.getParentDiscussionId()).isEqualTo(200L);

        ArgumentCaptor<BookClubDiscussion> captor = ArgumentCaptor.forClass(BookClubDiscussion.class);
        verify(discussionRepository).save(captor.capture());
        assertThat(captor.getValue().getParentDiscussion()).isNotNull();
        assertThat(captor.getValue().getParentDiscussion().getId()).isEqualTo(200L);
    }

    // -------------------------------------------------------------------------
    // setCurrentBook tests
    // -------------------------------------------------------------------------

    @Test
    void setCurrentBook_updatesClub() {
        LibraryBranch branch = TestDataFactory.createBranch();
        Book book = TestDataFactory.createBook();

        BookClub club = BookClub.builder()
                .id(5L)
                .name("Sci-Fi Circle")
                .branch(branch)
                .maxMembers(20)
                .status(BookClubStatus.ACTIVE)
                .build();

        BookClub updatedClub = BookClub.builder()
                .id(5L)
                .name("Sci-Fi Circle")
                .branch(branch)
                .maxMembers(20)
                .status(BookClubStatus.ACTIVE)
                .currentBook(book)
                .build();

        when(clubRepository.findById(5L)).thenReturn(Optional.of(club));
        when(bookRepository.findById(book.getId())).thenReturn(Optional.of(book));
        when(clubRepository.save(any(BookClub.class))).thenReturn(updatedClub);
        when(membershipRepository.countByClubIdAndActiveTrue(5L)).thenReturn(8L);

        BookClubDTO result = bookClubService.setCurrentBook(5L, book.getId());

        assertThat(result).isNotNull();
        assertThat(result.getCurrentBookId()).isEqualTo(book.getId());
        assertThat(result.getCurrentBookTitle()).isEqualTo(book.getTitle());

        ArgumentCaptor<BookClub> captor = ArgumentCaptor.forClass(BookClub.class);
        verify(clubRepository).save(captor.capture());
        assertThat(captor.getValue().getCurrentBook()).isEqualTo(book);
    }
}
