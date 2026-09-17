package io.mosip.kernel.core.util.constant;

/**
 * MOSIP error codes and messages for HMAC utilities.
 * <p>
 * Contract: used when the requested HMAC algorithm is not available on the
 * JRE. Public fields duplicate the getters for historical callers.
 * </p>
 *
 * @see io.mosip.kernel.core.util.HMACUtils
 * @see io.mosip.kernel.core.util.HMACUtils2
 */
public enum HMACUtilConstants {
	/**
	 * Requested HMAC algorithm is not supported.
	 */
	MOSIP_NO_SUCH_ALGORITHM_ERROR_CODE("KER-UTL-203", "No such algorithm for the input");

	/**
	 * MOSIP error code {@code KER-UTL-203}.
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
	HMACUtilConstants(String string1, String string2) {
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
