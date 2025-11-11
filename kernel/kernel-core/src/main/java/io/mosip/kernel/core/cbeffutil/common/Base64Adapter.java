package io.mosip.kernel.core.cbeffutil.common;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import io.mosip.kernel.core.util.CryptoUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <b>Base64Adapter</b> is a JAXB {@link XmlAdapter} implementation that customizes
 * the marshalling and unmarshalling of {@code byte[]} data to/from Base64-encoded
 * {@link String} representations using MOSIP's internal cryptographic utility
 * ({@link CryptoUtil}).
 *
 * <h3>Purpose</h3>
 * <p>
 * JAXB, by default, uses its own Base64 encoding/decoding mechanism when binding
 * {@code byte[]} fields in XML. This can lead to inconsistencies in Base64 output
 * (e.g., line breaks, padding, or encoding variants) across different environments
 * or JAXB implementations. In MOSIP, consistent and secure Base64 handling is
 * critical—especially for biometric data embedded in CBEFF (Common Biometric
 * Exchange Formats Framework) XML structures.
 * </p>
 *
 * <p>
 * This adapter <i>bypasses</i> JAXB's default Base64 encoder/decoder and delegates
 * the task to {@link CryptoUtil}, ensuring:
 * <ul>
 *   <li>Uniform Base64 encoding across all MOSIP modules</li>
 *   <li>Compliance with MOSIP security and formatting standards</li>
 *   <li>Proper handling of biometric image payloads in CBEFF</li>
 * </ul>
 * </p>
 *
 * <h3>Usage in JAXB Context</h3>
 * <p>
 * Annotate {@code byte[]} fields in CBEFF-related POJOs with:
 * </p>
 * <pre>
 * &#64;XmlJavaTypeAdapter(Base64Adapter.class)
 * private byte[] imageData;
 * </pre>
 *
 * <h3>Thread Safety</h3>
 * <p>
 * This class is thread-safe as it contains no mutable state and delegates to
 * stateless utility methods in {@link CryptoUtil}.
 * </p>
 *
 * @author Ramadurai Pandian
 * @since 1.0.0
 *
 * @see XmlAdapter
 * @see CryptoUtil
 * @see <a href="https://www.ibm.com/docs/en/was-liberty/base?topic=liberty-common-biometric-exchange-formats-framework-cbeff">CBEFF Specification</a>
 */
public class Base64Adapter extends XmlAdapter<String, byte[]> {

    private static final Logger LOGGER = LoggerFactory.getLogger(Base64Adapter.class);
    /**
     * Converts a Base64-encoded {@link String} from XML into a {@code byte[]} during
     * JAXB unmarshalling.
     *
     * <p>
     * This method is invoked automatically by JAXB when reading XML data into a Java
     * object. The input string is expected to be a valid Base64 representation of
     * binary biometric data (e.g., fingerprint, iris, or face image).
     * </p>
     *
     * @param data the Base64-encoded string read from XML; may be {@code null}
     * @return the decoded byte array representing raw biometric image data,
     *         or {@code null} if input is {@code null}
     * @throws Exception if the input string is not valid Base64 or decoding fails
     *                   (e.g., due to corruption or invalid padding)
     *
     * @implNote Delegates to {@link CryptoUtil#decodeBase64(String)} to ensure
     *           MOSIP-compliant decoding.
     */
    @Override
    public byte[] unmarshal(String data) throws Exception {
        LOGGER.info("Unmarshalling Base64 data");
        LOGGER.info("start unmarshal");
        LOGGER.info("data: {}", data.substring(0,10));
        LOGGER.info("end unmarshal");
        return CryptoUtil.decodeBase64(data);
    }
    /**
     * Converts a {@code byte[]} biometric image into a Base64-encoded {@link String}
     * during JAXB marshalling.
     *
     * <p>
     * This method is called by JAXB when serializing a Java object to XML. The
     * resulting Base64 string will be embedded directly in the CBEFF XML structure.
     * </p>
     *
     * @param data the raw biometric image byte array to encode; may be {@code null}
     * @return a standard Base64-encoded string (without line breaks),
     *         or {@code null} if input is {@code null}
     * @throws Exception if encoding fails due to internal errors in the crypto utility
     *
     * @implNote Uses {@link CryptoUtil#encodeBase64String(byte[])} to produce
     *           URL-safe, standardized Base64 output compliant with MOSIP specifications.
     */
    @Override
    public String marshal(byte[] data) throws Exception {
        String value = CryptoUtil.encodeBase64String(data);
        LOGGER.info("marshalling Base64 data");
        LOGGER.info("start marshal");
        LOGGER.info("value: {}", value.substring(0,10));
        LOGGER.info("end marshal");
        return value;
    }
}