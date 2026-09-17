package io.mosip.kernel.idgenerator.rid.exception;

import io.mosip.kernel.core.exception.BaseUncheckedException;

/**
 * Exception class for inputs lengths.
 * 
 * @author Ritesh Sinha
 * @author Sidhant Agarwal
 * @since 1.0.0
 *
 */

public class InputLengthException extends BaseUncheckedException {
	private static final long serialVersionUID = 2842522173497867519L;

	/**
	 * Creates an empty exception.
	 */
	public InputLengthException() {
		super();

	}

	/**
	 * Creates an exception with code, message, and cause.
	 *
	 * @param errorCode    MOSIP error code
	 * @param errorMessage human-readable message
	 * @param rootCause    underlying cause
	 */
	public InputLengthException(String errorCode, String errorMessage, Throwable rootCause) {
		super(errorCode, errorMessage, rootCause);

	}

	/**
	 * Creates an exception with code and message.
	 *
	 * @param errorCode    MOSIP error code
	 * @param errorMessage human-readable message
	 */
	public InputLengthException(String errorCode, String errorMessage) {
		super(errorCode, errorMessage);

	}

}
