package io.mosip.kernel.keygenerator.generator;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.mosip.kernel.core.util.DateUtils;
import io.mosip.kernel.keymanagerservice.dto.KeyPairGenerateRequestDto;
import io.mosip.kernel.keymanagerservice.entity.KeyAlias;
import io.mosip.kernel.keymanagerservice.helper.KeymanagerDBHelper;
import io.mosip.kernel.keymanagerservice.repository.KeyAliasRepository;
import io.mosip.kernel.keymanagerservice.service.KeymanagerService;

/**
 * The Class MasterKeysGenerator.
 *
 * @author Mahammed Taheer
 */
@SuppressWarnings("restriction")
@Component
public class KeysGenerator {
    
    private static final String ROOT_APP_ID = "ROOT";

    private static final String BLANK_REF_ID = "";

    private static final String MOSIP_CN = "MOSIP-";

    private static final String DUMMY_RESP_TYPE = "CSR";

    private static final String IDENTITY_CACHE_REF_ID = "IDENTITY_CACHE";
    
    @Value("${mosip.kernel.keymanager.autogen.appids.list}")
    private String appIdsList;
    
    /**
	 * Organizational Unit for generating certificate
	 */
	@Value("${mosip.kernel.keymanager.certificate.default.organizational-unit}")
	private String organizationUnit;

	/**
	 * Organization for generating certificate
	 */
	@Value("${mosip.kernel.keymanager.certificate.default.organization}")
	private String organization;

	/**
	 * Location for generating certificate
	 */
	@Value("${mosip.kernel.keymanager.certificate.default.location}")
	private String location;

	/**
	 * State for generating certificate
	 */
	@Value("${mosip.kernel.keymanager.certificate.default.state}")
	private String state;

	/**
	 * Country for generating certificate
	 */
	@Value("${mosip.kernel.keymanager.certificate.default.country}")
    private String country;
    

	@Value("${mosip.kernel.zkcrypto.generate.ida.publickey:false}")
    private boolean genPubKeyFlag;
    
    @Value("${mosip.kernel.zkcrypto.publickey.application.id}")
    private String idaAppId;

    @Value("${mosip.kernel.zkcrypto.publickey.reference.id}")
    private String idaPubKeyRefId;

    @Value("${mosip.kernel.keymanager.autogen.basekeys.list}")
    private String baseKeys;

    
	@Autowired
	private KeyAliasRepository keyAliasRepository;
    
    @Autowired
	KeymanagerService keymanagerService;

    @Autowired
    RandomKeysGenerator randomKeysGenerator;

    @Autowired
    private KeymanagerDBHelper dbHelper;
    
    public void generateKeys() throws Exception {
        
        String rootKeyAlias = getKeyAlias(ROOT_APP_ID, BLANK_REF_ID);
        if(Objects.isNull(rootKeyAlias)) {
            generateMasterKey(ROOT_APP_ID, BLANK_REF_ID, MOSIP_CN + ROOT_APP_ID);
            System.out.println("Generated ROOT Key.");
        }

        List<String> keyAppIdsList = getListKeys();
        keyAppIdsList.forEach(appId -> {
            String[] strArr = appId.split(":", -1);
            String applicationId = strArr[0];
            String referenceId = BLANK_REF_ID;
            String commonName = MOSIP_CN + applicationId.toUpperCase();
            if (strArr.length > 1) {
                referenceId = strArr[1];
                commonName = commonName + "-" + referenceId.toUpperCase();
            }
            if (referenceId.equalsIgnoreCase(IDENTITY_CACHE_REF_ID)) {
                randomKeysGenerator.generateRandomKeys(applicationId, referenceId);
                System.out.println("Generated Cache Key & Random Keys.");
            } else {
                String masterKeyAlias = getKeyAlias(applicationId, referenceId);
                if(Objects.isNull(masterKeyAlias)) {
                    generateMasterKey(applicationId, referenceId, commonName);
                    System.out.println("Generated Master Key for Application ID & ReferenceId: " + appId);
                } else {
                    System.out.println("Master Key Already exists for Application ID & ReferenceId: " + appId);
                }
            }
        });

        if (genPubKeyFlag) {
            createIDAPublicKeyEntry();
        }

        List<String> baseKeysList = getBaseKeysList();

        baseKeysList.forEach(appId -> {
            String[] strArr = appId.split(":", -1);
            if (strArr.length == 2) {
                String applicationId = strArr[0];
                String referenceId = strArr[1];
                if (referenceId.length() != 0) {
                    generateBaseKey(applicationId, referenceId);
                    System.out.println("Base Key Successful. AppId: " +  applicationId + ", refId: " + referenceId);
                } else {
                    System.err.println("Configured Reference Id is not valid. Configured value: " + appId);
                }
            } else {
                System.err.println("Configured Base Key is not valid. Configured value: " + appId);
            }
        });
    }

