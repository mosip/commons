package io.mosip.kernel.keymigrate.service.impl;

import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.mosip.kernel.core.crypto.exception.InvalidDataException;
import io.mosip.kernel.core.crypto.spi.CryptoCoreSpec;
import io.mosip.kernel.core.keymanager.model.CertificateParameters;
import io.mosip.kernel.core.keymanager.spi.KeyStore;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.core.util.CryptoUtil;
import io.mosip.kernel.core.util.DateUtils;
import io.mosip.kernel.keymanagerservice.constant.KeymanagerConstant;
import io.mosip.kernel.keymanagerservice.constant.KeymanagerErrorConstant;
import io.mosip.kernel.keymanagerservice.entity.KeyAlias;
import io.mosip.kernel.keymanagerservice.exception.NoUniqueAliasException;
import io.mosip.kernel.keymanagerservice.helper.KeymanagerDBHelper;
import io.mosip.kernel.keymanagerservice.logger.KeymanagerLogger;
import io.mosip.kernel.keymanagerservice.util.KeymanagerUtil;
import io.mosip.kernel.keymigrate.constant.KeyMigratorConstants;
import io.mosip.kernel.keymigrate.dto.KeyMigrateBaseKeyRequestDto;
import io.mosip.kernel.keymigrate.dto.KeyMigrateBaseKeyResponseDto;
import io.mosip.kernel.keymigrate.dto.ZKKeyDataDto;
import io.mosip.kernel.keymigrate.dto.ZKKeyMigrateCertficateResponseDto;
import io.mosip.kernel.keymigrate.dto.ZKKeyMigrateRequestDto;
import io.mosip.kernel.keymigrate.dto.ZKKeyMigrateResponseDto;
import io.mosip.kernel.keymigrate.dto.ZKKeyResponseDto;
import io.mosip.kernel.keymigrate.service.spi.KeyMigratorService;
import io.mosip.kernel.keymanagerservice.entity.DataEncryptKeystore;
import io.mosip.kernel.keymanagerservice.repository.DataEncryptKeystoreRepository;


/**
 * Service Implementation for {@link KeyMigratorService} interface
 * 
 * @author Mahammed Taheer
 *
 * @since 1.1.6
 */
@Lazy
@Service
@Transactional
public class KeyMigratorServiceImpl implements KeyMigratorService {

	private static final Logger LOGGER = KeymanagerLogger.getLogger(KeyMigratorServiceImpl.class);
    
    private static final String CREATED_BY = "System-Migrator";
    
    @Value("${mosip.kernel.partner.sign.masterkey.application.id:PMS}")
	private String pmsSignAppId;
    
    @Value("${mosip.kernel.certificate.sign.algorithm:SHA256withRSA}")
	private String signAlgorithm;

    @Value("${mosip.kernel.zkcrypto.masterkey.application.id:KERNEL}")
	private String masterKeyAppId;

	@Value("${mosip.kernel.zkcrypto.masterkey.reference.id:IDENTITY_CACHE}")
    private String masterKeyRefId;

    @Value("${mosip.kernel.zkcrypto.wrap.algorithm-name:AES/ECB/NoPadding}")
	private String aesECBTransformation;

    /**
	 * KeymanagerDBHelper instance to handle all DB operations
	 */
	@Autowired
	private KeymanagerDBHelper dbHelper;

    @Autowired
	KeymanagerUtil keymanagerUtil;

    /**
	 * Keystore instance to handles and store cryptographic keys.
	 */
	@Autowired
	private KeyStore keyStore;

    /**
	 * {@link CryptoCoreSpec} instance for cryptographic functionalities.
	 */
	@Autowired
	private CryptoCoreSpec<byte[], byte[], SecretKey, PublicKey, PrivateKey, String> cryptoCore;

    @Autowired
    DataEncryptKeystoreRepository dataEncryptKeystoreRepository;

