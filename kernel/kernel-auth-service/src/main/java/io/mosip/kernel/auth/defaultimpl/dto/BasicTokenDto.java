/**
 * 
 */
package io.mosip.kernel.auth.defaultimpl.dto;

import lombok.Data;

/**
 * Access and refresh token pair produced by {@code TokenGenerator} for basic
 * (non-Keycloak) JWT issuance, including OTP-verified tokens.
 *
 * @author Ramadurai Pandian
 *
 */
@Data
public class BasicTokenDto {

	/**
	 * Generated MOSIP authentication JWT.
	 */
	private String authToken;

	/**
	 * Generated refresh token paired with {@link #authToken}.
	 */
	private String refreshToken;

	/**
	 * Absolute expiry time of {@link #authToken} as a numeric timestamp.
	 */
	private long expiryTime;
}
