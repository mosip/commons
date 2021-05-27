package io.mosip.kernel.zkcryptoservice.service.impl;

import java.nio.ByteBuffer;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.mosip.kernel.core.crypto.spi.CryptoCoreSpec;
import io.mosip.kernel.core.keymanager.spi.KeyStore;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.core.util.CryptoUtil;
import io.mosip.kernel.core.util.DateUtils;
import io.mosip.kernel.cryptomanager.util.CryptomanagerUtils;
import io.mosip.kernel.keymanagerservice.constant.KeymanagerConstant;
import io.mosip.kernel.keymanagerservice.dto.SymmetricKeyRequestDto;
import io.mosip.kernel.keymanagerservice.entity.KeyAlias;
import io.mosip.kernel.keymanagerservice.exception.NoUniqueAliasException;
import io.mosip.kernel.keymanagerservice.helper.KeymanagerDBHelper;
import io.mosip.kernel.keymanagerservice.logger.KeymanagerLogger;
import io.mosip.kernel.keymanagerservice.repository.DataEncryptKeystoreRepository;
import io.mosip.kernel.keymanagerservice.repository.KeyAliasRepository;
import io.mosip.kernel.keymanagerservice.repository.KeyStoreRepository;
import io.mosip.kernel.keymanagerservice.service.KeymanagerService;
import io.mosip.kernel.keymanagerservice.util.KeymanagerUtil;
import io.mosip.kernel.zkcryptoservice.constant.ZKCryptoErrorConstants;
import io.mosip.kernel.zkcryptoservice.constant.ZKCryptoManagerConstants;
import io.mosip.kernel.zkcryptoservice.dto.CryptoDataDto;
import io.mosip.kernel.zkcryptoservice.dto.ReEncryptRandomKeyResponseDto;
import io.mosip.kernel.zkcryptoservice.dto.ZKCryptoRequestDto;
import io.mosip.kernel.zkcryptoservice.dto.ZKCryptoResponseDto;
import io.mosip.kernel.zkcryptoservice.exception.ZKCryptoException;
import io.mosip.kernel.zkcryptoservice.exception.ZKKeyDerivationException;
import io.mosip.kernel.zkcryptoservice.exception.ZKRandomKeyDecryptionException;
import io.mosip.kernel.zkcryptoservice.service.spi.ZKCryptoManagerService;

/**
 * Service Implementation for {@link ZKCryptoManagerService} interface
 * 
 * @author Mahammed Taheer
 *
 * @since 1.1.2
 */
@Service
@Transactional
public class ZKCryptoManagerServiceImpl implements ZKCryptoManagerService, InitializingBean {

	private static final Logger LOGGER = KeymanagerLogger.getLogger(ZKCryptoManagerServiceImpl.class);
	
    @Value("${mosip.kernel.crypto.symmetric-algorithm-name}")
	private String aesGCMTransformation;

	@Value("${mosip.kernel.zkcrypto.masterkey.application.id}")
	private String masterKeyAppId;

	@Value("${mosip.kernel.zkcrypto.masterkey.reference.id}")
    private String masterKeyRefId;

    @Value("${mosip.kernel.zkcrypto.publickey.application.id}")
	private String pubKeyApplicationId;

	@Value("${mosip.kernel.zkcrypto.publickey.reference.id}")
    private String pubKeyReferenceId;

    @Value("${mosip.kernel.zkcrypto.wrap.algorithm-name}")
	private String aesECBTransformation;
	
	@Value("${mosip.kernel.zkcrypto.derive.encrypt.algorithm-name}")
    private String aesECBPKCS5Transformation;
        
    @Autowired
	private DataEncryptKeystoreRepository dataEncryptKeystoreRepository;

	/**
	 * KeymanagerDBHelper instance to handle all DB operations
	 */
	@Autowired
	private KeymanagerDBHelper dbHelper;
	
	@Autowired
	private KeyStoreRepository keyStoreRepository;

