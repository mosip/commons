package io.mosip.kernel.core.util;

import static java.util.Arrays.copyOfRange;

import java.security.InvalidAlgorithmParameterException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Objects;
import java.util.Base64.Encoder;

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
 * Crypto Util for common methods in various module
 * 
 * @author Urvil Joshi
 *
 * @since 1.0.0
 */
public class CryptoUtil {
	
	private static final String SYMMETRIC_ALGORITHM = "AES/GCM/PKCS5Padding";

	private static final String AES = "AES";

	private static final int TAG_LENGTH = 128;

	private static SecureRandom secureRandom;
	
	private static Encoder urlSafeEncoder;
	
	
	static {
		urlSafeEncoder = Base64.getUrlEncoder().withoutPadding();
	}

	/**
	 * Private Constructor for this class
	 */
	private CryptoUtil() {

	}

	/**
	 * Combine data,key and key splitter
	 * 
	 * @param data        encrypted Data
	 * @param key         encrypted Key
	 * @param keySplitter keySplitter
	 * @return byte array consisting data,key and key splitter
	 */
	public static byte[] combineByteArray(byte[] data, byte[] key, String keySplitter) {
		byte[] keySplitterBytes = keySplitter.getBytes();
		byte[] combinedArray = new byte[key.length + keySplitterBytes.length + data.length];
		System.arraycopy(key, 0, combinedArray, 0, key.length);
		System.arraycopy(keySplitterBytes, 0, combinedArray, key.length, keySplitterBytes.length);
		System.arraycopy(data, 0, combinedArray, key.length + keySplitterBytes.length, data.length);
		return combinedArray;
	}

	/**
	 * Get splitter index for detaching key splitter from key and data
	 * 
	 * @param encryptedData     whole encrypted data
	 * @param keyDemiliterIndex keySplitterindex initialization value
	 * @param keySplitter       keysplitter value
	 * @return keyDemiliterIndex
	 */
	public static int getSplitterIndex(byte[] encryptedData, int keyDemiliterIndex, String keySplitter) {
		final byte keySplitterFirstByte = keySplitter.getBytes()[0];
		final int keySplitterLength = keySplitter.length();
		for (byte data : encryptedData) {
			if (data == keySplitterFirstByte) {
				final String keySplit = new String(
						copyOfRange(encryptedData, keyDemiliterIndex, keyDemiliterIndex + keySplitterLength));
				if (keySplitter.equals(keySplit)) {
					break;
				}
			}
			keyDemiliterIndex++;
		}
		return keyDemiliterIndex;
	}

	/**
	 * Encodes to BASE64 URL Safe
	 * 
	 * @param data data to encode
	 * @return encoded data
	 */
	@Deprecated(since = "1.1.5", forRemoval = true)
	public static String encodeBase64(byte[] data) {
		return urlSafeEncoder.encodeToString(data);
	}

	/**
	 * Encodes to BASE64 String
	 * 
	 * @param data data to encode
	 * @return encoded data
	 */
	@Deprecated(since = "1.1.5", forRemoval = true)
	public static String encodeBase64String(byte[] data) {
		return Base64.getEncoder().encodeToString(data);
	}

	/**
	 * Decodes from BASE64
	 * 
	 * @param data data to decode
	 * @return decoded data
	 */
	/*
	 * This impl was a upgrade from apache coded to java 8 as apache has a single
	 * decoder for decoding both url safe and standard base64 encoding but java 8
	 * has two decoders we are follwing this approach.
	 */
	@Deprecated(since = "1.1.5", forRemoval = true)
	public static byte[] decodeBase64(String data) {
		if (EmptyCheckUtils.isNullEmpty(data)) {
			return null;
		}
		try {
			return Base64.getUrlDecoder().decode(data);
		} catch (IllegalArgumentException exception) {
			return Base64.getDecoder().decode(data);
		}
	}

	public static String encodeToURLSafeBase64(byte[] data) {
		if (EmptyCheckUtils.isNullEmpty(data)) {
			return null;
		}
		return urlSafeEncoder.encodeToString(data);
	}

