package io.mosip.kernel.authcodeflowproxy.api.service.impl;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import jakarta.servlet.http.Cookie;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.kernel.authcodeflowproxy.api.validator.ValidateTokenUtil;
import io.mosip.kernel.core.exception.ExceptionUtils;
import io.mosip.kernel.core.exception.ServiceError;
import io.mosip.kernel.core.http.RequestWrapper;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.util.CryptoUtil;
import io.mosip.kernel.core.util.DateUtils2;
import io.mosip.kernel.core.util.EmptyCheckUtils;
import io.mosip.kernel.openid.bridge.api.constants.AuthErrorCode;
import io.mosip.kernel.openid.bridge.api.constants.Constants;
import io.mosip.kernel.openid.bridge.api.constants.Errors;
import io.mosip.kernel.openid.bridge.api.exception.AuthRestException;
import io.mosip.kernel.openid.bridge.api.exception.ClientException;
import io.mosip.kernel.openid.bridge.api.exception.ServiceException;
import io.mosip.kernel.authcodeflowproxy.api.service.LoginServiceV2;
import io.mosip.kernel.openid.bridge.api.utils.JWTUtils;
import io.mosip.kernel.openid.bridge.dto.AccessTokenResponse;
import io.mosip.kernel.openid.bridge.dto.AccessTokenResponseDTO;
import io.mosip.kernel.openid.bridge.dto.IAMErrorResponseDto;
import io.mosip.kernel.openid.bridge.dto.JWSSignatureRequestDto;
import io.mosip.kernel.openid.bridge.dto.JWTSignatureResponseDto;
import io.mosip.kernel.openid.bridge.model.MosipUserDto;

/**
 * Default {@link LoginServiceV2} implementation of the OAuth 2.0 authorization-code
 * flow against Keycloak (or a compatible IAM).
 * <p>
 * Builds authorization URLs with {@link UriComponentsBuilder#fromUriString(String)}
 * (Spring 7), exchanges the authorization code at the token endpoint using either a
 * client secret or a private-key JWT ({@code client_assertion}) signed by Keymanager,
 * validates tokens online via the auth manager or offline via {@link ValidateTokenUtil}
 * / JWKS, and builds Keycloak end-session URLs for logout. Cookie max-age and the
 * Secure flag are driven by configuration. Request timestamps for JWT signing use
 * {@link DateUtils2#getUTCCurrentDateTime()}.
 */
@Service
public class LoginServiceImpl implements LoginServiceV2 {

	/**
	 * Offline-validation success payload returned by {@link #valdiateToken(String)} when
	 * {@link #validateUrl} is empty and the JWT is valid.
	 */
	private static final String TOKEN_VALID = "TOKEN_VALID";

	/**
	 * Configured delimiter for composite redirect URIs. Bound from
	 * {@code mosip.kernel.auth-code-url-splitter}; defaults to {@code #URISPLITTER#}.
	 */
	@Value("${mosip.kernel.auth-code-url-splitter:#URISPLITTER#}")
	private String urlSplitter;

	/**
	 * Whether the Authorization cookie is marked {@code Secure}. Bound from
	 * {@code mosip.security.secure-cookie}; defaults to {@code false}.
	 */
	@Value("${mosip.security.secure-cookie:false}")
	private boolean isSecureCookie;

	/**
	 * Cookie name that stores the access token. Bound from {@code auth.token.header};
	 * defaults to {@code Authorization}.
	 */
	@Value("${auth.token.header:Authorization}")
	private String authTokenHeader;

	/**
	 * Authorization cookie max-age in seconds. Bound from {@code auth.jwt.expiry};
	 * defaults to {@code 1800000}.
	 */
	@Value("${auth.jwt.expiry:1800000}")
	private int authTokenExpiry;

	/**
	 * OAuth 2.0 {@code grant_type} sent to the token endpoint. Bound from
	 * {@code mosip.iam.module.login_flow.name}; defaults to {@code authorization_code}.
	 */
	@Value("${mosip.iam.module.login_flow.name:authorization_code}")
	private String loginFlowName;

