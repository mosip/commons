package io.mosip.kernel.core.exception;

/**
 * Unchecked MOSIP wrapper thrown when an array or string index is out of
 * range.
 * <p>
 * Contract: raised by {@link io.mosip.kernel.core.util.StringUtils} and similar
 * utilities when an index is negative or {@code >=} length.
 * </p>
 *
 * @author Sidhant Agarwal
 * @since 1.0.0
 */
public class ArrayIndexOutOfBoundsException extends BaseUncheckedException {
	/** Serializable version Id. */
	private static final long serialVersionUID = 522722202113670628L;

	/**
	 * Constructs an index exception with MOSIP error code, message, and cause.
	 *
	 * @param errorCode    never-null MOSIP error code
	 * @param errorMessage never-null human-readable description
	 * @param rootCause    underlying cause; may be null
	 */
	public ArrayIndexOutOfBoundsException(String errorCode, String errorMessage, Throwable rootCause) {
		super(errorCode, errorMessage, rootCause);
	}

}
