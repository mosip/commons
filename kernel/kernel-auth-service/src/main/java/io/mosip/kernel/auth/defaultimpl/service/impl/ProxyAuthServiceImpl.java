package io.mosip.kernel.auth.defaultimpl.service.impl;

import java.util.List;

import io.mosip.kernel.core.authmanager.model.*;
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
import io.mosip.kernel.auth.defaultimpl.constant.AuthErrorCode;
import io.mosip.kernel.auth.defaultimpl.exception.AuthManagerException;
import io.mosip.kernel.auth.defaultimpl.repository.UserStoreFactory;
import io.mosip.kernel.auth.defaultimpl.repository.impl.KeycloakImpl;
import io.mosip.kernel.auth.defaultimpl.service.OTPService;
import io.mosip.kernel.auth.defaultimpl.service.TokenService;
import io.mosip.kernel.auth.defaultimpl.service.UinService;
import io.mosip.kernel.auth.defaultimpl.util.ProxyTokenGenerator;
import io.mosip.kernel.auth.defaultimpl.util.TokenGenerator;
import io.mosip.kernel.auth.defaultimpl.util.TokenValidator;
import io.mosip.kernel.core.authmanager.spi.AuthService;

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

	private static final Logger logger = LoggerFactory.getLogger(ProxyAuthServiceImpl.class);

	private static final String CLIENTID_AND_TOKEN_COMBINATION_HAD_BEEN_VALIDATED_SUCCESSFULLY = "Clientid and Token combination had been validated successfully";

	private static final String LOG_OUT_FAILED = "log out failed";

	private static final String FAILED = "Failed";

	private static final String SUCCESS = "Success";

	private static final String SUCCESSFULLY_LOGGED_OUT = "successfully loggedout";

	@Autowired
	private ProxyTokenGenerator proxyTokenGenarator;

	@Value("${mosip.iam.open-id-url}")
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

	@Value("${mosip.iam.open-id-url}")
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

	@Value("${mosip.iam.authorization_endpoint}")
	private String authorizationEndpoint;

	@Value("${mosip.iam.token_endpoint}")
	private String tokenEndpoint;

	@Value("${mosip.admin_realm_id}")
	private String realmID;

	@Value("${spring.profiles.active}")
	String activeProfile;

	@Value("${auth.local.exp:1000000}")
	long localExp;

	@Value("${auth.local.secret:secret}")
	String localSecret;

	@Qualifier("authRestTemplate")
	@Autowired
	private RestTemplate authRestTemplate;

	@Value("${mosip.kernel.auth.proxy-otp}")
	private boolean proxyOtp;

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

	@Deprecated
	@Override
	public MosipUserTokenDto validateToken(String token) throws Exception {
		throw new UnsupportedOperationException("This openeration is not supported");
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
		return proxyTokenForLocalEnv(loginUser.getUserName(), AuthConstant.SUCCESS_STATUS,
				AuthConstant.USERPWD_SUCCESS_MESSAGE);
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
		mosipUser = new MosipUserDto();
		mosipUser.setUserId(userOtp.getUserId());
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
			authNResponseDto.setRefreshExpiryTime(mosipToken.getExpTime());
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
		return proxyTokenForLocalEnv(clientSecret.getClientId(), SUCCESS,
				CLIENTID_AND_TOKEN_COMBINATION_HAD_BEEN_VALIDATED_SUCCESSFULLY);
	}

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
		throw new UnsupportedOperationException("This openeration is not supported in local profile for now");
	}

	@Override
	public RolesListDto getAllRoles(String appId) {
		throw new UnsupportedOperationException("This openeration is not supported in local profile for now");
	}

	@Override
	public MosipUserListDto getListOfUsersDetails(List<String> userDetails, String appId) throws Exception {
		throw new UnsupportedOperationException("This openeration is not supported in local profile for now");
	}

	@Override
	public MosipUserSaltListDto getAllUserDetailsWithSalt(List<String> userDetails, String appId) throws Exception {
		throw new UnsupportedOperationException("This openeration is not supported in local profile for now");
	}

	@Override
	public RIdDto getRidBasedOnUid(String userId, String appId) throws Exception {
		throw new UnsupportedOperationException("This openeration is not supported in local profile for now");
	}

	@Override
	public AuthZResponseDto unBlockUser(String userId, String appId) throws Exception {
		throw new UnsupportedOperationException("This openeration is not supported in local profile for now");
	}

	@Override
	public AuthZResponseDto changePassword(String appId, PasswordDto passwordDto) throws Exception {
		throw new UnsupportedOperationException("This openeration is not supported in local profile for now");
	}

	@Override
	public AuthZResponseDto resetPassword(String appId, PasswordDto passwordDto) throws Exception {
		throw new UnsupportedOperationException("This openeration is not supported in local profile for now");
	}

	@Override
	public UserNameDto getUserNameBasedOnMobileNumber(String appId, String mobileNumber) throws Exception {
		throw new UnsupportedOperationException("This openeration is not supported in local profile for now");

	}

	@Override
	public MosipUserDto registerUser(UserRegistrationRequestDto userCreationRequestDto) {
		throw new UnsupportedOperationException("This openeration is not supported in local profile for now");
	}

	@Override
	public UserPasswordResponseDto addUserPassword(UserPasswordRequestDto userPasswordRequestDto) {
		throw new UnsupportedOperationException("This openeration is not supported in local profile for now");
	}

	@Override
	public UserRoleDto getUserRole(String appId, String userId) throws Exception {
		throw new UnsupportedOperationException("This openeration is not supported in local profile for now");
	}

	@Override
	public MosipUserDto getUserDetailBasedonMobileNumber(String appId, String mobileNumber) throws Exception {
		throw new UnsupportedOperationException("This openeration is not supported in local profile for now");
	}

	@Override
	public ValidationResponseDto validateUserName(String appId, String userName) {
		throw new UnsupportedOperationException("This openeration is not supported in local profile for now");
	}

	@Override
	public UserDetailsResponseDto getUserDetailBasedOnUserId(String appId, List<String> userIds) {
		throw new UnsupportedOperationException("This openeration is not supported in local profile for now");
	}

	@Override
	public MosipUserDto valdiateToken(String token) {
		// this will verify token
		DecodedJWT decodedJWT = JWT.require(Algorithm.none()).build().verify(token);
		MosipUserDto mosipUserDto = new MosipUserDto();
		String user = decodedJWT.getSubject();
		mosipUserDto.setToken(token);
		mosipUserDto.setMail(decodedJWT.getClaim(AuthConstant.EMAIL).asString());
		mosipUserDto.setMobile(decodedJWT.getClaim(AuthConstant.MOBILE).asString());
		mosipUserDto.setRole(decodedJWT.getClaim(AuthConstant.ROLES).asString());
		mosipUserDto.setName(user);
		mosipUserDto.setUserId(user);
		return mosipUserDto;
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

	@Override
	public AccessTokenResponseDTO loginRedirect(String state, String sessionState, String code, String stateCookie,
			String redirectURI) {
		throw new UnsupportedOperationException("This openeration is not supported in local profile for now");
	}

	@Override
	public String getKeycloakURI(String redirectURI, String state) {
		throw new UnsupportedOperationException("This openeration is not supported in local profile for now");
	}

	@Override
	public AuthNResponseDto authenticateUser(LoginUserWithClientId loginUser) throws Exception {
		return proxyTokenForLocalEnv(loginUser.getUserName(), AuthConstant.SUCCESS_STATUS,
				AuthConstant.USERPWD_SUCCESS_MESSAGE);
	}

}
