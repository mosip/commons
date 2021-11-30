package io.mosip.kernel.auth.defaultimpl.intercepter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

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
import io.mosip.kernel.auth.defaultimpl.dto.AccessTokenResponse;
import io.mosip.kernel.auth.defaultimpl.util.MemoryCache;
import io.mosip.kernel.auth.defaultimpl.util.TokenValidator;


/**
 * RestInterceptor for getting admin token
 * 
 * @author Urvil Joshi
 * @author Srinivasan
 *
 */
//@Component
public class RestInterceptor implements ClientHttpRequestInterceptor {

	private static final Logger LOGGER= LoggerFactory.getLogger(RestInterceptor.class);
	
	//@Autowired
	private MemoryCache<String, AccessTokenResponse> memoryCache;

	//@Autowired
	private TokenValidator tokenValidator;

	//@Qualifier("authRestTemplate")
	//@Autowired
	private RestTemplate restTemplate;
	
	public RestInterceptor(MemoryCache<String, AccessTokenResponse> memoryCache,TokenValidator tokenValidator,RestTemplate restTemplate) {
		this.memoryCache= memoryCache;
		this.tokenValidator= tokenValidator;
		this.restTemplate= restTemplate;
		
		
	}
	
	

	@Value("${mosip.iam.open-id-url}")
	private String keycloakOpenIdUrl;

	@Value("${mosip.iam.master.realm-id}")
	private String realmId;

	@Value("${mosip.keycloak.admin.client.id}")
	private String adminClientID;

	@Value("${mosip.keycloak.admin.user.id}")
	private String adminUserName;

	@Value("${mosip.keycloak.admin.secret.key}")
	private String adminSecret;


	@Override
	public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
			throws IOException {
		AccessTokenResponse accessTokenResponse = null;
		if ((accessTokenResponse = memoryCache.get("adminToken")) != null) {
			boolean accessTokenExpired = tokenValidator.isExpired(accessTokenResponse.getAccess_token());
			boolean refreshTokenExpired = tokenValidator.isExpired(accessTokenResponse.getRefresh_token());
			LOGGER.info("access token expired: " + accessTokenExpired + " ,refresh token expired: " + refreshTokenExpired);
			if (refreshTokenExpired){
				accessTokenResponse = getAdminToken(false, null);
			} else if (accessTokenExpired) {
				accessTokenResponse = getAdminToken(true, accessTokenResponse.getRefresh_token());
			}
			
			/* if (accessTokenExpired && refreshTokenExpired) {
				accessTokenResponse = getAdminToken(false, null);
			} else if (accessTokenExpired) {
				accessTokenResponse = getAdminToken(true, accessTokenResponse.getRefresh_token());
			} else if (refreshTokenExpired) {
				accessTokenResponse = getAdminToken(false, null);
			} */
		} else {
			accessTokenResponse = getAdminToken(false, null);
		}
		memoryCache.put("adminToken", accessTokenResponse);
		request.getHeaders().add("Authorization", "Bearer " + accessTokenResponse.getAccess_token());
		return execution.execute(request, body);
	}

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
		ResponseEntity<AccessTokenResponse> response=null;
		try {
		 response = restTemplate.postForEntity(
				uriComponentsBuilder.buildAndExpand(pathParams).toUriString(), request, AccessTokenResponse.class);
		}catch(HttpServerErrorException | HttpClientErrorException ex) {
			LOGGER.error(ex.getMessage());
		}
		
		return response.getBody();
	}

	private MultiValueMap<String, String> getAdminValueMap() {
		MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
		map.add(AuthConstant.GRANT_TYPE, AuthConstant.PASSWORDCONSTANT);
		map.add(AuthConstant.USER_NAME, adminUserName);
		map.add(AuthConstant.PASSWORDCONSTANT, adminSecret);
		map.add(AuthConstant.CLIENT_ID, adminClientID);
		return map;
	}

	private MultiValueMap<String, String> getAdminValueMap(String refreshToken) {
		MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
		map.add(AuthConstant.GRANT_TYPE, AuthConstant.REFRESH_TOKEN);
		map.add(AuthConstant.REFRESH_TOKEN, refreshToken);
		map.add(AuthConstant.CLIENT_ID, adminClientID);
		return map;
	}
}