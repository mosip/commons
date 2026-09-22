package io.mosip.kernel.auth.defaultimpl.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Keycloak realm role identifier used when assigning a role (for example
 * {@code INDIVIDUAL}) to a newly created user. Null fields are omitted from JSON.
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Roles {

	/**
	 * Keycloak role UUID. Omitted from JSON when {@code null}.
	 */
	@JsonInclude(value = Include.NON_NULL)
	private String id;

	/**
	 * Realm role name, such as {@code INDIVIDUAL}. Omitted when {@code null}.
	 */
	@JsonInclude(value = Include.NON_NULL)
	private String name;
}
