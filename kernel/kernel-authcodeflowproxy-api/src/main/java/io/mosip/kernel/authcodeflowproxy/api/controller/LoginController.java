package io.mosip.kernel.authcodeflowproxy.api.controller;

import io.mosip.kernel.authcodeflowproxy.api.validator.ValidateTokenUtil;
import io.mosip.kernel.core.http.ResponseFilter;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.util.EmptyCheckUtils;
import io.mosip.kernel.openid.bridge.api.constants.Constants;
import io.mosip.kernel.openid.bridge.api.constants.Errors;
import io.mosip.kernel.openid.bridge.api.exception.ClientException;
import io.mosip.kernel.openid.bridge.api.exception.ServiceException;
import io.mosip.kernel.authcodeflowproxy.api.service.LoginServiceV2;
import io.mosip.kernel.openid.bridge.api.utils.JWTUtils;
import io.mosip.kernel.openid.bridge.dto.AccessTokenResponseDTO;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * REST controller that proxies the OAuth 2.0 authorization-code flow against Keycloak
 * (or a compatible IdP) for MOSIP UI clients.
 * <p>
 * Endpoints start login, complete the authorization-code callback, validate the access
 * token cookie, and log the user out. Redirect targets are Base64-encoded path segments
 * and are checked against {@code auth.allowed.urls} (exact match or Ant-style patterns)
 * before any 302 is issued. Tokens are stored in HTTP-only cookies.
 */
@RestController
public class LoginController {
	
	/**
	 * Default cookie / claim name used for the OpenID Connect ID token when
	 * {@code idToken} is not configured in the environment.
	 */
	private static final String ID_TOKEN = "id_token";

	/**
	 * Logger for login, redirect-allowlist, and ID-token correlation failures.
	 */
	private final static Logger LOGGER= LoggerFactory.getLogger(LoginController.class);

	/**
	 * Environment property key that names the ID-token cookie. Falls back to {@link #ID_TOKEN}.
	 */
	private static final String IDTOKEN = "idToken";

	/**
	 * Cookie name that holds the access token. Bound from {@code auth.token.header};
	 * defaults to {@code Authorization}.
	 */
	@Value("${auth.token.header:Authorization}")
	private String authTokenHeader;
	
	/**
	 * Configured Keycloak locale cookie name. Bound from {@code iam.locale.cookie.name};
	 * defaults to {@code KEYCLOAK_LOCALE}.
	 */
	@Value("${iam.locale.cookie.name:KEYCLOAK_LOCALE}")
	private String localeCookieName;
	
	/**
	 * Path of the Keycloak locale cookie. Bound from
	 * {@code iam.locale.cookie.name} (same property key as {@link #localeCookieName})
	 * with default {@code /auth/realms/}.
	 */
	@Value("${iam.locale.cookie.name:/auth/realms/}")
	private String localeCookiePath;
	
	
	/**
	 * Allow-listed post-login / post-logout redirect URLs, split from the comma-separated
	 * {@code auth.allowed.urls} property. Entries may be exact URLs or Ant path patterns.
	 */
	@Value("#{'${auth.allowed.urls}'.split(',')}")
	private List<String> allowedUrls;

	/**
	 * Authorization-code flow service used to build Keycloak URLs, exchange codes, and
	 * create token cookies.
	 */
	@Autowired
	private LoginServiceV2 loginService;
	
	/**
	 * Offline JWT validator (expiry, issuer host, JWKS signature, audience/AZP).
	 */
	@Autowired
	private ValidateTokenUtil validateTokenHelper;

	/**
	 * Spring environment used to resolve ID-token cookie name and subject claim name.
	 */
	@Autowired
	private Environment environment;

	/**
	 * When {@code true}, the login-redirect also validates the OIDC ID token, checks that
	 * its subject matches the access-token subject, and sets an ID-token cookie.
	 * Bound from {@code auth.validate.id-token}; defaults to {@code false}.
	 */
	@Value("${auth.validate.id-token:false}")
	private boolean validateIdToken;
	
	/**
	 * Matcher used when an allow-list entry is an Ant-style pattern rather than an exact URL.
	 */
	@Autowired
	private AntPathMatcher antPathMatcher;
	
	/**
	 * For offline logout, there is no token invalidation happening in the IdP's
	 * end. It is expected that the cookies with the tokens only getting expired.
	 */
	@Value("${mosip.iam.logout.offline:false}")
	private boolean offlineLogout;

