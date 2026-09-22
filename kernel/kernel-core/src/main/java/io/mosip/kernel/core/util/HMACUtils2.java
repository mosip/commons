/**
 *
 */
package io.mosip.kernel.core.util;

import io.mosip.kernel.core.util.constant.HMACUtilConstants;

import java.nio.ByteOrder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Thread-safe SHA-256 digest, salt, and Base64 helpers.
 * <p>
 * Contract: uses thread-local {@link MessageDigest} instances. PBKDF2 iteration
 * count is {@code 27500} unless env {@code hashiteration} is a larger integer.
 * Does not perform MOSIP HTTP.
 * </p>
 *
 * @author Sasikumar Ganesan
 * @since 1.1.4
 */
public final class HMACUtils2 {
    /**
     * SHA-256 Algorithm
     */
    private static final String HASH_ALGORITHM_NAME = "SHA-256";

    // lookup array for converting byte to hex
    private static final char[] LOOKUP_TABLE_LOWER = new char[] { 0x30, 0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37, 0x38,
            0x39, 0x61, 0x62, 0x63, 0x64, 0x65, 0x66 };
    private static final char[] LOOKUP_TABLE_UPPER = new char[] { 0x30, 0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37, 0x38,
            0x39, 0x41, 0x42, 0x43, 0x44, 0x45, 0x46 };

    // Thread-local digests (MessageDigest is NOT thread-safe)
    private static final ThreadLocal<MessageDigest> MESSAGE_DIGEST_SHA256_TL = ThreadLocal.withInitial(() -> getDigest(HASH_ALGORITHM_NAME));
    private static final ThreadLocal<SecureRandom> SECURE_RANDOM_TL =
            ThreadLocal.withInitial(SecureRandom::new);

    private static final Base64.Encoder BASE64_ENCODER = Base64.getEncoder();
    private static final Base64.Decoder BASE64_DECODER = Base64.getDecoder();

    private static final ThreadLocal<SecretKeyFactory> PBKDF2_WITH_HMAC_SHA256_FACTORY_TL =
            ThreadLocal.withInitial(() -> {
                try {
                    return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
                } catch (io.mosip.kernel.core.exception.NoSuchAlgorithmException | java.security.NoSuchAlgorithmException e) {
                    throw new RuntimeException("PBKDF2 algorithm not found", e);
                }
            });

    private static final int DEFAULT_ITERATION_COUNT = 27500;
    private static final int ITERATION_COUNT;

    static {
        int envCount = DEFAULT_ITERATION_COUNT;
        String envValue = System.getenv("hashiteration");
        if (envValue != null) {
            try {
                int parsed = Integer.parseInt(envValue);
                if (parsed > DEFAULT_ITERATION_COUNT) {
                    envCount = parsed;
                }
            } catch (NumberFormatException ignored) {
                // keep default if invalid
            }
        }
        ITERATION_COUNT = envCount;
    }

    /**
     * Prevents instantiation of this utility.
     */
    private HMACUtils2() {
    }

    /**
     * Drops this thread's cached digest, PRNG, and PBKDF2 factory so pooled threads
     * do not retain them after the request ends.
     */
    public static void removeThreadLocals() {
        MESSAGE_DIGEST_SHA256_TL.remove();
        SECURE_RANDOM_TL.remove();
        PBKDF2_WITH_HMAC_SHA256_FACTORY_TL.remove();
    }

    /**
     * SHA-256 digests {@code bytes}.
     *
     * @param bytes never-null input
     * @return never-null 32-byte digest
     * @throws NoSuchAlgorithmException unused; digest is created at class init
     */
    public static byte[] generateHash(final byte[] bytes) throws NoSuchAlgorithmException {
        return MESSAGE_DIGEST_SHA256_TL.get().digest(bytes);
    }

