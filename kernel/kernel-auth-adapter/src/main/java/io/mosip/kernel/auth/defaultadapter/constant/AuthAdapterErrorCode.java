package io.mosip.kernel.auth.defaultadapter.constant;

/**
 * Error codes and default messages for this adapter.
 * <p>
 * Hosting MOSIP services see these codes in 401/403 envelopes produced by
 * {@link io.mosip.kernel.auth.defaultadapter.filter.AuthFilter} and
 * {@link io.mosip.kernel.auth.defaultadapter.exception.AuthAdapterExceptionHandler}.
 * <p>
 * This adapter is a library other MOSIP services put on the classpath.
 *
 * @author Urvil Joshi
 * @since 1.0.0
 */
public enum AuthAdapterErrorCode {
	/**
	 * HTTP 401 authentication failure ({@code KER-ATH-401}).
	 */
	UNAUTHORIZED("KER-ATH-401", "Authentication Failed"),
	/**
	 * HTTP 401 for a compact JWT that is not {@code header.payload.signature}
	 * ({@code KER-ATH-401}).
	 */
	INVALID_TOKEN("KER-ATH-401", "Authentication Failed : Invalid Token :"),
	/**
	 * HTTP 403 authorization failure ({@code KER-ATH-403}).
	 */
	FORBIDDEN("KER-ATH-403", "Forbidden"),
	/**
	 * Failure to reach the auth/OIDC service ({@code KER-ATH-002}).
	 */
	CONNECT_EXCEPTION("KER-ATH-002", "Fail to connect to auth service"),
	/**
	 * Failure while parsing an error payload from a remote response
	 * ({@code KER-ATH-001}).
	 */
	RESPONSE_PARSE_ERROR("KER-ATH-001", "Error occur while parsing error from response"),
	
	/**
	 * Problem connecting to the auth service ({@code KER-ATH-003}).
	 */
	CANNOT_CONNECT_TO_AUTH_SERVICE("KER-ATH-003","Problem in connecting to auth service"),
	
	/**
	 * I/O failure during token or user-info calls ({@code KER-ATH-004}).
	 */
	IO_EXCEPTION("KER-ATH-004", "IO Exception occured"),
	
	/**
	 * Cached client-credentials token is null after a fetch attempt
	 * ({@code KER-ATH-005}).
	 */
	SELF_AUTH_TOKEN_NULL("KER-ATH-005","Self cached auth token is null"),
	
	/**
	 * Application id has no realm in {@code mosip.kernel.auth.appids.realm.map}
	 * ({@code KER-ATH-006}).
	 */
	REALM_NOT_FOUND("KER-ATH-006", "RealM Not found in configured map."),
	
	/**
	 * Offline local-profile token validation is no longer supported
	 * ({@code KER-ATH-007}).
	 */
	OFFLINE_AUTH_DEPRECATED("KER-ATH-007", "Offline Auth Not Supported.");

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
	private AuthAdapterErrorCode(final String errorCode, final String errorMessage) {
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
