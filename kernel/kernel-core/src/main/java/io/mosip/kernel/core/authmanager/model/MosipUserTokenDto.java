package io.mosip.kernel.core.authmanager.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Authenticated user plus access/refresh tokens and expiry metadata.
 * <p>
 * Contract: returned by
 * {@link io.mosip.kernel.core.authmanager.spi.AuthZService#validateToken(String)}.
 * Token fields are null when validation fails; {@code status} and
 * {@code message} describe the outcome. Expiry values are epoch millis.
 * Treat tokens as sensitive. Does not perform I/O.
 * </p>
 *
 * @author Sabbu Uday Kumar
 * @since 1.0.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MosipUserTokenDto {
	/**
	 * User profile associated with the token; null when validation failed.
	 */
	private MosipUserDto mosipUserDto;
	/**
	 * Access token that was validated or newly issued; sensitive; may be null.
	 */
	private String token;
	/**
	 * Refresh token; sensitive; may be null if not issued.
	 */
	private String refreshToken;
	/**
	 * Access-token expiry as epoch milliseconds; {@code 0} if unknown.
	 */
	private long expTime;
	/**
	 * Human-readable outcome description; may be null or empty.
	 */
	private String message;
	/**
	 * High-level outcome such as {@code SUCCESS} or {@code FAILURE}; may be null.
	 */
	private String status;
	/**
	 * Refresh-token expiry as epoch milliseconds; {@code 0} if unknown.
	 */
	private long refreshExpTime;
}
