package io.mosip.kernel.core.exception;

/**
 * Unchecked MOSIP wrapper for arithmetic overflow or undefined numeric results.
 * <p>
 * Contract: raised by {@link io.mosip.kernel.core.util.MathUtils} and similar
 * utilities. Callers should treat this as a client-input or computation error.
 * </p>
 *
 * @author Ritesh Sinha
 * @author Sagar Mahapatra
 * @since 1.0.0
 * @see io.mosip.kernel.core.util.constant.MathUtilConstants
 */
public class ArithmeticException extends BaseUncheckedException {

	/** Serializable version Id. */
	private static final long serialVersionUID = 834721102100630614L;

	/**
	 * Constructs an arithmetic exception with MOSIP error code, message, and
	 * cause.
	 *
	 * @param arg0 never-null MOSIP error code
	 * @param arg1 never-null human-readable description
	 * @param arg2 underlying cause; may be null
	 */
	public ArithmeticException(String arg0, String arg1, Throwable arg2) {
		super(arg0, arg1, arg2);

	}

}