	/**
	 * Starts the OAuth 2.0 authorization-code flow without a UI locale hint.
	 * <p>
	 * Delegates to {@link #login(String, String, String, String, HttpServletResponse)}
	 * with {@code uiLocales} set to {@code null}.
	 *
	 * @param state cookie {@code state} value from a prior request, if present
	 * @param redirectURI Base64-encoded application redirect URI used as the Keycloak
	 *                    {@code redirect_uri} suffix
	 * @param stateParam {@code state} query parameter; used when the cookie is empty
	 * @param res HTTP response used to set the {@code state} cookie and issue a 302
	 *            to the Keycloak authorization endpoint
	 * @throws IOException if sending the redirect fails
	 * @throws ServiceException if {@code state} is missing or is not a UUID
	 */
	@GetMapping(value = "/login/{redirectURI}")
	public void login(@CookieValue(name = "state", required = false) String state,
			@PathVariable("redirectURI") String redirectURI,
			@RequestParam(name = "state", required = false) String stateParam, HttpServletResponse res)
			throws IOException {
		login(state, redirectURI, stateParam, null, res);
	}

	/**
	 * Starts the OAuth 2.0 authorization-code flow (v2), optionally forwarding
	 * {@code ui_locales} to Keycloak.
	 * <p>
	 * Resolves {@code state} from the cookie, then the query parameter. The value must be a
	 * UUID. Builds the Keycloak authorization URL, stores {@code state} in a secure,
	 * HTTP-only cookie at path {@code /}, and redirects (HTTP 302) to Keycloak.
	 *
	 * @param state cookie {@code state} value from a prior request, if present
	 * @param redirectURI Base64-encoded application redirect URI used as the Keycloak
	 *                    {@code redirect_uri} suffix
	 * @param stateParam {@code state} query parameter; used when the cookie is empty
	 * @param uiLocales optional OIDC {@code ui_locales} hint forwarded to Keycloak
	 * @param res HTTP response used to set the {@code state} cookie and issue a 302
	 * @throws IOException if sending the redirect fails
	 * @throws ServiceException if {@code state} is missing or is not a UUID
	 */
	@SuppressWarnings({"java:S2092", "java:S3330"}) // added suppress for sonarcloud. The secure flag, httpOnly flag is set to true through setCookieParams method. Line # 111.
	@GetMapping(value = "/login/v2/{redirectURI}")
	public void login(@CookieValue(name = "state", required = false) String state,
			@PathVariable("redirectURI") String redirectURI,
			@RequestParam(name = "state", required = false) String stateParam, 
			@RequestParam(name = "ui_locales", required = false) String uiLocales, HttpServletResponse res)
			throws IOException {
		String stateValue = EmptyCheckUtils.isNullEmpty(state) ? stateParam : state;
		if (EmptyCheckUtils.isNullEmpty(stateValue)) {
			throw new ServiceException(Errors.STATE_NULL_EXCEPTION.getErrorCode(),
					Errors.STATE_NULL_EXCEPTION.getErrorMessage());
		}

		// there is no UUID.parse method till so using this as alternative
		try {
			if (!UUID.fromString(stateValue).toString().equals(stateValue)) {
				throw new ServiceException(Errors.STATE_NOT_UUID_EXCEPTION.getErrorCode(),
						Errors.STATE_NOT_UUID_EXCEPTION.getErrorMessage());
			}
		} catch (IllegalArgumentException exception) {
			throw new ServiceException(Errors.STATE_NOT_UUID_EXCEPTION.getErrorCode(),
					Errors.STATE_NOT_UUID_EXCEPTION.getErrorMessage());
		}
		
		String uri = loginService.loginV2(redirectURI, stateValue, uiLocales);
		Cookie stateCookie = new Cookie("state", stateValue);
		setCookieParams(stateCookie,true,true,"/");
		res.addCookie(stateCookie);
		res.setStatus(302);
		res.sendRedirect(uri);
	}

