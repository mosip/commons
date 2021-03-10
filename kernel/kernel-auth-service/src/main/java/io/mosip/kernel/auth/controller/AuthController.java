package io.mosip.kernel.auth.controller;

import java.io.IOException;
import java.util.Objects;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import io.mosip.kernel.core.authmanager.model.*;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.web.authentication.www.NonceExpiredException;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import io.mosip.kernel.auth.defaultimpl.config.MosipEnvironment;
import io.mosip.kernel.auth.defaultimpl.constant.AuthConstant;
import io.mosip.kernel.auth.defaultimpl.constant.AuthErrorCode;
import io.mosip.kernel.auth.defaultimpl.dto.ClientSecretDto;
import io.mosip.kernel.auth.defaultimpl.dto.UserDetailsRequestDto;
import io.mosip.kernel.auth.defaultimpl.dto.UserRegistrationResponseDto;
import io.mosip.kernel.auth.defaultimpl.exception.AuthManagerException;
import io.mosip.kernel.core.authmanager.spi.AuthService;
import io.mosip.kernel.core.http.RequestWrapper;
import io.mosip.kernel.core.http.ResponseFilter;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.swagger.annotations.Api;

/**
 * Controller APIs for Authentication and Authorization
 * 
 * @author Ramadurai Pandian
 * @since 1.0.0
 *
 */

@CrossOrigin
@RestController
@Api(value = "Operation related to Authentication and Authorization", tags = { "authmanager" })
public class AuthController {

	private static Logger LOGGER = LoggerFactory.getLogger(AuthController.class);

    
	@Value("${mosip.security.secure-cookie:false}")
	private boolean isSecureCookie;
	
	
	@Value("${mosip.kernel.auth-code-url-splitter:#URISPLITTER#}")
	private String urlSplitter;

	/**
	 * Autowired reference for {@link MosipEnvironment}
	 */

	@Autowired
	private MosipEnvironment mosipEnvironment;

	/**
	 * Autowired reference for {@link AuthService}
	 */

	@Autowired
	private AuthService authService;

	/**
	 * API to authenticate using userName and password
	 * 
	 * request is of type {@link LoginUser}
	 * 
	 * @return ResponseEntity Cookie value with Auth token
	 */
	@Deprecated
	@ResponseFilter
	@PostMapping(value = "/authenticate/useridPwd")
	public ResponseWrapper<AuthNResponse> authenticateUseridPwd(@RequestBody @Valid RequestWrapper<LoginUser> request,
			HttpServletResponse res) throws Exception {
		ResponseWrapper<AuthNResponse> responseWrapper = new ResponseWrapper<>();
		AuthNResponse authNResponse = null;
		AuthNResponseDto authResponseDto = authService.authenticateUser(request.getRequest());
		if (authResponseDto != null) {
			LOGGER.info("Authentication for " + request.getRequest().getUserName() + " status " + authResponseDto.getStatus());
			Cookie cookie = createCookie(authResponseDto.getToken(), mosipEnvironment.getTokenExpiry());
			authNResponse = new AuthNResponse();
			res.addHeader(mosipEnvironment.getAuthTokenHeader(), authResponseDto.getToken());
			res.addCookie(cookie);
			authNResponse.setStatus(authResponseDto.getStatus());
			authNResponse.setMessage(authResponseDto.getMessage());

		}
		else {
			LOGGER.info("Authentication failed for " + request.getRequest().getUserName());
		}
		responseWrapper.setResponse(authNResponse);
		return responseWrapper;
	}

	private Cookie createCookie(final String content, final int expirationTimeSeconds) {
		final Cookie cookie = new Cookie(mosipEnvironment.getAuthTokenHeader(), content);
		cookie.setMaxAge(expirationTimeSeconds);
		cookie.setHttpOnly(true);
		cookie.setSecure(isSecureCookie);
		cookie.setPath("/");
		return cookie;
	}

