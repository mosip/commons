package io.mosip.kernel.openid.bridge.api.constants;

/**
 * Constants related to KEYCLOAK
 * <p>
 * JWT claim names, OAuth/OIDC request parameter names, and MOSIP
 * {@code mosip.iam.*} / keymanager property keys used by the auth-code proxy
 * and private-key JWT client assertion flow.
 * 
 * @author Urvil Joshi
 *
 */
public class Constants {

	/**
	 * JWT {@code sub} (subject) claim name, used as the client id in a client
	 * assertion.
	 */
	public static final Object SUB = "sub";
	/**
	 * JWT {@code aud} (audience) claim name; for client assertions this is the
	 * token endpoint.
	 */
	public static final Object AUD = "aud";
	/**
	 * JWT {@code exp} (expiration) claim name.
	 */
	public static final Object EXP = "exp";
	/**
	 * JWT {@code iss} (issuer) claim name; for client assertions this is the
	 * client id.
	 */
	public static final Object ISS = "iss";
	/**
	 * JWT {@code iat} (issued-at) claim name.
	 */
	public static final Object IAT = "iat";
	/**
	 * JWT {@code jti} (JWT ID) claim name; a unique id per client assertion.
	 */
	public static final Object JTI = "jti";
	/**
	 * Property key for private-key JWT client-assertion lifetime in seconds.
	 */
    public static final String JWT_EXPIRY_TIME = "mosip.iam.module.token.endpoint.private-key-jwt.expiry.seconds";
	/**
	 * Property key for the MOSIP IAM/Keycloak base URL.
	 */
	public static final String BASE_URL = "mosip.iam.base.url";
	/**
	 * Property key for the keymanager application id used when signing a client
	 * assertion.
	 */
	public static final String APPLICATION_ID = "APPLICATION_Id";
	/**
	 * Property key for the keymanager reference id used when signing a client
	 * assertion.
	 */
	public static final String CLIENT_ASSERTION_REFERENCE_ID = "mosip.client.assertion.reference.id";
	/**
	 * Property key controlling whether the JWS payload is included when signing
	 * the client assertion.
	 */
	public static final String IS_INCLUDE_PAYLOAD = "mosip.include.payload";
	/**
	 * Property key controlling whether the signing certificate is included in the
	 * JWS header.
	 */
	public static final String IS_INCLUDE_CERTIFICATE = "mosip.include.certificate";
	/**
	 * Property key controlling whether the certificate hash is included in the JWS
	 * header. The constant name retains the historical capital {@code N}.
	 */
	public static final String IS_iNCLUDE_CERT_HASH = "mosip.include.cert.hash";
	/**
	 * Property key for the MOSIP keymanager JWT sign HTTP endpoint.
	 */
	public static final String KEYMANAGER_JWT_SIGN_END_POINT = "mosip.keymanager.jwt.sign.end.point";
	/**
	 * OAuth token-request form field {@code client_assertion_type}.
	 */
	public static final String CLIENT_ASSERTION_TYPE = "client_assertion_type";
	/**
	 * Property key for the {@code client_assertion_type} value (typically
	 * {@code urn:ietf:params:oauth:client-assertion-type:jwt-bearer}).
	 */
	public static final String CLIENT_ASSERTION_TYPE_PROPERTY = "mosip.client.assertion.type";

	/**
	 * Prevents instantiation of this constants holder.
	 */
	private Constants() {
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
	
	/**
	 * Single space, used when concatenating IAM error descriptions onto MOSIP
	 * messages.
	 */
	public static final String WHITESPACE = " ";

	/**
	 * OIDC authorization-request {@code claims} query parameter name.
	 */
	public static final String CLAIMS = "claims";

	/**
	 * Property key for the JSON claims object added to the authorization request.
	 */
	public static final String CLAIMS_PROPERTY = "mosip.iam.module.login_flow.claims";

	/**
	 * OAuth token-request form field {@code client_assertion} (signed JWT).
	 */
	public static final String CLIENT_ASSERTION = "client_assertion";
	/**
	 * Property key naming the access-token claim used as MOSIP user subject.
	 */
	public static final String TOKEN_SUBJECT_CLAIM_NAME = "mosip.access_token.subject.claim-name";

	/**
	 * OIDC authorization-request {@code ui_locales} parameter for login-page
	 * language.
	 */
	public static final String UI_LOCALES = "ui_locales";
}
