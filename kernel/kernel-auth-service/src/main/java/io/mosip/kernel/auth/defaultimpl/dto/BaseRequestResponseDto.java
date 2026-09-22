/**
 * 
 */
package io.mosip.kernel.auth.defaultimpl.dto;

import lombok.Data;

/**
 * Shared MOSIP HTTP envelope fields used by internal request wrappers such as
 * {@link ClientSecretDto}, {@link UserOtpDto}, and {@link io.mosip.kernel.auth.defaultimpl.dto.otp.OtpUserDto}.
 *
 * @author Ramadurai Pandian
 *
 */
@Data
public class BaseRequestResponseDto {

	/**
	 * MOSIP API identifier from the request or response envelope.
	 */
	private String id;

	/**
	 * Envelope version string.
	 */
	private String version;

	/**
	 * Request or response timestamp from the envelope.
	 */
	private String timestamp;
}
