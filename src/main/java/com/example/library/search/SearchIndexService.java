package com.example.library.search;

import com.example.library.entity.Author;
import com.example.library.entity.Book;
import com.example.library.repository.AuthorRepository;
import com.example.library.repository.BookRepository;
import com.example.library.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Manages the lifecycle of {@link SearchIndex} entries.
 * Handles individual entity indexing, asynchronous full re-indexing,
 * removal of stale entries, and index statistics.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SearchIndexService {

    private final SearchIndexRepository repository;
    private final BookRepository bookRepository;
    private final MemberRepository memberRepository;
    private final AuthorRepository authorRepository;

    // -------------------------------------------------------------------------
    // Public indexing operations
    // -------------------------------------------------------------------------

    /**
     * Creates or updates the search index entry for a single {@link Book}.
     * The entry is keyed on (BOOK, book.id) so re-indexing is idempotent.
     */
    public void indexBook(Book book) {
        SearchIndex entry = repository
                .findByEntityTypeAndEntityId(EntityType.BOOK, book.getId())
                .orElse(SearchIndex.builder()
                        .entityType(EntityType.BOOK)
                        .entityId(book.getId())
                        .build());

        entry.setTitle(book.getTitle());
        entry.setContent(buildBookContent(book));
        entry.setTags(buildBookTags(book));
        entry.setLastIndexed(LocalDateTime.now());
        entry.setBoost(1.0f);
        entry.setEntityUrl("/api/v1/books/" + book.getId());

        repository.save(entry);
        log.debug("Indexed book id={} title='{}'", book.getId(), book.getTitle());
    }

    /**
     * Creates or updates the search index entry for a single {@link Author}.
     */
    public void indexAuthor(Author author) {
        SearchIndex entry = repository
                .findByEntityTypeAndEntityId(EntityType.AUTHOR, author.getId())
                .orElse(SearchIndex.builder()
                        .entityType(EntityType.AUTHOR)
                        .entityId(author.getId())
                        .build());

        String fullName = author.getFirstName() + " " + author.getLastName();
        StringBuilder content = new StringBuilder(fullName).append(" ");
        if (author.getBio() != null) content.append(author.getBio()).append(" ");
        if (author.getNationality() != null) content.append(author.getNationality()).append(" ");

        entry.setTitle(fullName);
        entry.setContent(content.toString().trim());
        entry.setTags(author.getNationality() != null ? author.getNationality() : "");
        entry.setLastIndexed(LocalDateTime.now());
        entry.setBoost(1.0f);
        entry.setEntityUrl("/api/v1/authors/" + author.getId());

        repository.save(entry);
        log.debug("Indexed author id={} name='{}'", author.getId(), fullName);
    }

    /**
     * Drops a single index entry.  Called when an entity is deleted from
     * the catalog.
     */
    public void removeFromIndex(EntityType type, Long entityId) {
        repository.findByEntityTypeAndEntityId(type, entityId)
                .ifPresent(entry -> {
                    repository.delete(entry);
                    log.debug("Removed {} id={} from search index", type, entityId);
                });
    }

    /**
     * Full re-index of all books and authors.  Runs asynchronously so that
     * it does not block the calling thread (typically a scheduler task).
     */
    @Async
    public void reindexAll() {
        log.info("Starting full reindex of all books and authors");

        List<Book> allBooks = bookRepository.findAll();
        allBooks.forEach(this::indexBook);
        log.info("Reindexed {} books", allBooks.size());

        List<Author> allAuthors = authorRepository.findAll();
        allAuthors.forEach(this::indexAuthor);
        log.info("Reindexed {} authors", allAuthors.size());
    }

    /**
     * Returns all index entries whose {@code lastIndexed} is before the
     * given threshold.  Used by the scheduler to identify and clean up
     * records that have not been updated recently.
     */
    @Transactional(readOnly = true)
    public List<SearchIndex> findStaleEntries(LocalDateTime threshold) {
        return repository.findStaleIndexEntries(threshold);
    }

    /**
     * Builds an aggregate map of index statistics for the admin dashboard.
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getIndexStats() {
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalEntries",  repository.count());
        stats.put("bookEntries",   repository.findByEntityType(EntityType.BOOK).size());
        stats.put("authorEntries", repository.findByEntityType(EntityType.AUTHOR).size());
        stats.put("staleEntries",
                repository.findStaleIndexEntries(LocalDateTime.now().minusDays(7)).size());
        return stats;
    }

    // -------------------------------------------------------------------------
    // Private content builders
    // -------------------------------------------------------------------------

    private String buildBookContent(Book book) {
        StringBuilder sb = new StringBuilder();
        sb.append(book.getTitle()).append(" ");
        if (book.getSubtitle()    != null) sb.append(book.getSubtitle()).append(" ");
        if (book.getDescription() != null) sb.append(book.getDescription()).append(" ");
        if (book.getIsbn()        != null) sb.append(book.getIsbn()).append(" ");
        if (book.getLanguage()    != null) sb.append(book.getLanguage()).append(" ");
        if (book.getAuthors()     != null) {
            book.getAuthors().forEach(a ->
                    sb.append(a.getFirstName()).append(" ").append(a.getLastName()).append(" "));
        }
        if (book.getGenres() != null) {
            book.getGenres().forEach(g -> sb.append(g.getName()).append(" "));
        }
        if (book.getPublisher() != null) {
            sb.append(book.getPublisher().getName()).append(" ");
        }
        return sb.toString().trim();
    }

    private String buildBookTags(Book book) {
        if (book.getGenres() == null || book.getGenres().isEmpty()) return "";
        return book.getGenres().stream()
                .map(g -> g.getName())
                .collect(Collectors.joining(","));
    }
}
