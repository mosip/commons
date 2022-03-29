package io.mosip.kernel.emailnotification.test.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;

import io.mosip.kernel.core.notification.model.SMSResponseDto;
import io.mosip.kernel.core.notification.spi.SMSServiceProvider;
import io.mosip.kernel.emailnotification.service.SmsNotification;
import io.mosip.kernel.emailnotification.test.NotificationTestBootApplication;

@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@SpringBootTest(classes = NotificationTestBootApplication.class)
public class LocalProxyTest {
	
	
	@Autowired
	private SmsNotification smsNotification;

	@MockBean
	private SMSServiceProvider smsServiceProvider;
	
	@Before
	public void init() {
		ReflectionTestUtils.setField(smsNotification, "isProxytrue", true);
	}
	
	
	@Test
	public void testSendSmsNotification() throws Exception {
		SMSResponseDto sMSResponseDto=smsNotification.sendSmsNotification("9320183102", "mock-msg");
		assertThat(sMSResponseDto.getStatus(), is("success"));
	}
	
	@Test
	public void testSendSmsNotificationLocalProxy() throws Exception {
		ReflectionTestUtils.setField(smsNotification, "activeProfile", "local");
		ReflectionTestUtils.setField(smsNotification, "isProxytrue", false);
		SMSResponseDto sMSResponseDto=smsNotification.sendSmsNotification("9320183102", "mock-msg");
		assertThat(sMSResponseDto.getStatus(), is("success"));
	}
	
	
	
	
	
	
	
}
