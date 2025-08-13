/**
 *
 */
package io.mosip.kernel.core.cbeffutil.common;

/**
 * DateAdapter.java
 *
 * <p><b>Purpose</b> — JAXB adapter to (un)marshal {@link java.time.LocalDateTime}
 * to/from an ISO‑8601 string in <em>UTC</em> (a.k.a. RFC‑3339 with a trailing {@code Z}).</p>
 *
 * <h2>Contract & Semantics</h2>
 * <ul>
 *   <li><b>XML → Java (unmarshal):</b> Accepts any ISO‑8601 datetime with an explicit offset
 *       or zone (e.g., {@code 2025-08-13T06:12:03Z} or {@code 2025-08-13T11:42:03+05:30}).
 *       The value is converted to <em>UTC</em> and returned as a {@link LocalDateTime}
 *       representing the <em>UTC wall‑clock</em> time (i.e., zone‑less, but assumed UTC).</li>
 *   <li><b>Java → XML (marshal):</b> Treats the provided {@link LocalDateTime} as a UTC
 *       timestamp and emits an ISO‑8601 string with {@code Z} (e.g., {@code 2025-08-13T06:12:03Z}).</li>
 * </ul>
 *
 * <p><b>Why LocalDateTime?</b> JAXB commonly maps datetimes to strings. Internally, many MOSIP
 * components keep UTC as {@code LocalDateTime}. This adapter preserves that convention while
 * making the XML wire format unambiguously UTC.</p>
 *
 * <h3>Examples</h3>
 * <pre>{@code
 * // Unmarshal (XML -> LocalDateTime[UTC])
 * LocalDateTime ldt = new DateAdapter().unmarshal("2025-08-13T11:42:03+05:30");
 * // ldt == 2025-08-13T06:12:03  (UTC)
 *
 * // Marshal (LocalDateTime[UTC] -> XML)
 * String s = new DateAdapter().marshal(LocalDateTime.of(2025, 8, 13, 6, 12, 3));
 * // s == "2025-08-13T06:12:03Z"
 * }</pre>
 *
 * <h3>Caveats</h3>
 * <ul>
 *   <li>This adapter assumes the provided {@code LocalDateTime} is already in UTC.
 *       If you hold local‑zone datetimes, convert them first (e.g., via {@code atZone(zone).withZoneSameInstant(UTC)}).</li>
 *   <li>Fractional seconds are preserved by {@link java.time.format.DateTimeFormatter#ISO_INSTANT} when present.</li>
 * </ul>
 */
import java.time.*;
import java.time.format.DateTimeFormatter;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public class DateAdapter extends XmlAdapter<String, LocalDateTime> {

    // Thread-safe formatters provided by java.time
    private static final DateTimeFormatter PARSER = DateTimeFormatter.ISO_DATE_TIME; // accepts offsets/zones
    private static final DateTimeFormatter PRINTER = DateTimeFormatter.ISO_INSTANT;  // prints with trailing 'Z'

    /**
     * Converts an ISO‑8601 datetime string (with offset or zone) into a UTC {@link LocalDateTime}.
     *
     * <p>Examples of accepted inputs:</p>
     * <ul>
     *   <li>{@code 2025-08-13T06:12:03Z}</li>
     *   <li>{@code 2025-08-13T11:42:03+05:30}</li>
     *   <li>{@code 2025-08-13T08:12:03-02:00}</li>
     * </ul>
     *
     * @param v ISO‑8601 datetime string with an explicit offset or zone; may be {@code null} or empty
     * @return {@link LocalDateTime} representing the same instant in UTC; returns {@code null} if input is {@code null} or empty
     * @throws Exception if the input cannot be parsed
     */
    @Override
    public LocalDateTime unmarshal(String v) throws Exception {
        if (v == null || v.isEmpty()) {
            return null;
        }
        // Parse with flexible ISO_DATE_TIME, normalize to UTC, then drop zone to get a UTC LocalDateTime
        OffsetDateTime odt = OffsetDateTime.parse(v, PARSER).withOffsetSameInstant(ZoneOffset.UTC);
        return odt.toLocalDateTime();
    }

    /**
     * Converts a UTC {@link LocalDateTime} into an ISO‑8601 string with a trailing {@code Z}.
     *
     * <p>Assumes the input {@code LocalDateTime} is UTC. If your value represents a local timezone,
     * convert it to UTC before calling this method.</p>
     *
     * @param v UTC {@link LocalDateTime}; may be {@code null}
     * @return ISO‑8601 string (e.g., {@code 2025-08-13T06:12:03Z}); returns {@code null} if input is {@code null}
     * @throws Exception never in normal operation; declared for JAXB compatibility
     */
    @Override
    public String marshal(LocalDateTime v) throws Exception {
        if (v == null) {
            return null;
        }
        // Treat the LocalDateTime as UTC and print as an Instant with 'Z'
        return v.atOffset(ZoneOffset.UTC).toInstant().toString(); // equivalent to PRINTER.format(...)
    }
}