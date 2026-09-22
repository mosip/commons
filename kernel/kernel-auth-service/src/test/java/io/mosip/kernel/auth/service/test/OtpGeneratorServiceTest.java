package io.mosip.kernel.auth.service.test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import java.nio.charset.Charset;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.kernel.auth.defaultimpl.config.MosipEnvironment;
import io.mosip.kernel.auth.defaultimpl.dto.otp.OtpGenerateResponseDto;
import io.mosip.kernel.auth.defaultimpl.exception.AuthManagerException;
import io.mosip.kernel.auth.defaultimpl.exception.AuthManagerServiceException;
import io.mosip.kernel.auth.defaultimpl.service.OTPGenerateService;
import io.mosip.kernel.auth.test.AuthTestBootApplication;
import io.mosip.kernel.core.authmanager.exception.AuthNException;
import io.mosip.kernel.core.authmanager.exception.AuthZException;
import io.mosip.kernel.core.authmanager.model.MosipUserDto;
import io.mosip.kernel.core.authmanager.model.OtpUser;

/**
 * Tests {@link OTPGenerateService} generate-OTP HTTP success and mapping of
 * MOSIP/IAM error payloads and HTTP 401/403/400 to AuthN/AuthZ/AuthManager
 * exceptions.
 * <p>
 * Uses Boot 4 {@link AutoConfigureMockMvc} from
 * {@code org.springframework.boot.webmvc.test.autoconfigure} and
 * {@link MockitoBean} for {@code authRestTemplate}.
 */
@SpringBootTest(classes = { AuthTestBootApplication.class })
@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
public class OtpGeneratorServiceTest {

	/** Auth RestTemplate replaced with a Mockito bean. */
	@Qualifier("authRestTemplate")
	@MockitoBean
	RestTemplate authRestTemplate;

	/** MOSIP environment URLs used to build the generate-OTP API. */
	@Autowired
	MosipEnvironment mosipEnvironment;

	/** JSON mapper from the test context. */
	@Autowired
	private ObjectMapper mapper;
	
	/** OTP generate service under test. */
	@Autowired
	private OTPGenerateService oTPGenerateService;
	
	
	/**
	 * Asserts multi-channel generate-OTP returns the OTP from a SUCCESS payload.
	 *
	 * @throws Exception if generate fails
	 */
	@Test
	public void generateOTPMultipleChannelsTest() throws Exception  {

		String otpResponse = "{\r\n" + "  \"id\": \"string\", \"version\": \"string\",\r\n"
				+ "  \"responsetime\": \"2022-01-09T19:38:09.740Z\",\r\n" + "  \"metadata\": {},\r\n"
				+ "  \"response\": {\r\n" + "    \"otp\": \"820121\",\r\n" + "    \"status\": \"SUCCESS\"\r\n"
				+ "  },\r\n" + "  \"errors\": [\r\n" + "  ]\r\n" + "}";
		when(authRestTemplate.postForEntity(Mockito.eq( mosipEnvironment.getGenerateOtpApi()),
				Mockito.any(), Mockito.eq(String.class)))
						.thenReturn(ResponseEntity.ok(otpResponse));
		MosipUserDto mosipUserDto = new MosipUserDto();
		mosipUserDto.setName("mock-user");
		OtpUser otpUser = new OtpUser();
		OtpGenerateResponseDto otpGenerateResponseDto =oTPGenerateService.generateOTPMultipleChannels(mosipUserDto, otpUser, "mock-token");
		assertThat(otpGenerateResponseDto.getOtp(),is("820121"));
	}
	
	/**
	 * Asserts a MOSIP errors payload on generate-OTP raises
	 * {@link AuthManagerServiceException}.
	 *
	 * @throws Exception if generate fails unexpectedly
	 */
	@Test(expected = AuthManagerServiceException.class)
	public void generateOTPMultipleChannelsAuthManagerServiceExceptionTest() throws Exception  {

		String otpResponse = "{\r\n" + "  \"id\": \"string\", \"version\": \"string\",\r\n"
				+ "  \"responsetime\": \"2022-01-09T19:38:09.740Z\",\r\n" + "  \"metadata\": {},\r\n"
				+ "  \"response\": { },\r\n" + "  \"errors\": [{ \"errorCode\": \"KER-OTP-006\", \"message\": \"AuthManagerServiceException\" } ]\r\n" + "}";
		when(authRestTemplate.postForEntity(Mockito.eq( mosipEnvironment.getGenerateOtpApi()),
				Mockito.any(), Mockito.eq(String.class)))
						.thenReturn(ResponseEntity.ok(otpResponse));
		MosipUserDto mosipUserDto = new MosipUserDto();
		mosipUserDto.setName("mock-user");
		OtpUser otpUser = new OtpUser();
		oTPGenerateService.generateOTPMultipleChannels(mosipUserDto, otpUser, "mock-token");
	}
	
