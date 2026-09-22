package io.mosip.kernel.auth.defaultimpl.intercepter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import jakarta.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import io.mosip.kernel.auth.defaultimpl.constant.AuthConstant;
import io.mosip.kernel.auth.defaultimpl.constant.AuthErrorCode;
import io.mosip.kernel.auth.defaultimpl.dto.AccessTokenResponse;
import io.mosip.kernel.auth.defaultimpl.exception.AuthManagerException;
import io.mosip.kernel.auth.defaultimpl.util.MemoryCache;
import io.mosip.kernel.auth.defaultimpl.util.TokenValidator;

/**
 * {@link ClientHttpRequestInterceptor} that attaches a Keycloak admin Bearer
 * token to outbound RestTemplate calls from authmanager.
 * <p>
 * Caches the token in {@link MemoryCache} under {@code adminToken}. Uses the
 * password grant when no token exists or the refresh token is expired, and the
 * refresh-token grant when only the access token is expired. Token endpoint is
 * {@code mosip.iam.open-id-url} plus {@code /token}, built with
 * {@link UriComponentsBuilder#fromUriString}.
 *
 * @author Urvil Joshi
 * @author Srinivasan
 */
public class RestInterceptor implements ClientHttpRequestInterceptor {

	/**
	 * Logger for token refresh decisions and Keycloak HTTP errors.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(RestInterceptor.class);

	/**
	 * In-memory cache of the last admin {@link AccessTokenResponse} (key
	 * {@code adminToken}).
	 */
	private MemoryCache<String, AccessTokenResponse> memoryCache;

	/**
	 * JWT expiry checker for access and refresh tokens.
	 */
	private TokenValidator tokenValidator;

	/**
	 * RestTemplate used to call the Keycloak token endpoint (not the intercepted
	 * client).
	 */
	private RestTemplate restTemplate;

	/**
	 * Creates an interceptor with the given cache, validator, and token client.
	 *
	 * @param memoryCache    cache that stores the admin token response
	 * @param tokenValidator expiry checker for cached JWTs
	 * @param restTemplate   client used only for the token endpoint
	 */
	public RestInterceptor(MemoryCache<String, AccessTokenResponse> memoryCache, TokenValidator tokenValidator,
			RestTemplate restTemplate) {
		this.memoryCache = memoryCache;
		this.tokenValidator = tokenValidator;
		this.restTemplate = restTemplate;

	}

	/**
	 * Keycloak OpenID base URL ({@code mosip.iam.open-id-url}), typically including
	 * a {@code {realmId}} path variable.
	 */
	@Value("${mosip.iam.open-id-url}")
	private String keycloakOpenIdUrl;

	/**
	 * Master/admin realm id substituted into the token URL
	 * ({@code mosip.iam.master.realm-id}).
	 */
	@Value("${mosip.iam.master.realm-id}")
	private String realmId;

	/**
	 * OAuth client id for the admin password/refresh grant
	 * ({@code mosip.keycloak.admin.client.id}).
	 */
	@Value("${mosip.keycloak.admin.client.id}")
	private String adminClientID;

	/**
	 * Admin username for the password grant ({@code mosip.keycloak.admin.user.id}).
	 */
	@Value("${mosip.keycloak.admin.user.id}")
	private String adminUserName;

	/**
	 * Admin password / client secret used as the password grant credential
	 * ({@code mosip.keycloak.admin.secret.key}).
	 */
	@Value("${mosip.keycloak.admin.secret.key}")
	private String adminSecret;

	/**
	 * Ensures a valid admin access token is on the request as
	 * {@code Authorization: Bearer ...}, refreshing from Keycloak when needed.
	 *
	 * @param request   outbound HTTP request
	 * @param body      request body
	 * @param execution next interceptor / request factory
	 * @return the executed response
	 * @throws IOException           if the request cannot be executed
	 * @throws AuthManagerException if no admin token can be obtained
	 */
	@Override
	public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
			throws IOException {
		AccessTokenResponse accessTokenResponse = null;
		if ((accessTokenResponse = memoryCache.get("adminToken")) != null) {
			boolean accessTokenExpired = tokenValidator.isExpired(accessTokenResponse.getAccess_token());
			boolean refreshTokenExpired = tokenValidator.isExpired(accessTokenResponse.getRefresh_token());
			LOGGER.info(
					"access token expired: " + accessTokenExpired + " ,refresh token expired: " + refreshTokenExpired);
			if (refreshTokenExpired) {
				accessTokenResponse = getAdminToken(false, null);
			} else if (accessTokenExpired) {
				accessTokenResponse = getAdminToken(true, accessTokenResponse.getRefresh_token());
			}

		} else {
			accessTokenResponse = getAdminToken(false, null);
		}
		if (accessTokenResponse != null) {
			memoryCache.put("adminToken", accessTokenResponse);
			request.getHeaders().add("Authorization", "Bearer " + accessTokenResponse.getAccess_token());
		} else {
			throw new AuthManagerException(AuthErrorCode.REST_EXCEPTION.getErrorCode(),
					"admin access token response is null");
		}
		return execution.execute(request, body);
	}

	/**
	 * Requests an admin token from Keycloak using either the refresh-token grant
	 * or the password grant.
	 *
	 * @param isGetRefreshToken {@code true} to use {@code refresh_token} grant
	 * @param refreshToken      refresh token when {@code isGetRefreshToken} is true;
	 *                          ignored otherwise
	 * @return token response body, or {@code null} if the HTTP call failed
	 */
	private AccessTokenResponse getAdminToken(boolean isGetRefreshToken, String refreshToken) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		MultiValueMap<String, String> tokenRequestBody = null;
		Map<String, String> pathParams = new HashMap<>();
		pathParams.put(AuthConstant.REALM_ID, realmId);
		UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromUriString(keycloakOpenIdUrl + "/token");
		LOGGER.info("location " + uriComponentsBuilder.toUriString() + " refresh token expired: " + isGetRefreshToken);
		if (isGetRefreshToken) {
			tokenRequestBody = getAdminValueMap(refreshToken);
		} else {
			tokenRequestBody = getAdminValueMap();
		}

		HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(tokenRequestBody, headers);
		ResponseEntity<AccessTokenResponse> response = null;
		try {
			response = restTemplate.postForEntity(uriComponentsBuilder.buildAndExpand(pathParams).toUriString(),
					request, AccessTokenResponse.class);
		} catch (HttpServerErrorException | HttpClientErrorException ex) {
			LOGGER.error(ex.getMessage());
		}

		return response != null ? response.getBody() : null;
	}

	/**
	 * Form body for the admin password grant (username, password, client id).
	 *
	 * @return URL-encoded token request fields
	 */
	private MultiValueMap<String, String> getAdminValueMap() {
		MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
		map.add(AuthConstant.GRANT_TYPE, AuthConstant.PASSWORDCONSTANT);
		map.add(AuthConstant.USER_NAME, adminUserName);
		map.add(AuthConstant.PASSWORDCONSTANT, adminSecret);
		map.add(AuthConstant.CLIENT_ID, adminClientID);
		return map;
	}

	/**
	 * Form body for the admin refresh-token grant.
	 *
	 * @param refreshToken previously issued refresh token
	 * @return URL-encoded token request fields
	 */
	private MultiValueMap<String, String> getAdminValueMap(String refreshToken) {
		MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
		map.add(AuthConstant.GRANT_TYPE, AuthConstant.REFRESH_TOKEN);
		map.add(AuthConstant.REFRESH_TOKEN, refreshToken);
		map.add(AuthConstant.CLIENT_ID, adminClientID);
		return map;
	}
}
