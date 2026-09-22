package io.mosip.kernel.openid.bridge.api.constants;

/**
 * Constants related to KEYCLOAK
 * <p>
 * OAuth/OIDC request parameter names shared by auth-manager Keycloak calls
 * (token, authorize, logout). Overlaps {@link Constants} for the subset used
 * without client-assertion or claims properties.
 * 
 * @author Urvil Joshi
 *
 */
public class IAMConstants {

	/**
	 * Prevents instantiation of this constants holder.
	 */
	private IAMConstants() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * OAuth/OIDC {@code redirect_uri} parameter name.
	 */
	public static final String REDIRECT_URI = "redirect_uri";

	/**
	 * OAuth authorization-code parameter name {@code code}.
	 */
	public static final String CODE = "code";

	/**
	 * OAuth client secret form field {@code client_secret}.
	 */
	public static final String CLIENT_SECRET = "client_secret";

	/**
	 * OAuth client id form/query field {@code client_id}.
	 */
	public static final String CLIENT_ID = "client_id";

	/**
	 * OAuth {@code grant_type} form field (for example {@code authorization_code}).
	 */
	public static final String GRANT_TYPE = "grant_type";

	/**
	 * OAuth/OIDC CSRF {@code state} parameter name.
	 */
	public static final String STATE = "state";

	/**
	 * OIDC {@code response_type} query parameter (typically {@code code}).
	 */
	public static final String RESPONSE_TYPE = "response_type";

	/**
	 * OIDC {@code scope} query/form parameter.
	 */
	public static final String SCOPE = "scope";

	/**
	 * Path-variable / map key for the Keycloak realm id.
	 */
	public static final String REALM_ID = "realmId";

	/**
	 * OIDC RP-initiated logout parameter {@code id_token_hint}.
	 */
	public static final String ID_TOKEN_HINT = "id_token_hint";
}