	/**
	 * API to send OTP
	 * 
	 * otpUser is of type {@link OtpUser}
	 * 
	 * @return ResponseEntity with OTP Sent message
	 */
	@ResponseFilter
	@PostMapping(value = "/authenticate/sendotp")
	@ResponseStatus(value = HttpStatus.OK)
	public ResponseWrapper<AuthNResponse> sendOTP(@RequestBody @Valid RequestWrapper<OtpUser> otpUserDto)
			throws Exception {
		ResponseWrapper<AuthNResponse> responseWrapper = new ResponseWrapper<>();
		AuthNResponse authNResponse = null;
		AuthNResponseDto authResponseDto = authService.authenticateWithOtp(otpUserDto.getRequest());
		if (authResponseDto != null) {
			LOGGER.info("Send OTP for user " + otpUserDto.getRequest().getUserId() + " status " + authResponseDto.getStatus());
			authNResponse = new AuthNResponse();
			authNResponse.setStatus(authResponseDto.getStatus());
			authNResponse.setMessage(authResponseDto.getMessage());
		}
		else {
			LOGGER.info("Send OTP failed for " + otpUserDto.getRequest().getUserId());
		}
		responseWrapper.setResponse(authNResponse);
		return responseWrapper;
	}

	/**
	 * API to validate OTP with user Id
	 * 
	 * userOtp is of type {@link UserOtp}
	 * 
	 * @return ResponseEntity with Cookie value with Auth token
	 */
	@ResponseFilter
	@PostMapping(value = "/authenticate/useridOTP")
	public ResponseWrapper<AuthNResponse> userIdOTP(@RequestBody @Valid RequestWrapper<UserOtp> userOtpDto,
			HttpServletResponse res) throws Exception {
		ResponseWrapper<AuthNResponse> responseWrapper = new ResponseWrapper<>();
		AuthNResponse authNResponse = null;
		AuthNResponseDto authResponseDto = authService.authenticateUserWithOtp(userOtpDto.getRequest());
		if (authResponseDto != null && authResponseDto.getToken() != null) {
			LOGGER.info("useridOTP for user " + userOtpDto.getRequest().getUserId() + " status " + authResponseDto.getStatus());
			Cookie cookie = createCookie(authResponseDto.getToken(), mosipEnvironment.getTokenExpiry());
			authNResponse = new AuthNResponse();
			res.addHeader(mosipEnvironment.getAuthTokenHeader(), authResponseDto.getToken());
			res.addCookie(cookie);
			authNResponse.setStatus(authResponseDto.getStatus());
			authNResponse.setMessage(authResponseDto.getMessage());
		} else if (authResponseDto != null ){
			LOGGER.info("useridOTP null for user " + userOtpDto.getRequest().getUserId() + " status " + authResponseDto.getStatus());
			authNResponse = new AuthNResponse();
			authNResponse.setStatus(authResponseDto.getStatus());
			authNResponse.setMessage(
					authResponseDto.getMessage() != null ? authResponseDto.getMessage() : "Otp validation failed");
		}
		else {
			LOGGER.error("useridOTP auth response null for user " + userOtpDto.getRequest().getUserId() );
		}
		responseWrapper.setResponse(authNResponse);
		return responseWrapper;
	}

	/**
	 * API to authenticate using clientId and secretKey
	 * 
	 * clientSecretDto is of type {@link ClientSecretDto}
	 * 
	 * @return ResponseEntity with Cookie value with Auth token
	 */
	@ResponseFilter
	@PostMapping(value = "/authenticate/clientidsecretkey")
	public ResponseWrapper<AuthNResponse> clientIdSecretKey(
			@RequestBody @Valid RequestWrapper<ClientSecret> clientSecretDto, HttpServletResponse res)
			throws Exception {
		ResponseWrapper<AuthNResponse> responseWrapper = new ResponseWrapper<>();
		AuthNResponse authNResponse = null;
		AuthNResponseDto authResponseDto = authService.authenticateWithSecretKey(clientSecretDto.getRequest());
		if (authResponseDto != null) {
			LOGGER.info("clientidsecretkey for user " + clientSecretDto.getRequest().getClientId() + " status " + authResponseDto.getStatus());
			Cookie cookie = createCookie(authResponseDto.getToken(), mosipEnvironment.getTokenExpiry());
			authNResponse = new AuthNResponse();
			res.addHeader(mosipEnvironment.getAuthTokenHeader(), authResponseDto.getToken());
			res.addCookie(cookie);
			authNResponse.setStatus(authResponseDto.getStatus());
			authNResponse.setMessage(authResponseDto.getMessage());			
		}
		else {
			LOGGER.info("clientidsecretkey null for user " + clientSecretDto.getRequest().getClientId());
		}
		
		responseWrapper.setResponse(authNResponse);
		return responseWrapper;
	}

