package com.example.library.search;

import com.example.library.entity.Book;
import com.example.library.pattern.observer.BookAddedEvent;
import com.example.library.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Spring application-event listener that triggers async search indexing
 * whenever books are added or updated in the catalog.
 *
 * The listener runs asynchronously (via @Async) so that event publishing
 * is never delayed by the indexing write path.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SearchIndexingEventListener {

    private final SearchIndexService searchIndexService;
    private final BookRepository bookRepository;

    /**
     * Indexes the newly added book when a {@link BookAddedEvent} is published.
     * The event carries only the book's ID; we reload the full entity so that
     * lazy-loaded collections (authors, genres) are available to the indexer.
     *
     * @param event the Spring application event published after a book is added
     */
    @EventListener
    @Async
    public void onBookAdded(BookAddedEvent event) {
        log.info("SearchIndexingEventListener: received BookAddedEvent for book id={}",
                event.getBookId());

        bookRepository.findById(event.getBookId()).ifPresentOrElse(
                book -> {
                    searchIndexService.indexBook(book);
                    log.info("SearchIndexingEventListener: indexed book id={} title='{}'",
                            book.getId(), book.getTitle());
                },
                () -> log.warn("SearchIndexingEventListener: book id={} not found in repository;"
                        + " skipping index", event.getBookId())
        );
    }
}
