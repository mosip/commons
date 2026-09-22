/**
 * 
 */
package io.mosip.kernel.auth.defaultimpl.exception;

import java.io.IOException;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.util.ContentCachingRequestWrapper;

import com.auth0.jwt.exceptions.JWTDecodeException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import io.mosip.kernel.auth.defaultimpl.constant.AuthConstant;
import io.mosip.kernel.auth.defaultimpl.constant.AuthErrorCode;
import io.mosip.kernel.core.exception.ExceptionUtils;
import io.mosip.kernel.core.exception.ServiceError;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.util.EmptyCheckUtils;

/**
 * REST advice that maps authmanager exceptions to MOSIP {@link ResponseWrapper}
 * bodies. Copies {@code id} and {@code version} from the cached request JSON
 * when {@link ContentCachingRequestWrapper} is present.
 *
 * @author Ramadurai Pandian
 *
 */
@RestControllerAdvice
public class AuthManagerExceptionHandler {

	/**
	 * Mapper used to read {@code id}/{@code version} from the cached request body.
	 */
	@Autowired
	private ObjectMapper objectMapper;

	/**
	 * Bean-validation failures on {@code @RequestBody} become
	 * {@link AuthErrorCode#INVALID_REQUEST} field errors (HTTP 200 with errors array).
	 *
	 * @param httpServletRequest current request
	 * @param e                  validation exception
	 * @return wrapped field errors
	 * @throws IOException if the cached request body cannot be parsed
	 */
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ResponseWrapper<ServiceError>> methodArgumentNotValidException(
			HttpServletRequest httpServletRequest, final MethodArgumentNotValidException e) throws IOException {
		ResponseWrapper<ServiceError> errorResponse = setErrors(httpServletRequest);
		final List<FieldError> fieldErrors = e.getBindingResult().getFieldErrors();
		fieldErrors.forEach(x -> {
			ServiceError error = new ServiceError(AuthErrorCode.INVALID_REQUEST.getErrorCode(),
					x.getField() + AuthConstant.WHITESPACE + x.getDefaultMessage());
			errorResponse.getErrors().add(error);
		});
		return new ResponseEntity<>(errorResponse, HttpStatus.OK);
	}

	/**
	 * Unreadable JSON body mapped as {@link AuthErrorCode#INVALID_REQUEST}.
	 *
	 * @param httpServletRequest current request
	 * @param e                  parse/read exception
	 * @return wrapped error
	 * @throws IOException if the cached request body cannot be parsed
	 */
	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ResponseWrapper<ServiceError>> onHttpMessageNotReadable(HttpServletRequest httpServletRequest,
			final HttpMessageNotReadableException e) throws IOException {
		ResponseWrapper<ServiceError> errorResponse = setErrors(httpServletRequest);
		ServiceError error = new ServiceError(AuthErrorCode.INVALID_REQUEST.getErrorCode(), e.getMessage());
		errorResponse.getErrors().add(error);
		return new ResponseEntity<>(errorResponse, HttpStatus.OK);
	}

	/**
	 * Builds an empty error wrapper and, when a cached body exists, copies MOSIP
	 * request {@code id} and {@code version}.
	 *
	 * @param httpServletRequest current request, possibly a content-caching wrapper
	 * @return response wrapper with id/version set when possible
	 * @throws IOException if cached JSON cannot be parsed
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

	/**
	 * Maps {@link AuthManagerException} to a single {@link ServiceError} (HTTP 200).
	 *
	 * @param request   current request
	 * @param exception authmanager failure
	 * @return wrapped error
	 * @throws IOException if the cached request body cannot be parsed
	 */
	@ExceptionHandler(value = { AuthManagerException.class })
	public ResponseEntity<ResponseWrapper<ServiceError>> customErrorMessage(HttpServletRequest request,
			AuthManagerException exception) throws IOException {
		ResponseWrapper<ServiceError> responseWrapper = setErrors(request);
		ServiceError error = new ServiceError(exception.getErrorCode(), exception.getMessage());
		responseWrapper.getErrors().add(error);
		ExceptionUtils.logRootCause(exception);
		return new ResponseEntity<>(responseWrapper, HttpStatus.OK);
	}

