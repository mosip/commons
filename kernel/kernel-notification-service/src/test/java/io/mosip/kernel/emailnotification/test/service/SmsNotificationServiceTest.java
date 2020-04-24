package io.mosip.kernel.emailnotification.test.service;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import io.mosip.kernel.core.notification.model.SMSResponseDto;
import io.mosip.kernel.core.notification.spi.SMSServiceProvider;
import io.mosip.kernel.emailnotification.service.impl.SmsNotificationServiceImpl;
import io.mosip.kernel.emailnotification.test.NotificationTestBootApplication;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { NotificationTestBootApplication.class })
public class SmsNotificationServiceTest {

	@Autowired
	SmsNotificationServiceImpl service;

	@MockBean
	private SMSServiceProvider smsServiceProvider;


	@Test
	public void sendSmsNotificationTest() {

		SMSResponseDto dto = new SMSResponseDto();
		dto.setStatus("success");
		dto.setMessage("Sms Request Sent");

		when(smsServiceProvider.sendSms(Mockito.anyString(), Mockito.anyString())).thenReturn(dto);

		assertThat(service.sendSmsNotification("8987876473", "your otp is 4646"),is(dto));

	}


}