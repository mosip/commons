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
 * This class defines the HMAC Util to be used in MOSIP Project. The HMAC Util
 * is implemented using desired methods of MessageDigest class of java security
 * package
 * @deprecated This class is not thread safe and could result in creating wrong digest. Please move the HMACUtils2 class for thread safe implementation.
 *
 * @author Omsaieswar Mulaklauri
 * @author Urvil Joshi
 *
 * @since 1.0.0
 */
@Deprecated
public final class HMACUtils {
    /**
     * SHA-256 Algorithm
     */
    private static final String HMAC_ALGORITHM_NAME = "SHA-256";

    /**
     * Message digests are secure one-way hash functions that take arbitrary-sized
     * data and output a fixed-length hash value
     */
    private static final ThreadLocal<MessageDigest> SHA256_TL = ThreadLocal.withInitial(() -> getDigest(HMAC_ALGORITHM_NAME));
    private static final ThreadLocal<SecureRandom> SECURE_RANDOM =
            ThreadLocal.withInitial(SecureRandom::new);

    private static final java.util.Base64.Encoder BASE64_ENCODER = java.util.Base64.getEncoder();
    private static final java.util.Base64.Decoder BASE64_DECODER = java.util.Base64.getDecoder();

    private static final ThreadLocal<SecretKeyFactory> PBKDF2_FACTORY_TL =
            ThreadLocal.withInitial(() -> {
                try {
                    return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
                } catch (NoSuchAlgorithmException | java.security.NoSuchAlgorithmException e) {
                    throw new RuntimeException("PBKDF2 algorithm not found", e);
                }
            });

    /*
     * No object initialization.
     */
    private HMACUtils() {
    }

    /**
     * Performs a digest using the specified array of bytes.
     *
     * @param bytes bytes to be hash generation
     * @return byte[] generated hash bytes
     */
    public static synchronized byte[] generateHash(final byte[] bytes) {
        return SHA256_TL.get().digest(bytes);
    }

    /**
     * Updates the digest using the specified byte
     *
     * @param bytes updates the digest using the specified byte
     */
    public static void update(final byte[] bytes) {
        SHA256_TL.get().update(bytes);
    }

    /**
     * Return the whole update digest
     *
     * @return byte[] updated hash bytes
     */
    public static byte[] updatedHash() {
        return SHA256_TL.get().digest();
    }

    /**
     * Return the digest as a plain text with Salt
     *
     * @param password digest bytes
     * @param salt  digest bytes
     * @return String converted digest as plain text
     */
    public static synchronized String digestAsPlainTextWithSalt(final byte[] password, final byte[] salt) {
        SHA256_TL.get().update(password);
        SHA256_TL.get().update(salt);
        return DatatypeConverter.printHexBinary(SHA256_TL.get().digest());
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
     * Return the digest as a plain text
     *
     * @param bytes digest bytes
     * @return String converted digest as plain text
     */
    public static synchronized String digestAsPlainText(final byte[] bytes) {
        return DatatypeConverter.printHexBinary(bytes).toUpperCase();
    }

    /**
     * Generate Random Salt (with default 16 bytes of length).
     *
     * @return Random Salt
     */
    public static byte[] generateSalt() {
        byte[] randomBytes = new byte[16];
        SECURE_RANDOM.get().nextBytes(randomBytes);
        return randomBytes;
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
        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 27500, 512);
        try {
            byte[] key = PBKDF2_FACTORY_TL.get().generateSecret(spec).getEncoded();
            return Base64.encodeBase64String(key);
        } catch (InvalidKeySpecException e) {
            throw new RuntimeException("Credential could not be encoded", e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static MessageDigest getDigest(String algo) {
        try {
            return MessageDigest.getInstance(algo);
        } catch (java.security.NoSuchAlgorithmException e) {
            // Should not happen for standard algorithms
            throw new IllegalStateException(algo + " not supported", e);
        }
    }
}