	/**
	 * Keystore instance to handles and store cryptographic keys.
	 */
	@Autowired
	private KeyStore keyStore;

	/**
	 * Utility to generate Metadata
	 */
	@Autowired
	KeymanagerUtil keymanagerUtil;

	/** The key manager. */
	@Autowired
	private KeymanagerService keyManagerService;

	/**
	 * {@link CryptomanagerUtils} instance
	 */
	@Autowired
	CryptomanagerUtils cryptomanagerUtil;


	@Autowired
	private CryptoCoreSpec<byte[], byte[], SecretKey, PublicKey, PrivateKey, String> cryptoCore;

	@Override
	public void afterPropertiesSet() throws Exception {
		// temporary fix to resolve issue occurring for first time(softhsm)/third time(real hsm) symmetric key retrival from HSM.
		for (int i = 0; i < 3; i++) {
			try {
				LOGGER.info(ZKCryptoManagerConstants.SESSIONID, ZKCryptoManagerConstants.ZK_ENCRYPT, 
						ZKCryptoManagerConstants.EMPTY, "Temporary solution to handle the first time decryption failure.");
				getDecryptedRandomKey("Tk8tU0VDRVJULUFWQUlMQUJMRS1URU1QLUZJWElORy0=");
			} catch(Throwable e) {
				// ignore
			}
		}
	}
    
    @Override
    public ZKCryptoResponseDto zkEncrypt(ZKCryptoRequestDto cryptoRequestDto) {
		LOGGER.info(ZKCryptoManagerConstants.SESSIONID, ZKCryptoManagerConstants.ZK_ENCRYPT, 
						ZKCryptoManagerConstants.EMPTY, "Zero Knowledge Encryption.");
		String id = cryptoRequestDto.getId();
		Stream<CryptoDataDto> cryptoDataList = cryptoRequestDto.getZkDataAttributes().stream();
		int randomKeyIndex = getRandomKeyIndex();
		String encryptedKeyData = dataEncryptKeystoreRepository.findKeyById(randomKeyIndex);
		Key secretRandomKey = getDecryptedRandomKey(encryptedKeyData);
		Key derivedKey = getDerivedKey(id, secretRandomKey);

		SecureRandom sRandom = new SecureRandom();
		List<CryptoDataDto> responseCryptoData = new ArrayList<>();
		cryptoDataList.forEach(reqCryptoData -> {
			String identifier = reqCryptoData.getIdentifier();
			byte[] dataToEncrypt = reqCryptoData.getValue().getBytes();
			byte[] nonce = new byte[ZKCryptoManagerConstants.GCM_NONCE_LENGTH];
			byte[] aad = new byte[ZKCryptoManagerConstants.GCM_AAD_LENGTH];

			sRandom.nextBytes(nonce);
			sRandom.nextBytes(aad);

			byte[] encryptedData = doCipherOps(derivedKey, dataToEncrypt, Cipher.ENCRYPT_MODE, nonce, aad);
			byte[] dbIndexBytes = getIndexBytes(randomKeyIndex);
			responseCryptoData.add(getResponseCryptoData(encryptedData, dbIndexBytes, nonce, aad, identifier));
		});
		ZKCryptoResponseDto cryptoResponseDto = new ZKCryptoResponseDto();
		cryptoResponseDto.setRankomKeyIndex(Integer.toString(randomKeyIndex));
		cryptoResponseDto.setZkDataAttributes(responseCryptoData);
		cryptoResponseDto.setEncryptedRandomKey(encryptRandomKey(secretRandomKey));
		keymanagerUtil.destoryKey((SecretKey) secretRandomKey);
		return cryptoResponseDto;
	}

