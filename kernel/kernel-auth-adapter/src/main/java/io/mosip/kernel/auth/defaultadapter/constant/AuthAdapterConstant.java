/**
 * 
 */
package io.mosip.kernel.auth.defaultadapter.constant;

/**
 * @author Ramadurai Saravana Pandian
 *
 */
public class AuthAdapterConstant {



	public static final String AUTH_ADMIN_COOKIE_PREFIX = "Mosip-Admin-Token";

	public static final String AUTH_REQUEST_COOOKIE_HEADER = "Authorization";

	public static final String AUTH_HEADER_COOKIE = "Cookie";

	public static final String AUTH_HEADER_SET_COOKIE = "Set-Cookie";

	public static final String LOGGER_TARGET = "System.err";

	public static final int UNAUTHORIZED = 403;

	public static final int NOTAUTHENTICATED = 401;

	public static final int INTERNEL_SERVER_ERROR = 500;

	public static final String HTTP_METHOD_NOT_NULL = "Http Method Cannot Be Null";

	public static final String ROLES_NOT_EMPTY_NULL = "Roles Cannot Be Empty or Null";
	
	public final static String APPTYPE_UIN = "UIN";

	public final static String APPTYPE_USERID = "USERID";

	public final static String APPTYPE_USER = "USERIDTYPE";

	public final static String USERPWD_SUCCESS_MESSAGE = "Username and password combination had been validated successfully";

	public final static String CLIENT_SECRET_SUCCESS_MESSAGE = "Clientid and Token combination had been validated successfully";

	public final static String TOKEN_SUCCESS_MESSAGE = "Token had been validated successfully";

	public static final String AUTH_HEADER = "Authorization=";

}
