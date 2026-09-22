package io.mosip.kernel.auth.defaultimpl.service.impl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.kernel.auth.defaultimpl.config.MosipEnvironment;
import io.mosip.kernel.auth.defaultimpl.constant.AuthConstant;
import io.mosip.kernel.auth.defaultimpl.exception.AuthManagerException;
import io.mosip.kernel.auth.defaultimpl.repository.impl.KeycloakImpl;
import io.mosip.kernel.auth.defaultimpl.service.OTPService;
import io.mosip.kernel.auth.defaultimpl.service.TokenService;
import io.mosip.kernel.auth.defaultimpl.service.UinService;
import io.mosip.kernel.auth.defaultimpl.util.ProxyTokenGenerator;
import io.mosip.kernel.auth.defaultimpl.util.TokenGenerator;
import io.mosip.kernel.auth.defaultimpl.util.TokenValidator;
import io.mosip.kernel.core.authmanager.model.AccessTokenResponseDTO;
import io.mosip.kernel.core.authmanager.model.AuthNResponse;
import io.mosip.kernel.core.authmanager.model.AuthNResponseDto;
import io.mosip.kernel.core.authmanager.model.AuthResponseDto;
import io.mosip.kernel.core.authmanager.model.ClientSecret;
import io.mosip.kernel.core.authmanager.model.IndividualIdDto;
import io.mosip.kernel.core.authmanager.model.LoginUser;
import io.mosip.kernel.core.authmanager.model.LoginUserWithClientId;
import io.mosip.kernel.core.authmanager.model.MosipUserDto;
import io.mosip.kernel.core.authmanager.model.MosipUserListDto;
import io.mosip.kernel.core.authmanager.model.MosipUserSaltListDto;
import io.mosip.kernel.core.authmanager.model.MosipUserTokenDto;
import io.mosip.kernel.core.authmanager.model.OtpUser;
import io.mosip.kernel.core.authmanager.model.RIdDto;
import io.mosip.kernel.core.authmanager.model.RefreshTokenRequest;
import io.mosip.kernel.core.authmanager.model.RefreshTokenResponse;
import io.mosip.kernel.core.authmanager.model.RolesListDto;
import io.mosip.kernel.core.authmanager.model.UserOtp;
import io.mosip.kernel.core.authmanager.model.UserRegistrationRequestDto;
import io.mosip.kernel.openid.bridge.api.service.AuthService;

/**
 * Proxy Implementation of Auth service which will not use IAM just give back
 * proxy token.
 * 
 * @author Ramadurai Pandian
 * @author Urvil Joshi
 * @author Srinivasan
 *
 */

@Profile("local")
@Service
public class ProxyAuthServiceImpl implements AuthService {

	/**
	 * Logger for proxy-auth diagnostics.
	 */
	private static final Logger logger = LoggerFactory.getLogger(ProxyAuthServiceImpl.class);

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
	 * Unsigned local JWT issuer.
	 */
	@Autowired
	private ProxyTokenGenerator proxyTokenGenarator;

	/**
	 * Keycloak OpenID URL (unused on most proxy paths).
	 */
	@Value("${mosip.iam.open-id-url}")
	private String keycloakOpenIdUrl;

	/**
	 * Admin realm id.
	 */
	@Value("${mosip.admin_realm_id}")
	private String realmId;

	/**
	 * Keycloak datastore (unused on some stub methods).
	 */
	@Autowired
	KeycloakImpl keycloakImpl;

	/**
	 * Local JWT minting (legacy).
	 */
	@Autowired
	TokenGenerator tokenGenerator;

	/**
	 * Token helpers.
	 */
	@Autowired
	TokenValidator tokenValidator;

	/**
	 * Persistent token store.
	 */
	@Autowired
	TokenService customTokenServices;

	/**
	 * OTP send/validate (proxy or real depending on profile beans).
	 */
	@Autowired
	OTPService oTPService;

