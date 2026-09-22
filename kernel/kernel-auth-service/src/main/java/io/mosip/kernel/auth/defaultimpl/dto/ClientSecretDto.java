/**
 * 
 */
package io.mosip.kernel.auth.defaultimpl.dto;

import io.mosip.kernel.core.authmanager.model.ClientSecret;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * MOSIP envelope wrapping a {@link ClientSecret} client-credentials login body.
 * Used when authenticating a service client with client id and secret.
 *
 * @author Ramadurai Pandian
 *
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ClientSecretDto extends BaseRequestResponseDto {

	/**
	 * Client id, secret, and application id used for the client-credentials grant.
	 */
	private ClientSecret request;

}
