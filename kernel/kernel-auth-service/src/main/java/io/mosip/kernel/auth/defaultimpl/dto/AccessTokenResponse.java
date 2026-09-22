package io.mosip.kernel.auth.defaultimpl.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Jackson mapping of Keycloak's OpenID token endpoint JSON.
 * Field names stay snake_case so they match the token response body
 * ({@code access_token}, {@code refresh_token}, {@code expires_in}, and so on).
 * Used when exchanging password, client-credentials, refresh, or authorization-code grants.
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class AccessTokenResponse {

	/**
	 * JWT (or opaque) access token issued by Keycloak.
	 */
	private String access_token;

	/**
	 * Access-token lifetime in seconds, as a string as returned by Keycloak.
	 */
	private String expires_in;

	/**
	 * Refresh-token lifetime in seconds, as a string as returned by Keycloak.
	 */
	private String refresh_expires_in;

	/**
	 * Refresh token used to obtain a new access token without re-authenticating.
	 */
	private String refresh_token;

	/**
	 * Token type advertised by Keycloak, typically {@code Bearer}.
	 */
	private String token_type;

	/**
	 * Keycloak login session identifier ({@code session_state}).
	 */
	private String session_state;

	/**
	 * Space-delimited OAuth 2.0 scopes granted with the token.
	 */
	private String scope;
}
