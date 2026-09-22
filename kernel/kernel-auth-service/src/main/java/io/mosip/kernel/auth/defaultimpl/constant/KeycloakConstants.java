package io.mosip.kernel.auth.defaultimpl.constant;

/**
 * Constants related to KEYCLOAK
 *
 * @author Urvil Joshi
 *
 */
public class KeycloakConstants {

	/**
	 * Private constructor
	 */
	private KeycloakConstants() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * OAuth 2.0 {@code redirect_uri} used in authorization-code and logout flows.
	 */
	public static final String REDIRECT_URI = "redirect_uri";

	/**
	 * Authorization-code grant form field {@code code}.
	 */
	public static final String CODE = "code";

	/**
	 * OAuth 2.0 form field {@code client_secret}.
	 */
	public static final String CLIENT_SECRET = "client_secret";

	/**
	 * OAuth 2.0 form field {@code client_id}.
	 */
	public static final String CLIENT_ID = "client_id";

	/**
	 * OAuth 2.0 form field {@code grant_type}.
	 */
	public static final String GRANT_TYPE = "grant_type";

	/**
	 * OIDC {@code state} parameter used to correlate login redirects.
	 */
	public static final String STATE = "state";

	/**
	 * OIDC {@code response_type} parameter.
	 */
	public static final String RESPONSE_TYPE = "response_type";

	/**
	 * OAuth 2.0 {@code scope} parameter.
	 */
	public static final String SCOPE = "scope";

	/**
	 * Path or form parameter name for the Keycloak realm id.
	 */
	public static final String REALM_ID = "realmId";

	/**
	 * OIDC logout hint {@code id_token_hint}.
	 */
	public static final String ID_TOKEN_HINT = "id_token_hint";
}
