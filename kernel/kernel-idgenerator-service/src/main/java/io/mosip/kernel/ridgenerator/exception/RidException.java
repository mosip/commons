package io.mosip.kernel.ridgenerator.exception;

import io.mosip.kernel.core.exception.BaseUncheckedException;

/**
 * Unchecked failure while fetching or updating the RID sequence.
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
	 * Instantiates the exception with a MOSIP error code, message, and cause.
	 *
	 * @param errorCode    MOSIP error code
	 * @param errorMessage MOSIP error message
	 * @param rootCause    underlying persistence or runtime cause
	 */
	public RidException(String errorCode, String errorMessage, Throwable rootCause) {
		super(errorCode, errorMessage, rootCause);
	}

}
