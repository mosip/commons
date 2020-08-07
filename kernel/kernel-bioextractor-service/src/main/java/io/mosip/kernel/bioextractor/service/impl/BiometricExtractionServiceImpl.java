package io.mosip.kernel.bioextractor.service.impl;

import static io.mosip.kernel.bioextractor.constant.BioExtractorConfigKeyConstants.CBEFF_DECRYPTION_APP_ID;
import static io.mosip.kernel.bioextractor.constant.BioExtractorConfigKeyConstants.CBEFF_ENCRYPTION_APP_ID;
import static io.mosip.kernel.bioextractor.constant.BiometricExtractionErrorConstants.DOWNLOAD_BIOMETRICS_ERROR;
import static io.mosip.kernel.bioextractor.constant.BiometricExtractionErrorConstants.*;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.mosip.kernel.bioextractor.api.BiometricExtractionService;
import io.mosip.kernel.bioextractor.dto.BioExtractNotifyRequestDTO;
import io.mosip.kernel.bioextractor.dto.BioExtractPromiseResponseDTO;
import io.mosip.kernel.bioextractor.dto.BioExtractRequestDTO;
import io.mosip.kernel.bioextractor.exception.BiometricExtractionException;
import io.mosip.kernel.bioextractor.integration.BioExctractorSecurityManager;
import io.mosip.kernel.bioextractor.integration.DataShareManager;
import io.mosip.kernel.bioextractor.integration.IdRepoNotificationManager;
import io.mosip.kernel.bioextractor.service.helper.AsyncHelper;
import io.mosip.kernel.bioextractor.service.helper.BioExtractionHelper;
import io.mosip.kernel.bioextractor.service.helper.RestHelper;
import io.mosip.kernel.core.cbeffutil.spi.CbeffUtil;
import io.mosip.kernel.core.util.CryptoUtil;
import io.mosip.kernel.core.util.HMACUtils;

@Service
public class BiometricExtractionServiceImpl implements BiometricExtractionService {

	private static final String HEADER = "header";

	@Autowired
	private AsyncHelper<BiometricExtractionException> asyncHelper;

	@Autowired
	private RestHelper restHelper;

	@Autowired
	private DataShareManager dataShareManager;

	@Autowired
	private BioExctractorSecurityManager securityManager;

	@Value("${" + CBEFF_DECRYPTION_APP_ID + "}")
	private String cbeffDecryptionAppId;

	@Value("${" + CBEFF_ENCRYPTION_APP_ID + "}")
	private String cbeffEncryptionAppId;

	@Autowired
	private CbeffUtil cbeffUtil;

	@Autowired
	private BioExtractionHelper bioExractionHelper;

	@Autowired
	private IdRepoNotificationManager idRepoNotificationManager;

	@Override
	public BioExtractPromiseResponseDTO extractBiometrics(BioExtractRequestDTO bioExtractRequestDTO)
			throws BiometricExtractionException {
		BioExtractPromiseResponseDTO bioExtractPromiseResponseDTO = new BioExtractPromiseResponseDTO();

		String biometricsUrl = bioExtractRequestDTO.getBiometricsUrl();
		byte[] cbeffFileContent = getCbeffFileContent(biometricsUrl);
		validateCbeffConent(cbeffFileContent);

		String promiseId = createPromiseId(biometricsUrl);
		Map<String, Object> properties = createProperties();
		asyncHelper.runAsync(() -> doBioExtraction(cbeffFileContent, promiseId, properties));
		bioExtractPromiseResponseDTO.setPromiseId(promiseId);
		return bioExtractPromiseResponseDTO;
	}

	private Map<String, Object> createProperties() {
		Map<String, Object> properties = new LinkedHashMap<>();
		Map<String, String> headersMap = restHelper.createTokenHeaderMap();
		properties.put(HEADER, headersMap);
		return properties;
	}

	private String createPromiseId(String biometricsUrl) {
		int rand = ThreadLocalRandom.current().nextInt(10000);
		String randStr = biometricsUrl + ":" + System.nanoTime() + ":" + rand;
		return HMACUtils.digestAsPlainText(HMACUtils.generateHash(randStr.getBytes()));
	}

	private byte[] getCbeffFileContent(String biometricsUrl) throws BiometricExtractionException {
		String encryptedCbeff = downloadCbeffFile(biometricsUrl);
		String decryptedCbeff = decryptCbeffFile(encryptedCbeff);
		byte[] cbeffContent = CryptoUtil.decodeBase64(decryptedCbeff);
		return cbeffContent;
	}

	public void validateCbeffConent(byte[] cbeffContent) throws BiometricExtractionException {
		try {
			cbeffUtil.validateXML(cbeffContent);
		} catch (Exception e) {
			throw new BiometricExtractionException(INVALID_CBEFF, e);
		}
	}

	@SuppressWarnings("unchecked")
	private void doBioExtraction(byte[] cbeffContent, String promiseId, Map<String, Object> properties)
			throws BiometricExtractionException {
		byte[] extractedTemplatesCbeff = bioExractionHelper.extractTemplates(cbeffContent);

		String encryptedData = encrypt(extractedTemplatesCbeff, (Map<String, String>)properties.get(HEADER));
		
		String url = dataShareManager.uploadBytes(encryptedData.getBytes(), UPLOAD_BIOMETRICS_ERROR);
	    
		BioExtractNotifyRequestDTO bioExtractNotifyReqDTO = new BioExtractNotifyRequestDTO(promiseId, url);
		idRepoNotificationManager.notifyIdRepo(bioExtractNotifyReqDTO);
		
	}

	private String encrypt(byte[] extractedTemplatesCbeff, Map<String, String> headers)
			throws BiometricExtractionException {
		return securityManager.encrypt(extractedTemplatesCbeff, cbeffEncryptionAppId, null, headers);
	}

	private String decryptCbeffFile(String encryptedCbeff) throws BiometricExtractionException {
		return securityManager.decrypt(encryptedCbeff, cbeffDecryptionAppId, null, null);
	}

	private String downloadCbeffFile(String biometricsUrl) throws BiometricExtractionException {
		return dataShareManager.downloadObject(biometricsUrl, String.class, DOWNLOAD_BIOMETRICS_ERROR);
	}

}
