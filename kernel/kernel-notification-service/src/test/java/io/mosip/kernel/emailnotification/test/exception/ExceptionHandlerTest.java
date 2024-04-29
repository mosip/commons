package io.mosip.kernel.emailnotification.test.exception;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.kernel.core.http.RequestWrapper;
import io.mosip.kernel.core.notification.spi.SMSServiceProvider;
import io.mosip.kernel.emailnotification.service.impl.SmsNotificationServiceImpl;
import io.mosip.kernel.emailnotification.test.NotificationTestBootApplication;

@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@SpringBootTest(classes = { NotificationTestBootApplication.class })
public class ExceptionHandlerTest {

	@Autowired
	private MockMvc mockMvc;
	
	@Autowired
	private ObjectMapper mapper;

	@MockBean
	SmsNotificationServiceImpl service;
	
	@MockBean
	private SMSServiceProvider sMSServiceProvider;

	@WithUserDetails("individual")
	@Test
	public void emptyContactNumberTest() throws Exception {
		String json = "{\"number\":\"\",\"message\":\"hello..your otp is 342891\"}";
		String requestWrapperJson = wrapRequest(json);
		mockMvc.perform(post("/sms/send").contentType(MediaType.APPLICATION_JSON).content(requestWrapperJson))
				.andExpect(status().isOk());
	}

	private String wrapRequest(String json) throws JsonProcessingException {
		RequestWrapper<String> requestWrapper = new RequestWrapper<String>();
		requestWrapper.setRequest(json);
		String requestWrapperJson = mapper.writeValueAsString(requestWrapper);
		return requestWrapperJson;
	}

	@WithUserDetails("individual")
	@Test
	public void nullContactNumberTest() throws Exception {
		String json = "{\"number\":null,\"message\":\"hello..your otp is 342891\"}";
		String requestWrapperJson = wrapRequest(json);
		mockMvc.perform(post("/sms/send").contentType(MediaType.APPLICATION_JSON).content(requestWrapperJson))
				.andExpect(status().isOk());
	}

	@WithUserDetails("individual")
	@Test
	public void nullMessageTest() throws Exception {
		String json = "{\"number\":\"8987672341\",\"message\":null}";
		String requestWrapperJson = wrapRequest(json);
		mockMvc.perform(post("/sms/send").contentType(MediaType.APPLICATION_JSON).content(requestWrapperJson))
				.andExpect(status().isOk());
	}

	@WithUserDetails("individual")
	@Test
	public void emptyMessageTest() throws Exception {
		String json = "{\"number\":\"\",\"message\":\"\"}";
		String requestWrapperJson = wrapRequest(json);
		mockMvc.perform(post("/sms/send").contentType(MediaType.APPLICATION_JSON).content(requestWrapperJson))
				.andExpect(status().isOk());
	}

	@WithUserDetails("individual")
	@Test
	public void contactNumberLengthTest() throws Exception {
		String json = "{\"number\":\"678\",\"message\":\"\"}";
		String requestWrapperJson = wrapRequest(json);
		mockMvc.perform(post("/sms/send").contentType(MediaType.APPLICATION_JSON).content(requestWrapperJson))
				.andExpect(status().isOk());
	}

	@WithUserDetails("individual")
	@Test
	public void invalidContactNumberTest() throws Exception {
		String json = "{\"number\":\"sdjnjkdfj\",\"message\":\"\"}";
		String requestWrapperJson = wrapRequest(json);
		mockMvc.perform(post("/sms/send").contentType(MediaType.APPLICATION_JSON).content(requestWrapperJson))
				.andExpect(status().isOk());
	}
}
