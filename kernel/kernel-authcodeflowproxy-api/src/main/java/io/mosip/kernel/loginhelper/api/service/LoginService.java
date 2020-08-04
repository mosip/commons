package io.mosip.kernel.loginhelper.api.service;

import javax.servlet.http.Cookie;

public interface LoginService {

	String login(String redirectURI, String state);

	Cookie createCookie(String authCookie);

}
