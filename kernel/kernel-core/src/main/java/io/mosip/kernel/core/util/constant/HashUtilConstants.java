package io.mosip.kernel.core.util.constant;

/**
 * MOSIP error codes and messages for {@link io.mosip.kernel.core.util.HashUtils}.
 * <p>
 * Contract: used when hash-seed arguments are even instead of odd. Public
 * fields duplicate the getters for historical callers.
 * </p>
 */
public enum HashUtilConstants {
	/**
	 * Initial odd number argument was even.
	 */
	MOSIP_ILLEGAL_ARGUMENT_INITIALODDNUMBER_ERROR_CODE("KER-UTL-201", "Entered initialOddNumber is even"),
	/**
	 * Multiplier odd number argument was even.
	 */
	MOSIP_ILLEGAL_ARGUMENT_MULTIPLIERODDNUMBER_ERROR_CODE("KER-UTL-202", "Entered multiplierOddNumber is even");

	/**
	 * MOSIP error code such as {@code KER-UTL-201}.
	 */
	public final String errorCode;
	/**
	 * Human-readable error message.
	 */
	public final String errorMessage;

	/**
	 * Binds the constant to its error code and message.
	 *
	 * @param string1 never-null error code
	 * @param string2 never-null error message
	 */
	HashUtilConstants(String string1, String string2) {
		this.errorCode = string1;
		this.errorMessage = string2;
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
	 * Returns the human-readable error message.
	 *
	 * @return never-null message
	 */
	public String getErrorMessage() {
		return errorMessage;
	}

}
