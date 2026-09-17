package io.mosip.kernel.core.authmanager.model;

import lombok.Data;

/**
 * Pairs a user identifier with that user's role assignment.
 * <p>
 * Contract: fields may be null on partial DTOs. Does not perform I/O.
 * </p>
 */
@Data
public class UserRoleDto {
	/**
	 * Unique user identifier; may be null.
	 */
	private String userId;
	/**
	 * Role name or comma-separated roles; may be null.
	 */
	private String role;

}
