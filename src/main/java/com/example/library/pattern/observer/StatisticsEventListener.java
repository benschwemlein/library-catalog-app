package com.example.library.pattern.observer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;

/**
 * Maintains in-memory per-event-type counters for operational statistics.
 *
 * <p>Counters are keyed by the event class simple name (e.g. "BookCheckedOutEvent").
 * The map uses {@link ConcurrentHashMap} with {@link LongAdder} values so that
 * concurrent updates from the async listener thread pool are safe without locking.</p>
 */
@Component
@Slf4j
public class StatisticsEventListener {

    private final ConcurrentHashMap<String, LongAdder> eventCounts = new ConcurrentHashMap<>();

    /**
     * Increment the counter for the received event type.
     */
    @Async
    @EventListener
    public void onAnyEvent(LibraryEvent event) {
        String eventType = event.getClass().getSimpleName();
        eventCounts.computeIfAbsent(eventType, k -> new LongAdder()).increment();
        log.debug("[Statistics] Recorded event={} total={}",
                eventType, eventCounts.get(eventType).sum());
    }

    /**
     * Return a snapshot of event counts keyed by event class name.
     * The returned map is a read-only copy; values reflect counts at the time of the call.
     */
    public Map<String, Long> getStatistics() {
        Map<String, Long> snapshot = new java.util.HashMap<>();
        eventCounts.forEach((k, v) -> snapshot.put(k, v.sum()));
        return Collections.unmodifiableMap(snapshot);
    }

    /**
     * Reset all counters to zero. Useful between test runs or reporting periods.
     */
    public void resetStatistics() {
        log.info("[Statistics] Resetting all event counters");
        eventCounts.clear();
    }

    /**
     * Return the count for a specific event type by class name.
     *
     * @param eventTypeName the simple class name (e.g. "BookCheckedOutEvent")
     * @return current count, or 0 if the event type has never been observed
     */
    public long getCount(String eventTypeName) {
        LongAdder adder = eventCounts.get(eventTypeName);
        return adder != null ? adder.sum() : 0L;
    }
}
