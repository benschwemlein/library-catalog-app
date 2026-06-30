package com.example.library.recommendation;

import com.example.library.entity.Author;
import com.example.library.entity.Book;
import com.example.library.entity.BookCopy;
import com.example.library.entity.Genre;
import com.example.library.entity.Loan;
import com.example.library.entity.LoanStatus;
import com.example.library.entity.Member;
import com.example.library.entity.MembershipTier;
import com.example.library.repository.BookRepository;
import com.example.library.repository.LoanRepository;
import com.example.library.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class ContentBasedFilteringServiceTest {

    @Mock
    private LoanRepository loanRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private ContentBasedFilteringService service;

    private Member member;
    private Genre scifiGenre;
    private Genre mysteryGenre;
    private Author author1;
    private Author author2;

    @BeforeEach
    void setUp() {
        member = Member.builder()
                .id(1L)
                .membershipNumber("M001")
                .membershipTier(MembershipTier.STANDARD)
                .active(true)
                .expiryDate(LocalDate.now().plusYears(1))
                .fineBalance(BigDecimal.ZERO)
                .build();

        scifiGenre = Genre.builder().id(1L).name("Science Fiction").build();
        mysteryGenre = Genre.builder().id(2L).name("Mystery").build();
        author1 = Author.builder().id(10L).firstName("Isaac").lastName("Asimov").build();
        author2 = Author.builder().id(20L).firstName("Agatha").lastName("Christie").build();
    }

    private Book buildBook(Long id, String title, int year, Set<Genre> genres, Set<Author> authors) {
        return Book.builder()
                .id(id)
                .title(title)
                .isbn("978-" + id)
                .publicationYear(year)
                .genres(genres)
                .authors(authors)
                .copies(Collections.emptyList())
                .build();
    }

    private Loan buildLoan(Long id, Member m, Book book) {
        BookCopy copy = BookCopy.builder().id(id * 100).book(book).build();
        return Loan.builder()
                .id(id)
                .member(m)
                .bookCopy(copy)
                .checkoutDate(LocalDateTime.now().minusDays(14))
                .dueDate(LocalDateTime.now().minusDays(7))
                .build();
    }

    @Test
    void buildProfile_memberWithHistory_buildsCorrectProfile() {
        Book borrowedBook = buildBook(1L, "Foundation", 1951, Set.of(scifiGenre), Set.of(author1));
        Loan loan = buildLoan(1L, member, borrowedBook);

        when(loanRepository.findByMember_IdAndStatus(member.getId(), LoanStatus.ACTIVE))
                .thenReturn(List.of(loan));
        when(loanRepository.findByMember_IdAndStatus(member.getId(), LoanStatus.RETURNED))
                .thenReturn(Collections.emptyList());

        MemberBorrowingProfile profile = service.buildProfile(member);

        assertThat(profile.getMemberId()).isEqualTo(member.getId());
        assertThat(profile.getGenreFrequency()).containsKey("Science Fiction");
        assertThat(profile.getAuthorFrequency()).containsKey(author1.getId());
        assertThat(profile.getBorrowedBookIds()).contains(borrowedBook.getId());
    }

    @Test
    void buildProfile_memberWithNoHistory_returnsEmptyProfile() {
        when(loanRepository.findByMember_IdAndStatus(member.getId(), LoanStatus.ACTIVE))
                .thenReturn(Collections.emptyList());
        when(loanRepository.findByMember_IdAndStatus(member.getId(), LoanStatus.RETURNED))
                .thenReturn(Collections.emptyList());

        MemberBorrowingProfile profile = service.buildProfile(member);

        assertThat(profile.getGenreFrequency()).isEmpty();
        assertThat(profile.getAuthorFrequency()).isEmpty();
        assertThat(profile.getBorrowedBookIds()).isEmpty();
    }

    @Test
    void recommend_byGenreAffinity_returnsGenreMatches() {
        // Profile: loves sci-fi
        MemberBorrowingProfile profile = MemberBorrowingProfile.builder()
                .memberId(member.getId())
                .genreFrequency(Map.of("Science Fiction", 5))
                .authorFrequency(Collections.emptyMap())
                .borrowedBookIds(Set.of(1L))
                .builtAt(LocalDateTime.now())
                .build();

        Book scifiBook = buildBook(2L, "Dune", 1965, Set.of(scifiGenre), new HashSet<>());
        Book mysteryBook = buildBook(3L, "And Then There Were None", 1939, Set.of(mysteryGenre), new HashSet<>());

        when(bookRepository.findAll()).thenReturn(List.of(scifiBook, mysteryBook));

        List<Book> result = service.recommend(profile, 5);

        assertThat(result).isNotEmpty();
        assertThat(result).contains(scifiBook);
        assertThat(result).doesNotContain(mysteryBook); // no genre match
    }

    @Test
    void recommend_byAuthorAffinity_returnsAuthorMatches() {
        MemberBorrowingProfile profile = MemberBorrowingProfile.builder()
                .memberId(member.getId())
                .genreFrequency(Collections.emptyMap())
                .authorFrequency(Map.of(author1.getId(), 3))
                .borrowedBookIds(Set.of(1L))
                .builtAt(LocalDateTime.now())
                .build();

        Book authorBook = buildBook(2L, "I Robot", 1950, new HashSet<>(), Set.of(author1));
        Book otherBook = buildBook(3L, "Unknown", 2000, new HashSet<>(), Set.of(author2));

        when(bookRepository.findAll()).thenReturn(List.of(authorBook, otherBook));

        List<Book> result = service.recommend(profile, 5);

        assertThat(result).contains(authorBook);
        assertThat(result).doesNotContain(otherBook); // author not in profile
    }

    @Test
    void recommend_excludesAlreadyBorrowedBooks() {
        Book alreadyBorrowed = buildBook(1L, "Already Read", 2000, Set.of(scifiGenre), new HashSet<>());
        Book newBook = buildBook(2L, "New Book", 2010, Set.of(scifiGenre), new HashSet<>());

        MemberBorrowingProfile profile = MemberBorrowingProfile.builder()
                .memberId(member.getId())
                .genreFrequency(Map.of("Science Fiction", 3))
                .authorFrequency(Collections.emptyMap())
                .borrowedBookIds(Set.of(alreadyBorrowed.getId()))
                .builtAt(LocalDateTime.now())
                .build();

        when(bookRepository.findAll()).thenReturn(List.of(alreadyBorrowed, newBook));

        List<Book> result = service.recommend(profile, 5);

        assertThat(result).doesNotContain(alreadyBorrowed);
        assertThat(result).contains(newBook);
    }

    @Test
    void recommend_memberWithNoHistory_returnsEmpty() {
        MemberBorrowingProfile profile = MemberBorrowingProfile.builder()
                .memberId(member.getId())
                .genreFrequency(Collections.emptyMap())
                .authorFrequency(Collections.emptyMap())
                .borrowedBookIds(Collections.emptySet())
                .builtAt(LocalDateTime.now())
                .build();

        Book book = buildBook(1L, "Some Book", 2020, Set.of(scifiGenre), new HashSet<>());
        when(bookRepository.findAll()).thenReturn(List.of(book));

        List<Book> result = service.recommend(profile, 5);

        // No genre or author affinity → no score above 0 → empty
        assertThat(result).isEmpty();
    }

    @Test
    void scoreBook_sameGenre_scoresHigherThanDifferentGenre() {
        MemberBorrowingProfile profile = MemberBorrowingProfile.builder()
                .memberId(member.getId())
                .genreFrequency(Map.of("Science Fiction", 5))
                .authorFrequency(Collections.emptyMap())
                .borrowedBookIds(Collections.emptySet())
                .builtAt(LocalDateTime.now())
                .build();

        Book scifiBook = buildBook(1L, "Sci-Fi Classic", 2000, Set.of(scifiGenre), new HashSet<>());
        Book mysteryBook = buildBook(2L, "Whodunit", 2000, Set.of(mysteryGenre), new HashSet<>());

        when(bookRepository.findAll()).thenReturn(List.of(scifiBook, mysteryBook));

        List<Book> result = service.recommend(profile, 5);

        assertThat(result).startsWith(scifiBook);
        assertThat(result).doesNotContain(mysteryBook);
    }

    @Test
    void recencyTiebreaker_newerBook_rankedHigher() {
        MemberBorrowingProfile profile = MemberBorrowingProfile.builder()
                .memberId(member.getId())
                .genreFrequency(Map.of("Science Fiction", 3))
                .authorFrequency(Collections.emptyMap())
                .borrowedBookIds(Collections.emptySet())
                .builtAt(LocalDateTime.now())
                .build();

        // Both sci-fi but different years
        Book olderBook = buildBook(1L, "Old Sci-Fi", 1985, Set.of(scifiGenre), new HashSet<>());
        Book newerBook = buildBook(2L, "New Sci-Fi", 2020, Set.of(scifiGenre), new HashSet<>());

        when(bookRepository.findAll()).thenReturn(List.of(olderBook, newerBook));

        List<Book> result = service.recommend(profile, 5);

        assertThat(result).hasSize(2);
        // Newer book should rank higher due to publication year bonus
        assertThat(result.get(0)).isEqualTo(newerBook);
    }

    @Test
    void recommend_returnsUpToLimit() {
        MemberBorrowingProfile profile = MemberBorrowingProfile.builder()
                .memberId(member.getId())
                .genreFrequency(Map.of("Science Fiction", 3))
                .authorFrequency(Collections.emptyMap())
                .borrowedBookIds(Collections.emptySet())
                .builtAt(LocalDateTime.now())
                .build();

        List<Book> manyBooks = List.of(
                buildBook(1L, "Book A", 2000, Set.of(scifiGenre), new HashSet<>()),
                buildBook(2L, "Book B", 2001, Set.of(scifiGenre), new HashSet<>()),
                buildBook(3L, "Book C", 2002, Set.of(scifiGenre), new HashSet<>()),
                buildBook(4L, "Book D", 2003, Set.of(scifiGenre), new HashSet<>()),
                buildBook(5L, "Book E", 2004, Set.of(scifiGenre), new HashSet<>())
        );

        when(bookRepository.findAll()).thenReturn(manyBooks);

        List<Book> result = service.recommend(profile, 3);

        assertThat(result).hasSize(3);
    }

    @Test
    void recommend_multipleGenreAndAuthorMatch_scoresHighest() {
        MemberBorrowingProfile profile = MemberBorrowingProfile.builder()
                .memberId(member.getId())
                .genreFrequency(Map.of("Science Fiction", 5))
                .authorFrequency(Map.of(author1.getId(), 3))
                .borrowedBookIds(Collections.emptySet())
                .builtAt(LocalDateTime.now())
                .build();

        // Book A: matching genre only (+3)
        Book genreOnlyBook = buildBook(1L, "Genre Match", 2000, Set.of(scifiGenre), new HashSet<>());
        // Book B: matching genre and author (+3+2=5)
        Book genreAndAuthorBook = buildBook(2L, "Full Match", 2000, Set.of(scifiGenre), Set.of(author1));

        when(bookRepository.findAll()).thenReturn(List.of(genreOnlyBook, genreAndAuthorBook));

        List<Book> result = service.recommend(profile, 5);

        assertThat(result).hasSize(2);
        assertThat(result.get(0)).isEqualTo(genreAndAuthorBook); // higher combined score
    }

    @Test
    void buildProfile_combinesActiveAndReturnedLoans() {
        Book activeBook = buildBook(1L, "Active Loan Book", 2000, Set.of(scifiGenre), Set.of(author1));
        Book returnedBook = buildBook(2L, "Returned Book", 2010, Set.of(mysteryGenre), Set.of(author2));

        Loan activeLoan = buildLoan(1L, member, activeBook);
        Loan returnedLoan = buildLoan(2L, member, returnedBook);

        when(loanRepository.findByMember_IdAndStatus(member.getId(), LoanStatus.ACTIVE))
                .thenReturn(List.of(activeLoan));
        when(loanRepository.findByMember_IdAndStatus(member.getId(), LoanStatus.RETURNED))
                .thenReturn(List.of(returnedLoan));

        MemberBorrowingProfile profile = service.buildProfile(member);

        assertThat(profile.getBorrowedBookIds()).containsExactlyInAnyOrder(1L, 2L);
        assertThat(profile.getGenreFrequency()).containsKeys("Science Fiction", "Mystery");
        assertThat(profile.getAuthorFrequency()).containsKeys(author1.getId(), author2.getId());
    }

    @Test
    void recommend_noBooks_returnsEmpty() {
        MemberBorrowingProfile profile = MemberBorrowingProfile.builder()
                .memberId(member.getId())
                .genreFrequency(Map.of("Science Fiction", 3))
                .authorFrequency(Collections.emptyMap())
                .borrowedBookIds(Collections.emptySet())
                .builtAt(LocalDateTime.now())
                .build();

        when(bookRepository.findAll()).thenReturn(Collections.emptyList());

        List<Book> result = service.recommend(profile, 5);

        assertThat(result).isEmpty();
    }

    @Test
    void recommend_allBooksAlreadyBorrowed_returnsEmpty() {
        Book book1 = buildBook(1L, "Borrowed", 2000, Set.of(scifiGenre), new HashSet<>());
        Book book2 = buildBook(2L, "Also Borrowed", 2010, Set.of(scifiGenre), new HashSet<>());

        MemberBorrowingProfile profile = MemberBorrowingProfile.builder()
                .memberId(member.getId())
                .genreFrequency(Map.of("Science Fiction", 5))
                .authorFrequency(Collections.emptyMap())
                .borrowedBookIds(Set.of(1L, 2L))
                .builtAt(LocalDateTime.now())
                .build();

        when(bookRepository.findAll()).thenReturn(List.of(book1, book2));

        List<Book> result = service.recommend(profile, 5);

        assertThat(result).isEmpty();
    }
}
