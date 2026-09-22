/**
 * 
 */
package io.mosip.kernel.auth.defaultimpl.constant;

/**
 * Leftover JWT validation error codes distinct from {@link AuthErrorCode}.
 *
 * @author Ramadurai Pandian
 *
 */
public class AuthErrorConstant {

	/**
	 * Error code reported when a JWT has expired.
	 */
	public final static String JWT_EXPIRED_ERROR_CODE = "AUTH-01";

	/**
	 * Error code reported when a JWT signature check fails.
	 */
	public final static String JWT_SIGNATURE_ERROR_CODE = "AUTH-02";

}
