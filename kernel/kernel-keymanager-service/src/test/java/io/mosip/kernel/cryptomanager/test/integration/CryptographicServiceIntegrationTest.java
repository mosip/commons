package io.mosip.kernel.cryptomanager.test.integration;

import static org.hamcrest.CoreMatchers.isA;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

import javax.crypto.SecretKey;

import org.junit.Before;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import io.mosip.kernel.core.crypto.spi.CryptoCoreSpec;
import io.mosip.kernel.core.http.RequestWrapper;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.keymanager.spi.KeyStore;
import io.mosip.kernel.core.util.CryptoUtil;
import io.mosip.kernel.core.util.DateUtils;
import io.mosip.kernel.cryptomanager.dto.CryptoWithPinRequestDto;
import io.mosip.kernel.cryptomanager.dto.CryptoWithPinResponseDto;
import io.mosip.kernel.cryptomanager.dto.CryptomanagerRequestDto;
import io.mosip.kernel.cryptomanager.dto.CryptomanagerResponseDto;
import io.mosip.kernel.cryptomanager.util.CryptomanagerUtils;
import io.mosip.kernel.keygenerator.bouncycastle.KeyGenerator;
import io.mosip.kernel.keymanager.hsm.util.CertificateUtility;
import io.mosip.kernel.keymanagerservice.dto.KeyPairGenerateResponseDto;
import io.mosip.kernel.keymanagerservice.dto.PublicKeyResponse;
import io.mosip.kernel.keymanagerservice.dto.SymmetricKeyRequestDto;
import io.mosip.kernel.keymanagerservice.dto.SymmetricKeyResponseDto;
import io.mosip.kernel.keymanagerservice.service.KeymanagerService;
import io.mosip.kernel.keymanagerservice.test.KeymanagerTestBootApplication;
import io.mosip.kernel.keymanagerservice.util.KeymanagerUtil;

@SpringBootTest(classes = KeymanagerTestBootApplication.class)
@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
public class CryptographicServiceIntegrationTest {

	/**
	 * {@link CryptoCoreSpec} instance for cryptographic functionalities.
	 */
	@MockBean
	private CryptoCoreSpec<byte[], byte[], SecretKey, PublicKey, PrivateKey, String> cryptoCore;

	@MockBean
	private CryptomanagerUtils cryptomanagerUtil;

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private KeyGenerator generator;

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	private KeyStore keyStore;

	/** The key manager. */
	@MockBean
	private KeymanagerService keyManagerService;

	@MockBean
	private KeymanagerUtil keymanagerUtil;

	private KeyPair keyPair;

	private Certificate cert;

	private String certData;

	private CryptomanagerRequestDto requestDto;

	private RequestWrapper<CryptomanagerRequestDto> requestWrapper;

	private CryptoWithPinRequestDto requestWithPinDto;

	private RequestWrapper<CryptoWithPinRequestDto> requestWithPinWrapper;

	private static final String ID = "mosip.crypto.service";
	private static final String VERSION = "V1.0";

	@Before
	public void setUp() {
		objectMapper = new ObjectMapper();
		objectMapper.registerModule(new JavaTimeModule());

		keyPair = generator.getAsymmetricKey();
		cert = CertificateUtility.generateX509Certificate(keyPair.getPrivate(), keyPair.getPublic(), 
				"mosip", "mosip", "mosip",
			"india", LocalDateTime.of(2010, 1, 1, 12, 00), LocalDateTime.of(2011, 1, 1, 12, 00), "SHA256withRSA", "BC");
		certData = keymanagerUtil.getPEMFormatedData(cert);
		requestWrapper = new RequestWrapper<>();
		requestWrapper.setId(ID);
		requestWrapper.setVersion(VERSION);
		requestWrapper.setRequesttime(LocalDateTime.now(ZoneId.of("UTC")));

		requestWithPinWrapper = new RequestWrapper<>();
		requestWithPinWrapper.setId(ID);
		requestWithPinWrapper.setVersion(VERSION);
		requestWithPinWrapper.setRequesttime(LocalDateTime.now(ZoneId.of("UTC")));
	}