    @Override
    public KeyMigrateBaseKeyResponseDto migrateBaseKey(KeyMigrateBaseKeyRequestDto baseKeyMigrateRequest){
        LOGGER.info(KeyMigratorConstants.SESSIONID, KeyMigratorConstants.BASE_KEY, 
                            KeyMigratorConstants.EMPTY, "Base Key Migration Migration.");

        String appId = baseKeyMigrateRequest.getApplicationId();
        String refId = baseKeyMigrateRequest.getReferenceId();
        String encryptedPrivateKey = baseKeyMigrateRequest.getEncryptedKeyData();
        String certificateData = baseKeyMigrateRequest.getCertificateData();
        LocalDateTime notBefore = baseKeyMigrateRequest.getNotBefore();
        LocalDateTime notAfter = baseKeyMigrateRequest.getNotAfter(); 
        LocalDateTime localDateTimeStamp = DateUtils.getUTCCurrentDateTime();

        Map<String, List<KeyAlias>> keyAliasMap = dbHelper.getKeyAliases(appId, KeyMigratorConstants.EMPTY, localDateTimeStamp);
		List<KeyAlias> currentKeyAlias = keyAliasMap.get(KeymanagerConstant.CURRENTKEYALIAS);

        if (currentKeyAlias.isEmpty() && !appId.equals(KeyMigratorConstants.PARTNER_APPID)) {
			LOGGER.error(KeyMigratorConstants.SESSIONID, KeymanagerConstant.CURRENTKEYALIAS,
					String.valueOf(currentKeyAlias.size()), "No CurrentKeyAlias found Throwing exception");
			throw new NoUniqueAliasException(KeymanagerErrorConstant.NO_UNIQUE_ALIAS.getErrorCode(),
					KeymanagerErrorConstant.NO_UNIQUE_ALIAS.getErrorMessage());
		}

        if (isValidKeyExists(appId, refId, notBefore, notAfter, localDateTimeStamp)) {
            LOGGER.error(KeyMigratorConstants.SESSIONID, KeyMigratorConstants.EMPTY,
                            KeyMigratorConstants.EMPTY, "Valid Key Already exists, not allowed to migrate.");
            KeyMigrateBaseKeyResponseDto responseDto = new KeyMigrateBaseKeyResponseDto();
            responseDto.setStatus(KeyMigratorConstants.MIGRAION_NOT_ALLOWED);
            responseDto.setTimestamp(localDateTimeStamp);
            return responseDto;
        }
        String baseKeyAlias = UUID.randomUUID().toString();
        String masterKeyAlias = currentKeyAlias.isEmpty() ? baseKeyAlias : currentKeyAlias.get(0).getAlias();

        // Re-Signing any base key Certificate is not possible because thumbprint will not match with the prepended thumbprint in encrypted data.
        // Re-signing of partner certificate is not required because existing trust certificates (MOSIP_ROOT & PMS) from old KM might 
        // have got synced with other components performing trust validation. New KM certificates (MOSIP_ROOT & PMS) will get synced
        // with other components and both will be used to validate the trust.
        //String reSignedCert = reSignCertificate(appId, masterKeyAlias, certificateData, localDateTimeStamp, notBefore, notAfter);
        dbHelper.storeKeyInDBStore(baseKeyAlias, masterKeyAlias, certificateData, encryptedPrivateKey);
		dbHelper.storeKeyInAlias(appId, notBefore, refId, baseKeyAlias, notAfter);

        LOGGER.info(KeyMigratorConstants.SESSIONID, KeyMigratorConstants.BASE_KEY, 
                            KeyMigratorConstants.EMPTY, "Migration Completed for App Id:" + appId + ", Ref Id: " + refId 
                                + ", Inserted UUID: " + baseKeyAlias);

        KeyMigrateBaseKeyResponseDto responseDto = new KeyMigrateBaseKeyResponseDto();
        responseDto.setStatus(KeyMigratorConstants.MIGRAION_SUCCESS);
        responseDto.setTimestamp(localDateTimeStamp);
        return responseDto;
    }

    private boolean isValidKeyExists(String applicationId, String referenceId, LocalDateTime notBefore, 
                    LocalDateTime notAfter, LocalDateTime localDateTimeStamp) {
        Map<String, List<KeyAlias>> keyAliasMap = dbHelper.getKeyAliases(applicationId, referenceId, localDateTimeStamp);
        List<KeyAlias> currentKeyAlias = keyAliasMap.get(KeymanagerConstant.CURRENTKEYALIAS);
        // Current key alias is empty, no need to check whether migrated key is valid or expired. Simply migrate it.
        if (currentKeyAlias.isEmpty()) {
            return false;
        }

        // Current key alias is not empty, check whether migrated key is also valid. 
        // Both valid, do not allowed to migrate the key. 
        if (localDateTimeStamp.isEqual(notBefore) || localDateTimeStamp.isEqual(notAfter)
				|| (localDateTimeStamp.isAfter(notBefore) && localDateTimeStamp.isBefore(notAfter))) {
            return true;
        }
        return false;
    }

