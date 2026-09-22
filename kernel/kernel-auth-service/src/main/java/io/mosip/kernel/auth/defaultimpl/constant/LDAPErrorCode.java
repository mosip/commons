/**
 * 
 */
package io.mosip.kernel.auth.defaultimpl.constant;

/**
 * LDAP user-store error codes ({@code KER-ATH-10x}) retained for the
 * historical LDAP IAM path.
 *
 * @author Ramadurai Pandian
 *
 */
public enum LDAPErrorCode {
	/**
	 * LDAP leftover: bind or connection failure
	 */
	LDAP_CONNECTION_ERROR("KER-ATH-101", "Error while connecting ldap request,Please check credentials"),

	/**
	 * LDAP leftover: request validation failure
	 */
	LDAP_PARSE_REQUEST_ERROR("KER-ATH-102", "Error while validating the ldap request"),

	/**
	 * LDAP leftover: unable to read user or role entries
	 */
	LDAP_ROLES_REQUEST_ERROR("KER-ATH-103", "Unable to fetch details from LDAP");

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
	private LDAPErrorCode(final String errorCode, final String errorMessage) {
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
