package io.mosip.kernel.core.cbeffutil.common;

import io.mosip.kernel.core.cbeffutil.exception.CbeffException;

import java.io.ByteArrayInputStream;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.CRC32;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

/**
 * @author M1049825
 *
 */
public class CbeffXSDValidator {

    /** Singleton, hardened SchemaFactory (W3C XML Schema). */
    private static final SchemaFactory SCHEMA_FACTORY;

    /** Cache of compiled Schemas keyed by a fast checksum of XSD bytes. */
    private static final ConcurrentHashMap<String, Schema> SCHEMA_CACHE = new ConcurrentHashMap<>();

    /** Pool of Validators for thread-safe validation, keyed by Schema. */
    private static final ConcurrentHashMap<Schema, Validator> VALIDATOR_POOL = new ConcurrentHashMap<>();

    static {
        SCHEMA_FACTORY = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        try {
            // Security features
            SCHEMA_FACTORY.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            SCHEMA_FACTORY.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);

            // Block all external resource resolution
            SCHEMA_FACTORY.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            SCHEMA_FACTORY.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
            // Some parsers also honor this for stylesheet imports; safe to set.
            //SCHEMA_FACTORY.setProperty(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
        } catch (Exception e) {
            // If a particular implementation does not support a property/feature,
            // we surface an explicit failure—better to fail closed than run insecurely.
            throw new IllegalStateException("Failed to harden SchemaFactory for XSD validation", e);
        }
    }

    private CbeffXSDValidator() {
        // Utility class; no instances.
    }
    /**
     * Validates an XML document (as bytes) against an XSD (as bytes).
     * <p>The XSD is compiled once and cached for reuse across calls.</p>
     *
     * @param xsdBytes the XSD content in bytes (must not be {@code null} or empty)
     * @param xmlBytes the XML content in bytes (must not be {@code null} or empty)
     * @return {@code true} if validation succeeds (otherwise an exception is thrown)
     * @throws Exception if compilation or validation fails
     */
    public static boolean validateXML(byte[] xsdBytes, byte[] xmlBytes) throws Exception {
        requireNonEmpty(xsdBytes, "xsdBytes");
        requireNonEmpty(xmlBytes, "xmlBytes");

        final Schema schema = getOrCompileSchema(xsdBytes);
        final Validator validator = getValidator(schema);
        validator.validate(new StreamSource(new ByteArrayInputStream(xmlBytes)));
        return true;
    }

    /**
     * Validates an XML document (as bytes) against a precompiled {@link Schema}.
     * <p>Use this overload when validating many XML documents against the same XSD
     * to avoid any cache lookups and maximize throughput.</p>
     *
     * @param schema   precompiled schema from {@link #compileSchema(byte[])}
     * @param xmlBytes the XML content in bytes
     * @return {@code true} if validation succeeds
     * @throws Exception if validation fails
     */
    public static boolean validateXML(final Schema schema, final byte[] xmlBytes) throws Exception {
        Objects.requireNonNull(schema, "schema");
        requireNonEmpty(xmlBytes, "xmlBytes");

        final Validator validator = getValidator(schema);
        validator.validate(new StreamSource(new ByteArrayInputStream(xmlBytes)));
        return true;
    }

    /**
     * Compiles an XSD (bytes) into a reusable {@link Schema} with hardened settings.
     * <p>The resulting {@code Schema} is thread-safe and can be cached by the caller.</p>
     *
     * @param xsdBytes XSD bytes
     * @return compiled, thread-safe {@link Schema}
     * @throws CbeffException if compilation fails
     */
    public static Schema compileSchema(final byte[] xsdBytes) throws CbeffException {
        requireNonEmpty(xsdBytes, "xsdBytes");
        try {
            return SCHEMA_FACTORY.newSchema(new StreamSource(new ByteArrayInputStream(xsdBytes)));
        } catch (Exception e) {
            throw new CbeffException("Failed to compile XSD schema::"+ e.getLocalizedMessage());
        }
    }

    private static Schema getOrCompileSchema(final byte[] xsdBytes) throws CbeffException {
        final String key = checksumKey(xsdBytes);
        Schema cached = SCHEMA_CACHE.get(key);
        if (cached != null) {
            return cached;
        }
        // Compile and publish to cache.
        final Schema compiled = compileSchema(xsdBytes);
        final Schema prior = SCHEMA_CACHE.putIfAbsent(key, compiled);
        return (prior != null) ? prior : compiled;
    }

    /** Fast CRC32-based key; includes length to reduce accidental collisions further. */
    private static String checksumKey(final byte[] data) {
        final CRC32 crc = new CRC32();
        crc.update(data, 0, data.length);
        // format: "<len>:<crc>"
        return data.length + ":" + Long.toUnsignedString(crc.getValue());
    }

    private static Validator getValidator(Schema schema) {
        return VALIDATOR_POOL.computeIfAbsent(schema, Schema::newValidator);
    }

    private static void requireNonEmpty(final byte[] arr, final String name) {
        if (arr == null || arr.length == 0) {
            throw new IllegalArgumentException(name + " must not be null or empty");
        }
    }
}