	/**
	 * API to validate token
	 * 
	 * 
	 * @return ResponseEntity with MosipUserDto
	 */
	@ResponseFilter
	@PostMapping(value = "/authorize/validateToken")
	public ResponseWrapper<MosipUserDto> validateToken(HttpServletRequest request, HttpServletResponse res)
			throws AuthManagerException, Exception {
		ResponseWrapper<MosipUserDto> responseWrapper = new ResponseWrapper<>();
		String authToken = null;
		Cookie[] cookies = request.getCookies();
		if (cookies == null) {
			throw new AuthManagerException(AuthErrorCode.COOKIE_NOTPRESENT_ERROR.getErrorCode(),
					AuthErrorCode.COOKIE_NOTPRESENT_ERROR.getErrorMessage());
		}
		MosipUserTokenDto mosipUserDtoToken = null;
		try {
			for (Cookie cookie : cookies) {
				if (cookie.getName().contains(AuthConstant.AUTH_COOOKIE_HEADER)) {
					authToken = cookie.getValue();
				}
			}
			if (authToken == null) {
				throw new AuthManagerException(AuthErrorCode.TOKEN_NOTPRESENT_ERROR.getErrorCode(),
						AuthErrorCode.TOKEN_NOTPRESENT_ERROR.getErrorMessage());
			}
			mosipUserDtoToken = authService.validateToken(authToken);			
			if (mosipUserDtoToken != null) {
				LOGGER.info("token expiry time: " + mosipUserDtoToken.getExpTime() + " token payload: " + mosipUserDtoToken.getToken().split("\\.")[1]);
				mosipUserDtoToken.setMessage(AuthConstant.TOKEN_SUCCESS_MESSAGE);
				Cookie cookie = createCookie(mosipUserDtoToken.getToken(), mosipEnvironment.getTokenExpiry());
				res.addCookie(cookie);
				responseWrapper.setResponse(mosipUserDtoToken.getMosipUserDto());
			}
			
		} catch (NonceExpiredException exp) {
			throw new AuthManagerException(AuthErrorCode.UNAUTHORIZED.getErrorCode(), exp.getMessage());
		}		
		return responseWrapper;
	}

	/**
	 * API to validate token
	 * 
	 * 
	 * @return ResponseEntity with MosipUserDto
	 * @throws IOException
	 * @throws JsonMappingException
	 * @throws JsonParseException
	 */
	@ResponseFilter
	@GetMapping(value = "/authorize/admin/validateToken")
	public ResponseWrapper<MosipUserDto> validateAdminToken(HttpServletRequest request, HttpServletResponse res) {
		String authToken = null;
		Cookie[] cookies = request.getCookies();
		if (cookies == null) {
			throw new AuthManagerException(AuthErrorCode.COOKIE_NOTPRESENT_ERROR.getErrorCode(),
					AuthErrorCode.COOKIE_NOTPRESENT_ERROR.getErrorMessage());
		}
		MosipUserDto mosipUserDto = null;
		try {
			for (Cookie cookie : cookies) {
				if (cookie.getName().contains(AuthConstant.AUTH_COOOKIE_HEADER)) {
					authToken = cookie.getValue();
				}
			}
			if (authToken == null) {
				throw new AuthManagerException(AuthErrorCode.TOKEN_NOTPRESENT_ERROR.getErrorCode(),
						AuthErrorCode.TOKEN_NOTPRESENT_ERROR.getErrorMessage());
			}

			mosipUserDto = authService.valdiateToken(authToken);
			LOGGER.debug("validate admin token successful " + " token payload: " + mosipUserDto.getToken().split("\\.")[1]);
			Cookie cookie = createCookie(mosipUserDto.getToken(), mosipEnvironment.getTokenExpiry());
			res.addCookie(cookie);
		} catch (NonceExpiredException exp) {
			throw new AuthManagerException(AuthErrorCode.UNAUTHORIZED.getErrorCode(), exp.getMessage());
		}
		ResponseWrapper<MosipUserDto> responseWrapper = new ResponseWrapper<>();
		responseWrapper.setResponse(mosipUserDto);
		return responseWrapper;
	}

