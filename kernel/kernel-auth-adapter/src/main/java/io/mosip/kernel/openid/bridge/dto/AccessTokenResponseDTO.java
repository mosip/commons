package io.mosip.kernel.openid.bridge.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Slim token payload returned to MOSIP clients after a successful
 * authorization-code exchange.
 * <p>
 * Populated from {@link AccessTokenResponse} (access token, expiry, and ID
 * token). Unlike {@link AccessTokenResponse}, field names are camelCase for
 * MOSIP JSON APIs.
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class AccessTokenResponseDTO {
	/**
	 * OIDC access token copied from {@link AccessTokenResponse#access_token}.
	 */
	private String accessToken;
	/**
	 * Access-token lifetime in seconds, copied from
	 * {@link AccessTokenResponse#expires_in}.
	 */
	private String expiresIn;
	/**
	 * OIDC ID token copied from {@link AccessTokenResponse#id_token}, when present.
	 */
	private String idToken;
}
