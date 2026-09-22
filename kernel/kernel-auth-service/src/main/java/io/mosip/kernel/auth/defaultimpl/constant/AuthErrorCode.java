package io.mosip.kernel.auth.defaultimpl.constant;

/**
 * Error Code for Auth Service.
 * MOSIP {@code KER-ATH-*} codes returned by authmanager on :8091
 * {@code /v1/authmanager} when IAM (Keycloak) or request validation fails.
 * 
 * @author Ramadurai Pandian
 * @since 1.0.0
 *
 */
public enum AuthErrorCode {

	/**
	 * UNAUTHORIZED
	 */
	UNAUTHORIZED("KER-ATH-401", "Authentication Failed"),
	/**
	 * FORBIDDEN
	 */
	FORBIDDEN("KER-ATH-403", "Forbidden : "),
	/**
	 * Token expired
	 */
	TOKEN_EXPIRED("KER-ATH-402", "Token expired"),
	/**
	 * Invalid Token
	 */
	INVALID_TOKEN("KER-ATH-401", "Authentication Failed : Invalid Token :"),
	/**
	 * FORBIDDEN
	 */
	CONNECT_EXCEPTION("KER-ATH-002", "Fail to connect to auth service"),
	/**
	 * RESPONSE_PARSE_ERROR
	 */
	RESPONSE_PARSE_ERROR("KER-ATH-001", "Error occur while parsing error from response"),
	/**
	 * RESPONSE_PARSE_ERROR
	 */
	REQUEST_VALIDATION_ERROR("KER-ATH-004", "Error while validating the request"),
	/**
	 * USER VALIDATION ERROR
	 */
	USER_VALIDATION_ERROR("KER-ATH-003", "User Detail doesn't exist"),

	/**
	 * PASSWORD VALIDATION ERROR
	 */
	PASSWORD_VALIDATION_ERROR("KER-ATH-005", "Incorrect Password"),

	/**
	 * Empty Cookie error
	 */
	COOKIE_NOTPRESENT_ERROR("KER-ATH-006", "Cookies are empty"),
	/**
	 * Empty Cookie error
	 */
	TOKEN_NOTPRESENT_ERROR("KER-ATH-007", "Token is not present in cookies"),
	/**
	 * Token Datastore error
	 */
	TOKEN_DATASTORE_ERROR("KER-ATH-008", "Token is not present in datastore,Please try with new token"),
	/**
	 * Client error
	 */
	SERVER_ERROR("KER-ATH-500", "Server error occured,Please check the logs "),

	/**
	 * OTP or notification template is missing for the requested language.
	 */
	TEMPLATE_ERROR("KER-ATH-436", " Template is missing for language "),

	/**
	 * Client error
	 */
	CLIENT_ERROR("KER-ATH-501", "Client error occured with message "),

	/**
	 * Empty Cookie error
	 */
	INVALID_DATASOURCE_ERROR("KER-ATH-008", "Invalid datasource and Please check the application id"),

	/**
	 * JNDI naming failure against LDAP.
	 */
	NAMING_EXCEPTION("KER-ATH-009", "Naming Exception occured"),

	/**
	 * Failure while closing an LDAP context.
	 */
	UNABLE_CLOSE_LDAP_CONTEXT("KER-ATH-010", "Exception occured while closing the context"),

	/**
	 * New password does not satisfy IAM password policy.
	 */
	PASSWORD_POLICY_EXCEPTION("KER-ATH-011", "Password that is entered does not meet the password policy"),

	/**
	 * Old password did not match during change-password.
	 */
	OLD_PASSWORD_NOT_MATCH("KER-ATH-012", "Old password entered is incorrect"),

	/**
	 * Username already exists in the user store.
	 */
	USER_ALREADY_EXIST("KER-ATH-013", "Username already exist in datastore"),

	/**
	 * Required role was not found while creating a user.
	 */
	ROLE_NOT_FOUND("KER-ATH-014", "Exception occured while creating a user "),

	/**
	 * User creation in IAM failed.
	 */
	USER_CREATE_EXCEPTION("KER-ATH-015", "Exception occured while creating a user "),

	/**
	 * Password insert/update for a new user failed.
	 */
	USER_PASSWORD_EXCEPTION("KER-ATH-016", "Exception occured while inserting a password for user "),

	/**
	 * Rollback of a partially created user failed.
	 */
	ROLLBACK_USER_EXCEPTION("KER-ATH-017", "Exception occured while rolling back user"),

	/**
	 * LDAP distinguished name could not be built.
	 */
	INVALID_DN("KER-ATH-018", "Exception occured while creating DN"),

	/**
	 * I/O failure while handling a paging request.
	 */
	IO_EXCEPTION("KER-ATH-020", "IO Exception occured while passing paging request"),
	/**
	 * Unchecked runtime failure.
	 */
	RUNTIME_EXCEPTION("KER-ATH-021", "Runtime exception"),

	/**
	 * Request field is null or empty.
	 */
	INVALID_REQUEST("KER-ATH-019", "should not be null or empty"),

	/**
	 * User does not exist in IAM.
	 */
	USER_NOT_FOUND("KER-ATH-022", "User not found"),

	/**
	 * User record has no isActive flag.
	 */
	IS_ACTIVE_FLAG_NOT_FOUND("KER-ATH-023", "IsActive flag is not present for this user"),

	/**
	 * Mobile number is missing or already registered (message as stored).
	 */
	MOBILE_NOT_REGISTERED("KER-ATH-024", "Mobile is registered/not present"),

	/**
	 * Downstream HTTP/REST call failed.
	 */
	REST_EXCEPTION("KER-ATH-025", "Exception occured while consuming service"),

	/**
	 * Failed to obtain an access token from Keycloak.
	 */
	KEYCLOAK_ACESSTOKEN_EXCEPTION("KER-ATH-021", "Error Occured while getting access token from keycloak"),
	/**
	 * OAuth state parameter did not match.
	 */
	KEYCLOAK_STATE_EXCEPTION("KER-ATH-022", "state is not maching"),
	/**
	 * Username/password or client credentials were rejected.
	 */
	INVALID_CREDENTIALS("KER-ATH-023","Invalid Credentials"),
	/**
	 * Keycloak realm for the application was not found ({@code %s} is the realm).
	 */
	REALM_NOT_FOUND("KER-ATH-026","Realm not found:: %s"),
	/**
	 * User has no individualId attribute.
	 */
	INDIVIDUAL_ID_NOT_FOUND("KER-ATH-027", "Individual Id not found for user"),
	/**
	 * Redirect/URI host is not in the allowed-domains list.
	 */
	DOMAIN_EXCEPTION("KER-ATH-028", "uri's domain name not found in allowed domains");

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
