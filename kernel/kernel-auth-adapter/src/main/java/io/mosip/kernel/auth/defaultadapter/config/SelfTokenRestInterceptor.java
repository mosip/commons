package io.mosip.kernel.auth.defaultadapter.config;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.RestTemplate;

import io.mosip.kernel.auth.defaultadapter.constant.AuthAdapterConstant;
import io.mosip.kernel.auth.defaultadapter.constant.AuthAdapterErrorCode;
import io.mosip.kernel.auth.defaultadapter.exception.AuthAdapterException;
import io.mosip.kernel.auth.defaultadapter.helper.TokenHelper;
import io.mosip.kernel.auth.defaultadapter.helper.TokenValidationHelper;
import io.mosip.kernel.auth.defaultadapter.model.TokenHolder;

/**
 * {@link RestTemplate} interceptor that attaches the service's own
 * client-credentials token and renews it after an HTTP 401.
 * <p>
 * Cookie replacement uses remove-then-add on {@code HttpHeaders} (Spring 6/7
 * {@code HttpHeaders} no longer has a replace helper). The cached token is
 * shared with {@link SelfTokenRenewalTaskExecutor}.
 * <p>
 * This adapter is a library other MOSIP services put on the classpath.
 *
 * @author Urvil Joshi
 */
public class SelfTokenRestInterceptor implements ClientHttpRequestInterceptor {

	/**
	 * OIDC client id, resolved per {@code spring.application.name} with a global
	 * fallback.
	 */
	private String clientID;

	/**
	 * OIDC client secret, resolved per application name with a global fallback.
	 */
	private String clientSecret;

	/**
	 * MOSIP application id used to look up the Keycloak realm.
	 */
	private String appID;

	/**
	 * Shared cache of the current client-credentials access token.
	 */
	private TokenHolder<String> cachedToken;

	/**
	 * Logger for token-fetch failures.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(SelfTokenRestInterceptor.class);

	/**
	 * RestTemplate used only to fetch and validate tokens (typically
	 * {@code plainRestTemplate}).
	 */
	private RestTemplate restTemplate;
	
	/**
	 * Obtains client-credentials tokens from the OIDC token endpoint.
	 */
	private TokenHelper tokenHelper;

	/**
	 * Online token validation used before renewing after HTTP 401.
	 */
	private TokenValidationHelper tokenValidationHelper;

	/**
	 * Loads client credentials for {@code applName} and stores collaborators.
	 *
	 * @param environment           Spring environment for property lookup
	 * @param restTemplate          client used to request and validate tokens
	 * @param cachedToken           shared token cache
	 * @param tokenHelper           client-credentials token client
	 * @param tokenValidationHelper online token validator
	 * @param applName              first {@code spring.application.name} segment
	 */
	public SelfTokenRestInterceptor(Environment environment, RestTemplate restTemplate,
			TokenHolder<String> cachedToken, TokenHelper tokenHelper, TokenValidationHelper tokenValidationHelper,
			String applName) {
		clientID = environment.getProperty("mosip.iam.adapter.clientid." + applName, environment.getProperty("mosip.iam.adapter.clientid", ""));
		clientSecret = environment.getProperty("mosip.iam.adapter.clientsecret." + applName, environment.getProperty("mosip.iam.adapter.clientsecret", ""));
		appID = environment.getProperty("mosip.iam.adapter.appid." + applName, environment.getProperty("mosip.iam.adapter.appid", ""));
		this.cachedToken = cachedToken;
		this.restTemplate = restTemplate;
		this.tokenHelper = tokenHelper;
		this.tokenValidationHelper = tokenValidationHelper;
	}

	/**
	 * Attaches {@code Cookie: Authorization=<cached token>}, executes the call,
	 * and on HTTP 401 validates then renews the token and retries once.
	 *
	 * @param request   the outbound HTTP request
	 * @param body      the request body
	 * @param execution the remainder of the interceptor chain
	 * @return the HTTP response (original or retry)
	 * @throws IOException if the request fails to execute
	 */
	@Override
	public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
			throws IOException {
		// null check if job is not able to fetch client id secret
		if (cachedToken.getToken() == null) {
			// try requesting new token. Added because IDA need the token before it gets created by the scheduler thread.
            String authToken = tokenHelper.getClientToken(clientID, clientSecret, appID, restTemplate);
			if (Objects.isNull(authToken)) {
				LOGGER.error("there is some issue with getting token with clienid and secret");
				throw new AuthAdapterException(AuthAdapterErrorCode.SELF_AUTH_TOKEN_NULL.getErrorCode(),
						AuthAdapterErrorCode.SELF_AUTH_TOKEN_NULL.getErrorMessage());
			}
			cachedToken.setToken(authToken);
		}
		request.getHeaders().add(AuthAdapterConstant.AUTH_HEADER_COOKIE,
				AuthAdapterConstant.AUTH_HEADER + cachedToken.getToken());

		ClientHttpResponse clientHttpResponse = execution.execute(request, body);
		if(clientHttpResponse.getStatusCode() != HttpStatus.UNAUTHORIZED) {
			return clientHttpResponse;
		}
		
		synchronized (this) {
			// online validation
			if(!isTokenValid(cachedToken.getToken())) {
				String authToken = tokenHelper.getClientToken(clientID, clientSecret, appID, restTemplate);
				cachedToken.setToken(authToken);		
			}
		}
		
		List<String> cookies = request.getHeaders().get(AuthAdapterConstant.AUTH_HEADER_COOKIE);
		if (cookies != null && !cookies.isEmpty()) {
			cookies=cookies.stream().filter(str -> !str.contains(AuthAdapterConstant.AUTH_HEADER)).collect(Collectors.toList());
		}
		request.getHeaders().remove(AuthAdapterConstant.AUTH_HEADER_COOKIE);
		if (cookies != null && !cookies.isEmpty()) {
			cookies.forEach(cookie -> request.getHeaders().add(AuthAdapterConstant.AUTH_HEADER_COOKIE, cookie));
		}
		request.getHeaders().add(AuthAdapterConstant.AUTH_HEADER_COOKIE,
				AuthAdapterConstant.AUTH_HEADER + cachedToken.getToken());
		return execution.execute(request, body);

	}

	/**
	 * Returns whether online user-info validation still accepts {@code authToken}.
	 *
	 * @param authToken the cached access token
	 * @return {@code true} if validation returned a user
	 */
	private boolean isTokenValid(String authToken) {
		return Objects.nonNull(tokenValidationHelper.getOnlineTokenValidatedUserResponse(authToken, restTemplate));
	}
}
