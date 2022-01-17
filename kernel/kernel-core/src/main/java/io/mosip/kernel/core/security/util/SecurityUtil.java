/*
 * 
 * 
 * 
 * 
 * 
 */
package io.mosip.kernel.core.security.util;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.SecureRandom;
import java.util.Objects;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.util.PrivateKeyFactory;
import org.bouncycastle.crypto.util.PublicKeyFactory;

import io.mosip.kernel.core.security.constants.MosipSecurityExceptionCodeConstants;
import io.mosip.kernel.core.security.constants.MosipSecurityMethod;
import io.mosip.kernel.core.security.exception.MosipInvalidKeyException;
import io.mosip.kernel.core.security.exception.MosipNoSuchAlgorithmException;
import io.mosip.kernel.core.security.exception.MosipNullDataException;
import io.mosip.kernel.core.security.exception.MosipNullKeyException;
import io.mosip.kernel.core.security.exception.MosipNullMethodException;

/**
 * Utility class for security
 * 
 * @author Urvil Joshi
 * @since 1.0.0
 */
public class SecurityUtil {

	private static final String SYMMETRIC_ALGORITHM = "AES/GCM/PKCS5Padding";

	private static final String AES = "AES";

	private static final int TAG_LENGTH = 128;

	private static SecureRandom secureRandom;

	/**
	 * Constructor for this class
	 */
	private SecurityUtil() {

	}

	/**
	 * {@link AsymmetricKeyParameter} from encoded private key
	 * 
	 * @param privateKey private Key for processing
	 * @return {@link AsymmetricKeyParameter} from encoded private key
	 * @throws MosipInvalidKeyException if key is not valid (length or form)
	 */
	public static AsymmetricKeyParameter bytesToPrivateKey(byte[] privateKey) throws MosipInvalidKeyException {
		AsymmetricKeyParameter keyParameter = null;
		try {
			keyParameter = PrivateKeyFactory.createKey(privateKey);
		} catch (NullPointerException e) {
			throw new MosipNullKeyException(MosipSecurityExceptionCodeConstants.MOSIP_NULL_KEY_EXCEPTION);
		} catch (ClassCastException e) {
			throw new MosipInvalidKeyException(
					MosipSecurityExceptionCodeConstants.MOSIP_INVALID_ASYMMETRIC_PRIVATE_KEY_EXCEPTION);
		} catch (IOException e) {
			throw new MosipInvalidKeyException(MosipSecurityExceptionCodeConstants.MOSIP_INVALID_KEY_CORRUPT_EXCEPTION);
		}
		return keyParameter;
	}

	/**
	 * {@link AsymmetricKeyParameter} from encoded public key
	 * 
	 * @param publicKey private Key for processing
	 * @return {@link AsymmetricKeyParameter} from encoded public key
	 * @throws MosipInvalidKeyException if key is not valid (length or form)
	 */
	public static AsymmetricKeyParameter bytesToPublicKey(byte[] publicKey) throws MosipInvalidKeyException {
		AsymmetricKeyParameter keyParameter = null;
		try {
			keyParameter = PublicKeyFactory.createKey(publicKey);
		} catch (NullPointerException e) {
			throw new MosipNullKeyException(MosipSecurityExceptionCodeConstants.MOSIP_NULL_KEY_EXCEPTION);
		} catch (IllegalArgumentException e) {
			throw new MosipInvalidKeyException(
					MosipSecurityExceptionCodeConstants.MOSIP_INVALID_ASYMMETRIC_PUBLIC_KEY_EXCEPTION);
		} catch (IOException e) {
			throw new MosipInvalidKeyException(MosipSecurityExceptionCodeConstants.MOSIP_INVALID_KEY_CORRUPT_EXCEPTION);
		}
		return keyParameter;
	}

	/**
	 * This method verifies mosip security method
	 * 
	 * @param mosipSecurityMethod mosipSecurityMethod given by user
	 */
	public static void checkMethod(MosipSecurityMethod mosipSecurityMethod) {
		if (mosipSecurityMethod == null) {
			throw new MosipNullMethodException(MosipSecurityExceptionCodeConstants.MOSIP_NULL_METHOD_EXCEPTION);
		}
	}

	// Added below method for temporarily to fix the build issue causing cross dependency between core & keymanager service.
	public static byte[] symmetricEncrypt(SecretKey key, byte[] data) {
		Objects.requireNonNull(key, MosipSecurityExceptionCodeConstants.MOSIP_INVALID_KEY_EXCEPTION.getErrorMessage());
		if (Objects.isNull(data) || data.length == 0) {
			throw new MosipNullDataException(MosipSecurityExceptionCodeConstants.MOSIP_INVALID_DATA_EXCEPTION);
		}

		Cipher cipher;
		try {
			cipher = Cipher.getInstance(SYMMETRIC_ALGORITHM);
		} catch (java.security.NoSuchAlgorithmException | NoSuchPaddingException e) {
			throw new MosipNoSuchAlgorithmException(
				MosipSecurityExceptionCodeConstants.MOSIP_NO_SUCH_ALGORITHM_EXCEPTION);
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
		} catch (java.security.InvalidKeyException e) {
			throw new MosipInvalidKeyException(MosipSecurityExceptionCodeConstants.MOSIP_INVALID_KEY_EXCEPTION);
		} catch (InvalidAlgorithmParameterException e) {
			throw new MosipInvalidKeyException(MosipSecurityExceptionCodeConstants.MOSIP_INVALID_KEY_EXCEPTION);
		} catch (IllegalBlockSizeException e) {
			throw new MosipInvalidKeyException(MosipSecurityExceptionCodeConstants.MOSIP_INVALID_DATA_EXCEPTION);
		} catch (BadPaddingException e) {
			throw new MosipInvalidKeyException(MosipSecurityExceptionCodeConstants.MOSIP_INVALID_DATA_EXCEPTION);
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
