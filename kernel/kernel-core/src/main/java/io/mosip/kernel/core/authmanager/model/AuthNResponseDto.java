/**
 * Authentication request and response models for kernel auth-manager SPIs.
 */
package io.mosip.kernel.core.authmanager.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Authentication result that includes access and refresh tokens plus expiry.
 * <p>
 * Contract: returned by {@link io.mosip.kernel.core.authmanager.spi.AuthNService}
 * after a successful login. Token fields are null on failure; {@code status}
 * and {@code message} describe the outcome. Expiry values are epoch millis
 * unless the identity provider documents otherwise. Does not perform I/O.
 * </p>
 *
 * @author Ramadurai Pandian
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthNResponseDto {

	/**
	 * Issued access token; null when authentication failed.
	 */
	private String token;

	/**
	 * Human-readable outcome description; may be null or empty.
	 */
	private String message;

	/**
	 * Issued refresh token; null when the provider did not issue one.
	 */
	private String refreshToken;

	/**
	 * Access-token expiry as epoch milliseconds; {@code 0} if unknown.
	 */
	private long expiryTime;

	/**
	 * Authenticated user identifier; null on failure.
	 */
	private String userId;

	/**
	 * High-level outcome such as {@code SUCCESS} or {@code FAILURE}; may be null.
	 */
	private String status;

	/**
	 * Refresh-token expiry as epoch milliseconds; {@code 0} if unknown.
	 */
	private long refreshExpiryTime;

}
