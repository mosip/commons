package io.mosip.kernel.auth.defaultimpl.dto;

import lombok.Data;

/**
 * Keycloak token or admin API error JSON ({@code error} and {@code error_description}).
 * Field {@link #error_description} keeps the Keycloak snake_case name for Jackson mapping.
 *
 * @author srinivasan
 */
@Data
public class KeycloakErrorResponseDto {

	/**
	 * Short Keycloak error code, for example {@code invalid_grant} or {@code unauthorized_client}.
	 */
	private String error;

	/**
	 * Human-readable Keycloak error description from the JSON field {@code error_description}.
	 */
	private String error_description;
}
