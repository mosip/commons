package io.mosip.kernel.openid.bridge.dto;

import lombok.Data;

/**
 * Error body returned by Keycloak (or a compatible IAM) on failed token or
 * admin calls, matching OAuth 2.0 {@code error} / {@code error_description}.
 */
@Data
public class IAMErrorResponseDto {

	/** OAuth 2.0 / Keycloak error code (for example {@code invalid_grant}). */
	private String error;

	/** Human-readable description of {@link #error} from the IAM. */
	private String error_description;
}
