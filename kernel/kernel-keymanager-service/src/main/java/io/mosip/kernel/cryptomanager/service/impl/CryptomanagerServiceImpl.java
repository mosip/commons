/*
 * 
 * 
 * 
 * 
 */
package io.mosip.kernel.cryptomanager.service.impl;

import static java.util.Arrays.copyOfRange;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.util.Arrays;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.core.crypto.spi.CryptoCoreSpec;
import io.mosip.kernel.core.util.CryptoUtil;
import io.mosip.kernel.cryptomanager.constant.CryptomanagerConstant;
import io.mosip.kernel.cryptomanager.constant.CryptomanagerErrorCode;
import io.mosip.kernel.cryptomanager.dto.CryptoWithPinRequestDto;
import io.mosip.kernel.cryptomanager.dto.CryptoWithPinResponseDto;
import io.mosip.kernel.cryptomanager.dto.CryptomanagerRequestDto;
import io.mosip.kernel.cryptomanager.dto.CryptomanagerResponseDto;
import io.mosip.kernel.cryptomanager.exception.CryptoManagerSerivceException;
import io.mosip.kernel.cryptomanager.service.CryptomanagerService;
import io.mosip.kernel.cryptomanager.util.CryptomanagerUtils;
import io.mosip.kernel.keygenerator.bouncycastle.KeyGenerator;
import io.mosip.kernel.keymanagerservice.logger.KeymanagerLogger;

/**
 * Service Implementation for {@link CryptomanagerService} interface
 * 
 * @author Urvil Joshi
 * @author Srinivasan
 *
 * @since 1.0.0
 */
@Service
public class CryptomanagerServiceImpl implements CryptomanagerService {

	private static final int GCM_NONCE_LENGTH = 12;

	private static final int PBE_SALT_LENGTH = 32;

	private static final String AES_KEY_TYPE = "AES";

	private static final Logger LOGGER = KeymanagerLogger.getLogger(CryptomanagerServiceImpl.class);

	/**
	 * KeySplitter for splitting key and data
	 */
	@Value("${mosip.kernel.data-key-splitter}")
	private String keySplitter;

	/** The 1.1.3 no thumbprint support flag. */
	@Value("${mosip.kernel.keymanager.113nothumbprint.support:false}")
	private boolean noThumbprint;

	@Value("${mosip.sign-certificate-refid:SIGN}")
	private String signRefId;

	/** The sign applicationid. */
	@Value("${mosip.sign.applicationid:KERNEL}")
	private String signApplicationId;

	/**
	 * {@link KeyGenerator} instance
	 */
	@Autowired
	KeyGenerator keyGenerator;

	/**
	 * {@link CryptomanagerUtils} instance
	 */
	@Autowired
	CryptomanagerUtils cryptomanagerUtil;