	/**
	 * OAuth 2.0 / OIDC client identifier registered with Keycloak.
	 * Bound from {@code mosip.iam.module.clientid}.
	 */
	@Value("${mosip.iam.module.clientid}")
	private String clientID;

	/**
	 * OAuth 2.0 client secret used when {@link #isJwtAuthEnabled} is {@code false}.
	 * Bound from {@code mosip.iam.module.clientsecret}.
	 */
	@Value("${mosip.iam.module.clientsecret}")
	private String clientSecret;

	/**
	 * Base {@code redirect_uri} registered with Keycloak; the login path suffix is appended.
	 * Bound from {@code mosip.iam.module.redirecturi}.
	 */
	@Value("${mosip.iam.module.redirecturi}")
	private String redirectURI;

	/**
	 * OAuth 2.0 {@code scope} requested at the authorization endpoint. Bound from
	 * {@code mosip.iam.module.login_flow.scope}; defaults to {@code cls}.
	 */
	@Value("${mosip.iam.module.login_flow.scope:cls}")
	private String scope;

	/**
	 * OAuth 2.0 {@code response_type} (authorization-code flow uses {@code code}).
	 * Bound from {@code mosip.iam.module.login_flow.response_type}; defaults to {@code code}.
	 */
	@Value("${mosip.iam.module.login_flow.response_type:code}")
	private String responseType;

	/**
	 * Keycloak authorization-endpoint template (may contain {@code {realmId}}).
	 * Bound from {@code mosip.iam.authorization_endpoint}.
	 */
	@Value("${mosip.iam.authorization_endpoint}")
	private String authorizationEndpoint;

	/**
	 * Keycloak realm identifier substituted into authorization and token URLs.
	 * Bound from {@code mosip.iam.module.admin_realm_id}.
	 */
	@Value("${mosip.iam.module.admin_realm_id}")
	private String realmID;

	/**
	 * Keycloak token-endpoint template (may contain {@code {realmId}}).
	 * Bound from {@code mosip.iam.token_endpoint}.
	 */
	@Value("${mosip.iam.token_endpoint}")
	private String tokenEndpoint;

	/**
	 * Auth-manager URL for online token validation. When null or empty, validation is
	 * performed offline via JWKS. Bound from {@code auth.server.admin.validate.url}.
	 */
	@Value("${auth.server.admin.validate.url:}")
	private String validateUrl;
	
	
	/**
	 * Query parameter name for the post-logout redirect on Keycloak's end-session endpoint.
	 * Bound from {@code mosip.iam.post-logout-uri-param-key}; defaults to
	 * {@code post_logout_redirect_uri}.
	 */
	@Value("${mosip.iam.post-logout-uri-param-key:post_logout_redirect_uri}")
	private String postLogoutRedirectURIParamKey;
	
	/**
	 * Path appended to the JWT {@code iss} claim to form Keycloak's end-session URL.
	 * Bound from {@code mosip.iam.end-session-endpoint-path}; defaults to
	 * {@code /protocol/openid-connect/logout}.
	 */
	@Value("${mosip.iam.end-session-endpoint-path:/protocol/openid-connect/logout}")
	private String endSessionEndpointPath;

	/**
	 * When {@code true}, the token request authenticates with a private-key JWT
	 * ({@code client_assertion}) instead of {@code client_secret}. Bound from
	 * {@code mosip.iam.module.token.endpoint.private-key-jwt.auth.enabled}; defaults to
	 * {@code false}.
	 */
	@Value("${mosip.iam.module.token.endpoint.private-key-jwt.auth.enabled:false}")
	private boolean isJwtAuthEnabled;
	
	/**
	 * For offline logout, there is no token invalidation happening in the IdP's
	 * end. It is expected that the cookies with the tokens only getting expired.
	 */
	@Value("${mosip.iam.logout.offline:false}")
	private boolean offlineLogout;

	/**
	 * HTTP client used for Keycloak token exchange and online auth-manager validation.
	 */
	@Autowired
	private RestTemplate restTemplate;

