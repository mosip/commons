package io.mosip.kernel.bioextractor.util;

import org.springframework.validation.Errors;

import io.mosip.kernel.bioextractor.exception.DataValidationException;

/**
 * The Class DataValidationUtil - Checks for errors in the error object
 * and throws {@link IDDataValidationException}, if any error is present.
 *
 * @author Manoj SP
 */
public final class DataValidationUtil {

	/**
	 * Instantiates a new data validation util.
	 */
	private DataValidationUtil() {
	}

	/**
	 * Get list of errors from error object and throws
	 * {@link IDDataValidationException}, if any error is present.
	 *
	 * @param errors the errors
	 * @throws IDDataValidationException the ID data validation exception
	 */
	public static void validate(Errors errors) throws DataValidationException {
		if (errors.hasErrors()) {
			DataValidationException exception = new DataValidationException();
			exception.clearArgs();
			errors.getAllErrors()
					.forEach(error -> {
						String errorCode = error.getCode();
						String errorMessage = error.getDefaultMessage();
						Object[] args = error.getArguments();
						exception.addInfo(errorCode, errorMessage, args);
					});
			throw exception;
		}
	}

}
