/**
 * 
 */
package io.mosip.kernel.smsserviceprovider.msg91.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import io.mosip.kernel.core.notification.exception.InvalidNumberException;
import io.mosip.kernel.core.notification.model.SMSResponseDto;
import io.mosip.kernel.core.notification.spi.SMSServiceProvider;
import io.mosip.kernel.core.util.StringUtils;
import io.mosip.kernel.smsserviceprovider.msg91.constant.SmsExceptionConstant;
import io.mosip.kernel.smsserviceprovider.msg91.constant.SmsPropertyConstant;

/**
 * @author Ritesh Sinha
 * @since 1.0.0
 */
@Component
public class SMSServiceProviderImpl implements SMSServiceProvider {

	@Autowired
	RestTemplate restTemplate;

	@Value("${mosip.kernel.sms.enabled:false}")
	boolean smsEnabled;

	@Value("${mosip.kernel.sms.country.code}")
	String countryCode;

	@Value("${mosip.kernel.sms.number.length}")
	int numberLength;

	@Value("${mosip.kernel.sms.api}")
	String api;

	@Value("${mosip.kernel.sms.sender}")
	String sender;

	@Value("${mosip.kernel.sms.password:null}")
	private String password;

	@Value("${mosip.kernel.sms.route:null}")
	String route;

	@Value("${mosip.kernel.sms.authkey:null}")
	String authkey;

	@Value("${mosip.kernel.sms.unicode:1}")
	String unicode;

	@Override
	public SMSResponseDto sendSms(String contactNumber, String message) {
		SMSResponseDto smsResponseDTO = new SMSResponseDto();
		validateInput(contactNumber);
		UriComponentsBuilder sms = UriComponentsBuilder.fromHttpUrl(api)
				.queryParam(SmsPropertyConstant.AUTH_KEY.getProperty(), authkey)
				.queryParam(SmsPropertyConstant.SMS_MESSAGE.getProperty(), message)
				.queryParam(SmsPropertyConstant.ROUTE.getProperty(), route)
				.queryParam(SmsPropertyConstant.SENDER_ID.getProperty(), sender)
				.queryParam(SmsPropertyConstant.RECIPIENT_NUMBER.getProperty(), contactNumber)
				.queryParam(SmsPropertyConstant.UNICODE.getProperty(), unicode)
				.queryParam(SmsPropertyConstant.COUNTRY_CODE.getProperty(), countryCode);
		try {
			restTemplate.getForEntity(sms.toUriString(), String.class);
		} catch (HttpClientErrorException | HttpServerErrorException e) {
			throw new RuntimeException(e.getResponseBodyAsString());
		}
		smsResponseDTO.setMessage(SmsPropertyConstant.SUCCESS_RESPONSE.getProperty());
		smsResponseDTO.setStatus("success");
		return smsResponseDTO;
	}

	private void validateInput(String contactNumber) {
		if (!StringUtils.isNumeric(contactNumber) || contactNumber.length() < numberLength
				|| contactNumber.length() > numberLength) {
			throw new InvalidNumberException(SmsExceptionConstant.SMS_INVALID_CONTACT_NUMBER.getErrorCode(),
					SmsExceptionConstant.SMS_INVALID_CONTACT_NUMBER.getErrorMessage() + numberLength
							+ SmsPropertyConstant.SUFFIX_MESSAGE.getProperty());
		}
	}

}