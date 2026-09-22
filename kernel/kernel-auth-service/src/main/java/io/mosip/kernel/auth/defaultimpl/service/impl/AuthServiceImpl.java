package io.mosip.kernel.auth.defaultimpl.service.impl;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.kernel.auth.defaultimpl.config.MosipEnvironment;
import io.mosip.kernel.auth.defaultimpl.constant.AuthConstant;
import io.mosip.kernel.auth.defaultimpl.constant.AuthErrorCode;
import io.mosip.kernel.auth.defaultimpl.constant.KeycloakConstants;
import io.mosip.kernel.auth.defaultimpl.dto.AccessTokenResponse;
import io.mosip.kernel.auth.defaultimpl.dto.AuthToken;
import io.mosip.kernel.auth.defaultimpl.dto.KeycloakErrorResponseDto;
import io.mosip.kernel.auth.defaultimpl.dto.RealmAccessDto;
import io.mosip.kernel.auth.defaultimpl.exception.AuthManagerException;
import io.mosip.kernel.auth.defaultimpl.exception.LoginException;
import io.mosip.kernel.auth.defaultimpl.repository.impl.KeycloakImpl;
import io.mosip.kernel.auth.defaultimpl.service.OTPService;
import io.mosip.kernel.auth.defaultimpl.service.TokenService;
import io.mosip.kernel.auth.defaultimpl.service.UinService;
import io.mosip.kernel.auth.defaultimpl.util.AuthUtil;
import io.mosip.kernel.auth.defaultimpl.util.TokenGenerator;
import io.mosip.kernel.auth.defaultimpl.util.TokenValidator;
import io.mosip.kernel.core.authmanager.model.AccessTokenResponseDTO;
import io.mosip.kernel.core.authmanager.model.AuthNResponse;
import io.mosip.kernel.core.authmanager.model.AuthNResponseDto;
import io.mosip.kernel.core.authmanager.model.AuthResponseDto;
import io.mosip.kernel.core.authmanager.model.AuthZResponseDto;
import io.mosip.kernel.core.authmanager.model.ClientSecret;
import io.mosip.kernel.core.authmanager.model.IndividualIdDto;
import io.mosip.kernel.core.authmanager.model.LoginUser;
import io.mosip.kernel.core.authmanager.model.LoginUserWithClientId;
import io.mosip.kernel.core.authmanager.model.MosipUserDto;
import io.mosip.kernel.core.authmanager.model.MosipUserListDto;
import io.mosip.kernel.core.authmanager.model.MosipUserSaltListDto;
import io.mosip.kernel.core.authmanager.model.MosipUserTokenDto;
import io.mosip.kernel.core.authmanager.model.OtpUser;
import io.mosip.kernel.core.authmanager.model.PasswordDto;
import io.mosip.kernel.core.authmanager.model.RIdDto;
import io.mosip.kernel.core.authmanager.model.RefreshTokenRequest;
import io.mosip.kernel.core.authmanager.model.RefreshTokenResponse;
import io.mosip.kernel.core.authmanager.model.RolesListDto;
import io.mosip.kernel.core.authmanager.model.UserDetailsResponseDto;
import io.mosip.kernel.core.authmanager.model.UserNameDto;
import io.mosip.kernel.core.authmanager.model.UserOtp;
import io.mosip.kernel.core.authmanager.model.UserPasswordRequestDto;
import io.mosip.kernel.core.authmanager.model.UserPasswordResponseDto;
import io.mosip.kernel.core.authmanager.model.UserRegistrationRequestDto;
import io.mosip.kernel.core.authmanager.model.UserRoleDto;
import io.mosip.kernel.core.authmanager.model.ValidationResponseDto;
import io.mosip.kernel.core.util.EmptyCheckUtils;
import io.mosip.kernel.openid.bridge.api.service.AuthService;

/**
 * Production ({@code !local}) auth service: authentication, authorization,
 * Keycloak user/role administration, and OIDC authorization-code helpers.
 * <p>
 * Talks to Keycloak IAM via OpenID {@code /token} and admin APIs. Selected
 * together with the other default IAM beans when
 * {@code mosip.iam.use.default.impl} is true.
 *
 * @author Ramadurai Pandian
 * @author Urvil Joshi
 * @author Srinivasan
 */
@Profile("!local")
@Service
public class AuthServiceImpl implements AuthService {

	/**
	 * Logger for Keycloak token-endpoint diagnostics.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(AuthServiceImpl.class);

	/**
	 * Success message after client-id/secret validation.
	 */
	private static final String CLIENTID_AND_TOKEN_COMBINATION_HAD_BEEN_VALIDATED_SUCCESSFULLY = "Clientid and Token combination had been validated successfully";

