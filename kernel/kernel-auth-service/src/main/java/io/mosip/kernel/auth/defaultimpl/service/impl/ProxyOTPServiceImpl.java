package io.mosip.kernel.auth.defaultimpl.service.impl;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.kernel.auth.defaultimpl.config.MosipEnvironment;
import io.mosip.kernel.auth.defaultimpl.constant.AuthConstant;
import io.mosip.kernel.auth.defaultimpl.constant.AuthErrorCode;
import io.mosip.kernel.auth.defaultimpl.dto.AccessTokenResponse;
import io.mosip.kernel.auth.defaultimpl.dto.otp.OtpEmailSendResponseDto;
import io.mosip.kernel.auth.defaultimpl.dto.otp.OtpGenerateRequest;
import io.mosip.kernel.auth.defaultimpl.dto.otp.OtpGenerateResponseDto;
import io.mosip.kernel.auth.defaultimpl.dto.otp.OtpSmsSendRequestDto;
import io.mosip.kernel.auth.defaultimpl.dto.otp.OtpValidatorResponseDto;
import io.mosip.kernel.auth.defaultimpl.dto.otp.SmsResponseDto;
import io.mosip.kernel.auth.defaultimpl.dto.otp.email.OTPEmailTemplate;
import io.mosip.kernel.auth.defaultimpl.exception.AuthManagerException;
import io.mosip.kernel.auth.defaultimpl.exception.AuthManagerServiceException;
import io.mosip.kernel.auth.defaultimpl.service.OTPGenerateService;
import io.mosip.kernel.auth.defaultimpl.service.OTPService;
import io.mosip.kernel.auth.defaultimpl.service.TokenGenerationService;
import io.mosip.kernel.auth.defaultimpl.util.OtpValidator;
import io.mosip.kernel.auth.defaultimpl.util.ProxyTokenGenerator;
import io.mosip.kernel.auth.defaultimpl.util.TemplateUtil;
import io.mosip.kernel.core.authmanager.exception.AuthNException;
import io.mosip.kernel.core.authmanager.exception.AuthZException;
import io.mosip.kernel.core.authmanager.model.AuthNResponseDto;
import io.mosip.kernel.core.authmanager.model.MosipUserDto;
import io.mosip.kernel.core.authmanager.model.MosipUserTokenDto;
import io.mosip.kernel.core.authmanager.model.OtpUser;
import io.mosip.kernel.core.exception.ExceptionUtils;
import io.mosip.kernel.core.exception.ServiceError;
import io.mosip.kernel.core.http.RequestWrapper;
import io.mosip.kernel.core.http.ResponseWrapper;

/**
 * Local-profile OTP service ({@code local}): can skip real OTP manager when
 * {@code mosip.kernel.auth.proxy-otp} is true and issues {@link ProxyTokenGenerator}
 * tokens on validate.
 *
 * @author Urvil Joshi
 *
 */