    /* private String reSignCertificate(String appId, String masterKeyAlias, String oldCertData, LocalDateTime localDateTimeStamp, 
                    LocalDateTime notBefore, LocalDateTime notAfter) {

        String keyAlias = masterKeyAlias;
        if (appId.equals(KeyMigratorConstants.PARTNER_APPID)){
            Map<String, List<KeyAlias>> keyAliasMap = dbHelper.getKeyAliases(pmsSignAppId, KeyMigratorConstants.EMPTY, localDateTimeStamp);
		    List<KeyAlias> currentKeyAlias = keyAliasMap.get(KeymanagerConstant.CURRENTKEYALIAS);
            if (currentKeyAlias.isEmpty()) {
                LOGGER.info(KeyMigratorConstants.SESSIONID, KeymanagerConstant.CURRENTKEYALIAS,
                        String.valueOf(currentKeyAlias.size()), "No CurrentKeyAlias found for PMS Sign Key. Throwing exception");
                throw new NoUniqueAliasException(KeymanagerErrorConstant.NO_UNIQUE_ALIAS.getErrorCode(),
                        KeymanagerErrorConstant.NO_UNIQUE_ALIAS.getErrorMessage());
            }
            keyAlias = currentKeyAlias.get(0).getAlias();
        }
        PrivateKeyEntry masterKeyEntry = keyStore.getAsymmetricKey(keyAlias);
        PrivateKey masterPrivateKey = masterKeyEntry.getPrivateKey();
        X509Certificate signerCert = masterKeyEntry.getCertificate();
        X500Principal signerPrincipal = signerCert.getSubjectX500Principal();

        X509Certificate oldCert = (X509Certificate) keymanagerUtil.convertToCertificate(oldCertData);
        X500Principal oldCertPrincipal = oldCert.getSubjectX500Principal();
        CertificateParameters certParams = keymanagerUtil.getCertificateParameters(oldCertPrincipal,
                                                    notBefore, notAfter);

        X509Certificate x509Cert = (X509Certificate) CertificateUtility.generateX509Certificate(masterPrivateKey, oldCert.getPublicKey(), 
                    certParams, signerPrincipal, signAlgorithm, keyStore.getKeystoreProviderName());
        return keymanagerUtil.getPEMFormatedData(x509Cert);
    } */

    @Override
    public ZKKeyMigrateCertficateResponseDto getZKTempCertificate() {

        LOGGER.info(KeyMigratorConstants.SESSIONID, KeyMigratorConstants.ZK_KEYS, 
                            KeyMigratorConstants.EMPTY, "Get Temporary Certificate for ZK keys migration.");

        LocalDateTime localDateTimeStamp = DateUtils.getUTCCurrentDateTime();

        Map<String, List<KeyAlias>> keyAliasMap = dbHelper.getKeyAliases(KeyMigratorConstants.ZK_TEMP_KEY_APP_ID, 
                        KeyMigratorConstants.ZK_TEMP_KEY_REF_ID, localDateTimeStamp);
        List<KeyAlias> KeyAlias = keyAliasMap.get(KeymanagerConstant.KEYALIAS);
        List<KeyAlias> currentKeyAlias = keyAliasMap.get(KeymanagerConstant.CURRENTKEYALIAS);

        ZKKeyMigrateCertficateResponseDto responseDto = new ZKKeyMigrateCertficateResponseDto();
        if (currentKeyAlias.isEmpty() && KeyAlias.size() > 0) {
            LOGGER.info(KeyMigratorConstants.SESSIONID, KeyMigratorConstants.ZK_KEYS,
                    String.valueOf(KeyAlias.size()), "Key Exists but expired, so removing the key and generating new key.");
            String alias = KeyAlias.get((KeyAlias.size() -1)).getAlias();
            LOGGER.info(KeyMigratorConstants.SESSIONID, KeyMigratorConstants.ZK_KEYS,
                    KeyMigratorConstants.EMPTY, "Found Alias to delete key. Alias: " + alias);
            keyStore.deleteKey(alias);
		    dbHelper.storeKeyInAlias(KeyMigratorConstants.ZK_TEMP_KEY_APP_ID, localDateTimeStamp, KeyMigratorConstants.ZK_TEMP_KEY_REF_ID, 
                    alias, localDateTimeStamp);
        } else if (currentKeyAlias.size() == 1) {
            LOGGER.info(KeyMigratorConstants.SESSIONID, KeyMigratorConstants.ZK_KEYS,
                    String.valueOf(currentKeyAlias.size()), "currentKeyAlias size is one, returning the certificate.");
            String alias = currentKeyAlias.get(0).getAlias();
            String certificateData = keymanagerUtil.getPEMFormatedData(keyStore.getCertificate(alias));
            responseDto.setCertificate(certificateData);
            responseDto.setTimestamp(localDateTimeStamp);
            return responseDto;
        } else if (currentKeyAlias.size() > 1) {
            LOGGER.error(KeyMigratorConstants.SESSIONID, KeyMigratorConstants.ZK_KEYS,
					String.valueOf(currentKeyAlias.size()), "No CurrentKeyAlias found Throwing exception");
			throw new NoUniqueAliasException(KeymanagerErrorConstant.NO_UNIQUE_ALIAS.getErrorCode(),
					KeymanagerErrorConstant.NO_UNIQUE_ALIAS.getErrorMessage());
        }

        String alias = UUID.randomUUID().toString();
		LocalDateTime expiryDateTime = localDateTimeStamp.plusDays(1);
		CertificateParameters certParams = keymanagerUtil.getCertificateParameters(KeyMigratorConstants.ZK_CERT_COMMON_NAME, 
                                localDateTimeStamp, expiryDateTime);
		keyStore.generateAndStoreAsymmetricKey(alias, null, certParams);
		dbHelper.storeKeyInAlias(KeyMigratorConstants.ZK_TEMP_KEY_APP_ID, localDateTimeStamp, KeyMigratorConstants.ZK_TEMP_KEY_REF_ID, 
                                alias, expiryDateTime);

        String certificateData = keymanagerUtil.getPEMFormatedData(keyStore.getCertificate(alias));
        responseDto.setCertificate(certificateData);
        responseDto.setTimestamp(localDateTimeStamp);
        return responseDto;
    }

