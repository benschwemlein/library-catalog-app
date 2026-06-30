package com.example.library.pattern.observer;

import org.springframework.context.ApplicationEvent;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Base class for all library domain events published via Spring's ApplicationEventPublisher.
 *
 * <p>Every event carries a correlation ID (for request tracing across async listeners)
 * and a timestamp recording when the event was created.</p>
 */
public abstract class LibraryEvent extends ApplicationEvent {

    private final String correlationId;
    private final LocalDateTime timestamp;

    protected LibraryEvent(Object source) {
        super(source);
        this.correlationId = UUID.randomUUID().toString();
        this.timestamp = LocalDateTime.now();
    }

    protected LibraryEvent(Object source, String correlationId) {
        super(source);
        this.correlationId = correlationId;
        this.timestamp = LocalDateTime.now();
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public LocalDateTime getEventTimestamp() {
        return timestamp;
    }

    /**
     * Human-readable event name used for audit logging and statistics.
     * Defaults to the simple class name but subclasses may override.
     */
    public String getEventName() {
        return this.getClass().getSimpleName();
    }
}
