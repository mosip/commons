/**
 * 
 */
package io.mosip.kernel.auth.defaultimpl.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Persisted MOSIP auth-token record stored and looked up by {@code TokenService}.
 * Holds the subject user id together with the current access token, refresh token,
 * and numeric expiry used when validating or rotating tokens.
 *
 * @author Ramadurai Pandian
 *
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthToken {

	/**
	 * User id the stored tokens belong to.
	 */
	private String userId;

	/**
	 * Current access token string associated with {@link #userId}.
	 */
	private String accessToken;

	/**
	 * Access-token expiry instant as a numeric timestamp used by the token store.
	 */
	private long expirationTime;

	/**
	 * Refresh token used to issue a replacement access token.
	 */
	private String refreshToken;
}
