package io.mosip.kernel.core.templatemanager.exception;

import io.mosip.kernel.core.exception.BaseUncheckedException;

/**
 * Unchecked exception thrown when a template resource cannot be found.
 * <p>
 * Contract: raised when {@code templateName} does not resolve under the
 * configured loader and path. Consult runtime logs for the searched location.
 * </p>
 *
 * @author Abhishek Kumar
 * @version 1.0.0
 * @since 2018-10-1
 */
public class TemplateResourceNotFoundException extends BaseUncheckedException {

	private static final long serialVersionUID = 3070414901455295210L;

	/**
	 * Constructs the exception with MOSIP error code and message.
	 *
	 * @param errorCode    never-null MOSIP error code
	 * @param errorMessage never-null human-readable description
	 */
	public TemplateResourceNotFoundException(String errorCode, String errorMessage) {
		super(errorCode, errorMessage);
	}

	/**
	 * Constructs the exception with MOSIP error code, message, and cause.
	 *
	 * @param errorCode    never-null MOSIP error code
	 * @param errorMessage never-null human-readable description
	 * @param rootCause    loader cause; may be null
	 */
	public TemplateResourceNotFoundException(String errorCode, String errorMessage, Throwable rootCause) {
		super(errorCode, errorMessage, rootCause);
	}

}
