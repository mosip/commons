package io.mosip.kernel.otpmanager.test.service;

import static org.hamcrest.CoreMatchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

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

import io.mosip.kernel.otpmanager.entity.OtpEntity;
import io.mosip.kernel.otpmanager.repository.OtpRepository;
import io.mosip.kernel.otpmanager.test.OtpmanagerTestBootApplication;

@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@SpringBootTest(classes = OtpmanagerTestBootApplication.class)
public class OtpValidatorServiceTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	OtpRepository repository;

	@WithUserDetails("individual")
	@Test
	public void testOtpValidatorServicePositiveCase() throws Exception {
		OtpEntity entity = new OtpEntity();
		//Hash of 1234 
		entity.setOtp("03AC674216F3E15C761EE1A5E255F067953623C8B388B4459E13F978D7C846F4");
		//Hash of testKey
		entity.setId("15291F67D99EA7BC578C3544DADFBB991E66FA69CB36FF70FE30E798E111FF5F");
		entity.setValidationRetryCount(0);
		entity.setStatusCode("OTP_UNUSED");
		entity.setUpdatedDtimes(LocalDateTime.now(ZoneId.of("UTC")).plusSeconds(50));
		when(repository.findById(OtpEntity.class, "15291F67D99EA7BC578C3544DADFBB991E66FA69CB36FF70FE30E798E111FF5F")).thenReturn(entity);
		mockMvc.perform(get("/otp/validate?key=testKey&otp=1234").contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andExpect(jsonPath("$.response.status", is("success")));
	}

	@WithUserDetails("individual")
	@Test
	public void testOtpValidatorServiceNegativeCase() throws Exception {
		OtpEntity entity = new OtpEntity();
		//Hash of 1234 
		entity.setOtp("03AC674216F3E15C761EE1A5E255F067953623C8B388B4459E13F978D7C846F4");
		//Hash of testKey
		entity.setId("15291F67D99EA7BC578C3544DADFBB991E66FA69CB36FF70FE30E798E111FF5F");
		entity.setValidationRetryCount(0);
		entity.setStatusCode("OTP_UNUSED");
		entity.setUpdatedDtimes(LocalDateTime.now());
		when(repository.findById(OtpEntity.class, "15291F67D99EA7BC578C3544DADFBB991E66FA69CB36FF70FE30E798E111FF5F")).thenReturn(entity);
		mockMvc.perform(get("/otp/validate?key=testKey&otp=5431").contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andExpect(jsonPath("$.response.status", is("failure")));
	}

	@WithUserDetails("individual")
	@Test
	public void testOtpValidatorServiceWhenMaxAttemptReached() throws Exception {
		OtpEntity entity = new OtpEntity();
		//Hash of 1234 
		entity.setOtp("03AC674216F3E15C761EE1A5E255F067953623C8B388B4459E13F978D7C846F4");
		//Hash of testKey
		entity.setId("15291F67D99EA7BC578C3544DADFBB991E66FA69CB36FF70FE30E798E111FF5F");
		entity.setValidationRetryCount(3);
		entity.setStatusCode("OTP_UNUSED");
		entity.setUpdatedDtimes(LocalDateTime.now());
		when(repository.findById(OtpEntity.class, "15291F67D99EA7BC578C3544DADFBB991E66FA69CB36FF70FE30E798E111FF5F")).thenReturn(entity);
		mockMvc.perform(get("/otp/validate?key=testKey&otp=5431").contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andExpect(jsonPath("$.response.status", is("failure")));
	}

	@WithUserDetails("individual")
	@Test
	public void testOtpValidatorServiceWhenKeyFreezedPositiveCase() throws Exception {
		OtpEntity entity = new OtpEntity();
		//Hash of 1234 
		entity.setOtp("03AC674216F3E15C761EE1A5E255F067953623C8B388B4459E13F978D7C846F4");
		//Hash of testKey
		entity.setId("15291F67D99EA7BC578C3544DADFBB991E66FA69CB36FF70FE30E798E111FF5F");
		entity.setValidationRetryCount(3);
		entity.setStatusCode("KEY_FREEZED");
		entity.setUpdatedDtimes(LocalDateTime.now(ZoneId.of("UTC")).minus(1, ChronoUnit.MINUTES));
		when(repository.findById(OtpEntity.class, "15291F67D99EA7BC578C3544DADFBB991E66FA69CB36FF70FE30E798E111FF5F")).thenReturn(entity);
		mockMvc.perform(get("/otp/validate?key=testKey&otp=2345").contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andExpect(jsonPath("$.response.status", is("failure")));
	}

	@WithUserDetails("individual")
	@Test
	public void testOtpValidatorServiceWhenKeyFreezedNegativeCase() throws Exception {
		OtpEntity entity = new OtpEntity();
		//Hash of 1234 
		entity.setOtp("03AC674216F3E15C761EE1A5E255F067953623C8B388B4459E13F978D7C846F4");
		//Hash of testKey
		entity.setId("15291F67D99EA7BC578C3544DADFBB991E66FA69CB36FF70FE30E798E111FF5F");
		entity.setValidationRetryCount(0);
		entity.setStatusCode("KEY_FREEZED");
		entity.setUpdatedDtimes(LocalDateTime.now().minus(20, ChronoUnit.SECONDS));
		when(repository.findById(OtpEntity.class, "15291F67D99EA7BC578C3544DADFBB991E66FA69CB36FF70FE30E798E111FF5F")).thenReturn(entity);
		mockMvc.perform(get("/otp/validate?key=testKey&otp=1234").contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andExpect(jsonPath("$.response.status", is("failure")));
	}

}
