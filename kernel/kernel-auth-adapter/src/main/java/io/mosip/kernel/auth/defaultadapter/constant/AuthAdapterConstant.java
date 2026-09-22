/**
 * 
 */
package io.mosip.kernel.auth.defaultadapter.constant;

/**
 * Header, cookie, claim, and HTTP-status constants used across this adapter.
 * <p>
 * This adapter is a library other MOSIP services put on the classpath.
 *
 * @author Ramadurai Saravana Pandian
 */
public class AuthAdapterConstant {



	/**
	 * Legacy admin cookie name prefix.
	 */
	public static final String AUTH_ADMIN_COOKIE_PREFIX = "Mosip-Admin-Token";

	/**
	 * Cookie name fragment and HTTP header name for the access token
	 * ({@code Authorization}).
	 */
	public static final String AUTH_REQUEST_COOOKIE_HEADER = "Authorization";

	/**
	 * HTTP header used to send {@code Authorization=<jwt>} on outbound calls.
	 */
	public static final String AUTH_HEADER_COOKIE = "Cookie";

	/**
	 * HTTP header used to return a token cookie on Vert.x responses.
	 */
	public static final String AUTH_HEADER_SET_COOKIE = "Set-Cookie";

	/**
	 * Historical logger target name (unused by current {@link io.mosip.kernel.auth.defaultadapter.config.LoggerConfiguration}).
	 */
	public static final String LOGGER_TARGET = "System.err";

	/**
	 * HTTP status used for Vert.x forbidden responses (numeric 403).
	 */
	public static final int UNAUTHORIZED = 403;

	/**
	 * HTTP status used for Vert.x unauthenticated responses (numeric 401).
	 */
	public static final int NOTAUTHENTICATED = 401;

	/**
	 * HTTP status used for Vert.x connect failures (numeric 500).
	 */
	public static final int INTERNEL_SERVER_ERROR = 500;

	/**
	 * Message when a Vert.x route is registered without an HTTP method.
	 */
	public static final String HTTP_METHOD_NOT_NULL = "Http Method Cannot Be Null";

	/**
	 * Message when Vert.x role validation is invoked with empty roles.
	 */
	public static final String ROLES_NOT_EMPTY_NULL = "Roles Cannot Be Empty or Null";
	
	/**
	 * Application type constant for UIN-based login.
	 */
	public final static String APPTYPE_UIN = "UIN";

	/**
	 * Application type constant for user-id login.
	 */
	public final static String APPTYPE_USERID = "USERID";

	/**
	 * Application type constant for user-id-type login.
	 */
	public final static String APPTYPE_USER = "USERIDTYPE";

	/**
	 * Success message for username/password validation.
	 */
	public final static String USERPWD_SUCCESS_MESSAGE = "Username and password combination had been validated successfully";

	/**
	 * Success message for client-id/secret validation.
	 */
	public final static String CLIENT_SECRET_SUCCESS_MESSAGE = "Clientid and Token combination had been validated successfully";

	/**
	 * Success message for token validation.
	 */
	public final static String TOKEN_SUCCESS_MESSAGE = "Token had been validated successfully";

	/**
	 * Cookie value prefix written as {@code Authorization=<jwt>}.
	 */
	public static final String AUTH_HEADER = "Authorization=";

	/**
	 * JWT claim name for email.
	 */
	public static final String EMAIL = "email";

	/**
	 * JWT claim name for mobile.
	 */
	public static final String MOBILE = "mobile";

	/**
	 * JWT claim name for roles when {@code realm_access} is absent.
	 */
	public static final String ROLES = "roles";

	/**
	 * Vert.x routing-context key storing {@code MosipUserDto}.
	 */
	public static final String ROUTING_CONTEXT_USER = "RoutingCtxUserName";

	/**
	 * JWT claim name for Keycloak {@code realm_access}.
	 */
	public static final String REALM_ACCESS = "realm_access";

	/**
	 * Role-list delimiter.
	 */
	public static final String COMMA = ",";

	/**
	 * JWT claim name for authorized party, used as audience fallback.
	 */
	public static final String AZP = "azp";

	/**
	 * JWT claim name for preferred username.
	 */
	public static final String PREFERRED_USERNAME = "preferred_username";

	/**
	 * JWT claim name for issuer.
	 */
	public static final String ISSUER = "iss";

	/**
	 * JWT claim name for audience.
	 */
	public static final String AUDIENCE = "aud";

	/**
	 * Bearer scheme prefix for the user-info {@code Authorization} header.
	 */
	public static final String BEARER_STR = "Bearer ";

	/**
	 * OIDC error JSON field {@code error_description}.
	 */
	public static final String ERROR_DESC = "error_description";

	/**
	 * OIDC error JSON field {@code error}.
	 */
	public static final String ERROR = "error";

	/**
	 * OAuth form field {@code grant_type}.
	 */
	public static final String GRANT_TYPE = "grant_type";

	/**
	 * OAuth form field {@code client_secret}.
	 */
	public static final String CLIENT_SECRET = "client_secret";

	/**
	 * OAuth form field {@code client_id}.
	 */
	public static final String CLIENT_ID = "client_id";

	/**
	 * OAuth grant type {@code client_credentials}.
	 */
	public static final String CLIENT_CREDENTIALS = "client_credentials";

	/**
	 * Token-endpoint JSON field {@code access_token}.
	 */
	public static final String ACCESS_TOKEN = "access_token";

	/**
	 * JWT claim name for OAuth scopes.
	 */
	public static final String SCOPE = "scope";
	/**
	 * Environment property key for the id-token cookie name.
	 */
	public static final String ID_TOKEN = "idToken";
	
	/**
	 * Compliance Toolkit query parameter {@code ctkTestCaseId}.
	 */
	public static final String CTK_TEST_CASE_ID = "ctkTestCaseId";
	
	/**
	 * Compliance Toolkit query parameter {@code ctkTestRunId}.
	 */
	public static final String CTK_TEST_RUN_ID = "ctkTestRunId";
	
	/**
	 * Compliance Toolkit request-body field {@code partnerId}.
	 */
	public static final String PARTNER_ID = "partnerId";
	
	/**
	 * Generic token field name.
	 */
	public static final String TOKEN = "token";
}
