package io.mosip.kernel.core.authmanager.model;

import jakarta.validation.constraints.NotBlank;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * OAuth client credentials required to refresh an access token.
 * <p>
 * Contract: both fields are required and non-blank. Passed with the refresh
 * token to
 * {@link io.mosip.kernel.core.authmanager.spi.AuthService#refreshToken(String, String, RefreshTokenRequest)}.
 * Treat {@code clientSecret} as sensitive.
 * </p>
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class RefreshTokenRequest {
	/**
	 * OAuth client identifier; required, non-blank.
	 */
	@NotBlank
	private String clientID;
	/**
	 * OAuth client secret; required, non-blank; never log this value.
	 */
	@NotBlank
	private String clientSecret;
}
