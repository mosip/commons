package io.mosip.kernel.emailnotification.constant;

/**
 * ENUM that provides with the constants for mail notifier.
 * 
 * @author Sagar Mahapatra
 * @since 1.0.0
 */
public enum MailNotifierConstants {
	/**
	 * Success message returned after an email request is accepted.
	 */
	MESSAGE_REQUEST_SENT("Email Request submitted"),
	/**
	 * Success status value set on the email response DTO.
	 */
	MESSAGE_SUCCESS_STATUS("success"),
	/**
	 * Session identifier used when logging mail errors.
	 */
	ERROR_CODE("ERROR-CODE"),
	/**
	 * Logger target identifier for console output.
	 */
	LOGGER_TARGET("System.err"),
	/**
	 * Empty string used as a placeholder session/user id in logs.
	 */
	EMPTY_STRING(""),
	/**
	 * String zero used when comparing empty recipient arrays.
	 */
	DIGIT_ZERO("0");

	/**
	 * The constant value.
	 */
	private String value;

	/**
	 * Private constructor for {@link MailNotifierConstants}.
	 * 
	 * @param message the constant value
	 */
	private MailNotifierConstants(String message) {
		this.value = message;
	}

	/**
	 * Returns the constant value.
	 * 
	 * @return the value
	 */
	public String getValue() {
		return value;
	}
}
