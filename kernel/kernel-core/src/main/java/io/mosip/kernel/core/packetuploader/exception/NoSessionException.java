package io.mosip.kernel.core.packetuploader.exception;

import io.mosip.kernel.core.exception.BaseUncheckedException;

/**
 * Unchecked exception thrown when an SFTP session is missing or already closed.
 * <p>
 * Contract: raised if upload or disconnect is attempted without a live session.
 * </p>
 *
 * @author Urvil Joshi
 * @since 1.0.0
 */
public class NoSessionException extends BaseUncheckedException {

	/**
	 * Unique id for serialization
	 */
	private static final long serialVersionUID = 4820565239606121727L;

	/**
	 * Constructor with errorCode, errorMessage, and rootCause
	 * 
	 * @param errorCode    The error code for this exception
	 * @param errorMessage The error message for this exception
	 * @param cause        Cause of this exception
	 */
	public NoSessionException(String errorCode, String errorMessage, Throwable cause) {
		super(errorCode, errorMessage, cause);
	}

}