	/**
	 * API to retry token when auth token expires
	 * 
	 * 
	 * @return ResponseEntity with MosipUserDto
	 */
	@ResponseFilter
	@PostMapping(value = "/authorize/refreshToken/{appid}")
	public ResponseWrapper<AuthNResponse> refreshToken(@PathVariable("appid") String appId,@RequestBody RefreshTokenRequest refreshTokenRequest,
			HttpServletRequest request, HttpServletResponse res) throws Exception {
		ResponseWrapper<AuthNResponse> responseWrapper = new ResponseWrapper<>();
		String refreshToken = null;
		Cookie[] cookies = request.getCookies();
		for (Cookie cookie : cookies) {
			if (cookie.getName().contains(AuthConstant.REFRESH_TOKEN)) {
				refreshToken = cookie.getValue();
				LOGGER.info("refresh token for app " + appId + " from cookie " + cookie.getName());
			}
		}
		Objects.requireNonNull(refreshToken, "No refresh token cookie found");		
		RefreshTokenResponse mosipUserDtoToken = authService.refreshToken(appId,refreshToken, refreshTokenRequest);
		LOGGER.info("New refresh token obtained for app " + appId + " expires (access token) by " + mosipUserDtoToken.getAccessTokenExpTime() + " refresh token expires in " + mosipUserDtoToken.getRefreshTokenExpTime() );
		Cookie cookie = createCookie(mosipUserDtoToken.getAccesstoken(), mosipEnvironment.getTokenExpiry());
		res.addCookie(cookie);
		res.addCookie(new Cookie("refresh_token", mosipUserDtoToken.getRefreshToken()));
		responseWrapper.setResponse(mosipUserDtoToken.getAuthNResponse());
		return responseWrapper;
	}

	/**
	 * API to invalidate token when both refresh and auth token expires
	 * 
	 * 
	 * @return ResponseEntity with MosipUserDto
	 */
	@ResponseFilter
	@PostMapping(value = "/authorize/invalidateToken")
	public ResponseWrapper<AuthNResponse> invalidateToken(HttpServletRequest request, HttpServletResponse res)
			throws Exception {
		ResponseWrapper<AuthNResponse> responseWrapper = new ResponseWrapper<>();
		String authToken = null;
		Cookie[] cookies = request.getCookies();
		if (cookies == null) {
			throw new AuthManagerException(AuthErrorCode.COOKIE_NOTPRESENT_ERROR.getErrorCode(),
					AuthErrorCode.COOKIE_NOTPRESENT_ERROR.getErrorMessage());
		}
		for (Cookie cookie : cookies) {
			if (cookie.getName().contains(AuthConstant.AUTH_COOOKIE_HEADER)) {
				authToken = cookie.getValue();
				LOGGER.info("Attempt to invalidate the token from cookie  " + cookie.getName() );
			}
		}
		if (authToken == null) {
			throw new AuthManagerException(AuthErrorCode.TOKEN_NOTPRESENT_ERROR.getErrorCode(),
					AuthErrorCode.TOKEN_NOTPRESENT_ERROR.getErrorMessage());
		}
		AuthNResponse authNResponse = authService.invalidateToken(authToken);
		LOGGER.info("Invalidated the token  " + authToken );
		responseWrapper.setResponse(authNResponse);
		return responseWrapper;
	}

