package io.mosip.kernel.authcodeflowproxy.api.exception;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.util.ContentCachingRequestWrapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import io.mosip.kernel.authcodeflowproxy.api.constants.Errors;
import io.mosip.kernel.core.exception.ExceptionUtils;
import io.mosip.kernel.core.exception.ServiceError;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.util.EmptyCheckUtils;

@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class AuthCodeProxyExceptionHandler {

	@Autowired
	private ObjectMapper objectMapper;

	
	@ExceptionHandler(ClientException.class)
	public ResponseEntity<ResponseWrapper<ServiceError>> clientException(
			HttpServletRequest httpServletRequest, final ClientException e) throws IOException {
		ExceptionUtils.logRootCause(e);
		return new ResponseEntity<>(
				getErrorResponse(httpServletRequest, e.getErrorCode(), e.getErrorText()), HttpStatus.OK);
	}
	
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
	
	@ExceptionHandler(AuthenticationServiceException.class)
	public ResponseEntity<ResponseWrapper<ServiceError>> servieException(
			HttpServletRequest httpServletRequest, final AuthenticationServiceException e) throws IOException {
		ExceptionUtils.logRootCause(e);
		return new ResponseEntity<>(
				getErrorResponse(httpServletRequest,Errors.INVALID_TOKEN.getErrorCode(), e.getMessage()), HttpStatus.OK);
	}

	@ExceptionHandler(AuthRestException.class)
	public ResponseEntity<ResponseWrapper<ServiceError>> authRestException(
			HttpServletRequest httpServletRequest, final AuthRestException exception) throws IOException {
		ExceptionUtils.logRootCause(exception);
		ResponseWrapper<ServiceError> errorResponse = setErrors(httpServletRequest);
		errorResponse.getErrors().addAll(exception.getList());
		return new ResponseEntity<>(errorResponse, exception.getHttpStatus());
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
	
	private ResponseWrapper<ServiceError> getErrorResponse(HttpServletRequest httpServletRequest, String errorCode,
			String errorMessage) throws IOException {
		ServiceError error = new ServiceError(errorCode, errorMessage);
		ResponseWrapper<ServiceError> errorResponse = setErrors(httpServletRequest);
		errorResponse.getErrors().add(error);
		return errorResponse;
	}
}