	/**
	 * UIN lookup when {@link #proxyOtp} is false.
	 */
	@Autowired
	UinService uinService;

	/**
	 * Environment-backed JWT settings.
	 */
	@Autowired
	MosipEnvironment mosipEnvironment;

	/**
	 * JSON mapper.
	 */
	@Autowired
	ObjectMapper objectmapper;

	/**
	 * Duplicate OpenID URL property.
	 */
	@Value("${mosip.iam.open-id-url}")
	private String openIdUrl;

	/**
	 * Admin login grant name.
	 */
	@Value("${mosip.admin.login_flow.name}")
	private String loginFlowName;

	/**
	 * Admin OAuth client id.
	 */
	@Value("${mosip.admin.clientid}")
	private String clientID;

	/**
	 * Admin OAuth secret.
	 */
	@Value("${mosip.admin.clientsecret}")
	private String clientSecret;

	/**
	 * Admin redirect URI prefix.
	 */
	@Value("${mosip.admin.redirecturi}")
	private String redirectURI;

	/**
	 * Admin login scope.
	 */
	@Value("${mosip.admin.login_flow.scope}")
	private String scope;

	/**
	 * Admin login response type.
	 */
	@Value("${mosip.admin.login_flow.response_type}")
	private String responseType;

	/**
	 * Authorization endpoint template.
	 */
	@Value("${mosip.iam.authorization_endpoint}")
	private String authorizationEndpoint;

	/**
	 * Token endpoint template.
	 */
	@Value("${mosip.iam.token_endpoint}")
	private String tokenEndpoint;

	/**
	 * Duplicate admin realm id property.
	 */
	@Value("${mosip.admin_realm_id}")
	private String realmID;

	/**
	 * Active Spring profile.
	 */
	@Value("${spring.profiles.active}")
	String activeProfile;

	/**
	 * Local proxy token expiry offset in millis.
	 */
	@Value("${auth.local.exp:1000000}")
	long localExp;

	/**
	 * Local HMAC secret (unused with Algorithm.none proxy tokens).
	 */
	@Value("${auth.local.secret:secret}")
	String localSecret;

	/**
	 * RestTemplate for unused IAM calls.
	 */
	@Qualifier("authRestTemplate")
	@Autowired
	private RestTemplate authRestTemplate;

	/**
	 * When true, OTP send uses stub user details instead of ID repository.
	 */
	@Value("${mosip.kernel.auth.proxy-otp}")
	private boolean proxyOtp;

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

