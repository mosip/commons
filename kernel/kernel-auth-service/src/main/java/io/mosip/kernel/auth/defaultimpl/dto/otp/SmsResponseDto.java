package io.mosip.kernel.auth.defaultimpl.dto.otp;

import lombok.Data;

/**
 * DTO for the kernel SMS notification service response after an OTP SMS is sent.
 *
 * @author Ramadurai Pandian
 * @since 1.0.0
 *
 */
@Data
public class SmsResponseDto {

	/**
	 * Response status from the SMS gateway or notification service.
	 */
	private String status;

	/**
	 * Response message describing the send result.
	 */
	private String message;
}
