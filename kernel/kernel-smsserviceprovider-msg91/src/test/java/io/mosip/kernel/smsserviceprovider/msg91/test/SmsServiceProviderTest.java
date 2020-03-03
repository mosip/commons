package io.mosip.kernel.smsserviceprovider.msg91.test;

import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import io.mosip.kernel.core.notification.exception.InvalidNumberException;
import io.mosip.kernel.core.notification.model.SMSResponseDto;
import io.mosip.kernel.smsserviceprovider.msg91.SMSServiceProviderBootApplication;
import io.mosip.kernel.smsserviceprovider.msg91.constant.SmsPropertyConstant;
import io.mosip.kernel.smsserviceprovider.msg91.dto.SmsServerResponseDto;
import io.mosip.kernel.smsserviceprovider.msg91.impl.SMSServiceProviderImpl;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { SMSServiceProviderBootApplication.class })
public class SmsServiceProviderTest {

	@Autowired
	SMSServiceProviderImpl service;

	@MockBean
	RestTemplate restTemplate;

	@Value("${mosip.kernel.sms.api}")
	String api;

	@Value("${mosip.kernel.sms.authkey}")
	String authkey;

	@Value("${mosip.kernel.sms.country.code}")
	String countryCode;

	@Value("${mosip.kernel.sms.sender}")
	String senderId;

	@Value("${mosip.kernel.sms.route}")
	String route;

	@Value("${mosip.kernel.sms.number.length}")
	String length;

	@Test
	public void sendSmsTest() {

		UriComponentsBuilder sms = UriComponentsBuilder.fromHttpUrl(api)
				.queryParam(SmsPropertyConstant.AUTH_KEY.getProperty(), authkey)
				.queryParam(SmsPropertyConstant.SMS_MESSAGE.getProperty(), "your otp is 4646")
				.queryParam(SmsPropertyConstant.ROUTE.getProperty(), route)
				.queryParam(SmsPropertyConstant.SENDER_ID.getProperty(), senderId)
				.queryParam(SmsPropertyConstant.RECIPIENT_NUMBER.getProperty(), "8987876473")
				.queryParam(SmsPropertyConstant.COUNTRY_CODE.getProperty(), countryCode);

		SmsServerResponseDto serverResponse = new SmsServerResponseDto();
		serverResponse.setType("success");
		SMSResponseDto dto = new SMSResponseDto();
		dto.setStatus(serverResponse.getType());
		dto.setMessage("Sms Request Sent");

		when(restTemplate.getForEntity(sms.toUriString(), String.class))
				.thenReturn(new ResponseEntity<>(serverResponse.toString(), HttpStatus.OK));

		when(restTemplate.postForEntity(Mockito.anyString(), Mockito.eq(Mockito.any()), Object.class))
				.thenReturn(new ResponseEntity<>(serverResponse, HttpStatus.OK));

		// assertThat(service.sendSms("8987876473", "your otp is 4646"),
		// is(dto));

	}

	@Test(expected = InvalidNumberException.class)
	public void invalidContactNumberTest() {
		service.sendSms("jsbchb", "hello your otp is 45373");
	}

	@Test(expected = InvalidNumberException.class)
	public void contactNumberMinimumThresholdTest() {
		service.sendSms("78978976", "hello your otp is 45373");
	}

	@Test(expected = InvalidNumberException.class)
	public void contactNumberMaximumThresholdTest() {
		service.sendSms("7897897458673484376", "hello your otp is 45373");
	}

	@Test
	public void validGateWayTest() {
		service.sendSms("1234567890", "hello your otp is 45373");
	}

}