	@Deprecated
	@Override
	public MosipUserTokenDto validateToken(String token) throws Exception {
		throw new UnsupportedOperationException("This openeration is not supported");
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
		return proxyTokenForLocalEnv(loginUser.getUserName(), AuthConstant.SUCCESS_STATUS,
				AuthConstant.USERPWD_SUCCESS_MESSAGE);
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

		MosipUserDto mosipUser = null;
		otpUser.getOtpChannel().replaceAll(String::toLowerCase);
		otpUser.setAppId(otpUser.getAppId().toLowerCase());
		otpUser.setOtpChannel(otpUser.getOtpChannel());

		if (AuthConstant.APPTYPE_UIN.equals(otpUser.getUseridtype())) {
			if (!proxyOtp) {
				mosipUser = uinService.getDetailsFromUin(otpUser);
			} else {
				mosipUser = new MosipUserDto();
				mosipUser.setMail("mosip@mosip.io");
				mosipUser.setMobile("91818181223");
				mosipUser.setRId("10012100240015720200428110601");
				mosipUser.setRole("IDA");
			}
			authNResponseDto = oTPService.sendOTPForUin(mosipUser, otpUser, "ida");
			authNResponseDto.setStatus(authNResponseDto.getStatus());
			authNResponseDto.setMessage(authNResponseDto.getMessage());
		} else if (AuthConstant.APPTYPE_USERID.equals(otpUser.getUseridtype())) {
			UserRegistrationRequestDto userCreationRequestDto = new UserRegistrationRequestDto();
			userCreationRequestDto.setUserName(otpUser.getUserId());
			userCreationRequestDto.setAppId(otpUser.getAppId());
			mosipUser = new MosipUserDto();
			mosipUser.setUserId(otpUser.getUserId());
			authNResponseDto = oTPService.sendOTP(mosipUser, otpUser, "mosip");
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
		String realm = realmId;
		if (userOtp.getAppId().equalsIgnoreCase(AuthConstant.PRE_REGISTRATION)) {
			realm = userOtp.getAppId();
		}
		mosipUser = new MosipUserDto();
		mosipUser.setUserId(userOtp.getUserId());
		if (AuthConstant.IDA.toLowerCase().equals(userOtp.getAppId().toLowerCase())) {
			mosipUser = uinService.getDetailsForValidateOtp(userOtp.getUserId());
		}
		mosipToken = oTPService.validateOTP(mosipUser, userOtp.getOtp(), userOtp.getAppId());
		if (mosipToken != null) {
			if (mosipToken.getMosipUserDto() != null) {
				authNResponseDto.setMessage(mosipToken.getMessage());
				authNResponseDto.setStatus(mosipToken.getStatus());
				authNResponseDto.setToken(mosipToken.getToken());
				authNResponseDto.setExpiryTime(mosipToken.getExpTime());
				authNResponseDto.setRefreshToken(mosipToken.getRefreshToken());
				authNResponseDto.setUserId(mosipToken.getMosipUserDto().getUserId());
				authNResponseDto.setRefreshExpiryTime(mosipToken.getExpTime());
			} else {
				authNResponseDto.setMessage(mosipToken.getMessage());
				authNResponseDto.setStatus(mosipToken.getStatus());
			}
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
		return proxyTokenForLocalEnv(clientSecret.getClientId(), SUCCESS,
				CLIENTID_AND_TOKEN_COMBINATION_HAD_BEEN_VALIDATED_SUCCESSFULLY);
	}

	/**
	 * Issues an unsigned proxy JWT for {@code subject} with {@link #localExp} offset.
	 *
	 * @param subject JWT subject
	 * @param status  response status
	 * @param message response message
	 * @return tokens pointing at the same proxy JWT
	 */
	private AuthNResponseDto proxyTokenForLocalEnv(String subject, String status, String message) {
		long exp = System.currentTimeMillis() + localExp;
		String token = proxyTokenGenarator.getProxyToken(subject, exp);
		logger.debug("token craeted for subject {} to expire at {}", subject, exp);
		AuthNResponseDto authNResponseDto = new AuthNResponseDto();
		authNResponseDto.setToken(token);
		authNResponseDto.setRefreshToken(token);
		authNResponseDto.setExpiryTime(exp);
		authNResponseDto.setRefreshExpiryTime(exp);
		authNResponseDto.setStatus(status);
		authNResponseDto.setMessage(message);
		return authNResponseDto;
	}

	/**
	 * Method used for generating refresh token
	 * 
	 * @param appID
	 * @param refereshToken
	 * @param refreshTokenRequest
	 * @return
	 * @throws Exception
	 */
	@Override
	public RefreshTokenResponse refreshToken(String appID, String refereshToken,
			RefreshTokenRequest refreshTokenRequest) throws Exception {
		throw new UnsupportedOperationException("This openeration is not supported in local profile for now");
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
		throw new UnsupportedOperationException("This openeration is not supported in local profile for now");
	}

	/**
	 * Not supported in the local profile.
	 *
	 * @param appId unused
	 * @return never
	 */
	@Override
	public RolesListDto getAllRoles(String appId) {
		throw new UnsupportedOperationException("This openeration is not supported in local profile for now");
	}

	/**
	 * Not supported in the local profile.
	 *
	 * @param userDetails unused
	 * @param appId       unused
	 * @return never
	 * @throws Exception never (unsupported)
	 */
	@Override
	public MosipUserListDto getListOfUsersDetails(List<String> userDetails, String appId) throws Exception {
		throw new UnsupportedOperationException("This openeration is not supported in local profile for now");
	}

	/**
	 * Not supported in the local profile.
	 *
	 * @param userDetails unused
	 * @param appId       unused
	 * @return never
	 * @throws Exception never (unsupported)
	 */
	@Override
	public MosipUserSaltListDto getAllUserDetailsWithSalt(List<String> userDetails, String appId) throws Exception {
		throw new UnsupportedOperationException("This openeration is not supported in local profile for now");
	}

	/**
	 * Not supported in the local profile.
	 *
	 * @param userId unused
	 * @param appId  unused
	 * @return never
	 * @throws Exception never (unsupported)
	 */
	@Override
	public RIdDto getRidBasedOnUid(String userId, String appId) throws Exception {
		throw new UnsupportedOperationException("This openeration is not supported in local profile for now");
	}


	/**
	 * Not supported in the local profile.
	 *
	 * @param userCreationRequestDto unused
	 * @return never
	 */
	@Override
	public MosipUserDto registerUser(UserRegistrationRequestDto userCreationRequestDto) {
		throw new UnsupportedOperationException("This openeration is not supported in local profile for now");
	}
	
	
	/**
	 * Deprecated / not supported in the local profile.
	 *
	 * @param token unused
	 * @return never
	 */
	@Override
	public MosipUserDto valdiateToken(String token) {
		// this will verify token
		throw new UnsupportedOperationException("This operation is deprecated in local profile.");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.mosip.kernel.auth.service.AuthService#logoutUser(java.lang.String)
	 */
	@Override
	public AuthResponseDto logoutUser(String token) {
		throw new UnsupportedOperationException("This openeration is not supported in local profile for now");
	}

	/**
	 * Not supported in the local profile.
	 *
	 * @param state         unused
	 * @param sessionState  unused
	 * @param code          unused
	 * @param stateCookie   unused
	 * @param redirectURI   unused
	 * @return never
	 */
	@Override
	public AccessTokenResponseDTO loginRedirect(String state, String sessionState, String code, String stateCookie,
			String redirectURI) {
		throw new UnsupportedOperationException("This openeration is not supported in local profile for now");
	}

	/**
	 * Not supported in the local profile.
	 *
	 * @param redirectURI unused
	 * @param state       unused
	 * @return never
	 */
	@Override
	public String getKeycloakURI(String redirectURI, String state) {
		throw new UnsupportedOperationException("This openeration is not supported in local profile for now");
	}

	/**
	 * Password-grant style local proxy token using the login user name as subject.
	 *
	 * @param loginUser credentials (only username is used)
	 * @return proxy tokens
	 * @throws Exception never thrown
	 */
	@Override
	public AuthNResponseDto authenticateUser(LoginUserWithClientId loginUser) throws Exception {
		return proxyTokenForLocalEnv(loginUser.getUserName(), AuthConstant.SUCCESS_STATUS,
				AuthConstant.USERPWD_SUCCESS_MESSAGE);
	}

	/**
	 * Not supported in the local profile.
	 *
	 * @param userId unused
	 * @param appId  unused
	 * @return never
	 */
	@Override
	public IndividualIdDto getIndividualIdBasedOnUserID(String userId, String appId) {
		throw new UnsupportedOperationException("This openeration is not supported in local profile for now");
	}

	/**
	 * Not supported in the local profile.
	 *
	 * @param realmId   unused
	 * @param roleName  unused
	 * @param pageStart unused
	 * @param pageFetch unused
	 * @param email     unused
	 * @param firstName unused
	 * @param lastName  unused
	 * @param username  unused
	 * @param search    unused
	 * @return never
	 */
	@Override
	public MosipUserListDto getListOfUsersDetails(String realmId, String roleName, int pageStart, int pageFetch,
			String email, String firstName, String lastName, String username, String search) {
		throw new UnsupportedOperationException("This openeration is not supported");
	}

}
