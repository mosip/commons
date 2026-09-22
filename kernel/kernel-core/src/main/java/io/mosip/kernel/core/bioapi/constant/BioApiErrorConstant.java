package io.mosip.kernel.core.bioapi.constant;

/**
 * MOSIP error codes and messages for biometric API (quality, match, input)
 * failures.
 * <p>
 * Contract: use {@link #getErrorCode()} and {@link #getMessage()} when
 * constructing {@link io.mosip.kernel.core.bioapi.exception.BiometricException}.
 * Message values may contain {@code %s} placeholders for parameter names.
 * </p>
 *
 * @author Manoj SP
 */
public enum BioApiErrorConstant {

	/**
	 * Input data is present but fails validation; message placeholder is the
	 * parameter name.
	 */
	INVALID_INPUT_PARAMETER("KER-BIO-001", "Invalid Input Parameter - %s"),

	/**
	 * A required input parameter is missing; message placeholder is the
	 * parameter name.
	 */
	MISSING_INPUT_PARAMETER("KER-BIO-002", "Missing Input Parameter - %s"),

	/**
	 * Quality check could not be performed on otherwise valid biometric data.
	 */
	QUALITY_CHECK_FAILED("KER-BIO-003", "Quality check of Biometric data failed"),

	/**
	 * Matching could not be performed on otherwise valid biometric data.
	 */
	MATCHING_FAILED("KER-BIO-004", "Matching of Biometric data failed"),

	/**
	 * Catch-all when no more specific biometric API error applies.
	 */
	UNKNOWN_ERROR("KER-BIO-005", "Unknown error occurred");

	/** The MOSIP error code, for example {@code KER-BIO-001}. */
	private final String errorCode;

	/** The human-readable message, possibly with {@code %s} placeholders. */
	private final String message;

	/**
	 * Instantiates a new error constant.
	 *
	 * @param errorCode never-null MOSIP error code
	 * @param message   never-null message template
	 */
	BioApiErrorConstant(final String errorCode, final String message) {
		this.errorCode = errorCode;
		this.message = message;
	}

	/**
	 * Returns the MOSIP error code.
	 *
	 * @return never-null error code such as {@code KER-BIO-001}
	 */
	public String getErrorCode() {
		return errorCode;
	}

	/**
	 * Returns the message template associated with this constant.
	 *
	 * @return never-null message; may contain {@code %s}
	 */
	public String getMessage() {
		return message;
	}

}
