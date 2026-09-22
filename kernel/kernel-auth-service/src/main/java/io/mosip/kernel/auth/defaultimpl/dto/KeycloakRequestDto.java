package io.mosip.kernel.auth.defaultimpl.dto;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Keycloak Admin API user representation used when creating a realm user.
 * Null or empty collections are omitted from JSON so optional profile fields
 * and credentials are not sent when unused.
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
public class KeycloakRequestDto {

	/**
	 * Keycloak login username. Omitted from JSON when {@code null}.
	 */
	@JsonInclude(value = Include.NON_NULL)
	private String username;

	/**
	 * User given name mapped to Keycloak {@code firstName}. Omitted when {@code null}.
	 */
	@JsonInclude(value = Include.NON_NULL)
	private String firstName;

	/**
	 * User email address. Omitted from JSON when {@code null}.
	 */
	@JsonInclude(value = Include.NON_NULL)
	private String email;

	/**
	 * Custom Keycloak attributes such as {@code mobile} and {@code gender}.
	 * Omitted when empty.
	 */
	@JsonInclude(value = Include.NON_EMPTY)
	private Map<String, List<Object>> attributes;

	/**
	 * Credential list, typically a single {@link KeycloakPasswordDTO} of type {@code password}.
	 * Omitted when empty.
	 */
	@JsonInclude(value = Include.NON_EMPTY)
	private List<KeycloakPasswordDTO> credentials;

	/**
	 * Realm-level role names to assign, for example {@code INDIVIDUAL} for pre-registration users.
	 * Omitted when empty.
	 */
	@JsonInclude(value = Include.NON_EMPTY)
	private List<String> realmRoles;

	/**
	 * Whether the Keycloak account is enabled after creation.
	 */
	private boolean enabled;
}
