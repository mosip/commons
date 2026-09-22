package io.mosip.kernel.auth.defaultimpl.dto.otp;

/**
 * Request body posted to the kernel SMS notification API when delivering an OTP.
 */
public class OtpSmsSendRequestDto {

	/**
	 * Destination mobile number.
	 */
	private String number;

	/**
	 * Rendered OTP SMS text.
	 */
	private String message;

	/**
	 * Creates an SMS OTP send request.
	 *
	 * @param number  destination mobile number
	 * @param message rendered SMS text
	 */
	public OtpSmsSendRequestDto(String number, String message) {
		this.number = number;
		this.message = message;
	}

	/**
	 * Returns the destination mobile number.
	 *
	 * @return mobile number
	 */
	public String getNumber() {
		return number;
	}

	/**
	 * Sets the destination mobile number.
	 *
	 * @param number mobile number to store
	 */
	public void setNumber(String number) {
		this.number = number;
	}

	/**
	 * Returns the rendered OTP SMS text.
	 *
	 * @return SMS message
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * Sets the rendered OTP SMS text.
	 *
	 * @param message SMS message to store
	 */
	public void setMessage(String message) {
		this.message = message;
	}
}
