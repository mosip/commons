package io.mosip.kernel.websub.api.exception;

import io.mosip.kernel.core.exception.BaseUncheckedException;

/**
 * This class act as a generic exception for this api.
 * 
 * @author Urvil Joshi
 *
 */
public class WebSubClientException extends BaseUncheckedException {

	/**
	 * Generated serial version id
	 */
	private static final long serialVersionUID = 8621530697947108810L;

	/**
	 * Constructor the initialize Handler exception
	 * 
	 * @param errorCode    The errorcode for this exception
	 * @param errorMessage The error message for this exception
	 */
	public WebSubClientException(String errorCode, String errorMessage) {
		super(errorCode, errorMessage);
	}

	/**
	 * @param errorCode    The errorcode for this exception
	 * @param errorMessage The error message for this exception
	 * @param rootCause    cause of the error occoured
	 */
	public WebSubClientException(String errorCode, String errorMessage, Throwable rootCause) {
		super(errorCode, errorMessage, rootCause);
	}

}
