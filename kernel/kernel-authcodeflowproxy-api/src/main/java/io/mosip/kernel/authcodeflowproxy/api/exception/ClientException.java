
package io.mosip.kernel.authcodeflowproxy.api.exception;

import io.mosip.kernel.core.exception.BaseUncheckedException;

public class ClientException extends BaseUncheckedException {

	private static final long serialVersionUID = 4060346018688709387L;

	/**
	 * Constructor the initialize Handler exception
	 * 
	 * @param errorCode    The error code for this exception
	 * @param errorMessage The error message for this exception
	 */
	public ClientException(String errorCode, String errorMessage) {
		super(errorMessage, errorCode);
	}

	public ClientException(String errorCode, String errorMessage, Throwable cause) {
		super(errorMessage, errorCode, cause);
	}
}
