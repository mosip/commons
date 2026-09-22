/**
 * Username/password login request models for kernel auth-manager SPIs.
 */
package io.mosip.kernel.core.authmanager.model;

import lombok.Data;

/**
 * Username and password login request without an OAuth client id.
 * <p>
 * Contract: deprecated for new callers; prefer {@link LoginUserWithClientId}.
 * All fields must be non-null and non-empty when authenticating. Treat
 * {@code password} as sensitive; do not log it.
 * </p>
 *
 * @author Ramadurai Pandian
 * @see LoginUserWithClientId
 */
@Data
public class LoginUser {

	/**
	 * Login name of the user; must be non-blank.
	 */
	private String userName;
	/**
	 * Clear-text password; must be non-blank; never log this value.
	 */
	private String password;
	/**
	 * MOSIP application identifier the user is logging into; must be non-blank.
	 */
	private String appId;
}
