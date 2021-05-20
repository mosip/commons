package io.mosip.kernel.crypto.jce.core;

import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.MGF1ParameterSpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import javax.annotation.PostConstruct;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PSource.PSpecified;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.encodings.OAEPEncoding;
import org.bouncycastle.crypto.engines.RSAEngine;
import org.bouncycastle.crypto.params.RSAKeyParameters;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwx.CompactSerializer;
import org.jose4j.lang.JoseException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.mosip.kernel.core.crypto.exception.InvalidDataException;
import io.mosip.kernel.core.crypto.exception.InvalidKeyException;
import io.mosip.kernel.core.crypto.exception.InvalidParamSpecException;
import io.mosip.kernel.core.crypto.exception.SignatureException;
import io.mosip.kernel.core.crypto.spi.CryptoCoreSpec;
import io.mosip.kernel.core.exception.NoSuchAlgorithmException;
import io.mosip.kernel.core.util.CryptoUtil;
import io.mosip.kernel.core.util.EmptyCheckUtils;
import io.mosip.kernel.crypto.jce.constant.SecurityExceptionCodeConstant;
import io.mosip.kernel.crypto.jce.util.CryptoUtils;

/**
 * This class provided <b> Basic and Core Cryptographic functionalities </b>.
 * 
 * This class follows {@link CryptoCoreSpec} and implement all basic
 * Cryptographic functions.
 * 
 * @author Urvil Joshi
 * @author Rajath
 * @since 1.0.0
 * 
 * @see CryptoCoreSpec
 * @see PrivateKey
 * @see PublicKey
 * @see SecretKey
 * @see Cipher
 * @see GCMParameterSpec
 * @see SecureRandom
 */
//Code optimization remaining (Code Dupe)
@Component
public class CryptoCore implements CryptoCoreSpec<byte[], byte[], SecretKey, PublicKey, PrivateKey, String> {

	private static final String PERIOD_SEPARATOR_REGEX = "\\.";

	// Used as a hack for softhsm oeap padding decryption usecase will be when we
	// will use in HSM
	private static final String RSA_ECB_NO_PADDING = "RSA/ECB/NoPadding";

	private static final String PKCS11_STORE_TYPE = "PKCS11";

	@Value("${mosip.kernel.keygenerator.asymmetric-key-length:2048}")
	private int asymmetricKeyLength;

	private static final String MGF1 = "MGF1";

	private static final String HASH_ALGO = "SHA-256";

	private static final String AES = "AES";

	@Value("${mosip.kernel.crypto.gcm-tag-length:128}")
	private int tagLength;

	@Value("${mosip.kernel.crypto.symmetric-algorithm-name:AES/GCM/PKCS5Padding}")
	private String symmetricAlgorithm;

	@Value("${mosip.kernel.crypto.asymmetric-algorithm-name:RSA/ECB/OAEPWITHSHA-256ANDMGF1PADDING}")
	private String asymmetricAlgorithm;

	@Value("${mosip.kernel.crypto.hash-algorithm-name:PBKDF2WithHmacSHA512}")
	private String passwordAlgorithm;

	@Value("${mosip.kernel.crypto.sign-algorithm-name:RS256}")
	private String signAlgorithm;

	@Value("${mosip.kernel.crypto.hash-symmetric-key-length:256}")
	private int symmetricKeyLength;

	@Value("${mosip.kernel.crypto.hash-iteration:100000}")
	private int iterations;

	@Value("${mosip.kernel.keymanager.hsm.keystore-type:PKCS11}")
	private String keystoreType;

	private SecureRandom secureRandom;

	@PostConstruct
	public void init() {
		secureRandom = new SecureRandom();
	}

