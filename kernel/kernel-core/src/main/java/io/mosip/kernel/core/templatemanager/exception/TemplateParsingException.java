package io.mosip.kernel.core.templatemanager.exception;

import io.mosip.kernel.core.exception.BaseUncheckedException;

/**
 * Unchecked exception thrown when a template cannot be parsed.
 * <p>
 * Contract: raised by {@link io.mosip.kernel.core.templatemanager.spi.TemplateManager}
 * when the template has syntax errors. Consult runtime logs for engine details.
 * </p>
 *
 * @author Abhishek Kumar
 * @version 1.0.0
 * @since 2018-10-4
 */
public class TemplateParsingException extends BaseUncheckedException {

	private static final long serialVersionUID = 1368132089641129425L;

	/**
	 * Constructs the exception with MOSIP error code and message.
	 *
	 * @param errorCode    never-null MOSIP error code
	 * @param errorMessage never-null human-readable description
	 */
	public TemplateParsingException(String errorCode, String errorMessage) {
		super(errorCode, errorMessage);
	}

	/**
	 * Constructs the exception with MOSIP error code, message, and cause.
	 *
	 * @param errorCode    never-null MOSIP error code
	 * @param errorMessage never-null human-readable description
	 * @param rootCause    parse engine cause; may be null
	 */
	public TemplateParsingException(String errorCode, String errorMessage, Throwable rootCause) {
		super(errorCode, errorMessage, rootCause);
	}

}
