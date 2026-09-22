/**
 * 
 */
package io.mosip.kernel.auth.defaultimpl.service.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.kernel.auth.defaultimpl.config.MosipEnvironment;
import io.mosip.kernel.auth.defaultimpl.constant.AuthConstant;
import io.mosip.kernel.auth.defaultimpl.constant.AuthErrorCode;
import io.mosip.kernel.auth.defaultimpl.dto.otp.OtpGenerateRequest;
import io.mosip.kernel.auth.defaultimpl.dto.otp.OtpGenerateRequestDto;
import io.mosip.kernel.auth.defaultimpl.dto.otp.OtpGenerateResponseDto;
import io.mosip.kernel.auth.defaultimpl.exception.AuthManagerException;
import io.mosip.kernel.auth.defaultimpl.exception.AuthManagerServiceException;
import io.mosip.kernel.auth.defaultimpl.service.OTPGenerateService;
import io.mosip.kernel.core.authmanager.exception.AuthNException;
import io.mosip.kernel.core.authmanager.exception.AuthZException;
import io.mosip.kernel.core.authmanager.model.MosipUserDto;
import io.mosip.kernel.core.authmanager.model.OtpUser;
import io.mosip.kernel.core.exception.ExceptionUtils;
import io.mosip.kernel.core.exception.ServiceError;
import io.mosip.kernel.core.http.RequestWrapper;
import io.mosip.kernel.core.http.ResponseWrapper;

/**
 * Calls the kernel OTP manager to generate OTPs for authmanager send-OTP
 * flows. Uses {@link MediaType} JSON wrappers and {@code getStatusCode().value()}
 * for 401/403.
 *
 * @author Ramadurai Pandian
 *
 */

@Component
public class OTPGenerateServiceImpl implements OTPGenerateService {

	/**
	 * RestTemplate for OTP manager HTTP.
	 */
	@Qualifier("authRestTemplate")
	@Autowired
	RestTemplate restTemplate;

	/**
	 * OTP generate API URL.
	 */
	@Autowired
	MosipEnvironment mosipEnvironment;

