package io.mosip.kernel.auth.defaultimpl.dto.otp;

import lombok.Data;

/**
 * DTO for OTP validation outcome returned to the auth manager after the OTP
 * manager accepts or rejects a submitted code.
 *
 * @author Ramadurai Pandian
 * @since 1.0.0
 *
 */
@Data
public class OtpValidatorResponseDto {

	/**
	 * Validation request status, typically {@code success} or {@code failure}.
	 */
	private String status;

	/**
	 * Validation request message from the OTP manager.
	 */
	private String message;
}
