/**
 *
 */
package io.mosip.kernel.core.util;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.xml.bind.DatatypeConverter;

import org.apache.commons.codec.binary.Base64;

import io.mosip.kernel.core.exception.NoSuchAlgorithmException;
import io.mosip.kernel.core.util.constant.HMACUtilConstants;

/**
 * SHA-256 digest and Base64 helpers used across MOSIP modules.
 * <p>
 * Contract: deprecated since 1.3.0; new code must use {@link HMACUtils2}.
 * {@link #generateHash(byte[])} and {@link #update(byte[])} share a static
 * {@link MessageDigest} and are not thread-safe unless callers synchronize.
 * Does not perform I/O.
 * </p>
 *
 * @deprecated This class is deprecated and will be removed in future releases.
 *             Please use {@link io.mosip.kernel.core.util.HMACUtils2} instead.
 * @author Omsaieswar Mulaklauri
 * @author Urvil Joshi
 * @since 1.0.0
 */
@Deprecated(since = "1.3.0", forRemoval = true)
public final class HMACUtils {
    /**
     * SHA-256 Algorithm
     */
    private static final String HMAC_ALGORITHM_NAME = "SHA-256";

    /** Reused PRNG; constructing {@link SecureRandom} per call is expensive and weakly seeded. */
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    /**
     * Message digests are secure one-way hash functions that take arbitrary-sized
     * data and output a fixed-length hash value
     */
    private static MessageDigest messageDigest;

    /**
     * Digests {@code bytes} with the shared SHA-256 {@link MessageDigest}.
     *
     * @param bytes never-null input
     * @return never-null 32-byte digest
     */
    public static synchronized byte[] generateHash(final byte[] bytes) {
        return messageDigest.digest(bytes);
    }

    /**
     * Feeds {@code bytes} into the shared digest without finishing it.
     *
     * @param bytes never-null chunk to append
     */
    public static void update(final byte[] bytes) {
        messageDigest.update(bytes);
    }

    /**
     * Finishes the shared digest and returns the accumulated hash.
     *
     * @return never-null 32-byte digest
     */
    public static byte[] updatedHash() {
        return messageDigest.digest();
    }

    /**
     * SHA-256 digests {@code password} then {@code salt} and returns uppercase hex.
     *
     * @param password never-null password bytes
     * @param salt     never-null salt bytes
     * @return never-null uppercase hex digest
     */
    public static synchronized String digestAsPlainTextWithSalt(final byte[] password, final byte[] salt) {
        messageDigest.update(password);
        messageDigest.update(salt);
        return DatatypeConverter.printHexBinary(messageDigest.digest());
//		KeySpec spec = null;
//        try {
//        	spec = new PBEKeySpec(new String(password,"UTF-8").toCharArray(), salt, 27500, 512);
//            byte[] key = getSecretKeyFactory().generateSecret(spec).getEncoded();
//            return Base64.encodeBase64String(key);
//        } catch (InvalidKeySpecException e) {
//            throw new RuntimeException("Credential could not be encoded", e);
//        } catch (Exception e) {
//            e.printStackTrace();
//            throw new RuntimeException(e);
//        }

    }

    /**
     * Encodes {@code bytes} as uppercase hex (does not hash them).
     *
     * @param bytes never-null digest bytes
     * @return never-null uppercase hex
     */
    public static synchronized String digestAsPlainText(final byte[] bytes) {
        return DatatypeConverter.printHexBinary(bytes).toUpperCase();
    }

    /**
     * Creates a message digest with the specified algorithm name.
     *
     * @param algorithm the standard name of the digest algorithm.
     *
     * @throws NoSuchAlgorithmException if specified algorithm went wrong
     * @description loaded messageDigest with specified algorithm
     */
    static {
        try {
            messageDigest = messageDigest != null ? messageDigest : MessageDigest.getInstance(HMAC_ALGORITHM_NAME);
        } catch (java.security.NoSuchAlgorithmException exception) {
            throw new NoSuchAlgorithmException(HMACUtilConstants.MOSIP_NO_SUCH_ALGORITHM_ERROR_CODE.getErrorCode(),
                    HMACUtilConstants.MOSIP_NO_SUCH_ALGORITHM_ERROR_CODE.getErrorMessage(), exception.getCause());
        }
    }

    /**
     * Returns 16 cryptographically random salt bytes.
     *
     * @return never-null 16-byte salt
     */
    public static byte[] generateSalt() {
        byte[] randomBytes = new byte[16];
        SECURE_RANDOM.nextBytes(randomBytes);
        return randomBytes;
    }

    /**
     * Returns {@code bytes} cryptographically random salt bytes.
     *
     * @param bytes salt length; must be positive
     * @return never-null salt of length {@code bytes}
     */
    public static byte[] generateSalt(int bytes) {
        byte[] randomBytes = new byte[bytes];
        SECURE_RANDOM.nextBytes(randomBytes);
        return randomBytes;
    }

    /**
     * Encodes {@code data} as standard Base64.
     *
     * @param data never-null bytes to encode
     * @return never-null Base64 string
     */
    public static String encodeBase64String(byte[] data) {
        return Base64.encodeBase64String(data);
    }

    /**
     * Decodes standard Base64 text.
     *
     * @param data never-null Base64 text
     * @return never-null decoded bytes
     */
    public static byte[] decodeBase64(String data) {
        return Base64.decodeBase64(data);
    }

    /**
     * Prevents instantiation of this utility.
     */
    private HMACUtils() {
    }

    /**
     * PBKDF2-encodes {@code password} with Base64 {@code salt}.
     *
     * @param password never-null password
     * @param salt     never-null Base64-encoded salt
     * @return never-null Base64-encoded derived key
     */
    private static String encode(String password, byte[] salt) {
        KeySpec spec = new PBEKeySpec(password.toCharArray(), Base64.decodeBase64(salt), 27500, 512);

        try {
            byte[] key = getSecretKeyFactory().generateSecret(spec).getEncoded();
            return Base64.encodeBase64String(key);
        } catch (InvalidKeySpecException e) {
            throw new RuntimeException("Credential could not be encoded", e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns a PBKDF2WithHmacSHA256 {@link SecretKeyFactory}.
     *
     * @return never-null factory
     * @throws java.security.NoSuchAlgorithmException when the algorithm is unavailable
     */
    private static SecretKeyFactory getSecretKeyFactory() throws java.security.NoSuchAlgorithmException {
        try {
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("PBKDF2 algorithm not found", e);
        }
    }
}