package io.mosip.kernel.authcodeflowproxy.api.controller;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;

import io.mosip.kernel.authcodeflowproxy.api.constants.Errors;
import io.mosip.kernel.authcodeflowproxy.api.dto.AccessTokenResponseDTO;
import io.mosip.kernel.authcodeflowproxy.api.dto.MosipUserDto;
import io.mosip.kernel.authcodeflowproxy.api.exception.ClientException;
import io.mosip.kernel.authcodeflowproxy.api.exception.ServiceException;
import io.mosip.kernel.authcodeflowproxy.api.service.LoginService;
import io.mosip.kernel.core.authmanager.model.AuthResponseDto;
import io.mosip.kernel.core.http.ResponseFilter;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.util.CryptoUtil;
import io.mosip.kernel.core.util.EmptyCheckUtils;

@RestController
public class LoginController {
	
	private final static Logger LOGGER= LoggerFactory.getLogger(LoginController.class);

	@Value("${auth.token.header:Authorization}")
	private String authTokenHeader;
	
	
	@Value("#{'${auth.allowed.urls}'.split(',')}")
	private List<String> allowedUrls;

	@Autowired
	private LoginService loginService;

	@GetMapping(value = "/login/{redirectURI}")
	public void login(@CookieValue(name = "state", required = false) String state,
			@PathVariable("redirectURI") String redirectURI,
			@RequestParam(name = "state", required = false) String stateParam, HttpServletResponse res)
			throws IOException {
		String stateValue = EmptyCheckUtils.isNullEmpty(state) ? stateParam : state;
		if (EmptyCheckUtils.isNullEmpty(stateValue)) {
			throw new ServiceException(Errors.STATE_NULL_EXCEPTION.getErrorCode(),
					Errors.STATE_NULL_EXCEPTION.getErrorMessage());
		}

		// there is no UUID.parse method till so using this as alternative
		try {
			if (!UUID.fromString(stateValue).toString().equals(stateValue)) {
				throw new ServiceException(Errors.STATE_NOT_UUID_EXCEPTION.getErrorCode(),
						Errors.STATE_NOT_UUID_EXCEPTION.getErrorMessage());
			}
		} catch (IllegalArgumentException exception) {
			throw new ServiceException(Errors.STATE_NOT_UUID_EXCEPTION.getErrorCode(),
					Errors.STATE_NOT_UUID_EXCEPTION.getErrorMessage());
		}
		
		String uri = loginService.login(redirectURI, stateValue);
		Cookie stateCookie = new Cookie("state", stateValue);
		stateCookie.setHttpOnly(true);
		stateCookie.setSecure(true);
		stateCookie.setPath("/");
		res.addCookie(stateCookie);
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
		String url = new String(Base64.decodeBase64(redirectURI.getBytes()));
		if(url.contains("#")) {
			url= url.split("#")[0];
		}
		if(!allowedUrls.contains(url)) {
			LOGGER.error("Url {} was not part of allowed url's",url);
			throw new ServiceException(Errors.ALLOWED_URL_EXCEPTION.getErrorCode(), Errors.ALLOWED_URL_EXCEPTION.getErrorMessage());
		}
		res.sendRedirect(url);	
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
	
	@ResponseFilter
	@GetMapping(value = "/logout/user")
	public void logoutUser(
			@CookieValue(value = "Authorization", required = false) String token,@RequestParam(name = "redirecturi", required = true) String redirectURI, HttpServletResponse res) throws IOException {
		redirectURI = new String(CryptoUtil.decodeURLSafeBase64(redirectURI));
		if(!allowedUrls.contains(redirectURI)) {
			LOGGER.error("Url {} was not part of allowed url's",redirectURI);
			throw new ServiceException(Errors.ALLOWED_URL_EXCEPTION.getErrorCode(), Errors.ALLOWED_URL_EXCEPTION.getErrorMessage());
		}
		String uri = loginService.logoutUser(token,redirectURI);
		res.sendRedirect(uri);
	}
	

}