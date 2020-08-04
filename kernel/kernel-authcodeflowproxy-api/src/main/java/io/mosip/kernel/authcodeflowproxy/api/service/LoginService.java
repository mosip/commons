package io.mosip.kernel.authcodeflowproxy.api.service;

import javax.servlet.http.Cookie;

public interface LoginService {

	String login(String redirectURI, String state);

	Cookie createCookie(String authCookie);

}
