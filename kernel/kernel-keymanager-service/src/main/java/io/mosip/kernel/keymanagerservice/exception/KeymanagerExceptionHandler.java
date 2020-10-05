/*
 * 
 * 
 * 
 * 
 */
package io.mosip.kernel.keymanagerservice.exception;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.util.ContentCachingRequestWrapper;

import io.mosip.kernel.core.crypto.exception.InvalidDataException;
import io.mosip.kernel.core.crypto.exception.InvalidKeyException;
import io.mosip.kernel.core.crypto.exception.NullDataException;
import io.mosip.kernel.core.exception.BaseUncheckedException;
import io.mosip.kernel.core.exception.ErrorResponse;
import io.mosip.kernel.core.exception.ExceptionUtils;
import io.mosip.kernel.core.exception.NoSuchAlgorithmException;
import io.mosip.kernel.core.exception.ServiceError;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.idgenerator.exception.TokenIdGeneratorException;
import io.mosip.kernel.core.keymanager.exception.KeystoreProcessingException;
import io.mosip.kernel.core.signatureutil.exception.ParseResponseException;
import io.mosip.kernel.core.signatureutil.exception.SignatureUtilClientException;
import io.mosip.kernel.core.signatureutil.exception.SignatureUtilException;
import io.mosip.kernel.core.util.EmptyCheckUtils;
import io.mosip.kernel.cryptomanager.constant.CryptomanagerErrorCode;
import io.mosip.kernel.cryptomanager.exception.CryptoManagerSerivceException;
import io.mosip.kernel.keymanagerservice.constant.KeymanagerConstant;
import io.mosip.kernel.keymanagerservice.constant.KeymanagerErrorConstant;
import io.mosip.kernel.lkeymanager.exception.InvalidArgumentsException;
import io.mosip.kernel.lkeymanager.exception.LicenseKeyServiceException;
import io.mosip.kernel.partnercertservice.exception.PartnerCertManagerException;
import io.mosip.kernel.signature.exception.RequestException;
import io.mosip.kernel.signature.exception.SignatureFailureException;
import io.mosip.kernel.zkcryptoservice.exception.ZKCryptoException;
import io.mosip.kernel.zkcryptoservice.exception.ZKKeyDerivationException;
import io.mosip.kernel.zkcryptoservice.exception.ZKRandomKeyDecryptionException;

/**
 * Rest Controller Advice for Keymanager Service
 * 
 * @author Dharmesh Khandelwal
 *
 * @since 1.0.0
 */
@RestControllerAdvice
public class KeymanagerExceptionHandler {

	@Autowired
	private ObjectMapper objectMapper;

	@ExceptionHandler(NullDataException.class)
	public ResponseEntity<ResponseWrapper<ServiceError>> nullDataException(HttpServletRequest httpServletRequest,
			final NullDataException e) throws IOException {
		ExceptionUtils.logRootCause(e);
		return new ResponseEntity<>(
				getErrorResponse(httpServletRequest, e.getErrorCode(), e.getErrorText(), HttpStatus.OK), HttpStatus.OK);
	}

	@ExceptionHandler(InvalidKeyException.class)
	public ResponseEntity<ResponseWrapper<ServiceError>> invalidKeyException(HttpServletRequest httpServletRequest,
			final InvalidKeyException e) throws IOException {
		ExceptionUtils.logRootCause(e);
		return new ResponseEntity<>(
				getErrorResponse(httpServletRequest, e.getErrorCode(), e.getErrorText(), HttpStatus.OK), HttpStatus.OK);
	}

