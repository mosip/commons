package io.mosip.kernel.authcodeflowproxy.api.constants;

/**
 * Constants related to KEYCLOAK
 * 
 * @author Urvil Joshi
 *
 */
public class Constants {

	/**
	 * Private constructor
	 */
	private Constants() {
		// TODO Auto-generated constructor stub
	}

	public static final String REDIRECT_URI = "redirect_uri";

	public static final String CODE = "code";

	public static final String CLIENT_SECRET = "client_secret";

	public static final String CLIENT_ID = "client_id";

	public static final String GRANT_TYPE = "grant_type";

	public static final String STATE = "state";

	public static final String RESPONSE_TYPE = "response_type";

	public static final String SCOPE = "scope";

	public static final String REALM_ID = "realmId";

	public static final String ID_TOKEN_HINT = "id_token_hint";
	
	public static final String WHITESPACE = " ";

	public static final String CLAIM = "claim";

	public static final String CLAIM_PROPERTY = "mosip.iam.module.login_flow.claim";
}
