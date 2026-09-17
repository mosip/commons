package io.mosip.kernel.idgenerator.rid.exception;

import io.mosip.kernel.core.exception.BaseUncheckedException;

/**
 * Exception class for empty inputs.
 * 
 * @author Ritesh Sinha
 * @author Sidhant Agarwal
 * @since 1.0.0
 *
 */

public class EmptyInputException extends BaseUncheckedException {
	private static final long serialVersionUID = 2842524563494167519L;

	/**
	 * Creates an empty exception.
	 */
	public EmptyInputException() {
		super();

	}

	/**
	 * Creates an exception with code, message, and cause.
	 *
	 * @param errorCode    MOSIP error code
	 * @param errorMessage human-readable message
	 * @param rootCause    underlying cause
	 */
	public EmptyInputException(String errorCode, String errorMessage, Throwable rootCause) {
		super(errorCode, errorMessage, rootCause);

	}

	/**
	 * Creates an exception with code and message.
	 *
	 * @param errorCode    MOSIP error code
	 * @param errorMessage human-readable message
	 */
	public EmptyInputException(String errorCode, String errorMessage) {
		super(errorCode, errorMessage);

	}

}
