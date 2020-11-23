
package io.mosip.kernel.cryptomanager.test.util;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
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
import io.mosip.kernel.core.keymanager.spi.KeyStore;
import io.mosip.kernel.core.util.CryptoUtil;
import io.mosip.kernel.core.util.DateUtils;
import io.mosip.kernel.cryptomanager.dto.CryptomanagerRequestDto;
import io.mosip.kernel.cryptomanager.dto.KeymanagerPublicKeyResponseDto;
import io.mosip.kernel.cryptomanager.util.CryptomanagerUtils;
import io.mosip.kernel.keymanagerservice.dto.KeyPairGenerateResponseDto;
import io.mosip.kernel.keymanagerservice.dto.PublicKeyResponse;
import io.mosip.kernel.keymanagerservice.exception.KeymanagerServiceException;
import io.mosip.kernel.keymanagerservice.service.KeymanagerService;
import io.mosip.kernel.keymanagerservice.test.KeymanagerTestBootApplication;

@SpringBootTest(classes = KeymanagerTestBootApplication.class)

@RunWith(SpringRunner.class)

@AutoConfigureMockMvc

@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
public class CryptographicUtilExceptionTest {


	@Autowired
	CryptomanagerUtils cryptomanagerUtil;

	@MockBean
	private KeyStore keyStore;

	/** The key manager. */
	@MockBean
	private KeymanagerService keyManagerService;

	@Before
	public void setUp() {
		ReflectionTestUtils.setField(cryptomanagerUtil, "asymmetricAlgorithmName", "test");
	
	}

	@Test(expected = KeymanagerServiceException.class)
	public void testNoSuchAlgorithmEncrypt() throws Exception {
		KeyPairGenerateResponseDto keyPairGenerateResponseDto = new KeyPairGenerateResponseDto("badCertificateData", null, LocalDateTime.now(),
				LocalDateTime.now().plusDays(100), LocalDateTime.now());
		String appid = "REGISTRATION";
		String refid = "ref123";

		when(keyManagerService.getCertificate(Mockito.eq(appid), Mockito.eq(Optional.of(refid))))
				.thenReturn(keyPairGenerateResponseDto);
		CryptomanagerRequestDto cryptomanagerRequestDto = new CryptomanagerRequestDto("REGISTRATION", "ref123",
				LocalDateTime.parse("2018-12-06T12:07:44.403Z", DateTimeFormatter.ISO_DATE_TIME), "test",
				"ykrkpgjjtChlVdvDNJJEnQ", "VGhpcyBpcyBzYW1wbGUgYWFk", false);
		cryptomanagerUtil.getCertificate(cryptomanagerRequestDto);
	}
}