	@ExceptionHandler(NoSuchAlgorithmException.class)
	public ResponseEntity<ResponseWrapper<ServiceError>> noSuchAlgorithmException(HttpServletRequest httpServletRequest,
			final NoSuchAlgorithmException e) throws IOException {
		ExceptionUtils.logRootCause(e);
		return new ResponseEntity<>(
				getErrorResponse(httpServletRequest, e.getErrorCode(), e.getErrorText(), HttpStatus.OK), HttpStatus.OK);
	}
	

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<ResponseWrapper<ServiceError>> illegalArgumentException(HttpServletRequest httpServletRequest,
			final IllegalArgumentException e) throws IOException {
		return new ResponseEntity<>(
				getErrorResponse(httpServletRequest,
						CryptomanagerErrorCode.INVALID_DATA_WITHOUT_KEY_BREAKER.getErrorCode(),
						CryptomanagerErrorCode.INVALID_DATA_WITHOUT_KEY_BREAKER.getErrorMessage(), HttpStatus.OK),
				HttpStatus.OK);
	}

	@ExceptionHandler(InvalidFormatException.class)
	public ResponseEntity<ResponseWrapper<ServiceError>> invalidFormatException(HttpServletRequest httpServletRequest,
			final InvalidFormatException e) throws IOException {
		return new ResponseEntity<>(
				getErrorResponse(httpServletRequest, KeymanagerErrorConstant.DATE_TIME_PARSE_EXCEPTION.getErrorCode(),
						e.getMessage() + KeymanagerConstant.WHITESPACE
								+ KeymanagerErrorConstant.DATE_TIME_PARSE_EXCEPTION.getErrorMessage(),
						HttpStatus.OK),
				HttpStatus.OK);
	}

	@ExceptionHandler(DateTimeParseException.class)
	public ResponseEntity<ResponseWrapper<ServiceError>> dateTimeParseException(HttpServletRequest httpServletRequest,
			final DateTimeParseException e) throws IOException {
		return new ResponseEntity<>(
				getErrorResponse(httpServletRequest, KeymanagerErrorConstant.DATE_TIME_PARSE_EXCEPTION.getErrorCode(),
						e.getMessage() + KeymanagerConstant.WHITESPACE
								+ KeymanagerErrorConstant.DATE_TIME_PARSE_EXCEPTION.getErrorMessage(),
						HttpStatus.OK),
				HttpStatus.OK);
	}

	@ExceptionHandler(InvalidDataException.class)
	public ResponseEntity<ResponseWrapper<ServiceError>> invalidDataException(HttpServletRequest httpServletRequest,
			final InvalidDataException e) throws IOException {
		ExceptionUtils.logRootCause(e);
		return new ResponseEntity<>(
				getErrorResponse(httpServletRequest, e.getErrorCode(), e.getErrorText(), HttpStatus.OK), HttpStatus.OK);
	}

	@ExceptionHandler(NoUniqueAliasException.class)
	public ResponseEntity<ResponseWrapper<ServiceError>> noUniqueAliasException(HttpServletRequest httpServletRequest,
			final NoUniqueAliasException e) throws IOException {
		ExceptionUtils.logRootCause(e);
		return new ResponseEntity<>(
				getErrorResponse(httpServletRequest, e.getErrorCode(), e.getErrorText(), HttpStatus.OK), HttpStatus.OK);
	}

	@ExceptionHandler(CryptoException.class)
	public ResponseEntity<ResponseWrapper<ServiceError>> cryptoException(HttpServletRequest httpServletRequest,
			final CryptoException e) throws IOException {
		ExceptionUtils.logRootCause(e);
		return new ResponseEntity<>(
				getErrorResponse(httpServletRequest, e.getErrorCode(), e.getErrorText(), HttpStatus.OK), HttpStatus.OK);
	}

	@ExceptionHandler(KeymanagerServiceException.class)
	public ResponseEntity<ResponseWrapper<ServiceError>> keymanagerServiceException(
			HttpServletRequest httpServletRequest, final KeymanagerServiceException e) throws IOException {
		ExceptionUtils.logRootCause(e);
		return new ResponseEntity<>(
				getErrorResponse(httpServletRequest, e.getErrorCode(), e.getErrorText(), HttpStatus.OK), HttpStatus.OK);
	}
	
