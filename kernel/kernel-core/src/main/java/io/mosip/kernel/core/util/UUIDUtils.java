package io.mosip.kernel.core.util;

import io.mosip.kernel.core.util.constant.HMACUtilConstants;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;
import java.util.UUID;

/**
 * RFC 4122 type-5 UUID generation from a namespace and name.
 * <p>
 * Contract: uses SHA-256 then truncates to 128 bits with version 5 and IETF
 * variant bits. {@code namespace} and {@code name} must be non-null. Does not
 * perform I/O.
 * </p>
 *
 * @author Bal Vikash Sharma
 */
public class UUIDUtils {

    private static final Charset UTF8 = StandardCharsets.UTF_8;

    /**
     * RFC 4122 DNS namespace UUID.
     */
    public static final UUID NAMESPACE_DNS = UUID.fromString("6ba7b810-9dad-11d1-80b4-00c04fd430c8");
    /**
     * RFC 4122 URL namespace UUID.
     */
    public static final UUID NAMESPACE_URL = UUID.fromString("6ba7b811-9dad-11d1-80b4-00c04fd430c8");
    /**
     * RFC 4122 OID namespace UUID.
     */
    public static final UUID NAMESPACE_OID = UUID.fromString("6ba7b812-9dad-11d1-80b4-00c04fd430c8");
    /**
     * RFC 4122 X.500 namespace UUID.
     */
    public static final UUID NAMESPACE_X500 = UUID.fromString("6ba7b814-9dad-11d1-80b4-00c04fd430c8");

    // Precompute namespace bytes once (hot path win)
    private static final byte[] NS_DNS_BYTES  = toBytes(NAMESPACE_DNS);
    private static final byte[] NS_URL_BYTES  = toBytes(NAMESPACE_URL);
    private static final byte[] NS_OID_BYTES  = toBytes(NAMESPACE_OID);
    private static final byte[] NS_X500_BYTES = toBytes(NAMESPACE_X500);

    // Thread-local digests (MessageDigest is NOT thread-safe)
    private static final ThreadLocal<MessageDigest> SHA256_TL = ThreadLocal.withInitial(() -> getDigest("SHA-256"));

    /**
     * Prevents instantiation of this utility.
     */
    private UUIDUtils() {
        super();
    }

    /**
     * Drops this thread's cached SHA-256 digest so pooled threads do not retain it
     * after the request ends.
     */
    public static void removeThreadLocals() {
        SHA256_TL.remove();
    }

    /**
     * Builds a type-5 UUID from UTF-8 bytes of {@code name} under {@code namespace}.
     *
     * @param namespace never-null RFC 4122 namespace UUID
     * @param name      never-null name string
     * @return never-null type-5 UUID
     * @throws NullPointerException when {@code namespace} or {@code name} is null
     */
    public static UUID getUUID(UUID namespace, String name) {
        return getUUIDFromBytes(namespace, Objects.requireNonNull(name, "name == null").getBytes(UTF8));
    }

    /**
     * Builds a type-5 UUID from {@code name} bytes under {@code namespace}.
     *
     * @param namespace never-null RFC 4122 namespace UUID
     * @param name      never-null name bytes
     * @return never-null type-5 UUID
     * @throws NullPointerException when {@code namespace} or {@code name} is null
     */
    public static UUID getUUIDFromBytes(UUID namespace, byte[] name) {
        byte[] nsBytes = fastNamespaceBytes(Objects.requireNonNull(namespace, "namespace is null"));
        if (nsBytes == null) {
            nsBytes = toBytes(Objects.requireNonNull(namespace, "namespace is null")); // fallback for custom namespace
        }
        MessageDigest digest = SHA256_TL.get();
        digest.reset();
        digest.update(nsBytes);
        digest.update(Objects.requireNonNull(name, "name is null"));
        
        byte[] sha1Bytes = digest.digest(); // 32 bytes
        sha1Bytes[6] &= 0x0f; /* clear version */
        sha1Bytes[6] |= 0x50; /* set to version 5 */
        sha1Bytes[8] &= 0x3f; /* clear variant */
        sha1Bytes[8] |= 0x80; /* set to IETF variant */
        return fromBytes(sha1Bytes);
    }

    /**
     * Converts the first 16 digest bytes into a {@link UUID}.
     *
     * @param data never-null digest at least 16 bytes long
     * @return never-null UUID
     */
    private static UUID fromBytes(byte[] data) {
        // Based on the private UUID(bytes[]) constructor
        long msb = 0;
        long lsb = 0;
        assert data.length >= 16;
        for (int i = 0; i < 8; i++)
            msb = (msb << 8) | (data[i] & 0xff);
        for (int i = 8; i < 16; i++)
            lsb = (lsb << 8) | (data[i] & 0xff);
        return new UUID(msb, lsb);
    }

    /**
     * Encodes a UUID as 16 big-endian bytes.
     *
     * @param uuid never-null UUID
     * @return never-null 16-byte array
     */
    private static byte[] toBytes(UUID uuid) {
        // inverted logic of fromBytes()
        byte[] out = new byte[16];
        long msb = uuid.getMostSignificantBits();
        long lsb = uuid.getLeastSignificantBits();
        for (int i = 0; i < 8; i++)
            out[i] = (byte) ((msb >> ((7 - i) * 8)) & 0xff);
        for (int i = 8; i < 16; i++)
            out[i] = (byte) ((lsb >> ((15 - i) * 8)) & 0xff);
        return out;
    }

    /**
     * Returns precomputed bytes for RFC 4122 namespace constants.
     *
     * @param ns namespace UUID; may be a custom value
     * @return cached bytes for well-known namespaces, otherwise null
     */
    private static byte[] fastNamespaceBytes(UUID ns) {
        // Identity compares are fine; constants are interned singletons
        if (ns == NAMESPACE_DNS)  return NS_DNS_BYTES;
        if (ns == NAMESPACE_URL)  return NS_URL_BYTES;
        if (ns == NAMESPACE_OID)  return NS_OID_BYTES;
        if (ns == NAMESPACE_X500) return NS_X500_BYTES;
        return null;
    }

    /**
     * Returns a {@link MessageDigest} for {@code algo}.
     *
     * @param algo never-null JCA algorithm name
     * @return never-null digest instance
     * @throws io.mosip.kernel.core.exception.NoSuchAlgorithmException when the algorithm is unavailable
     */
    private static MessageDigest getDigest(String algo) {
        try {
            return MessageDigest.getInstance(algo);
        } catch (NoSuchAlgorithmException exception) {
            throw new io.mosip.kernel.core.exception.NoSuchAlgorithmException(HMACUtilConstants.MOSIP_NO_SUCH_ALGORITHM_ERROR_CODE.getErrorCode(),
                    HMACUtilConstants.MOSIP_NO_SUCH_ALGORITHM_ERROR_CODE.getErrorMessage(), exception.getCause());
        }
    }
}