	/**
	 * Optional self-token {@link RestTemplate} used to call Keymanager's JWT sign API
	 * when {@link #isJwtAuthEnabled} is {@code true}. May be absent.
	 */
	@Autowired(required = false)
	@Qualifier("selfTokenRestTemplate")
	private RestTemplate selfTokenRestTemplate;

	/**
	 * JSON mapper for Keycloak token responses, Keymanager payloads, and auth-manager
	 * {@link MosipUserDto} bodies.
	 */
	@Autowired
	private ObjectMapper objectMapper;

	/**
	 * Spring environment for claims, client-assertion, and Keymanager sign properties.
	 */
	@Autowired
	private Environment environment;
	
	/**
	 * Offline JWT validator used when {@link #validateUrl} is empty.
	 */
	@Autowired
	private ValidateTokenUtil validateTokenUtil;

	/**
	 * Builds the Keycloak authorization URL without a UI locale hint.
	 * <p>
	 * Delegates to {@link #loginV2(String, String, String)} with {@code uiLocales} of
	 * {@code null}.
	 *
	 * @param redirectURI Base64-encoded application redirect URI suffix
	 * @param state CSRF {@code state} value echoed to Keycloak
	 * @return fully expanded authorization URL
	 * @throws ServiceException if configured {@code claims} cannot be UTF-8 encoded
	 */
	@Override
	public String login(String redirectURI, String state) {
		return loginV2(redirectURI, state, null);
	}
	
	/**
	 * Builds the Keycloak OAuth 2.0 authorization-endpoint URL.
	 * <p>
	 * Uses {@link UriComponentsBuilder#fromUriString(String)} with path variable
	 * {@code realmId}, then adds {@code client_id}, {@code redirect_uri}, {@code state},
	 * {@code response_type}, {@code scope}, optional {@code ui_locales}, and optional
	 * URL-encoded {@code claims} from {@link Constants#CLAIMS_PROPERTY}.
	 *
	 * @param redirectURI Base64-encoded application redirect URI suffix appended to
	 *                    {@link #redirectURI}
	 * @param state CSRF {@code state} value echoed to Keycloak
	 * @param uiLocales optional OIDC {@code ui_locales}; omitted when {@code null}
	 * @return fully expanded authorization URL
	 * @throws ServiceException if configured {@code claims} cannot be UTF-8 encoded
	 */
	@Override
	public String loginV2(String redirectURI, String state, String uiLocales) {
		Map<String, String> pathParam = new HashMap<>();
		pathParam.put("realmId", realmID);
		UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromUriString(authorizationEndpoint);
		uriComponentsBuilder.queryParam(Constants.CLIENT_ID, clientID);
		uriComponentsBuilder.queryParam(Constants.REDIRECT_URI, this.redirectURI + redirectURI);
		uriComponentsBuilder.queryParam(Constants.STATE, state);
		uriComponentsBuilder.queryParam(Constants.RESPONSE_TYPE, responseType);
		uriComponentsBuilder.queryParam(Constants.SCOPE, scope);
		String claim = this.environment.getProperty(Constants.CLAIMS_PROPERTY);
		if(uiLocales != null){
			uriComponentsBuilder.queryParam(Constants.UI_LOCALES, uiLocales);
		}
		if(claim != null){
			uriComponentsBuilder.queryParam(Constants.CLAIMS, urlEncode(claim));
		}
		return uriComponentsBuilder.buildAndExpand(pathParam).toString();
	}

	/**
	 * Percent-encodes {@code value} as UTF-8 for use as an OAuth query parameter.
	 *
	 * @param value raw string (typically the configured {@code claims} JSON)
	 * @return UTF-8 URL-encoded string
	 * @throws ServiceException if UTF-8 encoding is not supported
	 */
	private static String urlEncode(String value) {
	    try {
			return URLEncoder.encode(value, StandardCharsets.UTF_8.toString());
		} catch (UnsupportedEncodingException e) {
			throw new ServiceException(Errors.UNSUPPORTED_ENCODING_EXCEPTION.getErrorCode(),
					Errors.UNSUPPORTED_ENCODING_EXCEPTION.getErrorMessage() + Constants.WHITESPACE + e.getMessage(), e);
		}
	}