	@ResponseFilter
	@GetMapping(value = "/roles/{appid}")
	public ResponseWrapper<RolesListDto> getAllRoles(@PathVariable("appid") String appId) throws Exception {
		ResponseWrapper<RolesListDto> responseWrapper = new ResponseWrapper<>();
		RolesListDto rolesListDto = authService.getAllRoles(appId);
		LOGGER.info("Get roles for " + appId + ". Total roles:  " + rolesListDto.getRoles().size() );
		responseWrapper.setResponse(rolesListDto);
		return responseWrapper;
	}

	@ResponseFilter
	@PostMapping(value = "/userdetails/{appid}")
	public ResponseWrapper<MosipUserListDto> getListOfUsersDetails(
			@RequestBody RequestWrapper<UserDetailsRequestDto> userDetails, @PathVariable("appid") String appId)
			throws Exception {
		ResponseWrapper<MosipUserListDto> responseWrapper = new ResponseWrapper<>();
		MosipUserListDto mosipUsers = authService.getListOfUsersDetails(userDetails.getRequest().getUserDetails(),
				appId);
		LOGGER.info("Get userdetails for " + appId + ". Total users:  " + mosipUsers.getMosipUserDtoList().size() );
		responseWrapper.setResponse(mosipUsers);
		return responseWrapper;
	}

	@ResponseFilter
	@PostMapping(value = "/usersaltdetails/{appid}")
	public ResponseWrapper<MosipUserSaltListDto> getUserDetailsWithSalt(
			@RequestBody RequestWrapper<UserDetailsRequestDto> userDetails, @PathVariable("appid") String appId)
			throws Exception {
		ResponseWrapper<MosipUserSaltListDto> responseWrapper = new ResponseWrapper<>();
		MosipUserSaltListDto mosipUsers = authService
				.getAllUserDetailsWithSalt(userDetails.getRequest().getUserDetails(), appId);
		LOGGER.info("Get usersaltdetails for " + appId + ". Total user salts:  " + mosipUsers.getMosipUserSaltList().size() );
		responseWrapper.setResponse(mosipUsers);
		return responseWrapper;
	}

	/**
	 * This API will fetch RID based on appId and userId.
	 * 
	 * @param appId  - application Id
	 * @param userId - user Id
	 * @return {@link RIdDto}
	 * @throws Exception
	 */
	@ResponseFilter
	@GetMapping(value = "rid/{appid}/{userid}")
	public ResponseWrapper<RIdDto> getRId(@PathVariable("appid") String appId, @PathVariable("userid") String userId)
			throws Exception {
		ResponseWrapper<RIdDto> responseWrapper = new ResponseWrapper<>();
		RIdDto rIdDto = authService.getRidBasedOnUid(userId, appId);
		LOGGER.info("Get rid for " + appId + ". Rid:  " + rIdDto.getRId());
		responseWrapper.setResponse(rIdDto);
		return responseWrapper;
	}

	/**
	 * Fetch username based on the user id.
	 * 
	 * @param appId  - application id
	 * @param userId - user id
	 * @return {@link UserNameDto}
	 * @throws Exception - exception is thrown if
	 */
	@ResponseFilter
	@GetMapping(value = "unblock/{appid}/{userid}")
	public ResponseWrapper<AuthZResponseDto> getUserName(@PathVariable("appid") String appId,
			@PathVariable("userid") String userId) throws Exception {
		AuthZResponseDto authZResponseDto = authService.unBlockUser(userId, appId);
		LOGGER.info("unblock user " + userId + " appid " + appId + ". Response  status " + authZResponseDto.getStatus());
		ResponseWrapper<AuthZResponseDto> responseWrapper = new ResponseWrapper<>();
		responseWrapper.setResponse(authZResponseDto);
		return responseWrapper;
	}

	/**
	 * This API will change the password of the particular user
	 * 
	 * @param appId       - applicationId
	 * @param passwordDto - {@link PasswordDto}
	 * @return {@link AuthZResponseDto}
	 * @throws Exception
	 */
	@ResponseFilter
	@PostMapping(value = "/changepassword/{appid}")
	public ResponseWrapper<AuthZResponseDto> changePassword(@PathVariable("appid") String appId,
			@RequestBody @Valid RequestWrapper<PasswordDto> passwordDto) throws Exception {
		AuthZResponseDto mosipUserDto = authService.changePassword(appId, passwordDto.getRequest());
		LOGGER.info("changepassword appid " + appId + ". Response  status " + mosipUserDto.getStatus());
		ResponseWrapper<AuthZResponseDto> responseWrapper = new ResponseWrapper<>();		
		responseWrapper.setResponse(mosipUserDto);
		return responseWrapper;
	}

