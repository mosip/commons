package io.mosip.kernel.core.exception;

/**
 * Unchecked MOSIP wrapper for illegal method arguments.
 * <p>
 * Contract: raised by kernel utilities when a caller passes a null, empty, or
 * out-of-range argument. Treat as a client-input error.
 * </p>
 *
 * @author Ritesh Sinha
 * @author Sidhant Agarwal
 * @author Sagar Mahapatra
 * @author Ravi Balaji
 * @author Priya Soni
 * @since 1.0.0
 */
public class IllegalArgumentException extends BaseUncheckedException {
	/** Serializable version Id. */
	private static final long serialVersionUID = 924722202110630628L;

	/**
	 * Constructs the exception with MOSIP error code and message.
	 *
	 * @param errorCode    never-null MOSIP error code
	 * @param errorMessage never-null human-readable description
	 */
	public IllegalArgumentException(String errorCode, String errorMessage) {
		super(errorCode, errorMessage);

	}

	/**
	 * Constructs the exception with MOSIP error code, message, and cause.
	 *
	 * @param errorCode    never-null MOSIP error code
	 * @param errorMessage never-null human-readable description
	 * @param cause        underlying cause; may be null
	 */
	public IllegalArgumentException(String errorCode, String errorMessage, Throwable cause) {
		super(errorCode, errorMessage, cause);

	}

}
