package io.mosip.kernel.bioextractor.service.impl;

import static io.mosip.kernel.bioextractor.config.constant.BioExtractorConfigKeyConstants.CBEFF_DECRYPTION_APP_ID;
import static io.mosip.kernel.bioextractor.config.constant.BiometricExtractionErrorConstants.DOWNLOAD_BIOMETRICS_ERROR;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.mosip.kernel.bioextractor.api.BiometricExtractionService;
import io.mosip.kernel.bioextractor.dto.BioExtractPromiseResponseDTO;
import io.mosip.kernel.bioextractor.dto.BioExtractRequestDTO;
import io.mosip.kernel.bioextractor.exception.BiometricExtractionException;
import io.mosip.kernel.bioextractor.integration.BioExctractorSecurityManager;
import io.mosip.kernel.bioextractor.integration.DataShareManager;
import io.mosip.kernel.bioextractor.service.helper.AsyncHelper;
import io.mosip.kernel.bioextractor.service.helper.RestHelper;
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

	@Override
	public BioExtractPromiseResponseDTO extractBiometrics(BioExtractRequestDTO bioExtractRequestDTO)
			throws BiometricExtractionException {
		BioExtractPromiseResponseDTO bioExtractPromiseResponseDTO = new BioExtractPromiseResponseDTO();
		String promiseId = createPromiseId(bioExtractRequestDTO.getBiometricsUrl());
		Map<String, Object> properties = new LinkedHashMap<>();
		Map<String, String> headersMap = restHelper.createTokenHeaderMap();
		properties.put(HEADER, headersMap);
		asyncHelper.runAsync(() -> doBioExtraction(bioExtractRequestDTO.getBiometricsUrl(), promiseId, properties));
		bioExtractPromiseResponseDTO.setPromiseId(promiseId);
		return bioExtractPromiseResponseDTO;
	}

	private String createPromiseId(String biometricsUrl) {
		int rand = ThreadLocalRandom.current().nextInt(10000);
		String randStr = biometricsUrl + ":" + System.nanoTime() + ":" + rand;
		return HMACUtils.digestAsPlainText(HMACUtils.generateHash(randStr.getBytes()));
	}

	@SuppressWarnings("unchecked")
	private void doBioExtraction(String biometricsUrl, String promiseId, Map<String, Object> properties)
			throws BiometricExtractionException {
		String encryptedCbeff = downloadCbeffFile(biometricsUrl);
		String decryptedCbeff = decryptCbeffFile(encryptedCbeff, (Map<String, String>) properties.get(HEADER));
		String cbeffContent = new String(CryptoUtil.decodeBase64(decryptedCbeff));
		validateCbeffConent(cbeffContent);
	}

	private void validateCbeffConent(String cbeffContent) throws BiometricExtractionException {
		// TODO Auto-generated method stub

	}

	private String decryptCbeffFile(String encryptedCbeff, Map<String, String> header)
			throws BiometricExtractionException {
		return new String(securityManager.decrypt(encryptedCbeff, cbeffDecryptionAppId, null, header));
	}

	private String downloadCbeffFile(String biometricsUrl) throws BiometricExtractionException {
		return dataShareManager.downloadObject(biometricsUrl, String.class, DOWNLOAD_BIOMETRICS_ERROR);
	}

}