	@Override
	public byte[] symmetricEncrypt(SecretKey key, byte[] data, byte[] aad) {
		Objects.requireNonNull(key, SecurityExceptionCodeConstant.MOSIP_INVALID_KEY_EXCEPTION.getErrorMessage());
		CryptoUtils.verifyData(data);
		Cipher cipher;
		try {
			cipher = Cipher.getInstance(symmetricAlgorithm);
		} catch (java.security.NoSuchAlgorithmException | NoSuchPaddingException e) {
			throw new NoSuchAlgorithmException(
					SecurityExceptionCodeConstant.MOSIP_NO_SUCH_ALGORITHM_EXCEPTION.getErrorCode(),
					SecurityExceptionCodeConstant.MOSIP_NO_SUCH_ALGORITHM_EXCEPTION.getErrorMessage(), e);
		}
		byte[] output = null;
		byte[] randomIV = generateIV(cipher.getBlockSize());
		try {
			SecretKeySpec keySpec = new SecretKeySpec(key.getEncoded(), AES);
			GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(tagLength, randomIV);
			cipher.init(Cipher.ENCRYPT_MODE, keySpec, gcmParameterSpec);
			output = new byte[cipher.getOutputSize(data.length) + cipher.getBlockSize()];
			if (aad != null && aad.length != 0) {
				cipher.updateAAD(aad);
			}
			byte[] processData = doFinal(data, cipher);
			System.arraycopy(processData, 0, output, 0, processData.length);
			System.arraycopy(randomIV, 0, output, processData.length, randomIV.length);
		} catch (java.security.InvalidKeyException e) {
			throw new InvalidKeyException(SecurityExceptionCodeConstant.MOSIP_INVALID_KEY_EXCEPTION.getErrorCode(),
					SecurityExceptionCodeConstant.MOSIP_INVALID_KEY_EXCEPTION.getErrorMessage(), e);
		} catch (InvalidAlgorithmParameterException e) {
			throw new InvalidKeyException(
					SecurityExceptionCodeConstant.MOSIP_INVALID_PARAM_SPEC_EXCEPTION.getErrorCode(),
					SecurityExceptionCodeConstant.MOSIP_INVALID_PARAM_SPEC_EXCEPTION.getErrorMessage(), e);
		}
		return output;
	}

	@Override
	public byte[] symmetricEncrypt(SecretKey key, byte[] data, byte[] iv, byte[] aad) {
		Objects.requireNonNull(key, SecurityExceptionCodeConstant.MOSIP_INVALID_KEY_EXCEPTION.getErrorMessage());
		CryptoUtils.verifyData(data);
		if (iv == null) {
			return symmetricEncrypt(key, data, aad);
		}
		Cipher cipher;
		try {
			cipher = Cipher.getInstance(symmetricAlgorithm);
		} catch (java.security.NoSuchAlgorithmException | NoSuchPaddingException e) {
			throw new NoSuchAlgorithmException(
					SecurityExceptionCodeConstant.MOSIP_NO_SUCH_ALGORITHM_EXCEPTION.getErrorCode(),
					SecurityExceptionCodeConstant.MOSIP_NO_SUCH_ALGORITHM_EXCEPTION.getErrorMessage(), e);
		}
		try {
			SecretKeySpec keySpec = new SecretKeySpec(key.getEncoded(), AES);
			GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(tagLength, iv);
			cipher.init(Cipher.ENCRYPT_MODE, keySpec, gcmParameterSpec);
			if (aad != null && aad.length != 0) {
				cipher.updateAAD(aad);
			}
			return doFinal(data, cipher);
		} catch (java.security.InvalidKeyException e) {
			throw new InvalidKeyException(SecurityExceptionCodeConstant.MOSIP_INVALID_KEY_EXCEPTION.getErrorCode(),
					SecurityExceptionCodeConstant.MOSIP_INVALID_KEY_EXCEPTION.getErrorMessage(), e);
		} catch (InvalidAlgorithmParameterException e) {
			throw new InvalidParamSpecException(
					SecurityExceptionCodeConstant.MOSIP_INVALID_PARAM_SPEC_EXCEPTION.getErrorCode(),
					SecurityExceptionCodeConstant.MOSIP_INVALID_PARAM_SPEC_EXCEPTION.getErrorMessage(), e);
		}
	}

