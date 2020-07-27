package io.mosip.kernel.bioextractor.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import io.mosip.kernel.bioextractor.api.BiometricExtractionService;
import io.mosip.kernel.bioextractor.dto.BioExtractPromiseResponseDTO;
import io.mosip.kernel.bioextractor.dto.BioExtractRequestDTO;
import io.mosip.kernel.core.http.RequestWrapper;
import io.mosip.kernel.core.http.ResponseWrapper;

@RestController
public class BiometricExtractorController {
	
	@Autowired 
	private BiometricExtractionService biometricExtractionService;
	
	
	@PostMapping(value="/extracttemplates", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseWrapper<BioExtractPromiseResponseDTO> extractTemplates(@RequestBody RequestWrapper<BioExtractRequestDTO> bioExtractReqDTO) {
		BioExtractPromiseResponseDTO response = biometricExtractionService.extractBiometrics(bioExtractReqDTO.getRequest());
		ResponseWrapper<BioExtractPromiseResponseDTO> responseWrapper = new ResponseWrapper<BioExtractPromiseResponseDTO>();
		responseWrapper.setResponse(response);
		return responseWrapper;
	}

}