	/**
	 * Logout failure message.
	 */
	private static final String LOG_OUT_FAILED = "log out failed";

	/**
	 * Generic failure status string.
	 */
	private static final String FAILED = "Failed";

	/**
	 * Generic success status string.
	 */
	private static final String SUCCESS = "Success";

	/**
	 * Logout success message.
	 */
	private static final String SUCCESSFULLY_LOGGED_OUT = "successfully loggedout";

	/**
	 * Keycloak OpenID Connect base URL ({@code mosip.iam.open-id-url}), typically
	 * including {@code {realmId}} for {@link UriComponentsBuilder#fromUriString}.
	 */
	@Value("${mosip.iam.open-id-url}")
	private String keycloakOpenIdUrl;

	/**
	 * Keycloak admin/user-store implementation.
	 */
	@Autowired
	KeycloakImpl keycloakImpl;

	/**
	 * Local JWT minting (legacy path).
	 */
	@Autowired
	TokenGenerator tokenGenerator;

	/**
	 * Local and Keycloak JWT helpers (issuer, expiry, admin claims).
	 */
	@Autowired
	TokenValidator tokenValidator;

	/**
	 * Persistent OAuth token store.
	 */
	@Autowired
	TokenService customTokenServices;

	/**
	 * OTP send/validate against notification channels.
	 */
	@Autowired
	OTPService oTPService;

	/**
	 * UIN/ID-repository lookup for OTP.
	 */
	@Autowired
	UinService uinService;

	/**
	 * Environment-backed JWT and API URLs.
	 */
	@Autowired
	MosipEnvironment mosipEnvironment;

	/**
	 * JSON mapper for Keycloak error bodies.
	 */
	@Autowired
	ObjectMapper objectmapper;

	/**
	 * Duplicate of {@link #keycloakOpenIdUrl} from the same property.
	 */
	@Value("${mosip.iam.open-id-url}")
	private String openIdUrl;

	/**
	 * OAuth grant used for admin login redirect ({@code mosip.admin.login_flow.name}).
	 */
	@Value("${mosip.admin.login_flow.name}")
	private String loginFlowName;

	/**
	 * Admin OAuth client id.
	 */
	@Value("${mosip.admin.clientid}")
	private String clientID;

	/**
	 * Admin OAuth client secret.
	 */
	@Value("${mosip.admin.clientsecret}")
	private String clientSecret;

	/**
	 * Admin redirect URI prefix ({@code mosip.admin.redirecturi}).
	 */
	@Value("${mosip.admin.redirecturi}")
	private String redirectURI;

	/**
	 * OIDC scope for admin login.
	 */
	@Value("${mosip.admin.login_flow.scope}")
	private String scope;

	/**
	 * OIDC response type for admin login.
	 */
	@Value("${mosip.admin.login_flow.response_type}")
	private String responseType;

	/**
	 * Keycloak authorization endpoint template.
	 */
	@Value("${mosip.iam.authorization_endpoint}")
	private String authorizationEndpoint;

	/**
	 * Keycloak token endpoint template.
	 */
	@Value("${mosip.iam.token_endpoint}")
	private String tokenEndpoint;

	/**
	 * Admin realm id ({@code mosip.admin_realm_id}).
	 */
	@Value("${mosip.admin_realm_id}")
	private String realmID;

	/**
	 * Pre-registration realm id.
	 */
	@Value("${mosip.kernel.prereg.realm-id}")
	private String preRegRealmID;

	/**
	 * Keycloak base URL for userinfo ({@code mosip.iam.base-url}).
	 */
	@Value("${mosip.iam.base-url}")
	private String keycloakBaseURL;

	/**
	 * Maps MOSIP app ids to Keycloak realms.
	 */
	@Autowired
	private AuthUtil authUtil;

	/**
	 * RestTemplate for OpenID token and userinfo calls (not the pooled IAM template).
	 */
	@Qualifier("authRestTemplate")
	@Autowired
	private RestTemplate authRestTemplate;

	/**
	 * Method used for validating Auth token
	 * 
	 * @param token token
	 * 
	 * @return mosipUserDtoToken is of type {@link MosipUserTokenDto}
	 * 
	 * @throws Exception exception
	 * 
	 */

