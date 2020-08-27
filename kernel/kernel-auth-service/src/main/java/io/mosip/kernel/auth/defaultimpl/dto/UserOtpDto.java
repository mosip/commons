/**
 * 
 */
package io.mosip.kernel.auth.defaultimpl.dto;

import io.mosip.kernel.core.authmanager.model.UserOtp;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author Ramadurai Pandian
 *
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class UserOtpDto extends BaseRequestResponseDto {

	private UserOtp request;

}
