package io.mosip.kernel.otpmanager.test.service;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;

import io.mosip.kernel.core.otpmanager.spi.OtpGenerator;
import io.mosip.kernel.core.otpmanager.spi.OtpValidator;
import io.mosip.kernel.otpmanager.constant.OtpStatusConstants;
import io.mosip.kernel.otpmanager.dto.OtpGeneratorRequestDto;
import io.mosip.kernel.otpmanager.dto.OtpGeneratorResponseDto;
import io.mosip.kernel.otpmanager.dto.OtpValidatorResponseDto;
import io.mosip.kernel.otpmanager.test.OtpmanagerTestBootApplication;

@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@SpringBootTest(classes = OtpmanagerTestBootApplication.class)
public class LocalProxyTest {
	
	@Value("${mosip.kernel.auth.proxy-otp-value}")
	private String proxyOTP;
	
	@Autowired
	private OtpGenerator<OtpGeneratorRequestDto, OtpGeneratorResponseDto> otpGenerator;

	@Autowired
	private OtpValidator<ResponseEntity<OtpValidatorResponseDto>> otpValidator;
	
	@Before
	public void init() {
		ReflectionTestUtils.setField(otpGenerator, "activeProfile", "local");
		ReflectionTestUtils.setField(otpValidator, "activeProfile", "local");
	}
	
	
	@Test
	public void testOtpGeneratorServiceLocal() throws Exception {
		OtpGeneratorRequestDto otpGeneratorRequestDto = new OtpGeneratorRequestDto();
		otpGeneratorRequestDto.setKey("testKey");
		OtpGeneratorResponseDto otpGeneratorResponseDto = otpGenerator.getOtp(otpGeneratorRequestDto);
		assertThat(otpGeneratorResponseDto.getOtp(), is(proxyOTP));
	}
	
	@Test
	public void testValidatorServiceLocal() throws Exception {
		OtpGeneratorRequestDto otpGeneratorRequestDto = new OtpGeneratorRequestDto();
		otpGeneratorRequestDto.setKey("testKey");
		otpGenerator.getOtp(otpGeneratorRequestDto);
		assertThat(otpValidator.validateOtp(otpGeneratorRequestDto.getKey(), proxyOTP).getBody().getStatus(),is(OtpStatusConstants.SUCCESS_STATUS.getProperty()));
	}
	
	@Test
	public void testValidatorFailServiceLocal() throws Exception {
		OtpGeneratorRequestDto otpGeneratorRequestDto = new OtpGeneratorRequestDto();
		otpGeneratorRequestDto.setKey("testKey");
		otpGenerator.getOtp(otpGeneratorRequestDto);
		assertThat(otpValidator.validateOtp(otpGeneratorRequestDto.getKey(), "128302").getBody().getStatus(),is(OtpStatusConstants.FAILURE_STATUS.getProperty()));
	}
	
	
	
	
	
}
