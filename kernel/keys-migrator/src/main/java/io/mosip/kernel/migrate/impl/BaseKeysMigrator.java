package io.mosip.kernel.migrate.impl;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.net.HttpHeaders;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.core.crypto.spi.CryptoCoreSpec;
import io.mosip.kernel.core.http.RequestWrapper;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.keymanager.spi.KeyStore;
import io.mosip.kernel.core.util.CryptoUtil;
import io.mosip.kernel.cryptomanager.util.CryptomanagerUtils;
import io.mosip.kernel.keymanagerservice.constant.KeymanagerConstant;
import io.mosip.kernel.keymanagerservice.entity.DataEncryptKeystore;
import io.mosip.kernel.keymanagerservice.entity.KeyAlias;
import io.mosip.kernel.keymanagerservice.logger.KeymanagerLogger;
import io.mosip.kernel.keymanagerservice.repository.DataEncryptKeystoreRepository;
import io.mosip.kernel.keymanagerservice.repository.KeyAliasRepository;
import io.mosip.kernel.keymanagerservice.repository.KeyStoreRepository;
import io.mosip.kernel.keymanagerservice.service.KeymanagerService;
import io.mosip.kernel.keymanagerservice.util.KeymanagerUtil;
import io.mosip.kernel.keymigrate.dto.KeyMigrateBaseKeyRequestDto;
import io.mosip.kernel.keymigrate.dto.KeyMigrateBaseKeyResponseDto;
import io.mosip.kernel.keymigrate.dto.ZKKeyDataDto;
import io.mosip.kernel.keymigrate.dto.ZKKeyMigrateRequestDto;
import io.mosip.kernel.keymigrate.dto.ZKKeyMigrateResponseDto;
import io.mosip.kernel.keymigrate.dto.ZKKeyResponseDto;
/**
 * The Class BaseKeysMigrator.
 *
 * @author Mahammed Taheer
 */
@Component
public class BaseKeysMigrator {

    private static final Logger LOGGER = KeymanagerLogger.getLogger(BaseKeysMigrator.class);

    private static final String ROOT_APP_ID = "ROOT";

    private static final String BLANK_REF_ID = "";

    private static final String KERNEL_APP_ID = "KERNEL";

    private static final String IDENTITY_CACHE_REF_ID = "IDENTITY_CACHE";

    private static final String PARTNER_APP_ID = "PARTNER";

    @Value("${mosip.kernel.keymanager.autogen.appids.list}")
    private String appIdsList;

    @Value("${mosip.kernel.keymanager.keymigration.auth.url}")
	private String authTokenUrl;

	@Value("${mosip.kernel.keymanager.keymigration.auth.appId}")
	private String authAppId;

	@Value("${mosip.kernel.keymanager.keymigration.auth.cliendId}")
	private String clientId;

	@Value("${mosip.kernel.keymanager.keymigration.auth.secretKey}")
	private String secretKey;

    @Value("${mosip.kernel.keymanager.keymigration.getcertificate.url}")
	private String getCertifcateUrl;

    @Value("${mosip.kernel.keymanager.keymigration.uploadkey.url}")
	private String uploadKeyUrl;

    @Value("${mosip.kernel.keymanager.keymigration.getzktempcertificate.url}")
	private String getZKTempCertifcateUrl;

    @Value("${mosip.kernel.zkcrypto.wrap.algorithm-name}")
	private String aesECBTransformation;
    
    @Value("${mosip.kernel.keymanager.keymigration.zkkeys.migration.batch.size}")
    private int uploadBatchSize;

    @Value("${mosip.kernel.keymanager.keymigration.zkUploadkey.url}")
	private String zkUploadKeyUrl;

    @Autowired
	private ObjectMapper mapper;

    @Autowired
    private KeyAliasRepository keyAliasRepository;

    @Autowired
    KeymanagerService keymanagerService;

	@Autowired
	private RestTemplate restTemplate;

    /**
     * Keystore instance to handles and store cryptographic keys.
     */
    @Autowired
    private KeyStore keyStore;

    @Autowired
    KeymanagerUtil keymanagerUtil;
    
	/**
	 * {@link KeyStoreRepository} instance
	 */
	@Autowired
	KeyStoreRepository keyStoreRepository;

    @Autowired
    DataEncryptKeystoreRepository dataEncryptKeystoreRepository;

    /**
	 * {@link CryptomanagerUtils} instance
	 */
	@Autowired
	CryptomanagerUtils cryptomanagerUtil;


	@Autowired
	private CryptoCoreSpec<byte[], byte[], SecretKey, PublicKey, PrivateKey, String> cryptoCore;
    
    String token = "";

