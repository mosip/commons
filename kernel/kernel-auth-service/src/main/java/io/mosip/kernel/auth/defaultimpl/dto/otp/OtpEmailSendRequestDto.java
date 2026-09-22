package io.mosip.kernel.auth.defaultimpl.dto.otp;

/**
 * Request body sent to the kernel notification email API when delivering an OTP.
 */
public class OtpEmailSendRequestDto {

	/**
	 * Destination email address.
	 */
	private String email;

	/**
	 * Rendered OTP email body.
	 */
	private String message;

	/**
	 * Creates an email OTP send request.
	 *
	 * @param email   destination address
	 * @param message rendered message body
	 */
	public OtpEmailSendRequestDto(String email, String message) {
		this.email = email;
		this.message = message;
	}

	/**
	 * Returns the destination email address.
	 *
	 * @return email address
	 */
	public String getEmail() {
		return email;
	}

	/**
	 * Sets the destination email address.
	 *
	 * @param email email address to store
	 */
	public void setEmail(String email) {
		this.email = email;
	}

	/**
	 * Returns the rendered OTP email body.
	 *
	 * @return message body
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * Sets the rendered OTP email body.
	 *
	 * @param message message body to store
	 */
	public void setMessage(String message) {
		this.message = message;
	}
}
