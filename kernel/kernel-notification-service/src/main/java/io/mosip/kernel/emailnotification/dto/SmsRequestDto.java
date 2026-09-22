package io.mosip.kernel.emailnotification.dto;

import jakarta.validation.constraints.NotBlank;

import lombok.Data;

/**
 * Request body for {@code POST /sms/send}, wrapped in a MOSIP
 * {@code RequestWrapper}.
 * 
 * @author Ritesh Sinha
 * @since 1.0.0
 *
 */
@Data
public class SmsRequestDto {

	/**
	 * Destination contact number (MSISDN). Must not be blank.
	 */

	@NotBlank
	private String number;

	/**
	 * SMS body to send. Must not be blank.
	 */

	@NotBlank
	private String message;

}
