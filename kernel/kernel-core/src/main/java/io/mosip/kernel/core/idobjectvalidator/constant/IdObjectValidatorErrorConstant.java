package io.mosip.kernel.core.idobjectvalidator.constant;

/**
 * MOSIP error codes and messages for identity-object JSON schema validation.
 * <p>
 * Contract: use {@link #getErrorCode()} and {@link #getMessage()} when
 * constructing {@code idobjectvalidator.exception} types. Messages may
 * contain {@code %s} placeholders for field paths.
 * </p>
 *
 * @author Manoj SP
 * @author Swati Raj
 * @since 1.0.0
 */
public enum IdObjectValidatorErrorConstant {

	/**
	 * Identity schema could not be read.
	 */
	SCHEMA_IO_EXCEPTION("KER-IOV-001", "Failed to read schema"),

	/**
	 * Identity JSON failed schema validation.
	 */
	ID_OBJECT_VALIDATION_FAILED("KER-IOV-002", "Id Object validation failed"),

	/**
	 * Identity JSON could not be parsed or converted.
	 */
	ID_OBJECT_PARSING_FAILED("KER-IOV-003", "Failed to parse/convert Id Object"),

	/**
	 * A named input parameter is present but invalid; {@code %s} is the path.
	 */
	INVALID_INPUT_PARAMETER("KER-IOV-004", "Invalid input parameter - %s"),

	/**
	 * A named input parameter is missing; {@code %s} is the path.
	 */
	MISSING_INPUT_PARAMETER("KER-IOV-005", "Missing input parameter - %s"),

	/**
	 * Masterdata required for validation could not be loaded.
	 */
	MASTERDATA_LOAD_FAILED("KER-IOV-006", "Failed to load data from kernel masterdata"),
	
	/**
	 * Identity schema JSON is itself invalid.
	 */
	INVALID_ID_SCHEMA("KER-IOV-007", "Invalid ID schema");

	/**
	 * MOSIP error code such as {@code KER-IOV-001}.
	 */
	private final String errorCode;

	/**
	 * Human-readable message, possibly with {@code %s}.
	 */
	private final String message;

	/**
	 * Binds the constant to its error code and message.
	 *
	 * @param errorCode never-null MOSIP error code
	 * @param message   never-null message template
	 */
	IdObjectValidatorErrorConstant(final String errorCode, final String message) {
		this.errorCode = errorCode;
		this.message = message;
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
	 * Returns the message template.
	 *
	 * @return never-null message; may contain {@code %s}
	 */
	public String getMessage() {
		return message;
	}

}
