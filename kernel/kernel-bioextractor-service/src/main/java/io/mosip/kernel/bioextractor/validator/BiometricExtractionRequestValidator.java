package io.mosip.kernel.bioextractor.validator;

import static io.mosip.kernel.bioextractor.config.constant.BioExtractorConstants.REQUEST;
import static io.mosip.kernel.bioextractor.config.constant.BiometricExtractionErrorConstants.INVALID_INPUT_PARAMETER;
import static io.mosip.kernel.bioextractor.config.constant.BiometricExtractionErrorConstants.MISSING_INPUT_PARAMETER;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import io.mosip.kernel.bioextractor.dto.BioExtractRequestDTO;
import io.mosip.kernel.core.http.RequestWrapper;

@Component
public class BiometricExtractionRequestValidator implements Validator {

	private static final String REQUEST_BIO_URL = REQUEST + "/biometricsUrl";

	@Override
	public boolean supports(Class<?> clazz) {
		return RequestWrapper.class.equals(clazz);
	}

	@Override
	public void validate(Object target, Errors errors) {
		if (Objects.nonNull(target)) {
			RequestWrapper<BioExtractRequestDTO> requestWrapperDto = (RequestWrapper<BioExtractRequestDTO>) target;
			BioExtractRequestDTO request = requestWrapperDto.getRequest();
			
			validateBiometricsUrl(errors, request);

		}
		
	}

	private void validateBiometricsUrl(Errors errors, BioExtractRequestDTO request) {
		if(StringUtils.isEmpty(request.getBiometricsUrl())) {
			errors.rejectValue(REQUEST,
					MISSING_INPUT_PARAMETER.getErrorCode(),
					new String[] {REQUEST_BIO_URL},
					MISSING_INPUT_PARAMETER.getErrorMessage());
		} else if(!validateUrl(request.getBiometricsUrl())) {
			errors.rejectValue("request",
					INVALID_INPUT_PARAMETER.getErrorCode(),
					new String[] {REQUEST_BIO_URL + " : " + request.getBiometricsUrl()},
					INVALID_INPUT_PARAMETER.getErrorMessage());
		}
	}

	private boolean validateUrl(String biometricsUrl) {
		try {
			URL url = new URL(biometricsUrl);
			String protocol = url.getProtocol();
			return protocol.equalsIgnoreCase("http") || protocol.equalsIgnoreCase("https");
			
		} catch (MalformedURLException e) {
			return false;
		}
	}

}