/**
 * 
 */
package io.mosip.kernel.core.util;

import static org.mockito.ArgumentMatchers.nullable;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.xml.bind.DatatypeConverter;

import org.springframework.boot.autoconfigure.couchbase.CouchbaseProperties.Env;

import java.util.Base64;

import java.security.NoSuchAlgorithmException;

/**
 * This class defines the alternate safer HMAC Util to be used in MOSIP Project. The HMAC Util
 * is implemented using desired methods of MessageDigest class of java security
 * package
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
	

	/**
	 * Performs a digest using the specified array of bytes.
	 * 
	 * @param bytes bytes to be hash generation
	 * @return byte[] generated hash bytes
	 */
	public static byte[] generateHash(final byte[] bytes) throws NoSuchAlgorithmException{
		MessageDigest messageDigest = MessageDigest.getInstance(HASH_ALGORITHM_NAME);
		return messageDigest.digest(bytes);
	}

	/**
	 * Return the digest as a plain text with Salt
	 * 
	 * @param bytes digest bytes
	 * @param salt  digest bytes
	 * @return String converted digest as plain text
	 * @throws java.security.NoSuchAlgorithmException
	 */
	public static String digestAsPlainTextWithSalt(final byte[] password, final byte[] salt)
			throws NoSuchAlgorithmException {
		MessageDigest messageDigest = MessageDigest.getInstance(HASH_ALGORITHM_NAME);
		messageDigest.update(password);
		messageDigest.update(salt);
		return DatatypeConverter.printHexBinary(messageDigest.digest());
	}

	/**
	 * Return the digest as a plain text
	 * 
	 * @param bytes digest bytes
	 * @return String converted digest as plain text
	 * @throws NoSuchAlgorithmException
	 */
	public static String digestAsPlainText(final byte[] bytes) throws NoSuchAlgorithmException {
		return DatatypeConverter.printHexBinary(generateHash(bytes)).toUpperCase();
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
		SecureRandom random = new SecureRandom();
		byte[] randomBytes = new byte[bytes];
		random.nextBytes(randomBytes);
		return randomBytes;
	}

	/**
	 * Encodes to BASE64 String
	 * 
	 * @param data data to encode
	 * @return encoded data
	 */
	public static String encodeBase64String(byte[] data) {
		return Base64.getEncoder().encodeToString(data);
	}

	/**
	 * Decodes from BASE64
	 * 
	 * @param data data to decode
	 * @return decoded data
	 */
	public static byte[] decodeBase64(String data) {
		return Base64.getDecoder().decode(data);
	}

	/*
	 * No object initialization.
	 */
	private HMACUtils2() {
	}

	private static String encode(String password, byte[] salt) {
		int iterationCount= 27500; //default it has to be higher than this if you want to override
		if ( System.getenv("hashiteration") != null){
			String envCount = System.getenv("hashiteration");
			if(Integer.parseInt(envCount) > iterationCount) {
				iterationCount = Integer.parseInt(envCount);
			}
		}
		KeySpec spec = new PBEKeySpec(password.toCharArray(), Base64.getDecoder().decode(salt), iterationCount, 512);

		try {
			byte[] key = getSecretKeyFactory().generateSecret(spec).getEncoded();
			return Base64.getEncoder().encodeToString(key);
		} catch (InvalidKeySpecException e) {
			throw new RuntimeException("Credential could not be encoded", e);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	private static SecretKeyFactory getSecretKeyFactory() throws java.security.NoSuchAlgorithmException {
		try {
			return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("PBKDF2 algorithm not found", e);
		}
	}
}