@Profile("local")
@Service
public class ProxyOTPServiceImpl implements OTPService {

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.mosip.kernel.auth.service.OTPService#sendOTP(io.mosip.kernel.auth.
	 * entities.MosipUserDto, java.lang.String)
	 */

	/**
	 * RestTemplate for OTP manager and notification APIs.
	 */
	@Qualifier("authRestTemplate")
	@Autowired
	RestTemplate restTemplate;

	/**
	 * OTP generate/verify and sender API URLs.
	 */
	@Autowired
	MosipEnvironment mosipEnvironment;

	/**
	 * OTP manager generate client.
	 */
	@Autowired
	OTPGenerateService oTPGenerateService;

	/**
	 * Mapper for MOSIP response wrappers.
	 */
	@Autowired
	private ObjectMapper mapper;

	/**
	 * Internal token generation (unused on some proxy paths).
	 */
	@Autowired
	private TokenGenerationService tokenService;

	/**
	 * Email/SMS template merge from masterdata.
	 */
	@Autowired
	private TemplateUtil templateUtil;

	/**
	 * Validates required OTP request fields.
	 */
	@Autowired
	private OtpValidator authOtpValidator;

	/**
	 * Keycloak OpenID URL template.
	 */
	@Value("${mosip.iam.open-id-url}")
	private String keycloakOpenIdUrl;

	/**
	 * Default realm id.
	 */
	@Value("${mosip.iam.default.realm-id}")
	private String realmId;

	/**
	 * Authmanager OAuth client id.
	 */
	@Value("${mosip.kernel.auth.client.id}")
	private String authClientID;

	/**
	 * Pre-registration OAuth client id.
	 */
	@Value("${mosip.kernel.prereg.client.id}")
	private String preregClientId;

	/**
	 * Pre-registration OAuth secret.
	 */
	@Value("${mosip.kernel.prereg.secret.key}")
	private String preregSecretKey;

	/**
	 * Authmanager OAuth secret.
	 */
	@Value("${mosip.kernel.auth.secret.key}")
	private String authSecret;

	/**
	 * IDA OAuth client id.
	 */
	@Value("${mosip.kernel.ida.client.id}")
	private String idaClientID;

	/**
	 * IDA OAuth secret.
	 */
	@Value("${mosip.kernel.ida.secret.key}")
	private String idaSecret;

	/**
	 * Admin OAuth client id.
	 */
	@Value("${mosip.admin.clientid}")
	private String mosipAdminClientID;

	/**
	 * Admin OAuth secret.
	 */
	@Value("${mosip.admin.clientsecret}")
	private String mosipAdminSecret;

	/**
	 * Default pre-reg user password.
	 */
	@Value("${mosip.iam.pre-reg_user_password}")
	private String preRegUserPassword;

	/**
	 * Pre-registration realm id.
	 */
	@Value("${mosip.kernel.prereg.realm-id}")
	private String preregRealmId;

	/**
	 * Active Spring profile name.
	 */
	@Value("${spring.profiles.active}")
	String activeProfile;

	/**
	 * Local proxy token expiry offset in millis.
	 */
	@Value("${auth.local.exp:1000000}")
	long localExp;

	/**
	 * When true, OTP send/validate is stubbed for local development.
	 */
	@Value("${mosip.kernel.auth.proxy-otp}")
	private boolean proxyOtp;

	/**
	 * Unsigned local JWT issuer.
	 */
	@Autowired
	private ProxyTokenGenerator proxyTokenGenerator;




	/**
	 * POSTs the SMS notification payload to the configured sender API.
	 *
	 * @param message SMS body
	 * @param mobile  destination number
	 * @param token   internal auth cookie token
	 * @return SMS send result
	 */
	private SmsResponseDto sendOtpBySms(String message, String mobile, String token) {
		try {
			List<ServiceError> validationErrorsList = null;
			OtpSmsSendRequestDto otpSmsSendRequestDto = new OtpSmsSendRequestDto(mobile, message);
			SmsResponseDto otpSmsSendResponseDto = null;
			String url = mosipEnvironment.getOtpSenderSmsApi();
			RequestWrapper<OtpSmsSendRequestDto> reqWrapper = new RequestWrapper<>();
			reqWrapper.setRequesttime(LocalDateTime.now());
			reqWrapper.setRequest(otpSmsSendRequestDto);
			HttpHeaders headers = new HttpHeaders();
			headers.set(AuthConstant.COOKIE, AuthConstant.AUTH_HEADER + token);
			ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST,
					new HttpEntity<Object>(reqWrapper, headers), String.class);
			validationErrorsList = ExceptionUtils.getServiceErrorList(response.getBody());
			if (!validationErrorsList.isEmpty()) {
				throw new AuthManagerServiceException(validationErrorsList);
			}
			ResponseWrapper<?> responseObject;
			try {
				responseObject = mapper.readValue(response.getBody(), ResponseWrapper.class);
				otpSmsSendResponseDto = mapper.readValue(mapper.writeValueAsString(responseObject.getResponse()),
						SmsResponseDto.class);
			} catch (Exception e) {
				throw new AuthManagerException(String.valueOf(HttpStatus.UNAUTHORIZED.value()), e.getMessage());
			}
			return otpSmsSendResponseDto;
		} catch (HttpClientErrorException | HttpServerErrorException e) {
			String errmessage = e.getResponseBodyAsString();
			throw new AuthManagerException(String.valueOf(HttpStatus.UNAUTHORIZED.value()), errmessage);
		}
	}

	/**
	 * Validates OTP or, when {@link #proxyOtp} is true, returns a proxy token.
	 *
	 * @param mosipUser user identity
	 * @param otp       OTP value
	 * @param appId     application / realm context
	 * @return token DTO
	 */
	@Override
	public MosipUserTokenDto validateOTP(MosipUserDto mosipUser, String otp, String appId) {
		String key = new OtpGenerateRequest(mosipUser).getKey();
		MosipUserTokenDto mosipUserDtoToken = null;
		ResponseEntity<String> response = null;
		final String url = mosipEnvironment.getVerifyOtpUserApi();
		String token = null;
		AccessTokenResponse accessTokenResponse = null;
		AccessTokenResponse responseAccessTokenResponse = null;
		String realm = appId.equalsIgnoreCase("preregistration") ? appId : realmId;
		try {
			accessTokenResponse = new AccessTokenResponse();
			long exp = System.currentTimeMillis() + localExp;
			token = proxyTokenGenerator.getProxyToken("AUTH", exp);
			accessTokenResponse.setAccess_token(token);
			accessTokenResponse.setExpires_in(exp + "");

		} catch (Exception e) {
			throw new AuthManagerException(String.valueOf(HttpStatus.UNAUTHORIZED.value()), e.getMessage(), e);
		}
		UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url).queryParam("key", key).queryParam("otp",
				otp);
		HttpHeaders headers = new HttpHeaders();
		headers.set(AuthConstant.COOKIE, AuthConstant.AUTH_HEADER + token);
		response = restTemplate.exchange(builder.toUriString(), HttpMethod.GET, new HttpEntity<Object>(headers),
				String.class);
		if (response.getStatusCode().equals(HttpStatus.OK)) {
			String responseBody = response.getBody();
			List<ServiceError> validationErrorsList = null;
			validationErrorsList = ExceptionUtils.getServiceErrorList(responseBody);

			if (!validationErrorsList.isEmpty()) {
				throw new AuthManagerServiceException(validationErrorsList);
			}
			responseAccessTokenResponse = new AccessTokenResponse();
			long exp = System.currentTimeMillis() + localExp;
			responseAccessTokenResponse = new AccessTokenResponse();
			responseAccessTokenResponse.setAccess_token(proxyTokenGenerator.getProxyToken(mosipUser.getUserId(), exp));
			responseAccessTokenResponse.setRefresh_token(proxyTokenGenerator.getProxyToken(mosipUser.getUserId(), exp));
			responseAccessTokenResponse.setExpires_in(exp + "");
			OtpValidatorResponseDto otpResponse = null;
			ResponseWrapper<?> responseObject;
			try {
				responseObject = mapper.readValue(response.getBody(), ResponseWrapper.class);
				otpResponse = mapper.readValue(mapper.writeValueAsString(responseObject.getResponse()),
						OtpValidatorResponseDto.class);
			} catch (Exception e) {
				throw new AuthManagerException(String.valueOf(HttpStatus.UNAUTHORIZED.value()), e.getMessage(), e);
			}
			if (otpResponse.getStatus() != null && otpResponse.getStatus().equals("success")) {
				String expTime = accessTokenResponse.getExpires_in();
				mosipUserDtoToken = new MosipUserTokenDto(mosipUser, responseAccessTokenResponse.getAccess_token(),
						responseAccessTokenResponse.getRefresh_token(), Long.parseLong(expTime), null, null,
						Long.parseLong(expTime));
				mosipUserDtoToken.setMessage(otpResponse.getMessage());
				mosipUserDtoToken.setStatus(otpResponse.getStatus());
			} else {
				mosipUserDtoToken = new MosipUserTokenDto();
				mosipUserDtoToken.setMessage(otpResponse.getMessage());
				mosipUserDtoToken.setStatus(otpResponse.getStatus());
			}

		}
		return mosipUserDtoToken;
	}

	/**
	 * Sends OTP for UIN identities, or stubs success when {@link #proxyOtp} is true.
	 *
	 * @param mosipUser user with mail/mobile
	 * @param otpUser   channels and context
	 * @param appId     application / realm context
	 * @return send status
	 */
	@Override
	public AuthNResponseDto sendOTPForUin(MosipUserDto mosipUser, OtpUser otpUser, String appId) {
		AuthNResponseDto authNResponseDto = null;
		OtpEmailSendResponseDto otpEmailSendResponseDto = null;
		SmsResponseDto otpSmsSendResponseDto = null;
		String mobileMessage = null;
		OTPEmailTemplate emailTemplate = null;
		AccessTokenResponse accessTokenResponse = null;
		authOtpValidator.validateOTPUser(otpUser);
		try {
			// token = tokenService.getInternalTokenGenerationService();
			accessTokenResponse = getAuthAccessToken(idaClientID, idaSecret, realmId);
		} catch (HttpClientErrorException | HttpServerErrorException ex) {
			List<ServiceError> validationErrorsList = ExceptionUtils.getServiceErrorList(ex.getResponseBodyAsString());

			if (ex.getStatusCode().value() == 401) {
				if (!validationErrorsList.isEmpty()) {
					throw new AuthNException(validationErrorsList);
				} else {
					throw new AuthManagerException(AuthErrorCode.CLIENT_ERROR.getErrorCode(),
							AuthErrorCode.CLIENT_ERROR.getErrorMessage(), ex);
				}
			}
			if (ex.getStatusCode().value() == 403) {
				if (!validationErrorsList.isEmpty()) {
					throw new AuthZException(validationErrorsList);
				} else {
					throw new AuthManagerException(AuthErrorCode.CLIENT_ERROR.getErrorCode(), ex.getMessage(), ex);
				}
			}
			if (!validationErrorsList.isEmpty()) {
				throw new AuthManagerServiceException(validationErrorsList);
			} else {
				throw new AuthManagerException(AuthErrorCode.CLIENT_ERROR.getErrorCode(), ex.getMessage(), ex);
			}
		}
		OtpGenerateResponseDto otpGenerateResponseDto = null;
		if (accessTokenResponse != null) {
			if (!proxyOtp) {
				otpGenerateResponseDto = oTPGenerateService.generateOTP(mosipUser,
						accessTokenResponse.getAccess_token());
			} else {
				otpGenerateResponseDto = new OtpGenerateResponseDto();
				otpGenerateResponseDto.setOtp("123445");
				otpGenerateResponseDto.setStatus(AuthConstant.SUCCESS_STATUS);
			}
			if (otpGenerateResponseDto != null && otpGenerateResponseDto.getStatus().equals("USER_BLOCKED")) {
				authNResponseDto = new AuthNResponseDto();
				authNResponseDto.setStatus(AuthConstant.FAILURE_STATUS);
				authNResponseDto.setMessage(otpGenerateResponseDto.getStatus());
				return authNResponseDto;
			}
			if (otpGenerateResponseDto != null) {
				for (String channel : otpUser.getOtpChannel()) {
					switch (channel.toLowerCase()) {
					case AuthConstant.EMAIL:
						if (!proxyOtp) {
							emailTemplate = templateUtil.getEmailTemplate(otpGenerateResponseDto.getOtp(), otpUser,
									accessTokenResponse.getAccess_token());
							otpEmailSendResponseDto = sendOtpByEmail(emailTemplate, mosipUser.getUserId(),
									accessTokenResponse.getAccess_token());
						} else {
							otpEmailSendResponseDto = new OtpEmailSendResponseDto();
							otpEmailSendResponseDto.setMessage("Email Request submitted");
							otpEmailSendResponseDto.setStatus(AuthConstant.SUCCESS_STATUS);
						}
						break;
					case AuthConstant.PHONE:
						if (!proxyOtp) {
							mobileMessage = templateUtil.getOtpSmsMessage(otpGenerateResponseDto.getOtp(), otpUser,
									accessTokenResponse.getAccess_token());
							otpSmsSendResponseDto = sendOtpBySms(mobileMessage, mosipUser.getUserId(),
									accessTokenResponse.getAccess_token());
						} else {
							otpEmailSendResponseDto = new OtpEmailSendResponseDto();
							otpEmailSendResponseDto.setMessage("Sms Request Sent");
							otpEmailSendResponseDto.setStatus(AuthConstant.SUCCESS_STATUS);
						}
						break;
					}
				}
			}
		} else {
			throw new AuthManagerException(AuthErrorCode.CLIENT_ERROR.getErrorCode(), "response is null");
		}

		if (otpEmailSendResponseDto != null && otpSmsSendResponseDto != null) {
			authNResponseDto = new AuthNResponseDto();
			authNResponseDto.setStatus(AuthConstant.SUCCESS_STATUS);
			authNResponseDto.setMessage(AuthConstant.ALL_CHANNELS_MESSAGE);
		} else if (otpEmailSendResponseDto != null) {
			authNResponseDto = new AuthNResponseDto();
			authNResponseDto.setStatus(otpEmailSendResponseDto.getStatus());
			authNResponseDto.setMessage(otpEmailSendResponseDto.getMessage());
		} else if (otpSmsSendResponseDto != null) {
			authNResponseDto = new AuthNResponseDto();
			authNResponseDto.setStatus(otpSmsSendResponseDto.getStatus());
			authNResponseDto.setMessage(otpSmsSendResponseDto.getMessage());
		}
		return authNResponseDto;
	}

	/**
	 * Sends OTP for userid identities, or stubs success when {@link #proxyOtp} is true.
	 *
	 * @param mosipUser user with mail/mobile
	 * @param otpUser   channels and context
	 * @param appId     application / realm context
	 * @return send status
	 * @throws Exception if generation or send fails
	 */
	@Override
	public AuthNResponseDto sendOTP(MosipUserDto mosipUser, OtpUser otpUser, String appId) throws Exception {
		AuthNResponseDto authNResponseDto = null;
		OtpEmailSendResponseDto otpEmailSendResponseDto = null;
		SmsResponseDto otpSmsSendResponseDto = null;
		String mobileMessage = null;
		OTPEmailTemplate emailTemplate = null;
		AccessTokenResponse accessTokenResponse = null;
		authOtpValidator.validateOTPUser(otpUser);
		try {
			accessTokenResponse = new AccessTokenResponse();
			long exp = System.currentTimeMillis() + localExp;
			accessTokenResponse.setAccess_token(proxyTokenGenerator.getProxyToken("AUTH", exp));
			accessTokenResponse.setExpires_in(exp + "");
		} catch (HttpClientErrorException | HttpServerErrorException ex) {
			List<ServiceError> validationErrorsList = ExceptionUtils.getServiceErrorList(ex.getResponseBodyAsString());

			if (ex.getStatusCode().value() == 401) {
				if (!validationErrorsList.isEmpty()) {
					throw new AuthNException(validationErrorsList);
				} else {
					throw new AuthManagerException(AuthErrorCode.CLIENT_ERROR.getErrorCode(),
							AuthErrorCode.CLIENT_ERROR.getErrorMessage(), ex);
				}
			}
			if (ex.getStatusCode().value() == 403) {
				if (!validationErrorsList.isEmpty()) {
					throw new AuthZException(validationErrorsList);
				} else {
					throw new AuthManagerException(AuthErrorCode.CLIENT_ERROR.getErrorCode(), ex.getMessage(), ex);
				}
			}
			if (!validationErrorsList.isEmpty()) {
				throw new AuthManagerServiceException(validationErrorsList);
			} else {
				throw new AuthManagerException(AuthErrorCode.CLIENT_ERROR.getErrorCode(), ex.getMessage(), ex);
			}
		}
		OtpGenerateResponseDto otpGenerateResponseDto = null;
		if (!proxyOtp) {
			otpGenerateResponseDto = oTPGenerateService.generateOTP(mosipUser, accessTokenResponse.getAccess_token());
		} else {
			otpGenerateResponseDto = new OtpGenerateResponseDto();
			otpGenerateResponseDto.setOtp("123445");
			otpGenerateResponseDto.setStatus(AuthConstant.SUCCESS_STATUS);
		}
		if (otpGenerateResponseDto != null && otpGenerateResponseDto.getStatus().equals("USER_BLOCKED")) {
			authNResponseDto = new AuthNResponseDto();
			authNResponseDto.setStatus(AuthConstant.FAILURE_STATUS);
			authNResponseDto.setMessage(otpGenerateResponseDto.getStatus());
			return authNResponseDto;
		}
		if (otpGenerateResponseDto != null) {
			for (String channel : otpUser.getOtpChannel()) {
				switch (channel.toLowerCase()) {
				case AuthConstant.EMAIL:
					if (!proxyOtp) {
						emailTemplate = templateUtil.getEmailTemplate(otpGenerateResponseDto.getOtp(), otpUser,
								accessTokenResponse.getAccess_token());
						otpEmailSendResponseDto = sendOtpByEmail(emailTemplate, mosipUser.getUserId(),
								accessTokenResponse.getAccess_token());
					} else {
						otpEmailSendResponseDto = new OtpEmailSendResponseDto();
						otpEmailSendResponseDto.setMessage("Email Request submitted");
						otpEmailSendResponseDto.setStatus(AuthConstant.SUCCESS_STATUS);
					}
					break;
				case AuthConstant.PHONE:
					if (!proxyOtp) {
						mobileMessage = templateUtil.getOtpSmsMessage(otpGenerateResponseDto.getOtp(), otpUser,
								accessTokenResponse.getAccess_token());
						otpSmsSendResponseDto = sendOtpBySms(mobileMessage, mosipUser.getUserId(),
								accessTokenResponse.getAccess_token());
					} else {
						otpEmailSendResponseDto = new OtpEmailSendResponseDto();
						otpEmailSendResponseDto.setMessage("Sms Request Sent");
						otpEmailSendResponseDto.setStatus(AuthConstant.SUCCESS_STATUS);
					}
					break;
				}
			}
		}

		if (otpEmailSendResponseDto != null && otpSmsSendResponseDto != null) {
			authNResponseDto = new AuthNResponseDto();
			authNResponseDto.setStatus(AuthConstant.SUCCESS_STATUS);
			authNResponseDto.setMessage(AuthConstant.ALL_CHANNELS_MESSAGE);
		} else if (otpEmailSendResponseDto != null) {
			authNResponseDto = new AuthNResponseDto();
			authNResponseDto.setStatus(otpEmailSendResponseDto.getStatus());
			authNResponseDto.setMessage(otpEmailSendResponseDto.getMessage());
		} else if (otpSmsSendResponseDto != null) {
			authNResponseDto = new AuthNResponseDto();
			authNResponseDto.setStatus(otpSmsSendResponseDto.getStatus());
			authNResponseDto.setMessage(otpSmsSendResponseDto.getMessage());
		}
		return authNResponseDto;
	}

	/**
	 * POSTs the email notification payload to the configured sender API.
	 *
	 * @param emailTemplate subject and body
	 * @param email         destination address
	 * @param token         internal auth cookie token
	 * @return email send result
	 */
	private OtpEmailSendResponseDto sendOtpByEmail(OTPEmailTemplate emailTemplate, String email, String token) {
		ResponseEntity<String> response = null;
		String url = mosipEnvironment.getOtpSenderEmailApi();
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.MULTIPART_FORM_DATA);
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
		OtpEmailSendResponseDto otpEmailSendResponseDto = null;
		headers.set(AuthConstant.COOKIE, AuthConstant.AUTH_HEADER + token);
		MultiValueMap<String, String> map = new LinkedMultiValueMap<String, String>();
		map.add("mailTo", email);
		map.add("mailSubject", emailTemplate.getEmailSubject());
		map.add("mailContent", emailTemplate.getEmailContent());
		HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<MultiValueMap<String, String>>(map, headers);
		try {
			response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);
			if (response.getStatusCode().equals(HttpStatus.OK)) {
				String responseBody = response.getBody();
				List<ServiceError> validationErrorsList = null;
				validationErrorsList = ExceptionUtils.getServiceErrorList(responseBody);
				if (!validationErrorsList.isEmpty()) {
					throw new AuthManagerServiceException(validationErrorsList);
				}
				ResponseWrapper<?> responseObject;
				try {
					responseObject = mapper.readValue(response.getBody(), ResponseWrapper.class);
					otpEmailSendResponseDto = mapper.readValue(mapper.writeValueAsString(responseObject.getResponse()),
							OtpEmailSendResponseDto.class);
				} catch (Exception e) {
					throw new AuthManagerException(String.valueOf(HttpStatus.UNAUTHORIZED.value()), e.getMessage(), e);
				}
			}
		} catch (HttpClientErrorException | HttpServerErrorException ex) {
			List<ServiceError> validationErrorsList = ExceptionUtils.getServiceErrorList(ex.getResponseBodyAsString());

			if (ex.getStatusCode().value() == 401) {
				if (!validationErrorsList.isEmpty()) {
					throw new AuthNException(validationErrorsList);
				} else {
					throw new AuthManagerException(AuthErrorCode.CLIENT_ERROR.getErrorCode(),
							AuthErrorCode.CLIENT_ERROR.getErrorMessage(), ex);
				}
			}
			if (ex.getStatusCode().value() == 403) {
				if (!validationErrorsList.isEmpty()) {
					throw new AuthZException(validationErrorsList);
				} else {
					throw new AuthManagerException(AuthErrorCode.CLIENT_ERROR.getErrorCode(), ex.getMessage(), ex);
				}
			}
			if (!validationErrorsList.isEmpty()) {
				throw new AuthManagerServiceException(validationErrorsList);
			} else {
				throw new AuthManagerException(AuthErrorCode.CLIENT_ERROR.getErrorCode(), ex.getMessage(), ex);
			}
		}
		return otpEmailSendResponseDto;
	}

	/**
	 * Password grant for a username against the given realm.
	 *
	 * @param username user name
	 * @param realm    Keycloak realm
	 * @return token response
	 */
	private AccessTokenResponse getUserAccessToken(String username, String realm) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		MultiValueMap<String, String> tokenRequestBody = null;
		Map<String, String> pathParams = new HashMap<>();
		pathParams.put(AuthConstant.REALM_ID, realm);
		UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromUriString(keycloakOpenIdUrl + "/token");
		tokenRequestBody = getAdminValueMap(username, realm);
		HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(tokenRequestBody, headers);
		ResponseEntity<AccessTokenResponse> response = restTemplate.postForEntity(
				uriComponentsBuilder.buildAndExpand(pathParams).toUriString(), request, AccessTokenResponse.class);
		return response.getBody();
	}

	/**
	 * Client-credentials grant for internal OTP-manager calls.
	 *
	 * @param clientID     OAuth client id
	 * @param clientSecret OAuth secret
	 * @param realmId      Keycloak realm
	 * @return token response
	 */
	private AccessTokenResponse getAuthAccessToken(String clientID, String clientSecret, String realmId) {
		HttpHeaders headers = new HttpHeaders();
		if (realmId.equalsIgnoreCase(preregRealmId)) {
			clientID = preregClientId;
			clientSecret = preregSecretKey;
		}
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		MultiValueMap<String, String> tokenRequestBody = null;
		Map<String, String> pathParams = new HashMap<>();
		pathParams.put(AuthConstant.REALM_ID, realmId);
		UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromUriString(keycloakOpenIdUrl + "/token");
		tokenRequestBody = getClientValueMap(clientID, clientSecret);
		HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(tokenRequestBody, headers);
		ResponseEntity<AccessTokenResponse> response = restTemplate.postForEntity(
				uriComponentsBuilder.buildAndExpand(pathParams).toUriString(), request, AccessTokenResponse.class);
		return response.getBody();
	}

	/**
	 * Form body for prereg password grant.
	 *
	 * @param username user name
	 * @param realm    unused except as method contract
	 * @return form fields
	 */
	private MultiValueMap<String, String> getAdminValueMap(String username, String realm) {
		MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
		if (realm.equalsIgnoreCase(preregRealmId)) {
			map.add(AuthConstant.CLIENT_ID, preregClientId);
			map.add(AuthConstant.CLIENT_SECRET, preregSecretKey);
		} else {
			map.add(AuthConstant.CLIENT_ID, mosipAdminClientID);
			map.add(AuthConstant.CLIENT_SECRET, mosipAdminSecret);
		}
		map.add(AuthConstant.GRANT_TYPE, AuthConstant.PASSWORDCONSTANT);
		map.add(AuthConstant.USER_NAME, username);
		map.add(AuthConstant.PASSWORDCONSTANT, preRegUserPassword);
		return map;
	}

	/**
	 * Form body for client-credentials grant.
	 *
	 * @param clientID     OAuth client id
	 * @param clientSecret OAuth secret
	 * @return form fields
	 */
	private MultiValueMap<String, String> getClientValueMap(String clientID, String clientSecret) {
		MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
		map.add(AuthConstant.GRANT_TYPE, AuthConstant.CLIENT_CREDENTIALS);
		map.add(AuthConstant.CLIENT_ID, clientID);
		map.add(AuthConstant.CLIENT_SECRET, clientSecret);
		return map;
	}
}
