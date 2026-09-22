/**
 * 
 */
package io.mosip.kernel.auth.defaultimpl.exception;

/**
 * Login/IAM authentication failure with a MOSIP {@link #errorCode}. Mapped by
 * {@link AuthManagerExceptionHandler} to HTTP 500.
 *
 * @author Ramadurai Pandian
 *
 */
public class LoginException extends RuntimeException {

	/**
	 * MOSIP error code associated with this login failure.
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
	 * Login failure with a root cause.
	 *
	 * @param errorCode    MOSIP error code
	 * @param errorMessage client-facing message
	 * @param rootCause    underlying exception
	 */
	public LoginException(String errorCode, String errorMessage, Throwable rootCause) {
		super(errorMessage, rootCause);
		this.errorCode = errorCode;
	}

	/**
	 * Serialization identifier.
	 */
	private static final long serialVersionUID = 4060346018688709387L;

	/**
	 * Constructor the initialize Handler exception
	 * 
	 * @param errorCode    The error code for this exception
	 * @param errorMessage The error message for this exception
	 */
	public LoginException(String errorCode, String errorMessage) {
		super(errorMessage);
		this.errorCode = errorCode;
	}
}
