package io.mosip.kernel.loginhelper.api.service.impl;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.Cookie;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import io.mosip.kernel.loginhelper.api.service.LoginService;

public class LoginServiceImpl implements LoginService {
	
	@Value("${}")
	private String loginURL;
	
	@Value("${}")
	private String loginRedirectURL;
	
	
	@Autowired
	private RestTemplate restTemplate;

	@Override
	public String login(String redirectURI, String state) {
		UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(loginURL);
		Map<String, String> pathParam = new HashMap<>();
		pathParam.put("redirectURI", redirectURI);
		restTemplate.exchange(loginURL, method, requestEntity, responseType)
		return null;
	}

	@Override
	public Cookie loginRedirect(String state, String sessionState, String code, String stateCookie,
			String redirectURI) {
		// TODO Auto-generated method stub
		return null;
	}

}
