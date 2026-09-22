package io.mosip.kernel.core.auditmanager.spi;

/**
 * Writes a MOSIP audit request to the configured audit sink.
 * <p>
 * Contract: implementations persist or forward {@code T} (typically an
 * audit-request DTO) without mutating it. Call this SPI after a security- or
 * data-sensitive operation that must be recorded. Side effects are
 * implementation-defined (HTTP to audit-manager, or local persistence).
 * </p>
 *
 * @param <T> type of the audit request payload
 * @author Dharmesh Khandelwal
 * @since 1.0.0
 */
public interface AuditHandler<T> {

    /**
     * Persists or forwards the given audit request.
     * <p>
     * Contract: {@code auditRequest} must be non-null and fully populated.
     * Implementations may perform HTTP or database I/O.
     * </p>
     *
     * @param auditRequest never-null audit payload to write
     * @return {@code true} if the request was written successfully; {@code false}
     *         if the sink rejected it without throwing
     */
    boolean addAudit(T auditRequest);
    
}