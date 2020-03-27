package io.mosip.kernel.auth.service.impl;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.web.authentication.www.NonceExpiredException;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.kernel.auth.adapter.exception.AuthNException;
import io.mosip.kernel.auth.config.MosipEnvironment;
import io.mosip.kernel.auth.constant.AuthConstant;
import io.mosip.kernel.auth.constant.AuthErrorCode;
import io.mosip.kernel.auth.constant.KeycloakConstants;
import io.mosip.kernel.auth.dto.AccessTokenResponse;
import io.mosip.kernel.auth.dto.AccessTokenResponseDTO;
import io.mosip.kernel.auth.dto.AuthNResponse;
import io.mosip.kernel.auth.dto.AuthNResponseDto;
import io.mosip.kernel.auth.dto.AuthResponseDto;
import io.mosip.kernel.auth.dto.AuthToken;
import io.mosip.kernel.auth.dto.AuthZResponseDto;
import io.mosip.kernel.auth.dto.ClientSecret;
import io.mosip.kernel.auth.dto.KeycloakErrorResponseDto;
import io.mosip.kernel.auth.dto.LoginUser;
import io.mosip.kernel.auth.dto.MosipUserDto;
import io.mosip.kernel.auth.dto.MosipUserListDto;
import io.mosip.kernel.auth.dto.MosipUserSaltListDto;
import io.mosip.kernel.auth.dto.MosipUserTokenDto;
import io.mosip.kernel.auth.dto.PasswordDto;
import io.mosip.kernel.auth.dto.RIdDto;
import io.mosip.kernel.auth.dto.RealmAccessDto;
import io.mosip.kernel.auth.dto.RefreshTokenRequest;
import io.mosip.kernel.auth.dto.RefreshTokenResponse;
import io.mosip.kernel.auth.dto.RolesListDto;
import io.mosip.kernel.auth.dto.UserDetailsResponseDto;
import io.mosip.kernel.auth.dto.UserNameDto;
import io.mosip.kernel.auth.dto.UserOtp;
import io.mosip.kernel.auth.dto.UserPasswordRequestDto;
import io.mosip.kernel.auth.dto.UserPasswordResponseDto;
import io.mosip.kernel.auth.dto.UserRegistrationRequestDto;
import io.mosip.kernel.auth.dto.UserRoleDto;
import io.mosip.kernel.auth.dto.ValidationResponseDto;
import io.mosip.kernel.auth.dto.otp.OtpUser;
import io.mosip.kernel.auth.exception.AuthManagerException;
import io.mosip.kernel.auth.exception.LoginException;
import io.mosip.kernel.auth.repository.UserStoreFactory;
import io.mosip.kernel.auth.repository.impl.KeycloakImpl;
import io.mosip.kernel.auth.service.AuthService;
import io.mosip.kernel.auth.service.OTPService;
import io.mosip.kernel.auth.service.TokenService;
import io.mosip.kernel.auth.service.UinService;
import io.mosip.kernel.auth.util.TokenGenerator;
import io.mosip.kernel.auth.util.TokenValidator;
import io.mosip.kernel.core.util.EmptyCheckUtils;

/**
 * Auth Service for Authentication and Authorization
 * 
 * @author Ramadurai Pandian
 * @author Urvil Joshi
 * @author Srinivasan
 *
 */
@Profile("!local")
@Service
public class AuthServiceImpl implements AuthService {

	private static final Logger LOGGER = LoggerFactory.getLogger(AuthServiceImpl.class);

	private static final String CLIENTID_AND_TOKEN_COMBINATION_HAD_BEEN_VALIDATED_SUCCESSFULLY = "Clientid and Token combination had been validated successfully";

	private static final String LOG_OUT_FAILED = "log out failed";

	private static final String FAILED = "Failed";

	private static final String SUCCESS = "Success";

	private static final String SUCCESSFULLY_LOGGED_OUT = "successfully loggedout";

