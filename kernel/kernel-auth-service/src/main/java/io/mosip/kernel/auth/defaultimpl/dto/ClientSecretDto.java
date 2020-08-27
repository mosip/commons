/**
 * 
 */
package io.mosip.kernel.auth.defaultimpl.dto;

import io.mosip.kernel.core.authmanager.model.ClientSecret;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author Ramadurai Pandian
 *
 */

@Data
@EqualsAndHashCode(callSuper = true)
public class ClientSecretDto extends BaseRequestResponseDto {

	private ClientSecret request;

}
