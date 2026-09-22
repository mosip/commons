package io.mosip.kernel.core.util.constant;

/**
 * MOSIP error codes and messages for {@link io.mosip.kernel.core.util.DateUtils}.
 * <p>
 * Contract: use {@link #getErrorCode()} and {@link #getEexceptionMessage()}
 * when wrapping parse or argument failures. Public fields duplicate the
 * getters for historical callers.
 * </p>
 */
public enum DateUtilConstants {
	/**
	 * Illegal date argument (wrong type or out of range).
	 */
	ILLEGALARGUMENT_ERROR_CODE("KER-UTL-101", "Invalid Argument Found"),
	/**
	 * Required date argument was null.
	 */
	NULL_ARGUMENT_ERROR_CODE("KER-UTL-102", "Null Argument Found"),
	/**
	 * Date string could not be parsed with the expected pattern.
	 */
	PARSE_EXCEPTION_ERROR_CODE("KER-UTL-103", "Parsing error occours");

	/**
	 * MOSIP error code such as {@code KER-UTL-101}.
	 */
	public final String errorCode;
	/**
	 * Human-readable exception message associated with this constant.
	 */
	public final String exceptionMessage;

	/**
	 * Binds the constant to its error code and message.
	 *
	 * @param errorCode        never-null MOSIP error code
	 * @param exceptionMessage never-null default message
	 */
	DateUtilConstants(final String errorCode, final String exceptionMessage) {
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
	 * @return never-null message
	 */
	public String getEexceptionMessage() {
		return exceptionMessage;
	}
}
