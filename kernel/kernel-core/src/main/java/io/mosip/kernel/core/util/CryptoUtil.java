package io.mosip.kernel.core.util;

import static java.util.Arrays.copyOfRange;

import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Objects;
import java.util.Base64.Encoder;
import java.util.concurrent.ConcurrentHashMap;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.ArrayUtils;

import io.mosip.kernel.core.crypto.constant.CryptoExceptionCodeConstants;
import io.mosip.kernel.core.crypto.exception.InvalidKeyException;
import io.mosip.kernel.core.crypto.exception.NoSuchAlgorithmException;
import io.mosip.kernel.core.crypto.exception.NullDataException;

/**
 * Base64, fingerprint, and AES-GCM helpers shared across MOSIP crypto modules.
 * <p>
 * Contract: static helpers only; this class is not instantiable. Empty inputs
 * to encode/decode methods return null. AES-GCM uses a random IV appended to
 * ciphertext. Does not perform MOSIP HTTP.
 * </p>
 *
 * @author Urvil Joshi
 * @since 1.0.0
 */
public class CryptoUtil {

    private static final String SYMMETRIC_ALGORITHM = "AES/GCM/NoPadding";

    private static final String AES = "AES";

    private static final int TAG_LENGTH = 128;

    private static final Base64.Encoder URL_SAFE_ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Encoder STD_ENCODER = Base64.getEncoder();
    private static final Base64.Decoder URL_SAFE_DECODER = Base64.getUrlDecoder();
    private static final Base64.Decoder STD_DECODER = Base64.getDecoder();

    // ThreadLocal SecureRandom to avoid contention
    private static final ThreadLocal<SecureRandom> SECURE_RANDOM_TL =
            ThreadLocal.withInitial(() -> {
                SecureRandom sr = new SecureRandom();
                sr.nextBytes(new byte[1]); // warmup
                return sr;
            });

    // ThreadLocal Cipher instance cache
    private static final ThreadLocal<Cipher> AES_GCM_CIPHER_TL =
            ThreadLocal.withInitial(() -> {
                try {
                    return Cipher.getInstance(SYMMETRIC_ALGORITHM);
                } catch (Exception e) {
                    throw new NoSuchAlgorithmException(
                            CryptoExceptionCodeConstants.NO_SUCH_ALGORITHM_EXCEPTION.getErrorCode(), CryptoExceptionCodeConstants.NO_SUCH_ALGORITHM_EXCEPTION.getErrorMessage(), e);
                }
            });

    /**
     * Private Constructor for this class
     */
    private CryptoUtil() {

    }

    /**
     * Drops this thread's cached {@link SecureRandom} and AES-GCM {@link Cipher}
     * so pooled threads do not retain them after the request ends.
     */
    public static void removeThreadLocals() {
        SECURE_RANDOM_TL.remove();
        AES_GCM_CIPHER_TL.remove();
    }

    /**
     * Concatenates encrypted key, UTF-8 {@code keySplitter}, and encrypted data.
     *
     * @param data        never-null ciphertext
     * @param key         never-null encrypted session key
     * @param keySplitter never-null delimiter string
     * @return never-null concatenated bytes
     */
    public static byte[] combineByteArray(byte[] data, byte[] key, String keySplitter) {
        byte[] keySplitterBytes = keySplitter.getBytes(StandardCharsets.UTF_8);
        byte[] combinedArray = new byte[key.length + keySplitterBytes.length + data.length];
        System.arraycopy(key, 0, combinedArray, 0, key.length);
        System.arraycopy(keySplitterBytes, 0, combinedArray, key.length, keySplitterBytes.length);
        System.arraycopy(data, 0, combinedArray, key.length + keySplitterBytes.length, data.length);
        return combinedArray;
    }

    /**
     * Finds the index of {@code keySplitter} inside {@code encryptedData}.
     *
     * @param encryptedData     never-null combined key+splitter+data bytes
     * @param keyDelimiterIndex search start index, typically 0
     * @param keySplitter       never-null delimiter to locate
     * @return index of the splitter, or {@code keyDelimiterIndex} if not found (backward compatible)
     */
    public static int getSplitterIndex(byte[] encryptedData, int keyDelimiterIndex, String keySplitter) {
        byte[] splitterBytes = keySplitter.getBytes(StandardCharsets.UTF_8);
        byte firstByte = splitterBytes[0];
        int splitLen = splitterBytes.length;

        for (int i = keyDelimiterIndex; i <= encryptedData.length - splitLen; i++) {
            if (encryptedData[i] == firstByte) {
                boolean match = true;
                // Compare bytes directly instead of creating new arrays/strings
                for (int j = 0; j < splitLen; j++) {
                    if (encryptedData[i + j] != splitterBytes[j]) {
                        match = false;
                        break;
                    }
                }
                if (match) {
                    return i;
                }
            }
        }
        /*
        it's a wrong way it should always return -1 but to follow backward compatibility returning keyDelimiterIndex
         */
        return keyDelimiterIndex;
    }

    /**
     * Encodes {@code data} as URL-safe Base64 without padding.
     *
     * @param data never-null bytes to encode
     * @return never-null URL-safe Base64 string
     * @deprecated since 1.1.5; use {@link #encodeToURLSafeBase64(byte[])}
     */
    @Deprecated(since = "1.1.5", forRemoval = true)
    public static String encodeBase64(byte[] data) {
        return URL_SAFE_ENCODER.encodeToString(data);
    }

    /**
     * Encodes {@code data} as standard Base64.
     *
     * @param data never-null bytes to encode
     * @return never-null Base64 string
     * @deprecated since 1.1.5; use {@link #encodeToPlainBase64(byte[])}
     */
    @Deprecated(since = "1.1.5", forRemoval = true)
    public static String encodeBase64String(byte[] data) {
        return STD_ENCODER.encodeToString(data);
    }

