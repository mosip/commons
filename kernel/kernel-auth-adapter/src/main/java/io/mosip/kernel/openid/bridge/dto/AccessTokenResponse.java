package io.mosip.kernel.openid.bridge.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Jackson mapping of a Keycloak/OIDC token-endpoint success body
 * ({@code application/json} from {@code /protocol/openid-connect/token}).
 * <p>
 * Field names are snake_case to match the IAM JSON. Used when exchanging an
 * authorization code, refresh token, or client credentials.
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class AccessTokenResponse {
	/**
	 * OIDC access token (typically a JWT) returned as {@code access_token}.
	 */
	private String access_token;
	/**
	 * Access-token lifetime in seconds, as a string ({@code expires_in}).
	 */
	private String expires_in;
	/**
	 * Refresh-token lifetime in seconds, as a string ({@code refresh_expires_in}).
	 */
	private String refresh_expires_in;
	/**
	 * OIDC refresh token ({@code refresh_token}).
	 */
	private String refresh_token;
	/**
	 * Token type, usually {@code Bearer} ({@code token_type}).
	 */
	private String token_type;
	/**
	 * Keycloak browser session identifier ({@code session_state}).
	 */
	private String session_state;
	/**
	 * Space-delimited OIDC scopes granted on the access token ({@code scope}).
	 */
	private String scope;
	/**
	 * OIDC ID token JWT when {@code openid} scope was requested ({@code id_token}).
	 */
	private String id_token;
}
