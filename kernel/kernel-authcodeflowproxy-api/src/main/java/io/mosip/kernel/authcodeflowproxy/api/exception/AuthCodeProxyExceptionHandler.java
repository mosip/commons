package io.mosip.kernel.authcodeflowproxy.api.exception;

import java.io.IOException;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.util.ContentCachingRequestWrapper;

import com.auth0.jwt.exceptions.JWTDecodeException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import io.mosip.kernel.core.exception.ExceptionUtils;
import io.mosip.kernel.core.exception.ServiceError;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.util.DateUtils2;
import io.mosip.kernel.core.util.EmptyCheckUtils;
import io.mosip.kernel.openid.bridge.api.constants.Errors;
import io.mosip.kernel.openid.bridge.api.exception.AuthRestException;
import io.mosip.kernel.openid.bridge.api.exception.ClientException;
import io.mosip.kernel.openid.bridge.api.exception.ServiceException;

/**
 * Highest-precedence {@link RestControllerAdvice} for the authorization-code flow proxy.
 * <p>
 * Maps {@link ClientException}, {@link ServiceException},
 * {@link AuthenticationServiceException}, and {@link AuthRestException} to MOSIP
 * {@link ResponseWrapper} error bodies, copying {@code id} and {@code version} from a
 * cached request body when the request is a {@link ContentCachingRequestWrapper}.
 */
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class AuthCodeProxyExceptionHandler {

	/**
	 * JSON mapper used to read {@code id} and {@code version} from the cached request body.
	 */
	@Autowired
	private ObjectMapper objectMapper;

	
	/**
	 * Handles client-side authorization-code flow errors (missing cookie/token, state
	 * mismatch, ID-token correlation failure).
	 *
	 * @param httpServletRequest current request (used to populate wrapper metadata)
	 * @param e client exception whose MOSIP error code and text are returned
	 * @return HTTP 200 with a {@link ResponseWrapper} containing one {@link ServiceError}
	 * @throws IOException if the cached request body cannot be parsed
	 */
	@ExceptionHandler(ClientException.class)
	public ResponseEntity<ResponseWrapper<ServiceError>> clientException(
			HttpServletRequest httpServletRequest, final ClientException e) throws IOException {
		ExceptionUtils.logRootCause(e);
		return new ResponseEntity<>(
				getErrorResponse(httpServletRequest, e.getErrorCode(), e.getErrorText()), HttpStatus.OK);
	}
	
	/**
	 * Handles service-side authorization-code flow errors (allow-list, state UUID, Keycloak
	 * token exchange, JWT sign, encoding).
	 * <p>
	 * Uses HTTP 401 when the error code is {@link Errors#INVALID_TOKEN}; otherwise HTTP 200
	 * with the error in the body (MOSIP API convention).
	 *
	 * @param httpServletRequest current request (used to populate wrapper metadata)
	 * @param e service exception whose MOSIP error code and text are returned
	 * @return HTTP 401 for invalid token, otherwise HTTP 200, with a {@link ResponseWrapper}
	 *         containing one {@link ServiceError}
	 * @throws IOException if the cached request body cannot be parsed
	 */
	@ExceptionHandler(ServiceException.class)
	public ResponseEntity<ResponseWrapper<ServiceError>> servieException(
			HttpServletRequest httpServletRequest, final ServiceException e) throws IOException {
		ExceptionUtils.logRootCause(e);
		HttpStatus status;
		if(e.getErrorCode().equals(Errors.INVALID_TOKEN.getErrorCode())) {
			status = HttpStatus.UNAUTHORIZED;
		} else {
			status = HttpStatus.OK;
		}
		return new ResponseEntity<>(
				getErrorResponse(httpServletRequest, e.getErrorCode(), e.getErrorText()), status);
	}
	
	/**
	 * Handles missing/empty token on logout ({@link AuthenticationServiceException} thrown
	 * by {@code LoginServiceImpl#logoutUser}).
	 *
	 * @param httpServletRequest current request (used to populate wrapper metadata)
	 * @param e authentication failure whose message is used as the error text
	 * @return HTTP 200 with {@link Errors#INVALID_TOKEN} as the error code
	 * @throws IOException if the cached request body cannot be parsed
	 */
	@ExceptionHandler(AuthenticationServiceException.class)
	public ResponseEntity<ResponseWrapper<ServiceError>> servieException(
			HttpServletRequest httpServletRequest, final AuthenticationServiceException e) throws IOException {
		ExceptionUtils.logRootCause(e);
		return new ResponseEntity<>(
				getErrorResponse(httpServletRequest,Errors.INVALID_TOKEN.getErrorCode(), e.getMessage()), HttpStatus.OK);
	}

	/**
	 * Handles auth-manager or offline-validation failures that already carry a list of
	 * {@link ServiceError}s and an HTTP status.
	 *
	 * @param httpServletRequest current request (used to populate wrapper metadata)
	 * @param exception REST validation exception whose errors and status are forwarded
	 * @return response entity with {@code exception.getHttpStatus()} and all listed errors
	 * @throws IOException if the cached request body cannot be parsed
	 */
	@ExceptionHandler(AuthRestException.class)
	public ResponseEntity<ResponseWrapper<ServiceError>> authRestException(
			HttpServletRequest httpServletRequest, final AuthRestException exception) throws IOException {
		ExceptionUtils.logRootCause(exception);
		ResponseWrapper<ServiceError> errorResponse = setErrors(httpServletRequest);
		errorResponse.getErrors().addAll(exception.getList());
		return new ResponseEntity<>(errorResponse, exception.getHttpStatus());
	}

	/**
	 * Compact JWT must have three parts ({@code header.payload.signature}).
	 *
	 * @param httpServletRequest current request
	 * @param e                  malformed JWT
	 * @return HTTP 401 with {@link Errors#INVALID_TOKEN}
	 * @throws IOException if the cached request body cannot be parsed
	 */
	@ExceptionHandler(JWTDecodeException.class)
	public ResponseEntity<ResponseWrapper<ServiceError>> jwtDecodeException(
			HttpServletRequest httpServletRequest, final JWTDecodeException e) throws IOException {
		ExceptionUtils.logRootCause(e);
		return new ResponseEntity<>(
				getErrorResponse(httpServletRequest, Errors.INVALID_TOKEN.getErrorCode(),
						Errors.INVALID_TOKEN.getErrorMessage()),
				HttpStatus.UNAUTHORIZED);
	}

	
	/**
	 * Builds an empty MOSIP {@link ResponseWrapper} with UTC {@code responsetime} and, when
	 * the request is a {@link ContentCachingRequestWrapper} with a body, copies {@code id}
	 * and {@code version} from that JSON.
	 *
	 * @param httpServletRequest current request
	 * @return wrapper with metadata filled when a JSON body is present
	 * @throws IOException if the cached body is not valid JSON
	 */
	private ResponseWrapper<ServiceError> setErrors(HttpServletRequest httpServletRequest) throws IOException {
		ResponseWrapper<ServiceError> responseWrapper = new ResponseWrapper<>();
		responseWrapper.setResponsetime(DateUtils2.getUTCCurrentDateTime());
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
	 * Wraps a single MOSIP {@link ServiceError} in a {@link ResponseWrapper} populated by
	 * {@link #setErrors(HttpServletRequest)}.
	 *
	 * @param httpServletRequest current request
	 * @param errorCode MOSIP error code
	 * @param errorMessage MOSIP error message
	 * @return wrapper containing one error
	 * @throws IOException if the cached request body cannot be parsed
	 */
	private ResponseWrapper<ServiceError> getErrorResponse(HttpServletRequest httpServletRequest, String errorCode,
			String errorMessage) throws IOException {
		ServiceError error = new ServiceError(errorCode, errorMessage);
		ResponseWrapper<ServiceError> errorResponse = setErrors(httpServletRequest);
		errorResponse.getErrors().add(error);
		return errorResponse;
	}
}
