/*
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 */
package io.mosip.kernel.core.crypto.exception;

import io.mosip.kernel.core.exception.BaseUncheckedException;

/**
 * Unchecked exception thrown when a cryptographic key is invalid.
 * <p>
 * Contract: raised when key material fails validation or is the wrong type
 * (public vs private). Prefer
 * {@link io.mosip.kernel.core.crypto.constant.CryptoExceptionCodeConstants}.
 * </p>
 * 
 * @author Urvil Joshi
 * @since 1.0.0
 */
public class InvalidKeyException extends BaseUncheckedException {

	/**
	 * Unique id for serialization
	 */
	private static final long serialVersionUID = -3556229489431119187L;

	/**
	 * Constructor with errorCode and errorMessage
	 * 
	 * @param errorCode    The error code for this exception
	 * @param errorMessage The error message for this exception
	 */
	public InvalidKeyException(String errorCode, String errorMessage) {
		super(errorCode, errorMessage);
	}

	/**
	 * Constructor with errorCode, errorMessage, and rootCause
	 * 
	 * @param errorCode    The error code for this exception
	 * @param errorMessage The error message for this exception
	 * @param rootCause    Cause of this exception
	 */
	public InvalidKeyException(String errorCode, String errorMessage, Throwable rootCause) {
		super(errorCode, errorMessage, rootCause);
	}
}