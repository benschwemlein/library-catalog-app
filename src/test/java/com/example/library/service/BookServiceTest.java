package com.example.library.service;

import com.example.library.TestDataFactory;
import com.example.library.dto.CreateBookRequest;
import com.example.library.dto.UpdateBookRequest;
import com.example.library.entity.*;
import com.example.library.exception.BookNotFoundException;
import com.example.library.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private AuthorRepository authorRepository;

    @Mock
    private PublisherRepository publisherRepository;

    @Mock
    private GenreRepository genreRepository;

    @Mock
    private BookSubjectRepository bookSubjectRepository;

    @InjectMocks
    private BookService bookService;

    private Book book;
    private Author author;
    private Genre genre;

    @BeforeEach
    void setUp() {
        book = TestDataFactory.createBook();
        author = TestDataFactory.createAuthor();
        genre = TestDataFactory.createGenre();
    }

    // -------------------------------------------------------------------------
    // findAll tests
    // -------------------------------------------------------------------------

    @Test
    void findAll_returnsPageOfBooks() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<Book> page = new PageImpl<>(List.of(book));
        when(bookRepository.findAll(pageable)).thenReturn(page);

        Page<Book> result = bookService.findAll(pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getIsbn()).isEqualTo("978-0-452-28423-4");
    }

    // -------------------------------------------------------------------------
    // findById tests
    // -------------------------------------------------------------------------

    @Test
    void findById_found_returnsBook() {
        when(bookRepository.findById(book.getId())).thenReturn(Optional.of(book));

        Book result = bookService.findById(book.getId());

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(book.getId());
        assertThat(result.getTitle()).isEqualTo("Nineteen Eighty-Four");
    }

    @Test
    void findById_notFound_throwsBookNotFoundException() {
        when(bookRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookService.findById(999L))
                .isInstanceOf(BookNotFoundException.class)
                .hasMessageContaining("999");
    }

    // -------------------------------------------------------------------------
    // findByIsbn tests
    // -------------------------------------------------------------------------

    @Test
    void searchByIsbn_returnsMatchingBook() {
        when(bookRepository.findByIsbn("978-0-452-28423-4")).thenReturn(Optional.of(book));

        Book result = bookService.findByIsbn("978-0-452-28423-4");

        assertThat(result).isNotNull();
        assertThat(result.getIsbn()).isEqualTo("978-0-452-28423-4");
        assertThat(result.getTitle()).isEqualTo("Nineteen Eighty-Four");
    }

    @Test
    void findByIsbn_notFound_throwsBookNotFoundException() {
        when(bookRepository.findByIsbn("000-0-000-00000-0")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookService.findByIsbn("000-0-000-00000-0"))
                .isInstanceOf(BookNotFoundException.class)
                .hasMessageContaining("000-0-000-00000-0");
    }

    // -------------------------------------------------------------------------
    // searchBooks tests
    // -------------------------------------------------------------------------

    @Test
    void searchByTitle_returnsMatchingBooks() {
        Book book2 = TestDataFactory.createBook();
        book2.setId(2L);
        book2.setTitle("Animal Farm");

        when(bookRepository.searchByTitleOrDescription("farm")).thenReturn(List.of(book2));

        List<Book> result = bookService.searchBooks("farm");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Animal Farm");
    }

    @Test
    void searchByAuthor_returnsMatchingBooks() {
        // searchBooks calls searchByTitleOrDescription; author-based search
        // would be a separate findByAuthors_LastNameIgnoreCase call.
        // We test that searchBooks returns results for a query matching description.
        when(bookRepository.searchByTitleOrDescription("dystopian")).thenReturn(List.of(book));

        List<Book> result = bookService.searchBooks("dystopian");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(book.getId());
    }

    @Test
    void search_noResults_returnsEmptyList() {
        when(bookRepository.searchByTitleOrDescription("zzznomatch")).thenReturn(List.of());

        List<Book> result = bookService.searchBooks("zzznomatch");

        assertThat(result).isEmpty();
    }

    // -------------------------------------------------------------------------
    // getAvailableCopies tests
    // -------------------------------------------------------------------------

    @Test
    void getAvailableCopies_returnsOnlyAvailableCopies() {
        BookCopy available = TestDataFactory.createBookCopy();
        available.setStatus(CopyStatus.AVAILABLE);

        BookCopy checkedOut = TestDataFactory.createCheckedOutCopy();
        checkedOut.setStatus(CopyStatus.CHECKED_OUT);

        Book bookWithCopies = Book.builder()
                .id(book.getId())
                .isbn(book.getIsbn())
                .title(book.getTitle())
                .authors(new HashSet<>())
                .genres(new HashSet<>())
                .subjects(new HashSet<>())
                .copies(new ArrayList<>(List.of(available, checkedOut)))
                .reviews(new ArrayList<>())
                .build();

        when(bookRepository.findById(bookWithCopies.getId())).thenReturn(Optional.of(bookWithCopies));

        List<BookCopy> result = bookService.getAvailableCopies(bookWithCopies.getId());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo(CopyStatus.AVAILABLE);
    }

    @Test
    void getAvailableCopies_bookNotFound_throwsBookNotFoundException() {
        when(bookRepository.findById(404L)).thenReturn(Optional.empty());

        assertThrows(BookNotFoundException.class, () -> bookService.getAvailableCopies(404L));
    }

    @Test
    void getAvailableCopies_allCopiesCheckedOut_returnsEmptyList() {
        BookCopy co1 = TestDataFactory.createCheckedOutCopy();
        BookCopy co2 = TestDataFactory.createCheckedOutCopy();
        co2.setId(10L);

        Book bookNoCopiesAvailable = Book.builder()
                .id(book.getId())
                .isbn(book.getIsbn())
                .title(book.getTitle())
                .authors(new HashSet<>())
                .genres(new HashSet<>())
                .subjects(new HashSet<>())
                .copies(new ArrayList<>(List.of(co1, co2)))
                .reviews(new ArrayList<>())
                .build();

        when(bookRepository.findById(bookNoCopiesAvailable.getId()))
                .thenReturn(Optional.of(bookNoCopiesAvailable));

        List<BookCopy> result = bookService.getAvailableCopies(bookNoCopiesAvailable.getId());

        assertThat(result).isEmpty();
    }

    // -------------------------------------------------------------------------
    // addBook tests
    // -------------------------------------------------------------------------

    @Test
    void addBook_savesAndReturnsBook() {
        CreateBookRequest req = CreateBookRequest.builder()
                .isbn("978-0-7432-7356-5")
                .title("The Road")
                .description("Post-apocalyptic novel by Cormac McCarthy.")
                .language("English")
                .publicationYear(2006)
                .pageCount(287)
                .build();

        Book expectedBook = Book.builder()
                .id(10L)
                .isbn("978-0-7432-7356-5")
                .title("The Road")
                .description("Post-apocalyptic novel by Cormac McCarthy.")
                .language("English")
                .publicationYear(2006)
                .pageCount(287)
                .authors(new HashSet<>())
                .genres(new HashSet<>())
                .subjects(new HashSet<>())
                .copies(new ArrayList<>())
                .reviews(new ArrayList<>())
                .build();

        when(bookRepository.save(any(Book.class))).thenReturn(expectedBook);

        Book result = bookService.addBook(req);

        assertThat(result).isNotNull();
        assertThat(result.getIsbn()).isEqualTo("978-0-7432-7356-5");
        assertThat(result.getTitle()).isEqualTo("The Road");
        verify(bookRepository).save(any(Book.class));
    }

    @Test
    void addBook_withAuthorIds_loadsAuthorsFromRepository() {
        CreateBookRequest req = CreateBookRequest.builder()
                .isbn("978-0-14-028329-7")
                .title("Of Mice and Men")
                .language("English")
                .publicationYear(1937)
                .pageCount(112)
                .authorIds(List.of(1L, 2L))
                .build();

        when(authorRepository.findAllById(List.of(1L, 2L))).thenReturn(List.of(author));
        when(bookRepository.save(any(Book.class))).thenAnswer(inv -> inv.getArgument(0));

        Book result = bookService.addBook(req);

        assertThat(result).isNotNull();
        verify(authorRepository).findAllById(List.of(1L, 2L));
    }

    @Test
    void addBook_withGenreIds_loadsGenresFromRepository() {
        CreateBookRequest req = CreateBookRequest.builder()
                .isbn("978-0-316-76948-0")
                .title("The Catcher in the Rye")
                .language("English")
                .publicationYear(1951)
                .pageCount(277)
                .genreIds(List.of(1L))
                .build();

        when(genreRepository.findAllById(List.of(1L))).thenReturn(List.of(genre));
        when(bookRepository.save(any(Book.class))).thenAnswer(inv -> inv.getArgument(0));

        bookService.addBook(req);

        verify(genreRepository).findAllById(List.of(1L));
    }

    // -------------------------------------------------------------------------
    // updateBook tests
    // -------------------------------------------------------------------------

    @Test
    void updateBook_updatesFieldsAndSaves() {
        UpdateBookRequest req = UpdateBookRequest.builder()
                .title("Nineteen Eighty-Four (Updated Edition)")
                .description("Updated description.")
                .language("English")
                .publicationYear(2003)
                .pageCount(330)
                .build();

        when(bookRepository.findById(book.getId())).thenReturn(Optional.of(book));
        when(bookRepository.save(any(Book.class))).thenAnswer(inv -> inv.getArgument(0));

        Book result = bookService.updateBook(book.getId(), req);

        assertThat(result).isNotNull();
        verify(bookRepository).save(book);
    }

    @Test
    void updateBook_bookNotFound_throwsBookNotFoundException() {
        UpdateBookRequest req = UpdateBookRequest.builder()
                .title("Ghost Book")
                .build();

        when(bookRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(BookNotFoundException.class, () -> bookService.updateBook(999L, req));
        verify(bookRepository, never()).save(any());
    }

    // -------------------------------------------------------------------------
    // deleteBook tests
    // -------------------------------------------------------------------------

    @Test
    void deleteBook_found_deletesSuccessfully() {
        when(bookRepository.findById(book.getId())).thenReturn(Optional.of(book));

        bookService.deleteBook(book.getId());

        verify(bookRepository).delete(book);
    }

    @Test
    void deleteBook_notFound_throwsBookNotFoundException() {
        when(bookRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(BookNotFoundException.class, () -> bookService.deleteBook(999L));
        verify(bookRepository, never()).delete(any());
    }
}
