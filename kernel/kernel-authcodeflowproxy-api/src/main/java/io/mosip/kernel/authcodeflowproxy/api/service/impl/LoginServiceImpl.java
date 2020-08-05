package io.mosip.kernel.authcodeflowproxy.api.service.impl;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.Cookie;

import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.kernel.authcodeflowproxy.api.constants.Errors;
import io.mosip.kernel.authcodeflowproxy.api.dto.MosipUserDto;
import io.mosip.kernel.authcodeflowproxy.api.exception.AuthRestException;
import io.mosip.kernel.authcodeflowproxy.api.exception.ServiceException;
import io.mosip.kernel.authcodeflowproxy.api.service.LoginService;
import io.mosip.kernel.core.exception.ExceptionUtils;
import io.mosip.kernel.core.exception.ServiceError;
import io.mosip.kernel.core.http.ResponseWrapper;

@Service
public class LoginServiceImpl implements LoginService {
	
	@Value("${mosip.kernel.auth-code-url-splitter:#URISPLITTER#}")
	private String urlSplitter;
	
	@Value("${mosip.security.secure-cookie:false}")
	private boolean isSecureCookie;
	
	@Value("${auth.token.header:Authorization}")
	private String authTokenHeader;
	
	@Value("${auth.jwt.expiry:1800000}")
	private int authTokenExpiry;
		
	@Value("${mosip.kernel.auth.auth-code-flow-login-url}")
	private String authServiceLoginURL;
	
	@Value("${mosip.module.auth.auth-code-flow-login-redirect-url}")
	private String moduleRedirectURL;
	
	@Value("${auth.server.admin.validate.url}")
	private String validateUrl;
	
	@Autowired
	private RestTemplate restTemplate;
	
	@Autowired ObjectMapper objectMapper;
	

	@Override
	public String login(String redirectURI, String state) {
		UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(authServiceLoginURL);
		Map<String, String> pathParam = new HashMap<>();
		pathParam.put("redirectURI", redirectURI+urlSplitter+Base64.encodeBase64String(moduleRedirectURL.getBytes()));
		return uriComponentsBuilder.buildAndExpand(pathParam).toUriString();
	}

	@Override
	public Cookie createCookie(String authCookie) {
		final Cookie cookie = new Cookie(authTokenHeader, authCookie);
		cookie.setMaxAge(authTokenExpiry);
		cookie.setHttpOnly(true);
		cookie.setSecure(isSecureCookie);
		cookie.setPath("/");
		return cookie;
	}

	@Override
	public MosipUserDto valdiateToken(String authToken) {
		HttpHeaders headers= new HttpHeaders();
		headers.add("Cookie", authTokenHeader+"="+authToken);
		HttpEntity<String> requestEntity = new HttpEntity<>(headers);
		HttpEntity<String> response = restTemplate.exchange(validateUrl, HttpMethod.GET, requestEntity, String.class);
		if (response == null) {
			throw new ServiceException(Errors.CANNOT_CONNECT_TO_AUTH_SERVICE.getErrorCode(),
					Errors.CANNOT_CONNECT_TO_AUTH_SERVICE.getErrorMessage());
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
			throw new ServiceException(Errors.IO_EXCEPTION.getErrorCode(),
					Errors.IO_EXCEPTION.getErrorMessage());
		}
		return mosipUserDto;
	}

	


}
