package io.mosip.kernel.core.authmanager.model;

import java.io.Serializable;

/**
 * Serializable MOSIP role definition used in role-list responses.
 * <p>
 * Contract: fields may be null on partial records. Does not perform I/O.
 * </p>
 *
 * @author Sabbu Uday Kumar
 * @since 1.0.0
 */
public class Role implements Serializable {

	/**
	 * Serializable version ID.
	 */
	private static final long serialVersionUID = -2706412783263468725L;

	/**
	 * Unique role identifier in the identity store; may be null.
	 */
	private String roleId;

	/**
	 * Display name of the role; may be null.
	 */
	private String roleName;

	/**
	 * Human-readable description of the role; may be null or empty.
	 */
	private String roleDescription;

	/**
	 * Returns the unique role identifier.
	 *
	 * @return role id; may be null
	 */
	public String getRoleId() {
		return roleId;
	}

	/**
	 * Sets the unique role identifier.
	 *
	 * @param roleId role id; may be null
	 */
	public void setRoleId(String roleId) {
		this.roleId = roleId;
	}

	/**
	 * Returns the display name of the role.
	 *
	 * @return role name; may be null
	 */
	public String getRoleName() {
		return roleName;
	}

	/**
	 * Sets the display name of the role.
	 *
	 * @param roleName role name; may be null
	 */
	public void setRoleName(String roleName) {
		this.roleName = roleName;
	}

	/**
	 * Returns the human-readable role description.
	 *
	 * @return description; may be null or empty
	 */
	public String getRoleDescription() {
		return roleDescription;
	}

	/**
	 * Sets the human-readable role description.
	 *
	 * @param roleDescription description; may be null
	 */
	public void setRoleDescription(String roleDescription) {
		this.roleDescription = roleDescription;
	}

}
