package io.mosip.kernel.signature.test.integration;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

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
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.kernel.core.crypto.spi.CryptoCoreSpec;
import io.mosip.kernel.core.http.RequestWrapper;
import io.mosip.kernel.core.keymanager.spi.KeyStore;
import io.mosip.kernel.core.signatureutil.model.SignatureResponse;
import io.mosip.kernel.core.util.CryptoUtil;
import io.mosip.kernel.keygenerator.bouncycastle.KeyGenerator;
import io.mosip.kernel.keymanagerservice.dto.PublicKeyResponse;
import io.mosip.kernel.signature.dto.SignatureResponseDto;
import io.mosip.kernel.keymanagerservice.service.KeymanagerService;
import io.mosip.kernel.keymanagerservice.test.KeymanagerTestBootApplication;
import io.mosip.kernel.signature.dto.TimestampRequestDto;
import io.mosip.kernel.signature.service.SignatureService;

@SpringBootTest(classes = KeymanagerTestBootApplication.class)
@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
public class CryptoSignatureIntegrationTest {

	@Autowired
	private MockMvc mockMvc;
	
	private RequestWrapper<TimestampRequestDto> requestWrapper;
	
	@Autowired
	private CryptoCoreSpec<byte[], byte[], SecretKey, PublicKey, PrivateKey, String> cryptoCore;

	
	@MockBean
	private KeyStore keyStore;
	
	@Autowired
	private KeyGenerator generator;

	
	private KeyPair keyPair;
	
	@Autowired
	private ObjectMapper objectMapper;

	/** The key manager. */
	@MockBean
	private KeymanagerService keyManagerService;

	@MockBean
	private SignatureService signatureService;

	@MockBean
	private RestTemplate restTemplate;

	private static final String SIGNRESPONSEREQUEST = "{ \"id\": \"string\", \"metadata\": {}, \"request\": { \"data\": \"admin\" }, \"requesttime\": \"2018-12-10T06:12:52.994Z\", \"version\": \"string\" }";
	private static final String VALIDATEWITHPUBLICKEY = "{ \"id\": \"string\", \"metadata\": {}, \"request\": { \"publickey\": \"MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAnoocJbIeMuAzqSzuJX9CvXmFFka3Fz3C-u9vz6c8RsJSKBCe_SAOi31IvL992kuy1qO4XTS-cUuirx-djuF0E7r5TbQFKlNa-FoPJu8QRIGw2rWVQsc2c0Aqd5cfhr9fgTsM3V3URl1jXY645v9EPE0Ih5E26ld6JQQQ90mpvoa6XlJEf5SUAOuzvr5ws5VoZgEQ6wjO05dZSaEL9vrA5npsNSwLb55FqZb7w9qLZfYbPOBVxUZ-HTddBLP6KvlIHWzsVapjvhUHPgSO0AZDYmx3kkKb7jFuWelPibNyKy619AAnlQX3VR39CKi-6sPLRABs4v-npsFLNz9Wd_VJHwIDAQAB\", \"data\": \"admin\", \"signature\": \"ZeNsCOsdgf0UgpXDMry82hrHS6b1ZKvS-tZ_3HBGQHleIu1fZA6LNTtx7XZPFeC8dxsyuYO_iN3mVExM4J2tPlebzsRtuxHigi9o7DI_2xGqFudzlgoH55CP_BBNUDmGm6m-lTMkRx6X61dKfKDNo2NipZdM-a_cHf6Z0aVAU4LdJhV4xWOOm8Pb8sYIc2Nf6kUJRiidEGrxonUCfXX1XlnjMAo75wu99pN8G0mc7JhOehUqbwuXwKo4sQ694ae4F_AYl70sepX24v-0k0ga9esXR4i9rKaoHbzhQFtt2hangQkxHajq9ZTrXWMhd4msTzjHCKdEPXQFsTbKrgKtDQ\" }, \"requesttime\": \"2018-12-10T06:12:52.994Z\", \"version\": \"string\" }";
	private static final String VALIDATEWITHTIMESTAMP = "{ \"id\": \"string\", \"metadata\": {}, \"request\": { \"signature\": \"DrgkF2vm4WvBe04UNe-RePRcrg77uQpsH3GENRcglBsid-K0UDReeeZVKwimOdwV7Ht1j-_D1BFf2sCrM8ni7ztE5Xc_3TEaniOAnOgZDRSI0GG-uSqjH51AwTSl1PYdStfXtOn6HEfEU68JG7TdAliDI5C7thJ1YNmPnHusIsZzX6sW_VfvSpLeA_RzCqnUDH_VaEzZt_5zRYiQv9van4wt0P7HTfIBlQ5zaeO3wXOc3Pogct3ssKwqdaMmZdc7QTDOFqDZZVceMTIXKyiH-ZVs_u3QXRysiLVdXoz7d7yXHdWxQtzsfMjY7alMJNgbmu4X26LYNRemn65Mmn6ixA\", \"data\": \"test\", \"timestamp\": \"2019-05-20T07:28:04.269Z\" }, \"requesttime\": \"2018-12-10T06:12:52.994Z\", \"version\": \"string\" }";
	private static final String ID = "mosip.signature.service";
	private static final String VERSION = "V1.0";
	
