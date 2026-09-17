package io.mosip.kernel.core.authmanager.model;

import jakarta.validation.constraints.NotBlank;

import lombok.Data;

/**
 * Request to authenticate a user with a previously issued OTP.
 * <p>
 * Contract: all fields are required and non-blank. Passed to
 * {@link io.mosip.kernel.core.authmanager.spi.AuthNService#authenticateUserWithOtp(UserOtp)}.
 * Treat {@code otp} as sensitive; do not log it.
 * </p>
 */
@Data
public class UserOtp {

	/**
	 * User identifier that received the OTP; required, non-blank.
	 */
	@NotBlank
	private String userId;
	/**
	 * One-time password to verify; required, non-blank; sensitive.
	 */
	@NotBlank
	private String otp;
	/**
	 * MOSIP application identifier that requested the OTP; required, non-blank.
	 */
	@NotBlank
	private String appId;

}
