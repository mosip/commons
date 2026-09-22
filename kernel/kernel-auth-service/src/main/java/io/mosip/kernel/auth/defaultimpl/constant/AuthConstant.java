/**
 * 
 */
package io.mosip.kernel.auth.defaultimpl.constant;

/**
 * Shared string and numeric constants for auth-manager: application types,
 * success messages, OAuth/Keycloak form field names, OTP channels, JWT claim
 * keys, LDAP leftovers, and Keycloak Admin API query parameters.
 *
 * @author Ramadurai Pandian
 *
 */
public class AuthConstant {

	/**
	 * Application type indicating the subject is a UIN.
	 */
	public final static String APPTYPE_UIN = "UIN";

	/**
	 * Application type indicating the subject is a user id.
	 */
	public final static String APPTYPE_USERID = "USERID";

	/**
	 * Application type token used when the subject is a user-id type.
	 */
	public final static String APPTYPE_USER = "USERIDTYPE";

	/**
	 * Success message after username and password validation.
	 */
	public final static String USERPWD_SUCCESS_MESSAGE = "Username and password combination had been validated successfully";

	/**
	 * Success message after client-id and token validation.
	 */
	public final static String CLIENT_SECRET_SUCCESS_MESSAGE = "Clientid and Token combination had been validated successfully";

	/**
	 * Success message after token validation.
	 */
	public final static String TOKEN_SUCCESS_MESSAGE = "Token had been validated successfully";

	/**
	 * Suffix appended to an application id when resolving a named datasource.
	 */
	public final static String DATASOURCE = "_datasource";

	/**
	 * Legacy LDAP datastore identifier.
	 */
	public static final String LDAP = "ldap";

	/**
	 * Email OTP / notification channel name.
	 */
	public static final String EMAIL = "email";

	/**
	 * Phone OTP / notification channel name.
	 */
	public static final String PHONE = "phone";

	/**
	 * Success message after OTP is dispatched.
	 */
	public static final String OTP_SENT_MESSAGE = "OTP Sent Successfully";

	/**
	 * Success message after OTP validation.
	 */
	public static final String OTP_VALIDATION_MESSAGE = "OTP validated Successfully";

	/**
	 * HTTP header name used for the MOSIP authorization token ({@code Authorization}).
	 */
	public static final String AUTH_COOOKIE_HEADER = "Authorization";

	/**
	 * {@code Authorization=} prefix used when building a Cookie header value.
	 */
	public static final String AUTH_HEADER = "Authorization=";

	/**
	 * Success message after a token has been invalidated (logout).
	 */
	public static final String TOKEN_INVALID_MESSAGE = "Token has been invalidated successfully";

	/**
	 * Message returned when the auth token has expired.
	 */
	public static final String AUTH_TOKEN_EXPIRED_MESSAGE = "Auth token expired ";

	/**
	 * Success message after UIN contact channels are notified.
	 */
	public static final String UIN_NOTIFICATION_MESSAGE = "UIN sent successfully for the channels";

	/**
	 * Default realm role assigned to individual (pre-registration) users.
	 */
	public static final String INDIVIDUAL = "INDIVIDUAL";

	/**
	 * Offset applied when computing a returned expiry time.
	 */
	public static final int RETURN_EXP_TIME = -10;

	/**
	 * Success message when OTP is sent on every configured channel.
	 */
	public static final String ALL_CHANNELS_MESSAGE = "OTP message sent across all the channels";

	/**
	 * Generic success status token used in OTP and notification responses.
	 */
	public static final String SUCCESS_STATUS = "success";

	/**
	 * ID Authentication application identifier.
	 */
	public static final String IDA = "ida";

	/**
	 * HTTP {@code Cookie} header name.
	 */
	public static final String COOKIE = "Cookie";

	/**
	 * Generic failure status token used in OTP and notification responses.
	 */
	public static final String FAILURE_STATUS = "failure";

	/**
	 * JNDI initial context factory class for leftover LDAP binds.
	 */
	public static final String LDAP_INITAL_CONTEXT_FACTORY = "com.sun.jndi.ldap.LdapCtxFactory";

	/**
	 * LDAP password-policy attribute for account lock timestamp.
	 */
	public static final String PWD_ACCOUNT_LOCKED_TIME_ATTRIBUTE = "pwdAccountLockedTime";

