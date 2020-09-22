package io.mosip.kernel.keymanagerservice.exception;

import io.mosip.kernel.core.exception.BaseUncheckedException;

/**
 * Custom Exception Class in case of Invalid Key Generation Response object type
 * 
 * @author Mahammed Taheer
 * @since 1.0.10
 *
 */
public class InvalidResponseObjectTypeException extends BaseUncheckedException {

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
	public InvalidResponseObjectTypeException(String errorCode, String errorMessage) {
		super(errorCode, errorMessage);
	}

}
