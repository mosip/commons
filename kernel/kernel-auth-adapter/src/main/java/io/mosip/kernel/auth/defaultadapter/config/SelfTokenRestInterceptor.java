package io.mosip.kernel.auth.defaultadapter.config;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.kernel.auth.defaultadapter.constant.AuthAdapterConstant;
import io.mosip.kernel.auth.defaultadapter.constant.AuthAdapterErrorCode;
import io.mosip.kernel.auth.defaultadapter.exception.AuthAdapterException;
import io.mosip.kernel.auth.defaultadapter.exception.AuthRestException;
import io.mosip.kernel.auth.defaultadapter.util.TokenHolder;
import io.mosip.kernel.core.authmanager.model.ClientSecret;
import io.mosip.kernel.core.exception.ExceptionUtils;
import io.mosip.kernel.core.exception.ServiceError;
import io.mosip.kernel.core.http.RequestWrapper;

/**
 * This class intercepts and renew client token.
 * 
 * @author Urvil Joshi
 *
 */
public class SelfTokenRestInterceptor implements ClientHttpRequestInterceptor {

	private String clientID;

	private String clientSecret;

	private String appID;

	private String tokenURL;
	
	private String validateTokenURL;

	private TokenHolder<String> cachedToken;

	private static final Logger LOGGER = LoggerFactory.getLogger(SelfTokenRestInterceptor.class);

	private RestTemplate restTemplate;

	public SelfTokenRestInterceptor(Environment environment, RestTemplate restTemplate,
			TokenHolder<String> cachedToken) {
		clientID = environment.getProperty("mosip.iam.adapter.clientid", "");
		clientSecret = environment.getProperty("mosip.iam.adapter.clientsecret", "");
		appID = environment.getProperty("mosip.iam.adapter.appid", "");
		tokenURL = environment.getProperty("mosip.authmanager.client-token-endpoint", "");
		validateTokenURL = environment.getProperty("auth.server.admin.validate.url", "");
		this.cachedToken = cachedToken;
		this.restTemplate = restTemplate;
	}

	ObjectMapper objectMapper = new ObjectMapper();

	@Override
	public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
			throws IOException {
		// null check if job is not able to fetch client id secret
		if (cachedToken.getToken() == null) {
			LOGGER.error("there is some issue with getting token with clienid and secret");
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
			String authToken = getClientToken(clientID, clientSecret, appID);
			cachedToken.setToken(authToken);		
			}
		}
		
		List<String> cookies = request.getHeaders().get(AuthAdapterConstant.AUTH_HEADER_COOKIE);
		if (cookies != null && !cookies.isEmpty()) {
			cookies=cookies.stream().filter(str -> !str.contains(AuthAdapterConstant.AUTH_HEADER)).collect(Collectors.toList());
		}
		request.getHeaders().replace(AuthAdapterConstant.AUTH_HEADER_COOKIE, cookies);
		request.getHeaders().add(AuthAdapterConstant.AUTH_HEADER_COOKIE,
				AuthAdapterConstant.AUTH_HEADER + cachedToken.getToken());
		return execution.execute(request, body);

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
		HttpHeaders headers = new HttpHeaders();
		headers.add(AuthAdapterConstant.AUTH_HEADER_COOKIE, AuthAdapterConstant.AUTH_HEADER + authToken);
		HttpEntity<String> requestEntity = new HttpEntity<>(headers);
		HttpEntity<String> response = null;
		try {
			response = restTemplate.exchange(validateTokenURL, HttpMethod.GET, requestEntity, String.class);
		} catch (HttpServerErrorException | HttpClientErrorException e) {
			if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
				LOGGER.error("Token not valid {}", e.getResponseBodyAsString());
				return false;
			}
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

		return true;
	}
}
