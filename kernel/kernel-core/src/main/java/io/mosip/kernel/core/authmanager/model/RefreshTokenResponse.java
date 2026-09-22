package io.mosip.kernel.core.authmanager.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Result of exchanging a refresh token for a new access token pair.
 * <p>
 * Contract: token fields are null on failure; {@code authNResponse} describes
 * the outcome. Expiry fields are provider-formatted strings (typically epoch
 * or ISO-8601). Treat tokens as sensitive. Does not perform I/O.
 * </p>
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class RefreshTokenResponse {
	/**
	 * Status and message for the refresh attempt; may be null.
	 */
	private AuthNResponse authNResponse;
	/**
	 * Newly issued access token; sensitive; may be null on failure.
	 */
	private String accesstoken;
	/**
	 * Newly issued refresh token; sensitive; may be null on failure.
	 */
	private String refreshToken;
	/**
	 * Access-token expiry as a provider string; may be null.
	 */
	private String accessTokenExpTime;
	/**
	 * Refresh-token expiry as a provider string; may be null.
	 */
	private String refreshTokenExpTime;
}
