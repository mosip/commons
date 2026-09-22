package io.mosip.kernel.auth.defaultimpl.dto.otp;

/**
 * Status payload returned by the kernel SMS notification API after an OTP is sent.
 */
public class OtpSmsSendResponseDto {

	/**
	 * Delivery outcome, typically {@code success} or {@code failure}.
	 */
	private String status;

	/**
	 * Human-readable delivery result from the notification service.
	 */
	private String message;

	/**
	 * Returns the SMS delivery status.
	 *
	 * @return status string
	 */
	public String getStatus() {
		return status;
	}

	/**
	 * Sets the SMS delivery status.
	 *
	 * @param status status string to store
	 */
	public void setStatus(String status) {
		this.status = status;
	}

	/**
	 * Returns the notification service message.
	 *
	 * @return result message
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * Sets the notification service message.
	 *
	 * @param message result message to store
	 */
	public void setMessage(String message) {
		this.message = message;
	}
}
