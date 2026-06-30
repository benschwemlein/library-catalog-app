package com.example.library.recommendation;

import com.example.library.entity.Book;
import com.example.library.entity.BookCopy;
import com.example.library.entity.Loan;
import com.example.library.entity.Member;
import com.example.library.entity.MembershipTier;
import com.example.library.repository.BookRepository;
import com.example.library.repository.LoanRepository;
import com.example.library.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CollaborativeFilteringServiceTest {

    @Mock
    private LoanRepository loanRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private CollaborativeFilteringService service;

    private Member targetMember;
    private Member similarMember;
    private Member dissimilarMember;

    private Book book1;
    private Book book2;
    private Book book3;
    private Book book4;

    @BeforeEach
    void setUp() {
        targetMember = buildMember(1L, "M001");
        similarMember = buildMember(2L, "M002");
        dissimilarMember = buildMember(3L, "M003");

        book1 = buildBook(10L, "Book One");
        book2 = buildBook(20L, "Book Two");
        book3 = buildBook(30L, "Book Three");
        book4 = buildBook(40L, "Book Four");
    }

    private Member buildMember(Long id, String memberNumber) {
        return Member.builder()
                .id(id)
                .membershipNumber(memberNumber)
                .membershipTier(MembershipTier.STANDARD)
                .active(true)
                .expiryDate(LocalDate.now().plusYears(1))
                .fineBalance(BigDecimal.ZERO)
                .build();
    }

    private Book buildBook(Long id, String title) {
        return Book.builder()
                .id(id)
                .title(title)
                .isbn("978-" + id)
                .publicationYear(2020)
                .authors(new HashSet<>())
                .genres(new HashSet<>())
                .copies(Collections.emptyList())
                .build();
    }

    private Loan buildLoan(Long id, Member member, Book book) {
        BookCopy copy = BookCopy.builder().id(id * 100).book(book).build();
        return Loan.builder()
                .id(id)
                .member(member)
                .bookCopy(copy)
                .checkoutDate(LocalDateTime.now().minusDays(7))
                .dueDate(LocalDateTime.now().plusDays(7))
                .build();
    }

    @Test
    void findSimilarMembers_memberWithHistory_returnsOverlappingMembers() {
        // Target borrowed book1 and book2; similarMember also borrowed both (overlap >= 2)
        List<Loan> targetLoans = List.of(buildLoan(1L, targetMember, book1), buildLoan(2L, targetMember, book2));
        List<Loan> similarLoans = List.of(buildLoan(3L, similarMember, book1), buildLoan(4L, similarMember, book2));

        when(loanRepository.findByMember_Id(targetMember.getId())).thenReturn(targetLoans);
        when(memberRepository.findAll()).thenReturn(List.of(similarMember, dissimilarMember));
        when(loanRepository.findByMember_Id(similarMember.getId())).thenReturn(similarLoans);
        when(loanRepository.findByMember_Id(dissimilarMember.getId())).thenReturn(Collections.emptyList());

        List<Member> result = service.findSimilarMembers(targetMember, 5);

        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(similarMember);
    }

    @Test
    void findSimilarMembers_memberWithNoHistory_returnsEmptyList() {
        when(loanRepository.findByMember_Id(targetMember.getId())).thenReturn(Collections.emptyList());

        List<Member> result = service.findSimilarMembers(targetMember, 5);

        assertThat(result).isEmpty();
        verify(memberRepository, never()).findAll();
    }

    @Test
    void findSimilarMembers_excludesMemberSelf() {
        List<Loan> targetLoans = List.of(buildLoan(1L, targetMember, book1), buildLoan(2L, targetMember, book2));

        when(loanRepository.findByMember_Id(targetMember.getId())).thenReturn(targetLoans);
        when(memberRepository.findAll()).thenReturn(List.of(targetMember, similarMember));
        when(loanRepository.findByMember_Id(similarMember.getId())).thenReturn(Collections.emptyList());

        List<Member> result = service.findSimilarMembers(targetMember, 5);

        assertThat(result).doesNotContain(targetMember);
    }

    @Test
    void findSimilarMembers_minimumOverlapRequired() {
        // Target borrowed book1 and book2; other member only borrowed book1 (overlap = 1 < MIN_OVERLAP=2)
        List<Loan> targetLoans = List.of(buildLoan(1L, targetMember, book1), buildLoan(2L, targetMember, book2));
        List<Loan> partialLoans = List.of(buildLoan(3L, similarMember, book1)); // only 1 overlap

        when(loanRepository.findByMember_Id(targetMember.getId())).thenReturn(targetLoans);
        when(memberRepository.findAll()).thenReturn(List.of(similarMember));
        when(loanRepository.findByMember_Id(similarMember.getId())).thenReturn(partialLoans);

        List<Member> result = service.findSimilarMembers(targetMember, 5);

        assertThat(result).isEmpty();
    }

    @Test
    void recommend_excludesAlreadyBorrowedBooks() {
        Set<Long> alreadyBorrowed = Set.of(book1.getId(), book2.getId());
        List<Loan> similarLoans = List.of(
                buildLoan(1L, similarMember, book1), // already borrowed
                buildLoan(2L, similarMember, book3)  // new book
        );

        when(loanRepository.findByMember_Id(similarMember.getId())).thenReturn(similarLoans);
        when(bookRepository.findById(book3.getId())).thenReturn(Optional.of(book3));

        List<Book> result = service.recommend(targetMember, List.of(similarMember), alreadyBorrowed, 5);

        assertThat(result).containsExactly(book3);
        assertThat(result).doesNotContain(book1, book2);
    }

    @Test
    void recommend_noPeers_returnsEmptyList() {
        List<Book> result = service.recommend(targetMember, Collections.emptyList(), Collections.emptySet(), 5);

        assertThat(result).isEmpty();
    }

    @Test
    void scoreBooks_ranksHigherOverlap_first() {
        // book3 is borrowed by 2 similar members; book4 by 1 → book3 should rank higher
        Member anotherMember = buildMember(4L, "M004");
        List<Loan> member2Loans = List.of(buildLoan(1L, similarMember, book3), buildLoan(2L, similarMember, book4));
        List<Loan> member4Loans = List.of(buildLoan(3L, anotherMember, book3)); // only book3

        when(loanRepository.findByMember_Id(similarMember.getId())).thenReturn(member2Loans);
        when(loanRepository.findByMember_Id(anotherMember.getId())).thenReturn(member4Loans);
        when(bookRepository.findById(book3.getId())).thenReturn(Optional.of(book3));
        when(bookRepository.findById(book4.getId())).thenReturn(Optional.of(book4));

        List<Book> result = service.recommend(
                targetMember,
                List.of(similarMember, anotherMember),
                Collections.emptySet(),
                5
        );

        assertThat(result).hasSize(2);
        assertThat(result.get(0)).isEqualTo(book3); // higher score
        assertThat(result.get(1)).isEqualTo(book4);
    }

    @Test
    void recommend_limitsResultsToRequestedCount() {
        // Similar member has 4 books, but we only want 2
        List<Loan> similarLoans = List.of(
                buildLoan(1L, similarMember, book1),
                buildLoan(2L, similarMember, book2),
                buildLoan(3L, similarMember, book3),
                buildLoan(4L, similarMember, book4)
        );

        when(loanRepository.findByMember_Id(similarMember.getId())).thenReturn(similarLoans);
        when(bookRepository.findById(anyLong())).thenAnswer(inv -> {
            Long id = inv.getArgument(0);
            if (id.equals(book1.getId())) return Optional.of(book1);
            if (id.equals(book2.getId())) return Optional.of(book2);
            if (id.equals(book3.getId())) return Optional.of(book3);
            if (id.equals(book4.getId())) return Optional.of(book4);
            return Optional.empty();
        });

        List<Book> result = service.recommend(targetMember, List.of(similarMember), Collections.emptySet(), 2);

        assertThat(result).hasSize(2);
    }

    @Test
    void findSimilarMembers_limitsResultsToRequestedCount() {
        Member m2 = buildMember(2L, "M002");
        Member m3 = buildMember(3L, "M003");
        Member m4 = buildMember(4L, "M004");

        List<Loan> targetLoans = List.of(
                buildLoan(1L, targetMember, book1),
                buildLoan(2L, targetMember, book2),
                buildLoan(3L, targetMember, book3)
        );
        // All 3 peers have overlap >= 2 with target
        List<Loan> commonLoans = List.of(buildLoan(10L, m2, book1), buildLoan(11L, m2, book2));

        when(loanRepository.findByMember_Id(targetMember.getId())).thenReturn(targetLoans);
        when(memberRepository.findAll()).thenReturn(List.of(m2, m3, m4));
        when(loanRepository.findByMember_Id(m2.getId())).thenReturn(commonLoans);
        when(loanRepository.findByMember_Id(m3.getId())).thenReturn(
                List.of(buildLoan(12L, m3, book1), buildLoan(13L, m3, book3)));
        when(loanRepository.findByMember_Id(m4.getId())).thenReturn(
                List.of(buildLoan(14L, m4, book2), buildLoan(15L, m4, book3)));

        List<Member> result = service.findSimilarMembers(targetMember, 2);

        assertThat(result).hasSize(2);
    }

    @Test
    void recommend_withSinglePeer_returnsTheirUniqueBooks() {
        Set<Long> alreadyBorrowed = Set.of(book1.getId());
        List<Loan> peerLoans = List.of(
                buildLoan(1L, similarMember, book2),
                buildLoan(2L, similarMember, book3)
        );

        when(loanRepository.findByMember_Id(similarMember.getId())).thenReturn(peerLoans);
        when(bookRepository.findById(book2.getId())).thenReturn(Optional.of(book2));
        when(bookRepository.findById(book3.getId())).thenReturn(Optional.of(book3));

        List<Book> result = service.recommend(targetMember, List.of(similarMember), alreadyBorrowed, 10);

        assertThat(result).hasSize(2);
        assertThat(result).containsExactlyInAnyOrder(book2, book3);
    }

    @Test
    void findSimilarMembers_allMembersExcluded_returnsEmpty() {
        List<Loan> targetLoans = List.of(buildLoan(1L, targetMember, book1), buildLoan(2L, targetMember, book2));

        when(loanRepository.findByMember_Id(targetMember.getId())).thenReturn(targetLoans);
        // No other members exist (besides target, but target is excluded)
        when(memberRepository.findAll()).thenReturn(List.of(targetMember));

        List<Member> result = service.findSimilarMembers(targetMember, 5);

        assertThat(result).isEmpty();
    }

    @Test
    void recommend_bookNotFoundInRepository_excludedFromResults() {
        List<Loan> peerLoans = List.of(buildLoan(1L, similarMember, book1));

        when(loanRepository.findByMember_Id(similarMember.getId())).thenReturn(peerLoans);
        // Book not found in repository
        when(bookRepository.findById(book1.getId())).thenReturn(Optional.empty());

        List<Book> result = service.recommend(targetMember, List.of(similarMember), Collections.emptySet(), 5);

        assertThat(result).isEmpty();
    }
}
