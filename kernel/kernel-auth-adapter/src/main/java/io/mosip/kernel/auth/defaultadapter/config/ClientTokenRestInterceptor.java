package io.mosip.kernel.auth.defaultadapter.config;

import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
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
import io.mosip.kernel.auth.defaultadapter.util.MemoryCache;
import io.mosip.kernel.core.authmanager.model.ClientSecret;
import io.mosip.kernel.core.authmanager.model.MosipUserDto;
import io.mosip.kernel.core.exception.ExceptionUtils;
import io.mosip.kernel.core.exception.ServiceError;
import io.mosip.kernel.core.http.ResponseWrapper;

/** This class intercepts and renew client token.
 * 
 * @author Urvil Joshi
 *
 */
public class ClientTokenRestInterceptor implements ClientHttpRequestInterceptor {

	private static final String CLIENT_TOKEN_KEY = "clientToken";

	private String clientID;

	private String clientSecret;

	private String appID;

	private String tokenURL;

	private String validateTokenURL;

	private MemoryCache<String, String> memoryCache;

	private static final Logger LOGGER = LoggerFactory.getLogger(ClientTokenRestInterceptor.class);

	private RestTemplate restTemplate;

	public ClientTokenRestInterceptor(Environment environment) {
		clientID = environment.getProperty("mosip.iam.adapter.clientid", "");
		clientSecret = environment.getProperty("mosip.iam.adapter.clientsecret", "");
		appID = environment.getProperty("mosip.iam.adapter.appid", "");
		tokenURL = environment.getProperty("mosip.authmanager.base-url", "")
				+ environment.getProperty("mosip.authmanager.token-endpoint", "");
		validateTokenURL = environment.getProperty("auth.server.admin.validate.url", "");
		memoryCache = new MemoryCache<>(1);
		restTemplate = new RestTemplate();
	}

	ObjectMapper objectMapper = new ObjectMapper();

	@Override
	public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
			throws IOException {
		if (memoryCache.get(CLIENT_TOKEN_KEY)==null || !isTokenValid(memoryCache.get(CLIENT_TOKEN_KEY))) {
			String authToken = getClientToken(clientID, clientSecret, appID);
			memoryCache.put(CLIENT_TOKEN_KEY, authToken);
		}

		request.getHeaders().add(AuthAdapterConstant.COOKIE,
				AuthAdapterConstant.AUTH_HEADER + memoryCache.get(CLIENT_TOKEN_KEY));
		return execution.execute(request, body);
	}

	private String getClientToken(String clientID, String clienSecret, String appID) {
		ClientSecret clientCred = new ClientSecret();
		clientCred.setAppId(appID);
		clientCred.setClientId(clientID);
		clientCred.setSecretKey(clienSecret);
		HttpEntity<String> response = null;
		try {
			response = restTemplate.postForEntity(tokenURL, clientCred, String.class);
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
		ResponseWrapper<?> responseObject;
		MosipUserDto mosipUserDto;
		try {
			responseObject = objectMapper.readValue(response.getBody(), ResponseWrapper.class);
			mosipUserDto = objectMapper.readValue(objectMapper.writeValueAsString(responseObject.getResponse()),
					MosipUserDto.class);
		} catch (IOException e) {
			throw new AuthAdapterException(AuthAdapterErrorCode.IO_EXCEPTION.getErrorCode(),
					AuthAdapterErrorCode.IO_EXCEPTION.getErrorMessage());
		}

		return mosipUserDto.getToken();
	}

	private boolean isTokenValid(String authToken) {
		HttpHeaders headers = new HttpHeaders();
		headers.add(AuthAdapterConstant.COOKIE, AuthAdapterConstant.AUTH_HEADER + authToken);
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
