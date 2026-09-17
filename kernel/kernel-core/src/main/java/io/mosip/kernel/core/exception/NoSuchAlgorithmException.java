package io.mosip.kernel.core.exception;

/**
 * Unchecked MOSIP wrapper when a requested cryptographic or hash algorithm is
 * unavailable on the JRE.
 * <p>
 * Contract: wraps {@code java.security.NoSuchAlgorithmException}. Raised by
 * HMAC, crypto, and hash utilities.
 * </p>
 *
 * @author Urvil Joshi
 * @author Omsaieswar Mulakaluri
 * @since 1.0.0
 */
public class NoSuchAlgorithmException extends BaseUncheckedException {

	/**
	 * Unique id for serialization
	 */
	private static final long serialVersionUID = 8768923778001408221L;

	/**
	 * Constructs the exception with MOSIP error code and message.
	 *
	 * @param errorCode    never-null MOSIP error code
	 * @param errorMessage never-null human-readable description
	 */
	public NoSuchAlgorithmException(String errorCode, String errorMessage) {
		super(errorCode, errorMessage);

	}

	/**
	 * Constructs the exception with MOSIP error code, message, and cause.
	 *
	 * @param errorCode    never-null MOSIP error code
	 * @param errorMessage never-null human-readable description
	 * @param cause        underlying cause; may be null
	 */
	public NoSuchAlgorithmException(String errorCode, String errorMessage, Throwable cause) {
		super(errorCode, errorMessage, cause);

	}

}
