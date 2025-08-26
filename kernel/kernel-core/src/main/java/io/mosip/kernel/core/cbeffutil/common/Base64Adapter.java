package io.mosip.kernel.core.cbeffutil.common;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import io.mosip.kernel.core.util.CryptoUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base64Adapter.java
 *
 * This class is a JAXB {@link javax.xml.bind.annotation.adapters.XmlAdapter}
 * implementation that bridges between {@code String} (Base64-encoded data) and
 * {@code byte[]} (binary data) for XML marshalling and unmarshalling processes.
 *
 * <p>
 * Unlike the default JAXB Base64 handling (which relies on internal or
 * {@link java.util.Base64} implementations), this adapter delegates Base64
 * encoding/decoding to {@link io.mosip.kernel.core.util.CryptoUtil}, ensuring:
 * </p>
 * <ul>
 *     <li>Consistency across MOSIP components by using a single, vetted utility.</li>
 *     <li>Compliance with MOSIP’s cryptographic coding standards.</li>
 *     <li>Potential optimizations in {@code CryptoUtil} (e.g., buffer reuse, native acceleration).</li>
 * </ul>
 *
 * <h3>Example Usage</h3>
 * <pre>
 * {@code
 * // Marshalling (byte[] -> Base64 String)
 * String base64String = new Base64Adapter().marshal(binaryData);
 *
 * // Unmarshalling (Base64 String -> byte[])
 * byte[] binaryData = new Base64Adapter().unmarshal(base64String);
 * }
 * </pre>
 *
 * @author
 *     Ramadurai Pandian (original implementation)
 * @since 1.0.0
 */
public class Base64Adapter extends XmlAdapter<String, byte[]> {

    private static final Logger logger = LoggerFactory.getLogger(Base64Adapter.class);


    /**
     * Base64Adapter.java
     *
     * This class is a JAXB {@link javax.xml.bind.annotation.adapters.XmlAdapter}
     * implementation that bridges between {@code String} (Base64-encoded data) and
     * {@code byte[]} (binary data) for XML marshalling and unmarshalling processes.
     *
     * <p>
     * Unlike the default JAXB Base64 handling (which relies on internal or
     * {@link java.util.Base64} implementations), this adapter delegates Base64
     * encoding/decoding to {@link io.mosip.kernel.core.util.CryptoUtil}, ensuring:
     * </p>
     * <ul>
     *     <li>Consistency across MOSIP components by using a single, vetted utility.</li>
     *     <li>Compliance with MOSIP’s cryptographic coding standards.</li>
     *     <li>Potential optimizations in {@code CryptoUtil} (e.g., buffer reuse, native acceleration).</li>
     * </ul>
     *
     * <h3>Example Usage</h3>
     * <pre>
     * {@code
     * // Marshalling (byte[] -> Base64 String)
     * String base64String = new Base64Adapter().marshal(binaryData);
     *
     * // Unmarshalling (Base64 String -> byte[])
     * byte[] binaryData = new Base64Adapter().unmarshal(base64String);
     * }
     * </pre>
     *
     * @author
     *     Ramadurai Pandian (original implementation)
     * @since 1.0.0
     */

    @Override
    public byte[] unmarshal(String data) throws Exception {
        logger.info("Unmarshalling Base64 data");
        logger.info("start unmarshal");
        logger.info("data: {}", data);
        logger.info("end unmarshal");
        return CryptoUtil.decodeBase64(data);
    }

    /**
     * Encodes binary data into its Base64 string representation.
     *
     * @param data
     *     Raw biometric image data as a {@code byte[]}.
     *     May be {@code null} or empty, in which case an empty string is returned.
     *
     * @return
     *     Base64-encoded string representation of the given data.
     *     Never {@code null} — returns an empty string if input is {@code null} or empty.
     *
     * @throws Exception
     *     If encoding fails for any reason.
     */
    @Override
    public String marshal(byte[] data) throws Exception {
        String value = CryptoUtil.encodeBase64String(data);
        logger.info("marshalling Base64 data");
        logger.info("start marshal");
        logger.info("value: {}", value);
        logger.info("end marshal");
        return value;
    }
}