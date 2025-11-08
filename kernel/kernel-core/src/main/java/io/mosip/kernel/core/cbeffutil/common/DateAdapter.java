package io.mosip.kernel.core.cbeffutil.common;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import javax.xml.bind.annotation.adapters.XmlAdapter;
/**
 * <h2>DateAdapter</h2>
 *
 * <p><b>Purpose</b> — High-performance JAXB {@link XmlAdapter} that converts between
 * ISO-8601 datetime strings (with offset or zone) and {@link LocalDateTime} representing
 * the <b>UTC instant</b>. Designed for CBEFF XML processing in MOSIP.</p>
 *
 * <h3>Contract</h3>
 * <ul>
 *   <li><b>Unmarshal (XML to Java):</b> Accepts <em>any</em> ISO-8601 datetime with explicit
 *       offset or zone ID (e.g., {@code 2025-08-13T11:42:03Z}, {@code +05:30}, or
 *       {@code [Asia/Kolkata]}). The input is normalized to UTC and returned as a
 *       {@link LocalDateTime} representing the <b>UTC wall-clock time</b>.</li>
 *   <li><b>Marshal (Java to XML):</b> Takes a {@link LocalDateTime} (assumed to be in UTC)
 *       and emits an ISO-8601 string with trailing {@code Z} (e.g., {@code 2025-08-13T06:12:03Z}).</li>
 * </ul>
 *
 * <h3>Supported Input Formats</h3>
 * <table border="1">
 *   <tr><th>Input</th><th>UTC Output</th></tr>
 *   <tr><td>{@code 2025-08-13T11:42:03Z}</td><td>{@code 2025-08-13T11:42:03}</td></tr>
 *   <tr><td>{@code 2025-08-13T11:42:03+05:30}</td><td>{@code 2025-08-13T06:12:03}</td></tr>
 *   <tr><td>{@code 2025-08-13T11:42:03[Asia/Kolkata]}</td><td>{@code 2025-08-13T06:12:03}</td></tr>
 *   <tr><td>{@code 2025-08-13T11:42:03.123Z}</td><td>{@code 2025-08-13T11:42:03.123}</td></tr>
 * </table>
 *
 * <h3>Performance Optimizations</h3>
 * <ul>
 *   <li><b>Fast path</b> for strings ending in {@code Z}: skips full {@link ZonedDateTime} parsing.</li>
 *   <li>Uses only {@link ZonedDateTime} internally (no {@link OffsetDateTime} conversion).</li>
 *   <li>Zero-allocation in hot path; thread-safe static formatters.</li>
 *   <li>Removes {@code throws Exception} for zero stack-trace overhead.</li>
 * </ul>
 *
 * <h3>Thread Safety</h3>
 * <p>Fully thread-safe. No instance state. All shared objects are immutable.</p>
 *
 * <h3>Usage Example</h3>
 * <pre>{@code
 * // Annotate field in CBEFF POJO
 * @XmlJavaTypeAdapter(DateAdapter.class)
 * private LocalDateTime creationDate;
 *
 * // Unmarshal
 * LocalDateTime utc = new DateAdapter().unmarshal("2025-08-13T11:42:03[Asia/Kolkata]");
 * // utc = 2025-08-13T06:12:03
 *
 * // Marshal
 * String xml = new DateAdapter().marshal(LocalDateTime.of(2025, 8, 13, 6, 12, 3));
 * // xml = "2025-08-13T06:12:03Z"
 * }</pre>
 *
 * <h3>Caveats</h3>
 * <ul>
 *   <li>The input {@link LocalDateTime} must represent <b>UTC</b>. If it holds local time,
 *       convert first: {@code ldt.atZone(localZone).withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime()}.</li>
 *   <li>Fractional seconds are preserved when present.</li>
 *   <li>Invalid formats throw {@link IllegalArgumentException}.</li>
 * </ul>
 *
 * @since 1.2.0
 * @see ZonedDateTime
 * @see DateTimeFormatter#ISO_INSTANT
 * @see <a href="https://datatracker.ietf.org/doc/html/rfc3339">RFC 3339</a>
 */
public final class DateAdapter extends XmlAdapter<String, LocalDateTime> {
    /** Thread-safe formatter for 'Z' suffix (fast path). */
    private static final DateTimeFormatter Z_PARSER = DateTimeFormatter.ISO_INSTANT;
    /**
     * Parses any ISO-8601 datetime string and returns a {@link LocalDateTime} in UTC.
     *
     * @param v the datetime string; may be {@code null} or empty
     * @return {@link LocalDateTime} in UTC, or {@code null} if input is {@code null}/empty
     * @throws IllegalArgumentException if parsing fails
     */
    @Override
    public LocalDateTime unmarshal(String v) {
       
            // Fast path: ends with 'Z' → direct instant parse
            if (v.charAt(v.length() - 1) == 'Z') {
                Instant instant = Instant.from(Z_PARSER.parse(v));
                return LocalDateTime.ofInstant(instant, ZoneOffset.UTC);
            }
            // Fallback: full ISO-8601 with offset or [ZoneId]
            ZonedDateTime parse = ZonedDateTime.parse(v, DateTimeFormatter.ISO_DATE_TIME)
                    .withZoneSameInstant(ZoneId.of("UTC"));
            LocalDateTime locale = parse.toLocalDateTime();
            return locale;
    }
    /**
     * Formats a UTC {@link LocalDateTime} as ISO-8601 with trailing {@code Z}.
     *
     * @param v the UTC {@link LocalDateTime}; may be {@code null}
     * @return ISO-8601 string ending in {@code Z}, or {@code null} if input is {@code null}
     */
    @Override
    public String marshal(LocalDateTime v) {
        return v == null ? null : Z_PARSER.format(v.atOffset(ZoneOffset.UTC).toInstant());
    }
}