	/**
	 * Creates the HTTP-only Authorization cookie holding the access token.
	 *
	 * @param authCookie compact JWT access token
	 * @return cookie named {@link #authTokenHeader}, path {@code /}, max-age
	 *         {@link #authTokenExpiry}, Secure flag {@link #isSecureCookie}
	 */
	@Override
	public Cookie createCookie(String authCookie) {
		final Cookie cookie = new Cookie(authTokenHeader, authCookie);
		cookie.setMaxAge(authTokenExpiry);
		cookie.setHttpOnly(true);
		cookie.setSecure(isSecureCookie);
		cookie.setPath("/");
		return cookie;
	}
	
	/**
	 * Creates an already-expired Authorization cookie used for offline logout.
	 *
	 * @return cookie named {@link #authTokenHeader} with max-age {@code 0} and a null value
	 */
	@Override
	public Cookie createExpiringCookie() {
		final Cookie cookie = new Cookie(authTokenHeader, null);
		cookie.setMaxAge(0);
		cookie.setHttpOnly(true);
		cookie.setSecure(isSecureCookie);
		cookie.setPath("/");
		return cookie;
	}

	/**
	 * Validates {@code authToken}.
	 * <p>
	 * If {@link #validateUrl} is empty, performs offline JWKS validation and returns
	 * {@link #TOKEN_VALID}. Otherwise GETs the auth-manager validate URL with the token
	 * as a cookie and maps the body to {@link MosipUserDto}.
	 *
	 * @param authToken compact JWT access token
	 * @return {@link #TOKEN_VALID} when validating offline, otherwise a {@link MosipUserDto}
	 * @throws AuthRestException if the token is invalid or the auth manager returns
	 *                           service errors
	 * @throws ServiceException if the HTTP call fails without MOSIP errors or the body
	 *                          cannot be parsed
	 */
	@Override
	public Object valdiateToken(String authToken) {
		//For IdP if validateURL is null/empty, no need for online token validation (which is done through authmanager), perform it offline.
		if(validateUrl == null || validateUrl.isEmpty()) {
			ImmutablePair<Boolean, AuthErrorCode> tokenValid = validateTokenUtil.isTokenValid(authToken);
			if(tokenValid.left) {
				return TOKEN_VALID;
			} else {
				throw new AuthRestException(
						List.of(new ServiceError(tokenValid.right.getErrorCode(), tokenValid.right.getErrorMessage())),
						HttpStatus.UNAUTHORIZED);
			}
		}
		HttpHeaders headers = new HttpHeaders();
		headers.add("Cookie", authTokenHeader + "=" + authToken);
		HttpEntity<String> requestEntity = new HttpEntity<>(headers);
		ResponseEntity<String> response = null;
		try {
			response = restTemplate.exchange(validateUrl, HttpMethod.GET, requestEntity, String.class);
		} catch (HttpClientErrorException | HttpServerErrorException e) {
			String responseBody = e.getResponseBodyAsString();
			List<ServiceError> validationErrorList = ExceptionUtils.getServiceErrorList(responseBody);

			if (!validationErrorList.isEmpty()) {
				throw new AuthRestException(validationErrorList, HttpStatus.valueOf(e.getStatusCode().value()));
			} else {
				throw new ServiceException(Errors.REST_EXCEPTION.getErrorCode(), e.getResponseBodyAsString());
			}

		}
		String responseBody = response.getBody();
		List<ServiceError> validationErrorList = ExceptionUtils.getServiceErrorList(responseBody);

		if (!validationErrorList.isEmpty()) {
			throw new AuthRestException(validationErrorList, HttpStatus.valueOf(response.getStatusCode().value()));
		}
		ResponseWrapper<?> responseObject;
		MosipUserDto mosipUserDto;
		try {
			responseObject = objectMapper.readValue(response.getBody(), ResponseWrapper.class);
			mosipUserDto = objectMapper.readValue(objectMapper.writeValueAsString(responseObject.getResponse()),
					MosipUserDto.class);
		} catch (IOException e) {
			throw new ServiceException(Errors.IO_EXCEPTION.getErrorCode(), Errors.IO_EXCEPTION.getErrorMessage());
		}
		return mosipUserDto;
	}

