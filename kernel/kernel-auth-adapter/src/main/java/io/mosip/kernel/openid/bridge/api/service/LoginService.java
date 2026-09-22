package io.mosip.kernel.openid.bridge.api.service;


import io.mosip.kernel.openid.bridge.dto.AccessTokenResponseDTO;
import jakarta.servlet.http.Cookie;

/**
 * Service contract for MOSIP OpenID Connect authorization-code login against
 * Keycloak (or a compatible IAM).
 * <p>
 * Typical browser flow:
 * <ol>
 * <li>{@link #login(String, String)} builds the IAM authorization URL.</li>
 * <li>The IdP redirects back with an authorization {@code code} and
 * {@code state}.</li>
 * <li>{@link #loginRedirect(String, String, String, String, String)} exchanges
 * the code for tokens.</li>
 * <li>{@link #createCookie(String)} stores the access token for subsequent
 * calls; {@link #valdiateToken(String)} checks it.</li>
 * <li>{@link #logoutUser(String, String)} ends the IdP session and
 * {@link #createExpiringCookie()} clears the local cookie.</li>
 * </ol>
 * Implemented by {@code kernel-authcodeflowproxy-api}.
 */
public interface LoginService {

	/**
	 * Builds the Keycloak/OIDC authorization endpoint URL for the authorization-code
	 * flow.
	 * <p>
	 * The returned URL includes client id, redirect URI, CSRF {@code state},
	 * {@code response_type}, {@code scope}, and optional OIDC {@code claims}.
	 *
	 * @param redirectURI application callback path appended to the configured IAM
	 *                    redirect URI
	 * @param state       opaque CSRF value that must later match the state cookie
	 * @return fully expanded authorization URL to which the browser should be
	 *         redirected
	 */
	String login(String redirectURI, String state);

	/**
	 * Creates an HTTP-only cookie that holds the access token (or other auth
	 * cookie value) for subsequent MOSIP API calls.
	 *
	 * @param authCookie token value to store in the cookie
	 * @return servlet cookie configured with path {@code /}, max-age, and
	 *         http-only/secure flags from module properties
	 */
	Cookie createCookie(String authCookie);

	/**
	 * Validates an access token either online (auth manager) or offline (JWT
	 * signature/claims) depending on configuration.
	 * <p>
	 * The method name retains the historical spelling {@code valdiateToken}.
	 *
	 * @param authToken access token from the Authorization cookie
	 * @return validated MOSIP user details, or a success marker such as
	 *         {@code TOKEN_VALID} when online validation is disabled
	 */
	Object valdiateToken(String authToken);


	/**
	 * Completes the authorization-code callback: verifies CSRF {@code state}, then
	 * POSTs {@code grant_type}, {@code code}, and client credentials (or a
	 * private-key JWT {@code client_assertion}) to the IAM token endpoint.
	 *
	 * @param state         {@code state} query parameter returned by the IdP
	 * @param sessionState  OIDC {@code session_state} from the IdP callback, if
	 *                      present
	 * @param code          authorization code to exchange for tokens
	 * @param stateCookie   CSRF state previously stored in a cookie
	 * @param redirectURI   application callback path used as {@code redirect_uri}
	 *                      on the token request
	 * @return access token, expiry, and optional ID token from the IAM
	 */
	AccessTokenResponseDTO loginRedirect(String state, String sessionState, String code, String stateCookie,
			String redirectURI);

	/**
	 * Builds the IAM end-session URL (RP-initiated logout) using the current token
	 * as {@code id_token_hint} and the given post-logout redirect.
	 *
	 * @param token       ID token (or access token used as hint) for the IdP logout
	 *                    request
	 * @param redirectURI post-logout redirect URI for the browser
	 * @return fully expanded logout/end-session URL
	 */
	String logoutUser(String token, String redirectURI);

	/**
	 * Creates a cookie that expires immediately so the browser drops the auth
	 * cookie after logout.
	 *
	 * @return servlet cookie with max-age {@code 0} and a null value
	 */
	Cookie createExpiringCookie();

}
