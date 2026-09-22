package io.mosip.kernel.core.otpmanager.spi;

/**
 * Generates a MOSIP one-time password for a key.
 * <p>
 * Contract: implementations typically persist OTP state in
 * {@code mosip_kernel} (or a dedicated OTP store) and may freeze a key after
 * repeated requests. Call from OTP-manager. {@code otpDto} must be non-null
 * with a non-blank key.
 * </p>
 *
 * @param <S> OTP request DTO type
 * @param <D> generated OTP / response type
 * @author Ritesh Sinha
 * @since 1.0.0
 */
public interface OtpGenerator<S, D> {
	/**
	 * Generates an OTP for the key in {@code otpDto} unless the key is frozen.
	 * <p>
	 * Contract: may perform database I/O.
	 * </p>
	 *
	 * @param otpDto never-null generation request containing the key
	 * @return never-null generated OTP or response DTO
	 */
	public D getOtp(S otpDto);
}