    private List<String> getListKeys() {
         return Stream.of(appIdsList.split(",")).map(String::trim)
                .filter(appId -> !appId.equalsIgnoreCase(ROOT_APP_ID))
                .collect(Collectors.toList());
    }

    private List<String> getBaseKeysList() {
        return Stream.of(baseKeys.split(",")).map(String::trim)
               .collect(Collectors.toList());
   }

    private String getKeyAlias(String applicationId, String referenceId) {
		List<KeyAlias> keyAliases = keyAliasRepository.findByApplicationIdAndReferenceId(applicationId, referenceId)
                                    .stream().sorted((alias1, alias2) -> {
                                        return alias1.getKeyGenerationTime().compareTo(alias2.getKeyGenerationTime());
                                    }).collect(Collectors.toList());
		List<KeyAlias> currentKeyAliases = keyAliases.stream().filter((keyAlias) -> {
			                        return isValidTimestamp(LocalDateTime.now(), keyAlias);
		                            }).collect(Collectors.toList());

		if (!currentKeyAliases.isEmpty() && currentKeyAliases.size() == 1) {
			System.out.println("CurrentKeyAlias size is one. Will decrypt symmetric key for this alias");
			return currentKeyAliases.get(0).getAlias();
		}

		return null;
    }
    
    private boolean isValidTimestamp(LocalDateTime timeStamp, KeyAlias keyAlias) {
		return timeStamp.isEqual(keyAlias.getKeyGenerationTime()) || timeStamp.isEqual(keyAlias.getKeyExpiryTime())
				|| timeStamp.isAfter(keyAlias.getKeyGenerationTime())
						&& timeStamp.isBefore(keyAlias.getKeyExpiryTime());
    }
    
    private void generateMasterKey(String appId, String refId, String commonName){
        KeyPairGenerateRequestDto requestDto = new KeyPairGenerateRequestDto();
        requestDto.setApplicationId(appId);
        requestDto.setReferenceId(refId);
        requestDto.setForce(false);
        requestDto.setCommonName(commonName);
        requestDto.setOrganizationUnit(organizationUnit);
        requestDto.setOrganization(organization);
        requestDto.setLocation(location);
        requestDto.setState(state);
        requestDto.setCountry(country);
        keymanagerService.generateMasterKey(DUMMY_RESP_TYPE, requestDto);
    }

    private void createIDAPublicKeyEntry() {
        String keyExists = getKeyAlias(idaAppId, idaPubKeyRefId);
        if (Objects.isNull(keyExists)) {
            LocalDateTime localDateTimeStamp = DateUtils.getUTCCurrentDateTime();
            String alias = UUID.randomUUID().toString();
            dbHelper.storeKeyInAlias(idaAppId, localDateTimeStamp, idaPubKeyRefId, alias, localDateTimeStamp.plusDays(730));
            dbHelper.storeKeyInDBStore(alias, alias, "", "NA");
        }
    }

    private void generateBaseKey(String appId, String refId){
        keymanagerService.getCertificate(appId, Optional.of(refId));
    }
}
