package io.mosip.kernel.bioextractor.exception;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import io.mosip.kernel.bioextractor.config.constant.BiometricExtractionErrorConstants;
import io.mosip.kernel.bioextractor.filter.BiometricExtractorFilter;
import io.mosip.kernel.bioextractor.logger.BioExtractorLogger;
import io.mosip.kernel.core.exception.BaseCheckedException;
import io.mosip.kernel.core.exception.ExceptionUtils;
import io.mosip.kernel.core.exception.ServiceError;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.logger.spi.Logger;

@RestControllerAdvice
public class BiometricExtractionExceptionHandler {

	@Autowired
	private HttpServletRequest servletRequest;

	private static final Logger LOGGER = BioExtractorLogger.getLogger(BiometricExtractionExceptionHandler.class);

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
		LOGGER.error("", this.getClass().getSimpleName(), "handleAllExceptions",
				ex.getMessage() + "\n" + ExceptionUtils.getStackTrace(ex));
		BiometricExtractionException e = new BiometricExtractionException(
				BiometricExtractionErrorConstants.UNKNOWN_ERROR, ex);
		return new ResponseEntity<>(buildExceptionResponse(e, servletRequest), HttpStatus.OK);
	}

	/**
	 * Handle all exceptions.
	 *
	 * @param ex      the ex
	 * @param request the request
	 * @return the response entity
	 */
	@ExceptionHandler(BiometricExtractionException.class)
	protected ResponseEntity<Object> handleBiometricExtractionExceptions(BiometricExtractionException ex,
			WebRequest request) {
		LOGGER.error("", this.getClass().getSimpleName(), "handleAllExceptions",
				ex.getMessage() + "\n" + ExceptionUtils.getStackTrace(ex));
		return new ResponseEntity<>(buildExceptionResponse(ex, servletRequest), HttpStatus.OK);
	}

	private ResponseWrapper<?> buildExceptionResponse(BiometricExtractionException ex,
			HttpServletRequest servletRequest2) {
		ResponseWrapper<?> responseWrapper = new ResponseWrapper<>();
		List<ServiceError> errors = getErrors(ex);
		responseWrapper.setErrors(errors);
		return responseWrapper;
	}

	public static List<ServiceError> getErrors(Exception ex) {
		List<ServiceError> errors;
		if (ex instanceof BiometricExtractionException) {
			BiometricExtractionException baseException = (BiometricExtractionException) ex;
			List<String> errorCodes = ((BaseCheckedException) ex).getCodes();
			List<String> errorMessages = ((BaseCheckedException) ex).getErrorTexts();

			// Retrived error codes and error messages are in reverse order.
			Collections.reverse(errorCodes);
			Collections.reverse(errorMessages);
			if (ex instanceof DataValidationException) {
				DataValidationException validationException = (DataValidationException) ex;
				List<Object[]> args = validationException.getArgs();
				errors = IntStream.range(0, errorCodes.size()).mapToObj(i -> {
					String errorMessage;
					if (args != null && !args.isEmpty()) {
						if (args.get(i) != null) {
							errorMessage = String.format(errorMessages.get(i), args.get(i));
						} else {
							errorMessage = errorMessages.get(i);
						}
					} else {
						errorMessage = errorMessages.get(i);
					}

					return createError(validationException, errorMessage);
				}).distinct().collect(Collectors.toList());
			} else {
				errors = IntStream.range(0, errorCodes.size()).mapToObj(i -> createError(baseException, null))
						.distinct().collect(Collectors.toList());
			}

		} else {
			errors = Collections.emptyList();
		}

		return errors;
	}

	private static ServiceError createError(BaseCheckedException validationException, String errorMessage) {
		String errorText = errorMessage != null ? errorMessage : validationException.getErrorText();
		return new ServiceError(validationException.getErrorCode(), errorText);
	}

}
