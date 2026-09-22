package io.mosip.kernel.core.otpmanager.spi;

/**
 * Validates a MOSIP one-time password against a previously generated key.
 * <p>
 * Contract: implementations typically perform database I/O to compare and
 * consume the OTP. Call from OTP-manager. {@code key} and {@code otp} must be
 * non-null and non-blank. A consumed, expired, or mismatched OTP yields a
 * negative result rather than always throwing.
 * </p>
 *
 * @param <D> validation result type (boolean or status DTO)
 * @author Sagar Mahapatra
 * @since 1.0.0
 */
public interface OtpValidator<D> {
	/**
	 * Validates {@code otp} against the stored OTP for {@code key}.
	 * <p>
	 * Contract: may perform database I/O and mark the OTP consumed on success.
	 * Returns a negative result when the OTP does not match, has expired, or
	 * was already consumed.
	 * </p>
	 *
	 * @param key never-null, never-blank key the OTP was generated against
	 * @param otp never-null, never-blank OTP to verify
	 * @return validation result; never null
	 */
	public D validateOtp(String key, String otp);
}
