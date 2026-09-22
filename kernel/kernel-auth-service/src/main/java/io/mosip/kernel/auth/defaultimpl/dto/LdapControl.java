package io.mosip.kernel.auth.defaultimpl.dto;

import javax.naming.ldap.Control;

/**
 * Leftover JNDI LDAP control that requests the password-policy response control
 * (OID {@code 1.3.6.1.4.1.42.2.27.8.5.1}). The encoded value is unused ({@code null});
 * the control is marked critical.
 */
public class LdapControl implements Control {

	/**
	 * Serialization identifier for this LDAP control implementation.
	 */
	private static final long serialVersionUID = 8917803695932582767L;

	/**
	 * Returns the BER-encoded control value.
	 *
	 * @return always {@code null}; this leftover control does not carry encoded data
	 */
	public byte[] getEncodedValue() {
		return null;
	}

	/**
	 * Returns the LDAP password-policy control object identifier.
	 *
	 * @return OID {@code 1.3.6.1.4.1.42.2.27.8.5.1}
	 */
	public String getID() {
		return "1.3.6.1.4.1.42.2.27.8.5.1";
	}

	/**
	 * Indicates whether the directory must honor this control.
	 *
	 * @return {@code true} so the bind fails if the server does not support the control
	 */
	public boolean isCritical() {
		return true;
	}

	/**
	 * Wraps this instance as a single-element control array for JNDI environment use.
	 *
	 * @return an array containing only {@code this}
	 */
	public Control[] getControls() {
		return new Control[] { this };
	}
}
