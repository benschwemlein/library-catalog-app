package com.example.library.pattern.observer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Observes checkout and return events to feed the recommendation engine.
 *
 * <p>In a full implementation these handlers would call a recommendation service
 * (e.g., collaborative filtering or content-based model) to update affinity scores.
 * Here they log the trigger so the data pipeline can pick it up.</p>
 */
@Component
@Slf4j
public class RecommendationEventListener {

    /**
     * Record that a member checked out a book so the recommendation model can
     * strengthen the member-book affinity signal.
     */
    @Async
    @EventListener
    public void onBookCheckedOut(BookCheckedOutEvent event) {
        log.info("[Recommendations] Checkout signal: triggering recommendation data update " +
                "for memberId={} bookId={} loanId={} correlationId={}",
                event.getMemberId(), event.getBookId(), event.getLoanId(), event.getCorrelationId());

        // In production: recommendationService.recordCheckout(event.getMemberId(), event.getBookId());
        // This would update a collaborative-filtering matrix or enqueue a message for a
        // downstream ML pipeline.
    }

    /**
     * Record that a member returned a book. Returning promptly (not overdue) is a
     * positive signal; returns after a very long loan may indicate low engagement.
     */
    @Async
    @EventListener
    public void onBookReturned(BookReturnedEvent event) {
        log.info("[Recommendations] Return signal: recording return data for memberId={} bookId={} " +
                "wasOverdue={} loanId={} correlationId={}",
                event.getMemberId(), event.getBookId(),
                event.isWasOverdue(), event.getLoanId(), event.getCorrelationId());

        // In production: recommendationService.recordReturn(event.getMemberId(), event.getBookId(),
        //     event.getReturnDate(), event.isWasOverdue());
    }
}
