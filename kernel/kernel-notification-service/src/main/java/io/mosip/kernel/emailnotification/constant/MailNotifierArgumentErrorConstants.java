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
	RECEIVER_ADDRESS_NOT_FOUND("KER-NOE-001", "To must be valid. It can't be empty or null or invalid address."),
	SUBJECT_NOT_FOUND("KER-NOE-002", "Subject must be valid. It can't be empty or null."),
	CONTENT_NOT_FOUND("KER-NOE-003", "Content must be valid. It can't be empty or null."),
	MAIL_SEND_EXCEPTION_CODE("KER-NOE-004"), MAIL_AUTHENTICATION_EXCEPTION_CODE("KER-NOE-005"),
	MAIL_EXCEPTION_CODE("KER-NOE-006"), REQUEST_DATA_NOT_VALID("KER-NOE-999", "Data not valid"),
	SENDER_ADDRESS_NOT_FOUND("KER-NOE-007", "From must be valid. It can't be empty or null or invalid address."),
	INTERNAL_SERVER_ERROR("KER-NOE-500");

	/**
	 * The error code.
	 */
	private String errorCode;

	/**
	 * The error message.
	 */
	private String errorMessage;

	/**
	 * Single argument constructor for {@link MailNotifierArgumentErrorConstants}
	 * 
	 * @param errorCode this error code
	 */
	private MailNotifierArgumentErrorConstants(String errorCode) {
		this.errorCode = errorCode;
	}

	/**
	 * Multiple argument Constructor for {@link MailNotifierArgumentErrorConstants}
	 * 
	 * @param errorCode    this error code.
	 * @param errorMessage this error message.
	 */
	private MailNotifierArgumentErrorConstants(String errorCode, String errorMessage) {
		this.errorCode = errorCode;
		this.errorMessage = errorMessage;
	}

	/**
	 * Getter for error code.
	 * 
	 * @return the error code.
	 */
	public String getErrorCode() {
		return errorCode;
	}

	/**
	 * Getter for error message.
	 * 
	 * @return the error message.
	 */
	public String getErrorMessage() {
		return errorMessage;
	}
}
