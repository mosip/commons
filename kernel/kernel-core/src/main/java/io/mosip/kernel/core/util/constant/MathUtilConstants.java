package io.mosip.kernel.core.util.constant;

/**
 * MOSIP error codes and messages for {@link io.mosip.kernel.core.util.MathUtils}.
 * <p>
 * Contract: use {@link #getErrorCode()} and {@link #getEexceptionMessage()}
 * when wrapping arithmetic or argument failures. Public fields duplicate the
 * getters for historical callers.
 * </p>
 */
public enum MathUtilConstants {
	/**
	 * Arithmetic overflow or undefined result.
	 */
	ARITHMETIC_ERROR_CODE("KER-UTL-301", "Invalid Result Found"),
	/**
	 * Illegal numeric argument.
	 */
	ILLEGALARGUMENT_ERROR_CODE("KER-UTL-302", "Invalid Argument Found"),
	/**
	 * NaN used where a finite limit is required.
	 */
	NOT_A_NUMBER_ERROR_CODE("KER-UTL-303", "NaN cannot be use as limit"),
	/**
	 * Infinite value used where a finite limit is required.
	 */
	NOT_FINITE_NUMBER_ERROR_CODE("KER-UTL-304", "Infinite limit cannot be used"),
	/**
	 * Negative value used where a positive argument is required.
	 */
	NOTPOSITIVE_ERROR_CODE("KER-UTL-305", "Negative argument cannot be used"),
	/**
	 * Numeric value exceeds the supported range.
	 */
	NUMBER_IS_TOO_LARGE_ERROR_CODE("KER-UTL-306", ""),
	/**
	 * Range lower bound is greater than the upper bound.
	 */
	NULL_POINTER_ERROR_CODE("KER-UTL-307", "Lower limit is larger than upper limit");

	/**
	 * MOSIP error code such as {@code KER-UTL-301}.
	 */
	public final String errorCode;
	/**
	 * Human-readable exception message.
	 */
	public final String exceptionMessage;

	/**
	 * Binds the constant to its error code and message.
	 *
	 * @param errorCode        never-null MOSIP error code
	 * @param exceptionMessage never-null default message; may be empty
	 */
	MathUtilConstants(final String errorCode, final String exceptionMessage) {
		this.errorCode = errorCode;
		this.exceptionMessage = exceptionMessage;
	}

	/**
	 * Returns the MOSIP error code.
	 *
	 * @return never-null error code
	 */
	public String getErrorCode() {
		return errorCode;
	}

	/**
	 * Returns the default exception message (historical spelling of getter).
	 *
	 * @return never-null message; may be empty
	 */
	public String getEexceptionMessage() {
		return exceptionMessage;
	}
}