	@Override
	public MosipUserTokenDto validateToken(String token) throws Exception {
		LOGGER.debug("invoked validate token");
		MosipUserTokenDto mosipUserDtoToken = tokenValidator.validateToken(token);
		AuthToken authToken = customTokenServices.getTokenDetails(token);
		if (authToken == null) {
			throw new AuthManagerException(AuthErrorCode.INVALID_TOKEN.getErrorCode(),
					AuthErrorCode.INVALID_TOKEN.getErrorMessage());
		}
		if (mosipUserDtoToken != null /* && (currentTime < authToken.getExpirationTime()) */) {
			LOGGER.debug("token valid for user name " + mosipUserDtoToken.getMosipUserDto().getName());
			return mosipUserDtoToken;
		} else {
			throw new NonceExpiredException(AuthConstant.AUTH_TOKEN_EXPIRED_MESSAGE);
		}
	}

	/**
	 * Method used for Authenticating User based on username and password
	 * 
	 * @param loginUser is of type {@link LoginUser}
	 * 
	 * @return authNResponseDto is of type {@link AuthNResponseDto}
	 * 
	 * @throws Exception exception
	 * 
	 */

	@Override
	public AuthNResponseDto authenticateUser(LoginUser loginUser) throws Exception {
		AuthNResponseDto authNResponseDto = null;
		HttpHeaders headers = new HttpHeaders();
		String realmId = authUtil.getRealmIdFromAppId(loginUser.getAppId());
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		MultiValueMap<String, String> tokenRequestBody = null;
		ResponseEntity<AccessTokenResponse> response = null;
		Map<String, String> pathParams = new HashMap<>();
		pathParams.put(AuthConstant.REALM_ID, realmId);
		UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromUriString(keycloakOpenIdUrl + "/token");
		LOGGER.debug("invoke " + uriComponentsBuilder.toUriString() + " realm " + realmId + " username "
				+ loginUser.getUserName() + "clientId " + clientID);
		tokenRequestBody = getPasswordValueMap(clientID, clientSecret, loginUser.getUserName(),
				loginUser.getPassword());
		HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(tokenRequestBody, headers);
		try {
			response = authRestTemplate.postForEntity(uriComponentsBuilder.buildAndExpand(pathParams).toUriString(),
					request, AccessTokenResponse.class);
		} catch (HttpClientErrorException | HttpServerErrorException ex) {
			LOGGER.error("Exception >>>>>>>>>>>> ", ex);
			if (ex.getStatusCode().value() == 401) {
				throw new AuthManagerException(AuthErrorCode.INVALID_CREDENTIALS.getErrorCode(),
						AuthErrorCode.INVALID_CREDENTIALS.getErrorMessage());
			} else if (ex.getStatusCode().value() == 400) {
				throw new AuthManagerException(AuthErrorCode.REQUEST_VALIDATION_ERROR.getErrorCode(),
						AuthErrorCode.REQUEST_VALIDATION_ERROR.getErrorMessage());
			}

			throw new AuthManagerException(AuthErrorCode.SERVER_ERROR.getErrorCode(),
					AuthErrorCode.SERVER_ERROR.getErrorCode());
		}
		AccessTokenResponse accessTokenResponse = response.getBody();
		if (accessTokenResponse != null) {
			authNResponseDto = new AuthNResponseDto();
			authNResponseDto.setToken(accessTokenResponse.getAccess_token());
			authNResponseDto.setRefreshToken(accessTokenResponse.getRefresh_token());
			authNResponseDto.setExpiryTime(Long.parseLong(accessTokenResponse.getExpires_in()));
			authNResponseDto.setStatus(AuthConstant.SUCCESS_STATUS);
			authNResponseDto.setMessage(AuthConstant.USERPWD_SUCCESS_MESSAGE);
			authNResponseDto.setRefreshExpiryTime(Long.parseLong(accessTokenResponse.getRefresh_expires_in()));
			return authNResponseDto;
		} else {
			throw new AuthManagerException(AuthErrorCode.CLIENT_ERROR.getErrorCode(), "reponse body received is null");
		}
	}

	/**
	 * Method used for sending OTP
	 * 
	 * @param otpUser is of type {@link OtpUser}
	 * 
	 * @return authNResponseDto is of type {@link AuthNResponseDto}
	 * 
	 * @throws Exception exception
	 * 
	 */

