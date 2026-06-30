package com.example.library.search;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Scheduled tasks that keep the search index healthy.
 *
 * Two jobs run on a fixed cron schedule:
 * <ul>
 *   <li>Weekly full re-index — rebuilds every entry from source entities.</li>
 *   <li>Daily stale-entry cleanup — removes entries not updated in 30 days.</li>
 * </ul>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SearchIndexScheduler {

    private final SearchIndexService searchIndexService;

    /**
     * Triggers a full re-index of all books and authors every Sunday at 2:00 AM.
     * The actual work runs asynchronously inside {@link SearchIndexService#reindexAll()}.
     */
    @Scheduled(cron = "0 0 2 * * SUN")
    public void weeklyFullReindex() {
        log.info("SearchIndexScheduler: starting weekly full search reindex");
        searchIndexService.reindexAll();
        log.info("SearchIndexScheduler: weekly full reindex triggered asynchronously");
    }

    /**
     * Removes stale index entries (not updated in 30 days) every day at 3:00 AM.
     * Entries become stale when their source entity is deleted without a
     * corresponding index removal call, or when an entity has not been
     * re-indexed within the expected rolling window.
     */
    @Scheduled(cron = "0 0 3 * * *")
    public void dailyStaleIndexCleanup() {
        log.info("SearchIndexScheduler: running daily stale search index cleanup");

        LocalDateTime threshold = LocalDateTime.now().minusDays(30);
        List<SearchIndex> stale = searchIndexService.findStaleEntries(threshold);

        log.info("SearchIndexScheduler: found {} stale search index entries to remove",
                stale.size());

        for (SearchIndex entry : stale) {
            try {
                searchIndexService.removeFromIndex(entry.getEntityType(), entry.getEntityId());
            } catch (Exception e) {
                log.error("SearchIndexScheduler: failed to remove stale entry id={}: {}",
                        entry.getId(), e.getMessage(), e);
            }
        }

        log.info("SearchIndexScheduler: stale index cleanup complete — removed {} entries",
                stale.size());
    }
}
