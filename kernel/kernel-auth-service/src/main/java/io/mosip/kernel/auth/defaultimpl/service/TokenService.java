/**
 * 
 */
package io.mosip.kernel.auth.defaultimpl.service;

import io.mosip.kernel.auth.defaultimpl.dto.AuthToken;
import io.mosip.kernel.auth.defaultimpl.dto.TimeToken;

/**
 * Persists and looks up OAuth access/refresh tokens in the IAM token store
 * ({@code iam.oauth_access_token}).
 *
 * @author Ramadurai Pandian
 *
 */
public interface TokenService {

	/**
	 * Inserts a token row, or updates if the user already has a stored token.
	 *
	 * @param token access, refresh, user, and expiry
	 */
	void StoreToken(AuthToken token);

	/**
	 * Updates the stored token for an existing user.
	 *
	 * @param token replacement token fields
	 */
	void UpdateToken(AuthToken token);

	/**
	 * Loads a stored token by access-token value.
	 *
	 * @param token access token string
	 * @return stored token, or {@code null} if missing
	 */
	AuthToken getTokenDetails(String token);

	/**
	 * Writes a new access token for {@code userName} and returns the updated row.
	 *
	 * @param token          existing access token (lookup key in some impls)
	 * @param newAccessToken replacement token and expiry
	 * @param userName       user id
	 * @return updated stored token
	 */
	AuthToken getUpdatedAccessToken(String token, TimeToken newAccessToken, String userName);

	/**
	 * Deletes the stored token for the user owning {@code token}.
	 *
	 * @param token access token to revoke
	 */
	void revokeToken(String token);

	/**
	 * Loads a stored token by user name.
	 *
	 * @param userName user id
	 * @return stored token, or {@code null} if missing
	 */
	AuthToken getTokenBasedOnName(String userName);

}