	@WithUserDetails("reg-processor")
	@Test
	public void testEncrypt() throws Exception {
		KeyPairGenerateResponseDto responseDto = new KeyPairGenerateResponseDto(certData, null, LocalDateTime.now(), 
					LocalDateTime.now(), LocalDateTime.now());
		when(cryptoCore.symmetricEncrypt(Mockito.any(), Mockito.any(), Mockito.any()))
				.thenReturn("MOCKENCRYPTEDDATA".getBytes());
		when(cryptoCore.asymmetricEncrypt(Mockito.any(), Mockito.any()))
				.thenReturn("MOCKENCRYPTEDSESSIONKEY".getBytes());

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
		when(cryptomanagerUtil.isDataValid(Mockito.anyString())).thenReturn(true);
		when(cryptomanagerUtil.generateRandomBytes(Mockito.anyInt())).thenReturn("RANDOMBYTES".getBytes());
		when(cryptomanagerUtil.concatByteArrays(Mockito.any(), Mockito.any())).thenReturn("CONCATEDHEADER".getBytes());
		when(keyManagerService.getCertificate(Mockito.eq(appid), Mockito.eq(Optional.of(refid))))
				.thenReturn(responseDto);
		when(cryptomanagerUtil.getCertificate(Mockito.any())).thenReturn(cert);
		when(cryptomanagerUtil.getCertificateThumbprint(Mockito.any())).thenReturn("CERTTHUMBPRINT".getBytes());
		when(cryptomanagerUtil.concatCertThumbprint(Mockito.any(), Mockito.any())).thenReturn("CONCATEDENCRYPTEDSESSIONKEY".getBytes());
		
		String requestBody = objectMapper.writeValueAsString(requestWrapper);

		MvcResult result = mockMvc
				.perform(post("/encrypt").contentType(MediaType.APPLICATION_JSON).content(requestBody))
				.andExpect(status().isOk()).andReturn();
		ResponseWrapper<?> responseWrapper = objectMapper.readValue(result.getResponse().getContentAsString(),
				ResponseWrapper.class);
		CryptomanagerResponseDto cryptomanagerResponseDto = objectMapper.readValue(
				objectMapper.writeValueAsString(responseWrapper.getResponse()), CryptomanagerResponseDto.class);

		assertThat(cryptomanagerResponseDto.getData(), isA(String.class));
	}

