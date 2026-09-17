package io.mosip.kernel.core.exception;

/**
 * Unchecked MOSIP wrapper thrown when a date, number, or similar value cannot
 * be parsed.
 * <p>
 * Contract: raised by {@link io.mosip.kernel.core.util.DateUtils} when a
 * string does not match the expected pattern.
 * </p>
 *
 * @author Bal Vikash Sharma
 * @since 1.0.0
 */
public class ParseException extends BaseUncheckedException {
	private static final long serialVersionUID = 924722202110630628L;

	/**
	 * Constructs the exception with MOSIP error code and message.
	 *
	 * @param errorCode    never-null MOSIP error code
	 * @param errorMessage never-null human-readable description
	 */
	public ParseException(String errorCode, String errorMessage) {
		super(errorCode, errorMessage);

	}

	/**
	 * Constructs the exception with MOSIP error code, message, and cause.
	 *
	 * @param errorCode    never-null MOSIP error code
	 * @param errorMessage never-null human-readable description
	 * @param cause        underlying cause; may be null
	 */
	public ParseException(String errorCode, String errorMessage, Throwable cause) {
		super(errorCode, errorMessage, cause);

	}

}
