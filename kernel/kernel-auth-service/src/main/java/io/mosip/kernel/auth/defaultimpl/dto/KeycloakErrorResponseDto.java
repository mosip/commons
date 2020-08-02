package io.mosip.kernel.auth.defaultimpl.dto;

import lombok.Data;

/**
 * Instantiates a new keycloak error response dto.
 * 
 * @author srinivasan
 */
@Data
public class KeycloakErrorResponseDto {

	/** The error. */
	private String error;

	/** The error description. */
	private String error_description;
}
