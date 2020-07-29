package io.mosip.kernel.bioextractor.exception;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import io.mosip.kernel.bioextractor.config.constant.BiometricExtractionErrorConstants;
import io.mosip.kernel.bioextractor.filter.BiometricExtractorFilter;
import io.mosip.kernel.core.exception.ServiceError;
import io.mosip.kernel.core.http.ResponseWrapper;

@RestControllerAdvice
public class BiometricExtractionExceptionHandler {
	
	@Autowired
	private HttpServletRequest servletRequest;
	
	private static final Logger LOGGER = LoggerFactory.getLogger(BiometricExtractorFilter.class);

	public BiometricExtractionExceptionHandler() {
	}
	
	/**
	 * Handle all exceptions.
	 *
	 * @param ex      the ex
	 * @param request the request
	 * @return the response entity
	 */
	@ExceptionHandler(Exception.class)
	protected ResponseEntity<Object> handleAllExceptions(Exception ex, WebRequest request) {
		LOGGER.debug(ex.getMessage(), ex);
		BiometricExtractionException e = new BiometricExtractionException(BiometricExtractionErrorConstants.UNKNOWN_ERROR, ex);
		return new ResponseEntity<>(buildExceptionResponse(e , servletRequest), HttpStatus.OK);
	}
	
	/**
	 * Handle all exceptions.
	 *
	 * @param ex      the ex
	 * @param request the request
	 * @return the response entity
	 */
	@ExceptionHandler(BiometricExtractionException.class)
	protected ResponseEntity<Object> handleBiometricExtractionExceptions(BiometricExtractionException ex, WebRequest request) {
		LOGGER.debug(ex.getMessage(), ex);
		return new ResponseEntity<>(buildExceptionResponse(ex, servletRequest), HttpStatus.OK);
	}

	private ResponseWrapper<?> buildExceptionResponse(BiometricExtractionException ex, HttpServletRequest servletRequest2) {
		ResponseWrapper<?> responseWrapper = new ResponseWrapper<>();
		List<ServiceError> errors = new ArrayList<>();
		ServiceError e = new ServiceError(ex.getErrorCode(), ex.getErrorText());
		errors.add(e);
		responseWrapper.setErrors(errors);
		return responseWrapper;
	}

}
