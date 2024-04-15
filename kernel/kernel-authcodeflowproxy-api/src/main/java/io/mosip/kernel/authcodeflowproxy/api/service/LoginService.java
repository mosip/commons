package io.mosip.kernel.authcodeflowproxy.api.service;

import jakarta.servlet.http.Cookie;


import io.mosip.kernel.authcodeflowproxy.api.dto.AccessTokenResponseDTO;

import io.mosip.kernel.authcodeflowproxy.api.dto.MosipUserDto;
import io.mosip.kernel.core.authmanager.model.AuthResponseDto;

public interface LoginService {

	String login(String redirectURI, String state);

	Cookie createCookie(String authCookie);

	MosipUserDto valdiateToken(String authToken);


	AccessTokenResponseDTO loginRedirect(String state, String sessionState, String code, String stateCookie,
			String redirectURI);

	String logoutUser(String token, String redirectURI);


}
