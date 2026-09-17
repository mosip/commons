package io.mosip.kernel.core.websub.model;

import lombok.Data;

import java.util.Map;

/**
 * WebSub / MOSIP event payload describing a domain occurrence.
 * <p>
 * Contract: nested under {@link EventModel}. Fields may be null depending on
 * the publisher. {@code data} holds event-specific attributes. Does not
 * perform I/O.
 * </p>
 *
 * @see EventModel
 * @see Type
 */
@Data
public class Event {

    /**
     * Unique event identifier; may be null if the publisher omitted it.
     */
    private String id;
    /**
     * Correlation / transaction identifier; may be null.
     */
    private String transactionId;
    /**
     * Event type namespace and name; may be null.
     */
    private Type type;
    /**
     * Event time as an ISO-8601 string; may be null.
     */
    private String timestamp;
    /**
     * URI to fetch large payloads via data-share; may be null.
     */
    private String dataShareUri;
    /**
     * Event-specific attributes; may be null or empty.
     */
    private Map<String, Object> data;
}