	/**
	 * Asserts HTTP 403 with a plain body on multi-channel generate-OTP raises
	 * {@link AuthManagerException}.
	 *
	 * @throws Exception if generate fails unexpectedly
	 */
	@Test(expected = AuthManagerException.class)
	public void generateOTPMultipleChannelsAuthManagerExceptionTest() throws Exception  {

		
		when(authRestTemplate.postForEntity(Mockito.eq( mosipEnvironment.getGenerateOtpApi()),
				Mockito.any(), Mockito.eq(String.class)))
						.thenThrow(new HttpClientErrorException(HttpStatus.FORBIDDEN, "403", "access denied".getBytes(),
								Charset.defaultCharset()));
		MosipUserDto mosipUserDto = new MosipUserDto();
		mosipUserDto.setName("mock-user");
		OtpUser otpUser = new OtpUser();
		oTPGenerateService.generateOTPMultipleChannels(mosipUserDto, otpUser, "mock-token");
		
	}
	
	
	/**
	 * Asserts HTTP 403 with KER-ATH-403 on generate-OTP raises {@link AuthZException}.
	 *
	 * @throws Exception if generate fails unexpectedly
	 */
	@Test(expected = AuthZException.class)
	public void generateOTPAuthZExceptionTest() throws Exception  {


		String resp = "{\r\n" + "  \"id\": \"string\", \"version\": \"string\",\r\n"
				+ "  \"responsetime\": \"2022-01-09T19:38:09.740Z\",\r\n" + "  \"metadata\": {},\r\n"
				+ "  \"response\": { },\r\n" + "  \"errors\": [{ \"errorCode\": \"KER-ATH-403\", \"message\": \"access denied\" } ]\r\n" + "}";
		when(authRestTemplate.postForEntity(Mockito.eq( mosipEnvironment.getGenerateOtpApi()),
				Mockito.any(), Mockito.eq(String.class)))
						.thenThrow(new HttpClientErrorException(HttpStatus.FORBIDDEN, "403", resp.getBytes(),
								Charset.defaultCharset()));
		MosipUserDto mosipUserDto = new MosipUserDto();
		mosipUserDto.setName("mock-user");
		oTPGenerateService.generateOTP(mosipUserDto, "mock-token");
		
	}
	
	/**
	 * Asserts HTTP 403 with a plain body on generate-OTP raises
	 * {@link AuthManagerException}.
	 *
	 * @throws Exception if generate fails unexpectedly
	 */
	@Test(expected = AuthManagerException.class)
	public void generateOTPAuthManagerExceptionForbiddenTest() throws Exception  {

		when(authRestTemplate.postForEntity(Mockito.eq( mosipEnvironment.getGenerateOtpApi()),
				Mockito.any(), Mockito.eq(String.class)))
						.thenThrow(new HttpClientErrorException(HttpStatus.FORBIDDEN, "403", "access denied".getBytes(),
								Charset.defaultCharset()));
		MosipUserDto mosipUserDto = new MosipUserDto();
		mosipUserDto.setName("mock-user");
		oTPGenerateService.generateOTP(mosipUserDto, "mock-token");
		
	}
	
	/**
	 * Asserts HTTP 401 with KER-ATH-401 on generate-OTP raises {@link AuthNException}.
	 *
	 * @throws Exception if generate fails unexpectedly
	 */
	@Test(expected = AuthNException.class)
	public void generateOTPAuthNExceptionTest() throws Exception  {


		String resp = "{\r\n" + "  \"id\": \"string\", \"version\": \"string\",\r\n"
				+ "  \"responsetime\": \"2022-01-09T19:38:09.740Z\",\r\n" + "  \"metadata\": {},\r\n"
				+ "  \"response\": { },\r\n" + "  \"errors\": [{ \"errorCode\": \"KER-ATH-401\", \"message\": \"UNAUTHORIZED\" } ]\r\n" + "}";
		when(authRestTemplate.postForEntity(Mockito.eq( mosipEnvironment.getGenerateOtpApi()),
				Mockito.any(), Mockito.eq(String.class)))
						.thenThrow(new HttpClientErrorException(HttpStatus.UNAUTHORIZED, "401", resp.getBytes(),
								Charset.defaultCharset()));
		MosipUserDto mosipUserDto = new MosipUserDto();
		mosipUserDto.setName("mock-user");
		oTPGenerateService.generateOTP(mosipUserDto, "mock-token");
		
	}
	