	@Value("${mosip.kernel.open-id-url}")
	private String keycloakOpenIdUrl;

	@Value("${mosip.admin_realm_id}")
	private String realmId;

	@Autowired
	UserStoreFactory userStoreFactory;

	@Autowired
	KeycloakImpl keycloakImpl;

	@Autowired
	TokenGenerator tokenGenerator;

	@Autowired
	TokenValidator tokenValidator;

	@Autowired
	TokenService customTokenServices;

	@Autowired
	OTPService oTPService;

	@Autowired
	UinService uinService;

	@Autowired
	MosipEnvironment mosipEnvironment;

	@Autowired
	ObjectMapper objectmapper;

	@Autowired
	private RestTemplate restTemplate;

	@Value("${mosip.kernel.open-id-url}")
	private String openIdUrl;

	@Value("${mosip.admin.login_flow.name}")
	private String loginFlowName;

	@Value("${mosip.admin.clientid}")
	private String clientID;

	@Value("${mosip.admin.clientsecret}")
	private String clientSecret;

	@Value("${mosip.admin.redirecturi}")
	private String redirectURI;

	@Value("${mosip.admin.login_flow.scope}")
	private String scope;

	@Value("${mosip.admin.login_flow.response_type}")
	private String responseType;

	@Value("${mosip.keycloak.authorization_endpoint}")
	private String authorizationEndpoint;

	@Value("${mosip.keycloak.token_endpoint}")
	private String tokenEndpoint;

	@Value("${mosip.admin_realm_id}")
	private String realmID;

	@Value("${mosip.kernel.prereg.realm-id}")
	private String preRegRealmID;

	@Qualifier("authRestTemplate")
	@Autowired
	private RestTemplate authRestTemplate;

	/**
	 * Method used for validating Auth token
	 * 
	 * @param token
	 *            token
	 * 
	 * @return mosipUserDtoToken is of type {@link MosipUserTokenDto}
	 * 
	 * @throws Exception
	 *             exception
	 * 
	 */

	@Override
	public MosipUserTokenDto validateToken(String token) throws Exception {
		MosipUserTokenDto mosipUserDtoToken = tokenValidator.validateToken(token);
		AuthToken authToken = customTokenServices.getTokenDetails(token);
		if (authToken == null) {
			throw new AuthManagerException(AuthErrorCode.INVALID_TOKEN.getErrorCode(),
					AuthErrorCode.INVALID_TOKEN.getErrorMessage());
		}
		if (mosipUserDtoToken != null /* && (currentTime < authToken.getExpirationTime()) */) {
			return mosipUserDtoToken;
		} else {
			throw new NonceExpiredException(AuthConstant.AUTH_TOKEN_EXPIRED_MESSAGE);
		}
	}

	/**
	 * Method used for Authenticating User based on username and password
	 * 
	 * @param loginUser
	 *            is of type {@link LoginUser}
	 * 
	 * @return authNResponseDto is of type {@link AuthNResponseDto}
	 * 
	 * @throws Exception
	 *             exception
	 * 
	 */

