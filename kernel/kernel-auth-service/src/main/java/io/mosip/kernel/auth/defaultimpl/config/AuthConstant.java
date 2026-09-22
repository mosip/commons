package io.mosip.kernel.auth.defaultimpl.config;

/**
 * Leftover HTTP and JWT constants for cookie headers, Bearer prefix, token-error
 * substrings, and servlet status codes used by auth-manager filters and validators.
 */
public class AuthConstant {

	/**
	 * {@code Authorization=} prefix used when writing a Set-Cookie header value.
	 */
	public static final String AUTH_COOOKIE_HEADER = "Authorization=";

	/**
	 * {@code Bearer } scheme prefix prepended to access tokens.
	 */
	public static final String BEARER = "Bearer ";

	/**
	 * Cookie name prefix for MOSIP admin tokens.
	 */
	public static final String AUTH_ADMIN_COOKIE_PREFIX = "Mosip-Admin-Token";

	/**
	 * Incoming HTTP header name carrying the authorization token.
	 */
	public static final String AUTH_REQUEST_COOOKIE_HEADER = "Authorization";

	/**
	 * Substring matched in JWT library messages when a token has expired.
	 */
	public static final String AUTH_TOKEN_EXPIRED = "JWT expired";

	/**
	 * User-facing message when a token has expired.
	 */
	public static final String AUTH_TOKEN_EXPIRATION_MESSAGE = "Token expired";

	/**
	 * Substring matched in JWT library messages when signature verification fails.
	 */
	public static final String AUTH_SIGNATURE_TEXT = "JWT signature";

	/**
	 * User-facing message when JWT signature verification fails.
	 */
	public static final String AUTH_SIGNATURE_MESSAGE = "Security voilation JWT compromised signature failing";

	/**
	 * User-facing message for a rejected token.
	 */
	public static final String AUTH_INVALID_TOKEN = "Invalid Token";

	/**
	 * HTTP {@code Cookie} request header name.
	 */
	public static final String AUTH_HEADER_COOKIE = "Cookie";

	/**
	 * HTTP {@code Set-Cookie} response header name.
	 */
	public static final String AUTH_HEADER_SET_COOKIE = "Set-Cookie";

	/**
	 * Default logger output target.
	 */
	public static final String LOGGER_TARGET = "System.err";

	/**
	 * HTTP status used for forbidden/unauthorized access in leftover filter code (403).
	 */
	public static final int UNAUTHORIZED = 403;

	/**
	 * HTTP status for unauthenticated requests (401).
	 */
	public static final int NOTAUTHENTICATED = 401;

	/**
	 * HTTP status for unexpected server failures (500).
	 */
	public static final int INTERNEL_SERVER_ERROR = 500;

	/**
	 * Validation message when an HTTP method is missing from a security check.
	 */
	public static final String HTTP_METHOD_NOT_NULL = "Http Method Cannot Be Null";

	/**
	 * Validation message when required roles are missing.
	 */
	public static final String ROLES_NOT_EMPTY_NULL = "Roles Cannot Be Empty or Null";

}
