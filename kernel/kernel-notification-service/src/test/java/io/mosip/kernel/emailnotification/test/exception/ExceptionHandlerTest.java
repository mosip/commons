package io.mosip.kernel.emailnotification.test.exception;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;

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

import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.kernel.core.http.RequestWrapper;
import io.mosip.kernel.core.notification.spi.SMSServiceProvider;
import io.mosip.kernel.emailnotification.dto.SmsRequestDto;
import io.mosip.kernel.emailnotification.service.impl.SmsNotificationServiceImpl;
import io.mosip.kernel.emailnotification.test.NotificationTestBootApplication;

@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@SpringBootTest(classes = { NotificationTestBootApplication.class })
public class ExceptionHandlerTest {

	@Autowired
	private MockMvc mockMvc;
	
	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	SmsNotificationServiceImpl service;
	
	@MockBean
	private SMSServiceProvider sMSServiceProvider;

	@WithUserDetails("individual")
	@Test
	public void emptyContactNumberTest() throws Exception {
		SmsRequestDto requestDto = new SmsRequestDto();
		requestDto.setMessage("hello..your otp is 342891");
		requestDto.setNumber("");

		RequestWrapper<SmsRequestDto> reqWrapperDTO = new RequestWrapper<>();
		reqWrapperDTO.setId("ID");
		reqWrapperDTO.setMetadata(null);
		reqWrapperDTO.setRequest(requestDto);
		reqWrapperDTO.setRequesttime(LocalDateTime.now());
		reqWrapperDTO.setVersion("v1.0");
		String json = objectMapper.writeValueAsString(reqWrapperDTO);
		mockMvc.perform(post("/sms/send").contentType(MediaType.APPLICATION_JSON).content(json))
				.andExpect(status().isOk());
	}

	@WithUserDetails("individual")
	@Test
	public void nullContactNumberTest() throws Exception {
		SmsRequestDto requestDto = new SmsRequestDto();
		requestDto.setMessage("hello..your otp is 342891");
		requestDto.setNumber(null);

		RequestWrapper<SmsRequestDto> reqWrapperDTO = new RequestWrapper<>();
		reqWrapperDTO.setId("ID");
		reqWrapperDTO.setMetadata(null);
		reqWrapperDTO.setRequest(requestDto);
		reqWrapperDTO.setRequesttime(LocalDateTime.now());
		reqWrapperDTO.setVersion("v1.0");
		String json = objectMapper.writeValueAsString(reqWrapperDTO);
		mockMvc.perform(post("/sms/send").contentType(MediaType.APPLICATION_JSON).content(json))
				.andExpect(status().isOk());
	}

	@WithUserDetails("individual")
	@Test
	public void nullMessageTest() throws Exception {
		SmsRequestDto requestDto = new SmsRequestDto();
		requestDto.setMessage(null);
		requestDto.setNumber("8987672341");

		RequestWrapper<SmsRequestDto> reqWrapperDTO = new RequestWrapper<>();
		reqWrapperDTO.setId("ID");
		reqWrapperDTO.setMetadata(null);
		reqWrapperDTO.setRequest(requestDto);
		reqWrapperDTO.setRequesttime(LocalDateTime.now());
		reqWrapperDTO.setVersion("v1.0");
		String json = objectMapper.writeValueAsString(reqWrapperDTO);
		mockMvc.perform(post("/sms/send").contentType(MediaType.APPLICATION_JSON).content(json))
				.andExpect(status().isOk());
	}

	@WithUserDetails("individual")
	@Test
	public void emptyMessageTest() throws Exception {
		SmsRequestDto requestDto = new SmsRequestDto();
		requestDto.setMessage("");
		requestDto.setNumber("8987672341");

		RequestWrapper<SmsRequestDto> reqWrapperDTO = new RequestWrapper<>();
		reqWrapperDTO.setId("ID");
		reqWrapperDTO.setMetadata(null);
		reqWrapperDTO.setRequest(requestDto);
		reqWrapperDTO.setRequesttime(LocalDateTime.now());
		reqWrapperDTO.setVersion("v1.0");
		String json = objectMapper.writeValueAsString(reqWrapperDTO);
		mockMvc.perform(post("/sms/send").contentType(MediaType.APPLICATION_JSON).content(json))
				.andExpect(status().isOk());
	}

	@WithUserDetails("individual")
	@Test
	public void contactNumberLengthTest() throws Exception {
		SmsRequestDto requestDto = new SmsRequestDto();
		requestDto.setMessage("asdasd");
		requestDto.setNumber("678");

		RequestWrapper<SmsRequestDto> reqWrapperDTO = new RequestWrapper<>();
		reqWrapperDTO.setId("ID");
		reqWrapperDTO.setMetadata(null);
		reqWrapperDTO.setRequest(requestDto);
		reqWrapperDTO.setRequesttime(LocalDateTime.now());
		reqWrapperDTO.setVersion("v1.0");
		String json = objectMapper.writeValueAsString(reqWrapperDTO);
		mockMvc.perform(post("/sms/send").contentType(MediaType.APPLICATION_JSON).content(json))
				.andExpect(status().isOk());
	}

	@WithUserDetails("individual")
	@Test
	public void invalidContactNumberTest() throws Exception {
		SmsRequestDto requestDto = new SmsRequestDto();
		requestDto.setMessage("sdjnjkdfj");
		requestDto.setNumber("sdjnjkdfj");

		RequestWrapper<SmsRequestDto> reqWrapperDTO = new RequestWrapper<>();
		reqWrapperDTO.setId("ID");
		reqWrapperDTO.setMetadata(null);
		reqWrapperDTO.setRequest(requestDto);
		reqWrapperDTO.setRequesttime(LocalDateTime.now());
		reqWrapperDTO.setVersion("v1.0");
		String json = objectMapper.writeValueAsString(reqWrapperDTO);
		mockMvc.perform(post("/sms/send").contentType(MediaType.APPLICATION_JSON).content(json))
				.andExpect(status().isOk());
	}
}