	@Override
	public AuthNResponseDto authenticateUser(LoginUser loginUser) throws Exception {
		AuthNResponseDto authNResponseDto = null;
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		MultiValueMap<String, String> tokenRequestBody = null;
		ResponseEntity<AccessTokenResponse> response = null;
		Map<String, String> pathParams = new HashMap<>();
		pathParams.put(AuthConstant.REALM_ID, realmId);
		UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromUriString(keycloakOpenIdUrl + "/token");
		tokenRequestBody = getPasswordValueMap(clientID, clientSecret, loginUser.getUserName(),
				loginUser.getPassword());
		HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(tokenRequestBody, headers);
		try {
			response = authRestTemplate.postForEntity(uriComponentsBuilder.buildAndExpand(pathParams).toUriString(),
					request, AccessTokenResponse.class);
		} catch (HttpClientErrorException | HttpServerErrorException ex) {
			if (ex.getRawStatusCode() == 401) {
				throw new AuthManagerException(AuthErrorCode.INVALID_CREDENTIALS.getErrorCode(),
						AuthErrorCode.INVALID_CREDENTIALS.getErrorMessage());
			} else if (ex.getRawStatusCode() == 400) {
				throw new AuthManagerException(AuthErrorCode.REQUEST_VALIDATION_ERROR.getErrorCode(),
						AuthErrorCode.REQUEST_VALIDATION_ERROR.getErrorMessage());
			}

			throw new AuthManagerException(AuthErrorCode.SERVER_ERROR.getErrorCode(),
					AuthErrorCode.SERVER_ERROR.getErrorCode());
		}
		AccessTokenResponse accessTokenResponse = response.getBody();
		authNResponseDto = new AuthNResponseDto();
		authNResponseDto.setToken(accessTokenResponse.getAccess_token());
		authNResponseDto.setRefreshToken(accessTokenResponse.getRefresh_token());
		authNResponseDto.setExpiryTime(Long.parseLong(accessTokenResponse.getExpires_in()));
		authNResponseDto.setStatus(AuthConstant.SUCCESS_STATUS);
		authNResponseDto.setMessage(AuthConstant.USERPWD_SUCCESS_MESSAGE);
		return authNResponseDto;
	}

	/**
	 * Method used for sending OTP
	 * 
	 * @param otpUser
	 *            is of type {@link OtpUser}
	 * 
	 * @return authNResponseDto is of type {@link AuthNResponseDto}
	 * 
	 * @throws Exception
	 *             exception
	 * 
	 */

	@Override
	public AuthNResponseDto authenticateWithOtp(OtpUser otpUser) throws Exception {
		AuthNResponseDto authNResponseDto = null;

		MosipUserDto mosipUser = null;
		otpUser.getOtpChannel().replaceAll(String::toLowerCase);
		otpUser.setAppId(otpUser.getAppId().toLowerCase());
		otpUser.setOtpChannel(otpUser.getOtpChannel());
		if (AuthConstant.APPTYPE_UIN.equals(otpUser.getUseridtype())) {
			mosipUser = uinService.getDetailsFromUin(otpUser);
			authNResponseDto = oTPService.sendOTPForUin(mosipUser, otpUser, "ida");
			authNResponseDto.setStatus(authNResponseDto.getStatus());
			authNResponseDto.setMessage(authNResponseDto.getMessage());
		} else if (AuthConstant.APPTYPE_USERID.equals(otpUser.getUseridtype())) {
			UserRegistrationRequestDto userCreationRequestDto = new UserRegistrationRequestDto();
			userCreationRequestDto.setUserName(otpUser.getUserId());
			userCreationRequestDto.setAppId(otpUser.getAppId());
			mosipUser = registerUser(userCreationRequestDto);
			authNResponseDto = oTPService.sendOTP(mosipUser, otpUser);
			authNResponseDto.setStatus(authNResponseDto.getStatus());
			authNResponseDto.setMessage(authNResponseDto.getMessage());
		} else {
			throw new AuthManagerException(String.valueOf(HttpStatus.UNAUTHORIZED.value()), "Invalid User Id type");
		}
		return authNResponseDto;
	}

	/**
	 * Method used for Authenticating User based with username and OTP
	 * 
	 * @param userOtp
	 *            is of type {@link UserOtp}
	 * 
	 * @return authNResponseDto is of type {@link AuthNResponseDto}
	 * 
	 * @throws Exception
	 *             exception
	 * 
	 */

