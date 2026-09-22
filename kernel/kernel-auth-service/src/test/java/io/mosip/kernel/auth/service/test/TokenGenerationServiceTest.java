package io.mosip.kernel.auth.service.test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit4.SpringRunner;

import io.mosip.kernel.auth.defaultimpl.service.TokenGenerationService;
import io.mosip.kernel.auth.test.AuthTestBootApplication;
import io.mosip.kernel.core.authmanager.model.AuthNResponseDto;
import io.mosip.kernel.openid.bridge.api.service.AuthService;

/**
 * Tests {@link TokenGenerationService} internal and UIN-based token generation
 * when {@link AuthService#authenticateWithSecretKey} is mocked.
 * <p>
 * Uses Boot 4 {@link AutoConfigureMockMvc} from
 * {@code org.springframework.boot.webmvc.test.autoconfigure} and
 * {@link MockitoBean} for {@link AuthService}.
 */
@SpringBootTest(classes = { AuthTestBootApplication.class })
@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
public class TokenGenerationServiceTest {


	/** Token generation service under test. */
	@Autowired
	private TokenGenerationService tokenGenerationService;
	
	/** Auth service collaborator replaced with a Mockito bean. */
	@MockitoBean
	AuthService authService;

	/** Auth-manager application id from test properties. */
	@Value("${mosip.kernel.auth.app.id}")
	private String authAppId;

	/** Auth-manager client id from test properties. */
	@Value("${mosip.kernel.auth.client.id}")
	private String clientId;

	/** Auth-manager client secret from test properties. */
	@Value("${mosip.kernel.auth.secret.key}")
	private String secretKey;

	/** IDA application id from test properties. */
	@Value("${mosip.kernel.ida.app.id}")
	private String idaAppId;

	/** IDA client id from test properties. */
	@Value("${mosip.kernel.ida.client.id}")
	private String idaClientId;

	/** IDA client secret from test properties. */
	@Value("${mosip.kernel.ida.secret.key}")
	private String idaSecretKey;
	
	
	/**
	 * Asserts internal token generation returns the mocked secret-key token.
	 *
	 * @throws Exception if generation fails
	 */
	@Test
	public void getInternalTokenGenerationServiceTest() throws Exception  {
		AuthNResponseDto authNResponseDto = new AuthNResponseDto();
		authNResponseDto.setToken("Mock-token");
		when(authService.authenticateWithSecretKey(Mockito.any())).thenReturn(authNResponseDto);
		assertThat(tokenGenerationService.getInternalTokenGenerationService(),is("Mock-token"));
	}

	/**
	 * Asserts UIN-based token generation returns the mocked secret-key token.
	 *
	 * @throws Exception if generation fails
	 */
	@Test
	public void getUINBasedTokenTest() throws Exception  {
		AuthNResponseDto authNResponseDto = new AuthNResponseDto();
		authNResponseDto.setToken("Mock-token");
		when(authService.authenticateWithSecretKey(Mockito.any())).thenReturn(authNResponseDto);
		assertThat(tokenGenerationService.getUINBasedToken(),is("Mock-token"));
	}
}
