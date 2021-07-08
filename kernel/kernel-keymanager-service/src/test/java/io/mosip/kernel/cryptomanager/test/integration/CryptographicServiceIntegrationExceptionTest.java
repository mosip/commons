
package io.mosip.kernel.cryptomanager.test.integration;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;
import java.util.Optional;

import javax.crypto.SecretKey;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.util.UriComponentsBuilder;

import io.mosip.kernel.core.crypto.spi.CryptoCoreSpec;
import io.mosip.kernel.core.http.RequestWrapper;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.keymanager.spi.KeyStore;
import io.mosip.kernel.core.util.CryptoUtil;
import io.mosip.kernel.core.util.DateUtils;
import io.mosip.kernel.cryptomanager.dto.CryptomanagerRequestDto;
import io.mosip.kernel.cryptomanager.dto.CryptomanagerResponseDto;
import io.mosip.kernel.keymanagerservice.dto.KeyPairGenerateResponseDto;
import io.mosip.kernel.keymanagerservice.dto.PublicKeyResponse;
import io.mosip.kernel.keymanagerservice.service.KeymanagerService;
import io.mosip.kernel.keymanagerservice.test.KeymanagerTestBootApplication;

@SpringBootTest(classes = KeymanagerTestBootApplication.class)
@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
public class CryptographicServiceIntegrationExceptionTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	private KeyStore keyStore;

	/** The key manager. */
	@MockBean
	private KeymanagerService keyManagerService;

	@Autowired
	private ObjectMapper mapper;

	private MockRestServiceServer server;

	private UriComponentsBuilder builder;

	private Map<String, String> uriParams;

	private CryptomanagerRequestDto requestDto;

	private RequestWrapper<CryptomanagerRequestDto> requestWrapper;

	/**
	 * {@link CryptoCoreSpec} instance for cryptographic functionalities.
	 */
	@MockBean
	private CryptoCoreSpec<byte[], byte[], SecretKey, PublicKey, PrivateKey, String> cryptoCore;

	private static final String ID = "mosip.crypto.service";
	private static final String VERSION = "V1.0";

	@Before
	public void setUp() {
		mapper = new ObjectMapper();
		mapper.registerModule(new JavaTimeModule());

		requestWrapper = new RequestWrapper<>();
		requestWrapper.setId(ID);
		requestWrapper.setVersion(VERSION);
		requestWrapper.setRequesttime(LocalDateTime.now(ZoneId.of("UTC")));

	}

	@WithUserDetails("reg-processor")
	@Ignore
	@Test
	public void testInvalidSpecEncrypt() throws Exception {

		KeyPairGenerateResponseDto keyPairGenerateResponseDto = new KeyPairGenerateResponseDto("badCertificateData", null, LocalDateTime.now(),
				LocalDateTime.now().plusDays(100), LocalDateTime.now());
	
		String appid = "REGISTRATION";
		String data = "dXJ2aWw";
		String refid = "ref123";
		String timeStamp = "2018-12-06T12:07:44.403Z";

		requestDto = new CryptomanagerRequestDto();
		requestWrapper.setRequest(requestDto);
		requestDto.setApplicationId(appid);
		requestDto.setData(data);
		requestDto.setReferenceId(refid);
		requestDto.setTimeStamp(DateUtils.parseToLocalDateTime(timeStamp));

		when(keyManagerService.getCertificate(Mockito.eq(appid), Mockito.eq(Optional.of(refid))))
						.thenReturn(keyPairGenerateResponseDto);
		String requestBody = objectMapper.writeValueAsString(requestWrapper);
		MvcResult result = mockMvc
				.perform(post("/encrypt").contentType(MediaType.APPLICATION_JSON).content(requestBody))
				.andExpect(status().isOk()).andReturn();
		ResponseWrapper<CryptomanagerResponseDto> responseWrapper = objectMapper.readValue(
				result.getResponse().getContentAsString(),
				new TypeReference<ResponseWrapper<CryptomanagerResponseDto>>() {
				});
		assertThat(responseWrapper.getErrors().get(0).getErrorCode(), is("KER-KMS-013"));
	}

	@WithUserDetails("reg-processor")

	@Test
	public void testMethodArgumentNotValidException() throws Exception {
		requestDto = new CryptomanagerRequestDto();
		requestWrapper.setRequest(requestDto);

		requestDto.setApplicationId("");
		requestDto.setData("");
		requestDto.setReferenceId("ref123");
		requestDto.setTimeStamp(DateUtils.parseToLocalDateTime("2018-12-06T12:07:44.403Z"));
		String requestBody = objectMapper.writeValueAsString(requestWrapper);
		MvcResult result = mockMvc
				.perform(post("/encrypt").contentType(MediaType.APPLICATION_JSON).content(requestBody))
				.andExpect(status().isOk()).andReturn();
		ResponseWrapper<CryptomanagerResponseDto> responseWrapper = objectMapper.readValue(
				result.getResponse().getContentAsString(),
				new TypeReference<ResponseWrapper<CryptomanagerResponseDto>>() {
				});
		assertThat(responseWrapper.getErrors().get(0).getErrorCode(), is("KER-KMS-005"));
	}

	@WithUserDetails("reg-processor")

	@Test
	public void testInvalidFormatException() throws Exception {
		String requestBody = "{\r\n" + "\"id\":\"\",\r\n" + "\"version\":\"\",\r\n" + "\"requesttime\":\"\",\r\n"
				+ "\"metadata\":{},\r\n" + "\"request\":{\r\n" + "  \"applicationId\": \"REGISTRATION\",\r\n"
				+ "  \"data\": \"dXJ2aWwKCgoKam9zaGk=\",\r\n" + "  \"referenceId\": \"REF01\",\r\n"
				+ "  \"timeStamp\": \"2018-12-1\"\r\n" + "}\r\n" + "}";
		MvcResult result = mockMvc
				.perform(post("/encrypt").contentType(MediaType.APPLICATION_JSON).content(requestBody))
				.andExpect(status().isOk()).andReturn();
		ResponseWrapper<CryptomanagerResponseDto> responseWrapper = objectMapper.readValue(
				result.getResponse().getContentAsString(),
				new TypeReference<ResponseWrapper<CryptomanagerResponseDto>>() {
				});
		assertThat(responseWrapper.getErrors().get(0).getErrorCode(), is("KER-KMS-005"));
	}

	@WithUserDetails("reg-processor")

	@Test
	public void testIllegalArgumentException() throws Exception {
		requestDto = new CryptomanagerRequestDto();
		requestWrapper.setRequest(requestDto);

		requestDto.setApplicationId("REGISTRATION");
		requestDto.setData("dXJ2aWw");
		requestDto.setReferenceId("ref123");
		requestDto.setTimeStamp(DateUtils.parseToLocalDateTime("2018-12-06T12:07:44.403Z"));
		String requestBody = objectMapper.writeValueAsString(requestWrapper);
		MvcResult result = mockMvc
				.perform(post("/decrypt").contentType(MediaType.APPLICATION_JSON).content(requestBody))
				.andExpect(status().isOk()).andReturn();
		ResponseWrapper<CryptomanagerResponseDto> responseWrapper = objectMapper.readValue(
				result.getResponse().getContentAsString(),
				new TypeReference<ResponseWrapper<CryptomanagerResponseDto>>() {
				});
		assertThat(responseWrapper.getErrors().get(0).getErrorCode(), is("KER-CRY-003"));
	}
	
}
