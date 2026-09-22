package io.mosip.kernel.auth.defaultimpl.dto.otp;

/**
 * OTP-manager validate response describing whether the submitted OTP was accepted.
 */
public class OtpValidateResponseDto {

	/**
	 * Human-readable validation result from the OTP manager.
	 */
	private String message;

	/**
	 * Validation outcome, typically {@code success} or {@code failure}.
	 */
	private String status;

	/**
	 * Returns the validation message.
	 *
	 * @return result message
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * Sets the validation message.
	 *
	 * @param message result message to store
	 */
	public void setMessage(String message) {
		this.message = message;
	}

	/**
	 * Returns the validation status.
	 *
	 * @return status string
	 */
	public String getStatus() {
		return status;
	}

	/**
	 * Sets the validation status.
	 *
	 * @param status status string to store
	 */
	public void setStatus(String status) {
		this.status = status;
	}
}
