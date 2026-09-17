package io.mosip.kernel.core.authmanager.model;

import lombok.Data;

/**
 * Holds a single user name as returned by lookup APIs.
 * <p>
 * Contract: {@code userName} is null when no user was found. Does not
 * perform I/O.
 * </p>
 *
 * @author Srinivasan
 */
@Data
public class UserNameDto {

	/**
	 * Login or display user name; null if unmapped.
	 */
	private String userName;
}
