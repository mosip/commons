package io.mosip.kernel.bioextractor.service.impl;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import io.mosip.kernel.bioextractor.api.BiometricExtractionService;
import io.mosip.kernel.bioextractor.dto.BioExtractPromiseResponseDTO;
import io.mosip.kernel.bioextractor.dto.BioExtractRequestDTO;
import io.mosip.kernel.bioextractor.service.impl.async.AsyncHelper;

@Service
public class BiometricExtractionServiceImpl implements BiometricExtractionService {

	static AtomicInteger count = new AtomicInteger();
	static AtomicInteger externalCount = new AtomicInteger();
	
	@Autowired
	private AsyncHelper asyncHelper;
	
	@Override
	public BioExtractPromiseResponseDTO extractBiometrics(BioExtractRequestDTO bioExtractRequestDTO) {
		BioExtractPromiseResponseDTO bioExtractPromiseResponseDTO = new BioExtractPromiseResponseDTO();
		asyncHelper.runAsync(this::doBioExtraction);
		bioExtractPromiseResponseDTO.setPromiseId(String.valueOf(externalCount.getAndIncrement()));
		return bioExtractPromiseResponseDTO;
	}

	private CompletableFuture<Integer> doBioExtraction() {
		Integer cnt = count.getAndIncrement();
		System.out.println(cnt + ": Started... ");
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(cnt + ": Completed. ");
		return CompletableFuture.completedFuture(cnt);
		
	}

}
