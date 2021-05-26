/*
 * 
 * 
 * 
 * 
 */
package io.mosip.kernel.cryptomanager.util;

import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.digest.DigestUtils;
import java.util.Arrays;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import io.mosip.kernel.core.exception.ParseException;
import io.mosip.kernel.core.util.CryptoUtil;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.cryptomanager.constant.CryptomanagerConstant;
import io.mosip.kernel.cryptomanager.constant.CryptomanagerErrorCode;
import io.mosip.kernel.cryptomanager.dto.CryptomanagerRequestDto;
import io.mosip.kernel.cryptomanager.exception.CryptoManagerSerivceException;
import io.mosip.kernel.keymanagerservice.dto.SymmetricKeyRequestDto;
import io.mosip.kernel.keymanagerservice.service.KeymanagerService;
import io.mosip.kernel.keymanagerservice.util.KeymanagerUtil;
import io.mosip.kernel.keymanagerservice.logger.KeymanagerLogger;

/**
 * Util class for this project.
 *
 * @author Urvil Joshi
 * @author Manoj SP
 * @since 1.0.0
 */
@RefreshScope
@Component
public class CryptomanagerUtils {

	private static final Logger LOGGER = KeymanagerLogger.getLogger(CryptomanagerUtils.class);

	/** The Constant UTC_DATETIME_PATTERN. */
	private static final String UTC_DATETIME_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

	/** Asymmetric Algorithm Name. */
	@Value("${mosip.kernel.keygenerator.asymmetric-algorithm-name}")
	private String asymmetricAlgorithmName;

	/** Symmetric Algorithm Name. */
	@Value("${mosip.kernel.keygenerator.symmetric-algorithm-name}")
	private String symmetricAlgorithmName;

	
	/** Key Splitter. */
	@Value("${mosip.kernel.data-key-splitter}")
	private String keySplitter;

	/** The key manager. */
	@Autowired
	private KeymanagerService keyManager;

	@Autowired
	private KeymanagerUtil keymanagerUtil;

	/**
	 * Calls Key-Manager-Service to get public key of an application.
	 *
	 * @param cryptomanagerRequestDto            {@link CryptomanagerRequestDto} instance
	 * @return {@link Certificate} returned by Key Manager Service
	 */
	public Certificate getCertificate(CryptomanagerRequestDto cryptomanagerRequestDto) {
		String certData = getCertificateFromKeyManager(cryptomanagerRequestDto.getApplicationId(),
										cryptomanagerRequestDto.getReferenceId());

		return keymanagerUtil.convertToCertificate(certData);
	}

	/**
	 * Gets the certificate from key manager.
	 *
	 * @param appId the app id
	 * @param refId the ref id
	 * @return the certificate data from key manager
	 */
	private String getCertificateFromKeyManager(String appId, String refId) {
		return keyManager.getCertificate(appId, Optional.ofNullable(refId)).getCertificate();
	}


	/**
	 * Calls Key-Manager-Service to decrypt symmetric key.
	 *
	 * @param cryptomanagerRequestDto            {@link CryptomanagerRequestDto} instance
	 * @return Decrypted {@link SecretKey} from Key Manager Service
	 */
	public SecretKey getDecryptedSymmetricKey(CryptomanagerRequestDto cryptomanagerRequestDto) {
		byte[] symmetricKey = CryptoUtil.decodeBase64(decryptSymmetricKeyUsingKeyManager(cryptomanagerRequestDto));
		return new SecretKeySpec(symmetricKey, 0, symmetricKey.length, symmetricAlgorithmName);
	}

	/**
	 * Decrypt symmetric key using key manager.
	 *
	 * @param cryptomanagerRequestDto the cryptomanager request dto
	 * @return the string
	 */
	private String decryptSymmetricKeyUsingKeyManager(CryptomanagerRequestDto cryptomanagerRequestDto) {
		SymmetricKeyRequestDto symmetricKeyRequestDto = new SymmetricKeyRequestDto(
				cryptomanagerRequestDto.getApplicationId(), cryptomanagerRequestDto.getTimeStamp(),
				cryptomanagerRequestDto.getReferenceId(), cryptomanagerRequestDto.getData(), cryptomanagerRequestDto.getPrependThumbprint());
		return keyManager.decryptSymmetricKey(symmetricKeyRequestDto).getSymmetricKey();
	}

