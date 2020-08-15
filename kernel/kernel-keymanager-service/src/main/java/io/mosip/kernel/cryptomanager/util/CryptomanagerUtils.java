/*
 * 
 * 
 * 
 * 
 */
package io.mosip.kernel.cryptomanager.util;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import io.mosip.kernel.core.exception.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import io.mosip.kernel.core.crypto.exception.InvalidKeyException;
import io.mosip.kernel.core.util.CryptoUtil;
import io.mosip.kernel.core.util.DateUtils;
import io.mosip.kernel.cryptomanager.constant.CryptomanagerErrorCode;
import io.mosip.kernel.cryptomanager.dto.CryptomanagerRequestDto;
import io.mosip.kernel.keymanagerservice.dto.SymmetricKeyRequestDto;
import io.mosip.kernel.keymanagerservice.service.KeymanagerService;

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

	/**
	 * Calls Key-Manager-Service to get public key of an application.
	 *
	 * @param cryptomanagerRequestDto            {@link CryptomanagerRequestDto} instance
	 * @return {@link PublicKey} returned by Key Manager Service
	 */
	public PublicKey getPublicKey(CryptomanagerRequestDto cryptomanagerRequestDto) {
		try {
			String 	publicKey = getPublicKeyFromKeyManager(cryptomanagerRequestDto.getApplicationId(),
						DateUtils.formatToISOString(cryptomanagerRequestDto.getTimeStamp()),
						cryptomanagerRequestDto.getReferenceId());
			return KeyFactory.getInstance(asymmetricAlgorithmName)
					.generatePublic(new X509EncodedKeySpec(CryptoUtil.decodeBase64(publicKey)));
		} catch (InvalidKeySpecException e) {
			throw new InvalidKeyException(CryptomanagerErrorCode.INVALID_SPEC_PUBLIC_KEY.getErrorCode(),
					CryptomanagerErrorCode.INVALID_SPEC_PUBLIC_KEY.getErrorMessage());
		} catch (NoSuchAlgorithmException e) {
			throw new io.mosip.kernel.core.exception.NoSuchAlgorithmException(
					CryptomanagerErrorCode.NO_SUCH_ALGORITHM_EXCEPTION.getErrorCode(),
					CryptomanagerErrorCode.NO_SUCH_ALGORITHM_EXCEPTION.getErrorMessage());
		}
	}

	/**
	 * Gets the public key from key manager.
	 *
	 * @param appId the app id
	 * @param timestamp the timestamp
	 * @param refId the ref id
	 * @return the public key from key manager
	 */
	private String getPublicKeyFromKeyManager(String appId, String timestamp, String refId) {
		return keyManager.getPublicKey(appId, timestamp, Optional.ofNullable(refId)).getPublicKey();
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
				cryptomanagerRequestDto.getReferenceId(), cryptomanagerRequestDto.getData());
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
}
