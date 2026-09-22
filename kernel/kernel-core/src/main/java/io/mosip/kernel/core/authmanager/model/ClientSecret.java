/**
 * Client-credential request models for kernel auth-manager SPIs.
 */
package io.mosip.kernel.core.authmanager.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Client-id and secret used to authenticate a confidential OAuth client.
 * <p>
 * Contract: all fields must be non-null and non-empty when passed to
 * {@link io.mosip.kernel.core.authmanager.spi.AuthNService#authenticateWithSecretKey(ClientSecret)}.
 * Treat {@code secretKey} as sensitive; do not log it.
 * </p>
 *
 * @author Ramadurai Pandian
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClientSecret {

	/**
	 * OAuth client identifier; must be non-blank for authentication.
	 */
	private String clientId;
	/**
	 * OAuth client secret; must be non-blank; never log this value.
	 */
	private String secretKey;
	/**
	 * MOSIP application identifier that owns the client; must be non-blank.
	 */
	private String appId;
}