	/**
	 * Exchanges the authorization code for tokens at Keycloak's token endpoint.
	 * <p>
	 * Rejects the request when {@code state} does not match {@code stateCookie}. POSTs
	 * {@code application/x-www-form-urlencoded} with {@code grant_type}, {@code client_id},
	 * {@code code}, {@code redirect_uri}, and either {@code client_secret} or a
	 * {@code client_assertion} JWT. The token URL is built with
	 * {@link UriComponentsBuilder#fromUriString(String)} and {@code realmId} expansion.
	 *
	 * @param state {@code state} returned by Keycloak
	 * @param sessionState OIDC {@code session_state} from the callback (unused)
	 * @param code authorization code to redeem
	 * @param stateCookie {@code state} cookie set at login
	 * @param redirectURI Base64-encoded redirect URI suffix registered with Keycloak
	 * @return DTO with access token, expiry, and ID token
	 * @throws ClientException if {@code state} and {@code stateCookie} differ
	 * @throws ServiceException if Keycloak returns an error or the token JSON cannot be parsed
	 */
	@Override
	public AccessTokenResponseDTO loginRedirect(String state, String sessionState, String code, String stateCookie,
												String redirectURI) {
		// Compare states
		if (!stateCookie.equals(state)) {
			throw new ClientException(Errors.STATE_EXCEPTION.getErrorCode(), Errors.STATE_EXCEPTION.getErrorMessage());
		}

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
		map.add(Constants.GRANT_TYPE, loginFlowName);
		map.add(Constants.CLIENT_ID, clientID);
		if(isJwtAuthEnabled){
			map.add(Constants.CLIENT_ASSERTION, getClientAssertion());
			map.add(Constants.CLIENT_ASSERTION_TYPE, this.environment.getProperty(Constants.CLIENT_ASSERTION_TYPE_PROPERTY));
		} else{
			map.add(Constants.CLIENT_SECRET, clientSecret);
		}
		map.add(Constants.CODE, code);
		map.add(Constants.REDIRECT_URI, this.redirectURI + redirectURI);
		Map<String, String> pathParam = new HashMap<>();
		pathParam.put("realmId", realmID);
		UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(tokenEndpoint);
		HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(map, headers);
		ResponseEntity<String> responseEntity = null;
		try {
			responseEntity = restTemplate.exchange(uriBuilder.buildAndExpand(pathParam).toUriString(), HttpMethod.POST,
					entity, String.class);

		} catch (HttpClientErrorException | HttpServerErrorException e) {
			IAMErrorResponseDto keycloakErrorResponseDto = parseKeyClockErrorResponse(e);

			throw new ServiceException(Errors.ACESSTOKEN_EXCEPTION.getErrorCode(),
					Errors.ACESSTOKEN_EXCEPTION.getErrorMessage() + Constants.WHITESPACE
							+ keycloakErrorResponseDto.getError_description(), e);
		}
		AccessTokenResponse accessTokenResponse = null;
		try {
			accessTokenResponse = objectMapper.readValue(responseEntity.getBody(), AccessTokenResponse.class);
		} catch (IOException exception) {
			throw new ServiceException(Errors.RESPONSE_PARSE_ERROR.getErrorCode(),
					Errors.RESPONSE_PARSE_ERROR.getErrorMessage() + Constants.WHITESPACE + exception.getMessage(), exception);
		}
		AccessTokenResponseDTO accessTokenResponseDTO = new AccessTokenResponseDTO();
		accessTokenResponseDTO.setAccessToken(accessTokenResponse.getAccess_token());
		accessTokenResponseDTO.setExpiresIn(accessTokenResponse.getExpires_in());
		accessTokenResponseDTO.setIdToken(accessTokenResponse.getId_token());
		return accessTokenResponseDTO;
	}

