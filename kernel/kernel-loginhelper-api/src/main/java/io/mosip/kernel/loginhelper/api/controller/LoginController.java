package io.mosip.kernel.loginhelper.api.controller;

import java.io.IOException;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.mosip.kernel.loginhelper.api.service.LoginService;


@RestController
public class LoginController {
	
	@Autowired
	private LoginService loginService; 
		
	@GetMapping(value = "/login/{redirectURI}")
	public void login(@CookieValue("state") String state, @PathVariable("redirectURI") String redirectURI,
			HttpServletResponse res) throws IOException {
		String uri = loginService.login(redirectURI, state);
		res.setStatus(302);
		res.sendRedirect(uri);
	}

	@GetMapping(value = "/login-redirect/{redirectURI}")
	public void loginRedirect(@PathVariable("redirectURI") String redirectURI, @RequestParam("state") String state,
			@RequestParam("session_state") String sessionState, @RequestParam("code") String code,
			@CookieValue("state") String stateCookie, HttpServletResponse res) throws IOException {
		Cookie cookie = loginService.loginRedirect(state, sessionState, code, stateCookie,
				redirectURI);
		String uri = new String(Base64.decodeBase64(redirectURI.getBytes()));
		res.addCookie(cookie);
		res.setStatus(302);
		res.sendRedirect(uri);
	}
}