    @Override
    public ZKCryptoResponseDto zkDecrypt(ZKCryptoRequestDto cryptoRequestDto) {
		LOGGER.info(ZKCryptoManagerConstants.SESSIONID, ZKCryptoManagerConstants.ZK_DECRYPT, 
						ZKCryptoManagerConstants.EMPTY, "Zero Knowledge Decryption.");
		String id = cryptoRequestDto.getId();
		Stream<CryptoDataDto> cryptoDataList = cryptoRequestDto.getZkDataAttributes().stream();

		List<CryptoDataDto> responseCryptoData = new ArrayList<>();
		cryptoDataList.forEach(reqCryptoData -> {
			String identifier = reqCryptoData.getIdentifier();
			String dataToDecrypt = reqCryptoData.getValue();

			byte[] decodedData = CryptoUtil.decodeBase64(dataToDecrypt);
			byte[] dbIndexBytes = Arrays.copyOfRange(decodedData, 0, ZKCryptoManagerConstants.INT_BYTES_LEN);
			byte[] nonce = Arrays.copyOfRange(decodedData, ZKCryptoManagerConstants.INT_BYTES_LEN, 
														   ZKCryptoManagerConstants.GCM_NONCE_PLUS_INT_BYTES_LEN);
			byte[] aad = Arrays.copyOfRange(decodedData, ZKCryptoManagerConstants.GCM_NONCE_PLUS_INT_BYTES_LEN,
								ZKCryptoManagerConstants.GCM_NONCE_PLUS_INT_BYTES_PLUS_GCM_AAD_LEN);
			byte[] encryptedData = Arrays.copyOfRange(decodedData, ZKCryptoManagerConstants.GCM_NONCE_PLUS_INT_BYTES_PLUS_GCM_AAD_LEN,
															decodedData.length);
			
			int randomKeyIndex = getIndexInt(dbIndexBytes);
			String encryptedKeyData = dataEncryptKeystoreRepository.findKeyById(randomKeyIndex);
			Key secretRandomKey = getDecryptedRandomKey(encryptedKeyData);
			Key derivedKey = getDerivedKey(id, secretRandomKey);
			byte[] decryptedData = doCipherOps(derivedKey, encryptedData, Cipher.DECRYPT_MODE, nonce, aad);
			responseCryptoData.add(getResponseCryptoData(decryptedData, identifier));
			keymanagerUtil.destoryKey((SecretKey) secretRandomKey);
		});
		ZKCryptoResponseDto cryptoResponseDto = new ZKCryptoResponseDto();
		cryptoResponseDto.setZkDataAttributes(responseCryptoData);		
		return cryptoResponseDto;
	}
	
	private int getRandomKeyIndex() {
		List<Integer> indexes = dataEncryptKeystoreRepository.getIdsByKeyStatus(ZKCryptoManagerConstants.ACTIVE_STATUS);
		int randomNum = ThreadLocalRandom.current().nextInt(0, indexes.size() + 1);
		return indexes.get(randomNum);
	}

	private int getIndexInt(byte[] indexBytes) {
		ByteBuffer bBuff = ByteBuffer.wrap(indexBytes);
		return bBuff.getInt();
	}
	
	private Key getDecryptedRandomKey(String encryptedKey) {
		LOGGER.info(ZKCryptoManagerConstants.SESSIONID, ZKCryptoManagerConstants.RANDOM_KEY, 
					ZKCryptoManagerConstants.RANDOM_KEY, "Random Key Decryption.");
		byte[] unwrappedKey = doFinal(encryptedKey, Cipher.DECRYPT_MODE);
		return new SecretKeySpec(unwrappedKey, 0, unwrappedKey.length, "AES");
		
	}

	private String getEncryptedRandomKey(String randomKey) {
		LOGGER.info(ZKCryptoManagerConstants.SESSIONID, ZKCryptoManagerConstants.RANDOM_KEY, 
						ZKCryptoManagerConstants.RANDOM_KEY, "Random Key Encryption.");
		byte[] wrappedKey = doFinal(randomKey, Cipher.ENCRYPT_MODE);
		return Base64.getEncoder().encodeToString(wrappedKey);
	}

