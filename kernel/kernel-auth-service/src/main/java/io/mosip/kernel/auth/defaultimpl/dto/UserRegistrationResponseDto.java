package io.mosip.kernel.auth.defaultimpl.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Acknowledgement returned after a Keycloak user is created, carrying the new login name.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserRegistrationResponseDto {

	/**
	 * User name of the created Keycloak account.
	 */
	private String userName;

}
