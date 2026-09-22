package io.mosip.kernel.openid.bridge.api.constants;

/**
 * Error Code for Auth Adapter
 * <p>
 * HTTP-aligned MOSIP error codes used when token validation or authorization
 * fails in the OpenID Bridge / auth-code proxy (typically mapped to 401/403).
 * 
 * @author Urvil Joshi
 * @since 1.0.0
 *
 */
public enum AuthErrorCode {
	/**
	 * UNAUTHORIZED — authentication failed (invalid or missing token). MOSIP code
	 * {@code KER-ATH-401}.
	 */
	UNAUTHORIZED("KER-ATH-401", "Authentication Failed"),
	/**
	 * Compact JWT is not {@code header.payload.signature}. MOSIP code
	 * {@code KER-ATH-401}.
	 */
	INVALID_TOKEN("KER-ATH-401", "Authentication Failed : Invalid Token :"),
	/**
	 * FORBIDDEN — the caller is authenticated but not allowed to access the
	 * resource. MOSIP code {@code KER-ATH-403}.
	 */
	FORBIDDEN("KER-ATH-403", "Forbidden"),
	
	;
	

	/**
	 * The error code
	 */
	private final String errorCode;
	/**
	 * The error message
	 */
	private final String errorMessage;

	/**
	 * Constructor to set error code and message
	 * 
	 * @param errorCode    the error code
	 * @param errorMessage the error message
	 */
	private AuthErrorCode(final String errorCode, final String errorMessage) {
		this.errorCode = errorCode;
		this.errorMessage = errorMessage;
	}

	/**
	 * Function to get error code
	 * 
	 * @return {@link #errorCode}
	 */
	public String getErrorCode() {
		return errorCode;
	}

	/**
	 * Function to get the error message
	 * 
	 * @return {@link #errorMessage}r
	 */
	public String getErrorMessage() {
		return errorMessage;
	}

}
