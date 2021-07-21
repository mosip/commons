package io.mosip.kernel.auth.defaultadapter.config;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import com.auth0.jwt.JWT;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;

import io.mosip.kernel.auth.defaultadapter.constant.AuthAdapterConstant;
import io.mosip.kernel.auth.defaultadapter.constant.AuthAdapterErrorCode;
import io.mosip.kernel.auth.defaultadapter.exception.AuthAdapterException;
import io.mosip.kernel.auth.defaultadapter.exception.AuthRestException;
import io.mosip.kernel.auth.defaultadapter.helper.TokenHelper;
import io.mosip.kernel.auth.defaultadapter.model.TokenHolder;
import io.mosip.kernel.core.authmanager.model.ClientSecret;
import io.mosip.kernel.core.exception.ExceptionUtils;
import io.mosip.kernel.core.exception.ServiceError;
import io.mosip.kernel.core.http.RequestWrapper;
import io.mosip.kernel.core.util.DateUtils;
//TODO change props to conditional properties
public class SelfTokenRenewalTaskExecutor {

	private static final Logger LOGGER = LoggerFactory.getLogger(SelfTokenRenewalTaskExecutor.class);

	@Value("${mosip.iam.adapter.clientid:}")
	private String clientID;

	@Value("${mosip.iam.adapter.clientsecret:}")
	private String clientSecret;

	@Value("${mosip.iam.adapter.issuer-url:}")
	private String issuerURL;

	@Value("${mosip.iam.adapter.appid:}")
	private String appID;

	@Value("${mosip.authmanager.client-token-endpoint:}")
	private String tokenURL;

	@Value("${mosip.iam.adapter.token-expiry-check-frequency:5}")
	private int tokenExpiryCheckFrequency;
	
	@Value("${mosip.iam.adapter.renewal-before-expiry-interval:5}")
	private int renewalBeforeExpiryInterval;
	
	@Value("${mosip.iam.adapter.self-token-renewal-enable:false}")
	private boolean isRenewalEnable;

	private TokenHolder<String> cachedTokenObject;

	private RestTemplate restTemplate;
	
	private TokenHelper tokenHelper;

	public SelfTokenRenewalTaskExecutor(TokenHolder<String> cachedTokenObject, RestTemplate restTemplate,TokenHelper tokenHelper) {

		this.cachedTokenObject = cachedTokenObject;
		this.restTemplate = restTemplate;
		this.tokenHelper = tokenHelper;
	}

	@PostConstruct
	private void init() {
		if(isRenewalEnable) {
		ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
		taskScheduler.setPoolSize(1);
		taskScheduler.initialize();
		taskScheduler.scheduleAtFixedRate(new SelfTokenHandlerTask(), TimeUnit.MINUTES.toMillis(tokenExpiryCheckFrequency));
		}
	}

	private class SelfTokenHandlerTask implements Runnable {

		public void run() {
			if (cachedTokenObject.getToken() == null || !isTokenValid(cachedTokenObject.getToken())) {
				String authToken =tokenHelper.getClientToken(clientID, clientSecret, appID,restTemplate,tokenURL);
				cachedTokenObject.setToken(authToken);
			}
		}
	}


	private boolean isTokenValid(String authToken) {
		try {
			DecodedJWT decodedJWT = JWT.decode(authToken);
			Map<String, Claim> claims = decodedJWT.getClaims();
			LocalDateTime expiryTime = DateUtils
					.convertUTCToLocalDateTime(DateUtils.getUTCTimeFromDate(decodedJWT.getExpiresAt()));

			
				// time is added here so that expiry will be checked after that time and if it
				// does it will renew token
			if (!DateUtils.before(DateUtils.getUTCCurrentDateTime().plusMinutes(renewalBeforeExpiryInterval),
					expiryTime)) {
				return false;
			} else if (!claims.get("clientId").asString().equals(clientID)) {
				return false;
			} else {
				return true;
			}
		} catch (JWTDecodeException e) {
			LOGGER.error("JWT DECODE EXCEPTION ::".concat(e.getMessage()).concat(ExceptionUtils.getStackTrace(e)));
			return false;
		} catch (Exception e) {
			LOGGER.error(e.getMessage().concat(ExceptionUtils.getStackTrace(e)));
			return false;
		}

	}

}