	@ExceptionHandler(CryptoManagerSerivceException.class)
	public ResponseEntity<ResponseWrapper<ServiceError>> cryptoManagerServieException(
			HttpServletRequest httpServletRequest, final CryptoManagerSerivceException e) throws IOException {
		ExceptionUtils.logRootCause(e);
		return new ResponseEntity<>(
				getErrorResponse(httpServletRequest, e.getErrorCode(), e.getErrorText(), HttpStatus.OK), HttpStatus.OK);
	}

	@ExceptionHandler(InvalidApplicationIdException.class)
	public ResponseEntity<ResponseWrapper<ServiceError>> invalidApplicationIdException(
			HttpServletRequest httpServletRequest, final InvalidApplicationIdException e) throws IOException {
		ExceptionUtils.logRootCause(e);
		return new ResponseEntity<>(
				getErrorResponse(httpServletRequest, e.getErrorCode(), e.getErrorText(), HttpStatus.OK), HttpStatus.OK);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ResponseWrapper<ServiceError>> methodArgumentNotValidException(
			HttpServletRequest httpServletRequest, final MethodArgumentNotValidException e) throws IOException {
		ResponseWrapper<ServiceError> errorResponse = setErrors(httpServletRequest);
		final List<FieldError> fieldErrors = e.getBindingResult().getFieldErrors();
		fieldErrors.forEach(x -> {
			ServiceError error = new ServiceError(KeymanagerErrorConstant.INVALID_REQUEST.getErrorCode(),
					x.getField() + KeymanagerConstant.WHITESPACE + x.getDefaultMessage());
			errorResponse.getErrors().add(error);
		});
		return new ResponseEntity<>(errorResponse, HttpStatus.OK);
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ResponseWrapper<ServiceError>> onHttpMessageNotReadable(HttpServletRequest httpServletRequest,
			final HttpMessageNotReadableException e) throws IOException {
		ResponseWrapper<ServiceError> errorResponse = setErrors(httpServletRequest);
		ServiceError error = new ServiceError(KeymanagerErrorConstant.INVALID_REQUEST.getErrorCode(), e.getMessage());
		errorResponse.getErrors().add(error);
		return new ResponseEntity<>(errorResponse, HttpStatus.OK);
	}

	@ExceptionHandler(MissingServletRequestParameterException.class)
	public ResponseEntity<ResponseWrapper<ServiceError>> onMissingServletRequestParameterException(
			HttpServletRequest httpServletRequest, final MissingServletRequestParameterException e) throws IOException {
		ResponseWrapper<ServiceError> errorResponse = setErrors(httpServletRequest);
		ServiceError error = new ServiceError(KeymanagerErrorConstant.INVALID_REQUEST.getErrorCode(), e.getMessage());
		errorResponse.getErrors().add(error);
		return new ResponseEntity<>(errorResponse, HttpStatus.OK);
	}
	
	@ExceptionHandler(TokenIdGeneratorException.class)
	public ResponseEntity<ResponseWrapper<ServiceError>> emptyLengthException(
			final HttpServletRequest httpServletRequest, final TokenIdGeneratorException e) throws IOException {
		ResponseWrapper<ServiceError> responseWrapper = setErrors(httpServletRequest);
		ServiceError error = new ServiceError(e.getErrorCode(), e.getErrorText());
		responseWrapper.getErrors().add(error);
		return new ResponseEntity<>(responseWrapper, HttpStatus.OK);

	}

	@ExceptionHandler(ZKCryptoException.class)
	public ResponseEntity<ResponseWrapper<ServiceError>> zkCryptoException(
			HttpServletRequest httpServletRequest, final ZKCryptoException e) throws IOException {
		ExceptionUtils.logRootCause(e);
		return new ResponseEntity<>(
				getErrorResponse(httpServletRequest, e.getErrorCode(), e.getErrorText(), HttpStatus.OK), HttpStatus.OK);
	}

	@ExceptionHandler(ZKKeyDerivationException.class)
	public ResponseEntity<ResponseWrapper<ServiceError>> zkKeyDerivationException(
			HttpServletRequest httpServletRequest, final ZKKeyDerivationException e) throws IOException {
		ExceptionUtils.logRootCause(e);
		return new ResponseEntity<>(
				getErrorResponse(httpServletRequest, e.getErrorCode(), e.getErrorText(), HttpStatus.OK), HttpStatus.OK);
	}

	@ExceptionHandler(ZKRandomKeyDecryptionException.class)
	public ResponseEntity<ResponseWrapper<ServiceError>> zkRandomKeyDecryptionException(
			HttpServletRequest httpServletRequest, final ZKRandomKeyDecryptionException e) throws IOException {
		ExceptionUtils.logRootCause(e);
		return new ResponseEntity<>(
				getErrorResponse(httpServletRequest, e.getErrorCode(), e.getErrorText(), HttpStatus.OK), HttpStatus.OK);
	}

	@ExceptionHandler(PartnerCertManagerException.class)
	public ResponseEntity<ResponseWrapper<ServiceError>> partnerCertManagerException(
			HttpServletRequest httpServletRequest, final PartnerCertManagerException e) throws IOException {
		ExceptionUtils.logRootCause(e);
		return new ResponseEntity<>(
				getErrorResponse(httpServletRequest, e.getErrorCode(), e.getErrorText(), HttpStatus.OK), HttpStatus.OK);
	}
	
	
	@ExceptionHandler(KeystoreProcessingException.class)
	public ResponseEntity<ResponseWrapper<ServiceError>> keystoreProcessingException(
			HttpServletRequest httpServletRequest, final KeystoreProcessingException e) throws IOException {
		ExceptionUtils.logRootCause(e);
		return new ResponseEntity<>(
				getErrorResponse(httpServletRequest, e.getErrorCode(), e.getErrorText(), HttpStatus.OK), HttpStatus.OK);
	}
	/**
	 * Method to handle {@link InvalidArgumentsException}.
	 * 
	 * @param httpServletRequest the request
	 * @param exception          the exception.
	 * @return {@link ErrorResponse}.
	 * @throws IOException the IO exception
	 */
	@ExceptionHandler(InvalidArgumentsException.class)
	public ResponseEntity<ResponseWrapper<ServiceError>> validateInputArguments(HttpServletRequest httpServletRequest,
			final InvalidArgumentsException exception) throws IOException {
		ResponseWrapper<ServiceError> errorResponse = setErrors(httpServletRequest);
		errorResponse.getErrors().addAll(exception.getList());
		return new ResponseEntity<>(errorResponse, HttpStatus.OK);
	}

	/**
	 * Method to handle {@link LicenseKeyServiceException}.
	 * 
	 * @param httpServletRequest the request
	 * @param exception          the exception.
	 * @return {@link ErrorResponse}.
	 * @throws IOException the IO exception
	 */
	@ExceptionHandler(LicenseKeyServiceException.class)
	public ResponseEntity<ResponseWrapper<ServiceError>> handleServiceException(HttpServletRequest httpServletRequest,
			final LicenseKeyServiceException exception) throws IOException {
		ResponseWrapper<ServiceError> errorResponse = setErrors(httpServletRequest);
		errorResponse.getErrors().addAll(exception.getList());
		return new ResponseEntity<>(errorResponse, HttpStatus.OK);
	}

	
	@ExceptionHandler(RequestException.class)
	public ResponseEntity<ResponseWrapper<ServiceError>> controlRequestException(
			final HttpServletRequest httpServletRequest, final RequestException e) throws IOException {
		ExceptionUtils.logRootCause(e);
		return getErrorResponseEntity(e, HttpStatus.OK, httpServletRequest);
	}

	@ExceptionHandler(SignatureFailureException.class)
	public ResponseEntity<ResponseWrapper<ServiceError>> signatureFailureException(
			final HttpServletRequest httpServletRequest, final SignatureFailureException e) throws IOException {
		ExceptionUtils.logRootCause(e);
		return getErrorResponseEntity(e, HttpStatus.OK, httpServletRequest);
	}

	@ExceptionHandler(SignatureUtilClientException.class)
	public ResponseEntity<ResponseWrapper<ServiceError>> signatureUtilClientException(
			final HttpServletRequest httpServletRequest, final SignatureUtilClientException e) throws IOException {
		ResponseWrapper<ServiceError> responseWrapper = setErrors(httpServletRequest);
		responseWrapper.getErrors().addAll(e.getList());
		ExceptionUtils.logRootCause(e);
		return new ResponseEntity<>(responseWrapper, HttpStatus.OK);
	}

	@ExceptionHandler(SignatureUtilException.class)
	public ResponseEntity<ResponseWrapper<ServiceError>> signatureUtilException(
			final HttpServletRequest httpServletRequest, final SignatureUtilException e) throws IOException {
		ExceptionUtils.logRootCause(e);
		return getErrorResponseEntity(e, HttpStatus.OK, httpServletRequest);
	}

	@ExceptionHandler(ParseResponseException.class)
	public ResponseEntity<ResponseWrapper<ServiceError>> parseResponseException(
			final HttpServletRequest httpServletRequest, final ParseResponseException e) throws IOException {
		ExceptionUtils.logRootCause(e);
		return getErrorResponseEntity(e, HttpStatus.OK, httpServletRequest);
	}

	@ExceptionHandler(value = { Exception.class, RuntimeException.class })
	public ResponseEntity<ResponseWrapper<ServiceError>> defaultErrorHandler(HttpServletRequest httpServletRequest,
			Exception e) throws IOException {
		ResponseWrapper<ServiceError> errorResponse = setErrors(httpServletRequest);
		ServiceError error = new ServiceError(KeymanagerErrorConstant.INTERNAL_SERVER_ERROR.getErrorCode(),
				e.getMessage());
		errorResponse.getErrors().add(error);
		ExceptionUtils.logRootCause(e);
		return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
	}

	private ResponseWrapper<ServiceError> getErrorResponse(HttpServletRequest httpServletRequest, String errorCode,
			String errorMessage, HttpStatus httpStatus) throws IOException {
		ResponseWrapper<ServiceError> errorResponse = setErrors(httpServletRequest);
		ServiceError error = new ServiceError(errorCode, errorMessage);
		errorResponse.getErrors().add(error);
		return errorResponse;
	}

	private ResponseWrapper<ServiceError> setErrors(HttpServletRequest httpServletRequest) throws IOException {
		ResponseWrapper<ServiceError> responseWrapper = new ResponseWrapper<>();
		responseWrapper.setResponsetime(LocalDateTime.now(ZoneId.of("UTC")));
		String requestBody = null;
		if (httpServletRequest instanceof ContentCachingRequestWrapper) {
			requestBody = new String(((ContentCachingRequestWrapper) httpServletRequest).getContentAsByteArray());
		}
		if (EmptyCheckUtils.isNullEmpty(requestBody)) {
			return responseWrapper;
		}
		objectMapper.registerModule(new JavaTimeModule());
		JsonNode reqNode = objectMapper.readTree(requestBody);
		responseWrapper.setId(reqNode.path("id").asText());
		responseWrapper.setVersion(reqNode.path("version").asText());
		return responseWrapper;
	}
	
	private ResponseEntity<ResponseWrapper<ServiceError>> getErrorResponseEntity(BaseUncheckedException e,
			HttpStatus httpStatus, HttpServletRequest httpServletRequest) throws IOException {
		ResponseWrapper<ServiceError> responseWrapper = setErrors(httpServletRequest);
		ServiceError error = new ServiceError(e.getErrorCode(), e.getErrorText());
		responseWrapper.getErrors().add(error);
		return new ResponseEntity<>(responseWrapper, httpStatus);
	}
}
