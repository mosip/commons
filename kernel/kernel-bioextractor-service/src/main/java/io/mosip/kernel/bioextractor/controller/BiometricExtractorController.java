package io.mosip.kernel.bioextractor.controller;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import io.mosip.kernel.bioextractor.api.BiometricExtractionService;
import io.mosip.kernel.bioextractor.dto.BioExtractPromiseResponseDTO;
import io.mosip.kernel.bioextractor.dto.BioExtractRequestDTO;
import io.mosip.kernel.bioextractor.exception.BiometricExtractionException;
import io.mosip.kernel.bioextractor.util.DataValidationUtil;
import io.mosip.kernel.bioextractor.validator.BiometricExtractionRequestValidator;
import io.mosip.kernel.core.http.RequestWrapper;
import io.mosip.kernel.core.http.ResponseWrapper;
import springfox.documentation.annotations.ApiIgnore;

@RestController
public class BiometricExtractorController {

	@Autowired
	private BiometricExtractionRequestValidator validator;

	@Autowired
	private BiometricExtractionService biometricExtractionService;

	@InitBinder
	private void initBinder(WebDataBinder binder) {
		binder.setValidator(validator);
	}

	@PreAuthorize("hasAnyRole('REGISTRATION_PROCESSOR')")
	@PostMapping(value="/extracttemplates", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseWrapper<BioExtractPromiseResponseDTO> extractTemplates(@Valid  @RequestBody RequestWrapper<BioExtractRequestDTO> bioExtractReqDTO, @ApiIgnore Errors errors) throws BiometricExtractionException{
		
		DataValidationUtil.validate(errors);
		
		BioExtractPromiseResponseDTO response = biometricExtractionService.extractBiometrics(bioExtractReqDTO.getRequest());
		ResponseWrapper<BioExtractPromiseResponseDTO> responseWrapper = new ResponseWrapper<BioExtractPromiseResponseDTO>();
		responseWrapper.setResponse(response);
		return responseWrapper;
	}

}
