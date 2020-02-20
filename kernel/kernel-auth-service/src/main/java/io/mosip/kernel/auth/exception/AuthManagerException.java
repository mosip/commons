
package io.mosip.kernel.auth.exception;

/**
 * @author Ramadurai Pandian
 *
 */
public class AuthManagerException extends RuntimeException {

	/**
	 * 
	 */

	private String errorCode;

	public String getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}

	private static final long serialVersionUID = 4060346018688709387L;

	/**
	 * @param errorCode    The error code for this exception
	 * @param errorMessage The error message for this exception
	 * @param cause        The throwable cause
	 */
	public AuthManagerException(String errorCode, String errorMessage, Throwable cause) {
		super(errorCode.concat(" --> ").concat(errorMessage), cause);
	}

	/**
	 * Constructor the initialize Handler exception
	 * 
	 * @param errorCode    The error code for this exception
	 * @param errorMessage The error message for this exception
	 */
	public AuthManagerException(String errorCode, String errorMessage) {
		super(errorMessage);
		this.errorCode = errorCode;
	}
}
