/**
 * User-salt models for kernel auth-manager SPIs.
 */
package io.mosip.kernel.core.authmanager.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Pairs a user identifier with that user's password salt.
 * <p>
 * Contract: used when hashing or verifying passwords. Treat {@code salt} as
 * sensitive. Does not perform I/O.
 * </p>
 *
 * @author Ramadurai Pandian
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MosipUserSalt {

	/**
	 * User identifier the salt belongs to; may be null on partial DTOs.
	 */
	private String userId;
	/**
	 * Password salt for the user; may be null if none is stored; sensitive.
	 */
	private String salt;

}
