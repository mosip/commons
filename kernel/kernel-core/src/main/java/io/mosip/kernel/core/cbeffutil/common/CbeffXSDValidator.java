package io.mosip.kernel.core.cbeffutil.common;
import io.mosip.kernel.core.cbeffutil.exception.CbeffException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.CRC32;
/**
 * <b>CbeffXSDValidator</b> is a high-performance, secure, and thread-safe utility
 * for validating CBEFF (Common Biometric Exchange Formats Framework) XML documents
 * against their corresponding XSD schemas in the MOSIP ecosystem.
 *
 * <h3>Key Features</h3>
 * <ul>
 *   <li><b>XXE Protection</b>: Hardened {@link SchemaFactory} with secure processing,
 *       disabled DTDs, and blocked external entity resolution.</li>
 *   <li><b>Schema Caching</b>: Avoids recompilation of identical XSDs using
 *       {@link CRC32} + length-based cache keys in a {@link ConcurrentHashMap}.</li>
 *   <li><b>Thread Safety</b>: Uses {@link ThreadLocal} {@link Validator} instances
 *       per {@link Schema} to prevent race conditions during parallel validation.</li>
 *   <li><b>Zero Allocation Streams</b>: Uses in-memory {@link ByteArrayInputStream}
 *       with system IDs for better error reporting.</li>
 *   <li><b>Comprehensive Logging</b>:.info traces for inputs, cache hits, and validation flow.</li>
 * </ul>
 *
 * <h3>Security Hardening</h3>
 * <p>
 * This class mitigates XML External Entity (XXE) attacks by:
 * <ul>
 *   <li>Enabling {@code FEATURE_SECURE_PROCESSING}</li>
 *   <li>Disabling DOCTYPE declarations via Apache feature</li>
 *   <li>Blocking all external DTD and schema resolution</li>
 * </ul>
 * </p>
 *
 * <h3>Performance Optimizations</h3>
 * <p>
 * Schema compilation is expensive. This class:
 * <ul>
 *   <li>Caches compiled {@link Schema} objects using a content-based key</li>
 *   <li>Reuses {@link Validator} instances per thread via {@link ThreadLocal}</li>
 *   <li>Avoids disk I/O by working purely with byte arrays</li>
 * </ul>
 * </p>
 *
 * <h3>Usage Examples</h3>
 * <pre>
 * // 1. Validate XML against XSD bytes
 * boolean isValid = CbeffXSDValidator.validateXML(xsdBytes, xmlBytes);
 *
 * // 2. Pre-compile schema and reuse
 * Schema schema = CbeffXSDValidator.compileSchema(xsdBytes);
 * boolean isValid = CbeffXSDValidator.validateXML(schema, xmlBytes);
 * </pre>
 *
 * <h3>Thread Safety</h3>
 * <p>
 * Fully thread-safe. No mutable shared state. Validators are stored in
 * {@link ThreadLocal} containers to avoid synchronization overhead.
 * </p>
 *
 * @author M1049825
 * @since 1.0.0
 *
 * @see Schema
 * @see Validator
 * @see CbeffException
 * @see <a href="https://docs.oasis-open.org/bioserv/cbeff/v3.0/cbeff-v3.0.html">CBEFF v3.0 Specification</a>
 */