	@Override
	public AuthNResponseDto authenticateUserWithOtp(UserOtp userOtp) throws Exception {
		AuthNResponseDto authNResponseDto = new AuthNResponseDto();
		MosipUserTokenDto mosipToken = null;
		MosipUserDto mosipUser = null;
		String realm = realmId;
		if (userOtp.getAppId().equalsIgnoreCase(AuthConstant.PRE_REGISTRATION)) {
			realm = userOtp.getAppId();
		}

		if (keycloakImpl.isUserAlreadyPresent(userOtp.getUserId(), realm)) {
			mosipUser = new MosipUserDto();
			mosipUser.setUserId(userOtp.getUserId());
		}
		if (mosipUser == null && AuthConstant.IDA.toLowerCase().equals(userOtp.getAppId().toLowerCase())) {
			mosipUser = uinService.getDetailsForValidateOtp(userOtp.getUserId());
		}
		if (mosipUser != null) {
			mosipToken = oTPService.validateOTP(mosipUser, userOtp.getOtp(), userOtp.getAppId());
		} else {
			throw new AuthManagerException(AuthErrorCode.USER_VALIDATION_ERROR.getErrorCode(),
					AuthErrorCode.USER_VALIDATION_ERROR.getErrorMessage());
		}
		if (mosipToken != null && mosipToken.getMosipUserDto() != null) {
			authNResponseDto.setMessage(mosipToken.getMessage());
			authNResponseDto.setStatus(mosipToken.getStatus());
			authNResponseDto.setToken(mosipToken.getToken());
			authNResponseDto.setExpiryTime(mosipToken.getExpTime());
			authNResponseDto.setRefreshToken(mosipToken.getRefreshToken());
			authNResponseDto.setUserId(mosipToken.getMosipUserDto().getUserId());
		} else {
			authNResponseDto.setMessage(mosipToken.getMessage());
			authNResponseDto.setStatus(mosipToken.getStatus());
		}
		return authNResponseDto;
	}

	/**
	 * Method used for Authenticating User based with secretkey and password
	 * 
	 * @param clientSecret
	 *            is of type {@link ClientSecret}
	 * 
	 * @return authNResponseDto is of type {@link AuthNResponseDto}
	 * 
	 * @throws Exception
	 *             exception
	 * 
	 */

	@Override
	public AuthNResponseDto authenticateWithSecretKey(ClientSecret clientSecret) throws Exception {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		MultiValueMap<String, String> tokenRequestBody = null;
		Map<String, String> pathParams = new HashMap<>();
		pathParams.put(AuthConstant.REALM_ID, realmId);
		UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromUriString(keycloakOpenIdUrl + "/token");
		tokenRequestBody = getClientSecretValueMap(clientSecret.getClientId(), clientSecret.getSecretKey());
		HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(tokenRequestBody, headers);
		ResponseEntity<AccessTokenResponse> response = authRestTemplate.postForEntity(
				uriComponentsBuilder.buildAndExpand(pathParams).toUriString(), request, AccessTokenResponse.class);
		AccessTokenResponse accessTokenResponse = response.getBody();
		AuthNResponseDto authNResponseDto = new AuthNResponseDto();
		authNResponseDto.setToken(accessTokenResponse.getAccess_token());
		authNResponseDto.setRefreshToken(accessTokenResponse.getRefresh_token());
		authNResponseDto.setExpiryTime(Long.parseLong(accessTokenResponse.getExpires_in()));
		authNResponseDto.setStatus(SUCCESS);
		authNResponseDto.setMessage(CLIENTID_AND_TOKEN_COMBINATION_HAD_BEEN_VALIDATED_SUCCESSFULLY);
		return authNResponseDto;
	}

	/**
	 * Method used for generating refresh token
	 * 
	 * @param existingToken
	 *            existing token
	 * 
	 * @return mosipUserDtoToken is of type {@link MosipUserTokenDto}
	 * 
	 * @throws Exception
	 *             exception
	 * 
	 */

