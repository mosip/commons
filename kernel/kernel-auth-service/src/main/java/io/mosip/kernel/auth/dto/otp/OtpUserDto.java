/**
 * 
 */
package io.mosip.kernel.auth.dto.otp;

import io.mosip.kernel.auth.dto.BaseRequestResponseDto;
import io.mosip.kernel.core.authmanager.model.OtpUser;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author Ramadurai Pandian
 *
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class OtpUserDto extends BaseRequestResponseDto {

	private OtpUser request;

}