    @SuppressWarnings("rawtypes")
	@PostConstruct
	public void generateToken() {
		RequestWrapper<ObjectNode> requestWrapper = new RequestWrapper<>();
		ObjectNode request = mapper.createObjectNode();
		request.put("appId", authAppId);
		request.put("clientId", clientId);
		request.put("secretKey", secretKey);
		requestWrapper.setRequest(request);
		ResponseEntity<ResponseWrapper> response = restTemplate.postForEntity(authTokenUrl, requestWrapper,
				ResponseWrapper.class);
		token = response.getHeaders().getFirst("authorization");
		restTemplate.setInterceptors(Collections.singletonList(new ClientHttpRequestInterceptor() {

			@Override
			public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
					throws IOException {
				request.getHeaders().add(HttpHeaders.COOKIE, "Authorization=" + token);
				return execution.execute(request, body);
			}
		}));
	}

    public void migrateKeys() throws Exception {
        LOGGER.info("Starting Key Manager Generated Base Keys Migration...");
        migrateKeyMgrKeys();
        LOGGER.info("Completed Key Manager Generated Base Keys Migration...");

        LOGGER.info("Starting Partner uploaded Certificates Migration...");
        migratePartnerKeys();
        LOGGER.info("Completed Partner uploaded Certificates Migration...");

        LOGGER.info("Starting ZK Random Keys Migration...");
        migrateZKRandomKeys();
        LOGGER.info("Completed ZK Random Keys Migration...");
    }

    private void migrateKeyMgrKeys() {
        List<String> masterKeysList = getMasterKeysList();
        masterKeysList.forEach(masterKeyAppId -> {
            if (!masterKeyAppId.equals("KERNEL:IDENTITY_CACHE")) {
                LOGGER.info("Started Migration for AppId: " + masterKeyAppId);
                List<KeyAlias> masterKeyAlias = getKeyAlias(masterKeyAppId, BLANK_REF_ID);
                masterKeyAlias.forEach(masterKeyUuidObj -> {
                    String masterKeyUuid = masterKeyUuidObj.getAlias();
                    List<io.mosip.kernel.keymanagerservice.entity.KeyStore> baseKeys = getBaseKeysList(masterKeyUuid);
                    if (Objects.isNull(baseKeys) || baseKeys.size() == 0){
                        LOGGER.info("Base Keys is null or Size Zero. AppId: " + masterKeyAppId + ", Uuid: " + masterKeyUuid);
                    } else {
                        LOGGER.info("Total Number of Base Keys found: " + baseKeys.size() + ", AppId: " + masterKeyAppId 
                                        + ", Uuid: " + masterKeyUuid);
                        PrivateKeyEntry masterKeyEntry = null;
                        try {
                            masterKeyEntry = keyStore.getAsymmetricKey(masterKeyUuid);
                        } catch (Exception exp ){
                            LOGGER.error("Error Getting the Master Key from KeyStore. APP Id: " + masterKeyAppId);
                        }
                        if (masterKeyEntry == null) {
                            LOGGER.error("Error Getting the Master Key from KeyStore. Continuing with other key. Uuid: " + masterKeyUuid);
                        } else {
                            reEncryptAndUpload(masterKeyEntry, masterKeyAppId, baseKeys);
                        }
                    }
                });
            }
        });
    }

    private void reEncryptAndUpload(PrivateKeyEntry masterKeyEntry, String masterKeyAppId, 
                            List<io.mosip.kernel.keymanagerservice.entity.KeyStore> baseKeys) {
        PrivateKey masterKey =  masterKeyEntry.getPrivateKey();
        PublicKey masterPublicKey = masterKeyEntry.getCertificate().getPublicKey();
        X509Certificate newKeyMgrCert = getMasterCertificate(masterKeyAppId);
        PublicKey newKeyMgrPubKey = newKeyMgrCert.getPublicKey();
        baseKeys.forEach(baseKey -> {
            String baseKeyUuid = baseKey.getAlias();
            LOGGER.info("Base Key Found for Master ID: " + masterKeyAppId + " & baseKeyUuid: " + baseKeyUuid);
            try {
                byte[] decryptedPrivateKey = keymanagerUtil.decryptKey(CryptoUtil.decodeBase64(baseKey.getPrivateKey()), 
                            masterKey, masterPublicKey);
                KeyFactory keyFactory = KeyFactory.getInstance(KeymanagerConstant.RSA);
                PrivateKey privateKey = keyFactory.generatePrivate(new PKCS8EncodedKeySpec(decryptedPrivateKey));
                String encryptedPrivateKey = CryptoUtil.encodeBase64(keymanagerUtil.encryptKey(privateKey, newKeyMgrPubKey));
                Optional<KeyAlias> keyAliasObj = keyAliasRepository.findById(baseKeyUuid);
                uploadKeyToNewKeyMgr(keyAliasObj, encryptedPrivateKey, baseKey.getCertificateData());
            } catch (Exception e) {
                LOGGER.error("Error Re-Encrypting the Base key." + e.getMessage());
                e.printStackTrace();
            }
        });
    }
   
