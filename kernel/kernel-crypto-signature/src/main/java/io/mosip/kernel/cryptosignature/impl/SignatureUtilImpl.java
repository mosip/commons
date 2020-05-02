package io.mosip.kernel.cryptosignature.impl;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.kernel.core.crypto.spi.CryptoCoreSpec;
import io.mosip.kernel.core.exception.ExceptionUtils;
import io.mosip.kernel.core.exception.ServiceError;
import io.mosip.kernel.core.http.RequestWrapper;
import io.mosip.kernel.core.signatureutil.exception.SignatureUtilClientException;
import io.mosip.kernel.core.signatureutil.exception.SignatureUtilException;
import io.mosip.kernel.core.signatureutil.model.SignatureResponse;
import io.mosip.kernel.core.signatureutil.spi.SignatureUtil;
import io.mosip.kernel.core.util.CryptoUtil;
import io.mosip.kernel.core.util.DateUtils;
import io.mosip.kernel.cryptosignature.constant.SigningDataErrorCode;
import io.mosip.kernel.cryptosignature.dto.PublicKeyResponse;
import io.mosip.kernel.cryptosignature.dto.SignResponseDto;
import io.mosip.kernel.cryptosignature.dto.SignatureRequestDto;
import io.mosip.kernel.cryptosignature.dto.TimestampRequestDto;
import io.mosip.kernel.cryptosignature.dto.ValidatorResponseDto;
import io.mosip.kernel.cryptosignature.exception.ExceptionHandler;
import io.mosip.kernel.keygenerator.bouncycastle.KeyGenerator;

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

	@Value("${mosip.kernel.keygenerator.asymmetric-algorithm-name}")
	private String asymmetricAlgorithmName;

	/** The sync data request id. */
	@Value("${mosip.kernel.signature.signature-request-id}")
	private String signDataRequestId;

	/** The sync data version id. */
	@Value("${mosip.kernel.signature.signature-version-id}")
	private String signDataVersionId;

	/** The encrypt url. */
	@Value("${mosip.kernel.keymanager-service-sign-url}")
	private String signUrl;
	
	/** The encrypt url. */
	@Value("${mosip.kernel.keymanager-service-validate-url}")
	private String validateUrl;
	
	public static final String VALIDATION_SUCCESSFUL = "Validation Successful";
	public static final String SUCCESS = "success";



	/** The rest template. */
	@Autowired
	RestTemplate restTemplate;

	/** The object mapper. */
	@Autowired
	private ObjectMapper objectMapper;

	/** The sign applicationid. */
	@Value("${mosip.sign.applicationid:KERNEL}")
	private String signApplicationid;

	/** The sign refid. */
	@Value("${mosip.sign.refid:SIGN}")
	private String signRefid;

	private static final String RESPONSE_SOURCE = "Keymanager";

	@Autowired
	private CryptoCoreSpec<byte[], byte[], SecretKey, PublicKey, PrivateKey, String> cryptoCore;

	/** The key gen. */
	@Autowired
	KeyGenerator keyGen;

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
		PublicKey key = KeyFactory.getInstance("RSA")
				.generatePublic(new X509EncodedKeySpec(CryptoUtil.decodeBase64(publickey)));
		return cryptoCore.verifySignature(data.getBytes(), signature, key);
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
		TimestampRequestDto timestampRequestDto = new TimestampRequestDto();
		timestampRequestDto.setData(actualData);
		timestampRequestDto.setSignature(signature);
		timestampRequestDto.setTimestamp(DateUtils.convertUTCToLocalDateTime(timestamp));
		RequestWrapper<TimestampRequestDto> requestWrapper = new RequestWrapper<>();
		requestWrapper.setId(signDataRequestId);
		requestWrapper.setVersion(signDataVersionId);
		requestWrapper.setRequest(timestampRequestDto);
		ResponseEntity<String> responseEntity = null;
		try {
			responseEntity = restTemplate.postForEntity(validateUrl, requestWrapper, String.class);
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
		ValidatorResponseDto validationResponse = ExceptionHandler.getResponse(objectMapper, responseEntity,
				ValidatorResponseDto.class);
		return (validationResponse.getStatus().equalsIgnoreCase(SUCCESS) && validationResponse.getMessage().equalsIgnoreCase(VALIDATION_SUCCESSFUL));
		
	}
}
