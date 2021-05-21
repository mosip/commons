package io.mosip.kernel.cryptosignature.service.impl;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import io.mosip.kernel.core.exception.ExceptionUtils;
import io.mosip.kernel.core.exception.ServiceError;
import io.mosip.kernel.core.http.RequestWrapper;
import io.mosip.kernel.core.signatureutil.exception.SignatureUtilClientException;
import io.mosip.kernel.core.signatureutil.exception.SignatureUtilException;
import io.mosip.kernel.core.signatureutil.model.SignatureResponse;
import io.mosip.kernel.core.signatureutil.spi.SignatureUtil;
import io.mosip.kernel.cryptosignature.constant.SigningDataErrorCode;
import io.mosip.kernel.cryptosignature.dto.SignResponseDto;
import io.mosip.kernel.cryptosignature.dto.SignatureRequestDto;
import io.mosip.kernel.cryptosignature.exception.ExceptionHandler;

/**
 * SignatureUtilImpl implements {@link SignatureUtil} .
 * 
 * @author Srinivasan
 * @author Urvil Joshi
 * @author Raj Jha
 * @since 1.0.0
 */
@Component
public class SignatureUtilImpl implements SignatureUtil {

	/** The sync data request id. */
	@Value("${mosip.kernel.signature.signature-request-id}")
	private String signDataRequestId;

	/** The sync data version id. */
	@Value("${mosip.kernel.signature.signature-version-id}")
	private String signDataVersionId;

	/** The encrypt url. */
	@Value("${mosip.kernel.keymanager-service-sign-url}")
	private String signUrl;
	
	
	/** The rest template. */
	@Autowired
	RestTemplate restTemplate;

	/** The object mapper. */
	@Autowired
	private ObjectMapper objectMapper;

	private static final String RESPONSE_SOURCE = "Keymanager";

    /*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.mosip.kernel.core.signatureutil.spi.SignatureUtil#validateWithPublicKey(
	 * java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public boolean validateWithPublicKey(String signature, String data, String publickey)
			throws InvalidKeySpecException, NoSuchAlgorithmException {
        throw new SignatureUtilException(SigningDataErrorCode.REST_NOT_SUPPORTED_EXCEPTION.getErrorCode(),
                SigningDataErrorCode.REST_NOT_SUPPORTED_EXCEPTION.getErrorMessage());
	}

	@Override
	public SignatureResponse sign(String response) {
		SignatureRequestDto signatureRequestDto = new SignatureRequestDto();
		signatureRequestDto.setData(response);
		RequestWrapper<SignatureRequestDto> requestWrapper = new RequestWrapper<>();
		requestWrapper.setId(signDataRequestId);
		requestWrapper.setVersion(signDataVersionId);
		requestWrapper.setRequest(signatureRequestDto);
		ResponseEntity<String> responseEntity = null;
		try {
			responseEntity = restTemplate.postForEntity(signUrl, requestWrapper, String.class);
		} catch (HttpClientErrorException | HttpServerErrorException ex) {
			List<ServiceError> validationErrorsList = ExceptionUtils.getServiceErrorList(ex.getResponseBodyAsString());

			ExceptionHandler.authExceptionHandler(ex, validationErrorsList, RESPONSE_SOURCE);

			if (!validationErrorsList.isEmpty()) {
				throw new SignatureUtilClientException(validationErrorsList);
			} else {
				throw new SignatureUtilException(SigningDataErrorCode.REST_CRYPTO_CLIENT_EXCEPTION.getErrorCode(),
						SigningDataErrorCode.REST_CRYPTO_CLIENT_EXCEPTION.getErrorMessage(), ex);
			}
		}
		ExceptionHandler.throwExceptionIfExist(responseEntity);
		SignResponseDto signatureResponse = ExceptionHandler.getResponse(objectMapper, responseEntity,
				SignResponseDto.class);
		SignatureResponse signatureResp= new SignatureResponse();
		signatureResp.setData(signatureResponse.getSignature());
		signatureResp.setTimestamp(signatureResponse.getTimestamp());
		return signatureResp;
	}

	@Override
	public boolean validate(String signature, String actualData, String timestamp){
		throw new SignatureUtilException(SigningDataErrorCode.REST_NOT_SUPPORTED_EXCEPTION.getErrorCode(),
                SigningDataErrorCode.REST_NOT_SUPPORTED_EXCEPTION.getErrorMessage());
	}
}