	/**
	 * Asserts HTTP 401 with a plain body on generate-OTP raises
	 * {@link AuthManagerException}.
	 *
	 * @throws Exception if generate fails unexpectedly
	 */
	@Test(expected = AuthManagerException.class)
	public void generateOTPAuthManagerExceptionUnAuthTest() throws Exception  {

		when(authRestTemplate.postForEntity(Mockito.eq( mosipEnvironment.getGenerateOtpApi()),
				Mockito.any(), Mockito.eq(String.class)))
						.thenThrow(new HttpClientErrorException(HttpStatus.UNAUTHORIZED, "401", "UNAUTHORIZED".getBytes(),
								Charset.defaultCharset()));
		MosipUserDto mosipUserDto = new MosipUserDto();
		mosipUserDto.setName("mock-user");
		oTPGenerateService.generateOTP(mosipUserDto, "mock-token");
		
	}
	
	/**
	 * Asserts HTTP 400 with a MOSIP errors payload on generate-OTP raises
	 * {@link AuthManagerServiceException}.
	 *
	 * @throws Exception if generate fails unexpectedly
	 */
	@Test(expected = AuthManagerServiceException.class)
	public void generateOTPAuthManagerServiceExceptionTest() throws Exception  {


		String resp = "{\r\n" + "  \"id\": \"string\", \"version\": \"string\",\r\n"
				+ "  \"responsetime\": \"2022-01-09T19:38:09.740Z\",\r\n" + "  \"metadata\": {},\r\n"
				+ "  \"response\": { },\r\n" + "  \"errors\": [{ \"errorCode\": \"KER-OTP-400\", \"message\": \"bad req\" } ]\r\n" + "}";
		when(authRestTemplate.postForEntity(Mockito.eq( mosipEnvironment.getGenerateOtpApi()),
				Mockito.any(), Mockito.eq(String.class)))
						.thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST, "400", resp.getBytes(),
								Charset.defaultCharset()));
		MosipUserDto mosipUserDto = new MosipUserDto();
		mosipUserDto.setName("mock-user");
		oTPGenerateService.generateOTP(mosipUserDto, "mock-token");
		
	}
	
	/**
	 * Asserts HTTP 400 with a plain body on generate-OTP raises
	 * {@link AuthManagerException}.
	 *
	 * @throws Exception if generate fails unexpectedly
	 */
	@Test(expected = AuthManagerException.class)
	public void generateOTPClientErrorTest() throws Exception  {

		when(authRestTemplate.postForEntity(Mockito.eq( mosipEnvironment.getGenerateOtpApi()),
				Mockito.any(), Mockito.eq(String.class)))
						.thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST, "400", "Bad Req".getBytes(),
								Charset.defaultCharset()));
		MosipUserDto mosipUserDto = new MosipUserDto();
		mosipUserDto.setName("mock-user");
		oTPGenerateService.generateOTP(mosipUserDto, "mock-token");
		
	}
	
	/**
	 * Asserts HTTP 200 with a MOSIP errors payload on generate-OTP raises
	 * {@link AuthManagerServiceException}.
	 *
	 * @throws Exception if generate fails unexpectedly
	 */
	@Test(expected = AuthManagerServiceException.class)
	public void generateOTPValidationErrorTest() throws Exception  {
		String resp = "{\r\n" + "  \"id\": \"string\", \"version\": \"string\",\r\n"
				+ "  \"responsetime\": \"2022-01-09T19:38:09.740Z\",\r\n" + "  \"metadata\": {},\r\n"
				+ "  \"response\": { },\r\n" + "  \"errors\": [{ \"errorCode\": \"KER-OTP-400\", \"message\": \"bad req\" } ]\r\n" + "}";
		when(authRestTemplate.postForEntity(Mockito.eq( mosipEnvironment.getGenerateOtpApi()),
				Mockito.any(), Mockito.eq(String.class)))
		.thenReturn(ResponseEntity.ok(resp));
		MosipUserDto mosipUserDto = new MosipUserDto();
		mosipUserDto.setName("mock-user");
		oTPGenerateService.generateOTP(mosipUserDto, "mock-token");
		
	}
}
