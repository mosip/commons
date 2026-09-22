
package io.mosip.kernel.auth.defaultimpl.exception;

/**
 * Unchecked failure from authmanager business logic (validation, IAM, tokens).
 * Carries a MOSIP {@code KER-ATH-*} {@link #errorCode} for the HTTP error wrapper.
 *
 * @author Ramadurai Pandian
 *
 */
public class AuthManagerException extends RuntimeException {

	/**
	 * MOSIP error code associated with this failure.
	 */

	private String errorCode;

	/**
	 * Returns the MOSIP error code.
	 *
	 * @return error code, or {@code null} if only the chained constructor was used
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
	 * Serialization identifier.
	 */
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