	private byte[] doFinal(String secretData, int mode) {
		try {
			Cipher cipher = Cipher.getInstance(aesECBTransformation);

			byte[] secretDataBytes = Base64.getDecoder().decode(secretData);
			cipher.init(mode, getMasterKeyFromHSM());
			return cipher.doFinal(secretDataBytes, 0, secretDataBytes.length);
		} catch(NoSuchAlgorithmException | InvalidKeyException | NoSuchPaddingException
			| IllegalBlockSizeException | BadPaddingException | IllegalArgumentException e) {
			LOGGER.error(ZKCryptoManagerConstants.SESSIONID, ZKCryptoManagerConstants.RANDOM_KEY, 
						ZKCryptoManagerConstants.EMPTY,	"Error Cipher Operations of Random Key.");
			throw new ZKKeyDerivationException(ZKCryptoErrorConstants.RANDOM_KEY_CIPHER_FAILED.getErrorCode(), 
				ZKCryptoErrorConstants.RANDOM_KEY_CIPHER_FAILED.getErrorMessage(), e);
		}
	}

	private Key getDerivedKey(String id, Key key) {
		try {
			LOGGER.info(ZKCryptoManagerConstants.SESSIONID, ZKCryptoManagerConstants.DERIVE_KEY, 
						ZKCryptoManagerConstants.DERIVE_KEY, "Derive key with Random Key.");
			byte[] idBytes = id.getBytes();
			MessageDigest mDigest = MessageDigest.getInstance(ZKCryptoManagerConstants.HASH_ALGO);
			mDigest.update(idBytes, 0, idBytes.length);
			byte[] hashBytes = mDigest.digest();
			
			Cipher cipher = Cipher.getInstance(aesECBTransformation);
			cipher.init(Cipher.ENCRYPT_MODE, key);
			byte[] encryptedData = cipher.doFinal(hashBytes, 0, hashBytes.length);
			return new SecretKeySpec(encryptedData, 0, encryptedData.length, "AES");
		} catch(NoSuchAlgorithmException | InvalidKeyException | NoSuchPaddingException |
							 IllegalBlockSizeException | BadPaddingException e) {
			LOGGER.error(ZKCryptoManagerConstants.SESSIONID, ZKCryptoManagerConstants.DERIVE_KEY, 
							ZKCryptoManagerConstants.EMPTY,	"Error Deriving Key with Random Key." + e.getMessage());
			throw new ZKRandomKeyDecryptionException(ZKCryptoErrorConstants.KEY_DERIVATION_ERROR.getErrorCode(), 
					ZKCryptoErrorConstants.KEY_DERIVATION_ERROR.getErrorMessage());
		}
	}

	private Key getMasterKeyFromHSM() {
		LOGGER.info(ZKCryptoManagerConstants.SESSIONID, ZKCryptoManagerConstants.MASTER_KEY, 
						ZKCryptoManagerConstants.RANDOM_KEY, "Retrieve Master Key from HSM.");
		String keyAlias = getKeyAlias(masterKeyAppId, masterKeyRefId);
		if (Objects.nonNull(keyAlias)) {
			return keyStore.getSymmetricKey(keyAlias);
		}
		
		LOGGER.error(ZKCryptoManagerConstants.SESSIONID, ZKCryptoManagerConstants.MASTER_KEY,
						ZKCryptoManagerConstants.MASTER_KEY, "No Key Alias found.");
		throw new NoUniqueAliasException(ZKCryptoErrorConstants.NO_UNIQUE_ALIAS.getErrorCode(),
						ZKCryptoErrorConstants.NO_UNIQUE_ALIAS.getErrorMessage());
	}

