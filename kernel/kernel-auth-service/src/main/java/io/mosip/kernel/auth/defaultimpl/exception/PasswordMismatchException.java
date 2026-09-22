package io.mosip.kernel.auth.defaultimpl.exception;

import io.mosip.kernel.core.exception.BaseUncheckedException;

/**
 * Raised when the supplied password does not match the stored credential
 * (change-password / verify flows).
 */
public class PasswordMismatchException extends BaseUncheckedException {

	/**
	 * Serialization identifier.
	 */
	private static final long serialVersionUID = -4849582696173207678L;

	/**
	 * MOSIP error code for this mismatch.
	 */

	private String errorCode;

	/**
	 * Returns the MOSIP error code.
	 *
	 * @return error code
	 */
	public String getErrorCode() {
		return errorCode;
	}

	/**
	 * Sets the MOSIP error code.
	 *
	 * @param errorCode MOSIP error code
	 */
	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}

	/**
	 * Constructor the initialize Handler exception
	 * 
	 * @param errorCode    The error code for this exception
	 * @param errorMessage The error message for this exception
	 */
	public PasswordMismatchException(String errorCode, String errorMessage) {
		super(errorMessage);
		this.errorCode = errorCode;
	}

}
