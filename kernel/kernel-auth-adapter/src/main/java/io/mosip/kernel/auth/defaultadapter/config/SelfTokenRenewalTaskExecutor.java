package io.mosip.kernel.auth.defaultadapter.config;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.reactive.function.client.WebClient;

import com.auth0.jwt.JWT;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;

import io.mosip.kernel.auth.defaultadapter.helper.TokenHelper;
import io.mosip.kernel.auth.defaultadapter.model.TokenHolder;
import io.mosip.kernel.core.exception.ExceptionUtils;
import io.mosip.kernel.core.util.DateUtils2;
import jakarta.annotation.PostConstruct;

/**
 * Schedules periodic renewal of the service's client-credentials token into
 * {@link TokenHolder}.
 * <p>
 * When {@code mosip.iam.adapter.self-token-renewal-enable} is true, a single
 * thread runs {@link SelfTokenHandlerTask} every
 * {@code mosip.iam.adapter.token-expiry-check-frequency} minutes. TokenHelper
 * obtains tokens with WebClient {@code retrieve()}.
 * <p>
 * This adapter is a library other MOSIP services put on the classpath.
 */
public class SelfTokenRenewalTaskExecutor {

	/**
	 * Logger for JWT decode and renewal failures.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(SelfTokenRenewalTaskExecutor.class);

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
	 * Minutes between expiry checks.
	 */
	@Value("${mosip.iam.adapter.token-expiry-check-frequency:5}")
	private int tokenExpiryCheckFrequency;
	
	/**
	 * Minutes before expiry at which the token is treated as invalid and renewed.
	 */
	@Value("${mosip.iam.adapter.renewal-before-expiry-interval:5}")
	private int renewalBeforeExpiryInterval;

	/**
	 * When {@code false}, {@link #init()} does not start the scheduler.
	 */
	@Value("${mosip.iam.adapter.self-token-renewal-enable:true}")
    private boolean isRenewalEnable;

	/**
	 * Shared cache written by this scheduler and read by self-token HTTP clients.
	 */
	private TokenHolder<String> cachedTokenObject;

	/**
	 * Obtains client-credentials tokens from the OIDC token endpoint.
	 */
	private TokenHelper tokenHelper;

	/**
	 * WebClient used to request tokens (typically {@code plainWebClient}).
	 */
	private WebClient webClient;

	/**
	 * Loads client credentials for {@code applName} and stores collaborators.
	 *
	 * @param cachedTokenObject shared token cache
	 * @param webClient         client used to request tokens
	 * @param tokenHelper       client-credentials token client
	 * @param environment       Spring environment for property lookup
	 * @param applName          first {@code spring.application.name} segment
	 */
	public SelfTokenRenewalTaskExecutor(TokenHolder<String> cachedTokenObject, WebClient webClient, TokenHelper tokenHelper,
					Environment environment, String applName) {

		this.cachedTokenObject = cachedTokenObject;
		this.webClient = webClient;
		this.tokenHelper = tokenHelper;
		this.clientID = environment.getProperty("mosip.iam.adapter.clientid." + applName, environment.getProperty("mosip.iam.adapter.clientid", ""));
		this.clientSecret = environment.getProperty("mosip.iam.adapter.clientsecret." + applName, environment.getProperty("mosip.iam.adapter.clientsecret", ""));
		this.appID = environment.getProperty("mosip.iam.adapter.appid." + applName, environment.getProperty("mosip.iam.adapter.appid", ""));
	}

	/**
	 * Starts a single-thread scheduler when self-token renewal is enabled.
	 */
	@PostConstruct
	private void init() {
		if(isRenewalEnable) {
			ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
			taskScheduler.setPoolSize(1);
			taskScheduler.initialize();
			taskScheduler.scheduleAtFixedRate(new SelfTokenHandlerTask(), TimeUnit.MINUTES.toMillis(tokenExpiryCheckFrequency));
		}
	}

	/**
	 * Runnable that refreshes {@link #cachedTokenObject} when the token is missing
	 * or no longer valid per {@link SelfTokenRenewalTaskExecutor#isTokenValid(String)}.
	 */
	private class SelfTokenHandlerTask implements Runnable {

		/**
		 * Fetches a new client token when the cache is empty or the JWT is expired,
		 * about to expire, or bound to a different client id.
		 */
		public void run() {
			if (cachedTokenObject.getToken() == null || !isTokenValid(cachedTokenObject.getToken())) {
				String authToken = tokenHelper.getClientToken(clientID, clientSecret, appID, webClient);
				cachedTokenObject.setToken(authToken);
			}
		}
	}

	/**
	 * Decodes {@code authToken} and returns {@code true} when expiry is still
	 * after now plus {@link #renewalBeforeExpiryInterval} and the {@code clientId}
	 * claim matches {@link #clientID}.
	 *
	 * @param authToken the cached JWT
	 * @return {@code false} on expiry, client mismatch, or decode failure
	 */
	private boolean isTokenValid(String authToken) {
		try {
			DecodedJWT decodedJWT = JWT.decode(authToken);
			Map<String, Claim> claims = decodedJWT.getClaims();
			LocalDateTime expiryTime = DateUtils2.convertUTCToLocalDateTime(DateUtils2.getUTCTimeFromDate(decodedJWT.getExpiresAt()));

			// time is added here so that expiry will be checked after that time and if it
			// does it will renew token
			if (!DateUtils2.before(DateUtils2.getUTCCurrentDateTime().plusMinutes(renewalBeforeExpiryInterval), expiryTime)) {
				return false;
			} else if (!claims.get("clientId").asString().equals(clientID)) {
				return false;
			} else {
				return true;
			}
		} catch (JWTDecodeException e) {
			LOGGER.error("JWT DECODE EXCEPTION ::".concat(e.getMessage()).concat(ExceptionUtils.getStackTrace(e)));
		} catch (Exception e) {
			LOGGER.error(e.getMessage().concat(ExceptionUtils.getStackTrace(e)));
		}
		return false;
	}

}
