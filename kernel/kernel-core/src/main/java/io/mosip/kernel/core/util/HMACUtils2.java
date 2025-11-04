/**
 *
 */
package io.mosip.kernel.core.util;

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
 * This class defines the alternate safer HMAC Util to be used in MOSIP Project.
 * The HMAC Util is implemented using desired methods of MessageDigest class of
 * java security package
 *
 * @author Sasikumar Ganesan
 *
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
    private static final ThreadLocal<MessageDigest> SHA256_TL = ThreadLocal.withInitial(() -> getDigest(HASH_ALGORITHM_NAME));
    private static final ThreadLocal<SecureRandom> SECURE_RANDOM =
            ThreadLocal.withInitial(SecureRandom::new);

    private static final Base64.Encoder BASE64_ENCODER = Base64.getEncoder();
    private static final Base64.Decoder BASE64_DECODER = Base64.getDecoder();

    private static final ThreadLocal<SecretKeyFactory> PBKDF2_FACTORY =
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

    /*
     * No object initialization.
     */
    private HMACUtils2() {
    }

    /**
     * Performs a digest using the specified array of bytes.
     *
     * @param bytes bytes to be hash generation
     * @return byte[] generated hash bytes
     */
    public static byte[] generateHash(final byte[] bytes) throws NoSuchAlgorithmException {
        final MessageDigest messageDigest = SHA256_TL.get();
        return messageDigest.digest(bytes);
    }

    /**
     * Return the digest as a plain text with Salt
     *
     * @param pwd digest bytes
     * @param salt  digest bytes
     * @return String converted digest as plain text
     * @throws java.security.NoSuchAlgorithmException
     */
    public static String digestAsPlainTextWithSalt(final byte[] pwd, final byte[] salt)
            throws NoSuchAlgorithmException {
        final MessageDigest messageDigest = SHA256_TL.get();
        messageDigest.update(pwd);
        messageDigest.update(salt);
        return encodeBytesToHex(messageDigest.digest(), true, ByteOrder.BIG_ENDIAN);
    }

    /**
     * Return the digest as a plain text
     *
     * @param bytes digest bytes
     * @return String converted digest as plain text
     * @throws NoSuchAlgorithmException
     */
    public static String digestAsPlainText(final byte[] bytes) throws NoSuchAlgorithmException {
        return encodeBytesToHex(generateHash(bytes), true, ByteOrder.BIG_ENDIAN);
    }

    /**
     * Generate Random Salt (with default 16 bytes of length).
     *
     * @return Random Salt
     */
    public static byte[] generateSalt() {
        return generateSalt(16);
    }

    /**
     * Generate Random Salt (with given length)
     *
     * @param bytes length of random salt
     * @return Random Salt of given length
     */
    public static byte[] generateSalt(int bytes) {
        byte[] randomBytes = new byte[bytes];
        SECURE_RANDOM.get().nextBytes(randomBytes);
        return randomBytes;
    }

    /**
     * Encodes to BASE64 String
     *
     * @param data data to encode
     * @return encoded data
     */
    public static String encodeBase64String(byte[] data) {
        return BASE64_ENCODER.encodeToString(data);
    }

    /**
     * Decodes from BASE64
     *
     * @param data data to decode
     * @return decoded data
     */
    public static byte[] decodeBase64(String data) {
        return BASE64_DECODER.decode(data);
    }


    private static String encode(String password, byte[] salt) {
        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATION_COUNT, 512);
        try {
            byte[] key = PBKDF2_FACTORY.get().generateSecret(spec).getEncoded();
            return encodeBase64String(key);
        } catch (InvalidKeySpecException e) {
            throw new RuntimeException("Credential could not be encoded", e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

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

    private static MessageDigest getDigest(String algo) {
        try {
            return MessageDigest.getInstance(algo);
        } catch (NoSuchAlgorithmException e) {
            // Should not happen for standard algorithms
            throw new IllegalStateException(algo + " not supported", e);
        }
    }
}