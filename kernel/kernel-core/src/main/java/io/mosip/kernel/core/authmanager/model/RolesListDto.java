package io.mosip.kernel.core.authmanager.model;

import java.io.Serializable;
import java.util.List;

/**
 * Serializable wrapper for a list of {@link Role} records.
 * <p>
 * Contract: returned by
 * {@link io.mosip.kernel.core.authmanager.spi.AuthService#getAllRoles(String)}.
 * {@code roles} may be null or empty. Does not perform I/O.
 * </p>
 *
 * @author Sabbu Uday Kumar
 * @since 1.0.0
 */
public class RolesListDto implements Serializable {
	/**
	 * Serializable version ID.
	 */
	private static final long serialVersionUID = -5863653796023079898L;

	/**
	 * Roles for the requested application; may be null or empty.
	 */
	List<Role> roles;

	/**
	 * Returns the roles in this response.
	 *
	 * @return role list; may be null or empty
	 */
	public List<Role> getRoles() {
		return roles;
	}

	/**
	 * Replaces the roles in this response.
	 *
	 * @param roles role list to store; may be null
	 */
	public void setRoles(List<Role> roles) {
		this.roles = roles;
	}

}
