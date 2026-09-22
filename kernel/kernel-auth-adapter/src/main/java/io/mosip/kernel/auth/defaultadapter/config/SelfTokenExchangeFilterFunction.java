package io.mosip.kernel.auth.defaultadapter.config;

import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;

import io.mosip.kernel.auth.defaultadapter.constant.AuthAdapterConstant;
import io.mosip.kernel.auth.defaultadapter.constant.AuthAdapterErrorCode;
import io.mosip.kernel.auth.defaultadapter.exception.AuthAdapterException;
import io.mosip.kernel.auth.defaultadapter.helper.TokenHelper;
import io.mosip.kernel.auth.defaultadapter.helper.TokenValidationHelper;
import io.mosip.kernel.auth.defaultadapter.model.TokenHolder;
import reactor.core.publisher.Mono;
/**
 * {@link WebClient} {@link ExchangeFilterFunction} that attaches the service's
 * own client-credentials token and renews it after HTTP 401.
 * <p>
 * Downstream calls use {@code ExchangeFunction#exchange}. TokenHelper and
 * ValidateTokenHelper use {@code retrieve()} plus Jackson 2 on a String body
 * (Boot 4 default codecs are Jackson 3). On HTTP 401 the cookie is replaced by
 * rebuilding {@link ClientRequest} because its headers are read-only.
 * <p>
 * This adapter is a library other MOSIP services put on the classpath.
 *
 * @author Mahammed Taheer
 */
public class SelfTokenExchangeFilterFunction implements ExchangeFilterFunction {

    /**
     * Logger for token-fetch failures.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(SelfTokenExchangeFilterFunction.class);
    
    /**
     * OIDC client id, resolved per application name with a global fallback.
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
	 * Obtains client-credentials tokens from the OIDC token endpoint.
	 */
	private TokenHelper tokenHelper;

	/**
	 * Online token validation used before renewing after HTTP 401.
	 */
	private TokenValidationHelper tokenValidationHelper;

    /**
     * WebClient used only to fetch and validate tokens (typically
     * {@code plainWebClient}).
     */
    private WebClient webClient;

    /**
     * Loads client credentials for {@code applName} and stores collaborators.
     *
     * @param environment           Spring environment for property lookup
     * @param webClient             client used to request and validate tokens
     * @param cachedToken           shared token cache
     * @param tokenHelper           client-credentials token client
     * @param tokenValidationHelper online token validator
     * @param applName              first {@code spring.application.name} segment
     */
    public SelfTokenExchangeFilterFunction(Environment environment, WebClient webClient,
                    TokenHolder<String> cachedToken, TokenHelper tokenHelper, TokenValidationHelper tokenValidationHelper,
                    String applName) {
        clientID = environment.getProperty("mosip.iam.adapter.clientid." + applName, environment.getProperty("mosip.iam.adapter.clientid", ""));
        clientSecret = environment.getProperty("mosip.iam.adapter.clientsecret." + applName, environment.getProperty("mosip.iam.adapter.clientsecret", ""));
        appID = environment.getProperty("mosip.iam.adapter.appid." + applName, environment.getProperty("mosip.iam.adapter.appid", ""));
        this.cachedToken = cachedToken;
        this.webClient = webClient;
        this.tokenHelper = tokenHelper;
        this.tokenValidationHelper = tokenValidationHelper;
    }

    /**
     * Attaches the cached Authorization cookie, exchanges the request, and on
     * HTTP 401 validates then renews the token and retries once.
     *
     * @param request the outbound client request
     * @param next    the next exchange function in the WebClient filter chain
     * @return a {@link Mono} of the client response
     */
    @Override
    public Mono<ClientResponse> filter(ClientRequest request, ExchangeFunction next) {
        
        // null check if job is not able to fetch client id secret
		if (cachedToken.getToken() == null) {
            // try requesting new token. Added because IDA need the token before it gets created by the scheduler thread.
            String authToken = tokenHelper.getClientToken(clientID, clientSecret, appID, webClient);
			if (Objects.isNull(authToken)) {
			    LOGGER.error("there is some issue with getting token with clienid and secret");
			    throw new AuthAdapterException(AuthAdapterErrorCode.SELF_AUTH_TOKEN_NULL.getErrorCode(),
					AuthAdapterErrorCode.SELF_AUTH_TOKEN_NULL.getErrorMessage());
            }
            cachedToken.setToken(authToken);
		}
        
        ClientRequest newReq = ClientRequest.from(request).header(AuthAdapterConstant.AUTH_HEADER_COOKIE,
						AuthAdapterConstant.AUTH_HEADER + cachedToken.getToken()).build();
        Mono<ClientResponse>  clientResponse = next.exchange(newReq);
        ClientResponse response = clientResponse.block();
	if (response != null && response.statusCode() != HttpStatus.UNAUTHORIZED) {
            return Mono.just(response);
        }

        synchronized (this) {
			// online validation
			if(!isTokenValid(cachedToken.getToken())) {
				String authToken = tokenHelper.getClientToken(clientID, clientSecret, appID, webClient);
				cachedToken.setToken(authToken);		
			}
		}

        List<String> cookies = request.headers().getOrEmpty(AuthAdapterConstant.AUTH_HEADER_COOKIE).stream()
			.filter(str -> !str.contains(AuthAdapterConstant.AUTH_HEADER)).collect(Collectors.toList());
		ClientRequest.Builder retryBuilder = ClientRequest.from(request);
		retryBuilder.headers(headers -> {
			headers.remove(AuthAdapterConstant.AUTH_HEADER_COOKIE);
			cookies.forEach(cookie -> headers.add(AuthAdapterConstant.AUTH_HEADER_COOKIE, cookie));
			headers.add(AuthAdapterConstant.AUTH_HEADER_COOKIE,
					AuthAdapterConstant.AUTH_HEADER + cachedToken.getToken());
		});
		return next.exchange(retryBuilder.build());
    }

    /**
     * Returns whether online user-info validation still accepts {@code authToken}.
     *
     * @param authToken the cached access token
     * @return {@code true} if validation returned a user
     */
	private boolean isTokenValid(String authToken) {
		return Objects.nonNull(tokenValidationHelper.doOnlineTokenValidation(authToken, webClient));
	}
}
