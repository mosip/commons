package io.mosip.kernel.auth.defaultimpl.dto;

import lombok.Data;

/**
 * Keycloak JWT {@code realm_access} claim. Jackson maps the nested JSON object
 * so {@link #roles} receives the realm role names granted to the token subject.
 *
 * @author Srinivasan
 */
@Data
public class RealmAccessDto {

	/**
	 * Realm-level role names from the JWT {@code realm_access.roles} array.
	 */
	private String[] roles;
}