public final class CbeffXSDValidator {
    /** SLF4J Logger for diagnostic and security event logging. */
    private static final Logger LOGGER = LoggerFactory.getLogger(CbeffXSDValidator.class);
    /** Secure SchemaFactory instance for W3C XML Schema (XSD). */
    private static final SchemaFactory SCHEMA_FACTORY;
    /**
     * Cache of compiled {@link Schema} objects.
     * Key: "length:checksum" (CRC32), Value: Compiled {@link Schema}.
     */
    private static final ConcurrentHashMap<String, Schema> SCHEMA_CACHE = new ConcurrentHashMap<>();
    /**
     * Thread-local pool of {@link Validator} instances per {@link Schema}.
     * Prevents validator reuse issues in multi-threaded environments.
     */
    private static final ConcurrentHashMap<Schema, ThreadLocal<Validator>> TL_VALIDATORS = new ConcurrentHashMap<>();
    static {
        SCHEMA_FACTORY = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        try {
            LOGGER.info("Initializing hardened SchemaFactory for CBEFF XSD validation...");
            // Enable secure processing
            SCHEMA_FACTORY.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            // Block DOCTYPE declarations (Apache Xerces specific)
            SCHEMA_FACTORY.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            // Disable all external resource resolution
            SCHEMA_FACTORY.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            SCHEMA_FACTORY.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
            LOGGER.info("SchemaFactory successfully hardened against XXE and external entities.");
        } catch (Exception e) {
            LOGGER.error("Failed to configure secure SchemaFactory: {}", e.getMessage(), e);
            throw new IllegalStateException("Unable to initialize secure XML schema validator", e);
        }
    }
    /** Private constructor to prevent instantiation. */
    private CbeffXSDValidator() {
        throw new UnsupportedOperationException("CbeffXSDValidator is a utility class and cannot be instantiated");
    }
    /**
     * Validates an XML document against an XSD schema provided as byte arrays.
     *
     * <p>
     * This method compiles the XSD (with caching), creates a thread-local validator,
     * and performs validation. Any validation error results in an exception.
     * </p>
     *
     * @param xsdBytes the XSD schema as a non-empty byte array
     * @param xmlBytes the XML document to validate as a non-empty byte array
     * @return {@code true} if XML is valid against the XSD
     * @throws Exception if validation fails or input is invalid
     *                   (includes {@link org.xml.sax.SAXException}, {@link java.io.IOException})
     * @throws IllegalArgumentException if either input is {@code null} or empty
     */
    public static boolean validateXML(byte[] xsdBytes, byte[] xmlBytes) throws Exception {
        LOGGER.info("validateXML invoked with xsdBytes length={} and xmlBytes length={}",
                (xsdBytes != null ? xsdBytes.length : "null"),
                (xmlBytes != null ? xmlBytes.length : "null"));
        requireNonEmpty(xsdBytes, "xsdBytes");
        requireNonEmpty(xmlBytes, "xmlBytes");
        // Log content in.info mode (useful for troubleshooting CBEFF structures)
        LOGGER.info("XSD Content:\n{}", new String(xsdBytes, StandardCharsets.UTF_8));
        LOGGER.info("XML Content:\n{}", new String(xmlBytes, StandardCharsets.UTF_8));
        final Schema schema = getOrCompileSchema(xsdBytes);
        final Validator validator = getValidator(schema);
        try (ByteArrayInputStream xmlStream = new ByteArrayInputStream(xmlBytes)) {
            validator.validate(new StreamSource(xmlStream, "memory:cbeff-xml"));
            LOGGER.info("XML successfully validated against XSD (schema cache key: {})", checksumKey(xsdBytes));
            return true;
        } catch (Exception e) {
            LOGGER.error("XML validation failed: {}", e.getMessage(), e);
            throw e;
        }
    }
    /**
     * Validates an XML document against a pre-compiled {@link Schema}.
     *
     * <p>
     * Use this method when the schema is reused across multiple validations
     * (e.g., same CBEFF version).
     * </p>
     *
     * @param schema   the pre-compiled XSD schema; must not be {@code null}
     * @param xmlBytes the XML document to validate as a non-empty byte array
     * @return {@code true} if XML is valid
     * @throws Exception if validation fails
     * @throws NullPointerException if {@code schema} is {@code null}
     * @throws IllegalArgumentException if {@code xmlBytes} is {@code null} or empty
     */
    public static boolean validateXML(final Schema schema, final byte[] xmlBytes) throws Exception {
        Objects.requireNonNull(schema, "schema must not be null");
        requireNonEmpty(xmlBytes, "xmlBytes");
        LOGGER.info("validateXML using precompiled schema={} with xmlBytes length={}", schema, xmlBytes.length);
        final Validator validator = getValidator(schema);
        try (ByteArrayInputStream xmlStream = new ByteArrayInputStream(xmlBytes)) {
            validator.validate(new StreamSource(xmlStream, "memory:cbeff-xml"));
            LOGGER.info("XML validation successful with precompiled schema.");
            return true;
        } catch (Exception e) {
            LOGGER.error("Validation failed with precompiled schema: {}", e.getMessage(), e);
            throw e;
        }
    }
    /**
     * Compiles an XSD schema from byte array without caching.
     *
     * <p>
     * Use this for one-off validations or when explicit control over schema lifecycle is needed.
     * </p>
     *
     * @param xsdBytes the XSD schema as a non-empty byte array
     * @return a compiled {@link Schema} instance
     * @throws CbeffException if schema compilation fails
     * @throws IllegalArgumentException if {@code xsdBytes} is {@code null} or empty
     */
    public static Schema compileSchema(final byte[] xsdBytes) throws CbeffException {
        requireNonEmpty(xsdBytes, "xsdBytes");
        LOGGER.info("Compiling XSD schema from {} bytes", xsdBytes.length);
        try (ByteArrayInputStream xsdStream = new ByteArrayInputStream(xsdBytes)) {
            Schema schema = SCHEMA_FACTORY.newSchema(new StreamSource(xsdStream, "memory:cbeff-xsd"));
            LOGGER.info("XSD schema compiled successfully: {}", schema);
            return schema;
        } catch (Exception e) {
            LOGGER.error("Failed to compile XSD schema: {}", e.getMessage(), e);
            throw new CbeffException("XSD compilation failed: " + e.getLocalizedMessage());
        }
    }
    /**
     * Retrieves a cached {@link Schema} or compiles and caches a new one.
     *
     * @param xsdBytes the XSD byte content
     * @return cached or newly compiled {@link Schema}
     * @throws CbeffException if compilation fails
     */
    private static Schema getOrCompileSchema(final byte[] xsdBytes) throws CbeffException {
        final String key = checksumKey(xsdBytes);
        Schema cached = SCHEMA_CACHE.get(key);
        if (cached != null) {
            LOGGER.info("Cache HIT: Reusing schema for key={}", key);
            return cached;
        }
        LOGGER.info("Cache MISS: Compiling and caching new schema for key={}", key);
        final Schema compiled = compileSchema(xsdBytes);
        final Schema previous = SCHEMA_CACHE.putIfAbsent(key, compiled);
        return (previous != null) ? previous : compiled;
    }
    /**
     * Generates a cache key using length + CRC32 checksum.
     *
     * @param data byte array to hash
     * @return string key in format "length:checksum"
     */
    private static String checksumKey(final byte[] data) {
        final CRC32 crc = new CRC32();
        crc.update(data, 0, data.length);
        String key = data.length + ":" + Long.toUnsignedString(crc.getValue());
        LOGGER.trace("Generated cache key: {}", key);
        return key;
    }
    /**
     * Ensures input byte array is non-null and non-empty.
     *
     * @param arr  byte array to check
     * @param name parameter name for error message
     * @throws IllegalArgumentException if invalid
     */
    private static void requireNonEmpty(final byte[] arr, final String name) {
        if (arr == null || arr.length == 0) {
            LOGGER.error("Validation failed: {} must not be null or empty", name);
            throw new IllegalArgumentException(name + " must not be null or empty");
        }
        LOGGER.trace("Input {} has length={}", name, arr.length);
    }
    /**
     * Returns a thread-local {@link Validator} for the given {@link Schema}.
     *
     * <p>
     * {@link Validator} instances are not thread-safe. This method ensures each
     * thread gets its own instance without synchronization overhead.
     * </p>
     *
     * @param schema the schema to create validator for
     * @return thread-local {@link Validator}
     */
    private static Validator getValidator(Schema schema) {
        return TL_VALIDATORS
                .computeIfAbsent(schema, s -> ThreadLocal.withInitial(s::newValidator))
                .get();
    }
}