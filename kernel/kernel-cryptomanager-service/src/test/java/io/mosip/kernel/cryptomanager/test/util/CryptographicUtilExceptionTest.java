package io.mosip.kernel.cryptomanager.test.util;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.kernel.core.exception.NoSuchAlgorithmException;
import io.mosip.kernel.core.exception.ServiceError;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.util.CryptoUtil;
import io.mosip.kernel.cryptomanager.dto.CryptomanagerRequestDto;
import io.mosip.kernel.cryptomanager.dto.KeymanagerPublicKeyResponseDto;
import io.mosip.kernel.cryptomanager.test.CryptoManagerTestBootApplication;
import io.mosip.kernel.cryptomanager.util.CryptomanagerUtils;

@SpringBootTest(classes = CryptoManagerTestBootApplication.class)
@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
public class CryptographicUtilExceptionTest {

	@Value("${mosip.kernel.keymanager-service-publickey-url}")
	private String publicKeyUrl;

	@Autowired
	CryptomanagerUtils cryptomanagerUtil;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private RestTemplate restTemplate;

	private MockRestServiceServer server;

	private UriComponentsBuilder builder;

	private Map<String, String> uriParams;

	@Before
	public void setUp() {
		server = MockRestServiceServer.bindTo(restTemplate).build();
		ReflectionTestUtils.setField(cryptomanagerUtil, "asymmetricAlgorithmName", "test");
		uriParams = new HashMap<>();
		uriParams.put("applicationId", "REGISTRATION");
		builder = UriComponentsBuilder.fromUriString(publicKeyUrl).queryParam("timeStamp", "2018-12-06T12:07:44.403Z")
				.queryParam("referenceId", "ref123");
	}

	@Test(expected = NoSuchAlgorithmException.class)
	public void testNoSuchAlgorithmEncrypt() throws Exception {
		ResponseWrapper<KeymanagerPublicKeyResponseDto> responseWrapper = new ResponseWrapper<>();
		List<ServiceError> errors = new ArrayList<>();
		responseWrapper.setErrors(errors);
		KeymanagerPublicKeyResponseDto keymanagerPublicKeyResponseDto = new KeymanagerPublicKeyResponseDto(
				CryptoUtil.encodeBase64("badprivatekey".getBytes()), LocalDateTime.now(),
				LocalDateTime.now().plusDays(100));
		responseWrapper.setResponse(keymanagerPublicKeyResponseDto);
		server.expect(requestTo(builder.buildAndExpand(uriParams).toUriString()))
				.andRespond(withSuccess(objectMapper.writeValueAsString(responseWrapper), MediaType.APPLICATION_JSON));
		CryptomanagerRequestDto cryptomanagerRequestDto = new CryptomanagerRequestDto("REGISTRATION", "ref123",
				LocalDateTime.parse("2018-12-06T12:07:44.403Z", DateTimeFormatter.ISO_DATE_TIME), "test",
				"ykrkpgjjtChlVdvDNJJEnQ", "VGhpcyBpcyBzYW1wbGUgYWFk");
		cryptomanagerUtil.getPublicKey(cryptomanagerRequestDto);
	}
}
