package io.mosip.kernel.bioextractor.service.impl;

import java.util.concurrent.ThreadLocalRandom;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.mosip.kernel.bioextractor.api.BiometricExtractionService;
import io.mosip.kernel.bioextractor.dto.BioExtractPromiseResponseDTO;
import io.mosip.kernel.bioextractor.dto.BioExtractRequestDTO;
import io.mosip.kernel.bioextractor.exception.BiometricExtractionException;
import io.mosip.kernel.bioextractor.service.impl.async.AsyncHelper;
import io.mosip.kernel.core.util.HMACUtils;

@Service
public class BiometricExtractionServiceImpl implements BiometricExtractionService {

	@Autowired
	private AsyncHelper<BiometricExtractionException> asyncHelper;
	
	@Override
	public BioExtractPromiseResponseDTO extractBiometrics(BioExtractRequestDTO bioExtractRequestDTO)  throws BiometricExtractionException{
		BioExtractPromiseResponseDTO bioExtractPromiseResponseDTO = new BioExtractPromiseResponseDTO();
		String promiseId = createPromiseId(bioExtractRequestDTO.getBiometricsUrl());
		asyncHelper.runAsync(() -> doBioExtraction(bioExtractRequestDTO.getBiometricsUrl(), promiseId));
		bioExtractPromiseResponseDTO.setPromiseId(promiseId);
		throw new RuntimeException("test");

//		return bioExtractPromiseResponseDTO;
	}

	private String createPromiseId(String biometricsUrl) {
		int rand = ThreadLocalRandom.current().nextInt(10000);
		String randStr = biometricsUrl + ":" + System.nanoTime() + ":" + rand;
		return HMACUtils.digestAsPlainText(HMACUtils.generateHash(randStr.getBytes()));
	}

	private void doBioExtraction(String biometricsUrl, String promiseId) throws BiometricExtractionException{
		System.out.println("Started " + promiseId);
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Completed " + promiseId);
	}

}
