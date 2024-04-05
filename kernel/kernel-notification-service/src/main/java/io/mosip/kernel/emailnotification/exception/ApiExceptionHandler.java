package io.mosip.kernel.emailnotification.exception;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.util.ContentCachingRequestWrapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import io.mosip.kernel.core.exception.ExceptionUtils;
import io.mosip.kernel.core.exception.ServiceError;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.util.EmptyCheckUtils;
import io.mosip.kernel.emailnotification.constant.MailNotifierArgumentErrorConstants;
import io.mosip.kernel.emailnotification.constant.SmsExceptionConstant;

/**
 * Central exception handler for mail-notifier service.
 * 
 * @author Sagar Mahapatra
 * @since 1.0.0
 */
@RestControllerAdvice
public class ApiExceptionHandler {

	/**
	 * Autowired reference for {@link ObjectMapper}.
	 */
	@Autowired
	private ObjectMapper objectMapper;
	
	private static final String WHITESPACE = " ";

	/**
	 * This method handles {@link InvalidArgumentsException}.
	 * 
	 * @param httpServletRequest the servlet request.
	 * @param exception          the exception.
	 * @return the error response.
	 * @throws IOException when the response is not mapped.
	 */
	@ExceptionHandler(InvalidArgumentsException.class)
	public ResponseEntity<ResponseWrapper<ServiceError>> mailNotifierArgumentsValidation(
			final HttpServletRequest httpServletRequest, final InvalidArgumentsException exception) throws IOException {
		ResponseWrapper<ServiceError> responseWrapper = setErrors(httpServletRequest);
		responseWrapper.getErrors().addAll(exception.getList());
		return new ResponseEntity<>(responseWrapper, HttpStatus.OK);
	}
	
	/**
	 * This method handles MethodArgumentNotValidException type of exceptions.
	 * 
	 * @param httpServletRequest the request
	 * @param exception          The exception
	 * @return The response entity.
	 * @throws IOException the IOException
	 */
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ResponseWrapper<ServiceError>> smsInvalidInputsFound(
			final HttpServletRequest httpServletRequest, final MethodArgumentNotValidException exception)
			throws IOException {
		ResponseWrapper<ServiceError> responseWrapper = setErrors(httpServletRequest);
		BindingResult bindingResult = exception.getBindingResult();
		final List<FieldError> fieldErrors = bindingResult.getFieldErrors();
		fieldErrors.forEach(x -> {
			ServiceError error = new ServiceError(SmsExceptionConstant.SMS_ILLEGAL_INPUT.getErrorCode(),
					x.getField() + WHITESPACE + x.getDefaultMessage());
			responseWrapper.getErrors().add(error);
		});
		return new ResponseEntity<>(responseWrapper, HttpStatus.OK);
	}

	/**
	 * @param httpServletRequest the servlet request.
	 * @param exception          the exception.
	 * @return the error response.
	 * @throws IOException when the response is not mapped.
	 */
	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ResponseWrapper<ServiceError>> onHttpMessageNotReadable(
			final HttpServletRequest httpServletRequest, final HttpMessageNotReadableException exception)
			throws IOException {
		ResponseWrapper<ServiceError> responseWrapper = setErrors(httpServletRequest);
		ServiceError error = new ServiceError(MailNotifierArgumentErrorConstants.REQUEST_DATA_NOT_VALID.getErrorCode(),
				exception.getMessage());
		responseWrapper.getErrors().add(error);
		return new ResponseEntity<>(responseWrapper, HttpStatus.OK);
	}

	/**
	 * @param httpServletRequest the servlet request.
	 * @param exception          the exception.
	 * @return the error response.
	 * @throws IOException when the response is not mapped.
	 */
	@ExceptionHandler(value = { Exception.class, RuntimeException.class })
	public ResponseEntity<ResponseWrapper<ServiceError>> defaultErrorHandler(
			final HttpServletRequest httpServletRequest, Exception exception) throws IOException {
		ResponseWrapper<ServiceError> responseWrapper = setErrors(httpServletRequest);
		ServiceError error = new ServiceError(MailNotifierArgumentErrorConstants.INTERNAL_SERVER_ERROR.getErrorCode(),
				exception.getMessage());
		responseWrapper.getErrors().add(error);
		ExceptionUtils.logRootCause(exception);
		return new ResponseEntity<>(responseWrapper, HttpStatus.INTERNAL_SERVER_ERROR);
	}

	/**
	 * This method sets the error response.
	 * 
	 * @param httpServletRequest the servlet request.
	 * @return the error response wrapped in {@link ResponseWrapper}.
	 * @throws IOException when the response is not mapped.
	 */
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
}