	private String getKeyAlias(String keyAppId, String keyRefId) {
		LOGGER.info(ZKCryptoManagerConstants.SESSIONID, ZKCryptoManagerConstants.MASTER_KEY, 
						ZKCryptoManagerConstants.RANDOM_KEY, "Retrieve Master Key Alias from DB.");

		Map<String, List<KeyAlias>> keyAliasMap = dbHelper.getKeyAliases(keyAppId, keyRefId, DateUtils.getUTCCurrentDateTime());
		
		List<KeyAlias> currentKeyAliases = keyAliasMap.get(KeymanagerConstant.CURRENTKEYALIAS);

		if (!currentKeyAliases.isEmpty() && currentKeyAliases.size() == 1) {
			LOGGER.info(ZKCryptoManagerConstants.SESSIONID, ZKCryptoManagerConstants.MASTER_CURRENT_ALIAS, "getKeyAlias",
					"CurrentKeyAlias size is one. Will decrypt random symmetric key for this alias");
			return currentKeyAliases.get(0).getAlias();
		}

		LOGGER.error(ZKCryptoManagerConstants.SESSIONID, ZKCryptoManagerConstants.MASTER_KEY, 
					ZKCryptoManagerConstants.RANDOM_KEY, "CurrentKeyAlias is not unique. KeyAlias count: " + currentKeyAliases.size());
		throw new NoUniqueAliasException(ZKCryptoErrorConstants.NO_UNIQUE_ALIAS.getErrorCode(),
						ZKCryptoErrorConstants.NO_UNIQUE_ALIAS.getErrorMessage());
	}

	private byte[] doCipherOps(Key key, byte[] data, int mode, byte[] nonce, byte[] aad) {
		LOGGER.info(ZKCryptoManagerConstants.SESSIONID, ZKCryptoManagerConstants.DATA_CIPHER, 
						ZKCryptoManagerConstants.EMPTY, "Data Encryption/Decryption Process");
		try {
			Cipher cipher = Cipher.getInstance(aesGCMTransformation);
			GCMParameterSpec gcmSpec = new GCMParameterSpec(ZKCryptoManagerConstants.GCM_TAG_LENGTH * 8, nonce);
			cipher.init(mode, key, gcmSpec);
			cipher.updateAAD(aad);
			return cipher.doFinal(data, 0, data.length);
		} catch(NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException |
			InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException ex) {
			LOGGER.error(ZKCryptoManagerConstants.SESSIONID, ZKCryptoManagerConstants.DATA_CIPHER, 
					ZKCryptoManagerConstants.DATA_CIPHER, "Error Ciphering inputed data." + ex.getMessage());
			throw new ZKCryptoException(ZKCryptoErrorConstants.DATA_CIPHER_OPS_ERROR.getErrorCode(),
						ZKCryptoErrorConstants.DATA_CIPHER_OPS_ERROR.getErrorMessage());
		}
	}

	private byte[] getIndexBytes(int randomIndex) {
		ByteBuffer byteBuff = ByteBuffer.allocate(ZKCryptoManagerConstants.INT_BYTES_LEN);
		byteBuff.putInt(randomIndex);
		return byteBuff.array();
	}

	private CryptoDataDto getResponseCryptoData(byte[] encryptedData, byte[] dbIndexBytes, byte[] nonce, byte[] aad, String identifier) {
		byte[] finalEncData = new byte[encryptedData.length + dbIndexBytes.length + ZKCryptoManagerConstants.GCM_AAD_LENGTH
											+ ZKCryptoManagerConstants.GCM_NONCE_LENGTH];
		System.arraycopy(dbIndexBytes, 0, finalEncData, 0, dbIndexBytes.length);
		System.arraycopy(nonce, 0, finalEncData, dbIndexBytes.length, nonce.length);
		System.arraycopy(aad, 0, finalEncData, dbIndexBytes.length + nonce.length, aad.length);
		System.arraycopy(encryptedData, 0, finalEncData, dbIndexBytes.length + nonce.length + aad.length,
				encryptedData.length);
		String concatEncryptedData = CryptoUtil.encodeBase64(finalEncData);
		CryptoDataDto resCryptoData = new CryptoDataDto();
		resCryptoData.setIdentifier(identifier);
		resCryptoData.setValue(concatEncryptedData);
		return resCryptoData;
	}