	/**
	 * This API will reset the password of the particular user
	 * 
	 * @param appId       - applicationId
	 * @param passwordDto -{@link PasswordDto}
	 * @return {@link AuthZResponseDto}
	 * @throws Exception
	 */
	@ResponseFilter
	@PostMapping(value = "/resetpassword/{appid}")
	public ResponseWrapper<AuthZResponseDto> resetPassword(@PathVariable("appid") String appId,
			@RequestBody @Valid RequestWrapper<PasswordDto> passwordDto) throws Exception {
		AuthZResponseDto mosipUserDto = authService.resetPassword(appId, passwordDto.getRequest());
		LOGGER.info("resetpassword appid " + appId + ". Response  status " + mosipUserDto.getStatus());
		ResponseWrapper<AuthZResponseDto> responseWrapper = new ResponseWrapper<>();
		responseWrapper.setResponse(mosipUserDto);
		return responseWrapper;
	}

	/**
	 * 
	 * @param mobile - mobile number
	 * @param appId  - applicationId
	 * @return {@link UserNameDto}
	 * @throws Exception
	 */
	@ResponseFilter
	@GetMapping(value = "/username/{appid}/{mobilenumber}")
	public ResponseWrapper<UserNameDto> getUsernameBasedOnMobileNumber(@PathVariable("mobilenumber") String mobile,
			@PathVariable("appid") String appId) throws Exception {
		UserNameDto userNameDto = authService.getUserNameBasedOnMobileNumber(appId, mobile);
		LOGGER.info("fetch username based on mobile number - appid " + appId + ". Response  status " + userNameDto.getUserName());
		ResponseWrapper<UserNameDto> responseWrapper = new ResponseWrapper<>();
		responseWrapper.setResponse(userNameDto);
		return responseWrapper;
	}

	/**
	 * Create a user account in Data Store
	 * 
	 * @param userCreationRequestDto {@link UserRegistrationRequestDto}
	 * @return {@link UserRegistrationResponseDto}
	 */
	@ResponseFilter
	@PostMapping(value = "/user/addpassword")
	@Deprecated(forRemoval = true, since = "1.1.4")
	public ResponseWrapper<UserPasswordResponseDto> addPassword(
			@RequestBody @Valid RequestWrapper<UserPasswordRequestDto> userPasswordRequestDto) {
		ResponseWrapper<UserPasswordResponseDto> responseWrapper = new ResponseWrapper<>();
		UserPasswordResponseDto userPasswordResponseDto = authService.addUserPassword(userPasswordRequestDto.getRequest());
		if ( userPasswordResponseDto != null && userPasswordResponseDto.getUserName().equals(userPasswordRequestDto.getRequest().getUserName())  ){
			LOGGER.info("add password " + userPasswordRequestDto.getRequest().getUserName() + ". Response  status success" );
		}
		else{
			
			LOGGER.info("add password " + userPasswordRequestDto.getRequest().getUserName() + ". Response  status failed" );
		}

		responseWrapper.setResponse(userPasswordResponseDto);
		return responseWrapper;
	}

	@GetMapping("/role/{appId}/{userId}")
	@ResponseFilter
	public ResponseWrapper<UserRoleDto> getUserRole(@PathVariable("appId") String appId,
			@PathVariable("userId") String userId) throws Exception {
		ResponseWrapper<UserRoleDto> responseWrapper = new ResponseWrapper<>();
		UserRoleDto userRole = authService.getUserRole(appId, userId);
		LOGGER.info("role appid " + appId + " user " + userId + ". role " + userRole.getRole() );
		responseWrapper.setResponse(userRole);
		return responseWrapper;
	}

