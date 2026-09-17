package io.mosip.kernel.core.util.exception;

import io.mosip.kernel.core.exception.BaseUncheckedException;

/**
 * Unchecked exception thrown when a numeric argument exceeds the allowed range.
 * <p>
 * Contract: raised by {@link io.mosip.kernel.core.util.MathUtils} when a value
 * is larger than the operation permits.
 * </p>
 *
 * @author Ritesh Sinha
 * @since 1.0.0
 */
public class NumberIsTooLargeException extends BaseUncheckedException {

	/** Serializable version Id. */
	private static final long serialVersionUID = 424322202100630614L;

	/**
	 * Constructs the exception with MOSIP error code, message, and cause.
	 *
	 * @param arg0 never-null MOSIP error code
	 * @param arg1 never-null human-readable description
	 * @param arg2 underlying cause; may be null
	 */
	public NumberIsTooLargeException(String arg0, String arg1, Throwable arg2) {
		super(arg0, arg1, arg2);
	}

}
