package io.mosip.kernel.auth.defaultimpl.dto.otp;

/**
 * OTP value posted to the OTP manager when validating a previously generated OTP.
 */
public class OtpValidateRequestDto {

	/**
	 * User-supplied one-time password to check against the store.
	 */
	private String otp;

	/**
	 * Returns the OTP being validated.
	 *
	 * @return OTP value
	 */
	public String getOtp() {
		return otp;
	}

	/**
	 * Sets the OTP being validated.
	 *
	 * @param otp OTP value to store
	 */
	public void setOtp(String otp) {
		this.otp = otp;
	}
}