    private void migratePartnerKeys() {
        List<KeyAlias> partnerKeyAlias = getPartnerKeyAlias(PARTNER_APP_ID);
        partnerKeyAlias.forEach(partnerKeyObj -> {
            String partnerKeyUuid = partnerKeyObj.getAlias();
            String refId = partnerKeyObj.getReferenceId();
            List<io.mosip.kernel.keymanagerservice.entity.KeyStore> partnerKeys = getBaseKeysList(partnerKeyUuid);
            if (Objects.isNull(partnerKeys) || partnerKeys.size() == 0){
                LOGGER.info("Partner Keys is null or Size Zero. AppId: " + PARTNER_APP_ID + ", RefId: " + refId);
            } else {
                LOGGER.info("Total Number of Partner Keys found: " + partnerKeys.size() + ", AppId: " + PARTNER_APP_ID 
                                + ", RefId: " + refId);
                String noPrivateKey = partnerKeys.get(0).getPrivateKey();
                String certData = partnerKeys.get(0).getCertificateData(); 
                uploadKeyToNewKeyMgr(Optional.of(partnerKeyObj), noPrivateKey, certData);
            }
        });
    }

    private void uploadKeyToNewKeyMgr(Optional<KeyAlias> keyAliasObj, String encryptedPrivateKey, String certData) {

        KeyMigrateBaseKeyRequestDto reqDto = new KeyMigrateBaseKeyRequestDto();
        KeyAlias keyAlias = keyAliasObj.get();
        reqDto.setApplicationId(keyAlias.getApplicationId());
        reqDto.setReferenceId(keyAlias.getReferenceId());
        reqDto.setEncryptedKeyData(encryptedPrivateKey);
        reqDto.setCertificateData(certData);
        reqDto.setNotBefore(keyAlias.getKeyGenerationTime());
        reqDto.setNotAfter(keyAlias.getKeyExpiryTime());

        RequestWrapper<KeyMigrateBaseKeyRequestDto> requestWrapper = new RequestWrapper<>();
		requestWrapper.setRequest(reqDto);

        ResponseEntity<ResponseWrapper<KeyMigrateBaseKeyResponseDto>> response = restTemplate.exchange(uploadKeyUrl,
					HttpMethod.POST, new HttpEntity<>(requestWrapper),
					new ParameterizedTypeReference<ResponseWrapper<KeyMigrateBaseKeyResponseDto>>() {
					});
        LOGGER.info("Upload Response: " + response.getBody().getResponse().getStatus());
        LOGGER.info("Upload Base Key Completed. AppId: " + keyAlias.getApplicationId() + ", RefId: " + keyAlias.getReferenceId());
    }