    @Override
    public ZKKeyMigrateResponseDto migrateZKKeys(ZKKeyMigrateRequestDto migrateZKKeysRequestDto) {
        LOGGER.info(KeyMigratorConstants.SESSIONID, KeyMigratorConstants.ZK_KEYS, 
                            KeyMigratorConstants.EMPTY, "ZK keys migration request.");

        LocalDateTime localDateTimeStamp = DateUtils.getUTCCurrentDateTime();
        Stream<ZKKeyDataDto> encryptedKeyList = migrateZKKeysRequestDto.getZkEncryptedDataList().stream();
        boolean purgeKeyFlag = migrateZKKeysRequestDto.getPurgeTempKeyFlag() == null ? false: migrateZKKeysRequestDto.getPurgeTempKeyFlag();

        LOGGER.info(KeyMigratorConstants.SESSIONID, KeyMigratorConstants.ZK_KEYS, 
                            KeyMigratorConstants.EMPTY, "ZK migration keys list size: " + migrateZKKeysRequestDto.getZkEncryptedDataList().size());

        String tempKeyAlias = getKeyAlias(KeyMigratorConstants.ZK_TEMP_KEY_APP_ID, KeyMigratorConstants.ZK_TEMP_KEY_REF_ID, localDateTimeStamp);
        String zkMasterKeyAlias = getKeyAlias(masterKeyAppId, masterKeyRefId, localDateTimeStamp);
        PrivateKeyEntry keyEntry= keyStore.getAsymmetricKey(tempKeyAlias);
        PrivateKey tempPrivateKey = keyEntry.getPrivateKey();
        PublicKey tempPublicKey = keyEntry.getCertificate().getPublicKey();
        Key zkMasterKey = keyStore.getSymmetricKey(zkMasterKeyAlias);

        List<ZKKeyResponseDto> keyResponseList = new ArrayList<>();
        encryptedKeyList.forEach(encryptedKey -> {
            byte[] encryptedKeyBytes = CryptoUtil.decodeBase64(encryptedKey.getEncryptedKeyData());
            int keyIndex = encryptedKey.getKeyIndex();
            ZKKeyResponseDto keyResponseDto = new ZKKeyResponseDto();
            keyResponseDto.setKeyIndex(keyIndex);
            if (!isKeyIndexExist(keyIndex)) {
                
                byte[] encryptedSessionKey = encryptRandomKey(encryptedKeyBytes, zkMasterKey, tempPrivateKey, tempPublicKey );
                if (encryptedSessionKey != null) {
                    String encodedKey = Base64.getEncoder().encodeToString(encryptedSessionKey);
                    insertKey(keyIndex, encodedKey, KeyMigratorConstants.ACTIVE_STATUS);
                    keyResponseDto.setStatusMessage(KeyMigratorConstants.MIGRAION_SUCCESS);
                } else {
                    keyResponseDto.setStatusMessage(KeyMigratorConstants.MIGRAION_FAILED);
                }
            } else {
                keyResponseDto.setStatusMessage(KeyMigratorConstants.MIGRAION_NOT_ALLOWED);
            }
            keyResponseList.add(keyResponseDto);
        });
        LOGGER.info(KeyMigratorConstants.SESSIONID, KeyMigratorConstants.ZK_KEYS, 
                            KeyMigratorConstants.EMPTY, "Purge Flag Value: " + purgeKeyFlag);
        if (purgeKeyFlag) {
            keyStore.deleteKey(tempKeyAlias);
            LOGGER.info(KeyMigratorConstants.SESSIONID, KeyMigratorConstants.ZK_KEYS, 
                            KeyMigratorConstants.EMPTY, "Key Purged from Store. Key Alias: " + tempKeyAlias);
            dbHelper.storeKeyInAlias(KeyMigratorConstants.ZK_TEMP_KEY_APP_ID, localDateTimeStamp, KeyMigratorConstants.ZK_TEMP_KEY_REF_ID, 
                    tempKeyAlias, localDateTimeStamp);
        }
        ZKKeyMigrateResponseDto responseDto = new ZKKeyMigrateResponseDto();
        responseDto.setZkEncryptedDataList(keyResponseList);
        return responseDto;
    }