	/**
	 * Mapper for MOSIP {@code ResponseWrapper} bodies.
	 */
	@Autowired
	private ObjectMapper mapper;

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.mosip.kernel.auth.service.OTPGenerateService#generateOTP(io.mosip.
	 * kernel. auth.entities.MosipUserDto, java.lang.String)
	 */
	/**
	 * Calls the OTP manager generate API for a single user. HTTP 401/403 are
	 * mapped via {@code getStatusCode().value()}.
	 *
	 * @param mosipUserDto user identity for OTP
	 * @param token        internal auth cookie token
	 * @return OTP generate response
	 */
	@Override
	public OtpGenerateResponseDto generateOTP(MosipUserDto mosipUserDto, String token) {
		try {
			List<ServiceError> validationErrorsList = null;
			OtpGenerateResponseDto otpGenerateResponseDto;
			OtpGenerateRequestDto otpGenerateRequestDto = new OtpGenerateRequestDto(mosipUserDto);
			final String url = mosipEnvironment.getGenerateOtpApi();

			RequestWrapper<OtpGenerateRequestDto> reqWrapper = new RequestWrapper<>();
			reqWrapper.setRequesttime(LocalDateTime.now());
			reqWrapper.setRequest(otpGenerateRequestDto);
			HttpHeaders headers = new HttpHeaders();
			headers.set(AuthConstant.COOKIE, AuthConstant.AUTH_HEADER + token);
			HttpEntity<RequestWrapper<OtpGenerateRequestDto>> request = new HttpEntity<>(reqWrapper, headers);
			ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
			validationErrorsList = ExceptionUtils.getServiceErrorList(response.getBody());
			if (!validationErrorsList.isEmpty()) {
				throw new AuthManagerServiceException(validationErrorsList);
			}
			ResponseWrapper<?> responseObject;
			try {
				responseObject = mapper.readValue(response.getBody(), ResponseWrapper.class);
				otpGenerateResponseDto = mapper.readValue(mapper.writeValueAsString(responseObject.getResponse()),
						OtpGenerateResponseDto.class);
			} catch (Exception e) {
				throw new AuthManagerException(AuthErrorCode.RESPONSE_PARSE_ERROR.getErrorCode(),
						AuthErrorCode.RESPONSE_PARSE_ERROR.getErrorMessage(), e);
			}
			return otpGenerateResponseDto;
		} catch (HttpClientErrorException | HttpServerErrorException ex) {
			List<ServiceError> validationErrorsList = ExceptionUtils.getServiceErrorList(ex.getResponseBodyAsString());

			if (ex.getStatusCode().value() == 401) {
				if (!validationErrorsList.isEmpty()) {
					throw new AuthNException(validationErrorsList);
				} else {
					throw new AuthManagerException(AuthErrorCode.RESPONSE_PARSE_ERROR.getErrorCode(),
							AuthErrorCode.RESPONSE_PARSE_ERROR.getErrorMessage(), ex);
				}
			}
			if (ex.getStatusCode().value() == 403) {
				if (!validationErrorsList.isEmpty()) {
					throw new AuthZException(validationErrorsList);
				} else {
					throw new AuthManagerException(AuthErrorCode.RESPONSE_PARSE_ERROR.getErrorCode(),
							AuthErrorCode.RESPONSE_PARSE_ERROR.getErrorMessage(), ex);
				}
			}
			if (!validationErrorsList.isEmpty()) {
				throw new AuthManagerServiceException(validationErrorsList);
			} else {
				throw new AuthManagerException(AuthErrorCode.CLIENT_ERROR.getErrorCode(), ex.getMessage(), ex);
			}
		}
	}

	/**
	 * Calls the OTP manager generate API for multiple notification channels.
	 *
	 * @param mosipUserDto user identity
	 * @param otpUser      channels and context
	 * @param token        internal auth cookie token
	 * @return OTP generate response
	 */
	@Override
	public OtpGenerateResponseDto generateOTPMultipleChannels(MosipUserDto mosipUserDto, OtpUser otpUser,
			String token) {
		try {
			List<ServiceError> validationErrorsList = null;
			OtpGenerateResponseDto otpGenerateResponseDto;
			OtpGenerateRequest otpGenerateRequestDto = new OtpGenerateRequest(mosipUserDto);
			final String url = mosipEnvironment.getGenerateOtpApi();

			RequestWrapper<OtpGenerateRequest> reqWrapper = new RequestWrapper<>();
			reqWrapper.setRequesttime(LocalDateTime.now());
			reqWrapper.setRequest(otpGenerateRequestDto);
			HttpHeaders headers = new HttpHeaders();
			headers.set(AuthConstant.COOKIE, AuthConstant.AUTH_HEADER + token);
			HttpEntity<RequestWrapper<OtpGenerateRequest>> request = new HttpEntity<>(reqWrapper, headers);
			ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
			validationErrorsList = ExceptionUtils.getServiceErrorList(response.getBody());
			if (!validationErrorsList.isEmpty()) {
				throw new AuthManagerServiceException(validationErrorsList);
			}
			ResponseWrapper<?> responseObject;
			try {
				responseObject = mapper.readValue(response.getBody(), ResponseWrapper.class);
				otpGenerateResponseDto = mapper.readValue(mapper.writeValueAsString(responseObject.getResponse()),
						OtpGenerateResponseDto.class);
			} catch (Exception e) {
				throw new AuthManagerException(String.valueOf(HttpStatus.UNAUTHORIZED.value()), e.getMessage(), e);
			}
			return otpGenerateResponseDto;
		} catch (HttpClientErrorException | HttpServerErrorException exp) {
			throw new AuthManagerException(String.valueOf(HttpStatus.UNAUTHORIZED.value()), exp.getMessage(), exp);
		}
	}

}
