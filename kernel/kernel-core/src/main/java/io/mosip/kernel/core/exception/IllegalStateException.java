/*
 * 
 * 
 * 
 * 
 * 
 */
package io.mosip.kernel.core.exception;

/**
 * Unchecked exception thrown when an object is in an illegal state for the
 * requested operation.
 * <p>
 * Contract: historically used when a logger filename pattern has an invalid
 * date format; also raised for other illegal-state cases in kernel utilities.
 * </p>
 *
 * @author Urvil Joshi
 * @since 1.0.0
 */
public class IllegalStateException extends BaseUncheckedException {

	private static final long serialVersionUID = 105555532L;

	/**
	 * Constructs the exception with MOSIP error code and message.
	 *
	 * @param errorCode    never-null MOSIP error code
	 * @param errorMessage never-null human-readable description
	 */
	public IllegalStateException(String errorCode, String errorMessage) {
		super(errorCode, errorMessage);
	}

	/**
	 * Constructs the exception with MOSIP error code, message, and cause.
	 *
	 * @param errorCode    never-null MOSIP error code
	 * @param errorMessage never-null human-readable description
	 * @param cause        underlying cause; may be null
	 */
	public IllegalStateException(String errorCode, String errorMessage, Throwable cause) {
		super(errorCode, errorMessage, cause);
	}

}