	@Override
	public RefreshTokenResponse refreshToken(String appID, String refreshToken, RefreshTokenRequest refreshTokenRequest)
			throws Exception {
		MultiValueMap<String, String> tokenRequestBody = new LinkedMultiValueMap<>();
		tokenRequestBody.add(AuthConstant.GRANT_TYPE, AuthConstant.REFRESH_TOKEN);
		tokenRequestBody.add(AuthConstant.REFRESH_TOKEN, refreshToken);
		tokenRequestBody.add(AuthConstant.CLIENT_ID, refreshTokenRequest.getClientID());
		tokenRequestBody.add(AuthConstant.CLIENT_SECRET, refreshTokenRequest.getClientID());

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		Map<String, String> pathParams = new HashMap<>();

		if (appID.equalsIgnoreCase("preregistration")) {
			pathParams.put(AuthConstant.REALM_ID, preRegRealmID);
		} else {
			pathParams.put(AuthConstant.REALM_ID, realmId);
		}
		UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromUriString(keycloakOpenIdUrl + "/token");

		HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(tokenRequestBody, headers);
		ResponseEntity<AccessTokenResponse> response = null;
		try {
			response = restTemplate.postForEntity(uriComponentsBuilder.buildAndExpand(pathParams).toUriString(),
					request, AccessTokenResponse.class);
		} catch (HttpServerErrorException | HttpClientErrorException ex) {
			LOGGER.error(ex.getMessage());
		}
		Objects.requireNonNull(response);
		AccessTokenResponse accessTokenResponse = response.getBody();
		AuthNResponse authNResponse = new AuthNResponse("SUCCESS", "Access token refreshed");
		return new RefreshTokenResponse(authNResponse, accessTokenResponse.getAccess_token(),
				accessTokenResponse.getRefresh_token(), accessTokenResponse.getExpires_in(),
				accessTokenResponse.getExpires_in());
	}

	/**
	 * Method used for invalidate token
	 * 
	 * @param token
	 *            token
	 * 
	 * @return authNResponse is of type {@link AuthNResponse}
	 * 
	 * @throws Exception
	 *             exception
	 * 
	 */

	@Override
	public AuthNResponse invalidateToken(String token) throws Exception {
		AuthNResponse authNResponse = null;
		customTokenServices.revokeToken(token);
		authNResponse = new AuthNResponse();
		authNResponse.setStatus(AuthConstant.SUCCESS_STATUS);
		authNResponse.setMessage(AuthConstant.TOKEN_INVALID_MESSAGE);
		return authNResponse;
	}

	@Override
	public RolesListDto getAllRoles(String appId) {
		RolesListDto rolesListDto = keycloakImpl.getAllRoles();
		return rolesListDto;
	}

	@Override
	public MosipUserListDto getListOfUsersDetails(List<String> userDetails, String appId) throws Exception {
		MosipUserListDto mosipUserListDto = keycloakImpl.getListOfUsersDetails(userDetails);
		return mosipUserListDto;
	}

	@Override
	public MosipUserSaltListDto getAllUserDetailsWithSalt(List<String> userDetails, String appId) throws Exception {
		return keycloakImpl.getAllUserDetailsWithSalt(userDetails);
	}

	@Override
	public RIdDto getRidBasedOnUid(String userId, String appId) throws Exception {
		return keycloakImpl.getRidFromUserId(userId);

	}

	@Deprecated
	@Override
	public AuthZResponseDto unBlockUser(String userId, String appId) throws Exception {
		return userStoreFactory.getDataStoreBasedOnApp(appId).unBlockAccount(userId);
	}
	
	@Deprecated
	@Override
	public AuthZResponseDto changePassword(String appId, PasswordDto passwordDto) throws Exception {
		return userStoreFactory.getDataStoreBasedOnApp(appId).changePassword(passwordDto);
	}

	@Deprecated
	@Override
	public AuthZResponseDto resetPassword(String appId, PasswordDto passwordDto) throws Exception {
		return userStoreFactory.getDataStoreBasedOnApp(appId).resetPassword(passwordDto);
	}

	@Deprecated
	@Override
	public UserNameDto getUserNameBasedOnMobileNumber(String appId, String mobileNumber) throws Exception {
		return userStoreFactory.getDataStoreBasedOnApp("registrationclient")
				.getUserNameBasedOnMobileNumber(mobileNumber);

	}

