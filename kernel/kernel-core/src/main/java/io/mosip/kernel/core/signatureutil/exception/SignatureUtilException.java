package io.mosip.kernel.core.signatureutil.exception;

import io.mosip.kernel.core.exception.BaseUncheckedException;

/**
 * Unchecked exception thrown when kernel signature sign or verify fails.
 * <p>
 * Contract: wraps keymanager or crypto failures from
 * {@link io.mosip.kernel.core.signatureutil.spi.SignatureUtil}. Callers should
 * treat the signature as not produced or not valid.
 * </p>
 *
 * @author Srinivasan
 */
public class SignatureUtilException extends BaseUncheckedException {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 6291038762313595129L;

	/**
	 * Constructs the exception with MOSIP error code, message, and cause.
	 *
	 * @param errorCode    never-null MOSIP error code
	 * @param errorMessage never-null human-readable description
	 * @param rootCause    underlying cause; may be null
	 */
	public SignatureUtilException(String errorCode, String errorMessage, Throwable rootCause) {
		super(errorCode, errorMessage, rootCause);

	}

	/**
	 * Constructs the exception with MOSIP error code and message.
	 *
	 * @param errorCode    never-null MOSIP error code
	 * @param errorMessage never-null human-readable description
	 */
	public SignatureUtilException(String errorCode, String errorMessage) {
		super(errorCode, errorMessage);

	}
}
