
package io.mosip.kernel.cryptomanager.test.util;

import static org.mockito.Mockito.when;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.time.LocalDateTime;

import javax.crypto.SecretKey;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.kernel.core.crypto.spi.CryptoCoreSpec;
import io.mosip.kernel.core.keymanager.spi.KeyStore;
import io.mosip.kernel.core.util.CryptoUtil;
import io.mosip.kernel.cryptomanager.dto.CryptomanagerRequestDto;
import io.mosip.kernel.cryptomanager.util.CryptomanagerUtils;
import io.mosip.kernel.keygenerator.bouncycastle.KeyGenerator;
import io.mosip.kernel.keymanagerservice.dto.KeyPairGenerateResponseDto;
import io.mosip.kernel.keymanagerservice.dto.SymmetricKeyResponseDto;
import io.mosip.kernel.keymanagerservice.exception.KeymanagerServiceException;
import io.mosip.kernel.keymanagerservice.service.KeymanagerService;
import io.mosip.kernel.keymanagerservice.test.KeymanagerTestBootApplication;

@SpringBootTest(classes = KeymanagerTestBootApplication.class)

@RunWith(SpringRunner.class)

@AutoConfigureMockMvc
public class CryptographicUtilWithKeyManagerTest {

	@MockBean
	private KeyStore keyStore;


	@Autowired
	private CryptomanagerUtils cryptomanagerUtil;

	@MockBean
	private ObjectMapper objectMapper;

	@MockBean
	private KeymanagerService keyManagerService;

	@Autowired
	private KeyGenerator generator;

	@Autowired
	private RestTemplate restTemplate;

	/**
	 * {@link CryptoCoreSpec} instance for cryptographic functionalities.
	 */
	@MockBean
	private CryptoCoreSpec<byte[], byte[], SecretKey, PublicKey, PrivateKey, String> cryptoCore;

	@Before
	public void setUp() {
		KeyPairGenerateResponseDto keyPairGenerateResponseDto = new KeyPairGenerateResponseDto();
		keyPairGenerateResponseDto.setCertificate("");
		when(keyManagerService.getCertificate(Mockito.any(), Mockito.any())).thenReturn(keyPairGenerateResponseDto);
		SymmetricKeyResponseDto symmetricKeyResponseDto = new SymmetricKeyResponseDto(
				CryptoUtil.encodeBase64(generator.getSymmetricKey().getEncoded()));
		when(keyManagerService.decryptSymmetricKey(Mockito.any())).thenReturn(symmetricKeyResponseDto);
	}

	@Test(expected = KeymanagerServiceException.class)
	public void testEncrypt() throws Exception {
		cryptomanagerUtil.getCertificate(
				new CryptomanagerRequestDto("REGISTRATION", "ref123", LocalDateTime.parse("2018-12-06T12:07:44.403"),
						"dXJ2aWw", "ykrkpgjjtChlVdvDNJJEnQ", "VGhpcyBpcyBzYW1wbGUgYWFk", false));
	}

	@Test
	public void testDecrypt() throws Exception {
		when(cryptoCore.symmetricDecrypt(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn("dXJ2aWw".getBytes());
		cryptomanagerUtil.getDecryptedSymmetricKey(
				new CryptomanagerRequestDto("REGISTRATION", "ref123", LocalDateTime.parse("2018-12-06T12:07:44.403"),
						"dXJ2aWwjS0VZX1NQTElUVEVSI3Vydmls", "ykrkpgjjtChlVdvDNJJEnQ", "VGhpcyBpcyBzYW1wbGUgYWFk", false));
	}

}
