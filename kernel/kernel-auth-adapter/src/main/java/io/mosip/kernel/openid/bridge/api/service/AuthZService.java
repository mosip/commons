/**
 * 
 */
package io.mosip.kernel.openid.bridge.api.service;

import io.mosip.kernel.core.authmanager.model.MosipUserTokenDto;

/**
 * Authorization (AuthZ) SPI for validating MOSIP access tokens issued by
 * Keycloak.
 * <p>
 * Implementations introspect or decode the JWT and return the associated MOSIP
 * user plus token metadata.
 *
 * @author Ramadurai Pandian
 *
 */
public interface AuthZService {

	/**
	 * Validates the given access token and returns the MOSIP user bound to it
	 * together with token/refresh metadata.
	 *
	 * @param token Bearer access token (typically a Keycloak JWT)
	 * @return user details and token fields when validation succeeds
	 * @throws Exception if the token is missing, expired, or rejected by the IAM
	 */
	MosipUserTokenDto validateToken(String token) throws Exception;

}
