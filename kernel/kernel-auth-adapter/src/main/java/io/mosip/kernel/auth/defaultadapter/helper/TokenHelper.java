package io.mosip.kernel.auth.defaultadapter.helper;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import io.mosip.kernel.auth.defaultadapter.config.SelfTokenRestInterceptor;
import io.mosip.kernel.auth.defaultadapter.constant.AuthAdapterConstant;
import io.mosip.kernel.auth.defaultadapter.constant.AuthAdapterErrorCode;
import io.mosip.kernel.auth.defaultadapter.exception.AuthAdapterException;
import io.mosip.kernel.auth.defaultadapter.exception.AuthRestException;
import io.mosip.kernel.core.authmanager.model.ClientSecret;
import io.mosip.kernel.core.exception.ExceptionUtils;
import io.mosip.kernel.core.exception.ServiceError;
import io.mosip.kernel.core.http.RequestWrapper;

@Component
public class TokenHelper {

	private static final Logger LOGGER = LoggerFactory.getLogger(TokenHelper.class);

	private String getClientToken(String clientID, String clienSecret, String appID, RestTemplate restTemplate,
			String tokenURL) {
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

}
