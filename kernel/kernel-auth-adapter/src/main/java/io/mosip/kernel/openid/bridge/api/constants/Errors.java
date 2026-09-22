package io.mosip.kernel.openid.bridge.api.constants;

/**
 * MOSIP error codes for the authorization-code proxy ({@code KER-ACP-*}) and
 * related auth failures.
 * <p>
 * Used by {@link io.mosip.kernel.openid.bridge.api.exception.ClientException},
 * {@link io.mosip.kernel.openid.bridge.api.exception.ServiceException}, and
 * {@link io.mosip.kernel.openid.bridge.api.exception.AuthRestException}.
 * <p>
 * TODO: Use from core when adapter api will merge.
 */
public enum Errors {

	/**
	 * Empty Cookie error — no cookies on the request.
	 */
	COOKIE_NOTPRESENT_ERROR("KER-ACP-001", "Cookies are empty"),
	/**
	 * Empty Cookie error — Authorization cookie has no token value.
	 */
	TOKEN_NOTPRESENT_ERROR("KER-ACP-002", "Token is not present in cookies"),
	/**
	 * IO Exception while reading an HTTP or paging payload.
	 */
	IO_EXCEPTION("KER-ACP-003", "IO Exception occured while passing paging request"),
	/**
	 * Cannot connect to auth service (auth manager).
	 */

	CANNOT_CONNECT_TO_AUTH_SERVICE("KER-ACP-004", "Problem in connecting to auth service"),

	/**
	 * RESPONSE_PARSE_ERROR — failed to deserialize IAM or MOSIP JSON.
	 */
	RESPONSE_PARSE_ERROR("KER-ACP-005", "Error occur while parsing error from response"),

	/**
	 * Failed to obtain an access token from Keycloak/IAM (token endpoint).
	 */
	ACESSTOKEN_EXCEPTION("KER-ACP-006", "Error Occured while getting access token from iam"),

	/**
	 * CSRF {@code state} from the IdP does not match the state cookie.
	 */
	STATE_EXCEPTION("KER-ACP-007", "state is not maching"),
	
	/**
	 * Generic HTTP client/server error while calling a downstream service.
	 */
	REST_EXCEPTION("KER-ACP-008", "Exception occured while consuming service"),
	/**
	 * Access token is invalid; MOSIP code {@code KER-ATH-401}.
	 */
	INVALID_TOKEN("KER-ATH-401", "Authentication Failed : Invalid Token :"),
	/**
	 * Unclassified proxy failure ({@code KER-ACP-500}).
	 */
	EXCEPTION("KER-ACP-500", "Exception occured "),
	/**
	 * Redirect or target URL is not in the configured allow-list.
	 */
	ALLOWED_URL_EXCEPTION("KER-ACP-009", "url not found in allowed url's"),
	/**
	 * CSRF {@code state} is null or empty.
	 */
	STATE_NULL_EXCEPTION("KER-ACP-010", "state is null or empty"),
	/**
	 * CSRF {@code state} is present but is not a UUID.
	 */
	STATE_NOT_UUID_EXCEPTION("KER-ACP-011", "state is not uuid"),
	/**
	 * Character encoding failed while URL-encoding OIDC query parameters.
	 */
	UNSUPPORTED_ENCODING_EXCEPTION("KER-ACP-012", "unsupported encoding exception :"),
	/**
	 * Keymanager failed to sign the private-key JWT client assertion.
	 */
	JWT_SIGN_EXCEPTION("KER-ACP-013", "Failed to sign jwt" ),
	/**
	 * Jackson failed while building client-assertion JSON claims.
	 */
	JSON_PROCESSING_EXCEPTION("KER-ACP-014", "Json Processing Exception");

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
	private Errors(final String errorCode, final String errorMessage) {
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