	/**
	 * {@link CryptoCoreSpec} instance for cryptographic functionalities.
	 */
	@Autowired
	private CryptoCoreSpec<byte[], byte[], SecretKey, PublicKey, PrivateKey, String> cryptoCore;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.mosip.kernel.cryptography.service.CryptographyService#encrypt(io.mosip.
	 * kernel.cryptography.dto.CryptographyRequestDto)
	 */
	@Override
	public CryptomanagerResponseDto encrypt(CryptomanagerRequestDto cryptoRequestDto) {
		LOGGER.info(CryptomanagerConstant.SESSIONID, CryptomanagerConstant.ENCRYPT, CryptomanagerConstant.ENCRYPT, 
						"Request for data encryption.");
		
		if(!cryptomanagerUtil.isDataValid(cryptoRequestDto.getReferenceId()) || 
			(cryptoRequestDto.getApplicationId().equalsIgnoreCase(signApplicationId) && 
				cryptoRequestDto.getReferenceId().equalsIgnoreCase(signRefId))) {
			LOGGER.error(CryptomanagerConstant.SESSIONID, CryptomanagerConstant.ENCRYPT, CryptomanagerConstant.ENCRYPT,
								"Not Allowed to preform encryption with Master Key.");
			throw new CryptoManagerSerivceException(CryptomanagerErrorCode.ENCRYPT_NOT_ALLOWED_ERROR.getErrorCode(),
						CryptomanagerErrorCode.ENCRYPT_NOT_ALLOWED_ERROR.getErrorMessage());
		}

		SecretKey secretKey = keyGenerator.getSymmetricKey();
		final byte[] encryptedData;
		byte[] headerBytes = new byte[0];
		if (cryptomanagerUtil.isValidSalt(CryptomanagerUtils.nullOrTrim(cryptoRequestDto.getSalt()))) {
			encryptedData = cryptoCore.symmetricEncrypt(secretKey, CryptoUtil.decodeBase64(cryptoRequestDto.getData()),
					CryptoUtil.decodeBase64(CryptomanagerUtils.nullOrTrim(cryptoRequestDto.getSalt())),
					CryptoUtil.decodeBase64(CryptomanagerUtils.nullOrTrim(cryptoRequestDto.getAad())));
		} else {
			byte[] aad = CryptoUtil.decodeBase64(CryptomanagerUtils.nullOrTrim(cryptoRequestDto.getAad()));
			if (aad == null || aad.length == 0){
				encryptedData = generateAadAndEncryptData(secretKey, cryptoRequestDto.getData());
				headerBytes = CryptomanagerConstant.VERSION_RSA_2048;
			} else {
				encryptedData = cryptoCore.symmetricEncrypt(secretKey, CryptoUtil.decodeBase64(cryptoRequestDto.getData()),
										aad);
			}
		}

		Certificate certificate = cryptomanagerUtil.getCertificate(cryptoRequestDto);
		LOGGER.info(CryptomanagerConstant.SESSIONID, CryptomanagerConstant.ENCRYPT, CryptomanagerConstant.ENCRYPT, 
						"Found the cerificate, proceeding with session key encryption.");
		PublicKey publicKey = certificate.getPublicKey();
		final byte[] encryptedSymmetricKey = cryptoCore.asymmetricEncrypt(publicKey, secretKey.getEncoded());
		LOGGER.info(CryptomanagerConstant.SESSIONID, CryptomanagerConstant.ENCRYPT, CryptomanagerConstant.ENCRYPT, 
						"Session key encryption completed.");
		boolean prependThumbprint = cryptoRequestDto.getPrependThumbprint() == null ? false : cryptoRequestDto.getPrependThumbprint();
		CryptomanagerResponseDto cryptoResponseDto = new CryptomanagerResponseDto();
		// support of 1.1.3 no thumbprint is configured as true & encryption request with no thumbprint
		// request thumbprint flag will not be considered if support no thumbprint is set to false. 
		if (noThumbprint && !prependThumbprint) {
			byte[] finalEncKeyBytes = cryptomanagerUtil.concatByteArrays(headerBytes, encryptedSymmetricKey);
			cryptoResponseDto.setData(CryptoUtil.encodeBase64(CryptoUtil.combineByteArray(encryptedData, finalEncKeyBytes, keySplitter)));
			return cryptoResponseDto;
		} 
		byte[] certThumbprint = cryptomanagerUtil.getCertificateThumbprint(certificate);
		byte[] concatedData = cryptomanagerUtil.concatCertThumbprint(certThumbprint, encryptedSymmetricKey);
		byte[] finalEncKeyBytes = cryptomanagerUtil.concatByteArrays(headerBytes, concatedData);
		cryptoResponseDto.setData(CryptoUtil.encodeBase64(CryptoUtil.combineByteArray(encryptedData, 
							finalEncKeyBytes, keySplitter)));
		return cryptoResponseDto;
	}

	private byte[] generateAadAndEncryptData(SecretKey secretKey, String data){
		LOGGER.info(CryptomanagerConstant.SESSIONID, CryptomanagerConstant.ENCRYPT, CryptomanagerConstant.ENCRYPT, 
						"Provided AAD value is null or empty byte array. So generating random 32 bytes for AAD.");
		byte[] aad = cryptomanagerUtil.generateRandomBytes(CryptomanagerConstant.GCM_AAD_LENGTH);
		byte[] nonce = copyOfRange(aad, 0, CryptomanagerConstant.GCM_NONCE_LENGTH);
		byte[] encData = cryptoCore.symmetricEncrypt(secretKey, CryptoUtil.decodeBase64(data),
								nonce, aad);
		return cryptomanagerUtil.concatByteArrays(aad, encData);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.mosip.kernel.cryptography.service.CryptographyService#decrypt(io.mosip.
	 * kernel.cryptography.dto.CryptographyRequestDto)
	 */
	@Override
	public CryptomanagerResponseDto decrypt(CryptomanagerRequestDto cryptoRequestDto) {
		LOGGER.info(CryptomanagerConstant.SESSIONID, CryptomanagerConstant.DECRYPT, CryptomanagerConstant.DECRYPT, 
						"Request for data decryption.");

		int keyDemiliterIndex = 0;
		byte[] encryptedHybridData = CryptoUtil.decodeBase64(cryptoRequestDto.getData());
		keyDemiliterIndex = CryptoUtil.getSplitterIndex(encryptedHybridData, keyDemiliterIndex, keySplitter);
		byte[] encryptedKey = copyOfRange(encryptedHybridData, 0, keyDemiliterIndex);
		byte[] encryptedData = copyOfRange(encryptedHybridData, keyDemiliterIndex + keySplitter.length(),
				encryptedHybridData.length);
		
		byte[] headerBytes = cryptomanagerUtil.parseEncryptKeyHeader(encryptedKey);
		cryptoRequestDto.setData(CryptoUtil.encodeBase64(copyOfRange(encryptedKey, headerBytes.length, encryptedKey.length)));
		SecretKey decryptedSymmetricKey = cryptomanagerUtil.getDecryptedSymmetricKey(cryptoRequestDto);
		LOGGER.info(CryptomanagerConstant.SESSIONID, CryptomanagerConstant.DECRYPT, CryptomanagerConstant.DECRYPT, 
						"Session Decryption completed.");
		final byte[] decryptedData;
		if (cryptomanagerUtil.isValidSalt(CryptomanagerUtils.nullOrTrim(cryptoRequestDto.getSalt()))) {
			decryptedData = cryptoCore.symmetricDecrypt(decryptedSymmetricKey, encryptedData,
					CryptoUtil.decodeBase64(CryptomanagerUtils.nullOrTrim(cryptoRequestDto.getSalt())),
					CryptoUtil.decodeBase64(CryptomanagerUtils.nullOrTrim(cryptoRequestDto.getAad())));
		} else {
			if (Arrays.equals(headerBytes, CryptomanagerConstant.VERSION_RSA_2048)) {
				decryptedData = splitAadAndDecryptData(decryptedSymmetricKey, encryptedData);
			} else {
				decryptedData = cryptoCore.symmetricDecrypt(decryptedSymmetricKey, encryptedData,
						CryptoUtil.decodeBase64(CryptomanagerUtils.nullOrTrim(cryptoRequestDto.getAad())));
			}
		}
		LOGGER.info(CryptomanagerConstant.SESSIONID, CryptomanagerConstant.DECRYPT, CryptomanagerConstant.DECRYPT, 
						"Data decryption completed.");
		CryptomanagerResponseDto cryptoResponseDto = new CryptomanagerResponseDto();
		cryptoResponseDto.setData(CryptoUtil.encodeBase64(decryptedData));
		return cryptoResponseDto;
	}

	private byte[] splitAadAndDecryptData(SecretKey symmetricKey, byte[] encryptedData) {

		byte[] aad = copyOfRange(encryptedData, 0, CryptomanagerConstant.GCM_AAD_LENGTH);
		byte[] nonce = copyOfRange(aad, 0, CryptomanagerConstant.GCM_NONCE_LENGTH);
		byte[] finalEncData = copyOfRange(encryptedData, CryptomanagerConstant.GCM_AAD_LENGTH, encryptedData.length);
		return cryptoCore.symmetricDecrypt(symmetricKey, finalEncData, nonce, aad);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.mosip.kernel.cryptomanager.service.CryptomanagerService#encryptWithPin(io.mosip.
	 * kernel.cryptomanager.dto.CryptoWithPinRequestDto)
	 */
	@Override
	public CryptoWithPinResponseDto encryptWithPin(CryptoWithPinRequestDto requestDto) {
		LOGGER.info(CryptomanagerConstant.SESSIONID, CryptomanagerConstant.ENCRYPT_PIN, CryptomanagerConstant.ENCRYPT_PIN, 
						"Request for data encryption with Pin.");

		String dataToEnc = requestDto.getData();
		String userPin = requestDto.getUserPin();

		if(!cryptomanagerUtil.isDataValid(dataToEnc) || !cryptomanagerUtil.isDataValid(userPin)) {
			LOGGER.error(CryptomanagerConstant.SESSIONID, CryptomanagerConstant.ENCRYPT_PIN, CryptomanagerConstant.ENCRYPT_PIN,
								"Either Data to encrypt or user pin is blank.");
			throw new CryptoManagerSerivceException(CryptomanagerErrorCode.INVALID_REQUEST.getErrorCode(),
						CryptomanagerErrorCode.INVALID_REQUEST.getErrorMessage());
		}

		SecureRandom sRandom = new SecureRandom(); 
		byte[] pbeSalt = new byte[PBE_SALT_LENGTH];
		sRandom.nextBytes(pbeSalt);

		SecretKey derivedKey = getDerivedKey(userPin, pbeSalt);
		byte[] gcmNonce = new byte[GCM_NONCE_LENGTH];
		sRandom.nextBytes(gcmNonce);
		byte[] encryptedData = cryptoCore.symmetricEncrypt(derivedKey, dataToEnc.getBytes(), gcmNonce, pbeSalt);

		byte[] finalEncryptedData = new byte[encryptedData.length + PBE_SALT_LENGTH + GCM_NONCE_LENGTH];
		System.arraycopy(pbeSalt, 0, finalEncryptedData, 0, pbeSalt.length);
		System.arraycopy(gcmNonce, 0, finalEncryptedData, pbeSalt.length, gcmNonce.length);
		System.arraycopy(encryptedData, 0, finalEncryptedData, pbeSalt.length + gcmNonce.length, encryptedData.length);
		CryptoWithPinResponseDto responseDto = new CryptoWithPinResponseDto();
		responseDto.setData(CryptoUtil.encodeBase64(finalEncryptedData));
		return responseDto;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.mosip.kernel.cryptomanager.service.CryptomanagerService#decryptWithPin(io.mosip.
	 * kernel.cryptomanager.dto.CryptoWithPinRequestDto)
	 */
	@Override
	public CryptoWithPinResponseDto decryptWithPin(CryptoWithPinRequestDto requestDto) {
		LOGGER.info(CryptomanagerConstant.SESSIONID, CryptomanagerConstant.ENCRYPT_PIN, CryptomanagerConstant.ENCRYPT_PIN, 
						"Request for data decryption with Pin.");

		String dataToDec = requestDto.getData();
		String userPin = requestDto.getUserPin();

		if(!cryptomanagerUtil.isDataValid(dataToDec) || !cryptomanagerUtil.isDataValid(userPin)) {
			LOGGER.error(CryptomanagerConstant.SESSIONID, CryptomanagerConstant.ENCRYPT_PIN, CryptomanagerConstant.ENCRYPT_PIN,
								"Either Data to decrypt or user pin is blank.");
			throw new CryptoManagerSerivceException(CryptomanagerErrorCode.INVALID_REQUEST.getErrorCode(),
						CryptomanagerErrorCode.INVALID_REQUEST.getErrorMessage());
		}

		byte[] decodedEncryptedData = CryptoUtil.decodeBase64(dataToDec);
		byte[] pbeSalt = Arrays.copyOfRange(decodedEncryptedData, 0, PBE_SALT_LENGTH);
		byte[] gcmNonce = Arrays.copyOfRange(decodedEncryptedData, PBE_SALT_LENGTH, PBE_SALT_LENGTH + GCM_NONCE_LENGTH);
		byte[] encryptedData = Arrays.copyOfRange(decodedEncryptedData, PBE_SALT_LENGTH + GCM_NONCE_LENGTH,	decodedEncryptedData.length);

		SecretKey derivedKey = getDerivedKey(userPin, pbeSalt);
		byte[]  decryptedData = cryptoCore.symmetricDecrypt(derivedKey, encryptedData, gcmNonce, pbeSalt);
		CryptoWithPinResponseDto responseDto = new CryptoWithPinResponseDto();
		responseDto.setData(new String(decryptedData));
		return responseDto;
	}

	private SecretKey getDerivedKey(String userPin, byte[] salt) {
		String derivedKeyHex = cryptoCore.hash(userPin.getBytes(), salt);
		byte[] derivedKey = cryptomanagerUtil.hexDecode(derivedKeyHex);
		return new SecretKeySpec(derivedKey, AES_KEY_TYPE);
	}

}