    private List<String> getMasterKeysList() {
        return Stream.of(appIdsList.split(",")).map(String::trim)
               .filter(appId -> !appId.equalsIgnoreCase(ROOT_APP_ID))
               .collect(Collectors.toList());
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private X509Certificate getMasterCertificate(String appId) {
        Map<String, String> uriParams = new HashMap<>();
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(getCertifcateUrl)
				                .queryParam("applicationId", appId)
				                .queryParam("referenceId", BLANK_REF_ID);
                
        ResponseEntity<Map> response = restTemplate.exchange(builder.build(uriParams), HttpMethod.GET, null, Map.class);
        String certificate =  (String) ((Map<String, Object>) response.getBody().get("response")).get("certificate");
        return (X509Certificate) keymanagerUtil.convertToCertificate(certificate);
	}

    private List<KeyAlias> getKeyAlias(String applicationId, String referenceId) {
		List<KeyAlias> keyAliases = keyAliasRepository.findByApplicationIdAndReferenceId(applicationId, referenceId)
                                    .stream().sorted((alias1, alias2) -> {
                                        return alias1.getKeyGenerationTime().compareTo(alias2.getKeyGenerationTime());
                                    }).collect(Collectors.toList());
		return keyAliases;
    }

    private List<KeyAlias> getPartnerKeyAlias(String applicationId) {
		List<KeyAlias> keyAliases = keyAliasRepository.findByApplicationId(applicationId)
                                    .stream().sorted((alias1, alias2) -> {
                                        return alias1.getKeyGenerationTime().compareTo(alias2.getKeyGenerationTime());
                                    }).collect(Collectors.toList());
		return keyAliases;
    }
    
    private List<io.mosip.kernel.keymanagerservice.entity.KeyStore> getBaseKeysList(String masterKeyAppId) {
        List<io.mosip.kernel.keymanagerservice.entity.KeyStore> baseKeysList = keyStoreRepository.findByMasterAlias(masterKeyAppId);
        return baseKeysList;
    }

    private void migrateZKRandomKeys() {
        List<DataEncryptKeystore> zkRandomKeys = dataEncryptKeystoreRepository.findAll();
        X509Certificate zkTempCertificate = getZKTempCertificate();
        PublicKey zkPublicKey = zkTempCertificate.getPublicKey();
        List<KeyAlias> masterKeyAlias = keyAliasRepository.findByApplicationIdAndReferenceId(KERNEL_APP_ID, IDENTITY_CACHE_REF_ID);
        String zkMasterKeyAlias = masterKeyAlias.get(0).getAlias();
        Key zkMasterKey = keyStore.getSymmetricKey(zkMasterKeyAlias);
        
        List<ZKKeyDataDto> keyDataDtoList = new ArrayList<>();
        zkRandomKeys.forEach(zkKey -> {
            int keyIndex = zkKey.getId();
            String encryptedKey = zkKey.getKey();
            byte[] decryptedZKKey = decryptRandomKey(encryptedKey, zkMasterKey);
            byte[] encryptedRandomKey = cryptoCore.asymmetricEncrypt(zkPublicKey, decryptedZKKey);
            String encodedKey = CryptoUtil.encodeBase64(encryptedRandomKey);
            ZKKeyDataDto keyDataDto = new ZKKeyDataDto();
            keyDataDto.setKeyIndex(keyIndex);
            keyDataDto.setEncryptedKeyData(encodedKey);
            keyDataDtoList.add(keyDataDto);
            int listSize = keyDataDtoList.size();
            if (listSize != 1 && (listSize % uploadBatchSize == 0)) {
                LOGGER.info("Uploading Key List: " + listSize);
                uploadZKKeyToNewKeyMgr(keyDataDtoList, false);
                keyDataDtoList.clear();
                LOGGER.info("Total Completed Uploaded: " + keyIndex);
            }
        });
        int listSize = keyDataDtoList.size();
        LOGGER.info("Uploading Key List(final): " + listSize);
        uploadZKKeyToNewKeyMgr(keyDataDtoList, true);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private X509Certificate getZKTempCertificate() {
        Map<String, String> uriParams = new HashMap<>();
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(getZKTempCertifcateUrl);
                
        ResponseEntity<Map> response = restTemplate.exchange(builder.build(uriParams), HttpMethod.GET, null, Map.class);
        String certificate =  (String) ((Map<String, Object>) response.getBody().get("response")).get("certificate");
        return (X509Certificate) keymanagerUtil.convertToCertificate(certificate);
	}

    private byte[] decryptRandomKey(String secretData, Key zkMasterKey) {
		try {
            byte[] secretDataBytes = Base64.getDecoder().decode(secretData);
			Cipher cipher = Cipher.getInstance(aesECBTransformation);

			cipher.init(Cipher.DECRYPT_MODE, zkMasterKey);
			return cipher.doFinal(secretDataBytes, 0, secretDataBytes.length);
		} catch(NoSuchAlgorithmException | InvalidKeyException | NoSuchPaddingException
			| IllegalBlockSizeException | BadPaddingException | IllegalArgumentException e) {
            LOGGER.error("Error Decrypting ZK Key." + e.getMessage());
		}
        return null;
	}

    private void uploadZKKeyToNewKeyMgr(List<ZKKeyDataDto> keyDataDtoList, boolean purgeKeyFlag) {

        ZKKeyMigrateRequestDto reqDto = new ZKKeyMigrateRequestDto();
        reqDto.setPurgeTempKeyFlag(purgeKeyFlag);
        reqDto.setZkEncryptedDataList(keyDataDtoList);

        RequestWrapper<ZKKeyMigrateRequestDto> requestWrapper = new RequestWrapper<>();
		requestWrapper.setRequest(reqDto);

        ResponseEntity<ResponseWrapper<ZKKeyMigrateResponseDto>> response = restTemplate.exchange(zkUploadKeyUrl,
					HttpMethod.POST, new HttpEntity<>(requestWrapper),
					new ParameterizedTypeReference<ResponseWrapper<ZKKeyMigrateResponseDto>>() {
					});
        List<ZKKeyResponseDto> responseDtoList = response.getBody().getResponse().getZkEncryptedDataList();

        LOGGER.info("Upload Response Key Size: " + responseDtoList.size());
        for (ZKKeyResponseDto keyDto : responseDtoList) {
            LOGGER.info("Upload KeyIndex: " + keyDto.getKeyIndex() + ", Status: " + keyDto.getStatusMessage());
        }
    }
}