	private CryptoDataDto getResponseCryptoData(byte[] decryptedData, String identifier) {
		
		String decryptedDataStr = new String(decryptedData);
		CryptoDataDto resCryptoData = new CryptoDataDto();
		resCryptoData.setIdentifier(identifier);
		resCryptoData.setValue(decryptedDataStr);
		return resCryptoData;
	}

	private String encryptRandomKey(Key secretRandomKey) {
		LOGGER.info(ZKCryptoManagerConstants.SESSIONID, ZKCryptoManagerConstants.ENCRYPT_RANDOM_KEY, 
						ZKCryptoManagerConstants.EMPTY, "Encrypting Random Key with Public Key.");
		
		String keyAlias = getKeyAlias(pubKeyApplicationId, pubKeyReferenceId);
		Optional<io.mosip.kernel.keymanagerservice.entity.KeyStore> dbKeyStore = keyStoreRepository.findByAlias(keyAlias);
		if (!dbKeyStore.isPresent()) {
			LOGGER.info(ZKCryptoManagerConstants.SESSIONID, ZKCryptoManagerConstants.ENCRYPT_RANDOM_KEY, 
						ZKCryptoManagerConstants.ENCRYPT_RANDOM_KEY, "Key in DBStore does not exist for this alias. Throwing exception");
			throw new NoUniqueAliasException(ZKCryptoErrorConstants.NO_UNIQUE_ALIAS.getErrorCode(),
							ZKCryptoErrorConstants.NO_UNIQUE_ALIAS.getErrorMessage());
		}
		String certificateData = dbKeyStore.get().getCertificateData();
		X509Certificate x509Cert = (X509Certificate) keymanagerUtil.convertToCertificate(certificateData);
		PublicKey publicKey = x509Cert.getPublicKey();
		byte[] encryptedRandomKey = cryptoCore.asymmetricEncrypt(publicKey, secretRandomKey.getEncoded());
		byte[] certThumbprint = cryptomanagerUtil.getCertificateThumbprint(x509Cert);
		byte[] concatedData = cryptomanagerUtil.concatCertThumbprint(certThumbprint, encryptedRandomKey);
		return CryptoUtil.encodeBase64(concatedData);
	}

	@Override
	public ReEncryptRandomKeyResponseDto zkReEncryptRandomKey(String encryptedKey){
		LOGGER.info(ZKCryptoManagerConstants.SESSIONID, ZKCryptoManagerConstants.RE_ENCRYPT_RANDOM_KEY, 
						ZKCryptoManagerConstants.EMPTY, "Re-Encrypt Random Key.");
		if (encryptedKey == null || encryptedKey.trim().isEmpty()) {
			LOGGER.error(ZKCryptoManagerConstants.SESSIONID, ZKCryptoManagerConstants.RE_ENCRYPT_RANDOM_KEY, 
					ZKCryptoManagerConstants.RE_ENCRYPT_RANDOM_KEY, "Invalid Encrypted Key input.");
			throw new ZKCryptoException(ZKCryptoErrorConstants.INVALID_ENCRYPTED_RANDOM_KEY.getErrorCode(),
						ZKCryptoErrorConstants.INVALID_ENCRYPTED_RANDOM_KEY.getErrorMessage());
		}
		LocalDateTime localDateTimeStamp = DateUtils.getUTCCurrentDateTime();
		SymmetricKeyRequestDto symmetricKeyRequestDto = new SymmetricKeyRequestDto(
									pubKeyApplicationId, localDateTimeStamp, pubKeyReferenceId, encryptedKey, true);
		String randomKey = keyManagerService.decryptSymmetricKey(symmetricKeyRequestDto).getSymmetricKey();
		String encryptedRandomKey = getEncryptedRandomKey(Base64.getEncoder().encodeToString(CryptoUtil.decodeBase64(randomKey)));
		ReEncryptRandomKeyResponseDto responseDto = new ReEncryptRandomKeyResponseDto();
		responseDto.setEncryptedKey(encryptedRandomKey);
		return responseDto;
	}
}