	@WithUserDetails("reg-processor")
	@Test
	public void testDecrypt() throws Exception {
		SymmetricKeyResponseDto symmetricKeyResponseDto = new SymmetricKeyResponseDto(
				CryptoUtil.encodeBase64(generator.getSymmetricKey().getEncoded()));

		when(cryptoCore.symmetricDecrypt(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn("dXJ2aWw".getBytes());
       
		requestDto = new CryptomanagerRequestDto();
		requestWrapper.setRequest(requestDto);
		String appid = "REGISTRATION";
		String data = "dXJ2aWwjS0VZX1NQTElUVEVSI3Vydmls";
		String refid = "ref123";
		LocalDateTime timeStamp = DateUtils.parseToLocalDateTime("2018-12-06T12:07:44.403Z");
		requestDto.setApplicationId(appid);
		requestDto.setData(data);
		requestDto.setReferenceId("ref123");
		requestDto.setTimeStamp(timeStamp);
		SymmetricKeyRequestDto symmetricKeyRequestDto = new SymmetricKeyRequestDto(
				appid, timeStamp,
				refid, data, true);
		when(keyManagerService.decryptSymmetricKey(Mockito.any())).thenReturn(symmetricKeyResponseDto);
		when(cryptomanagerUtil.parseEncryptKeyHeader(Mockito.any())).thenReturn("".getBytes());
		String requestBody = objectMapper.writeValueAsString(requestWrapper);
		MvcResult result = mockMvc
				.perform(post("/decrypt").contentType(MediaType.APPLICATION_JSON).content(requestBody))
				.andExpect(status().isOk()).andReturn();
		ResponseWrapper<?> responseWrapper = objectMapper.readValue(result.getResponse().getContentAsString(),
				ResponseWrapper.class);
		CryptomanagerResponseDto cryptomanagerResponseDto = objectMapper.readValue(
				objectMapper.writeValueAsString(responseWrapper.getResponse()), CryptomanagerResponseDto.class);

		assertThat(cryptomanagerResponseDto.getData(), isA(String.class));
	}

	@WithUserDetails("reg-processor")
	@Test
	public void testEncryptWithPin() throws Exception {
		when(cryptoCore.hash(Mockito.any(), Mockito.any())).thenReturn("MOCKSECRETKEY");
		when(cryptoCore.symmetricEncrypt(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
				.thenReturn("MOCKENCRYPTEDDATA".getBytes());
		when(cryptomanagerUtil.hexDecode(Mockito.any())).thenReturn("MOCKHEXDATA".getBytes());
		
		requestWithPinDto = new CryptoWithPinRequestDto();
		requestWithPinDto.setData("Test Pin Encryption.");
		requestWithPinDto.setUserPin("AB1234");
		requestWithPinWrapper.setRequest(requestWithPinDto);
		
		when(cryptomanagerUtil.isDataValid(Mockito.anyString())).thenReturn(true);
		String requestBody = objectMapper.writeValueAsString(requestWithPinWrapper);

		MvcResult result = mockMvc
				.perform(post("/encryptWithPin").contentType(MediaType.APPLICATION_JSON).content(requestBody))
				.andExpect(status().isOk()).andReturn();
		ResponseWrapper<?> responseWrapper = objectMapper.readValue(result.getResponse().getContentAsString(),
				ResponseWrapper.class);
		CryptoWithPinResponseDto responseDto = objectMapper.readValue(
				objectMapper.writeValueAsString(responseWrapper.getResponse()), CryptoWithPinResponseDto.class);

		assertThat(responseDto.getData(), isA(String.class));
	}

	@WithUserDetails("reg-processor")
	@Test
	public void testDecryptWithPin() throws Exception {
		when(cryptoCore.hash(Mockito.any(), Mockito.any())).thenReturn("MOCKSECRETKEY");
		when(cryptoCore.symmetricDecrypt(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
				.thenReturn("MOCKENCRYPTEDDATA".getBytes());
		when(cryptomanagerUtil.hexDecode(Mockito.any())).thenReturn("MOCKHEXDATA".getBytes());
		
		requestWithPinDto = new CryptoWithPinRequestDto();
		requestWithPinDto.setData("GeB26aCD779DlCzRKkHlwAyctlI1Fh5SvLTctR_8uCZW-OOUombMq_Pt9eM4r40nWxoD_Mt-j3OVd9t9uXrcmECh5ec");
		requestWithPinDto.setUserPin("AB1234");
		requestWithPinWrapper.setRequest(requestWithPinDto);
		
		when(cryptomanagerUtil.isDataValid(Mockito.anyString())).thenReturn(true);
		String requestBody = objectMapper.writeValueAsString(requestWithPinWrapper);

		MvcResult result = mockMvc
				.perform(post("/decryptWithPin").contentType(MediaType.APPLICATION_JSON).content(requestBody))
				.andExpect(status().isOk()).andReturn();
		ResponseWrapper<?> responseWrapper = objectMapper.readValue(result.getResponse().getContentAsString(),
				ResponseWrapper.class);
		CryptoWithPinResponseDto responseDto = objectMapper.readValue(
				objectMapper.writeValueAsString(responseWrapper.getResponse()), CryptoWithPinResponseDto.class);

		assertThat(responseDto.getData(), isA(String.class));
	}
}