	/**
	 * 
	 * @param mobile - mobile number
	 * @param appId  - applicationId
	 * @return {@link MosipUserDto}
	 * @throws Exception
	 */
	@ResponseFilter
	@GetMapping(value = "/userdetail/{appid}/{mobilenumber}")
	public ResponseWrapper<MosipUserDto> getUserDetailBasedOnMobileNumber(@PathVariable("mobilenumber") String mobile,
			@PathVariable("appid") String appId) throws Exception {
		MosipUserDto mosipUserDto = authService.getUserDetailBasedonMobileNumber(appId, mobile);
		LOGGER.info("userdetail with mobile number appid " + appId + " user " + mosipUserDto.getUserId() );
		ResponseWrapper<MosipUserDto> responseWrapper = new ResponseWrapper<>();
		responseWrapper.setResponse(mosipUserDto);
		return responseWrapper;
	}

	/**
	 * 
	 * @param mobile - mobile number
	 * @param appId  - applicationId
	 * @return {@link MosipUserDto}
	 * @throws Exception
	 */
	@ResponseFilter
	@GetMapping(value = "/validate/{appid}/{userid}")
	public ResponseWrapper<ValidationResponseDto> validateUserName(@PathVariable("userid") String userId,
			@PathVariable("appid") String appId) {
		ValidationResponseDto validationResponseDto = authService.validateUserName(appId, userId);
		LOGGER.info("validate with appid " + appId + " user " + userId + ". Response " + validationResponseDto.getStatus() );
		ResponseWrapper<ValidationResponseDto> responseWrapper = new ResponseWrapper<>();
		responseWrapper.setResponse(validationResponseDto);
		return responseWrapper;
	}

	/**
	 * Gets the user detail based on user id.
	 *
	 * @param appId  the app id
	 * @param userId the user id
	 * @return {@link UserDetailsDto}
	 */
	@ResponseFilter
	@PostMapping(value = "/userdetail/regid/{appid}")
	public ResponseWrapper<UserDetailsResponseDto> getUserDetailBasedOnUserId(@PathVariable("appid") String appId,
			@RequestBody RequestWrapper<UserDetailsRequestDto> userDetails) {
		UserDetailsResponseDto userDetailsDto = authService.getUserDetailBasedOnUserId(appId,
				userDetails.getRequest().getUserDetails());
		LOGGER.info("userdetail regid with appid " + appId + " request user count " + userDetails.getRequest().getUserDetails().size() + ". Response user count " + userDetailsDto.getUserDetails().size() );
		ResponseWrapper<UserDetailsResponseDto> responseWrapper = new ResponseWrapper<>();
		responseWrapper.setResponse(userDetailsDto);
		return responseWrapper;
	}

	/**
	 * 
	 * @param req - {@link HttpServletRequest}
	 * @param res - {@link HttpServletResponse}
	 * @return {@link ResponseWrapper}
	 */
	@ResponseFilter
	@DeleteMapping(value = "/logout/user")
	public ResponseWrapper<AuthResponseDto> logoutUser(
			@CookieValue(value = "Authorization", required = false) String token, HttpServletResponse res) {
		AuthResponseDto authResponseDto = authService.logoutUser(token);
		LOGGER.info("logout user " + token + ". Response " + authResponseDto.getStatus() );
		ResponseWrapper<AuthResponseDto> responseWrapper = new ResponseWrapper<>();
		responseWrapper.setResponse(authResponseDto);
		return responseWrapper;
	}

	@Deprecated
	@GetMapping(value = "/login/{redirectURI}")
	public void login(@CookieValue("state") String state, @PathVariable("redirectURI") String redirectURI,
			HttpServletResponse res) throws IOException {
		String uri = authService.getKeycloakURI(redirectURI, state);
		LOGGER.info("redirect open id login uri " + uri );
		res.setStatus(302);
		res.sendRedirect(uri);
	}

