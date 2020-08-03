package io.mosip.kernel.bioextractor.service.impl;

import java.util.concurrent.ThreadLocalRandom;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.mosip.kernel.bioextractor.api.BiometricExtractionService;
import io.mosip.kernel.bioextractor.dto.BioExtractPromiseResponseDTO;
import io.mosip.kernel.bioextractor.dto.BioExtractRequestDTO;
import io.mosip.kernel.bioextractor.exception.BiometricExtractionException;
import io.mosip.kernel.bioextractor.integration.DataShareManager;
import io.mosip.kernel.bioextractor.service.impl.async.AsyncHelper;
import io.mosip.kernel.core.util.HMACUtils;

@Service
public class BiometricExtractionServiceImpl implements BiometricExtractionService {

	@Autowired
	private AsyncHelper<BiometricExtractionException> asyncHelper;
	
	@Autowired
	private DataShareManager dataShareManager;
	
	@Override
	public BioExtractPromiseResponseDTO extractBiometrics(BioExtractRequestDTO bioExtractRequestDTO)  throws BiometricExtractionException{
		BioExtractPromiseResponseDTO bioExtractPromiseResponseDTO = new BioExtractPromiseResponseDTO();
		String promiseId = createPromiseId(bioExtractRequestDTO.getBiometricsUrl());
		asyncHelper.runAsync(() -> doBioExtraction(bioExtractRequestDTO.getBiometricsUrl(), promiseId));
		bioExtractPromiseResponseDTO.setPromiseId(promiseId);
		return bioExtractPromiseResponseDTO;
	}

	private String createPromiseId(String biometricsUrl) {
		int rand = ThreadLocalRandom.current().nextInt(10000);
		String randStr = biometricsUrl + ":" + System.nanoTime() + ":" + rand;
		return HMACUtils.digestAsPlainText(HMACUtils.generateHash(randStr.getBytes()));
	}

	private void doBioExtraction(String biometricsUrl, String promiseId) throws BiometricExtractionException{
		String encryptedCbeff = downloadCbeffFile(biometricsUrl);
	}

	private String downloadCbeffFile(String biometricsUrl) throws BiometricExtractionException {
		return dataShareManager.downloadObject(biometricsUrl, String.class);
	}

}
