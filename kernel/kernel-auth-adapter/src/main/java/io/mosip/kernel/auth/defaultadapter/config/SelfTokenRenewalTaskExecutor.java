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
import io.mosip.kernel.auth.defaultadapter.util.TokenHolder;
import io.mosip.kernel.core.authmanager.model.ClientSecret;
import io.mosip.kernel.core.exception.ExceptionUtils;
import io.mosip.kernel.core.exception.ServiceError;
import io.mosip.kernel.core.http.RequestWrapper;
import io.mosip.kernel.core.util.DateUtils;

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

	private TokenHolder<String> cachedTokenObject;

	private RestTemplate restTemplate;

	public SelfTokenRenewalTaskExecutor(TokenHolder<String> cachedTokenObject, RestTemplate restTemplate) {

		this.cachedTokenObject = cachedTokenObject;
		this.restTemplate = restTemplate;
	}

	@PostConstruct
	private void init() {
		ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
		taskScheduler.setPoolSize(1);
		taskScheduler.initialize();
		taskScheduler.scheduleAtFixedRate(new SelfTokenHandlerTask(), TimeUnit.MINUTES.toMillis(tokenExpiryCheckFrequency));
	}

	private class SelfTokenHandlerTask implements Runnable {

		public void run() {
			if (cachedTokenObject.getToken() == null || !isTokenValid(cachedTokenObject.getToken())) {
				String authToken = getClientToken(clientID, clientSecret, appID);
				cachedTokenObject.setToken(authToken);
			}
		}
	}

	private String getClientToken(String clientID, String clienSecret, String appID) {
		RequestWrapper<ClientSecret> requestWrapper = new RequestWrapper<>();
		ClientSecret clientCred = new ClientSecret();
		clientCred.setAppId(appID);
		clientCred.setClientId(clientID);
		clientCred.setSecretKey(clienSecret);
		requestWrapper.setRequest(clientCred);
		HttpEntity<String> response = null;
		try {
			response = restTemplate.postForEntity(tokenURL, requestWrapper, String.class);
		} catch (HttpServerErrorException | HttpClientErrorException e) {
			LOGGER.error("error connecting to auth service {}", e.getResponseBodyAsString());
			throw new AuthAdapterException(AuthAdapterErrorCode.CANNOT_CONNECT_TO_AUTH_SERVICE.getErrorCode(),
					e.getResponseBodyAsString());
		}
		if (response == null) {
			throw new AuthAdapterException(AuthAdapterErrorCode.CANNOT_CONNECT_TO_AUTH_SERVICE.getErrorCode(),
					AuthAdapterErrorCode.CANNOT_CONNECT_TO_AUTH_SERVICE.getErrorMessage());
		}
		String responseBody = response.getBody();
		List<ServiceError> validationErrorList = ExceptionUtils.getServiceErrorList(responseBody);
		if (!validationErrorList.isEmpty()) {
			throw new AuthRestException(validationErrorList);
		}
		HttpHeaders headers = response.getHeaders();
		List<String> cookies = headers.get(AuthAdapterConstant.AUTH_HEADER_SET_COOKIE);
		if (cookies == null || cookies.isEmpty())
			throw new AuthAdapterException(AuthAdapterErrorCode.IO_EXCEPTION.getErrorCode(),
					AuthAdapterErrorCode.IO_EXCEPTION.getErrorMessage());

		String authToken = cookies.get(0).split(";")[0].split(AuthAdapterConstant.AUTH_HEADER)[1];

		return authToken;
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