package io.mosip.kernel.auth.defaultadapter.exception;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.util.ContentCachingRequestWrapper;

import com.auth0.jwt.exceptions.JWTDecodeException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import io.mosip.kernel.auth.defaultadapter.constant.AuthAdapterErrorCode;
import io.mosip.kernel.core.authmanager.exception.AuthNException;
import io.mosip.kernel.core.authmanager.exception.AuthZException;
import io.mosip.kernel.core.exception.ExceptionUtils;
import io.mosip.kernel.core.exception.ServiceError;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.util.DateUtils2;
import io.mosip.kernel.core.util.EmptyCheckUtils;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Highest-precedence {@code @RestControllerAdvice} that maps Spring Security
 * and MOSIP auth exceptions to MOSIP {@link ResponseWrapper} JSON.
 * <p>
 * Hosting services receive a consistent error envelope for 401/403 without
 * implementing their own advice. Request {@code id} and {@code version} are
 * copied from a {@link ContentCachingRequestWrapper} body when present.
 * <p>
 * This adapter is a library other MOSIP services put on the classpath.
 */
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class AuthAdapterExceptionHandler {

	/**
	 * Mapper used to parse cached request JSON for {@code id} and {@code version}.
	 */
	@Autowired
	private ObjectMapper objectMapper;

	/**
	 * Maps {@link AuthenticationException} to HTTP 401 with
	 * {@link AuthAdapterErrorCode#UNAUTHORIZED}.
	 *
	 * @param httpServletRequest the failed request
	 * @param e                  the authentication exception
	 * @return MOSIP error wrapper with status 401
	 * @throws IOException if the cached request body cannot be read
	 */
	@ExceptionHandler(AuthenticationException.class)
	public ResponseEntity<ResponseWrapper<ServiceError>> onAuthenticationException(
			final HttpServletRequest httpServletRequest, final AuthenticationException e) throws IOException {
		ResponseWrapper<ServiceError> errorResponse = setErrors(httpServletRequest);
		ServiceError error = new ServiceError(AuthAdapterErrorCode.UNAUTHORIZED.getErrorCode(), e.getMessage());
		errorResponse.getErrors().add(error);
		ExceptionUtils.logRootCause(e);
		return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
	}

	/**
	 * Maps {@link AccessDeniedException} to HTTP 403 with
	 * {@link AuthAdapterErrorCode#FORBIDDEN}.
	 *
	 * @param httpServletRequest the failed request
	 * @param e                  the access-denied exception
	 * @return MOSIP error wrapper with status 403
	 * @throws IOException if the cached request body cannot be read
	 */
	@ExceptionHandler(AccessDeniedException.class)
	public ResponseEntity<ResponseWrapper<ServiceError>> onAccessDeniedException(
			final HttpServletRequest httpServletRequest, final AccessDeniedException e) throws IOException {
		ResponseWrapper<ServiceError> errorResponse = setErrors(httpServletRequest);
		ServiceError error = new ServiceError(AuthAdapterErrorCode.FORBIDDEN.getErrorCode(),
				AuthAdapterErrorCode.FORBIDDEN.getErrorMessage());
		errorResponse.getErrors().add(error);
		ExceptionUtils.logRootCause(e);
		return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
	}

	/**
	 * Maps {@link AuthManagerException} to HTTP 401.
	 *
	 * @param httpServletRequest the failed request
	 * @param e                  the auth-manager exception
	 * @return MOSIP error wrapper with status 401
	 * @throws IOException if the cached request body cannot be read
	 */
	@ExceptionHandler(AuthManagerException.class)
	public ResponseEntity<ResponseWrapper<ServiceError>> authManagerException(
			final HttpServletRequest httpServletRequest, final AuthManagerException e) throws IOException {
		ResponseWrapper<ServiceError> errorResponse = setErrors(httpServletRequest);
		ServiceError error = new ServiceError(AuthAdapterErrorCode.UNAUTHORIZED.getErrorCode(), e.getMessage());
		errorResponse.getErrors().add(error);
		ExceptionUtils.logRootCause(e);
		return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
	}

	/**
	 * Maps {@link AuthNException} to HTTP 401, copying its {@link ServiceError}
	 * list.
	 *
	 * @param httpServletRequest the failed request
	 * @param e                  the authentication exception from kernel-core
	 * @return MOSIP error wrapper with status 401
	 * @throws IOException if the cached request body cannot be read
	 */
	@ExceptionHandler(AuthNException.class)
	public ResponseEntity<ResponseWrapper<ServiceError>> authNException(final HttpServletRequest httpServletRequest,
			final AuthNException e) throws IOException {
		ResponseWrapper<ServiceError> errorResponse = setErrors(httpServletRequest);
		errorResponse.getErrors().addAll(e.getList());
		ExceptionUtils.logRootCause(e);
		return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
	}

	/**
	 * Maps {@link AuthZException} to HTTP 403, copying its {@link ServiceError}
	 * list.
	 *
	 * @param httpServletRequest the failed request
	 * @param e                  the authorization exception from kernel-core
	 * @return MOSIP error wrapper with status 403
	 * @throws IOException if the cached request body cannot be read
	 */
	@ExceptionHandler(AuthZException.class)
	public ResponseEntity<ResponseWrapper<ServiceError>> authZException(final HttpServletRequest httpServletRequest,
			final AuthZException e) throws IOException {
		ResponseWrapper<ServiceError> errorResponse = setErrors(httpServletRequest);
		errorResponse.getErrors().addAll(e.getList());
		ExceptionUtils.logRootCause(e);
		return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
	}

	/**
	 * Compact JWT must have three parts ({@code header.payload.signature}).
	 *
	 * @param httpServletRequest the failed request
	 * @param e                  malformed JWT
	 * @return MOSIP error wrapper with {@link AuthAdapterErrorCode#INVALID_TOKEN}
	 *         and status 401
	 * @throws IOException if the cached request body cannot be read
	 */
	@ExceptionHandler(JWTDecodeException.class)
	public ResponseEntity<ResponseWrapper<ServiceError>> jwtDecodeException(
			final HttpServletRequest httpServletRequest, final JWTDecodeException e) throws IOException {
		ResponseWrapper<ServiceError> errorResponse = setErrors(httpServletRequest);
		ServiceError error = new ServiceError(AuthAdapterErrorCode.INVALID_TOKEN.getErrorCode(),
				AuthAdapterErrorCode.INVALID_TOKEN.getErrorMessage());
		errorResponse.getErrors().add(error);
		ExceptionUtils.logRootCause(e);
		return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
	}

	/**
	 * Builds a {@link ResponseWrapper} with UTC response time and, when the
	 * request is a {@link ContentCachingRequestWrapper} with a JSON body, copies
	 * {@code id} and {@code version}.
	 *
	 * @param httpServletRequest the inbound request
	 * @return an error wrapper ready for {@link ServiceError} entries
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

}
