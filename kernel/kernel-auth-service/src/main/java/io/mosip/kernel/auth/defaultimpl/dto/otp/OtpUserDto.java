/**
 * 
 */
package io.mosip.kernel.auth.defaultimpl.dto.otp;

import io.mosip.kernel.auth.defaultimpl.dto.BaseRequestResponseDto;
import io.mosip.kernel.core.authmanager.model.OtpUser;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * MOSIP envelope wrapping an {@link OtpUser} body used to request OTP generation
 * and channel delivery (SMS, email, or both).
 *
 * @author Ramadurai Pandian
 *
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class OtpUserDto extends BaseRequestResponseDto {

	/**
	 * User, channels, and application context for OTP generation.
	 */
	private OtpUser request;

}
