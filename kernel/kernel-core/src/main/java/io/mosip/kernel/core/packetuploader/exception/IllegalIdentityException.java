package io.mosip.kernel.core.packetuploader.exception;

import io.mosip.kernel.core.exception.BaseUncheckedException;

/**
 * Unchecked exception thrown when the SFTP private-key identity is invalid.
 * <p>
 * Contract: raised when the configured key cannot be loaded or does not match
 * the server. Does not retry automatically.
 * </p>
 *
 * @author Urvil Joshi
 * @since 1.0.0
 */
public class IllegalIdentityException extends BaseUncheckedException {

	/**
	 * Unique id for serialization
	 */
	private static final long serialVersionUID = -7665593898258210837L;

	/**
	 * Constructor with errorCode, errorMessage, and rootCause
	 * 
	 * @param errorCode    The error code for this exception
	 * @param errorMessage The error message for this exception
	 * @param cause        Cause of this exception
	 */
	public IllegalIdentityException(String errorCode, String errorMessage, Throwable cause) {
		super(errorCode, errorMessage, cause);
	}

}