	/**
	 * LDAP password-policy attribute for failed bind timestamps.
	 */
	public static final String PWD_FAILURE_TIME_ATTRIBUTE = "pwdFailureTime";

	/**
	 * Validation message used when a required request field is blank.
	 */
	public static final String INVALID_REQUEST = "should not be null or empty";

	/**
	 * Single space separator.
	 */
	public static final String WHITESPACE = " ";

	/**
	 * SMS notification type token.
	 */
	public static final String SMS_NOTIFYTYPE = "SMS";

	/**
	 * Email notification type token.
	 */
	public static final String EMAIL_NOTIFYTYPE = "EMAIL";

	/**
	 * OAuth 2.0 {@code client_credentials} grant type.
	 */
	public static final String CLIENT_CREDENTIALS = "client_credentials";

	/**
	 * OAuth 2.0 form field {@code client_secret}.
	 */
	public static final String CLIENT_SECRET = "client_secret";

	/**
	 * OAuth 2.0 form field {@code client_id}.
	 */
	public static final String CLIENT_ID = "client_id";

	/**
	 * OAuth 2.0 / Keycloak credential type and form field {@code password}.
	 */
	public static final String PASSWORDCONSTANT = "password";

	/**
	 * OAuth 2.0 form field {@code grant_type}.
	 */
	public static final String GRANT_TYPE = "grant_type";

	/**
	 * OAuth 2.0 form field {@code username}.
	 */
	public static final String USER_NAME = "username";

	/**
	 * Comma separator used when joining roles or lists.
	 */
	public static final String COMMA = ",";

	/**
	 * Registration id (RID) attribute and claim key.
	 */
	public static final String RID = "rid";

	/**
	 * Keycloak user attribute and claim key for mobile number.
	 */
	public static final String MOBILE = "mobile";

	/**
	 * JWT claim {@code preferred_username}.
	 */
	public static final String PREFERRED_USERNAME = "preferred_username";

	/**
	 * JWT claim {@code realm_access} holding realm roles.
	 */
	public static final String REALM_ACCESS = "realm_access";

	/**
	 * Path or form parameter name for the Keycloak realm id.
	 */
	public static final String REALM_ID = "realmId";

	/**
	 * OAuth 2.0 form field and grant type {@code refresh_token}.
	 */
	public static final String REFRESH_TOKEN = "refresh_token";

	/**
	 * Pre-registration application identifier.
	 */
	public static final String PRE_REGISTRATION="preregistration";

	/**
	 * JWT / Keycloak JSON field name for realm roles.
	 */
	public static final String ROLES = "roles";

	/**
	 * Camel-case individual-id attribute name.
	 */
	public static final String INDIVIDUAL_ID = "individualId";

	/**
	 * Lower-case individual-id attribute name used in some Keycloak mappings.
	 */
	public static final String INDIVIDUALID = "individualid";

	/**
	 * Keycloak realm path or query parameter name.
	 */
	public static final String REALM = "realm";

	/**
	 * Keycloak Admin API query parameter for role-name lookup.
	 */
	public static final String ROLE_NAME="role-name";

	/**
	 * Keycloak Admin API {@code exact} query flag.
	 */
	public static final String EXACT = "exact";

	/**
	 * Keycloak Admin API {@code briefRepresentation} query flag.
	 */
	public static final String BRIEF_REPRESENTATION = "briefRepresentation";

	/**
	 * Keycloak Admin API {@code max} page-size query parameter.
	 */
	public static final String MAX = "max";

	/**
	 * Keycloak user JSON field {@code firstName}.
	 */
	public static final String FIRST_NAME = "firstName";

	/**
	 * Keycloak user JSON field {@code lastName}.
	 */
	public static final String LAST_NAME = "lastName";

	/**
	 * Keycloak user JSON field {@code attributes}.
	 */
	public static final String ATTRIBUTES = "attributes";

	/**
	 * Generic {@code name} JSON or query field.
	 */
	public static final String NAME = "name";

	/**
	 * Keycloak Admin API {@code search} query parameter.
	 */
	public static final String SEARCH = "search";

	/**
	 * Keycloak Admin API {@code first} pagination offset.
	 */
	public static final String FIRST = "first";

	/**
	 * Generic {@code id} JSON or query field.
	 */
	public static final String ID = "id";

}