    private String getKeyAlias(String keyAppId, String keyRefId, LocalDateTime localDateTimeStamp) {
		LOGGER.info(KeyMigratorConstants.SESSIONID, KeyMigratorConstants.ZK_KEYS, 
                KeyMigratorConstants.EMPTY, "Retrieve Master Key Alias from DB. AppId: " + keyAppId);

		Map<String, List<KeyAlias>> keyAliasMap = dbHelper.getKeyAliases(keyAppId, keyRefId, localDateTimeStamp);
		
		List<KeyAlias> currentKeyAliases = keyAliasMap.get(KeymanagerConstant.CURRENTKEYALIAS);

		if (!currentKeyAliases.isEmpty() && currentKeyAliases.size() == 1) {
			LOGGER.info(KeyMigratorConstants.SESSIONID, KeyMigratorConstants.ZK_KEYS, "getKeyAlias",
					"CurrentKeyAlias size is one. return the current key alias.");
			return currentKeyAliases.get(0).getAlias();
		}

		LOGGER.error(KeyMigratorConstants.SESSIONID, KeyMigratorConstants.ZK_KEYS, 
                    KeyMigratorConstants.EMPTY, "CurrentKeyAlias is not unique. KeyAlias count: " + currentKeyAliases.size());
		throw new NoUniqueAliasException(KeymanagerErrorConstant.NO_UNIQUE_ALIAS.getErrorCode(),
                KeymanagerErrorConstant.NO_UNIQUE_ALIAS.getErrorMessage());
	}

    private byte[] encryptRandomKey(byte[] encryptedKeyBytes, Key zkMasterKey, PrivateKey tempPrivateKey, PublicKey tempPublicKey) {
		try {
            byte[] secretDataBytes = cryptoCore.asymmetricDecrypt(tempPrivateKey, tempPublicKey, encryptedKeyBytes);
			Cipher cipher = Cipher.getInstance(aesECBTransformation);

			cipher.init(Cipher.ENCRYPT_MODE, zkMasterKey);
			return cipher.doFinal(secretDataBytes, 0, secretDataBytes.length);
		} catch(NoSuchAlgorithmException | InvalidKeyException | NoSuchPaddingException
			| IllegalBlockSizeException | BadPaddingException | IllegalArgumentException 
            | InvalidDataException | io.mosip.kernel.core.crypto.exception.InvalidKeyException e) {
			LOGGER.error(KeyMigratorConstants.SESSIONID, KeyMigratorConstants.ZK_KEYS, 
                        KeyMigratorConstants.EMPTY,	"Error in encrypting random Key in key migration process.", e);
		}
        return null;
	}

    private boolean isKeyIndexExist(int keyIdx) {
        return Objects.nonNull(dataEncryptKeystoreRepository.findKeyById(keyIdx));
    }

    private void insertKey(int id, String secretData, String status) {
		DataEncryptKeystore data = new DataEncryptKeystore();
		data.setId(id);
		data.setKey(secretData);
		data.setKeyStatus(status);
		data.setCrBy(CREATED_BY);
		data.setCrDTimes(LocalDateTime.now());
		dataEncryptKeystoreRepository.save(data);
	}
}
