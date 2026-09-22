package io.mosip.kernel.core.websub.model;

import lombok.Data;

/**
 * Namespaced event-type identifier used inside a WebSub {@link Event}.
 * <p>
 * Contract: {@code namespace} and {@code name} together identify the event
 * kind (for example MOSIP credential status). Either field may be null on
 * partial payloads. Does not perform I/O.
 * </p>
 *
 * @see Event
 */
@Data
public class Type {

    /**
     * Type namespace URI or MOSIP module name; may be null.
     */
    private String namespace;
    /**
     * Local type name; may be null.
     */
    private String name;
}
