package com.example.library;

import com.example.catalog.model.User;
import com.example.library.entity.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * Static factory methods for creating fully-populated test entities.
 * All entities use realistic data and Lombok builder patterns where available.
 */
public class TestDataFactory {

    // -------------------------------------------------------------------------
    // User (catalog model)
    // -------------------------------------------------------------------------

    public static User createUser() {
        return User.builder()
                .id(1L)
                .firstName("Jane")
                .lastName("Doe")
                .email("jane.doe@example.com")
                .password("$2a$10$hashed.password.here")
                .role(com.example.catalog.model.Role.USER)
                .build();
    }

    // -------------------------------------------------------------------------
    // Author
    // -------------------------------------------------------------------------

    public static Author createAuthor() {
        return Author.builder()
                .id(1L)
                .firstName("George")
                .lastName("Orwell")
                .bio("Eric Arthur Blair, known by his pen name George Orwell, was an English novelist and essayist.")
                .birthDate(LocalDate.of(1903, 6, 25))
                .nationality("British")
                .books(new HashSet<>())
                .build();
    }

    // -------------------------------------------------------------------------
    // Genre
    // -------------------------------------------------------------------------

    public static Genre createGenre() {
        return Genre.builder()
                .id(1L)
                .name("Dystopian Fiction")
                .description("A genre exploring oppressive, imagined futures.")
                .build();
    }

    // -------------------------------------------------------------------------
    // Publisher
    // -------------------------------------------------------------------------

    public static Publisher createPublisher() {
        return Publisher.builder()
                .id(1L)
                .name("Secker and Warburg")
                .address("20 Vauxhall Bridge Road")
                .city("London")
                .country("United Kingdom")
                .website("https://www.penguinrandomhouse.co.uk")
                .build();
    }

    // -------------------------------------------------------------------------
    // Book
    // -------------------------------------------------------------------------

    public static Book createBook() {
        Author author = createAuthor();
        Genre genre = createGenre();
        Publisher publisher = createPublisher();

        HashSet<Author> authors = new HashSet<>();
        authors.add(author);

        HashSet<Genre> genres = new HashSet<>();
        genres.add(genre);

        return Book.builder()
                .id(1L)
                .isbn("978-0-452-28423-4")
                .title("Nineteen Eighty-Four")
                .subtitle("A Novel")
                .description("A dystopian social science fiction novel set in a totalitarian society.")
                .publicationYear(1949)
                .pageCount(328)
                .language("English")
                .coverImageUrl("https://covers.example.com/1984.jpg")
                .authors(authors)
                .publisher(publisher)
                .genres(genres)
                .subjects(new HashSet<>())
                .copies(new ArrayList<>())
                .reviews(new ArrayList<>())
                .createdAt(LocalDateTime.of(2024, 1, 15, 10, 0))
                .updatedAt(LocalDateTime.of(2024, 6, 1, 12, 0))
                .build();
    }

    // -------------------------------------------------------------------------
    // LibraryBranch
    // -------------------------------------------------------------------------

    public static LibraryBranch createBranch() {
        return LibraryBranch.builder()
                .id(1L)
                .name("Central Library")
                .address("100 Main Street")
                .city("Springfield")
                .phone("555-0100")
                .email("central@library.example.com")
                .openingHours("Mon-Fri 9am-8pm, Sat 10am-6pm, Sun 12pm-5pm")
                .copies(new ArrayList<>())
                .staff(new ArrayList<>())
                .build();
    }

    // -------------------------------------------------------------------------
    // Member
    // -------------------------------------------------------------------------

    public static Member createMember() {
        return Member.builder()
                .id(1L)
                .user(createUser())
                .membershipNumber("MEM-2024-00001")
                .membershipTier(MembershipTier.STANDARD)
                .joinDate(LocalDate.of(2024, 1, 10))
                .expiryDate(LocalDate.now().plusYears(1))
                .fineBalance(BigDecimal.ZERO)
                .loans(new ArrayList<>())
                .holds(new ArrayList<>())
                .active(true)
                .build();
    }

    public static Member createPremiumMember() {
        return Member.builder()
                .id(2L)
                .user(createUser())
                .membershipNumber("MEM-2024-00002")
                .membershipTier(MembershipTier.PREMIUM)
                .joinDate(LocalDate.of(2023, 6, 1))
                .expiryDate(LocalDate.now().plusYears(2))
                .fineBalance(BigDecimal.ZERO)
                .loans(new ArrayList<>())
                .holds(new ArrayList<>())
                .active(true)
                .build();
    }

    public static Member createStudentMember() {
        return Member.builder()
                .id(3L)
                .user(createUser())
                .membershipNumber("MEM-2024-00003")
                .membershipTier(MembershipTier.STUDENT)
                .joinDate(LocalDate.of(2024, 9, 1))
                .expiryDate(LocalDate.now().plusMonths(10))
                .fineBalance(BigDecimal.ZERO)
                .loans(new ArrayList<>())
                .holds(new ArrayList<>())
                .active(true)
                .build();
    }

