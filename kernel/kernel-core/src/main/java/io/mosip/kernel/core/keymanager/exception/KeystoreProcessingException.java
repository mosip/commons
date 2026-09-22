package io.mosip.kernel.core.keymanager.exception;

import io.mosip.kernel.core.exception.BaseUncheckedException;

/**
 * Unchecked exception thrown when a keystore operation fails.
 * <p>
 * Contract: raised by {@link io.mosip.kernel.core.keymanager.spi.KeyStore}
 * on load, store, or delete failures other than a missing alias.
 * </p>
 *
 * @author Dharmesh Khandelwal
 * @since 1.0.0
 */
public class KeystoreProcessingException extends BaseUncheckedException {
	/**
	 * The generated serial version id
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Constructs the exception with MOSIP error code and message.
	 *
	 * @param errorCode    never-null MOSIP error code
	 * @param errorMessage never-null human-readable description
	 */
	public KeystoreProcessingException(String errorCode, String errorMessage) {
		super(errorCode, errorMessage);
	}

	/**
	 * Constructs the exception with MOSIP error code, message, and cause.
	 *
	 * @param errorCode    never-null MOSIP error code
	 * @param errorMessage never-null human-readable description
	 * @param rootCause    underlying cause; may be null
	 */
	public KeystoreProcessingException(String errorCode, String errorMessage, Throwable rootCause) {
		super(errorCode, errorMessage, rootCause);
	}

}