	public static byte[] decodeURLSafeBase64(String data) {
		if (EmptyCheckUtils.isNullEmpty(data)) {
			return null;
		}
		return Base64.getUrlDecoder().decode(data);
	}

	public static String encodeToPlainBase64(byte[] data) {
		if (EmptyCheckUtils.isNullEmpty(data)) {
			return null;
		}
		return Base64.getEncoder().encodeToString(data);
	}

	public static byte[] decodePlainBase64(String data) {
		if (EmptyCheckUtils.isNullEmpty(data)) {
			return null;
		}
		return Base64.getDecoder().decode(data);
	}

	/**
	 * Compute Fingerprint of a key
	 * 
	 * @param data     key data
	 * @param metaData metadata related to key
	 * @return fingerprint
	 */
	public static String computeFingerPrint(String data, String metaData) {
		return computeFingerPrint(data.getBytes(), metaData);
	}

	/**
	 * Compute Fingerprint of a key
	 * 
	 * @param data     key data
	 * @param metaData metadata related to key
	 * @return fingerprint
	 */
	public static String computeFingerPrint(byte[] data, String metaData) {
		byte[] combinedPlainTextBytes = null;
		if (EmptyCheckUtils.isNullEmpty(metaData)) {
			combinedPlainTextBytes = ArrayUtils.addAll(data);
		} else {
			combinedPlainTextBytes = ArrayUtils.addAll(data, metaData.getBytes());
		}
		return Hex.encodeHexString(HMACUtils.generateHash(combinedPlainTextBytes)).replaceAll("..(?!$)", "$0:");
	}
	
	// Added below method for temporarily to fix the build issue causing cross dependency between core & keymanager service.
		public static byte[] symmetricEncrypt(SecretKey key, byte[] data) {
			Objects.requireNonNull(key, CryptoExceptionCodeConstants.INVALID_KEY_EXCEPTION.getErrorMessage());
			if (Objects.isNull(data) || data.length == 0) {
				throw new NullDataException(CryptoExceptionCodeConstants.INVALID_DATA_EXCEPTION.getErrorCode(),CryptoExceptionCodeConstants.INVALID_DATA_EXCEPTION.getErrorMessage());
			}

			Cipher cipher;
			try {
				cipher = Cipher.getInstance(SYMMETRIC_ALGORITHM);
			} catch (java.security.NoSuchAlgorithmException | NoSuchPaddingException e) {
				throw new NoSuchAlgorithmException(
					CryptoExceptionCodeConstants.NO_SUCH_ALGORITHM_EXCEPTION.getErrorCode(),CryptoExceptionCodeConstants.NO_SUCH_ALGORITHM_EXCEPTION.getErrorMessage(),e);
			}
			try {
				byte[] output = null;
				byte[] randomIV = generateIV(cipher.getBlockSize());
				SecretKeySpec keySpec = new SecretKeySpec(key.getEncoded(), AES);
				GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(TAG_LENGTH, randomIV);
				cipher.init(Cipher.ENCRYPT_MODE, keySpec, gcmParameterSpec);
				output = new byte[cipher.getOutputSize(data.length) + cipher.getBlockSize()];
				byte[] processData = cipher.doFinal(data);
				System.arraycopy(processData, 0, output, 0, processData.length);
				System.arraycopy(randomIV, 0, output, processData.length, randomIV.length);
				return output;
			} catch (java.security.InvalidKeyException|InvalidAlgorithmParameterException|IllegalBlockSizeException|BadPaddingException e) {
				throw new InvalidKeyException(CryptoExceptionCodeConstants.INVALID_KEY_EXCEPTION.getErrorCode(),CryptoExceptionCodeConstants.INVALID_KEY_EXCEPTION.getErrorMessage(),e);
			}
		}

		private static byte[] generateIV(int blockSize) {
			byte[] byteIV = new byte[blockSize];
			
			if (Objects.isNull(secureRandom)) 
				secureRandom = new SecureRandom();

			secureRandom.nextBytes(byteIV);
			return byteIV;
		}
}