	@Override
	public AuthNResponseDto authenticateWithOtp(OtpUser otpUser) throws Exception {
		AuthNResponseDto authNResponseDto = null;
		String realmId = authUtil.getRealmIdFromAppId(otpUser.getAppId());
		MosipUserDto mosipUser = null;
		otpUser.getOtpChannel().replaceAll(String::toLowerCase);
		otpUser.setAppId(otpUser.getAppId().toLowerCase());
		otpUser.setOtpChannel(otpUser.getOtpChannel());
		if (AuthConstant.APPTYPE_UIN.equals(otpUser.getUseridtype())) {
			mosipUser = uinService.getDetailsFromUin(otpUser);
			LOGGER.debug("OTP, apptype uin with userid " + mosipUser.getUserId() + " realm " + realmId);
			authNResponseDto = oTPService.sendOTPForUin(mosipUser, otpUser, realmId);
			authNResponseDto.setStatus(authNResponseDto.getStatus());
			authNResponseDto.setMessage(authNResponseDto.getMessage());
		} else if (AuthConstant.APPTYPE_USERID.equals(otpUser.getUseridtype())) {
			UserRegistrationRequestDto userCreationRequestDto = new UserRegistrationRequestDto();
			LOGGER.debug("OTP, apptype userid for userid " + otpUser.getUserId() + " realm " + realmId);
			userCreationRequestDto.setUserName(otpUser.getUserId());
			userCreationRequestDto.setAppId(otpUser.getAppId());
			mosipUser = registerUser(userCreationRequestDto);
			authNResponseDto = oTPService.sendOTP(mosipUser, otpUser, realmId);
			LOGGER.debug("otp request status " + otpUser.getUserId() + " realm " + realmId + " status "
					+ authNResponseDto.getStatus());
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
	 * @param userOtp is of type {@link UserOtp}
	 * 
	 * @return authNResponseDto is of type {@link AuthNResponseDto}
	 * 
	 * @throws Exception exception
	 * 
	 */

	@Override
	public AuthNResponseDto authenticateUserWithOtp(UserOtp userOtp) throws Exception {
		AuthNResponseDto authNResponseDto = new AuthNResponseDto();
		MosipUserTokenDto mosipToken = null;
		MosipUserDto mosipUser = null;
		String realm = authUtil.getRealmIdFromAppId(userOtp.getAppId());
		if (userOtp.getAppId().equalsIgnoreCase(AuthConstant.PRE_REGISTRATION)) {
			realm = userOtp.getAppId();
		}

		LOGGER.debug("otp request status " + userOtp.getUserId() + " realm " + realm);
		if (keycloakImpl.isUserAlreadyPresent(userOtp.getUserId(), realm)) {
			mosipUser = new MosipUserDto();
			mosipUser.setUserId(userOtp.getUserId());
			LOGGER.info("user already present " + userOtp.getUserId() + " realm " + realm);
		}
		if (mosipUser == null && AuthConstant.IDA.toLowerCase().equals(userOtp.getAppId().toLowerCase())) {
			mosipUser = uinService.getDetailsForValidateOtp(userOtp.getUserId());
		}
		if (mosipUser != null) {
			mosipToken = oTPService.validateOTP(mosipUser, userOtp.getOtp(), realm);
			LOGGER.info("Validate otp" + userOtp.getUserId() + " realm " + realm + " status " + mosipToken.getStatus());
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
			authNResponseDto.setRefreshExpiryTime(mosipToken.getRefreshExpTime());
		} else {
			authNResponseDto.setMessage(mosipToken.getMessage());
			authNResponseDto.setStatus(mosipToken.getStatus());
		}
		return authNResponseDto;
	}

	/**
	 * Method used for Authenticating User based with secretkey and password
	 * 
	 * @param clientSecret is of type {@link ClientSecret}
	 * 
	 * @return authNResponseDto is of type {@link AuthNResponseDto}
	 * 
	 * @throws Exception exception
	 * 
	 */

	@Override
	public AuthNResponseDto authenticateWithSecretKey(ClientSecret clientSecret) throws Exception {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		MultiValueMap<String, String> tokenRequestBody = null;
		Map<String, String> pathParams = new HashMap<>();
		String realmId = authUtil.getRealmIdFromAppId(clientSecret.getAppId());
		pathParams.put(AuthConstant.REALM_ID, realmId);
		UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromUriString(keycloakOpenIdUrl + "/token");
		LOGGER.info("invoke " + uriComponentsBuilder.toUriString() + " secret key based authentication "
				+ clientSecret.getClientId() + " realm " + realmId);
		LOGGER.info("This should not be invoked often. If you see too many of this then we need to fix the bug.");
		tokenRequestBody = getClientSecretValueMap(clientSecret.getClientId(), clientSecret.getSecretKey());
		HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(tokenRequestBody, headers);
		ResponseEntity<AccessTokenResponse> response = authRestTemplate.postForEntity(
				uriComponentsBuilder.buildAndExpand(pathParams).toUriString(), request, AccessTokenResponse.class);
		AccessTokenResponse accessTokenResponse = null;
			accessTokenResponse = response.getBody();
		if (accessTokenResponse != null) {
			LOGGER.info("secret key based authentication " + clientSecret.getClientId() + " realm " + realmId
					+ " accesstoken expires in " + accessTokenResponse.getExpires_in() + " refresh token expires in "
					+ accessTokenResponse.getRefresh_expires_in());
			AuthNResponseDto authNResponseDto = new AuthNResponseDto();
			authNResponseDto.setToken(accessTokenResponse.getAccess_token());
			authNResponseDto.setRefreshToken(accessTokenResponse.getRefresh_token());
			authNResponseDto.setExpiryTime(Long.parseLong(accessTokenResponse.getExpires_in()));
			authNResponseDto.setStatus(SUCCESS);
			authNResponseDto.setMessage(CLIENTID_AND_TOKEN_COMBINATION_HAD_BEEN_VALIDATED_SUCCESSFULLY);
			return authNResponseDto;
		} else {
			throw new AuthManagerException(AuthErrorCode.CLIENT_ERROR.getErrorCode(), "responsebody is null");
		}
	}

	/**
	 * Method used for generating refresh token
	 * 
	 * @param refreshToken existing refresh token
	 * 
	 * @return mosipUserDtoToken is of type {@link MosipUserTokenDto}
	 * 
	 * @throws Exception exception
	 * 
	 */

	@Override
	public RefreshTokenResponse refreshToken(String appID, String refreshToken, RefreshTokenRequest refreshTokenRequest)
			throws Exception {
		MultiValueMap<String, String> tokenRequestBody = new LinkedMultiValueMap<>();
		tokenRequestBody.add(AuthConstant.GRANT_TYPE, AuthConstant.REFRESH_TOKEN);
		tokenRequestBody.add(AuthConstant.REFRESH_TOKEN, refreshToken);
		tokenRequestBody.add(AuthConstant.CLIENT_ID, refreshTokenRequest.getClientID());
		tokenRequestBody.add(AuthConstant.CLIENT_SECRET, refreshTokenRequest.getClientSecret());
		String realmId = authUtil.getRealmIdFromAppId(appID);
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		Map<String, String> pathParams = new HashMap<>();

		pathParams.put(AuthConstant.REALM_ID, realmId);

		UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromUriString(keycloakOpenIdUrl + "/token");
		LOGGER.info("refresh token based authentication " + uriComponentsBuilder.toUriString() + " realm " + realmId
				+ " client id " + refreshTokenRequest.getClientID());
		HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(tokenRequestBody, headers);
		ResponseEntity<AccessTokenResponse> response = null;
		try {
			response = authRestTemplate.postForEntity(uriComponentsBuilder.buildAndExpand(pathParams).toUriString(),
					request, AccessTokenResponse.class);
		} catch (HttpServerErrorException | HttpClientErrorException ex) {
			LOGGER.error("refresh token based authentication " + uriComponentsBuilder.toUriString() + " realm "
					+ realmId + " client id " + refreshTokenRequest.getClientID() + " failed with error ");
			LOGGER.error(ex.getMessage());
		}
		Objects.requireNonNull(response);
		AccessTokenResponse accessTokenResponse = response.getBody();
		if (accessTokenResponse != null) {
			AuthNResponse authNResponse = new AuthNResponse("SUCCESS", "Access token refreshed");
			LOGGER.info("refresh token based authentication " + uriComponentsBuilder.toUriString() + " realm " + realmId
					+ " client id " + refreshTokenRequest.getClientID() + ". Response status: "
					+ authNResponse.getStatus());
			return new RefreshTokenResponse(authNResponse, accessTokenResponse.getAccess_token(),
					accessTokenResponse.getRefresh_token(), accessTokenResponse.getExpires_in(),
					accessTokenResponse.getExpires_in());
		} else {
			throw new AuthManagerException(AuthErrorCode.CLIENT_ERROR.getErrorCode(), "response body is null");
		}
	}

	/**
	 * Method used for invalidate token
	 * 
	 * @param token token
	 * 
	 * @return authNResponse is of type {@link AuthNResponse}
	 * 
	 * @throws Exception exception
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

	/**
	 * Lists Keycloak roles for the realm mapped from {@code appId}.
	 *
	 * @param appId MOSIP application id
	 * @return roles list
	 */
	@Override
	public RolesListDto getAllRoles(String appId) {
		appId = authUtil.getRealmIdFromAppId(appId);
		RolesListDto rolesListDto = keycloakImpl.getAllRoles(appId);
		return rolesListDto;
	}

	/**
	 * Loads user details for the given user ids in the mapped realm.
	 *
	 * @param userDetails user ids
	 * @param appId       MOSIP application id
	 * @return MOSIP user list
	 * @throws Exception if Keycloak lookup fails
	 */
	@Override
	public MosipUserListDto getListOfUsersDetails(List<String> userDetails, String appId) throws Exception {
		appId = authUtil.getRealmIdFromAppId(appId);
		MosipUserListDto mosipUserListDto = keycloakImpl.getListOfUsersDetails(userDetails, appId);
		return mosipUserListDto;
	}

	/**
	 * Loads users with salt metadata for the given user ids.
	 *
	 * @param userDetails user ids
	 * @param appId       MOSIP application id
	 * @return users with salts
	 * @throws Exception if Keycloak lookup fails
	 */
	@Override
	public MosipUserSaltListDto getAllUserDetailsWithSalt(List<String> userDetails, String appId) throws Exception {
		appId = authUtil.getRealmIdFromAppId(appId);
		return keycloakImpl.getAllUserDetailsWithSalt(userDetails, appId);
	}

	/**
	 * Resolves RID from user id in the mapped realm.
	 *
	 * @param userId user id
	 * @param appId  MOSIP application id
	 * @return RID wrapper
	 * @throws Exception if the user or attribute is missing
	 */
	@Override
	public RIdDto getRidBasedOnUid(String userId, String appId) throws Exception {
		appId = authUtil.getRealmIdFromAppId(appId);
		return keycloakImpl.getRidFromUserId(userId, appId);

	}

	/**
	 * Registers a user in Keycloak.
	 *
	 * @param userCreationRequestDto registration request
	 * @return created user
	 */
	@Override
	public MosipUserDto registerUser(UserRegistrationRequestDto userCreationRequestDto) {
		return keycloakImpl.registerUser(userCreationRequestDto);
	}

	/**
	 * Validates a Keycloak access token via userinfo and maps JWT claims to
	 * {@link MosipUserDto}. Uses {@link UriComponentsBuilder#fromUriString}.
	 *
	 * @param token compact access token
	 * @return user DTO including the original token
	 */
	@Override
	public MosipUserDto valdiateToken(String token) {
		Map<String, String> pathparams = new HashMap<>();
		if (EmptyCheckUtils.isNullEmpty(token)) {
			throw new AuthenticationServiceException(AuthErrorCode.INVALID_TOKEN.getErrorMessage());
		}

		DecodedJWT decodedJWT = JWT.decode(token);
		String issuer = decodedJWT.getClaim("iss").asString();
		if (EmptyCheckUtils.isNullEmpty(issuer)) {
			throw new JWTDecodeException("Invalid JWT: missing iss claim");
		}
		String realm = issuer.substring(issuer.lastIndexOf("/") + 1);

		ResponseEntity<String> response = null;
		MosipUserDto mosipUserDto = null;
		StringBuilder urlBuilder = new StringBuilder().append(keycloakBaseURL).append("/auth/realms/").append(realm)
				.append("/protocol/openid-connect/userinfo");
		UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromUriString(urlBuilder.toString());
		LOGGER.info("validate token request to " + uriComponentsBuilder.toUriString());

		HttpHeaders headers = new HttpHeaders();

		String accessToken = "Bearer " + token;
		headers.add("Authorization", accessToken);

		HttpEntity<String> httpRequest = new HttpEntity<>(headers);
		try {
			response = authRestTemplate.exchange(uriComponentsBuilder.buildAndExpand(pathparams).toUriString(),
					HttpMethod.GET, httpRequest, String.class);
		} catch (HttpClientErrorException | HttpServerErrorException e) {
			LOGGER.error("Token validation failed for accessToken {}", accessToken);
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
			mosipUserDto = getClaims(decodedJWT, token);
			LOGGER.info("Response received for user id " + mosipUserDto.getUserId() + " is "
					+ response.getStatusCode().toString());
		}
		return mosipUserDto;

	}

	// Logger logger = LoggerFactory.getLogger(this.getClass().getName());

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
		String issuer = tokenValidator.getissuer(token);
		ResponseEntity<String> response = null;
		AuthResponseDto authResponseDto = new AuthResponseDto();
		StringBuilder urlBuilder = new StringBuilder().append(issuer).append("/protocol/openid-connect/logout");
		UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromUriString(urlBuilder.toString())
				.queryParam(KeycloakConstants.ID_TOKEN_HINT, token);

		LOGGER.info("logout user {} uri: {}", token, uriComponentsBuilder.toUriString());
		try {
			response = authRestTemplate.getForEntity(uriComponentsBuilder.buildAndExpand(pathparams).toUriString(),
					String.class);

		} catch (HttpClientErrorException | HttpServerErrorException e) {
			LOGGER.error("error occcur in logout : {}", e.getResponseBodyAsString());
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
		LOGGER.info("logout status {} for token {}", authResponseDto.getStatus(), token);
		return authResponseDto;
	}

	/**
	 * Maps decoded Keycloak claims (roles, email, mobile, RID) onto {@link MosipUserDto}.
	 *
	 * @param decodedJWT decoded access token
	 * @param cookie     original token string stored on the DTO
	 * @return user DTO
	 */
	private MosipUserDto getClaims(DecodedJWT decodedJWT, String cookie) {

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

	/**
	 * Completes the admin authorization-code login: checks {@code state}, then
	 * exchanges {@code code} at the Keycloak token endpoint.
	 *
	 * @param state         OAuth state from the callback
	 * @param sessionState  Keycloak session state (unused)
	 * @param code          authorization code
	 * @param stateCookie   expected state from cookie
	 * @param redirectURI   suffix appended to {@link #redirectURI}
	 * @return access token and expiry
	 */
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
			responseEntity = authRestTemplate.exchange(uriBuilder.buildAndExpand(pathParam).toUriString(),
					HttpMethod.POST, entity, String.class);

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

	/**
	 * Parses Keycloak error JSON from an HTTP status exception.
	 *
	 * @param exception client or server error from RestTemplate
	 * @return error DTO
	 */
	private KeycloakErrorResponseDto parseKeyClockErrorResponse(HttpStatusCodeException exception) {
		KeycloakErrorResponseDto keycloakErrorResponseDto = null;
		try {
			keycloakErrorResponseDto = objectmapper.readValue(exception.getResponseBodyAsString(),
					KeycloakErrorResponseDto.class);
			LOGGER.error(keycloakErrorResponseDto.getError());
		} catch (IOException e) {
			throw new LoginException(AuthErrorCode.RESPONSE_PARSE_ERROR.getErrorCode(),
					AuthErrorCode.RESPONSE_PARSE_ERROR.getErrorMessage() + AuthConstant.WHITESPACE + e.getMessage());
		}
		return keycloakErrorResponseDto;
	}

	/**
	 * Builds the Keycloak authorization-endpoint URL for admin login.
	 *
	 * @param redirectURI suffix appended to {@link #redirectURI}
	 * @param state       OAuth state parameter
	 * @return authorization URL
	 */
	@Override
	public String getKeycloakURI(String redirectURI, String state) {
		Map<String, String> pathParam = new HashMap<>();
		pathParam.put("realmId", realmID);
		UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromUriString(authorizationEndpoint);
		uriComponentsBuilder.queryParam(KeycloakConstants.CLIENT_ID, clientID);
		uriComponentsBuilder.queryParam(KeycloakConstants.REDIRECT_URI, this.redirectURI + redirectURI);
		uriComponentsBuilder.queryParam(KeycloakConstants.STATE, state);
		uriComponentsBuilder.queryParam(KeycloakConstants.RESPONSE_TYPE, responseType);
		uriComponentsBuilder.queryParam(KeycloakConstants.SCOPE, scope);

		return uriComponentsBuilder.buildAndExpand(pathParam).toString();
	}

	/**
	 * Form body for the OAuth password grant against Keycloak {@code /token}.
	 *
	 * @param clientID     OAuth client id
	 * @param clientSecret OAuth client secret
	 * @param username     user name
	 * @param password     password
	 * @return form fields
	 */
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

	/**
	 * Form body for the OAuth client-credentials grant.
	 *
	 * @param clientID     OAuth client id
	 * @param clientSecret OAuth client secret
	 * @return form fields
	 */
	private MultiValueMap<String, String> getClientSecretValueMap(String clientID, String clientSecret) {
		MultiValueMap<String, String> map = new LinkedMultiValueMap<String, String>();
		map.add(AuthConstant.GRANT_TYPE, AuthConstant.CLIENT_CREDENTIALS);
		map.add(AuthConstant.CLIENT_ID, clientID);
		map.add(AuthConstant.CLIENT_SECRET, clientSecret);
		return map;
	}

	/**
	 * Password grant using client id/secret from {@link LoginUserWithClientId}
	 * (syncdata internal authenticate). Uses {@code getStatusCode().value()} for
	 * 401/400 mapping.
	 *
	 * @param loginUser credentials plus client id/secret
	 * @return tokens and status
	 * @throws Exception if Keycloak rejects the grant
	 */
	@Override
	public AuthNResponseDto authenticateUser(LoginUserWithClientId loginUser) throws Exception {
		AuthNResponseDto authNResponseDto = null;
		HttpHeaders headers = new HttpHeaders();
		String realmId = authUtil.getRealmIdFromAppId(loginUser.getAppId());
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		MultiValueMap<String, String> tokenRequestBody = null;
		ResponseEntity<AccessTokenResponse> response = null;
		Map<String, String> pathParams = new HashMap<>();
		pathParams.put(AuthConstant.REALM_ID, realmId);
		UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromUriString(keycloakOpenIdUrl + "/token");
		LOGGER.debug("invoke " + uriComponentsBuilder.toUriString() + " realm " + realmId + " username "
				+ loginUser.getUserName() + "clientId " + loginUser.getClientId());
		tokenRequestBody = getPasswordValueMap(loginUser.getClientId(), loginUser.getClientSecret(),
				loginUser.getUserName(), loginUser.getPassword());
		HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(tokenRequestBody, headers);
		try {
			response = authRestTemplate.postForEntity(uriComponentsBuilder.buildAndExpand(pathParams).toUriString(),
					request, AccessTokenResponse.class);
		} catch (HttpClientErrorException | HttpServerErrorException ex) {
			LOGGER.error("Exception >>>>>>>>>>>> ", ex);
			if (ex.getStatusCode().value() == 401) {
				throw new AuthManagerException(AuthErrorCode.INVALID_CREDENTIALS.getErrorCode(),
						AuthErrorCode.INVALID_CREDENTIALS.getErrorMessage());
			} else if (ex.getStatusCode().value() == 400) {
				throw new AuthManagerException(AuthErrorCode.REQUEST_VALIDATION_ERROR.getErrorCode(),
						AuthErrorCode.REQUEST_VALIDATION_ERROR.getErrorMessage());
			}

			throw new AuthManagerException(AuthErrorCode.SERVER_ERROR.getErrorCode(),
					AuthErrorCode.SERVER_ERROR.getErrorCode());
		}
		AccessTokenResponse accessTokenResponse = response.getBody();
		if (accessTokenResponse != null) {
			authNResponseDto = new AuthNResponseDto();
			authNResponseDto.setToken(accessTokenResponse.getAccess_token());
			authNResponseDto.setRefreshToken(accessTokenResponse.getRefresh_token());
			authNResponseDto.setExpiryTime(Long.parseLong(accessTokenResponse.getExpires_in()));
			authNResponseDto.setStatus(AuthConstant.SUCCESS_STATUS);
			authNResponseDto.setMessage(AuthConstant.USERPWD_SUCCESS_MESSAGE);
			authNResponseDto.setRefreshExpiryTime(Long.parseLong(accessTokenResponse.getRefresh_expires_in()));
			return authNResponseDto;
		} else {
			throw new AuthManagerException(AuthErrorCode.CLIENT_ERROR.getErrorCode(), "response body is null");
		}
	}

	/**
	 * Resolves individual id from user id in the mapped realm.
	 *
	 * @param userId user id
	 * @param appId  MOSIP application id
	 * @return individual id wrapper
	 */
	@Override
	public IndividualIdDto getIndividualIdBasedOnUserID(String userId, String appId) {
		return keycloakImpl.getIndividualIdFromUserId(userId, authUtil.getRealmIdFromAppId(appId));
	}

	/**
	 * Searches Keycloak users; {@code realmId} argument is treated as a MOSIP app
	 * id and mapped to a realm.
	 *
	 * @param realmId   MOSIP application id (mapped to realm)
	 * @param roleName  optional role filter
	 * @param pageStart pagination offset
	 * @param pageFetch page size
	 * @param email     optional email filter
	 * @param firstName optional first name
	 * @param lastName  optional last name
	 * @param username  optional username
	 * @param search    optional free-text search
	 * @return matching users
	 */
	@Override
	public MosipUserListDto getListOfUsersDetails(String realmId, String roleName, int pageStart, int pageFetch,
			String email, String firstName, String lastName, String username, String search) {
		return keycloakImpl.getListOfUsersDetails(authUtil.getRealmIdFromAppId(realmId), roleName, pageStart, pageFetch,
				email, firstName, lastName, username, search);
	}
}
