/*
 * 
 * 
 * 
 * 
 */
package io.mosip.kernel.core.crypto.exception;

import io.mosip.kernel.core.exception.BaseUncheckedException;

/**
 * Unchecked exception thrown when digital-signature create or verify fails.
 * <p>
 * Contract: raised by {@link io.mosip.kernel.core.crypto.spi.CryptoCoreSpec}
 * {@code sign} / {@code verifySignature} implementations. Prefer
 * {@link io.mosip.kernel.core.crypto.constant.CryptoExceptionCodeConstants}.
 * </p>
 * 
 * @author Urvil Joshi
 * @since 1.0.0
 */
public class SignatureException extends BaseUncheckedException {

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
	public SignatureException(String errorCode, String errorMessage) {
		super(errorCode, errorMessage);
	}

	/**
	 * Constructor with errorCode, errorMessage, and rootCause
	 * 
	 * @param errorCode    The error code for this exception
	 * @param errorMessage The error message for this exception
	 * @param rootCause    Cause of this exception
	 */
	public SignatureException(String errorCode, String errorMessage, Throwable rootCause) {
		super(errorCode, errorMessage, rootCause);
	}

}
