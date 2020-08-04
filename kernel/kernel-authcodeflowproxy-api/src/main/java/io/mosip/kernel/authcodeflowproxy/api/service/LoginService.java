package io.mosip.kernel.authcodeflowproxy.api.service;

import javax.servlet.http.Cookie;


import io.mosip.kernel.authcodeflowproxy.api.dto.MosipUserDto;

public interface LoginService {

	String login(String redirectURI, String state);

	Cookie createCookie(String authCookie);
  
	MosipUserDto valdiateToken(String authToken);

}
