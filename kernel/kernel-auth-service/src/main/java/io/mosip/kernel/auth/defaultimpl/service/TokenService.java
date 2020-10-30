/**
 * 
 */
package io.mosip.kernel.auth.defaultimpl.service;

import io.mosip.kernel.auth.defaultimpl.dto.AuthToken;
import io.mosip.kernel.auth.defaultimpl.dto.TimeToken;

/**
 * @author Ramadurai Pandian
 *
 */
public interface TokenService {

	void StoreToken(AuthToken token);

	void UpdateToken(AuthToken token);

	AuthToken getTokenDetails(String token);

	AuthToken getUpdatedAccessToken(String token, TimeToken newAccessToken, String userName);

	void revokeToken(String token);

	AuthToken getTokenBasedOnName(String userName);

}