	/**
	 * Obtains a private-key JWT {@code client_assertion} from Keymanager.
	 * <p>
	 * Signs the assertion claims from {@link #getClientAssertionData()} via the configured
	 * Keymanager JWT sign endpoint. {@code requesttime} is set with
	 * {@link DateUtils2#getUTCCurrentDateTime()}.
	 *
	 * @return signed compact JWT for the token request
	 * @throws ServiceException if Keymanager HTTP signing fails
	 */
	private String getClientAssertion() {
		JWSSignatureRequestDto jwsSignatureRequestDto = new JWSSignatureRequestDto();
		try {
			jwsSignatureRequestDto.setDataToSign( CryptoUtil.encodeToPlainBase64(getClientAssertionData()));
			jwsSignatureRequestDto.setReferenceId(this.environment.getProperty(Constants.CLIENT_ASSERTION_REFERENCE_ID));
			jwsSignatureRequestDto.setApplicationId(this.environment.getProperty(Constants.APPLICATION_ID));
			jwsSignatureRequestDto.setIncludePayload(Boolean.valueOf(this.environment.getProperty(Constants.IS_INCLUDE_PAYLOAD)));
			jwsSignatureRequestDto.setIncludeCertificate(Boolean.valueOf(this.environment.getProperty(Constants.IS_INCLUDE_CERTIFICATE)));
			jwsSignatureRequestDto.setIncludeCertHash(Boolean.valueOf(this.environment.getProperty(Constants.IS_iNCLUDE_CERT_HASH)));

			RequestWrapper<JWSSignatureRequestDto> requestWrapper = new RequestWrapper<>();
			requestWrapper.setRequest(jwsSignatureRequestDto);
			requestWrapper.setRequesttime(DateUtils2.getUTCCurrentDateTime());
			HttpEntity<RequestWrapper<JWSSignatureRequestDto>> requestWrapperHttpEntity = new HttpEntity<>(requestWrapper);
			ResponseWrapper<?> responseWrapper =
					selfTokenRestTemplate.exchange(URI.create(Objects.requireNonNull(this.environment.getProperty(Constants.KEYMANAGER_JWT_SIGN_END_POINT))),
							HttpMethod.POST, requestWrapperHttpEntity, ResponseWrapper.class).getBody();
			Object responseObject = Objects.requireNonNull(responseWrapper).getResponse();
			JWTSignatureResponseDto responseDto= objectMapper.convertValue(responseObject, JWTSignatureResponseDto.class);
			return responseDto.getJwtSignedData();
		} catch (HttpClientErrorException | HttpServerErrorException e) {
			throw new ServiceException(Errors.JWT_SIGN_EXCEPTION.getErrorCode(),
					Errors.JWT_SIGN_EXCEPTION.getErrorMessage());
		}
	}

	/**
	 * Builds the unsigned client-assertion payload: {@code sub} and {@code iss} are
	 * {@link #clientID}, {@code aud} is {@link #tokenEndpoint}, {@code exp}/{@code iat}
	 * are epoch seconds, and {@code jti} is a random UUID.
	 *
	 * @return JSON bytes of the assertion claims
	 * @throws ServiceException if the claims cannot be serialized
	 */
	private byte[] getClientAssertionData() {
		Map dataToSignMap = new LinkedHashMap();
		dataToSignMap.put(Constants.SUB, clientID);
		dataToSignMap.put(Constants.ISS, clientID);
		dataToSignMap.put(Constants.AUD, tokenEndpoint);
		dataToSignMap.put(Constants.EXP, getExpiryTime());
		dataToSignMap.put(Constants.IAT, getEpochTime());
		dataToSignMap.put(Constants.JTI, java.util.UUID.randomUUID().toString());
		String jsonObject = null;
		try {
			jsonObject = objectMapper.writeValueAsString(dataToSignMap);
		} catch (JsonProcessingException e) {
			throw new ServiceException(Errors.JSON_PROCESSING_EXCEPTION.getErrorCode(),
					Errors.JSON_PROCESSING_EXCEPTION.getErrorMessage());
		}
		return jsonObject.getBytes();
	}

