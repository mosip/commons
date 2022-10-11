package io.mosip.kernel.authcodeflowproxy.api.constants;

/**
 * Constants related to KEYCLOAK
 * 
 * @author Urvil Joshi
 *
 */
public class Constants {

	public static final Object SUB = "sub";
	public static final Object AUD = "aud";
	public static final Object EXP = "exp";
	public static final Object ISS = "iss";
	public static final Object IAT = "iat";
	public static final String JWT_EXPIRY_TIME = "mosip.iam.module.token.endpoint.private-key-jwt.expiry.seconds";
	public static final Object BASE_URL = "mosip.iam.base.url";
	public static final String MOSIP_RESIDENT_IDP_CLIENT_ID = "mosip.resident.idp.client.id";
	public static final String APPLICATION_ID = "APPLICATION_Id";
	public static final String CLIENT_ASSERTION_REFERENCE_ID = "mosip.client.assertion.reference.id";
	public static final String IS_INCLUDE_PAYLOAD = "mosip.include.payload";
	public static final String IS_INCLUDE_CERTIFICATE = "mosip.include.certificate";
	public static final String IS_iNCLUDE_CERT_HASH = "mosip.include.cert.hash";
	public static final String KEYMANAGER_JWT_SIGN_END_POINT = "mosip.keymanager.jwt.sign.end.point";

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

	public static final String CLIENT_ASSERTION = "client_assertion ";
}
