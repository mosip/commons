package io.mosip.kernel.core.authmanager.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Confirms which user name a password-set operation applied to.
 * <p>
 * Contract: {@code userName} is null if the operation did not identify a
 * user. Does not perform I/O.
 * </p>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserPasswordResponseDto {

	/**
	 * User name whose password was updated; may be null on failure.
	 */
	private String userName;
}