	@Override
	public MosipUserDto registerUser(UserRegistrationRequestDto userCreationRequestDto) {
		return keycloakImpl.registerUser(userCreationRequestDto);
	}

	@Deprecated
	@Override
	public UserPasswordResponseDto addUserPassword(UserPasswordRequestDto userPasswordRequestDto) {
		return userStoreFactory.getDataStoreBasedOnApp(userPasswordRequestDto.getAppId())
				.addPassword(userPasswordRequestDto);
	}

	@Override
	public UserRoleDto getUserRole(String appId, String userId) throws Exception {
		MosipUserDto mosipuser = null;
		mosipuser = userStoreFactory.getDataStoreBasedOnApp(appId).getUserRoleByUserId(userId);
		UserRoleDto userRole = new UserRoleDto();
		userRole.setUserId(mosipuser.getUserId());
		userRole.setRole(mosipuser.getRole());
		return userRole;
	}

	@Deprecated
	@Override
	public MosipUserDto getUserDetailBasedonMobileNumber(String appId, String mobileNumber) throws Exception {

		return userStoreFactory.getDataStoreBasedOnApp(appId).getUserDetailBasedonMobileNumber(mobileNumber);
	}

	@Deprecated
	@Override
	public ValidationResponseDto validateUserName(String appId, String userName) {
		return userStoreFactory.getDataStoreBasedOnApp(appId).validateUserName(userName);
	}

	
	@Override
	public UserDetailsResponseDto getUserDetailBasedOnUserId(String appId, List<String> userIds) {
		return userStoreFactory.getDataStoreBasedOnApp(appId).getUserDetailBasedOnUid(userIds);
	}

	@Override
	public MosipUserDto valdiateToken(String token) {
		Map<String, String> pathparams = new HashMap<>();
		if (EmptyCheckUtils.isNullEmpty(token)) {
			throw new AuthenticationServiceException(AuthErrorCode.INVALID_TOKEN.getErrorMessage());
		}
		String issuer = tokenValidator.getissuer(token);
		ResponseEntity<String> response = null;
		MosipUserDto mosipUserDto = null;
		System.out.println("validate token url " + openIdUrl);
		StringBuilder urlBuilder = new StringBuilder().append(issuer).append("/protocol/openid-connect/userinfo");
		UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromUriString(urlBuilder.toString());
		HttpHeaders headers = new HttpHeaders();
		System.out.println(token);
		String accessToken = "Bearer " + token;
		headers.add("Authorization", accessToken);

		HttpEntity<String> httpRequest = new HttpEntity<>(headers);
		try {
			response = restTemplate.exchange(uriComponentsBuilder.buildAndExpand(pathparams).toUriString(),
					HttpMethod.GET, httpRequest, String.class);
		} catch (HttpClientErrorException | HttpServerErrorException e) {
			KeycloakErrorResponseDto keycloakErrorResponseDto = parseKeyClockErrorResponse(e);
			if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
				throw new AuthenticationServiceException(AuthErrorCode.INVALID_TOKEN.getErrorMessage()
						+ keycloakErrorResponseDto.getError_description());
			} else if (e.getStatusCode() == HttpStatus.FORBIDDEN) {
				throw new AccessDeniedException(
						AuthErrorCode.FORBIDDEN.getErrorMessage() + keycloakErrorResponseDto.getError_description());
			} else {
				throw new AuthManagerException(AuthErrorCode.REST_EXCEPTION.getErrorCode(),
						AuthErrorCode.REST_EXCEPTION.getErrorMessage() + " " + e.getResponseBodyAsString());
			}
		}

