package io.mosip.kernel.emailnotification.constant;

/**
 * ENUM that provides with the error codes and messages for mail notifier
 * arguments.
 * 
 * @author Sagar Mahapatra
 * @since 1.0.0
 *
 */
public enum MailNotifierArgumentErrorConstants {
	/**
	 * Error when the TO address is missing, empty, or invalid (KER-NOE-001).
	 */
	RECEIVER_ADDRESS_NOT_FOUND("KER-NOE-001", "To must be valid. It can't be empty or null or invalid address."),
	/**
	 * Error when the mail subject is missing or blank (KER-NOE-002).
	 */
	SUBJECT_NOT_FOUND("KER-NOE-002", "Subject must be valid. It can't be empty or null."),
	/**
	 * Error when the mail body is missing or blank (KER-NOE-003).
	 */
	CONTENT_NOT_FOUND("KER-NOE-003", "Content must be valid. It can't be empty or null."),
	/**
	 * Error code for {@code MailSendException} on the async handler (KER-NOE-004).
	 */
	MAIL_SEND_EXCEPTION_CODE("KER-NOE-004"),
	/**
	 * Error code for {@code MailAuthenticationException} on the async handler
	 * (KER-NOE-005).
	 */
	MAIL_AUTHENTICATION_EXCEPTION_CODE("KER-NOE-005"),
	/**
	 * Error code for generic Spring {@code MailException} on the async handler
	 * (KER-NOE-006).
	 */
	MAIL_EXCEPTION_CODE("KER-NOE-006"),
	/**
	 * Error when the HTTP request body cannot be parsed (KER-NOE-999).
	 */
	REQUEST_DATA_NOT_VALID("KER-NOE-999", "Data not valid"),
	/**
	 * Error when the From address is missing, empty, or invalid (KER-NOE-007).
	 */
	SENDER_ADDRESS_NOT_FOUND("KER-NOE-007", "From must be valid. It can't be empty or null or invalid address."),
	/**
	 * Internal server error for email notification (KER-NOE-500).
	 */
	INTERNAL_SERVER_ERROR("KER-NOE-500");

	/**
	 * The MOSIP error code.
	 */
	private String errorCode;

	/**
	 * The default error message; may be {@code null} for code-only constants.
	 */
	private String errorMessage;

	/**
	 * Single argument constructor for {@link MailNotifierArgumentErrorConstants}.
	 * 
	 * @param errorCode this error code
	 */
	private MailNotifierArgumentErrorConstants(String errorCode) {
		this.errorCode = errorCode;
	}

	/**
	 * Multiple argument constructor for {@link MailNotifierArgumentErrorConstants}.
	 * 
	 * @param errorCode    this error code
	 * @param errorMessage this error message
	 */
	private MailNotifierArgumentErrorConstants(String errorCode, String errorMessage) {
		this.errorCode = errorCode;
		this.errorMessage = errorMessage;
	}

	/**
	 * Returns the MOSIP error code.
	 * 
	 * @return the error code
	 */
	public String getErrorCode() {
		return errorCode;
	}

	/**
	 * Returns the default error message.
	 * 
	 * @return the error message, or {@code null} when the constant is code-only
	 */
	public String getErrorMessage() {
		return errorMessage;
	}
}