    /**
     * SHA-256 digests {@code pwd} then {@code salt} and returns uppercase hex.
     *
     * @param pwd  never-null password bytes
     * @param salt never-null salt bytes
     * @return never-null uppercase hex digest
     * @throws NoSuchAlgorithmException unused; digest is created at class init
     */
    public static String digestAsPlainTextWithSalt(final byte[] pwd, final byte[] salt)
            throws NoSuchAlgorithmException {
        MessageDigest digest = MESSAGE_DIGEST_SHA256_TL.get();
        digest.reset();
        digest.update(pwd);
        digest.update(salt);
        return encodeBytesToHex(digest.digest(), true, ByteOrder.BIG_ENDIAN);
    }

    /**
     * SHA-256 digests {@code bytes} and returns uppercase hex.
     *
     * @param bytes never-null input
     * @return never-null uppercase hex digest
     * @throws NoSuchAlgorithmException unused; digest is created at class init
     */
    public static String digestAsPlainText(final byte[] bytes) throws NoSuchAlgorithmException {
        return encodeBytesToHex(generateHash(bytes), true, ByteOrder.BIG_ENDIAN);
    }

    /**
     * Returns 16 cryptographically random salt bytes.
     *
     * @return never-null 16-byte salt
     */
    public static byte[] generateSalt() {
        return generateSalt(16);
    }

    /**
     * Returns {@code bytes} cryptographically random salt bytes.
     *
     * @param bytes salt length; must be positive
     * @return never-null salt of length {@code bytes}
     */
    public static byte[] generateSalt(int bytes) {
        byte[] randomBytes = new byte[bytes];
        SECURE_RANDOM_TL.get().nextBytes(randomBytes);
        return randomBytes;
    }

    /**
     * Encodes {@code data} as standard Base64.
     *
     * @param data never-null bytes to encode
     * @return never-null Base64 string
     */
    public static String encodeBase64String(byte[] data) {
        return BASE64_ENCODER.encodeToString(data);
    }

    /**
     * Decodes standard Base64 text.
     *
     * @param data never-null Base64 text
     * @return never-null decoded bytes
     */
    public static byte[] decodeBase64(String data) {
        return BASE64_DECODER.decode(data);
    }


    /**
     * PBKDF2-encodes {@code password} with Base64 {@code salt}.
     *
     * @param password never-null password
     * @param salt     never-null Base64-encoded salt
     * @return never-null Base64-encoded derived key
     */
    private static String encode(String password, byte[] salt) {
        KeySpec spec = new PBEKeySpec(password.toCharArray(), Base64.getDecoder().decode(salt), ITERATION_COUNT, 512);
        try {
            byte[] key = PBKDF2_WITH_HMAC_SHA256_FACTORY_TL.get().generateSecret(spec).getEncoded();
            return encodeBase64String(key);
        } catch (InvalidKeySpecException e) {
            throw new RuntimeException("Credential could not be encoded", e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Encodes {@code byteArray} as hex using {@code byteOrder}.
     *
     * @param byteArray never-null bytes
     * @param upperCase {@code true} for A-F, {@code false} for a-f
     * @param byteOrder never-null {@link ByteOrder#BIG_ENDIAN} or {@link ByteOrder#LITTLE_ENDIAN}
     * @return never-null hex string of length {@code byteArray.length * 2}
     */
    public static String encodeBytesToHex(byte[] byteArray, boolean upperCase, ByteOrder byteOrder) {
        final int len = byteArray.length;

        // our output size will be exactly 2x byte-array length
        final char[] buffer = new char[len * 2];

        // choose lower or uppercase lookup table
        final char[] lookup = upperCase ? LOOKUP_TABLE_UPPER : LOOKUP_TABLE_LOWER;

        int index;
        for (int i = 0; i < len; i++) {
            // for little endian we count from last to first
            index = (byteOrder == ByteOrder.BIG_ENDIAN) ? i : len - i - 1;

            // extract the upper 4 bit and look up char (0-A)
            buffer[i << 1] = lookup[(byteArray[index] >> 4) & 0xF];
            // extract the lower 4 bit and look up char (0-A)
            buffer[(i << 1) + 1] = lookup[(byteArray[index] & 0xF)];
        }
        return new String(buffer);
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