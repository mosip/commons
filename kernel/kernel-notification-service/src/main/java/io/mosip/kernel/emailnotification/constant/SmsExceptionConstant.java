package io.mosip.kernel.emailnotification.constant;

/**
 * This enum provides all the exception constants for sms notification.
 * 
 * @author Ritesh sinha
 * @since 1.0.0
 *
 */
public enum SmsExceptionConstant {

	/**
	 * Error when number or message is empty or null (KER-NOS-001).
	 */
	SMS_ILLEGAL_INPUT("KER-NOS-001", "Number and message can't be empty, null"),
	/**
	 * Internal server error for SMS notification (KER-NOS-500).
	 */
    INTERNAL_SERVER_ERROR("KER-NOS-500", "Internal server error");

	/**
	 * The MOSIP error code.
	 */
	private String errorCode;

	/**
	 * The default error message.
	 */
	private String errorMessage;

	/**
	 * Instantiates an SMS exception constant with the given code and message.
	 *
	 * @param errorCode    the MOSIP error code to be set
	 * @param errorMessage the default error message to be set
	 */
	private SmsExceptionConstant(String errorCode, String errorMessage) {
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
	 * @return the error message
	 */
	public String getErrorMessage() {
		return errorMessage;
	}

}