	/**
	 * Change Parameter form to trim if not null.
	 *
	 * @param parameter            parameter
	 * @return null if null;else trimmed string
	 */
	public static String nullOrTrim(String parameter) {
		return parameter == null ? null : parameter.trim();
	}

	/**
	 * Function to check is salt is valid.
	 *
	 * @param salt            salt
	 * @return true if salt is valid, else false
	 */
	public boolean isValidSalt(String salt) {
		return salt != null && !salt.trim().isEmpty();
	}

	/**
	 * Parse a date string of pattern UTC_DATETIME_PATTERN into
	 * {@link LocalDateTime}.
	 *
	 * @param dateTimeof type {@link String} of pattern UTC_DATETIME_PATTERN
	 * @return a {@link LocalDateTime} of given pattern
	 */
	public LocalDateTime parseToLocalDateTime(String dateTime) {
		return LocalDateTime.parse(dateTime, DateTimeFormatter.ofPattern(UTC_DATETIME_PATTERN));
	}

	/**
	 * hex decode string to byte array
	 *
	 * @param hexData type {@link String} 
	 * @return a {@link byte[]} of given data
	 */
	public byte[] hexDecode(String hexData) {

		char[] hexDataCharArr = hexData.toCharArray();
		int dataLength = hexDataCharArr.length;

        if ((dataLength & 0x01) != 0) {
			throw new ParseException(CryptomanagerErrorCode.HEX_DATA_PARSE_EXCEPTION.getErrorCode(), 
					CryptomanagerErrorCode.HEX_DATA_PARSE_EXCEPTION.getErrorMessage());
        }

        byte[] decodedBytes = new byte[dataLength >> 1];

        for (int i = 0, j = 0; j < dataLength; i++) {
            int f = Character.digit(hexDataCharArr[j], 16) << 4;
            j++;
            f = f | Character.digit(hexDataCharArr[j], 16);
            j++;
            decodedBytes[i] = (byte) (f & 0xFF);
        }
        return decodedBytes;
	}

	public byte[] getCertificateThumbprint(Certificate cert) {
		try {
            return DigestUtils.sha256(cert.getEncoded());
		} catch (CertificateEncodingException e) {
			LOGGER.error(CryptomanagerConstant.SESSIONID, CryptomanagerConstant.ENCRYPT, "", 
									"Error generating certificate thumbprint.");
            throw new CryptoManagerSerivceException(CryptomanagerErrorCode.CERTIFICATE_THUMBPRINT_ERROR.getErrorCode(),
						CryptomanagerErrorCode.CERTIFICATE_THUMBPRINT_ERROR.getErrorMessage());
		}
	}

	public byte[] concatCertThumbprint(byte[] certThumbprint, byte[] encryptedKey){
		byte[] finalData = new byte[CryptomanagerConstant.THUMBPRINT_LENGTH + encryptedKey.length];
		System.arraycopy(certThumbprint, 0, finalData, 0, certThumbprint.length);
		System.arraycopy(encryptedKey, 0, finalData, certThumbprint.length, encryptedKey.length);
		return finalData;
	}

	public byte[] generateRandomBytes(int size) {
		byte[] randomBytes = new byte[size];
		SecureRandom secureRandom = new SecureRandom();
		secureRandom.nextBytes(randomBytes);
		return randomBytes;
	}

	public byte[] concatByteArrays(byte[] array1, byte[] array2){
		byte[] finalData = new byte[array1.length + array2.length];
		System.arraycopy(array1, 0, finalData, 0, array1.length);
		System.arraycopy(array2, 0, finalData, array1.length, array2.length);
		return finalData;
	}

	public byte[] parseEncryptKeyHeader(byte[] encryptedKey){
		byte[] versionHeaderBytes = Arrays.copyOfRange(encryptedKey, 0, CryptomanagerConstant.VERSION_RSA_2048.length);
		if (!Arrays.equals(versionHeaderBytes, CryptomanagerConstant.VERSION_RSA_2048)) {
			return new byte[0];
		}
		return versionHeaderBytes;
	}

	public boolean isDataValid(String anyData) {
		return anyData != null && !anyData.trim().isEmpty();
	}
}