	/**
	 * Completes the OAuth 2.0 authorization-code callback from Keycloak.
	 * <p>
	 * When {@code error} is empty, exchanges {@code code} for tokens (after CSRF
	 * {@code state} matching), validates the access token via JWKS, and sets the
	 * Authorization cookie. When {@link #validateIdToken} is enabled, also validates the
	 * ID token, requires matching {@code sub} claims, and sets the ID-token cookie.
	 * Always 302-redirects to the Base64-decoded {@code redirectURI} after allow-list
	 * checks; IdP {@code error} values are appended as a query parameter.
	 *
	 * @param redirectURI Base64-encoded application URL to redirect to after the callback
	 * @param state OAuth 2.0 {@code state} returned by Keycloak
	 * @param sessionState OIDC {@code session_state} from Keycloak (accepted, unused here)
	 * @param code authorization code to exchange at the token endpoint
	 * @param error Keycloak error code when the authorization request failed
	 * @param stateCookie {@code state} cookie set at login, compared with {@code state}
	 * @param req current HTTP request
	 * @param res HTTP response used to set token cookies and issue the 302
	 * @throws IOException if sending the redirect fails
	 * @throws ClientException if the ID token is missing or {@code sub} claims do not match
	 * @throws ServiceException if the decoded redirect URL is not allow-listed
	 */
	@SuppressWarnings({"javasecurity:S5146", "java:S2092", "java:S3330"}) // added suppress for sonarcloud. The URLs whitelisting with the configured value in properties. Line # 156.
	// The secure flag, httpOnly flag is set to true through setCookieParams method. Line # 151.
	@GetMapping(value = "/login-redirect/{redirectURI}")
	public void loginRedirect(@PathVariable("redirectURI") String redirectURI, @RequestParam(value="state", required = false) String state,
			@RequestParam(value="session_state",required = false) String sessionState, @RequestParam(value="code", required = false) String code, 
			@RequestParam(value="error", required = false) String error,
			@CookieValue(value="state", required = false) String stateCookie, HttpServletRequest req, HttpServletResponse res) throws IOException {
		if(error == null || error.isEmpty()){
			AccessTokenResponseDTO jwtResponseDTO = loginService.loginRedirect(state, sessionState, code, stateCookie,
					redirectURI);
			String accessToken = jwtResponseDTO.getAccessToken();
			validateTokenHelper.validateToken(accessToken);
			Cookie cookie = loginService.createCookie(accessToken);
			res.addCookie(cookie);
			if(validateIdToken) {
				String subjectClaimNameProperty = this.environment.getProperty(Constants.TOKEN_SUBJECT_CLAIM_NAME);
				String authTokenSub =  JWTUtils.getSubClaimValueFromToken
						(cookie.getValue(), subjectClaimNameProperty);
				String idTokenProperty  = this.environment.getProperty(IDTOKEN, ID_TOKEN);
				String idToken = jwtResponseDTO.getIdToken();
				if(idToken == null) {
					LOGGER.error("Id token is null.");
					throw new ClientException(Errors.TOKEN_NOTPRESENT_ERROR.getErrorCode(),
							Errors.TOKEN_NOTPRESENT_ERROR.getErrorMessage() + ": " + idTokenProperty);
				}
				String idTokenSub = JWTUtils.getSubClaimValueFromToken(idToken,
						subjectClaimNameProperty);
				if(idTokenSub != null && !idTokenSub.equalsIgnoreCase(authTokenSub)){
					LOGGER.error("Id token Sub value and auth token sub value are not matching.");
					throw new ClientException(Errors.INVALID_TOKEN.getErrorCode(),
							Errors.INVALID_TOKEN.getErrorMessage());
				}
				validateTokenHelper.validateToken(idToken);
				Cookie idTokenCookie = new Cookie(idTokenProperty, idToken);
				setCookieParams(idTokenCookie,true,true,"/");
				res.addCookie(idTokenCookie);
			}
		}
		res.setStatus(302);
		String redirectUrl = new String(Base64.decodeBase64(redirectURI.getBytes()));
		
		boolean matchesAllowedUrls = matchesAllowedUrls(redirectUrl);
		if(!matchesAllowedUrls) {
			LOGGER.error("Url {} was not part of allowed url's", redirectUrl.replaceAll("[\n\r]", "_"));
			throw new ServiceException(Errors.ALLOWED_URL_EXCEPTION.getErrorCode(), Errors.ALLOWED_URL_EXCEPTION.getErrorMessage());
		}
		// If error exist appending that as a query param along with redirecturi
		if(error != null && !error.isEmpty()){
			redirectUrl = redirectUrl+"?error="+error;
		}
		res.sendRedirect(redirectUrl);	
	}



	/**
	 * Returns whether {@code url} is allow-listed: exact match (fragment stripped) against
	 * {@link #allowedUrls}, or Ant-style match via {@link #antPathMatcher}.
	 *
	 * @param url decoded redirect URL to check
	 * @return {@code true} if the URL is permitted
	 */
	private boolean matchesAllowedUrls(String url) {
		boolean hasMatch = allowedUrls.contains(url.contains("#") ? url.split("#")[0] : url);
		if(!hasMatch) {		
			hasMatch = allowedUrls.stream()
				.filter(pattern -> antPathMatcher.isPattern(pattern))
				.anyMatch(pattern -> antPathMatcher.match(pattern, url));
		}
		return hasMatch;
	}

	/**
	 * Applies {@code HttpOnly}, {@code Secure}, and path attributes on a cookie.
	 *
	 * @param idTokenCookie cookie to mutate
	 * @param isHttpOnly whether the cookie is HTTP-only
	 * @param isSecure whether the cookie is marked Secure
	 * @param path cookie path (typically {@code /})
	 */
	private void setCookieParams(Cookie idTokenCookie, boolean isHttpOnly, boolean isSecure,String path) {
		idTokenCookie.setHttpOnly(isHttpOnly);
		idTokenCookie.setSecure(isSecure);
		idTokenCookie.setPath(path);
	}

