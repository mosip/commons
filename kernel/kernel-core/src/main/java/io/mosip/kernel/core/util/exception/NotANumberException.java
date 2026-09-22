package io.mosip.kernel.core.util.exception;

import io.mosip.kernel.core.exception.BaseUncheckedException;

/**
 * Unchecked exception thrown when a numeric argument is NaN.
 * <p>
 * Contract: raised by {@link io.mosip.kernel.core.util.MathUtils} when a
 * {@code double} is not a number.
 * </p>
 *
 * @author Ritesh Sinha
 * @since 1.0.0
 */
public class NotANumberException extends BaseUncheckedException {

	/** Serializable version Id. */
	private static final long serialVersionUID = 874722202100630614L;

	/**
	 * Constructs the exception with MOSIP error code, message, and cause.
	 *
	 * @param arg0 never-null MOSIP error code
	 * @param arg1 never-null human-readable description
	 * @param arg2 underlying cause; may be null
	 */
	public NotANumberException(String arg0, String arg1, Throwable arg2) {
		super(arg0, arg1, arg2);
	}

}
