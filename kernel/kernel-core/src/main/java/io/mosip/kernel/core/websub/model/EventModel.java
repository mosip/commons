package io.mosip.kernel.core.websub.model;

import lombok.Data;

/**
 * WebSub notification envelope published to a MOSIP topic.
 * <p>
 * Contract: used by {@link io.mosip.kernel.core.websub.spi.PublisherClient}.
 * {@code topic} must be non-blank when publishing. Nested {@code event} may
 * be null for heartbeats. Does not perform I/O.
 * </p>
 *
 * @see Event
 */
@Data
public class EventModel {

    /**
     * Publisher identifier; may be null.
     */
    private String publisher;
    /**
     * WebSub topic name; must be non-blank when publishing.
     */
    private String topic;
    /**
     * Publication time as an ISO-8601 string; may be null.
     */
    private String publishedOn;
    /**
     * Domain event payload; may be null.
     */
    private Event event;
}
