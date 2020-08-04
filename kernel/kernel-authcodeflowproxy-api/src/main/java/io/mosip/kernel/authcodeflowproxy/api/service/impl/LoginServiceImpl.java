package io.mosip.kernel.authcodeflowproxy.api.service.impl;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.Cookie;

import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.util.UriComponentsBuilder;

import io.mosip.kernel.authcodeflowproxy.api.service.LoginService;

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

	


}
