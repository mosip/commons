package io.mosip.kernel.core.exception;

/**
 * Unchecked MOSIP wrapper for an invalid regular-expression pattern.
 * <p>
 * Contract: raised by {@link io.mosip.kernel.core.util.StringUtils} when a
 * regex cannot be compiled.
 * </p>
 *
 * @author Sidhant Agarwal
 * @since 1.0.0
 */
public class PatternSyntaxException extends BaseUncheckedException {
	/** Serializable version Id. */
	private static final long serialVersionUID = 123456202110630628L;

	/**
	 * Constructs the exception with MOSIP error code and message.
	 *
	 * @param errorCode    never-null MOSIP error code
	 * @param errorMessage never-null human-readable description
	 */
	public PatternSyntaxException(String errorCode, String errorMessage) {
		super(errorCode, errorMessage);
	}

	/**
	 * Constructs the exception with MOSIP error code, message, and cause.
	 *
	 * @param errorCode    never-null MOSIP error code
	 * @param errorMessage never-null human-readable description
	 * @param rootCause    underlying cause; may be null
	 */
	public PatternSyntaxException(String errorCode, String errorMessage, Throwable rootCause) {
		super(errorCode, errorMessage, rootCause);
	}

}
