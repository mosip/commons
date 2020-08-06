package io.mosip.kernel.authcodeflowproxy.api.controller;

import java.io.IOException;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.mosip.kernel.authcodeflowproxy.api.constants.Errors;
import io.mosip.kernel.authcodeflowproxy.api.dto.AccessTokenResponseDTO;

import io.mosip.kernel.authcodeflowproxy.api.dto.MosipUserDto;
import io.mosip.kernel.authcodeflowproxy.api.exception.ClientException;
import io.mosip.kernel.authcodeflowproxy.api.service.LoginService;
import io.mosip.kernel.core.http.ResponseFilter;
import io.mosip.kernel.core.http.ResponseWrapper;

@RestController
public class LoginController {

	@Value("${auth.token.header:Authorization}")
	private String authTokenHeader;

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
		AccessTokenResponseDTO jwtResponseDTO = loginService.loginRedirect(state, sessionState, code, stateCookie,
				redirectURI);
		Cookie cookie = loginService.createCookie(jwtResponseDTO.getAccessToken());
		res.addCookie(cookie);
		res.setStatus(302);
		String uri = new String(Base64.decodeBase64(redirectURI.getBytes()));
		res.sendRedirect(uri);	
		}


	@ResponseFilter
	@GetMapping(value = "/authorize/admin/validateToken")
	public ResponseWrapper<MosipUserDto> validateAdminToken(HttpServletRequest request, HttpServletResponse res) {
		String authToken = null;
		Cookie[] cookies = request.getCookies();
		if (cookies == null) {
			throw new ClientException(Errors.COOKIE_NOTPRESENT_ERROR.getErrorCode(),
					Errors.COOKIE_NOTPRESENT_ERROR.getErrorMessage());
		}
		MosipUserDto mosipUserDto = null;

		for (Cookie cookie : cookies) {
			if (cookie.getName().contains(authTokenHeader)) {
				authToken = cookie.getValue();
			}
		}
		if (authToken == null) {
			throw new ClientException(Errors.TOKEN_NOTPRESENT_ERROR.getErrorCode(),
					Errors.TOKEN_NOTPRESENT_ERROR.getErrorMessage());
		}

		mosipUserDto = loginService.valdiateToken(authToken);
		Cookie cookie = loginService.createCookie(authToken);
		res.addCookie(cookie);
		ResponseWrapper<MosipUserDto> responseWrapper = new ResponseWrapper<>();
		responseWrapper.setResponse(mosipUserDto);
		return responseWrapper;
	}
}