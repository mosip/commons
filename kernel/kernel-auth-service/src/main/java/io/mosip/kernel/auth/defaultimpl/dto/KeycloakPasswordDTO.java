package io.mosip.kernel.auth.defaultimpl.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Keycloak Admin API credential object placed on {@link KeycloakRequestDto#credentials}.
 * {@link #type} is set to {@code password} and {@link #value} holds the secret.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class KeycloakPasswordDTO {

	/**
	 * Password secret sent to Keycloak when creating or updating a user.
	 */
	private String value;

	/**
	 * Credential type expected by Keycloak, typically {@code password}.
	 */
	private String type;
}
