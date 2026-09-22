package io.mosip.kernel.core.util.constant;

/**
 * MOSIP error codes and messages for {@link io.mosip.kernel.core.util.CalendarUtils}.
 * <p>
 * Contract: use {@link #getErrorCode()} when constructing MOSIP exceptions from
 * calendar-utility failures. Constants that hold messages are not error codes.
 * </p>
 */
public enum CalendarUtilConstants {

	/**
	 * Error code for an illegal calendar argument.
	 */
	ILLEGAL_ARGUMENT_CODE("KER-UTL-001"),
	/**
	 * Error code for calendar arithmetic overflow.
	 */
	ARITHMETIC_EXCEPTION_CODE("KER-UTL-002"),
	/**
	 * Error code for a null calendar argument.
	 */
	NULL_ARGUMENT_CODE("KER-UTL-003"),
	/**
	 * Message for an invalid calendar argument.
	 */
	ILLEGAL_ARGUMENT_MESSAGE("Invalid_Argument"),
	/**
	 * Message when a year exceeds the supported maximum (~280 million).
	 */
	YEAR_OVERFLOW_MESSAGE("Year_can't_be_greater_than_280million"),
	/**
	 * Message when a required date argument is null.
	 */
	DATE_NULL_MESSAGE("Date_can't_be_null");

	/**
	 * MOSIP error code or message text stored by this constant.
	 */
	private final String errorCode;

	/**
	 * Binds the constant to its code or message string.
	 *
	 * @param eCode never-null code or message
	 */
	private CalendarUtilConstants(String eCode) {
		errorCode = eCode;
	}

	/**
	 * Returns the code or message bound to this constant.
	 *
	 * @return never-null string
	 */
	public String getErrorCode() {
		return errorCode;
	}
}
