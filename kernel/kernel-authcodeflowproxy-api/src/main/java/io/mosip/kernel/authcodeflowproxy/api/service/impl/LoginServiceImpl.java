package io.mosip.kernel.authcodeflowproxy.api.service.impl;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.Cookie;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.kernel.authcodeflowproxy.api.constants.Constants;
import io.mosip.kernel.authcodeflowproxy.api.constants.Errors;
import io.mosip.kernel.authcodeflowproxy.api.dto.AccessTokenResponse;
import io.mosip.kernel.authcodeflowproxy.api.dto.AccessTokenResponseDTO;
import io.mosip.kernel.authcodeflowproxy.api.dto.IAMErrorResponseDto;
import io.mosip.kernel.authcodeflowproxy.api.dto.MosipUserDto;
import io.mosip.kernel.authcodeflowproxy.api.exception.AuthRestException;
import io.mosip.kernel.authcodeflowproxy.api.exception.ClientException;
import io.mosip.kernel.authcodeflowproxy.api.exception.ServiceException;
import io.mosip.kernel.authcodeflowproxy.api.service.LoginService;
import io.mosip.kernel.core.exception.ExceptionUtils;
import io.mosip.kernel.core.exception.ServiceError;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.util.EmptyCheckUtils;

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

	@Value("${mosip.iam.module.login_flow.name:authorization_code}")
	private String loginFlowName;

	@Value("${mosip.iam.module.clientid}")
	private String clientID;

	@Value("${mosip.iam.module.clientsecret}")
	private String clientSecret;

	@Value("${mosip.iam.module.redirecturi}")
	private String redirectURI;

	@Value("${mosip.iam.module.login_flow.scope:cls}")
	private String scope;

	@Value("${mosip.iam.module.login_flow.response_type:code}")
	private String responseType;

	@Value("${mosip.iam.authorization_endpoint}")
	private String authorizationEndpoint;

	@Value("${mosip.iam.module.admin_realm_id}")
	private String realmID;

	@Value("${mosip.iam.token_endpoint}")
	private String tokenEndpoint;

	@Value("${auth.server.admin.validate.url}")
	private String validateUrl;
	
	
	@Value("${mosip.iam.post-logout-uri-param-key:post_logout_redirect_uri}")
	private String postLogoutRedirectURIParamKey;
	
	@Value("${mosip.iam.end-session-endpoint-path:/protocol/openid-connect/logout}")
	private String endSessionEndpointPath;
	

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private ObjectMapper objectMapper;

	@Override
	public String login(String redirectURI, String state) {
		Map<String, String> pathParam = new HashMap<>();
		pathParam.put("realmId", realmID);
		UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(authorizationEndpoint);
		uriComponentsBuilder.queryParam(Constants.CLIENT_ID, clientID);
		uriComponentsBuilder.queryParam(Constants.REDIRECT_URI, this.redirectURI + redirectURI);
		uriComponentsBuilder.queryParam(Constants.STATE, state);
		uriComponentsBuilder.queryParam(Constants.RESPONSE_TYPE, responseType);
		uriComponentsBuilder.queryParam(Constants.SCOPE, scope);

		return uriComponentsBuilder.buildAndExpand(pathParam).toString();

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

		HttpHeaders headers = new HttpHeaders();
		headers.add("Cookie", authTokenHeader + "=" + authToken);
		HttpEntity<String> requestEntity = new HttpEntity<>(headers);
		ResponseEntity<String> response = null;
		try {
			response = restTemplate.exchange(validateUrl, HttpMethod.GET, requestEntity, String.class);
		} catch (HttpClientErrorException | HttpServerErrorException e) {
			String responseBody = e.getResponseBodyAsString();
			List<ServiceError> validationErrorList = ExceptionUtils.getServiceErrorList(responseBody);

			if (!validationErrorList.isEmpty()) {
				throw new AuthRestException(validationErrorList, e.getStatusCode());
			} else {
				throw new ServiceException(Errors.REST_EXCEPTION.getErrorCode(), e.getResponseBodyAsString());
			}

		}
		String responseBody = response.getBody();
		List<ServiceError> validationErrorList = ExceptionUtils.getServiceErrorList(responseBody);

		if (!validationErrorList.isEmpty()) {
			throw new AuthRestException(validationErrorList, response.getStatusCode());
		}
		ResponseWrapper<?> responseObject;
		MosipUserDto mosipUserDto;
		try {
			responseObject = objectMapper.readValue(response.getBody(), ResponseWrapper.class);
			mosipUserDto = objectMapper.readValue(objectMapper.writeValueAsString(responseObject.getResponse()),
					MosipUserDto.class);
		} catch (IOException e) {
			throw new ServiceException(Errors.IO_EXCEPTION.getErrorCode(), Errors.IO_EXCEPTION.getErrorMessage());
		}
		return mosipUserDto;
	}

	@Override
	public AccessTokenResponseDTO loginRedirect(String state, String sessionState, String code, String stateCookie,
			String redirectURI) {
		// Compare states
		if (!stateCookie.equals(state)) {
			throw new ClientException(Errors.STATE_EXCEPTION.getErrorCode(), Errors.STATE_EXCEPTION.getErrorMessage());
		}

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
		map.add(Constants.GRANT_TYPE, loginFlowName);
		map.add(Constants.CLIENT_ID, clientID);
		map.add(Constants.CLIENT_SECRET, clientSecret);
		map.add(Constants.CODE, code);
		map.add(Constants.REDIRECT_URI, this.redirectURI + redirectURI);
		Map<String, String> pathParam = new HashMap<>();
		pathParam.put("realmId", realmID);
		UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(tokenEndpoint);
		HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(map, headers);
		ResponseEntity<String> responseEntity = null;
		try {
			responseEntity = restTemplate.exchange(uriBuilder.buildAndExpand(pathParam).toUriString(), HttpMethod.POST,
					entity, String.class);

		} catch (HttpClientErrorException | HttpServerErrorException e) {
			IAMErrorResponseDto keycloakErrorResponseDto = parseKeyClockErrorResponse(e);
			throw new ServiceException(Errors.ACESSTOKEN_EXCEPTION.getErrorCode(),
					Errors.ACESSTOKEN_EXCEPTION.getErrorMessage() + Constants.WHITESPACE
							+ keycloakErrorResponseDto.getError_description());
		}
		AccessTokenResponse accessTokenResponse = null;
		try {
			accessTokenResponse = objectMapper.readValue(responseEntity.getBody(), AccessTokenResponse.class);
		} catch (IOException exception) {
			throw new ServiceException(Errors.RESPONSE_PARSE_ERROR.getErrorCode(),
					Errors.RESPONSE_PARSE_ERROR.getErrorMessage() + Constants.WHITESPACE + exception.getMessage());
		}
		AccessTokenResponseDTO accessTokenResponseDTO = new AccessTokenResponseDTO();
		accessTokenResponseDTO.setAccessToken(accessTokenResponse.getAccess_token());
		accessTokenResponseDTO.setExpiresIn(accessTokenResponse.getExpires_in());
		return accessTokenResponseDTO;
	}

	private IAMErrorResponseDto parseKeyClockErrorResponse(HttpStatusCodeException exception) {
		IAMErrorResponseDto keycloakErrorResponseDto = null;
		try {
			keycloakErrorResponseDto = objectMapper.readValue(exception.getResponseBodyAsString(),
					IAMErrorResponseDto.class);

		} catch (IOException e) {
			throw new ServiceException(Errors.RESPONSE_PARSE_ERROR.getErrorCode(),
					Errors.RESPONSE_PARSE_ERROR.getErrorMessage() + Constants.WHITESPACE + e.getMessage());
		}
		return keycloakErrorResponseDto;
	}

	@Override
	public String logoutUser(String token,String redirectURI) {
		if (EmptyCheckUtils.isNullEmpty(token)) {
			throw new AuthenticationServiceException(Errors.INVALID_TOKEN.getErrorMessage());
		}
		String issuer = getissuer(token);
		StringBuilder urlBuilder = new StringBuilder().append(issuer).append(endSessionEndpointPath);
		UriComponentsBuilder uriComponentsBuilder;
		try {
			uriComponentsBuilder = UriComponentsBuilder.fromUriString(urlBuilder.toString())
					.queryParam(postLogoutRedirectURIParamKey, URLEncoder.encode(redirectURI, StandardCharsets.UTF_8.toString()));
		} catch (UnsupportedEncodingException e) {
			throw new ServiceException(Errors.UNSUPPORTED_ENCODING_EXCEPTION.getErrorCode(),
					Errors.UNSUPPORTED_ENCODING_EXCEPTION.getErrorMessage() + Constants.WHITESPACE + e.getMessage());
		}
		return uriComponentsBuilder.toUriString();
	}

	public String getissuer(String token) {
		DecodedJWT decodedJWT = JWT.decode(token);
		return decodedJWT.getClaim("iss").asString();
	}

}
