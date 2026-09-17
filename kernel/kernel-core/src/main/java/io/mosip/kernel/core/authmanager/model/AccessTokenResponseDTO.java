package io.mosip.kernel.core.authmanager.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Carries an OAuth/OIDC access token and its lifetime from the token endpoint.
 * <p>
 * Contract: used as a response DTO; fields may be null if the identity
 * provider omitted them. Does not perform I/O.
 * </p>
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class AccessTokenResponseDTO {
	/**
	 * Issued access token string; null if the token endpoint did not return one.
	 */
	private String accessToken;
	/**
	 * Token lifetime as returned by the provider, typically seconds as a decimal
	 * string; null if omitted.
	 */
	private String expiresIn;
}
