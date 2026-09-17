package io.mosip.kernel.core.util.constant;

/**
 * MOSIP error codes and messages for {@link io.mosip.kernel.core.util.StringUtils}.
 * <p>
 * Contract: use {@link #getErrorCode()} and {@link #getErrorMessage()} when
 * wrapping index, regex, or argument failures.
 * </p>
 *
 * @author Sidhant Agarwal
 * @since 1.0.0
 */
public enum StringUtilConstants {

	/**
	 * Array or string index is out of bounds.
	 */
	MOSIP_ARRAY_INDEX_OUT_OF_BOUNDS_ERROR_CODE("KER-UTL-501", "Array Index out of bounds"),
	/**
	 * Regular-expression pattern is syntactically invalid.
	 */
	MOSIP_PATTERN_SYNTAX_ERROR_CODE("KER-UTL-503", "Pattern Syntax Exception"),
	/**
	 * Illegal string-utility argument.
	 */
	MOSIP_ILLEGAL_ARGUMENT_ERROR_CODE("KER-UTL-502", "Illegal Argument Exception");

	/**
	 * MOSIP error code such as {@code KER-UTL-501}.
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
	StringUtilConstants(String string1, String string2) {
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
