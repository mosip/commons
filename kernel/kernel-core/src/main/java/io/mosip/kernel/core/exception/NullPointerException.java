package io.mosip.kernel.core.exception;

/**
 * Unchecked MOSIP wrapper thrown when a required argument is null.
 * <p>
 * Contract: raised by kernel utilities when a never-null parameter is null.
 * Treat as a client-input error.
 * </p>
 *
 * @author Urvil Joshi
 * @author Ritesh Sinha
 * @author Sagar Mahapatra
 * @author Priya Soni
 * @since 1.0.0
 */
public class NullPointerException extends BaseUncheckedException {

	/** Serializable version Id. */
	private static final long serialVersionUID = 784321102100630614L;

	/**
	 * Constructs the exception with MOSIP error code and message.
	 *
	 * @param errorCode    never-null MOSIP error code
	 * @param errorMessage never-null human-readable description
	 */
	public NullPointerException(String errorCode, String errorMessage) {
		super(errorCode, errorMessage);
	}

	/**
	 * Constructs the exception with MOSIP error code, message, and cause.
	 *
	 * @param arg0 never-null MOSIP error code
	 * @param arg1 never-null human-readable description
	 * @param arg2 underlying cause; may be null
	 */
	public NullPointerException(String arg0, String arg1, Throwable arg2) {
		super(arg0, arg1, arg2);

	}

}
