package io.mosip.kernel.core.cbeffutil.common;

import io.mosip.kernel.core.cbeffutil.exception.CbeffException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
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

    private static final Logger LOGGER = LoggerFactory.getLogger(CbeffXSDValidator.class);

    /** Singleton, hardened SchemaFactory (W3C XML Schema). */
    private static final SchemaFactory SCHEMA_FACTORY;

    /** Cache of compiled Schemas keyed by a fast checksum of XSD bytes. */
    private static final ConcurrentHashMap<String, Schema> SCHEMA_CACHE = new ConcurrentHashMap<>();

    private static final ConcurrentHashMap<Schema, ThreadLocal<Validator>> TL_VALIDATORS = new ConcurrentHashMap<>();

    static {
        SCHEMA_FACTORY = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        try {
            LOGGER.info("Initializing hardened SchemaFactory...");
            // Security features
            SCHEMA_FACTORY.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            SCHEMA_FACTORY.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);

            // Block all external resource resolution
            SCHEMA_FACTORY.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            SCHEMA_FACTORY.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
            LOGGER.info("SchemaFactory initialized with security features.");
        } catch (Exception e) {
            LOGGER.error("Failed to harden SchemaFactory: {}", e.getMessage(), e);
            throw new IllegalStateException("Failed to harden SchemaFactory for XSD validation", e);
        }
    }

    private CbeffXSDValidator() {
        // Utility class; no instances.
    }

    public static boolean validateXML(byte[] xsdBytes, byte[] xmlBytes) throws Exception {
        LOGGER.debug("validateXML invoked with xsdBytes length={} and xmlBytes length={}",
                (xsdBytes != null ? xsdBytes.length : null),
                (xmlBytes != null ? xmlBytes.length : null));
        LOGGER.debug("XML Content xml bytes :\n{}", new String(xmlBytes, StandardCharsets.UTF_8));
        LOGGER.debug("XML Content xsd bytes :\n{}", new String(xsdBytes, StandardCharsets.UTF_8));


        requireNonEmpty(xsdBytes, "xsdBytes");
        requireNonEmpty(xmlBytes, "xmlBytes");

        final Schema schema = getOrCompileSchema(xsdBytes);
        LOGGER.debug("Schema obtained: {}", schema);

        final Validator validator = getValidator(schema);
        LOGGER.debug("Validator created: {}", validator);

        try {
            validator.validate(new StreamSource(new ByteArrayInputStream(xmlBytes), "memory:xml"));
            LOGGER.info("XML validation successful.");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("XML validation failed: {}", e.getMessage(), e);
            throw e;
        }
    }

    public static boolean validateXML(final Schema schema, final byte[] xmlBytes) throws Exception {
        Objects.requireNonNull(schema, "schema");
        LOGGER.debug("validateXML with precompiled schema={} xmlBytes length={}", schema,
                (xmlBytes != null ? xmlBytes.length : null));

        requireNonEmpty(xmlBytes, "xmlBytes");

        final Validator validator = getValidator(schema);
        LOGGER.debug("Validator created: {}", validator);

        try {
            validator.validate(new StreamSource(new ByteArrayInputStream(xmlBytes), "memory:xml"));
            LOGGER.info("XML validation successful with precompiled schema.");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("Validation failed with precompiled schema: {}", e.getMessage(), e);
            throw e;
        }
    }

    public static Schema compileSchema(final byte[] xsdBytes) throws CbeffException {
        requireNonEmpty(xsdBytes, "xsdBytes");
        LOGGER.debug("Compiling schema from xsdBytes length={}", xsdBytes.length);
        try {
            Schema schema = SCHEMA_FACTORY.newSchema(new StreamSource(new ByteArrayInputStream(xsdBytes), "memory:xsd"));
            LOGGER.info("XSD schema compiled successfully: {}", schema);
            return schema;
        } catch (Exception e) {
            LOGGER.error("Failed to compile XSD schema: {}", e.getMessage(), e);
            throw new CbeffException("Failed to compile XSD schema::" + e.getLocalizedMessage());
        }
    }

    private static Schema getOrCompileSchema(final byte[] xsdBytes) throws CbeffException {
        final String key = checksumKey(xsdBytes);
        LOGGER.debug("Checksum key for schema: {}", key);

        Schema cached = SCHEMA_CACHE.get(key);
        if (cached != null) {
            LOGGER.debug("Schema found in cache for key={}", key);
            return cached;
        }
        LOGGER.debug("Schema not found in cache. Compiling new schema for key={}", key);
        final Schema compiled = compileSchema(xsdBytes);
        final Schema prior = SCHEMA_CACHE.putIfAbsent(key, compiled);
        return (prior != null) ? prior : compiled;
    }

    private static String checksumKey(final byte[] data) {
        final CRC32 crc = new CRC32();
        crc.update(data, 0, data.length);
        String key = data.length + ":" + Long.toUnsignedString(crc.getValue());
        LOGGER.debug("Generated checksumKey={}", key);
        return key;
    }

    private static void requireNonEmpty(final byte[] arr, final String name) {
        if (arr == null || arr.length == 0) {
            LOGGER.error("{} must not be null or empty", name);
            throw new IllegalArgumentException(name + " must not be null or empty");
        }
        LOGGER.trace("{} length={}", name, arr.length);
    }

    private static Validator getValidator(Schema schema) {
        Validator validator = TL_VALIDATORS
                .computeIfAbsent(schema, s -> ThreadLocal.withInitial(s::newValidator))
                .get();
        LOGGER.debug("Validator instance hash={} for schema={}", validator.hashCode(), schema);
        return validator;
    }
}