	StringBuilder builder;
	SignatureResponse signResponse;
	SignatureResponseDto signatureResponseDTO;

	@Before
	public void setup() {
		keyPair = generator.getAsymmetricKey();
		signResponse = new SignatureResponse();
		signatureResponseDTO= new SignatureResponseDto();
		signatureResponseDTO.setData("asdasdsadf4e..soidopasid");
		signResponse.setData("asdasdsadf4e..iosdipoasopd");
		signResponse.setTimestamp(LocalDateTime.now(ZoneOffset.UTC));
		requestWrapper = new RequestWrapper<>();
		requestWrapper.setId(ID);
		requestWrapper.setVersion(VERSION);
		requestWrapper.setRequesttime(LocalDateTime.now(ZoneId.of("UTC")));
	}

	@Test
	@WithUserDetails("reg-processor")
	public void signResponseSuccess() throws Exception {
		when(signatureService.sign(Mockito.any())).thenReturn(signResponse);
		mockMvc.perform(post("/sign").contentType(MediaType.APPLICATION_JSON).content(SIGNRESPONSEREQUEST))
				.andExpect(status().isOk());
	}


	@Test
	@WithUserDetails("reg-processor")
	public void signResponseTimeStampValidationInvalid() throws Exception {
		PublicKeyResponse<String> publicKeyResponseDto = new PublicKeyResponse<>("alias",
				CryptoUtil.encodeBase64(keyPair.getPublic().getEncoded()), LocalDateTime.now(),
				LocalDateTime.now().plusDays(100));
		when(keyManagerService.getSignPublicKey(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(publicKeyResponseDto);
		String data="test";
		String signedData=cryptoCore.sign(data.getBytes(), keyPair.getPrivate());
		TimestampRequestDto dto= new TimestampRequestDto(signedData,"wrongdata",LocalDateTime.now(ZoneId.of("UTC")));
		requestWrapper.setRequest(dto);
		mockMvc.perform(post("/validate").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(requestWrapper)))
				.andExpect(status().isOk());
	}

	@Test
	@WithUserDetails("reg-processor")
	public void signResponseTimeStampValidationValid() throws Exception {
		PublicKeyResponse<String> publicKeyResponseDto = new PublicKeyResponse<>("alias",
				CryptoUtil.encodeBase64(keyPair.getPublic().getEncoded()), LocalDateTime.now(),
				LocalDateTime.now().plusDays(100));
		when(keyManagerService.getSignPublicKey(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(publicKeyResponseDto);
		String data="test";
		String signedData=cryptoCore.sign(data.getBytes(), keyPair.getPrivate());
		TimestampRequestDto dto= new TimestampRequestDto(signedData,data,LocalDateTime.now(ZoneId.of("UTC")));
		requestWrapper.setRequest(dto);
		mockMvc.perform(post("/validate").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(requestWrapper)))
				.andExpect(status().isOk());
	}

	@Test
	@WithUserDetails("reg-processor")
	public void signResponseTimeStampValidationException() throws Exception {
		when(keyManagerService.getSignPublicKey(Mockito.any(), Mockito.any(), Mockito.any())).thenThrow(RuntimeException.class);
		String data="test";
		String signedData=cryptoCore.sign(data.getBytes(), keyPair.getPrivate());
		TimestampRequestDto dto= new TimestampRequestDto(signedData,data,LocalDateTime.now(ZoneId.of("UTC")));
		requestWrapper.setRequest(dto);
		mockMvc.perform(post("/validate").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(requestWrapper)))
				.andExpect(status().isOk());
	}

}
