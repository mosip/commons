package io.mosip.kernel.core.hotlist.constant;

/**
 * Status values applied to a hotlisted identifier.
 * <p>
 * Contract: this type is not instantiable. Use {@link #BLOCKED} to deny
 * authentication or registration for an identifier and {@link #UNBLOCKED} to
 * restore it. Values must match hotlist API strings exactly.
 * </p>
 *
 * @author Manoj SP
 */
public class HotlistStatus {

	/**
	 * Identifier is blocked and must be rejected by consuming services.
	 */
	public static final String BLOCKED = "BLOCKED";

	/**
	 * Identifier is not blocked (or has been unblocked).
	 */
	public static final String UNBLOCKED = "UNBLOCKED";
}
