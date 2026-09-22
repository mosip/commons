/*
 * 
 * 
 * 
 * 
 */
package io.mosip.kernel.core.crypto.exception;

import io.mosip.kernel.core.exception.BaseUncheckedException;

/**
 * Unchecked exception thrown when cryptographic algorithm parameters are
 * invalid.
 * <p>
 * Contract: raised for invalid IV, AAD, or algorithm parameter specs. Prefer
 * {@link io.mosip.kernel.core.crypto.constant.CryptoExceptionCodeConstants}.
 * </p>
 * 
 * @author Urvil Joshi
 * @since 1.0.0
 */
public class InvalidParamSpecException extends BaseUncheckedException {

	/**
	 * Unique id for serialization
	 */
	private static final long serialVersionUID = 1650218542197755276L;

	/**
	 * Constructor with errorCode and errorMessage
	 * 
	 * @param errorCode    The error code for this exception
	 * @param errorMessage The error message for this exception
	 */
	public InvalidParamSpecException(String errorCode, String errorMessage) {
		super(errorCode, errorMessage);
	}

	/**
	 * Constructor with errorCode, errorMessage, and rootCause
	 * 
	 * @param errorCode    The error code for this exception
	 * @param errorMessage The error message for this exception
	 * @param rootCause    Cause of this exception
	 */
	public InvalidParamSpecException(String errorCode, String errorMessage, Throwable rootCause) {
		super(errorCode, errorMessage, rootCause);
	}

}
