package com.example.catalog;

import com.example.catalog.model.Role;
import com.example.catalog.model.User;
import com.example.catalog.repo.UserRepository;
import com.example.library.bookclub.BookClub;
import com.example.library.bookclub.BookClubRepository;
import com.example.library.bookclub.BookClubStatus;
import com.example.library.circulation.CirculationRule;
import com.example.library.circulation.CirculationRuleRepository;
import com.example.library.circulation.ItemType;
import com.example.library.entity.*;
import com.example.library.digitalresource.DigitalLicense;
import com.example.library.digitalresource.DigitalLicenseRepository;
import com.example.library.digitalresource.DigitalResource;
import com.example.library.digitalresource.DigitalResourceFormat;
import com.example.library.digitalresource.DigitalResourceRepository;
import com.example.library.digitalresource.DigitalResourceType;
import com.example.library.digitalresource.LicenseType;
import com.example.library.readingchallenge.ReadingChallenge;
import com.example.library.readingchallenge.ReadingChallengeRepository;
import com.example.library.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Component
@Order(10)
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final MemberRepository memberRepository;
    private final LibraryBranchRepository branchRepository;
    private final AuthorRepository authorRepository;
    private final GenreRepository genreRepository;
    private final PublisherRepository publisherRepository;
    private final BookRepository bookRepository;
    private final BookCopyRepository bookCopyRepository;
    private final LibraryEventRepository eventRepository;
    private final LoanRepository loanRepository;
    private final HoldRepository holdRepository;
    private final FineRepository fineRepository;
    private final NotificationRepository notificationRepository;
    private final BookReviewRepository bookReviewRepository;
    private final StaffMemberRepository staffMemberRepository;
    private final BookClubRepository bookClubRepository;
    private final ReadingChallengeRepository readingChallengeRepository;
    private final CirculationRuleRepository circulationRuleRepository;
    private final DigitalResourceRepository digitalResourceRepository;
    private final DigitalLicenseRepository digitalLicenseRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepository,
                      MemberRepository memberRepository,
                      LibraryBranchRepository branchRepository,
                      AuthorRepository authorRepository,
                      GenreRepository genreRepository,
                      PublisherRepository publisherRepository,
                      BookRepository bookRepository,
                      BookCopyRepository bookCopyRepository,
                      LibraryEventRepository eventRepository,
                      LoanRepository loanRepository,
                      HoldRepository holdRepository,
                      FineRepository fineRepository,
                      NotificationRepository notificationRepository,
                      BookReviewRepository bookReviewRepository,
                      StaffMemberRepository staffMemberRepository,
                      BookClubRepository bookClubRepository,
                      ReadingChallengeRepository readingChallengeRepository,
                      CirculationRuleRepository circulationRuleRepository,
                      DigitalResourceRepository digitalResourceRepository,
                      DigitalLicenseRepository digitalLicenseRepository,
                      PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.memberRepository = memberRepository;
        this.branchRepository = branchRepository;
        this.authorRepository = authorRepository;
        this.genreRepository = genreRepository;
        this.publisherRepository = publisherRepository;
        this.bookRepository = bookRepository;
        this.bookCopyRepository = bookCopyRepository;
        this.eventRepository = eventRepository;
        this.loanRepository = loanRepository;
        this.holdRepository = holdRepository;
        this.fineRepository = fineRepository;
        this.notificationRepository = notificationRepository;
        this.bookReviewRepository = bookReviewRepository;
        this.staffMemberRepository = staffMemberRepository;
        this.bookClubRepository = bookClubRepository;
        this.readingChallengeRepository = readingChallengeRepository;
        this.circulationRuleRepository = circulationRuleRepository;
        this.digitalResourceRepository = digitalResourceRepository;
        this.digitalLicenseRepository = digitalLicenseRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (branchRepository.count() > 0) return;

        // ── Branches ─────────────────────────────────────────────────────────
        List<LibraryBranch> branches = branchRepository.saveAll(List.of(
                branch("City Library Main Branch",  "100 Library Lane",  "Springfield", "(555) 100-0001"),
                branch("North End Library", "450 Oak Street",    "Springfield", "(555) 100-0002"),
                branch("Westside Branch", "789 Elm Avenue",    "Shelbyville", "(555) 100-0003")
        ));
        LibraryBranch main  = branches.get(0);
        LibraryBranch north = branches.get(1);
        LibraryBranch south = branches.get(2);

        // ── Genres ───────────────────────────────────────────────────────────
        List<Genre> genres = genreRepository.saveAll(List.of(
                genre("Fiction",     "Literary and genre fiction"),
                genre("Technology",  "Computer science and software engineering"),
                genre("Science",     "Natural and applied sciences"),
                genre("History",     "World and regional history"),
                genre("Biography",   "Life stories and memoirs"),
                genre("Mystery",     "Detective and crime fiction"),
                genre("Fantasy",     "Fantasy and speculative fiction"),
                genre("Philosophy",  "Philosophy and ethics")
        ));
        Genre fiction    = genres.get(0);
        Genre technology = genres.get(1);
        Genre science    = genres.get(2);
        Genre history    = genres.get(3);
        Genre biography  = genres.get(4);
        Genre mystery    = genres.get(5);
        Genre fantasy    = genres.get(6);
        Genre philosophy = genres.get(7);

        // ── Publishers ───────────────────────────────────────────────────────
        List<Publisher> publishers = publisherRepository.saveAll(List.of(
                publisher("O'Reilly Media",      "Sebastopol",    "USA"),
                publisher("Manning Publications","Shelter Island", "USA"),
                publisher("Penguin Books",        "London",        "UK"),
                publisher("Anchor Books",         "New York",      "USA"),
                publisher("Scribner",             "New York",      "USA"),
                publisher("Harper & Row",         "New York",      "USA")
        ));
        Publisher oreilly  = publishers.get(0);
        Publisher manning  = publishers.get(1);
        Publisher penguin  = publishers.get(2);
        Publisher anchor   = publishers.get(3);
        Publisher scribner = publishers.get(4);
        Publisher harper   = publishers.get(5);

        // ── Authors ──────────────────────────────────────────────────────────
        List<Author> authors = authorRepository.saveAll(List.of(
                author("James",   "Gosling",    "Canadian",   "Creator of the Java programming language."),
                author("Joshua",  "Bloch",      "American",   "Author of Effective Java; former Java architect at Google."),
                author("Robert",  "Martin",     "American",   "Software engineer known as Uncle Bob."),
                author("Martin",  "Fowler",     "British",    "Chief Scientist at ThoughtWorks, authority on software design."),
                author("George",  "Orwell",     "British",    "Author of 1984 and Animal Farm."),
                author("J.R.R.", "Tolkien",    "British",    "Creator of Middle-earth and The Lord of the Rings."),
                author("Agatha",  "Christie",   "British",    "The Queen of Mystery, author of 66 detective novels."),
                author("Stephen", "Hawking",    "British",    "Theoretical physicist and cosmologist."),
                author("Yuval",   "Harari",     "Israeli",    "Historian and author of Sapiens and Homo Deus."),
                author("Ernest",  "Hemingway",  "American",   "Nobel Prize-winning novelist and short story writer."),
                author("Harper",  "Lee",        "American",   "Author of To Kill a Mockingbird."),
                author("F. Scott","Fitzgerald", "American",   "Author of The Great Gatsby.")
        ));
        Author gosling    = authors.get(0);
        Author bloch      = authors.get(1);
        Author martin     = authors.get(2);
        Author fowler     = authors.get(3);
        Author orwell     = authors.get(4);
        Author tolkien    = authors.get(5);
        Author christie   = authors.get(6);
        Author hawking    = authors.get(7);
        Author harari     = authors.get(8);
        Author hemingway  = authors.get(9);
        Author lee        = authors.get(10);
        Author fitzgerald = authors.get(11);

        // ── Books ────────────────────────────────────────────────────────────
        List<Book> books = bookRepository.saveAll(List.of(
                book("978-0-13-468599-1", "Effective Java",
                        "Best practices for the Java platform", 2018, 412, "English",
                        bloch, oreilly, Set.of(technology)),
                book("978-0-13-235088-4", "Clean Code",
                        "A Handbook of Agile Software Craftsmanship", 2008, 464, "English",
                        martin, oreilly, Set.of(technology)),
                book("978-0-13-468999-7", "Refactoring",
                        "Improving the Design of Existing Code", 2018, 448, "English",
                        fowler, oreilly, Set.of(technology)),
                book("978-1-61729-456-6", "Spring in Action",
                        "Covers Spring 5 and Spring Boot", 2018, 520, "English",
                        martin, manning, Set.of(technology)),
                book("978-0-45-228285-3", "1984",
                        "A dystopian social science fiction novel", 1949, 328, "English",
                        orwell, penguin, Set.of(fiction)),
                book("978-0-45-228306-5", "Animal Farm",
                        "An allegorical novella reflecting on Soviet totalitarianism", 1945, 112, "English",
                        orwell, penguin, Set.of(fiction, philosophy)),
                book("978-0-54-792822-7", "The Fellowship of the Ring",
                        "Part One of The Lord of the Rings", 1954, 432, "English",
                        tolkien, anchor, Set.of(fiction, fantasy)),
                book("978-0-06-207350-1", "Murder on the Orient Express",
                        "Hercule Poirot investigates a murder aboard the Orient Express", 1934, 256, "English",
                        christie, anchor, Set.of(mystery, fiction)),
                book("978-0-55-317521-7", "A Brief History of Time",
                        "From the Big Bang to Black Holes", 1988, 212, "English",
                        hawking, anchor, Set.of(science)),
                book("978-0-20-163361-5", "Design Patterns",
                        "Elements of Reusable Object-Oriented Software", 1994, 395, "English",
                        gosling, oreilly, Set.of(technology)),
                book("978-0-06-231609-7", "Sapiens: A Brief History of Humankind",
                        "A narrative of human history from the Stone Age to the 21st century", 2011, 443, "English",
                        harari, harper, Set.of(history, science)),
                book("978-0-74-327356-5", "The Old Man and the Sea",
                        "A short novel about an aging Cuban fisherman", 1952, 127, "English",
                        hemingway, scribner, Set.of(fiction)),
                book("978-0-44-631078-9", "To Kill a Mockingbird",
                        "A classic of modern American literature on racial injustice", 1960, 281, "English",
                        lee, harper, Set.of(fiction)),
                book("978-0-74-327356-6", "The Great Gatsby",
                        "A novel about the American dream set in the Jazz Age", 1925, 180, "English",
                        fitzgerald, scribner, Set.of(fiction)),
                book("978-0-54-792823-4", "The Two Towers",
                        "Part Two of The Lord of the Rings", 1954, 352, "English",
                        tolkien, anchor, Set.of(fiction, fantasy)),
                book("978-0-54-792824-1", "The Return of the King",
                        "Part Three of The Lord of the Rings", 1955, 416, "English",
                        tolkien, anchor, Set.of(fiction, fantasy)),
                book("978-0-06-112008-4", "To Kill a Mockingbird (Anniversary Ed.)",
                        "50th anniversary edition with new foreword by Harper Lee", 2010, 336, "English",
                        lee, harper, Set.of(fiction)),
                book("978-0-39-333348-1", "Homo Deus",
                        "A Brief History of Tomorrow — what will become of Homo Sapiens?", 2015, 449, "English",
                        harari, harper, Set.of(history, science)),
                book("978-0-13-110362-7", "The C Programming Language",
                        "The definitive reference for the C programming language", 1988, 272, "English",
                        gosling, oreilly, Set.of(technology)),
                book("978-0-06-207353-2", "And Then There Were None",
                        "Ten strangers stranded on an island; one by one they are murdered", 1939, 272, "English",
                        christie, anchor, Set.of(mystery))
        ));

        Book effectiveJava      = books.get(0);
        Book cleanCode          = books.get(1);
        Book refactoring        = books.get(2);
        Book springInAction     = books.get(3);
        Book nineteenEightyFour = books.get(4);
        Book animalFarm         = books.get(5);
        Book fellowship         = books.get(6);
        Book murderOrient       = books.get(7);
        Book briefHistory       = books.get(8);
        Book designPatterns     = books.get(9);
        Book sapiens            = books.get(10);
        Book oldManSea          = books.get(11);
        Book mockingbird        = books.get(12);
        Book greatGatsby        = books.get(13);
        Book twoTowers          = books.get(14);
        Book returnKing         = books.get(15);
        Book homoDeus           = books.get(17);
        Book andThenNone        = books.get(19);

        // ── Book Copies ──────────────────────────────────────────────────────
        List<BookCopy> copies = bookCopyRepository.saveAll(List.of(
                copy(effectiveJava,      main,  "BC-001", CopyCondition.NEW,  CopyStatus.AVAILABLE),
                copy(effectiveJava,      main,  "BC-002", CopyCondition.GOOD, CopyStatus.AVAILABLE),
                copy(effectiveJava,      north, "BC-003", CopyCondition.GOOD, CopyStatus.AVAILABLE),
                copy(cleanCode,          main,  "BC-004", CopyCondition.NEW,  CopyStatus.AVAILABLE),
                copy(cleanCode,          main,  "BC-005", CopyCondition.GOOD, CopyStatus.CHECKED_OUT),
                copy(cleanCode,          south, "BC-006", CopyCondition.FAIR, CopyStatus.AVAILABLE),
                copy(refactoring,        main,  "BC-007", CopyCondition.NEW,  CopyStatus.AVAILABLE),
                copy(refactoring,        north, "BC-008", CopyCondition.GOOD, CopyStatus.AVAILABLE),
                copy(springInAction,     main,  "BC-009", CopyCondition.NEW,  CopyStatus.AVAILABLE),
                copy(springInAction,     south, "BC-010", CopyCondition.GOOD, CopyStatus.CHECKED_OUT),
                copy(nineteenEightyFour, main,  "BC-011", CopyCondition.GOOD, CopyStatus.AVAILABLE),
                copy(nineteenEightyFour, north, "BC-012", CopyCondition.FAIR, CopyStatus.AVAILABLE),
                copy(nineteenEightyFour, south, "BC-013", CopyCondition.NEW,  CopyStatus.AVAILABLE),
                copy(animalFarm,         main,  "BC-014", CopyCondition.GOOD, CopyStatus.AVAILABLE),
                copy(animalFarm,         north, "BC-015", CopyCondition.FAIR, CopyStatus.AVAILABLE),
                copy(fellowship,         main,  "BC-016", CopyCondition.NEW,  CopyStatus.AVAILABLE),
                copy(fellowship,         north, "BC-017", CopyCondition.GOOD, CopyStatus.CHECKED_OUT),
                copy(murderOrient,       main,  "BC-018", CopyCondition.GOOD, CopyStatus.AVAILABLE),
                copy(murderOrient,       south, "BC-019", CopyCondition.NEW,  CopyStatus.AVAILABLE),
                copy(briefHistory,       main,  "BC-020", CopyCondition.GOOD, CopyStatus.AVAILABLE),
                copy(briefHistory,       north, "BC-021", CopyCondition.FAIR, CopyStatus.AVAILABLE),
                copy(designPatterns,     main,  "BC-022", CopyCondition.NEW,  CopyStatus.AVAILABLE),
                copy(designPatterns,     north, "BC-023", CopyCondition.GOOD, CopyStatus.AVAILABLE),
                // New copies for additional loans
                copy(sapiens,            main,  "BC-024", CopyCondition.NEW,  CopyStatus.CHECKED_OUT),
                copy(sapiens,            north, "BC-025", CopyCondition.GOOD, CopyStatus.AVAILABLE),
                copy(oldManSea,          main,  "BC-026", CopyCondition.GOOD, CopyStatus.CHECKED_OUT),
                copy(mockingbird,        main,  "BC-027", CopyCondition.NEW,  CopyStatus.AVAILABLE),
                copy(mockingbird,        south, "BC-028", CopyCondition.GOOD, CopyStatus.CHECKED_OUT),
                copy(greatGatsby,        main,  "BC-029", CopyCondition.FAIR, CopyStatus.CHECKED_OUT),
                copy(twoTowers,          main,  "BC-030", CopyCondition.NEW,  CopyStatus.AVAILABLE),
                copy(twoTowers,          north, "BC-031", CopyCondition.GOOD, CopyStatus.CHECKED_OUT),
                copy(returnKing,         main,  "BC-032", CopyCondition.NEW,  CopyStatus.AVAILABLE),
                copy(homoDeus,           main,  "BC-033", CopyCondition.NEW,  CopyStatus.CHECKED_OUT),
                copy(andThenNone,        main,  "BC-034", CopyCondition.GOOD, CopyStatus.AVAILABLE),
                copy(andThenNone,        south, "BC-035", CopyCondition.NEW,  CopyStatus.CHECKED_OUT)
        ));

        // ── Users + Members ──────────────────────────────────────────────────
        List<User> users = userRepository.saveAll(List.of(
                user("Alice",  "Admin",    "alice@citylibrary.org", Role.ADMIN),
                user("Bob",    "Staff",    "bob@citylibrary.org",   Role.MANAGER),
                user("Carol",  "Reader",   "carol@example.com",     Role.USER),
                user("David",  "Chen",     "david@example.com",     Role.USER),
                user("Eva",    "Martinez", "eva@example.com",       Role.USER),
                user("Frank",  "Thompson", "frank@example.com",     Role.USER),
                user("Grace",  "Kim",      "grace@example.com",     Role.USER),
                user("Helen",  "Wright",   "helen@example.com",     Role.USER),
                user("Ivan",   "Petrov",   "ivan@example.com",      Role.USER),
                user("Julia",  "Santos",   "julia@example.com",     Role.USER)
        ));

        // Alice and Bob are staff/admin users — give them member records so /members/me works
        memberRepository.save(member(users.get(0), "MEM-ADMIN-001", MembershipTier.PREMIUM));
        memberRepository.save(member(users.get(1), "MEM-STAFF-001", MembershipTier.PREMIUM));

        List<Member> members = memberRepository.saveAll(List.of(
                member(users.get(2), "MEM-001", MembershipTier.STANDARD),
                member(users.get(3), "MEM-002", MembershipTier.PREMIUM),
                member(users.get(4), "MEM-003", MembershipTier.STUDENT),
                member(users.get(5), "MEM-004", MembershipTier.PREMIUM),
                member(users.get(6), "MEM-005", MembershipTier.STANDARD),
                member(users.get(7), "MEM-006", MembershipTier.STUDENT),
                member(users.get(8), "MEM-007", MembershipTier.STANDARD),
                member(users.get(9), "MEM-008", MembershipTier.PREMIUM)
        ));
        Member carol  = members.get(0);
        Member david  = members.get(1);
        Member eva    = members.get(2);
        Member frank  = members.get(3);
        Member grace  = members.get(4);
        Member helen  = members.get(5);
        Member ivan   = members.get(6);
        Member julia  = members.get(7);

        // ── Loans ────────────────────────────────────────────────────────────
        List<Loan> loans = loanRepository.saveAll(List.of(
                // Carol: 2 active, 1 returned
                loan(copies.get(4),  carol,  main,  -7,  14, LoanStatus.ACTIVE,   0),
                loan(copies.get(23), carol,  main,  -5,  16, LoanStatus.ACTIVE,   0),
                loan(copies.get(13), carol,  main,  -21, -7, LoanStatus.RETURNED, 0),
                // David: 2 active, 1 overdue
                loan(copies.get(9),  david,  south, -3,  18, LoanStatus.ACTIVE,   0),
                loan(copies.get(30), david,  north, -12, 9,  LoanStatus.ACTIVE,   1),
                loan(copies.get(28), david,  south, -30, -9, LoanStatus.OVERDUE,  1),
                // Eva: 1 overdue (the original)
                loan(copies.get(16), eva,    north, -30, -9, LoanStatus.OVERDUE,  1),
                // Frank: 2 active
                loan(copies.get(25), frank,  main,  -4,  17, LoanStatus.ACTIVE,   0),
                loan(copies.get(32), frank,  main,  -8,  13, LoanStatus.ACTIVE,   0),
                // Grace: 1 active, 1 overdue
                loan(copies.get(27), grace,  south, -6,  15, LoanStatus.ACTIVE,   0),
                loan(copies.get(34), grace,  south, -25, -4, LoanStatus.OVERDUE,  0),
                // Helen: 2 active
                loan(copies.get(31), helen,  north, -10, 11, LoanStatus.ACTIVE,   0),
                loan(copies.get(33), helen,  main,  -2,  19, LoanStatus.ACTIVE,   0),
                // Ivan: 1 active, 1 returned
                loan(copies.get(21), ivan,   main,  -14, 7,  LoanStatus.ACTIVE,   0),
                loan(copies.get(6),  ivan,   main,  -40, -19,LoanStatus.RETURNED, 2),
                // Julia: 1 active
                loan(copies.get(19), julia,  main,  -1,  20, LoanStatus.ACTIVE,   0)
        ));
        Loan carolLoan1  = loans.get(0);
        Loan davidOverdue= loans.get(5);
        Loan evaOverdue  = loans.get(6);
        Loan graceOverdue= loans.get(10);

        // ── Holds ────────────────────────────────────────────────────────────
        holdRepository.saveAll(List.of(
                hold(fellowship,         carol,  main,  -5,  HoldStatus.READY,    LocalDateTime.now().minusDays(1)),
                hold(nineteenEightyFour, carol,  north, -3,  HoldStatus.PENDING,  null),
                hold(briefHistory,       david,  main,  -2,  HoldStatus.PENDING,  null),
                hold(murderOrient,       eva,    south, -7,  HoldStatus.PENDING,  null),
                hold(designPatterns,     frank,  main,  -1,  HoldStatus.PENDING,  null),
                hold(cleanCode,          grace,  south, -4,  HoldStatus.PENDING,  null),
                hold(animalFarm,         helen,  north, -6,  HoldStatus.READY,    LocalDateTime.now().minusDays(2)),
                hold(sapiens,            ivan,   main,  -2,  HoldStatus.PENDING,  null),
                hold(greatGatsby,        julia,  main,  -1,  HoldStatus.PENDING,  null),
                hold(twoTowers,          carol,  main,  -8,  HoldStatus.FULFILLED,null)
        ));

        // ── Fines ────────────────────────────────────────────────────────────
        List<Fine> fines = fineRepository.saveAll(List.of(
                fine(evaOverdue,    eva,    new BigDecimal("2.25"), "Overdue return — 9 days late",  -9,  null),
                fine(davidOverdue,  david,  new BigDecimal("3.50"), "Overdue return — 14 days late", -14, null),
                fine(graceOverdue,  grace,  new BigDecimal("1.25"), "Overdue return — 5 days late",  -5,  null),
                fine(loans.get(2),  carol,  new BigDecimal("2.50"), "Overdue return — 5 days late",  -6,  null),
                fine(carolLoan1,    carol,  new BigDecimal("0.75"), "Book returned with minor damage",-15, LocalDateTime.now().minusDays(14)),
                fine(loans.get(14), ivan,   new BigDecimal("4.75"), "Overdue return — 19 days late", -20, LocalDateTime.now().minusDays(5))
        ));

        // ── Notifications ─────────────────────────────────────────────────────
        notificationRepository.saveAll(List.of(
                notif(carol,  NotificationType.HOLD_READY,           "Your hold on 'The Fellowship of the Ring' is ready for pickup at Main Branch.", -1,  null,     NotificationChannel.IN_APP),
                notif(carol,  NotificationType.DUE_SOON,             "Reminder: 'Clean Code' is due in 3 days.", -3, null,                                          NotificationChannel.EMAIL),
                notif(carol,  NotificationType.FINE_ISSUED,          "A fine of $0.75 was issued for 'Clean Code' returned with damage.", -15, LocalDateTime.now().minusDays(14), NotificationChannel.EMAIL),
                notif(david,  NotificationType.OVERDUE,              "Your copy of 'To Kill a Mockingbird' is 9 days overdue. Fine accruing at $0.25/day.", -9, null, NotificationChannel.EMAIL),
                notif(david,  NotificationType.FINE_ISSUED,          "A fine of $3.50 has been issued for overdue return.", -9, null,                               NotificationChannel.IN_APP),
                notif(david,  NotificationType.HOLD_READY,           "Your hold on 'A Brief History of Time' is now pending fulfillment.", -2, LocalDateTime.now().minusDays(1), NotificationChannel.IN_APP),
                notif(eva,    NotificationType.OVERDUE,              "Your copy of 'The Fellowship of the Ring' is 9 days overdue. Please return it.", -9, null,    NotificationChannel.EMAIL),
                notif(eva,    NotificationType.FINE_ISSUED,          "A fine of $2.25 has been issued. Log in to pay online or visit any branch.", -9, null,        NotificationChannel.IN_APP),
                notif(eva,    NotificationType.MEMBERSHIP_EXPIRING,  "Your library membership expires in 30 days. Renew to keep borrowing privileges.", -2, null,   NotificationChannel.EMAIL),
                notif(frank,  NotificationType.DUE_SOON,             "'Sapiens' is due in 3 days. Renew online if you need more time.", -4, LocalDateTime.now().minusDays(3), NotificationChannel.IN_APP),
                notif(grace,  NotificationType.OVERDUE,              "Your copy of 'And Then There Were None' is 4 days overdue.", -4, null,                        NotificationChannel.EMAIL),
                notif(grace,  NotificationType.FINE_ISSUED,          "A fine of $1.25 has been issued for overdue return.", -4, null,                               NotificationChannel.IN_APP),
                notif(helen,  NotificationType.HOLD_READY,           "Your hold on 'Animal Farm' is ready for pickup at North Branch.", -2, null,                   NotificationChannel.IN_APP),
                notif(helen,  NotificationType.DUE_SOON,             "'The Two Towers' is due in 1 day. Visit the library to renew.", -1, null,                     NotificationChannel.EMAIL),
                notif(ivan,   NotificationType.FINE_ISSUED,          "A fine of $4.75 for overdue 'Refactoring' has been partially settled.", -5, LocalDateTime.now().minusDays(4), NotificationChannel.EMAIL),
                notif(julia,  NotificationType.DUE_SOON,             "Just a reminder: 'A Brief History of Time' is due in 7 days.", -1, LocalDateTime.now().minusDays(1), NotificationChannel.IN_APP)
        ));

        // ── Book Reviews ──────────────────────────────────────────────────────
        bookReviewRepository.saveAll(List.of(
                review(effectiveJava, carol, 5, "An absolute must-read for any Java developer. Bloch's tips transformed how I write code.", -45, true),
                review(effectiveJava, david, 5, "Deserves its legendary status. Every single item is practical and well-explained.", -30, true),
                review(effectiveJava, frank, 4, "Comprehensive and detailed. Some items feel dated for modern Java, but core concepts remain essential.", -10, true),
                review(cleanCode,     carol, 4, "Changed how I think about naming and structure. The chapter on functions alone is worth the price.", -60, true),
                review(cleanCode,     helen, 5, "Made me realize how much of my old code was unreadable. A transformative read.", -20, true),
                review(cleanCode,     julia, 3, "Good principles but the examples feel a bit dated. Still worth reading.", -5, true),
                review(nineteenEightyFour, eva,   5, "Chilling, timeless, and more relevant than ever. One of the greatest novels ever written.", -90, true),
                review(nineteenEightyFour, grace, 5, "Orwell's vision of totalitarianism is as haunting today as it was in 1949.", -14, true),
                review(nineteenEightyFour, ivan,  4, "Deeply unsettling in the best way. The appendix on Newspeak is a literary masterpiece.", -7, true),
                review(fellowship,    carol, 5, "Tolkien builds a world so rich you forget it isn't real. The perfect start to an epic trilogy.", -120, true),
                review(fellowship,    helen, 5, "Every re-read reveals something new. A true masterpiece of imaginative storytelling.", -35, true),
                review(murderOrient,  david, 4, "One of Christie's best. The solution is so audacious you won't see it coming.", -50, true),
                review(murderOrient,  julia, 5, "Poirot at his finest. The reveal had me gasping. Perfect mystery plotting.", -8, true),
                review(sapiens,       frank, 5, "A sweeping, thought-provoking history of humanity. Changed how I see civilization.", -25, true),
                review(sapiens,       ivan,  4, "Provocative and fascinating. Harari makes you question everything you thought you knew.", -12, true),
                review(briefHistory,  grace, 5, "Hawking makes the incomprehensible accessible. Mind-bending but surprisingly readable.", -40, true),
                review(briefHistory,  carol, 4, "More approachable than I expected. A great introduction to cosmology for non-scientists.", -15, true),
                review(designPatterns,david, 4, "Dense but invaluable. The patterns are timeless even if the UML diagrams feel dated.", -55, true),
                review(refactoring,   frank, 5, "Every developer who maintains legacy code needs this book. Fowler's examples are spot-on.", -18, true),
                review(mockingbird,   grace, 5, "Lee's prose is luminous and the moral core is unshakeable. A perfect novel.", -22, true)
        ));

        // ── Staff Members ─────────────────────────────────────────────────────
        List<StaffMember> staff = staffMemberRepository.saveAll(List.of(
                StaffMember.builder().user(users.get(0)).branch(main).role(StaffRole.ADMIN).startDate(LocalDate.now().minusYears(5)).build(),
                StaffMember.builder().user(users.get(1)).branch(main).role(StaffRole.MANAGER).startDate(LocalDate.now().minusYears(3)).build()
        ));

        // ── Book Clubs ────────────────────────────────────────────────────────
        bookClubRepository.saveAll(List.of(
                BookClub.builder().name("Classic Fiction Society").description("Exploring timeless works of classic fiction.").branch(main).facilitator(staff.get(1)).maxMembers(15).meetingSchedule("First Monday of each month at 6PM").currentBook(nineteenEightyFour).status(BookClubStatus.ACTIVE).build(),
                BookClub.builder().name("Tech Readers Circle").description("For those who love technology and programming books.").branch(north).facilitator(staff.get(1)).maxMembers(20).meetingSchedule("Every Wednesday at 7PM").currentBook(effectiveJava).status(BookClubStatus.ACTIVE).build(),
                BookClub.builder().name("Mystery & Thriller Club").description("Monthly deep dives into mystery and thriller novels.").branch(south).facilitator(staff.get(0)).maxMembers(12).meetingSchedule("Third Saturday of each month at 3PM").currentBook(murderOrient).status(BookClubStatus.ACTIVE).build()
        ));

        // ── Reading Challenges ────────────────────────────────────────────────
        readingChallengeRepository.saveAll(List.of(
                ReadingChallenge.builder().name("Summer Reading Challenge 2026").description("Read 10 books before summer ends!").startDate(LocalDate.now().minusDays(30)).endDate(LocalDate.now().plusDays(60)).targetBooks(10).targetGenreNames("Fiction,Mystery,Science Fiction").badge("SummerReader2026").active(true).build(),
                ReadingChallenge.builder().name("Tech Literacy Month").description("Read 3 technology or programming books this month.").startDate(LocalDate.now().minusDays(5)).endDate(LocalDate.now().plusDays(25)).targetBooks(3).targetGenreNames("Technology,Programming").badge("TechReader").active(true).build(),
                ReadingChallenge.builder().name("Classics Connoisseur").description("Tackle 5 classic works of literature by year end.").startDate(LocalDate.of(2026, 1, 1)).endDate(LocalDate.of(2026, 12, 31)).targetBooks(5).targetGenreNames("Fiction,Classic").badge("ClassicsConnoisseur").active(true).build()
        ));

        // ── Circulation Rules ─────────────────────────────────────────────────
        circulationRuleRepository.saveAll(List.of(
                CirculationRule.builder().membershipTier(null).itemType(ItemType.BOOK).loanPeriodDays(21).maxRenewals(2).fineRatePerDay(new BigDecimal("0.25")).maxFineAmount(new BigDecimal("10.00")).maxLoansAllowed(10).reservationHoldDays(7).minAgeRequired(0).active(true).build(),
                CirculationRule.builder().membershipTier(MembershipTier.STANDARD).itemType(ItemType.BOOK).loanPeriodDays(14).maxRenewals(1).fineRatePerDay(new BigDecimal("0.25")).maxFineAmount(new BigDecimal("5.00")).maxLoansAllowed(5).reservationHoldDays(5).minAgeRequired(0).active(true).build(),
                CirculationRule.builder().membershipTier(MembershipTier.PREMIUM).itemType(ItemType.BOOK).loanPeriodDays(28).maxRenewals(3).fineRatePerDay(new BigDecimal("0.10")).maxFineAmount(new BigDecimal("15.00")).maxLoansAllowed(15).reservationHoldDays(10).minAgeRequired(0).active(true).build(),
                CirculationRule.builder().membershipTier(null).itemType(ItemType.PERIODICAL).loanPeriodDays(7).maxRenewals(0).fineRatePerDay(new BigDecimal("0.50")).maxFineAmount(new BigDecimal("5.00")).maxLoansAllowed(5).reservationHoldDays(3).minAgeRequired(0).active(true).build(),
                CirculationRule.builder().membershipTier(null).itemType(ItemType.DIGITAL).loanPeriodDays(14).maxRenewals(1).fineRatePerDay(new BigDecimal("0.00")).maxFineAmount(new BigDecimal("0.00")).maxLoansAllowed(3).reservationHoldDays(1).minAgeRequired(0).active(true).build()
        ));

        // ── Digital Resources ─────────────────────────────────────────────────
        List<DigitalResource> digitalResources = digitalResourceRepository.saveAll(List.of(
                DigitalResource.builder().title("Effective Java (eBook)").description("The definitive guide to Java best practices by Joshua Bloch.").resourceType(DigitalResourceType.EBOOK).format(DigitalResourceFormat.EPUB).publisher("Addison-Wesley").isbn("9780134685991").licenseType(LicenseType.MULTI_USER).maxConcurrentUsers(10).publicationYear(2018).language("en").fileSizeBytes(3_145_728L).active(true).build(),
                DigitalResource.builder().title("Clean Code (eBook)").description("A handbook of agile software craftsmanship by Robert C. Martin.").resourceType(DigitalResourceType.EBOOK).format(DigitalResourceFormat.PDF).publisher("Prentice Hall").isbn("9780132350884").licenseType(LicenseType.MULTI_USER).maxConcurrentUsers(5).publicationYear(2008).language("en").fileSizeBytes(2_097_152L).active(true).build(),
                DigitalResource.builder().title("1984 (Audiobook)").description("George Orwell's dystopian masterpiece, narrated by Simon Prebble.").resourceType(DigitalResourceType.AUDIOBOOK).format(DigitalResourceFormat.MP3).publisher("Recorded Books").isbn("9780451524935").licenseType(LicenseType.MULTI_USER).maxConcurrentUsers(3).publicationYear(2008).language("en").durationMinutes(690).active(true).build(),
                DigitalResource.builder().title("Sapiens: A Brief History of Humankind (Audiobook)").description("Yuval Noah Harari's exploration of the human story, narrated by Derek Perkins.").resourceType(DigitalResourceType.AUDIOBOOK).format(DigitalResourceFormat.MP3).publisher("HarperAudio").isbn("9780062316110").licenseType(LicenseType.MULTI_USER).maxConcurrentUsers(3).publicationYear(2015).language("en").durationMinutes(915).active(true).build(),
                DigitalResource.builder().title("Introduction to Spring Boot").description("A video course covering Spring Boot 3 fundamentals and REST API development.").resourceType(DigitalResourceType.VIDEO).format(DigitalResourceFormat.MP4).publisher("City Library Productions").licenseType(LicenseType.UNLIMITED).maxConcurrentUsers(null).publicationYear(2024).language("en").durationMinutes(180).active(true).build(),
                DigitalResource.builder().title("Oxford Reference Online").description("Access to Oxford University Press reference works across disciplines.").resourceType(DigitalResourceType.DATABASE).format(DigitalResourceFormat.HTML).publisher("Oxford University Press").licenseType(LicenseType.UNLIMITED).maxConcurrentUsers(null).publicationYear(2024).language("en").active(true).build(),
                DigitalResource.builder().title("Refactoring: Improving the Design of Existing Code (eBook)").description("Martin Fowler's classic on code improvement techniques.").resourceType(DigitalResourceType.EBOOK).format(DigitalResourceFormat.EPUB).publisher("Addison-Wesley").isbn("9780134757599").licenseType(LicenseType.MULTI_USER).maxConcurrentUsers(5).publicationYear(2018).language("en").fileSizeBytes(4_194_304L).active(true).build(),
                DigitalResource.builder().title("The Hobbit (Audiobook)").description("J.R.R. Tolkien's beloved tale read by Rob Inglis.").resourceType(DigitalResourceType.AUDIOBOOK).format(DigitalResourceFormat.MP3).publisher("HarperAudio").isbn("9780261102217").licenseType(LicenseType.MULTI_USER).maxConcurrentUsers(4).publicationYear(2012).language("en").durationMinutes(682).active(true).build()
        ));
        // Seed licenses for MULTI_USER resources so they show as available
        digitalResources.stream()
                .filter(r -> r.getLicenseType() == LicenseType.MULTI_USER)
                .forEach(r -> digitalLicenseRepository.save(
                        DigitalLicense.builder().resource(r).branch(main)
                                .maxUsers(r.getMaxConcurrentUsers()).currentUsers(0)
                                .expiryDate(LocalDate.of(2027, 12, 31)).active(true).build()
                ));

        // ── Events ────────────────────────────────────────────────────────────
        eventRepository.saveAll(List.of(
                event("Java & Spring Workshop",          "Hands-on Spring Boot 3 workshop for developers.",                  main,  7,  3,  30, EventType.WORKSHOP),
                event("Author Reading: Modern Fiction",  "An evening with local fiction authors.",                           north, 14, 2,  50, EventType.AUTHOR_READING),
                event("Mystery Book Club",               "Monthly discussion of classic mysteries.",                         south, 3,  2,  20, EventType.BOOK_CLUB),
                event("Kids Story Hour",                 "Interactive storytelling for children ages 4-8.",                  main,  2,  1,  25, EventType.CHILDREN_PROGRAM),
                event("Science Café: Black Holes",       "Discussion inspired by A Brief History of Time.",                  north, 21, 2,  40, EventType.OTHER),
                event("Fantasy Worlds Book Club",        "Deep dive into Tolkien's Middle-earth and modern fantasy.",        main,  10, 2,  20, EventType.BOOK_CLUB),
                event("History of Human Civilization",  "Exploring Harari's Sapiens with a historian panel.",               south, 5,  3,  35, EventType.OTHER),
                event("Teen Reading Challenge Kickoff",  "Join our summer reading challenge — prizes for all participants!", main,  1,  2,  60, EventType.CHILDREN_PROGRAM)
        ));
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private LibraryBranch branch(String name, String address, String city, String phone) {
        return LibraryBranch.builder().name(name).address(address).city(city)
                .phone(phone).active(true).build();
    }

    private Genre genre(String name, String desc) {
        return Genre.builder().name(name).description(desc).build();
    }

    private Publisher publisher(String name, String city, String country) {
        return Publisher.builder().name(name).city(city).country(country).build();
    }

    private Author author(String first, String last, String nationality, String bio) {
        return Author.builder().firstName(first).lastName(last)
                .nationality(nationality).bio(bio).build();
    }

    private Book book(String isbn, String title, String desc, int year, int pages,
                      String lang, Author author, Publisher publisher, Set<Genre> bookGenres) {
        Book b = Book.builder().isbn(isbn).title(title).description(desc)
                .publicationYear(year).pageCount(pages).language(lang)
                .publisher(publisher).build();
        b.getAuthors().add(author);
        b.getGenres().addAll(bookGenres);
        return b;
    }

    private BookCopy copy(Book book, LibraryBranch branch, String barcode,
                          CopyCondition condition, CopyStatus status) {
        return BookCopy.builder().book(book).branch(branch).barcode(barcode)
                .condition(condition).status(status)
                .acquiredDate(LocalDate.now().minusMonths(6)).build();
    }

    private User user(String first, String last, String email, Role role) {
        return User.builder().firstName(first).lastName(last).email(email)
                .password(passwordEncoder.encode("password123")).role(role).build();
    }

    private Member member(User user, String number, MembershipTier tier) {
        return Member.builder().user(user).membershipNumber(number).membershipTier(tier)
                .joinDate(LocalDate.now().minusYears(1)).expiryDate(LocalDate.now().plusYears(1))
                .fineBalance(BigDecimal.ZERO).active(true).build();
    }

    private Loan loan(BookCopy copy, Member member, LibraryBranch branch,
                      int checkoutDaysAgo, int dueDaysFromNow, LoanStatus status, int renewals) {
        return Loan.builder().bookCopy(copy).member(member).branch(branch)
                .checkoutDate(LocalDateTime.now().plusDays(checkoutDaysAgo))
                .dueDate(LocalDateTime.now().plusDays(dueDaysFromNow))
                .status(status).renewalCount(renewals).build();
    }

    private Hold hold(Book book, Member member, LibraryBranch branch,
                      int daysAgo, HoldStatus status, LocalDateTime notifiedDate) {
        return Hold.builder()
                .book(book).member(member).pickupBranch(branch)
                .requestDate(LocalDateTime.now().plusDays(daysAgo))
                .expiryDate(status == HoldStatus.READY
                        ? LocalDateTime.now().plusDays(7) : null)
                .status(status)
                .notifiedDate(notifiedDate)
                .build();
    }

    private Fine fine(Loan loan, Member member, BigDecimal amount, String reason,
                      int issuedDaysAgo, LocalDateTime paidDate) {
        return Fine.builder()
                .loan(loan).member(member).amount(amount).reason(reason)
                .issuedDate(LocalDateTime.now().plusDays(issuedDaysAgo))
                .paidDate(paidDate)
                .waived(false)
                .build();
    }

    private Notification notif(Member member, NotificationType type, String message,
                                int sentDaysAgo, LocalDateTime readDate, NotificationChannel channel) {
        return Notification.builder()
                .member(member).type(type).message(message)
                .sentDate(LocalDateTime.now().plusDays(sentDaysAgo))
                .readDate(readDate)
                .channel(channel)
                .build();
    }

    private BookReview review(Book book, Member member, int rating, String text,
                               int daysAgo, boolean approved) {
        return BookReview.builder()
                .book(book).member(member).rating(rating).reviewText(text)
                .reviewDate(LocalDateTime.now().plusDays(daysAgo))
                .approved(approved)
                .build();
    }

    private LibraryEvent event(String title, String desc, LibraryBranch branch,
                               int daysFromNow, int durationHours, int capacity, EventType type) {
        LocalDateTime start = LocalDateTime.now().plusDays(daysFromNow);
        return LibraryEvent.builder().title(title).description(desc).branch(branch)
                .startDateTime(start).endDateTime(start.plusHours(durationHours))
                .capacity(capacity).registeredCount(0).eventType(type).build();
    }
}