		if (response.getStatusCode().is2xxSuccessful()) {
			mosipUserDto = getClaims(token);
		}
		return mosipUserDto;

	}

	Logger logger = LoggerFactory.getLogger(this.getClass().getName());

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.mosip.kernel.auth.service.AuthService#logoutUser(java.lang.String)
	 */
	@Override
	public AuthResponseDto logoutUser(String token) {
		if (EmptyCheckUtils.isNullEmpty(token)) {
			throw new AuthenticationServiceException(AuthErrorCode.INVALID_TOKEN.getErrorMessage());
		}
		Map<String, String> pathparams = new HashMap<>();
		pathparams.put(KeycloakConstants.REALM_ID, realmID);
		ResponseEntity<String> response = null;
		AuthResponseDto authResponseDto = new AuthResponseDto();
		StringBuilder urlBuilder = new StringBuilder().append(openIdUrl).append("logout");
		UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromUriString(urlBuilder.toString())
				.queryParam(KeycloakConstants.ID_TOKEN_HINT, token);
		try {
			response = restTemplate.getForEntity(uriComponentsBuilder.buildAndExpand(pathparams).toUriString(),
					String.class);
			System.out.println(response.getBody());
		} catch (HttpClientErrorException | HttpServerErrorException e) {
			throw new AuthManagerException(AuthErrorCode.REST_EXCEPTION.getErrorCode(),
					AuthErrorCode.REST_EXCEPTION.getErrorMessage() + e.getResponseBodyAsString());
		}

		if (response.getStatusCode().is2xxSuccessful()) {
			authResponseDto.setMessage(SUCCESSFULLY_LOGGED_OUT);
			authResponseDto.setStatus(SUCCESS);
		} else {
			authResponseDto.setMessage(LOG_OUT_FAILED);
			authResponseDto.setStatus(FAILED);
		}
		return authResponseDto;
	}

	private MosipUserDto getClaims(String cookie) {
		DecodedJWT decodedJWT = JWT.decode(cookie);

		Claim realmAccess = decodedJWT.getClaim(AuthConstant.REALM_ACCESS);

		RealmAccessDto access = realmAccess.as(RealmAccessDto.class);
		String[] roles = access.getRoles();
		StringBuilder builder = new StringBuilder();

		for (String r : roles) {
			builder.append(r);
			builder.append(AuthConstant.COMMA);
		}
		MosipUserDto dto = new MosipUserDto();
		dto.setUserId(decodedJWT.getClaim(AuthConstant.PREFERRED_USERNAME).asString());
		dto.setMail(decodedJWT.getClaim(AuthConstant.EMAIL).asString());
		dto.setMobile(decodedJWT.getClaim(AuthConstant.MOBILE).asString());
		dto.setName(decodedJWT.getClaim(AuthConstant.PREFERRED_USERNAME).asString());
		dto.setRId(decodedJWT.getClaim(AuthConstant.RID).asString());
		dto.setToken(cookie);
		dto.setRole(builder.toString());
		return dto;
	}

	@Override
	public AccessTokenResponseDTO loginRedirect(String state, String sessionState, String code, String stateCookie,
			String redirectURI) {
		// Compare states
		if (!stateCookie.equals(state)) {
			throw new AuthManagerException(AuthErrorCode.KEYCLOAK_STATE_EXCEPTION.getErrorCode(),
					AuthErrorCode.KEYCLOAK_STATE_EXCEPTION.getErrorMessage());
		}

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
		map.add(KeycloakConstants.GRANT_TYPE, loginFlowName);
		map.add(KeycloakConstants.CLIENT_ID, clientID);
		map.add(KeycloakConstants.CLIENT_SECRET, clientSecret);
		map.add(KeycloakConstants.CODE, code);
		map.add(KeycloakConstants.REDIRECT_URI, this.redirectURI + redirectURI);
		Map<String, String> pathParam = new HashMap<>();
		pathParam.put("realmId", realmID);
		UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(tokenEndpoint);
		HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(map, headers);
		ResponseEntity<String> responseEntity = null;
		try {
			responseEntity = restTemplate.exchange(uriBuilder.buildAndExpand(pathParam).toUriString(), HttpMethod.POST,
					entity, String.class);

		} catch (HttpClientErrorException | HttpServerErrorException e) {
			KeycloakErrorResponseDto keycloakErrorResponseDto = parseKeyClockErrorResponse(e);
			throw new LoginException(AuthErrorCode.KEYCLOAK_ACESSTOKEN_EXCEPTION.getErrorCode(),
					AuthErrorCode.KEYCLOAK_ACESSTOKEN_EXCEPTION.getErrorMessage() + AuthConstant.WHITESPACE
							+ keycloakErrorResponseDto.getError_description());
		}
		AccessTokenResponse accessTokenResponse = null;
		try {
			accessTokenResponse = objectmapper.readValue(responseEntity.getBody(), AccessTokenResponse.class);
		} catch (IOException exception) {
			throw new LoginException(AuthErrorCode.RESPONSE_PARSE_ERROR.getErrorCode(),
					AuthErrorCode.RESPONSE_PARSE_ERROR.getErrorMessage() + AuthConstant.WHITESPACE
							+ exception.getMessage());
		}
		AccessTokenResponseDTO accessTokenResponseDTO = new AccessTokenResponseDTO();
		accessTokenResponseDTO.setAccessToken(accessTokenResponse.getAccess_token());
		accessTokenResponseDTO.setExpiresIn(accessTokenResponse.getExpires_in());
		return accessTokenResponseDTO;
	}

	private KeycloakErrorResponseDto parseKeyClockErrorResponse(HttpStatusCodeException exception) {
		KeycloakErrorResponseDto keycloakErrorResponseDto = null;
		try {
			keycloakErrorResponseDto = objectmapper.readValue(exception.getResponseBodyAsString(),
					KeycloakErrorResponseDto.class);
		} catch (IOException e) {
			throw new LoginException(AuthErrorCode.RESPONSE_PARSE_ERROR.getErrorCode(),
					AuthErrorCode.RESPONSE_PARSE_ERROR.getErrorMessage() + AuthConstant.WHITESPACE + e.getMessage());
		}
		return keycloakErrorResponseDto;
	}

	@Override
	public String getKeycloakURI(String redirectURI, String state) {
		Map<String, String> pathParam = new HashMap<>();
		pathParam.put("realmId", realmID);
		UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(authorizationEndpoint);
		uriComponentsBuilder.queryParam(KeycloakConstants.CLIENT_ID, clientID);
		uriComponentsBuilder.queryParam(KeycloakConstants.REDIRECT_URI, this.redirectURI + redirectURI);
		uriComponentsBuilder.queryParam(KeycloakConstants.STATE, state);
		uriComponentsBuilder.queryParam(KeycloakConstants.RESPONSE_TYPE, responseType);
		uriComponentsBuilder.queryParam(KeycloakConstants.SCOPE, scope);

		return uriComponentsBuilder.buildAndExpand(pathParam).toString();
	}

	private MultiValueMap<String, String> getPasswordValueMap(String clientID, String clientSecret, String username,
			String password) {
		MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
		map.add(AuthConstant.GRANT_TYPE, AuthConstant.PASSWORDCONSTANT);
		map.add(AuthConstant.USER_NAME, username);
		map.add(AuthConstant.PASSWORDCONSTANT, password);
		map.add(AuthConstant.CLIENT_ID, clientID);
		map.add(AuthConstant.CLIENT_SECRET, clientSecret);
		return map;
	}

	private MultiValueMap<String, String> getClientSecretValueMap(String clientID, String clientSecret) {
		MultiValueMap<String, String> map = new LinkedMultiValueMap<String, String>();
		map.add(AuthConstant.GRANT_TYPE, AuthConstant.CLIENT_CREDENTIALS);
		map.add(AuthConstant.CLIENT_ID, clientID);
		map.add(AuthConstant.CLIENT_SECRET, clientSecret);
		return map;
	}

}
