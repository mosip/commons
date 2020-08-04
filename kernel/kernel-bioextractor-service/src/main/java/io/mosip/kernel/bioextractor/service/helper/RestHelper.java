package io.mosip.kernel.bioextractor.service.helper;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import io.mosip.kernel.auth.adapter.model.AuthUserDetails;
import io.mosip.kernel.bioextractor.config.constant.BiometricExtractionErrorConstants;
import io.mosip.kernel.bioextractor.exception.BiometricExtractionException;
import io.mosip.kernel.core.http.RequestWrapper;
import io.mosip.kernel.core.util.DateUtils;

@Component
public final class RestHelper {

	private static final String AUTHORIZATION = "Authorization";
	private static final String COOKIE = "Cookie";
	private static final String AUTHORIZATION_PREFIX = AUTHORIZATION + "=";
	@Autowired
	private RestTemplate restTemplate;

	private Optional<String> getToken() {
		return Optional.ofNullable(SecurityContextHolder.getContext())
											 .map(SecurityContext::getAuthentication)
											 .map(Authentication::getPrincipal)
											 .filter(auth -> auth instanceof AuthUserDetails)
											 .map(auth -> (AuthUserDetails)auth)
											 .map(AuthUserDetails::getToken);
												
	}

	public Map<String, String> createTokenHeaderMap() {
		Map<String, String> headersMap = new LinkedHashMap<>();
		Optional<String> token = getToken();
		if(token.isPresent()) {
			headersMap.put(COOKIE, AUTHORIZATION_PREFIX + token.get());
		}
		return headersMap;
	}

	public <R> R doGet(String url, Class<R> clazz, Map<String, String> headersMap,
			BiometricExtractionErrorConstants errConst) throws BiometricExtractionException {
		HttpHeaders headers = new HttpHeaders();
		HttpEntity<String> entity = new HttpEntity<>(headers);
		if (headersMap != null) {
			headersMap.forEach((key, val) -> headers.add(key, val));
		}
		try {
			ResponseEntity<R> responseEntity = restTemplate.exchange(url, HttpMethod.GET, entity, clazz);
			return responseEntity.getBody();
		} catch (RestClientException e) {
			throw new BiometricExtractionException(errConst, e);
		}
	}

	public <R, T> T doPost(String url, R requestWrapper,
			Map<String, String> headersMap, BiometricExtractionErrorConstants errConst, Class<T> responseClass)
			throws BiometricExtractionException {
		HttpHeaders headers = new HttpHeaders();
		if (headersMap != null) {
			headersMap.forEach(headers::add);
		}

		HttpEntity<R> entity = new HttpEntity<>(requestWrapper, headers);
		try {
			ResponseEntity<T> responseEntity = restTemplate.exchange(url, HttpMethod.POST, entity, responseClass);
			return responseEntity.getBody();
		} catch (RestClientException e) {
			throw new BiometricExtractionException(errConst, e);
		}
	}

	public <T> RequestWrapper<T> createRequtestWrapper() {
		RequestWrapper<T> requestWrapper = new RequestWrapper<>();
		requestWrapper.setRequesttime(DateUtils.getUTCCurrentDateTime());
		return requestWrapper;
	}

}