    // -------------------------------------------------------------------------
    // BookCopy
    // -------------------------------------------------------------------------

    public static BookCopy createBookCopy() {
        return BookCopy.builder()
                .id(1L)
                .book(createBook())
                .branch(createBranch())
                .barcode("BC-001-1984-01")
                .condition(CopyCondition.GOOD)
                .status(CopyStatus.AVAILABLE)
                .acquiredDate(LocalDate.of(2024, 1, 20))
                .loans(new ArrayList<>())
                .build();
    }

    public static BookCopy createCheckedOutCopy() {
        return BookCopy.builder()
                .id(2L)
                .book(createBook())
                .branch(createBranch())
                .barcode("BC-001-1984-02")
                .condition(CopyCondition.FAIR)
                .status(CopyStatus.CHECKED_OUT)
                .acquiredDate(LocalDate.of(2023, 5, 10))
                .loans(new ArrayList<>())
                .build();
    }

    // -------------------------------------------------------------------------
    // Loan
    // -------------------------------------------------------------------------

    public static Loan createLoan() {
        Member member = createMember();
        BookCopy copy = createBookCopy();
        LibraryBranch branch = createBranch();

        return Loan.builder()
                .id(1L)
                .bookCopy(copy)
                .member(member)
                .branch(branch)
                .checkoutDate(LocalDateTime.now().minusDays(7))
                .dueDate(LocalDateTime.now().plusDays(14))
                .returnDate(null)
                .renewalCount(0)
                .status(LoanStatus.ACTIVE)
                .build();
    }

    public static Loan createOverdueLoan() {
        Member member = createMember();
        BookCopy copy = createBookCopy();
        LibraryBranch branch = createBranch();

        return Loan.builder()
                .id(2L)
                .bookCopy(copy)
                .member(member)
                .branch(branch)
                .checkoutDate(LocalDateTime.now().minusDays(30))
                .dueDate(LocalDateTime.now().minusDays(9))
                .returnDate(null)
                .renewalCount(0)
                .status(LoanStatus.OVERDUE)
                .build();
    }

    public static Loan createReturnedLoan() {
        Member member = createMember();
        BookCopy copy = createBookCopy();
        LibraryBranch branch = createBranch();

        return Loan.builder()
                .id(3L)
                .bookCopy(copy)
                .member(member)
                .branch(branch)
                .checkoutDate(LocalDateTime.now().minusDays(20))
                .dueDate(LocalDateTime.now().minusDays(7))
                .returnDate(LocalDateTime.now().minusDays(8))
                .renewalCount(1)
                .status(LoanStatus.RETURNED)
                .build();
    }

    // -------------------------------------------------------------------------
    // Fine
    // -------------------------------------------------------------------------

    public static Fine createFine() {
        Loan loan = createOverdueLoan();
        Member member = loan.getMember();

        return Fine.builder()
                .id(1L)
                .loan(loan)
                .member(member)
                .amount(new BigDecimal("2.25"))
                .reason("Overdue: Nineteen Eighty-Four")
                .issuedDate(LocalDateTime.now().minusDays(2))
                .paidDate(null)
                .waived(false)
                .waivedBy(null)
                .waivedReason(null)
                .build();
    }

    public static Fine createPaidFine() {
        Loan loan = createReturnedLoan();
        Member member = loan.getMember();

        return Fine.builder()
                .id(2L)
                .loan(loan)
                .member(member)
                .amount(new BigDecimal("1.75"))
                .reason("Overdue: Nineteen Eighty-Four")
                .issuedDate(LocalDateTime.now().minusDays(10))
                .paidDate(LocalDateTime.now().minusDays(5))
                .waived(false)
                .waivedBy(null)
                .waivedReason(null)
                .build();
    }

    // -------------------------------------------------------------------------
    // Hold
    // -------------------------------------------------------------------------

    public static Hold createHold() {
        Book book = createBook();
        Member member = createMember();
        LibraryBranch branch = createBranch();

        return Hold.builder()
                .id(1L)
                .book(book)
                .member(member)
                .requestDate(LocalDateTime.now().minusDays(3))
                .expiryDate(null)
                .status(HoldStatus.PENDING)
                .notifiedDate(null)
                .pickupBranch(branch)
                .build();
    }

    public static Hold createReadyHold() {
        Book book = createBook();
        Member member = createMember();
        LibraryBranch branch = createBranch();

        return Hold.builder()
                .id(2L)
                .book(book)
                .member(member)
                .requestDate(LocalDateTime.now().minusDays(10))
                .expiryDate(LocalDateTime.now().plusDays(4))
                .status(HoldStatus.READY)
                .notifiedDate(LocalDateTime.now().minusDays(3))
                .pickupBranch(branch)
                .build();
    }
}