    /**
     * Decodes URL-safe or standard Base64 text.
     *
     * @param data Base64 text; null or empty yields null
     * @return decoded bytes, or null if {@code data} is null or empty
     * @deprecated since 1.1.5; use {@link #decodeURLSafeBase64(String)} or {@link #decodePlainBase64(String)}
     */
    @Deprecated(since = "1.1.5", forRemoval = true)
    public static byte[] decodeBase64(String data) {
        if (EmptyCheckUtils.isNullEmpty(data)) return null;
        try {
            return URL_SAFE_DECODER.decode(data);
        } catch (IllegalArgumentException exception) {
            return STD_DECODER.decode(data);
        }
    }

    /**
     * Encodes {@code data} as URL-safe Base64 without padding.
     *
     * @param data bytes to encode; null or empty yields null
     * @return URL-safe Base64 string, or null if {@code data} is null or empty
     */
    public static String encodeToURLSafeBase64(byte[] data) {
        if (EmptyCheckUtils.isNullEmpty(data)) return null;
        return URL_SAFE_ENCODER.encodeToString(data);
    }

    /**
     * Decodes URL-safe Base64 text.
     *
     * @param data Base64 text; null or empty yields null
     * @return decoded bytes, or null if {@code data} is null or empty
     */
    public static byte[] decodeURLSafeBase64(String data) {
        if (EmptyCheckUtils.isNullEmpty(data)) return null;
        return URL_SAFE_DECODER.decode(data);
    }

    /**
     * Encodes {@code data} as standard Base64.
     *
     * @param data bytes to encode; null or empty yields null
     * @return Base64 string, or null if {@code data} is null or empty
     */
    public static String encodeToPlainBase64(byte[] data) {
        if (EmptyCheckUtils.isNullEmpty(data)) return null;
        return STD_ENCODER.encodeToString(data);
    }

    /**
     * Decodes standard Base64 text.
     *
     * @param data Base64 text; null or empty yields null
     * @return decoded bytes, or null if {@code data} is null or empty
     */
    public static byte[] decodePlainBase64(String data) {
        if (EmptyCheckUtils.isNullEmpty(data)) return null;
        return STD_DECODER.decode(data);
    }

    /**
     * Computes a colon-separated hex fingerprint of UTF-8 {@code data} plus optional metadata.
     *
     * @param data     never-null key material as text
     * @param metaData optional metadata concatenated before hashing; may be null or empty
     * @return never-null colon-separated hex digest
     */
    public static String computeFingerPrint(String data, String metaData) {
        return computeFingerPrint(data.getBytes(), metaData);
    }

    /**
     * Computes a colon-separated hex fingerprint of {@code data} plus optional metadata.
     *
     * @param data     never-null key material
     * @param metaData optional metadata concatenated before hashing; may be null or empty
     * @return never-null colon-separated hex digest
     */
    public static String computeFingerPrint(byte[] data, String metaData) {
        byte[] combined = EmptyCheckUtils.isNullEmpty(metaData) ? ArrayUtils.addAll(data) :
                ArrayUtils.addAll(data, metaData.getBytes(StandardCharsets.UTF_8));

        return Hex.encodeHexString(HMACUtils.generateHash(combined)).replaceAll("..(?!$)", "$0:");
    }

    /**
     * AES-GCM encrypts {@code data} with {@code key}; the random IV is appended to the ciphertext.
     *
     * @param key  never-null AES secret key
     * @param data never-null, never-empty plaintext
     * @return never-null ciphertext with IV suffix
     * @throws io.mosip.kernel.core.crypto.exception.NullDataException when {@code data} is null or empty
     * @throws io.mosip.kernel.core.crypto.exception.InvalidKeyException when encryption fails
     */
    public static byte[] symmetricEncrypt(SecretKey key, byte[] data) {
        Objects.requireNonNull(key, CryptoExceptionCodeConstants.INVALID_KEY_EXCEPTION.getErrorMessage());
        if (Objects.isNull(data) || data.length == 0) {
            throw new NullDataException(CryptoExceptionCodeConstants.INVALID_DATA_EXCEPTION.getErrorCode(), CryptoExceptionCodeConstants.INVALID_DATA_EXCEPTION.getErrorMessage());
        }

        try {
            Cipher cipher = AES_GCM_CIPHER_TL.get();
            byte[] randomIV = generateIV(cipher.getBlockSize());
            SecretKeySpec keySpec = new SecretKeySpec(key.getEncoded(), AES);
            GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(TAG_LENGTH, randomIV);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, gcmParameterSpec);

            byte[] output = new byte[cipher.getOutputSize(data.length) + cipher.getBlockSize()];
            byte[] processData = cipher.doFinal(data);
            System.arraycopy(processData, 0, output, 0, processData.length);
            System.arraycopy(randomIV, 0, output, processData.length, randomIV.length);
            return output;
        } catch (java.security.InvalidKeyException | InvalidAlgorithmParameterException | IllegalBlockSizeException |
                 BadPaddingException e) {
            throw new InvalidKeyException(CryptoExceptionCodeConstants.INVALID_KEY_EXCEPTION.getErrorCode(), CryptoExceptionCodeConstants.INVALID_KEY_EXCEPTION.getErrorMessage(), e);
        }
    }

    /**
     * Fills a random IV of {@code blockSize} bytes.
     *
     * @param blockSize IV length in bytes
     * @return never-null IV
     */
    private static byte[] generateIV(int blockSize) {
        byte[] byteIV = new byte[blockSize];
        SECURE_RANDOM_TL.get().nextBytes(byteIV);
        return byteIV;
    }
}