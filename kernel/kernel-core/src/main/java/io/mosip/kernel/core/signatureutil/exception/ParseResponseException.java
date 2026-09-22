package io.mosip.kernel.core.signatureutil.exception;

import io.mosip.kernel.core.exception.BaseUncheckedException;

/**
 * Unchecked exception thrown when a keymanager signature response cannot be parsed.
 * <p>
 * Contract: raised when JSON or date fields in the HTTP response are malformed.
 * Does not retry the remote call.
 * </p>
 *
 * @author Srinivasan
 */
public class ParseResponseException extends BaseUncheckedException {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 3383837827871687253L;

	/**
	 * Constructs the exception with MOSIP error code, message, and cause.
	 *
	 * @param errorCode    never-null MOSIP error code
	 * @param errorMessage never-null human-readable description
	 * @param rootCause    parse failure; may be null
	 */
	public ParseResponseException(String errorCode, String errorMessage, Throwable rootCause) {
		super(errorCode, errorMessage, rootCause);

	}

	/**
	 * Constructs the exception with MOSIP error code and message.
	 *
	 * @param errorCode    never-null MOSIP error code
	 * @param errorMessage never-null human-readable description
	 */
	public ParseResponseException(String errorCode, String errorMessage) {
		super(errorCode, errorMessage);

	}

}
