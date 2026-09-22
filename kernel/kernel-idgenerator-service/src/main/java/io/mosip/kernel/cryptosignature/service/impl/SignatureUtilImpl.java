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
 * Signs ID-generator HTTP payloads by posting them to the keymanager sign API.
 * <p>
 * Local public-key validation ({@link #validateWithPublicKey} and {@link #validate})
 * is not supported and always throws {@link SignatureUtilException}.
 * </p>
 *
 * @author Srinivasan
 * @author Urvil Joshi
 * @author Raj Jha
 * @since 1.0.0
 */
@Component
public class SignatureUtilImpl implements SignatureUtil {

	/**
	 * MOSIP request wrapper id sent to keymanager ({@code mosip.kernel.signature.signature-request-id}).
	 */
	@Value("${mosip.kernel.signature.signature-request-id}")
	private String signDataRequestId;

	/**
	 * MOSIP request wrapper version sent to keymanager ({@code mosip.kernel.signature.signature-version-id}).
	 */
	@Value("${mosip.kernel.signature.signature-version-id}")
	private String signDataVersionId;

	/**
	 * Keymanager sign endpoint ({@code mosip.kernel.keymanager-service-sign-url}).
	 */
	@Value("${mosip.kernel.keymanager-service-sign-url}")
	private String signUrl;
	
	
	/** The rest template. */
	@Autowired
	RestTemplate restTemplate;

	/** The object mapper. */
	@Autowired
	private ObjectMapper objectMapper;

	private static final String RESPONSE_SOURCE = "Keymanager";

	/**
	 * Validates a signature with a caller-supplied public key.
	 * <p>
	 * Not implemented in this service.
	 * </p>
	 *
	 * @param signature the signature to verify
	 * @param data      the signed payload
	 * @param publickey the public key
	 * @return never returns; always throws
	 * @throws InvalidKeySpecException    declared by the SPI
	 * @throws NoSuchAlgorithmException   declared by the SPI
	 * @throws SignatureUtilException always, with {@link SigningDataErrorCode#REST_NOT_SUPPORTED_EXCEPTION}
	 */
	@Override
	public boolean validateWithPublicKey(String signature, String data, String publickey)
			throws InvalidKeySpecException, NoSuchAlgorithmException {
        throw new SignatureUtilException(SigningDataErrorCode.REST_NOT_SUPPORTED_EXCEPTION.getErrorCode(),
                SigningDataErrorCode.REST_NOT_SUPPORTED_EXCEPTION.getErrorMessage());
	}

	/**
	 * Posts {@code response} to keymanager and returns the signature and timestamp.
	 *
	 * @param response JSON (or other text) to sign
	 * @return signature bytes and timestamp from keymanager
	 * @throws SignatureUtilClientException when keymanager returns MOSIP service errors
	 * @throws SignatureUtilException       when the REST call fails without service errors
	 * @throws ParseResponseException       when the HTTP entity is missing or cannot be parsed
	 */
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

	/**
	 * Validates a signature against data and timestamp.
	 * <p>
	 * Not implemented in this service.
	 * </p>
	 *
	 * @param signature  the signature to verify
	 * @param actualData the signed payload
	 * @param timestamp  signing timestamp
	 * @return never returns; always throws
	 * @throws SignatureUtilException always, with {@link SigningDataErrorCode#REST_NOT_SUPPORTED_EXCEPTION}
	 */
	@Override
	public boolean validate(String signature, String actualData, String timestamp){
		throw new SignatureUtilException(SigningDataErrorCode.REST_NOT_SUPPORTED_EXCEPTION.getErrorCode(),
                SigningDataErrorCode.REST_NOT_SUPPORTED_EXCEPTION.getErrorMessage());
	}
}
