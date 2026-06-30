package com.example.library.bookclub;

import com.example.library.entity.Book;
import com.example.library.entity.LibraryBranch;
import com.example.library.entity.Member;
import com.example.library.entity.StaffMember;
import com.example.library.repository.BookRepository;
import com.example.library.repository.LibraryBranchRepository;
import com.example.library.repository.MemberRepository;
import com.example.library.repository.StaffMemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class BookClubService {

    private final BookClubRepository clubRepository;
    private final BookClubMembershipRepository membershipRepository;
    private final BookClubMeetingRepository meetingRepository;
    private final BookClubDiscussionRepository discussionRepository;
    private final MemberRepository memberRepository;
    private final BookRepository bookRepository;
    private final StaffMemberRepository staffMemberRepository;
    private final LibraryBranchRepository branchRepository;

    @Transactional
    public BookClubDTO createClub(String name, String description, Long branchId, Long facilitatorId,
                                   int maxMembers, String meetingSchedule) {
        LibraryBranch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new RuntimeException("Branch not found with id: " + branchId));

        StaffMember facilitator = null;
        if (facilitatorId != null) {
            facilitator = staffMemberRepository.findById(facilitatorId)
                    .orElseThrow(() -> new RuntimeException("Staff member not found with id: " + facilitatorId));
        }

        BookClub club = BookClub.builder()
                .name(name)
                .description(description)
                .branch(branch)
                .facilitator(facilitator)
                .maxMembers(maxMembers)
                .meetingSchedule(meetingSchedule)
                .status(BookClubStatus.ACTIVE)
                .build();

        BookClub saved = clubRepository.save(club);
        log.info("Created book club '{}' at branch id={}", name, branchId);
        return toDTO(saved, 0);
    }

    @Transactional(readOnly = true)
    public BookClubDTO getClub(Long clubId) {
        BookClub club = clubRepository.findById(clubId)
                .orElseThrow(() -> new RuntimeException("Book club not found with id: " + clubId));
        int memberCount = (int) membershipRepository.countByClubIdAndActiveTrue(clubId);
        return toDTO(club, memberCount);
    }

    @Transactional(readOnly = true)
    public List<BookClubDTO> getClubsByBranch(Long branchId) {
        return clubRepository.findByBranchId(branchId).stream()
                .map(c -> toDTO(c, (int) membershipRepository.countByClubIdAndActiveTrue(c.getId())))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<BookClubDTO> searchClubs(String query) {
        return clubRepository.findByNameContainingIgnoreCase(query).stream()
                .map(c -> toDTO(c, (int) membershipRepository.countByClubIdAndActiveTrue(c.getId())))
                .collect(Collectors.toList());
    }

    @Transactional
    public BookClubMembership joinClub(Long clubId, Long memberId) {
        BookClub club = clubRepository.findById(clubId)
                .orElseThrow(() -> new RuntimeException("Book club not found with id: " + clubId));

        if (club.getStatus() != BookClubStatus.ACTIVE) {
            throw new RuntimeException("Club is not accepting new members");
        }

        long currentCount = membershipRepository.countByClubIdAndActiveTrue(clubId);
        if (currentCount >= club.getMaxMembers()) {
            throw new RuntimeException("Club is at maximum capacity");
        }

        membershipRepository.findByClubIdAndMemberId(clubId, memberId).ifPresent(m -> {
            if (m.isActive()) {
                throw new RuntimeException("Member is already in this club");
            }
        });

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Member not found with id: " + memberId));

        BookClubMembership membership = BookClubMembership.builder()
                .club(club)
                .member(member)
                .joinDate(LocalDate.now())
                .role(BookClubMemberRole.MEMBER)
                .active(true)
                .build();

        BookClubMembership saved = membershipRepository.save(membership);
        log.info("Member id={} joined book club id={}", memberId, clubId);
        return saved;
    }

    @Transactional
    public void leaveClub(Long clubId, Long memberId) {
        BookClubMembership membership = membershipRepository.findByClubIdAndMemberId(clubId, memberId)
                .orElseThrow(() -> new RuntimeException(
                        "Membership not found for clubId=" + clubId + " memberId=" + memberId));
        membership.setActive(false);
        membershipRepository.save(membership);
        log.info("Member id={} left book club id={}", memberId, clubId);
    }

    @Transactional
    public BookClubMeeting scheduleMeeting(Long clubId, LocalDateTime meetingDate, String location, Long bookId) {
        BookClub club = clubRepository.findById(clubId)
                .orElseThrow(() -> new RuntimeException("Book club not found with id: " + clubId));

        Book discussedBook = null;
        if (bookId != null) {
            discussedBook = bookRepository.findById(bookId)
                    .orElseThrow(() -> new RuntimeException("Book not found with id: " + bookId));
        }

        BookClubMeeting meeting = BookClubMeeting.builder()
                .club(club)
                .meetingDate(meetingDate)
                .location(location)
                .discussedBook(discussedBook)
                .build();

        BookClubMeeting saved = meetingRepository.save(meeting);
        log.info("Scheduled meeting for book club id={} on {}", clubId, meetingDate);
        return saved;
    }

    @Transactional(readOnly = true)
    public List<BookClubMeeting> getUpcomingMeetings(Long clubId) {
        return meetingRepository.findByClubIdAndMeetingDateAfterOrderByMeetingDateAsc(clubId, LocalDateTime.now());
    }

    @Transactional
    public DiscussionPostDTO postDiscussion(Long clubId, Long memberId, String content, Long parentDiscussionId) {
        BookClub club = clubRepository.findById(clubId)
                .orElseThrow(() -> new RuntimeException("Book club not found with id: " + clubId));

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Member not found with id: " + memberId));

        BookClubDiscussion parent = null;
        if (parentDiscussionId != null) {
            parent = discussionRepository.findById(parentDiscussionId)
                    .orElseThrow(() -> new RuntimeException("Discussion not found with id: " + parentDiscussionId));
        }

        BookClubDiscussion discussion = BookClubDiscussion.builder()
                .club(club)
                .poster(member)
                .content(content)
                .parentDiscussion(parent)
                .edited(false)
                .build();

        BookClubDiscussion saved = discussionRepository.save(discussion);
        log.info("Member id={} posted discussion in club id={}", memberId, clubId);
        return toDiscussionDTO(saved, 0);
    }

    @Transactional(readOnly = true)
    public List<DiscussionPostDTO> getDiscussions(Long clubId, Long meetingId) {
        List<BookClubDiscussion> discussions =
                discussionRepository.findByClubIdAndParentDiscussionIsNullOrderByPostedAtDesc(clubId);

        if (meetingId != null) {
            discussions = discussions.stream()
                    .filter(d -> d.getMeeting() != null && meetingId.equals(d.getMeeting().getId()))
                    .collect(Collectors.toList());
        }

        return discussions.stream()
                .map(d -> {
                    int replyCount = discussionRepository
                            .findByParentDiscussionIdOrderByPostedAtAsc(d.getId()).size();
                    return toDiscussionDTO(d, replyCount);
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<DiscussionPostDTO> getReplies(Long discussionId) {
        return discussionRepository.findByParentDiscussionIdOrderByPostedAtAsc(discussionId).stream()
                .map(d -> toDiscussionDTO(d, 0))
                .collect(Collectors.toList());
    }

    @Transactional
    public BookClubDTO setCurrentBook(Long clubId, Long bookId) {
        BookClub club = clubRepository.findById(clubId)
                .orElseThrow(() -> new RuntimeException("Book club not found with id: " + clubId));
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found with id: " + bookId));
        club.setCurrentBook(book);
        BookClub saved = clubRepository.save(club);
        log.info("Set current book id={} for book club id={}", bookId, clubId);
        return toDTO(saved, (int) membershipRepository.countByClubIdAndActiveTrue(clubId));
    }

    // --- Private helpers ---

    BookClubDTO toDTO(BookClub club, int memberCount) {
        String facilitatorName = null;
        Long facilitatorId = null;
        if (club.getFacilitator() != null) {
            facilitatorId = club.getFacilitator().getId();
            com.example.catalog.model.User user = club.getFacilitator().getUser();
            if (user != null) {
                facilitatorName = user.getFirstName() + " " + user.getLastName();
            }
        }

        Long currentBookId = null;
        String currentBookTitle = null;
        if (club.getCurrentBook() != null) {
            currentBookId = club.getCurrentBook().getId();
            currentBookTitle = club.getCurrentBook().getTitle();
        }

        return BookClubDTO.builder()
                .id(club.getId())
                .name(club.getName())
                .description(club.getDescription())
                .branchId(club.getBranch().getId())
                .branchName(club.getBranch().getName())
                .facilitatorId(facilitatorId)
                .facilitatorName(facilitatorName)
                .maxMembers(club.getMaxMembers())
                .currentMemberCount(memberCount)
                .meetingSchedule(club.getMeetingSchedule())
                .currentBookId(currentBookId)
                .currentBookTitle(currentBookTitle)
                .status(club.getStatus())
                .createdAt(club.getCreatedAt())
                .build();
    }

    DiscussionPostDTO toDiscussionDTO(BookClubDiscussion d, int replyCount) {
        String posterName = null;
        if (d.getPoster() != null && d.getPoster().getUser() != null) {
            com.example.catalog.model.User user = d.getPoster().getUser();
            posterName = user.getFirstName() + " " + user.getLastName();
        }

        Long meetingId = d.getMeeting() != null ? d.getMeeting().getId() : null;
        Long parentId = d.getParentDiscussion() != null ? d.getParentDiscussion().getId() : null;

        return DiscussionPostDTO.builder()
                .id(d.getId())
                .clubId(d.getClub().getId())
                .meetingId(meetingId)
                .posterId(d.getPoster() != null ? d.getPoster().getId() : null)
                .posterName(posterName)
                .content(d.getContent())
                .postedAt(d.getPostedAt())
                .parentDiscussionId(parentId)
                .edited(d.isEdited())
                .editedAt(d.getEditedAt())
                .replyCount(replyCount)
                .build();
    }

    BookClubMeetingDTO toMeetingDTO(BookClubMeeting m) {
        Long discussedBookId = m.getDiscussedBook() != null ? m.getDiscussedBook().getId() : null;
        String discussedBookTitle = m.getDiscussedBook() != null ? m.getDiscussedBook().getTitle() : null;

        return BookClubMeetingDTO.builder()
                .id(m.getId())
                .clubId(m.getClub().getId())
                .discussedBookId(discussedBookId)
                .discussedBookTitle(discussedBookTitle)
                .meetingDate(m.getMeetingDate())
                .location(m.getLocation())
                .notes(m.getNotes())
                .attendanceCount(m.getAttendanceCount())
                .createdAt(m.getCreatedAt())
                .build();
    }
}