	/**
	 * Current UTC time as epoch seconds ({@code iat}).
	 *
	 * @return {@link Instant#getEpochSecond()} for {@link Instant#now()}
	 */
	private Object getEpochTime() {
		Instant instant = Instant.now();
		return instant.getEpochSecond();
	}

	/**
	 * Assertion expiry as epoch seconds ({@code exp}): now plus
	 * {@link Constants#JWT_EXPIRY_TIME} seconds.
	 *
	 * @return expiry epoch second
	 */
	private Object getExpiryTime() {
		int expirySec = Integer.parseInt(Objects.requireNonNull(this.environment.getProperty(Constants.JWT_EXPIRY_TIME)));
		Instant instant = Instant.now().plusSeconds(expirySec);
		return instant.getEpochSecond();
	}

	/**
	 * Parses Keycloak's error JSON ({@link IAMErrorResponseDto}) from a failed token
	 * HTTP call.
	 *
	 * @param exception Keycloak HTTP error
	 * @return parsed IAM error body
	 * @throws ServiceException if the error body cannot be parsed
	 */
	private IAMErrorResponseDto parseKeyClockErrorResponse(HttpStatusCodeException exception) {
		IAMErrorResponseDto keycloakErrorResponseDto = null;
		try {
			keycloakErrorResponseDto = objectMapper.readValue(exception.getResponseBodyAsString(),
					IAMErrorResponseDto.class);

		} catch (IOException e) {
			throw new ServiceException(Errors.RESPONSE_PARSE_ERROR.getErrorCode(),
					Errors.RESPONSE_PARSE_ERROR.getErrorMessage() + Constants.WHITESPACE + e.getMessage());
		}
		return keycloakErrorResponseDto;
	}

	/**
	 * Builds the post-logout redirect target.
	 * <p>
	 * Offline logout Base64-decodes {@code redirectURI} and returns it (cookies are
	 * expired by the controller). Online logout reads {@code iss} from {@code token} via
	 * {@link JWTUtils#getissuer(String)}, appends {@link #endSessionEndpointPath}, and
	 * adds a URL-encoded {@link #postLogoutRedirectURIParamKey} using
	 * {@link UriComponentsBuilder#fromUriString(String)}.
	 *
	 * @param token access token used to resolve the Keycloak issuer (must be non-empty)
	 * @param redirectURI offline: Base64-encoded application URL; online: already-decoded
	 *                    post-logout URL
	 * @return URL to which the browser should be redirected
	 * @throws AuthenticationServiceException if {@code token} is null or empty
	 * @throws ServiceException if UTF-8 encoding of the post-logout URI fails
	 */
	@Override
	public String logoutUser(String token,String redirectURI) {
		if (EmptyCheckUtils.isNullEmpty(token)) {
			throw new AuthenticationServiceException(Errors.INVALID_TOKEN.getErrorMessage());
		}
		
		if(offlineLogout) {
			return new String(Base64.decodeBase64(redirectURI.getBytes()));
		}
		
		String issuer = JWTUtils.getissuer(token);
		StringBuilder urlBuilder = new StringBuilder().append(issuer).append(endSessionEndpointPath);
		UriComponentsBuilder uriComponentsBuilder;
		try {
			uriComponentsBuilder = UriComponentsBuilder.fromUriString(urlBuilder.toString())
					.queryParam(postLogoutRedirectURIParamKey, URLEncoder.encode(redirectURI, StandardCharsets.UTF_8.toString()));
		} catch (UnsupportedEncodingException e) {
			throw new ServiceException(Errors.UNSUPPORTED_ENCODING_EXCEPTION.getErrorCode(),
					Errors.UNSUPPORTED_ENCODING_EXCEPTION.getErrorMessage() + Constants.WHITESPACE + e.getMessage(), e);
		}
		return uriComponentsBuilder.build().toString();
	}

}
