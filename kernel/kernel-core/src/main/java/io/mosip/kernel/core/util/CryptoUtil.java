package io.mosip.kernel.core.util;

import static java.util.Arrays.copyOfRange;

import java.util.Base64;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.ArrayUtils;

/**
 * Crypto Util for common methods in various module
 * 
 * @author Urvil Joshi
 *
 * @since 1.0.0
 */
public class CryptoUtil {

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
		return Base64.getUrlEncoder().encodeToString(data);
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
	/* This impl was a upgrade from apache coded to java 8 as apache has a single decoder  
	 * for decoding both url safe and standard base64 encoding but java 8 has two decoders 
	 * we are follwing this approach.
	 */
	@Deprecated(since = "1.1.5", forRemoval = true)
	public static byte[] decodeBase64(String data) {
		try {
			return Base64.getUrlDecoder().decode(data.getBytes());
		} catch (IllegalArgumentException exception) {
			return Base64.getDecoder().decode(data.getBytes());
		}
	}

	public static String base64EncodeURLSafe(byte[] data) {
		return Base64.getUrlEncoder().encodeToString(data);
	}

	public static byte[] base64DecodeURLSafe(String data) {
		return Base64.getUrlDecoder().decode(data.getBytes());
	}

	public static String base64Encode(byte[] data) {
		return Base64.getEncoder().encodeToString(data);
	}

	public static byte[] base64Decode(String data) {
		return Base64.getDecoder().decode(data.getBytes());
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

}