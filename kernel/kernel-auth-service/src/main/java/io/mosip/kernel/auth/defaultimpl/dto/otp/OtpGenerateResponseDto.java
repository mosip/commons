package io.mosip.kernel.auth.defaultimpl.dto.otp;

/**
 * OTP-manager generate response containing the issued OTP and generation status.
 */
public class OtpGenerateResponseDto {

	/**
	 * Generated one-time password value.
	 */
	private String otp;

	/**
	 * Generation outcome from the OTP manager.
	 */
	private String status;

	/**
	 * Returns the generated OTP.
	 *
	 * @return OTP value
	 */
	public String getOtp() {
		return otp;
	}

	/**
	 * Sets the generated OTP.
	 *
	 * @param otp OTP value to store
	 */
	public void setOtp(String otp) {
		this.otp = otp;
	}

	/**
	 * Returns the generation status.
	 *
	 * @return status string
	 */
	public String getStatus() {
		return status;
	}

	/**
	 * Sets the generation status.
	 *
	 * @param status status string to store
	 */
	public void setStatus(String status) {
		this.status = status;
	}
}