	/**
	 * Validates the access token from the Authorization cookie and refreshes that cookie.
	 * <p>
	 * When {@code auth.server.admin.validate.url} is set, validation is delegated to the
	 * auth manager; otherwise {@link ValidateTokenUtil} performs offline JWKS validation.
	 *
	 * @param request HTTP request whose cookies must contain the access token
	 * @param res HTTP response used to re-issue the Authorization cookie
	 * @return MOSIP {@link ResponseWrapper} whose {@code response} is a {@code MosipUserDto}
	 *         (online) or the string {@code TOKEN_VALID} (offline)
	 * @throws ClientException if cookies are missing or the Authorization cookie is absent
	 */
	@ResponseFilter
	@GetMapping(value = "/authorize/admin/validateToken")
	public ResponseWrapper<?> validateAdminToken(HttpServletRequest request, HttpServletResponse res) {
		String authToken = null;
		Cookie[] cookies = request.getCookies();
		if (cookies == null) {
			throw new ClientException(Errors.COOKIE_NOTPRESENT_ERROR.getErrorCode(),
					Errors.COOKIE_NOTPRESENT_ERROR.getErrorMessage());
		}
		Object mosipUserDto = null;

		for (Cookie cookie : cookies) {
			if (cookie.getName().contains(authTokenHeader)) {
				authToken = cookie.getValue();
			}
		}
		if (authToken == null) {
			throw new ClientException(Errors.TOKEN_NOTPRESENT_ERROR.getErrorCode(),
					Errors.TOKEN_NOTPRESENT_ERROR.getErrorMessage());
		}

		mosipUserDto = loginService.valdiateToken(authToken);
		Cookie cookie = loginService.createCookie(authToken);
		res.addCookie(cookie);
		ResponseWrapper<Object> responseWrapper = new ResponseWrapper<>();
		responseWrapper.setResponse(mosipUserDto);
		return responseWrapper;
	}
	
	/**
	 * Logs the user out and 302-redirects to an allow-listed URL.
	 * <p>
	 * {@code redirecturi} is Base64-decoded and must match {@link #allowedUrls}. Online
	 * logout builds Keycloak's OpenID Connect end-session URL from the token issuer.
	 * Offline logout ({@link #offlineLogout}) expires the Authorization cookie (and the
	 * ID-token cookie when {@link #validateIdToken} is true) without calling the IdP.
	 *
	 * @param token Authorization cookie value (access token)
	 * @param redirectURI Base64-encoded post-logout redirect URI
	 * @param res HTTP response used to expire cookies (offline) and issue the 302
	 * @throws IOException if sending the redirect fails
	 * @throws ServiceException if the decoded redirect URL is not allow-listed
	 */
	@SuppressWarnings({"javasecurity:S5146", "java:S2092", "java:S3330"}) // added suppress for sonarcloud. The URLs whitelisting with the configured value in properties. Line # 221.
	// The secure flag, httpOnly flag is set to true through setCookieParams method. Line # 240.
	@ResponseFilter
	@GetMapping(value = "/logout/user")
	public void logoutUser(
			@CookieValue(value = "Authorization", required = false) String token,@RequestParam(name = "redirecturi", required = true) String redirectURI, HttpServletResponse res) throws IOException {
		String redirectURL = new String(Base64.decodeBase64(redirectURI));
		if(!matchesAllowedUrls(redirectURL)) {
			redirectURL = redirectURL.replaceAll("[\n\r]", " ");
			LOGGER.error("Url {} was not part of allowed url's", redirectURL.replaceAll("[\n\r]", "_"));
			throw new ServiceException(Errors.ALLOWED_URL_EXCEPTION.getErrorCode(), Errors.ALLOWED_URL_EXCEPTION.getErrorMessage());
		}
		String uri = "";
		if(offlineLogout) {
		   uri = loginService.logoutUser(token,redirectURI);
		} else {
		   uri = loginService.logoutUser(token,redirectURL);
		}	
		if(offlineLogout) {
			Cookie cookie = loginService.createExpiringCookie();
			res.addCookie(cookie);
			
			if(validateIdToken) {
				String idTokenProperty  = this.environment.getProperty(IDTOKEN, ID_TOKEN);
				//Create expiring id_token cookie
				Cookie idTokenCookie = new Cookie(idTokenProperty, null);
				idTokenCookie.setMaxAge(0);
				setCookieParams(idTokenCookie,true,true,"/");
				res.addCookie(idTokenCookie);
			}
		}
		
		res.setStatus(302);
		res.sendRedirect(uri);
	}

}
