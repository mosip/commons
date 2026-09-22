/**
 * 
 */
package io.mosip.kernel.auth.defaultimpl.dto;

import io.mosip.kernel.core.authmanager.model.UserOtp;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * MOSIP envelope wrapping a {@link UserOtp} body used to validate a user-entered OTP.
 *
 * @author Ramadurai Pandian
 *
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class UserOtpDto extends BaseRequestResponseDto {

	/**
	 * User id, OTP value, and application id to validate.
	 */
	private UserOtp request;

}