	@Deprecated
	@GetMapping(value = "/login-redirect/{redirectURI}")
	public void loginRedirect(@PathVariable("redirectURI") String redirectURI, @RequestParam("state") String state,
			@RequestParam("session_state") String sessionState, @RequestParam("code") String code,
			@CookieValue("state") String stateCookie, HttpServletResponse res) throws IOException {
		AccessTokenResponseDTO jwtResponseDTO = authService.loginRedirect(state, sessionState, code, stateCookie,
				redirectURI);

		Cookie cookie = createCookie(jwtResponseDTO.getAccessToken(), Integer.parseInt(jwtResponseDTO.getExpiresIn()));
		res.addCookie(cookie);
		res.setStatus(302);
		String uri = new String(Base64.decodeBase64(redirectURI.getBytes()));

		LOGGER.info("login-redirect open id login uri " + uri );
		res.sendRedirect(uri);	
		}

	/**
	 * Erases Cookie from browser
	 * 
	 * @param cookie - {@link Cookie}
	 */
	private void removeCookie(Cookie cookie) {
		cookie.setValue("");
		cookie.setPath("/");
		cookie.setMaxAge(0);
	}


	/**
	 * Internal API used by syncdata delegate API
	 * @param request
	 * @param res
	 * @return
	 * @throws Exception
	 */
	@ResponseFilter
	@PostMapping(value = "/authenticate/internal/useridPwd")
	public ResponseWrapper<AuthNResponseDto> getAllAuthTokens(@RequestBody @Valid RequestWrapper<LoginUserWithClientId> request,
																HttpServletResponse res) throws Exception {
		ResponseWrapper<AuthNResponseDto> responseWrapper = new ResponseWrapper<>();
		AuthNResponseDto authResponseDto = authService.authenticateUser(request.getRequest());
		responseWrapper.setResponse(authResponseDto);
		return responseWrapper;
	}

	/**
	 * Internal API used by syncdata delegate API
	 * @param request
	 * @param res
	 * @return
	 * @throws Exception
	 */
	@ResponseFilter
	@PostMapping(value = "/authenticate/internal/userotp")
	public ResponseWrapper<AuthNResponseDto> getAllAuthTokensForOTP(@RequestBody @Valid RequestWrapper<UserOtp> request,
															  HttpServletResponse res) throws Exception {
		ResponseWrapper<AuthNResponseDto> responseWrapper = new ResponseWrapper<>();
		AuthNResponseDto authResponseDto = authService.authenticateUserWithOtp(request.getRequest());
		responseWrapper.setResponse(authResponseDto);
		return responseWrapper;
	}

	/**
	 * Internal API used by syncdata delegate API
	 * @param appId
	 * @param refreshTokenRequest
	 * @param request
	 * @param res
	 * @return
	 * @throws Exception
	 */
	@ResponseFilter
	@PostMapping(value = "/authorize/internal/refreshToken/{appid}")
	public ResponseWrapper<AuthNResponseDto> refreshAuthToken(@PathVariable("appid") String appId,@RequestBody RefreshTokenRequest refreshTokenRequest,
													   HttpServletRequest request, HttpServletResponse res) throws Exception {
		ResponseWrapper<AuthNResponseDto> responseWrapper = new ResponseWrapper<>();
		String refreshToken = null;
		Cookie[] cookies = request.getCookies();
		for (Cookie cookie : cookies) {
			if (cookie.getName().contains(AuthConstant.REFRESH_TOKEN)) {
				refreshToken = cookie.getValue();
				LOGGER.info("refresh token for app " + appId + " from cookie " + cookie.getName());
			}
		}
		Objects.requireNonNull(refreshToken, "No refresh token cookie found");
		RefreshTokenResponse mosipUserDtoToken = authService.refreshToken(appId,refreshToken, refreshTokenRequest);
		AuthNResponseDto authNResponseDto = new AuthNResponseDto();
		authNResponseDto.setToken(mosipUserDtoToken.getAccesstoken());
		authNResponseDto.setRefreshToken(mosipUserDtoToken.getRefreshToken());
		authNResponseDto.setExpiryTime(Long.parseLong(mosipUserDtoToken.getAccessTokenExpTime()));
		authNResponseDto.setRefreshExpiryTime(Long.parseLong(mosipUserDtoToken.getRefreshTokenExpTime()));
		responseWrapper.setResponse(authNResponseDto);
		return responseWrapper;
	}
}
