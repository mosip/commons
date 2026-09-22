/**
 * Authorization (AuthZ) SPI for MOSIP identity providers.
 */
package io.mosip.kernel.core.authmanager.spi;

import io.mosip.kernel.core.authmanager.model.MosipUserTokenDto;

/**
 * Validates access tokens against the MOSIP identity provider.
 * <p>
 * Contract: implementations perform HTTP (or local JWT verification) and
 * return user details bound to the token. Call this from filters or
 * interceptors before serving a protected resource.
 * </p>
 *
 * @author Ramadurai Pandian
 */
public interface AuthZService {

	/**
	 * Validates the given access token and returns the associated user.
	 * <p>
	 * Contract: {@code token} must be non-null and non-blank. Performs HTTP to
	 * the identity provider unless the implementation verifies JWT locally.
	 * </p>
	 *
	 * @param token never-null, never-blank access token
	 * @return never-null DTO; user and token fields may be null if invalid
	 * @throws Exception when the identity provider call fails
	 */
	MosipUserTokenDto validateToken(String token) throws Exception;

}