	@Override
	public byte[] symmetricDecrypt(SecretKey key, byte[] data, byte[] aad) {
		Objects.requireNonNull(key, SecurityExceptionCodeConstant.MOSIP_INVALID_KEY_EXCEPTION.getErrorMessage());
		CryptoUtils.verifyData(data);
		Cipher cipher;
		try {
			cipher = Cipher.getInstance(symmetricAlgorithm);
		} catch (java.security.NoSuchAlgorithmException | NoSuchPaddingException e) {
			throw new NoSuchAlgorithmException(
					SecurityExceptionCodeConstant.MOSIP_NO_SUCH_ALGORITHM_EXCEPTION.getErrorCode(),
					SecurityExceptionCodeConstant.MOSIP_NO_SUCH_ALGORITHM_EXCEPTION.getErrorMessage(), e);
		}
		byte[] output = null;
		try {
			byte[] randomIV = Arrays.copyOfRange(data, data.length - cipher.getBlockSize(), data.length);
			SecretKeySpec keySpec = new SecretKeySpec(key.getEncoded(), AES);
			GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(tagLength, randomIV);
			cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmParameterSpec);
			if (aad != null && aad.length != 0) {
				cipher.updateAAD(aad);
			}
			output = doFinal(Arrays.copyOf(data, data.length - cipher.getBlockSize()), cipher);
		} catch (java.security.InvalidKeyException e) {
			throw new InvalidKeyException(SecurityExceptionCodeConstant.MOSIP_INVALID_KEY_EXCEPTION.getErrorCode(),
					SecurityExceptionCodeConstant.MOSIP_INVALID_KEY_EXCEPTION.getErrorMessage(), e);
		} catch (InvalidAlgorithmParameterException e) {
			throw new InvalidKeyException(
					SecurityExceptionCodeConstant.MOSIP_INVALID_PARAM_SPEC_EXCEPTION.getErrorCode(),
					SecurityExceptionCodeConstant.MOSIP_INVALID_PARAM_SPEC_EXCEPTION.getErrorMessage(), e);
		} catch (ArrayIndexOutOfBoundsException e) {
			throw new InvalidDataException(
					SecurityExceptionCodeConstant.MOSIP_INVALID_DATA_LENGTH_EXCEPTION.getErrorCode(),
					SecurityExceptionCodeConstant.MOSIP_INVALID_DATA_LENGTH_EXCEPTION.getErrorMessage(), e);
		}
		return output;
	}

	@Override
	public byte[] symmetricDecrypt(SecretKey key, byte[] data, byte[] iv, byte[] aad) {
		Objects.requireNonNull(key, SecurityExceptionCodeConstant.MOSIP_INVALID_KEY_EXCEPTION.getErrorMessage());
		CryptoUtils.verifyData(data);
		if (iv == null) {
			return symmetricDecrypt(key, data, aad);
		}
		Cipher cipher;
		try {
			cipher = Cipher.getInstance(symmetricAlgorithm);
		} catch (java.security.NoSuchAlgorithmException | NoSuchPaddingException e) {
			throw new NoSuchAlgorithmException(
					SecurityExceptionCodeConstant.MOSIP_NO_SUCH_ALGORITHM_EXCEPTION.getErrorCode(),
					SecurityExceptionCodeConstant.MOSIP_NO_SUCH_ALGORITHM_EXCEPTION.getErrorMessage(), e);
		}
		try {
			SecretKeySpec keySpec = new SecretKeySpec(key.getEncoded(), AES);
			GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(tagLength, iv);
			cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmParameterSpec);
			if (aad != null) {
				cipher.updateAAD(aad);
			}
			return doFinal(data, cipher);
		} catch (java.security.InvalidKeyException e) {
			throw new InvalidKeyException(SecurityExceptionCodeConstant.MOSIP_INVALID_KEY_EXCEPTION.getErrorCode(),
					SecurityExceptionCodeConstant.MOSIP_INVALID_KEY_EXCEPTION.getErrorMessage(), e);
		} catch (InvalidAlgorithmParameterException e) {
			throw new InvalidParamSpecException(
					SecurityExceptionCodeConstant.MOSIP_INVALID_PARAM_SPEC_EXCEPTION.getErrorCode(),
					SecurityExceptionCodeConstant.MOSIP_INVALID_PARAM_SPEC_EXCEPTION.getErrorMessage(), e);
		}
	}

	@Override
	public byte[] asymmetricEncrypt(PublicKey key, byte[] data) {
		Objects.requireNonNull(key, SecurityExceptionCodeConstant.MOSIP_INVALID_KEY_EXCEPTION.getErrorMessage());
		CryptoUtils.verifyData(data);
		Cipher cipher;
		try {
			cipher = Cipher.getInstance(asymmetricAlgorithm);
		} catch (java.security.NoSuchAlgorithmException | NoSuchPaddingException e) {
			throw new NoSuchAlgorithmException(
					SecurityExceptionCodeConstant.MOSIP_NO_SUCH_ALGORITHM_EXCEPTION.getErrorCode(),
					SecurityExceptionCodeConstant.MOSIP_NO_SUCH_ALGORITHM_EXCEPTION.getErrorMessage(), e);
		}
		final OAEPParameterSpec oaepParams = new OAEPParameterSpec(HASH_ALGO, MGF1, MGF1ParameterSpec.SHA256,
				PSpecified.DEFAULT);
		try {
			cipher.init(Cipher.ENCRYPT_MODE, key, oaepParams);
		} catch (java.security.InvalidKeyException e) {
			throw new InvalidKeyException(SecurityExceptionCodeConstant.MOSIP_INVALID_KEY_EXCEPTION.getErrorCode(),
					e.getMessage(), e);
		} catch (InvalidAlgorithmParameterException e) {
			throw new InvalidParamSpecException(
					SecurityExceptionCodeConstant.MOSIP_INVALID_PARAM_SPEC_EXCEPTION.getErrorCode(),
					SecurityExceptionCodeConstant.MOSIP_INVALID_PARAM_SPEC_EXCEPTION.getErrorMessage(), e);
		}
		return doFinal(data, cipher);
	}
	
	@Override
	public byte[] asymmetricDecrypt(PrivateKey privateKey, byte[] data) {
		if (PKCS11_STORE_TYPE.equalsIgnoreCase(keystoreType)) {
			BigInteger keyModulus = ((RSAPrivateKey) privateKey).getModulus();
			return asymmetricDecrypt(privateKey, keyModulus, data, null);
		}
		return jceAsymmetricDecrypt(privateKey, data, null);
	}

	@Override
	public byte[] asymmetricDecrypt(PrivateKey privateKey, PublicKey publicKey, byte[] data) {
		if (PKCS11_STORE_TYPE.equalsIgnoreCase(keystoreType)) {
			BigInteger keyModulus = Objects.nonNull(publicKey) ? ((RSAPublicKey) publicKey).getModulus() : 
										((RSAPrivateKey) privateKey).getModulus();
			return asymmetricDecrypt(privateKey, keyModulus, data, null);
		}
		return jceAsymmetricDecrypt(privateKey, data, null);
	}

	@Override
	public byte[] asymmetricDecrypt(PrivateKey privateKey, PublicKey publicKey, byte[] data, String storeType) {
		if (PKCS11_STORE_TYPE.equalsIgnoreCase(keystoreType)) {
			BigInteger keyModulus = Objects.nonNull(publicKey) ? ((RSAPublicKey) publicKey).getModulus() : 
										((RSAPrivateKey) privateKey).getModulus();
			return asymmetricDecrypt(privateKey, keyModulus, data, storeType);
		}
		return jceAsymmetricDecrypt(privateKey, data, storeType);
	}

	private byte[] asymmetricDecrypt(PrivateKey privateKey, BigInteger keyModulus, byte[] data, String storeType) {
		Objects.requireNonNull(privateKey, SecurityExceptionCodeConstant.MOSIP_INVALID_KEY_EXCEPTION.getErrorMessage());
		CryptoUtils.verifyData(data);
		Cipher cipher;
		try {
			cipher = Objects.isNull(storeType) ? Cipher.getInstance(RSA_ECB_NO_PADDING) : 
						Cipher.getInstance(RSA_ECB_NO_PADDING, storeType);
		} catch (java.security.NoSuchAlgorithmException | NoSuchPaddingException | NoSuchProviderException e) {
			throw new NoSuchAlgorithmException(
					SecurityExceptionCodeConstant.MOSIP_NO_SUCH_ALGORITHM_EXCEPTION.getErrorCode(),
					SecurityExceptionCodeConstant.MOSIP_NO_SUCH_ALGORITHM_EXCEPTION.getErrorMessage(), e);
		}

		try {
			cipher.init(Cipher.DECRYPT_MODE, privateKey);
		} catch (java.security.InvalidKeyException e) {
			throw new InvalidKeyException(SecurityExceptionCodeConstant.MOSIP_INVALID_KEY_EXCEPTION.getErrorCode(),
					e.getMessage(), e);
		}
		/*
		 * This is a hack of removing OEAP padding after decryption with NO Padding as
		 * SoftHSM does not support it.Will be removed after HSM implementation
		 */
		byte[] paddedPlainText = doFinal(data, cipher);
		if (paddedPlainText.length < asymmetricKeyLength / 8) {
			byte[] tempPipe = new byte[asymmetricKeyLength / 8];
			System.arraycopy(paddedPlainText, 0, tempPipe, tempPipe.length - paddedPlainText.length,
					paddedPlainText.length);
			paddedPlainText = tempPipe;
		}
		
		return unpadOAEPPadding(paddedPlainText, keyModulus);
	}

	//	  This is a hack of removing OEAP padding after decryption with NO Padding as
	//	  SoftHSM does not support it.Will be removed after HSM implementation
	/**
	 * 
	 * @param paddedPlainText
	 * @param privateKey
	 * @return
	 */
	private byte[] unpadOAEPPadding(byte[] paddedPlainText, BigInteger keyModulus) {
		
	    try {
	    	OAEPEncoding encode = new OAEPEncoding(new RSAEngine(), new SHA256Digest());
		    BigInteger exponent = new BigInteger("1");
		    RSAKeyParameters keyParams = new RSAKeyParameters(false, keyModulus, exponent);
		    encode.init(false, keyParams);
			return encode.processBlock(paddedPlainText, 0, paddedPlainText.length);
		} catch (InvalidCipherTextException e) {
			throw new InvalidKeyException(SecurityExceptionCodeConstant.MOSIP_INVALID_KEY_EXCEPTION
					.getErrorCode(), e.getMessage(), e);
		}	    
	}
	 
	private byte[] jceAsymmetricDecrypt(PrivateKey privateKey, byte[] data, String storeType){
		Objects.requireNonNull(privateKey, SecurityExceptionCodeConstant.MOSIP_INVALID_KEY_EXCEPTION.getErrorMessage());
		CryptoUtils.verifyData(data);
		Cipher cipher;
		try {
			cipher = Objects.isNull(storeType) ? Cipher.getInstance(asymmetricAlgorithm) : 
						Cipher.getInstance(asymmetricAlgorithm, storeType);
			OAEPParameterSpec oaepParams = new OAEPParameterSpec(HASH_ALGO, MGF1, MGF1ParameterSpec.SHA256,
				PSpecified.DEFAULT);
			cipher.init(Cipher.DECRYPT_MODE, privateKey, oaepParams);
			return doFinal(data, cipher);
		} catch (java.security.NoSuchAlgorithmException | NoSuchPaddingException | NoSuchProviderException e) {
			throw new NoSuchAlgorithmException(
					SecurityExceptionCodeConstant.MOSIP_NO_SUCH_ALGORITHM_EXCEPTION.getErrorCode(),
					SecurityExceptionCodeConstant.MOSIP_NO_SUCH_ALGORITHM_EXCEPTION.getErrorMessage(), e);
		} catch (java.security.InvalidKeyException e) {
			throw new InvalidKeyException(SecurityExceptionCodeConstant.MOSIP_INVALID_KEY_EXCEPTION.getErrorCode(),
					e.getMessage(), e);
		} catch (InvalidAlgorithmParameterException e) {
			throw new InvalidParamSpecException(
					SecurityExceptionCodeConstant.MOSIP_INVALID_PARAM_SPEC_EXCEPTION.getErrorCode(),
					SecurityExceptionCodeConstant.MOSIP_INVALID_PARAM_SPEC_EXCEPTION.getErrorMessage(), e);
		}
	}


	@Override
	public String hash(byte[] data, byte[] salt) {
		CryptoUtils.verifyData(data);
		CryptoUtils.verifyData(salt, SecurityExceptionCodeConstant.SALT_PROVIDED_IS_NULL_OR_EMPTY.getErrorCode(),
				SecurityExceptionCodeConstant.SALT_PROVIDED_IS_NULL_OR_EMPTY.getErrorMessage());
		SecretKeyFactory secretKeyFactory;
		char[] convertedData = new String(data).toCharArray();
		PBEKeySpec pbeKeySpec = new PBEKeySpec(convertedData, salt, iterations, symmetricKeyLength);
		SecretKey key;
		try {
			secretKeyFactory = SecretKeyFactory.getInstance(passwordAlgorithm);
			key = secretKeyFactory.generateSecret(pbeKeySpec);
		} catch (InvalidKeySpecException e) {
			throw new InvalidParamSpecException(
					SecurityExceptionCodeConstant.MOSIP_INVALID_PARAM_SPEC_EXCEPTION.getErrorCode(), e.getMessage(), e);
		} catch (java.security.NoSuchAlgorithmException e) {
			throw new NoSuchAlgorithmException(
					SecurityExceptionCodeConstant.MOSIP_NO_SUCH_ALGORITHM_EXCEPTION.getErrorCode(),
					SecurityExceptionCodeConstant.MOSIP_NO_SUCH_ALGORITHM_EXCEPTION.getErrorMessage(), e);
		}
		return DatatypeConverter.printHexBinary(key.getEncoded());
	}

	@Override
	public String sign(byte[] data, PrivateKey privateKey) {
		Objects.requireNonNull(privateKey, SecurityExceptionCodeConstant.MOSIP_INVALID_KEY_EXCEPTION.getErrorMessage());
		CryptoUtils.verifyData(data);
		JsonWebSignature jws = new JsonWebSignature();
		jws.setPayloadBytes(data);
		jws.setAlgorithmHeaderValue(signAlgorithm);
		jws.setKey(privateKey);
		jws.setDoKeyValidation(false);
		try {
			return jws.getDetachedContentCompactSerialization();
		} catch (JoseException e) {
			throw new SignatureException(SecurityExceptionCodeConstant.MOSIP_SIGNATURE_EXCEPTION.getErrorCode(),
					e.getMessage(), e);
		}
	}

	@Override
	public boolean verifySignature(byte[] data, String sign, PublicKey publicKey) {
		if (EmptyCheckUtils.isNullEmpty(sign)) {
			throw new SignatureException(SecurityExceptionCodeConstant.MOSIP_SIGNATURE_EXCEPTION.getErrorCode(),
					SecurityExceptionCodeConstant.MOSIP_SIGNATURE_EXCEPTION.getErrorMessage());
		}
		Objects.requireNonNull(publicKey, SecurityExceptionCodeConstant.MOSIP_INVALID_KEY_EXCEPTION.getErrorMessage());
		CryptoUtils.verifyData(data);
		JsonWebSignature jws = new JsonWebSignature();
		try {
			String[] parts = sign.split(PERIOD_SEPARATOR_REGEX);
			parts[1] = CryptoUtil.encodeBase64(data);
			jws.setCompactSerialization(CompactSerializer.serialize(parts));
			jws.setKey(publicKey);
			return jws.verifySignature();
		} catch (ArrayIndexOutOfBoundsException | JoseException e) {
			throw new SignatureException(SecurityExceptionCodeConstant.MOSIP_SIGNATURE_EXCEPTION.getErrorCode(),
					e.getMessage(), e);
		}

	}

	@SuppressWarnings("unchecked")
	@Override
	public SecureRandom random() {
		return secureRandom;
	}

	/**
	 * Generator for IV(Initialisation Vector)
	 * 
	 * @param blockSize blocksize of current cipher
	 * @return generated IV
	 */
	private byte[] generateIV(int blockSize) {
		byte[] byteIV = new byte[blockSize];
		secureRandom.nextBytes(byteIV);
		return byteIV;
	}

	private byte[] doFinal(byte[] data, Cipher cipher) {
		try {
			return cipher.doFinal(data);
		} catch (IllegalBlockSizeException e) {
			throw new InvalidDataException(
					SecurityExceptionCodeConstant.MOSIP_INVALID_DATA_SIZE_EXCEPTION.getErrorCode(), e.getMessage(), e);
		} catch (BadPaddingException e) {
			throw new InvalidDataException(
					SecurityExceptionCodeConstant.MOSIP_INVALID_ENCRYPTED_DATA_CORRUPT_EXCEPTION.getErrorCode(),
					e.getMessage(), e);
		}
	}

	/*
	 * This two methods here are for temporary, Unit test for this will be written
	 * in next versions
	 */
	@Override
	public String sign(byte[] data, PrivateKey privateKey, X509Certificate x509Certificate) {
		Objects.requireNonNull(privateKey, SecurityExceptionCodeConstant.MOSIP_INVALID_KEY_EXCEPTION.getErrorMessage());
		CryptoUtils.verifyData(data);
		JsonWebSignature jws = new JsonWebSignature();
		List<X509Certificate> certList = new ArrayList<>();
		certList.add(x509Certificate);
		X509Certificate[] certArray = certList.toArray(new X509Certificate[] {});
		jws.setCertificateChainHeaderValue(certArray);
		jws.setPayloadBytes(data);
		jws.setAlgorithmHeaderValue(signAlgorithm);
		jws.setKey(privateKey);
		jws.setDoKeyValidation(false);
		try {
			return jws.getCompactSerialization();
		} catch (JoseException e) {
			throw new SignatureException(SecurityExceptionCodeConstant.MOSIP_SIGNATURE_EXCEPTION.getErrorCode(),
					e.getMessage(), e);
		}
	}

	/*
	 * This two methods here are for temporary, Unit test for this will be written
	 * in next versions
	 */
	@Override
	public boolean verifySignature(String sign) {
		if (EmptyCheckUtils.isNullEmpty(sign)) {
			throw new SignatureException(SecurityExceptionCodeConstant.MOSIP_SIGNATURE_EXCEPTION.getErrorCode(),
					SecurityExceptionCodeConstant.MOSIP_SIGNATURE_EXCEPTION.getErrorMessage());
		}
		JsonWebSignature jws = new JsonWebSignature();
		try {
			jws.setCompactSerialization(sign);
			List<X509Certificate> certificateChainHeaderValue = jws.getCertificateChainHeaderValue();
			X509Certificate certificate = certificateChainHeaderValue.get(0);
			certificate.checkValidity();
			PublicKey publicKey = certificate.getPublicKey();
			jws.setKey(publicKey);
			return jws.verifySignature();
		} catch (JoseException | CertificateExpiredException | CertificateNotYetValidException e) {
			throw new SignatureException(SecurityExceptionCodeConstant.MOSIP_SIGNATURE_EXCEPTION.getErrorCode(),
					e.getMessage(), e);
		}

	}

	
}
