package io.mosip.kernel.core.util.exception;

import io.mosip.kernel.core.exception.BaseUncheckedException;

/**
 * Unchecked exception thrown when a numeric argument is negative but must be positive.
 * <p>
 * Contract: raised by {@link io.mosip.kernel.core.util.MathUtils} for values
 * less than zero where a positive number is required.
 * </p>
 *
 * @author Ritesh Sinha
 * @since 1.0.0
 */
public class NotPositiveException extends BaseUncheckedException {

	/** Serializable version Id. */
	private static final long serialVersionUID = 764722202100630634L;

	/**
	 * Constructs the exception with MOSIP error code, message, and cause.
	 *
	 * @param arg0 never-null MOSIP error code
	 * @param arg1 never-null human-readable description
	 * @param arg2 underlying cause; may be null
	 */
	public NotPositiveException(String arg0, String arg1, Throwable arg2) {
		super(arg0, arg1, arg2);

	}

}
