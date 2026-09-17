package io.mosip.kernel.ridgenerator.exception;

import java.io.IOException;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.util.ContentCachingRequestWrapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import io.mosip.kernel.core.exception.ErrorResponse;
import io.mosip.kernel.core.exception.ServiceError;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.util.EmptyCheckUtils;
import io.mosip.kernel.ridgenerator.constant.RidGeneratorExceptionConstant;

	/**
	 * Maps MOSIP RID exceptions to HTTP 200 MOSIP error wrappers, except unhandled
	 * exceptions which are HTTP 500.
	 *
	 * @author Ritesh Sinha
	 * @since 1.0.0
	 *
	 */
@RestControllerAdvice
public class ApiExceptionalHandler {
	/**
	 * Reference to {@link ObjectMapper}.
	 */
	@Autowired
	private ObjectMapper objectMapper;

	/**
	 * Whitespace used when concatenating HTTP message-not-readable error text.
	 */
	public static final String WHITESPACE = " ";

	/**
	 * Returns HTTP 200 with {@link InputLengthException} as a MOSIP {@code errors} entry.
	 *
	 * @param httpServletRequest current servlet request
	 * @param e                  length validation failure
	 * @return MOSIP error wrapper
	 * @throws IOException when the cached request body cannot be parsed
	 */
	@ExceptionHandler(InputLengthException.class)
	public ResponseEntity<ResponseWrapper<ServiceError>> inputLengthException(
			final HttpServletRequest httpServletRequest, final InputLengthException e) throws IOException {
		ResponseWrapper<ServiceError> responseWrapper = setErrors(httpServletRequest);
		ServiceError error = new ServiceError(e.getErrorCode(), e.getErrorText());
		responseWrapper.getErrors().add(error);
		return new ResponseEntity<>(responseWrapper, HttpStatus.OK);

	}

	/**
	 * Returns HTTP 200 with {@link EmptyInputException} as a MOSIP {@code errors} entry.
	 *
	 * @param httpServletRequest current servlet request
	 * @param e                  empty-input validation failure
	 * @return MOSIP error wrapper
	 * @throws IOException when the cached request body cannot be parsed
	 */
	@ExceptionHandler(EmptyInputException.class)
	public ResponseEntity<ResponseWrapper<ServiceError>> emptyLengthException(
			final HttpServletRequest httpServletRequest, final EmptyInputException e) throws IOException {
		ResponseWrapper<ServiceError> responseWrapper = setErrors(httpServletRequest);
		ServiceError error = new ServiceError(e.getErrorCode(), e.getErrorText());
		responseWrapper.getErrors().add(error);
		return new ResponseEntity<>(responseWrapper, HttpStatus.OK);

	}

	/**
	 * Returns HTTP 200 when the request body cannot be read.
	 *
	 * @param httpServletRequest current servlet request
	 * @param exception          Spring message conversion failure
	 * @return MOSIP error wrapper
	 * @throws IOException when the cached request body cannot be parsed
	 */
	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ResponseWrapper<ServiceError>> onHttpMessageNotReadable(
			final HttpServletRequest httpServletRequest, final HttpMessageNotReadableException exception)
			throws IOException {

		ResponseWrapper<ServiceError> responseWrapper = setErrors(httpServletRequest);
		ServiceError error = new ServiceError(RidGeneratorExceptionConstant.HTTP_MESSAGE_NOT_READABLE.getErrorCode(),
				exception.getMessage());
		responseWrapper.getErrors().add(error);
		return new ResponseEntity<>(responseWrapper, HttpStatus.OK);
	}

	/**
	 * Returns HTTP 500 for unhandled exceptions.
	 *
	 * @param httpServletRequest current servlet request
	 * @param exception          unhandled failure
	 * @return MOSIP error wrapper
	 * @throws IOException when the cached request body cannot be parsed
	 */
	@ExceptionHandler(value = { Exception.class, RuntimeException.class })
	public ResponseEntity<ResponseWrapper<ServiceError>> defaultErrorHandler(
			final HttpServletRequest httpServletRequest, Exception exception) throws IOException {
		ResponseWrapper<ServiceError> responseWrapper = setErrors(httpServletRequest);
		ServiceError error = new ServiceError(RidGeneratorExceptionConstant.RUNTIME_EXCEPTION.getErrorCode(),
				exception.getMessage());
		responseWrapper.getErrors().add(error);
		return new ResponseEntity<>(responseWrapper, HttpStatus.INTERNAL_SERVER_ERROR);
	}

	/**
	 * Copies request {@code id} and {@code version} onto a new error wrapper when the body is cached JSON.
	 *
	 * @param httpServletRequest current servlet request
	 * @return wrapper with id/version set when present
	 * @throws IOException when the cached body is not valid JSON
	 */
	private ResponseWrapper<ServiceError> setErrors(HttpServletRequest httpServletRequest) throws IOException {
		ResponseWrapper<ServiceError> responseWrapper = new ResponseWrapper<>();
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
}