	/**
	 * Maps {@link AuthManagerServiceException} by copying its error list (HTTP 200).
	 *
	 * @param request current request
	 * @param e       aggregated service errors
	 * @return wrapped errors
	 * @throws IOException if the cached request body cannot be parsed
	 */
	@ExceptionHandler(value = { AuthManagerServiceException.class })
	public ResponseEntity<ResponseWrapper<ServiceError>> customErrorMessageList(HttpServletRequest request,
			AuthManagerServiceException e) throws IOException {
		ResponseWrapper<ServiceError> responseWrapper = setErrors(request);
		responseWrapper.getErrors().addAll(e.getList());
		ExceptionUtils.logRootCause(e);
		return new ResponseEntity<>(responseWrapper, HttpStatus.OK);
	}

	/**
	 * Maps {@link LoginException} to HTTP 500 with a single {@link ServiceError}.
	 *
	 * @param request   current request
	 * @param exception login/IAM failure
	 * @return wrapped error
	 * @throws IOException if the cached request body cannot be parsed
	 */
	@ExceptionHandler(value = { LoginException.class })
	public ResponseEntity<ResponseWrapper<ServiceError>> loginException(HttpServletRequest request,
			LoginException exception) throws IOException {
		ResponseWrapper<ServiceError> responseWrapper = setErrors(request);
		ServiceError error = new ServiceError(exception.getErrorCode(), exception.getMessage());
		responseWrapper.getErrors().add(error);
		ExceptionUtils.logRootCause(exception);
		return new ResponseEntity<>(responseWrapper, HttpStatus.INTERNAL_SERVER_ERROR);
	}

	/**
	 * Compact JWT must have three parts ({@code header.payload.signature}). A
	 * one-part or two-part string from Auth0 {@code JWT.decode} is not a server
	 * error. Declared before {@link #defaultErrorHandler} so a malformed token
	 * cannot fall through to HTTP 500.
	 *
	 * @param request current request
	 * @param e       malformed JWT
	 * @return wrapped {@link AuthErrorCode#INVALID_TOKEN} with HTTP 401
	 * @throws IOException if the cached request body cannot be parsed
	 */
	@ExceptionHandler(JWTDecodeException.class)
	public ResponseEntity<ResponseWrapper<ServiceError>> jwtDecodeException(HttpServletRequest request,
			JWTDecodeException e) throws IOException {
		ResponseWrapper<ServiceError> responseWrapper = setErrors(request);
		ServiceError error = new ServiceError(AuthErrorCode.INVALID_TOKEN.getErrorCode(),
				AuthErrorCode.INVALID_TOKEN.getErrorMessage());
		responseWrapper.getErrors().add(error);
		ExceptionUtils.logRootCause(e);
		return new ResponseEntity<>(responseWrapper, HttpStatus.UNAUTHORIZED);
	}

	/**
	 * Fallback for uncaught exceptions (HTTP 500, code {@code 500}).
	 *
	 * @param request current request
	 * @param e       any remaining exception
	 * @return wrapped error
	 * @throws IOException if the cached request body cannot be parsed
	 */
	@ExceptionHandler(value = { Exception.class, RuntimeException.class })
	public ResponseEntity<ResponseWrapper<ServiceError>> defaultErrorHandler(HttpServletRequest request, Exception e)
			throws IOException {
		ResponseWrapper<ServiceError> responseWrapper = setErrors(request);
		ServiceError error = new ServiceError("500", e.getMessage());
		responseWrapper.getErrors().add(error);
		ExceptionUtils.logRootCause(e);
		return new ResponseEntity<>(responseWrapper, HttpStatus.INTERNAL_SERVER_ERROR);
	}

}
