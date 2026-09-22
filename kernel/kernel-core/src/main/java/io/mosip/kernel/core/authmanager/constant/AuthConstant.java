package io.mosip.kernel.core.authmanager.constant;

/**
 * Shared string constants used by auth-manager request validation.
 * <p>
 * Contract: this type is not instantiable. Callers use the public constants
 * as validation messages; they are not configuration keys.
 * </p>
 */
public class AuthConstant {

	/**
	 * Prevents instantiation of this constants holder.
	 */
	private AuthConstant() {
	}

	/**
	 * Validation message indicating a required request field is null or empty.
	 */
	public static final String INVALID_REQUEST = "should not be null or empty";

}
