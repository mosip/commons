package io.mosip.kernel.idgenerator.rid.exception;

import io.mosip.kernel.core.exception.BaseUncheckedException;

/**
 * rid exception
 * 
 * @author Abhishek Kumar
 * @since 1.0.0
 */
public class RidException extends BaseUncheckedException {

	/**
	 * generated rid exception
	 */
	private static final long serialVersionUID = 4207046836454691003L;

	/**
	 * Creates an empty exception.
	 */
	public RidException() {
		super();
	}

	/**
	 * Creates an exception with code, message, and cause.
	 *
	 * @param errorCode    MOSIP error code
	 * @param errorMessage human-readable message
	 * @param rootCause    underlying cause
	 */
	public RidException(String errorCode, String errorMessage, Throwable rootCause) {
		super(errorCode, errorMessage, rootCause);
	}

	/**
	 * Creates an exception with code and message.
	 *
	 * @param errorCode    MOSIP error code
	 * @param errorMessage human-readable message
	 */
	public RidException(String errorCode, String errorMessage) {
		super(errorCode, errorMessage);
	}

	/**
	 * Creates an exception with a message only.
	 *
	 * @param errorMessage human-readable message
	 */
	public RidException(String errorMessage) {
		super(errorMessage);
	}

}
