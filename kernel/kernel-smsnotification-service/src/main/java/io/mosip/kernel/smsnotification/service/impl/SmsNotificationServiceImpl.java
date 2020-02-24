package io.mosip.kernel.smsnotification.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import io.mosip.kernel.core.notification.spi.SmsNotification;
import io.mosip.kernel.core.util.CryptoUtil;
import io.mosip.kernel.core.util.StringUtils;
import io.mosip.kernel.smsnotification.constant.SmsExceptionConstant;
import io.mosip.kernel.smsnotification.constant.SmsPropertyConstant;
import io.mosip.kernel.smsnotification.dto.SmsResponseDto;
import io.mosip.kernel.smsnotification.dto.SmsVendorRequestDto;
import io.mosip.kernel.smsnotification.exception.InvalidNumberException;

/**
 * This service class send SMS on the contact number provided.
 * 
 * @author Ritesh Sinha
 * @since 1.0.0
 *
 */
@RefreshScope
@Service
public class SmsNotificationServiceImpl implements SmsNotification<SmsResponseDto> {

	/**
	 * The reference that autowired rest template builder.
	 */
	@Autowired
	RestTemplate restTemplate;

	@Value("${mosip.kernel.sms.enabled:false}")
	boolean smsEnabled;

	@Value("${mosip.kernel.sms.country.code}")
	String countryCode;

	@Value("${mosip.kernel.sms.number.length}")
	int numberLength;

	@Value("${mosip.kernel.sms.gateway:null}")
	String smsGateway;

	@Value("${mosip.kernel.sms.api}")
	String api;

	@Value("${mosip.kernel.sms.sender}")
	String sender;

	@Value("${mosip.kernel.sms.username:null}")
	String username;

	@Value("${mosip.kernel.sms.password:null}")
	private String password;

	@Value("${mosip.kernel.sms.route:null}")
	String route;

	@Value("${mosip.kernel.sms.authkey:null}")
	String authkey;

	@Value("${mosip.kernel.sms.unicode:1}")
	String unicode;

	@Value("${mosip.kernel.sms.default.api:dummy_uri}")
	String defaultApi;

	@Value("${mosip.kernel.sms.default.authkey:dummy_auth_key}")
	String defaultAuthKey;

	@Value("${mosip.kernel.sms.default.sender:dummy_sender}")
	String defaultSender;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.mosip.kernel.core.notification.spi.SmsNotification#sendSmsNotification(
	 * java.lang.String, java.lang.String)
	 */
	@Override
	public SmsResponseDto sendSmsNotification(String contactNumber, String contentMessage) {
		SmsResponseDto result = new SmsResponseDto();
		if (smsEnabled) {
			switch (smsGateway) {
			case "msg91":
				msg91Gateway(contactNumber, contentMessage);
				break;
			case "infobip":
				infoBipSmsGateway(contactNumber, contentMessage);
				break;
			default:
				defaultGateway(contactNumber, contentMessage);
				break;
			}
		}

		result.setMessage(SmsPropertyConstant.SUCCESS_RESPONSE.getProperty());
		result.setStatus("success");
		return result;

	}

	private void defaultGateway(String contactNumber, String contentMessage) {
		validateInput(contactNumber);
		UriComponentsBuilder sms = UriComponentsBuilder.fromHttpUrl(defaultApi)
				.queryParam(SmsPropertyConstant.AUTH_KEY.getProperty(), defaultAuthKey)
				.queryParam(SmsPropertyConstant.SMS_MESSAGE.getProperty(), contentMessage)
				.queryParam(SmsPropertyConstant.SENDER_ID.getProperty(), defaultSender)
				.queryParam(SmsPropertyConstant.NUMBERS.getProperty(), contactNumber);
		ResponseEntity<String> responseEnt = null;
		try {
			responseEnt = restTemplate.getForEntity(sms.toUriString(), String.class);
		} catch (HttpClientErrorException | HttpServerErrorException e) {
			throw new RuntimeException(e.getResponseBodyAsString());
		}
	}

	private void validateInput(String contactNumber) {
		if (!StringUtils.isNumeric(contactNumber) || contactNumber.length() < numberLength
				|| contactNumber.length() > numberLength) {
			throw new InvalidNumberException(SmsExceptionConstant.SMS_INVALID_CONTACT_NUMBER.getErrorCode(),
					SmsExceptionConstant.SMS_INVALID_CONTACT_NUMBER.getErrorMessage() + numberLength
							+ SmsPropertyConstant.SUFFIX_MESSAGE.getProperty());
		}
	}

	private void msg91Gateway(String contactNumber, String contentMessage) {
		validateInput(contactNumber);
		UriComponentsBuilder sms = UriComponentsBuilder.fromHttpUrl(api)
				.queryParam(SmsPropertyConstant.AUTH_KEY.getProperty(), authkey)
				.queryParam(SmsPropertyConstant.SMS_MESSAGE.getProperty(), contentMessage)
				.queryParam(SmsPropertyConstant.ROUTE.getProperty(), route)
				.queryParam(SmsPropertyConstant.SENDER_ID.getProperty(), sender)
				.queryParam(SmsPropertyConstant.RECIPIENT_NUMBER.getProperty(), contactNumber)
				.queryParam(SmsPropertyConstant.UNICODE.getProperty(), unicode)
				.queryParam(SmsPropertyConstant.COUNTRY_CODE.getProperty(), countryCode);
		ResponseEntity<String> responseEnt = null;
		try {
			responseEnt = restTemplate.getForEntity(sms.toUriString(), String.class);
		} catch (HttpClientErrorException | HttpServerErrorException e) {
			throw new RuntimeException(e.getResponseBodyAsString());
		}
	}

	private void infoBipSmsGateway(String contactNumber, String contentMessage) {
		validateInput(contactNumber);
		HttpHeaders headers = new HttpHeaders();
		String authorization = username + ":" + password;
		String authorizationKey = "Basic " + CryptoUtil.encodeBase64String(authorization.getBytes());
		headers.add("authorization", authorizationKey);
		headers.add("Content-Type", "application/json");
		SmsVendorRequestDto vendorRequestDto = new SmsVendorRequestDto();
		vendorRequestDto.setFrom(sender);
		vendorRequestDto.setText(contentMessage);
		vendorRequestDto.setTo(countryCode + contactNumber);
		HttpEntity<SmsVendorRequestDto> httpEntity = new HttpEntity<>(vendorRequestDto, headers);
		try {
			restTemplate.postForEntity(api, httpEntity, Object.class);
		} catch (HttpClientErrorException | HttpServerErrorException e) {
			System.out.println("HttpErrorException: " + e.getMessage());
			System.out.println(e.getResponseBodyAsString());
			// throw new RuntimeException(e.getResponseBodyAsString());
		}
	}

}