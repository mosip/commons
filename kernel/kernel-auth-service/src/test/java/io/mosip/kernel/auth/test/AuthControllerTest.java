package io.mosip.kernel.auth.test;

import static org.mockito.Mockito.when;

import io.mosip.kernel.auth.controller.AuthController;
import io.mosip.kernel.core.authmanager.model.AuthNResponseDto;
import io.mosip.kernel.core.authmanager.model.LoginUser;
import io.mosip.kernel.openid.bridge.api.service.AuthService;

/**
 * Inactive skeleton for {@link AuthController} login, OTP, client-secret, and
 * token endpoints. Spring and JUnit annotations are commented out so these
 * methods do not currently run.
 *
 * //@author Ramadurai Pandian
 *
 */
//@RunWith(SpringRunner.class)
//@SpringBootTest(classes=AuthApp.class)
public class AuthControllerTest {

	/** Auth service collaborator (mock annotation commented out). */
	// @Mock
	private AuthService authService;

	/** Controller under test (inject-mocks annotation commented out). */
	// @InjectMocks
	AuthController controller;

	/** Login payload used by {@link #setUp()}. */
	private LoginUser loginUser;

	/**
	 * Builds a preregistration {@link LoginUser} for username/password tests.
	 *
	 * @throws Exception if setup fails
	 */
	// @Before
	public void setUp() throws Exception {
		loginUser = new LoginUser();
		loginUser.setUserName("individual");
		loginUser.setPassword("individual");
		loginUser.setAppId("preregistration");

	}

	/**
	 * Would assert userid/password authenticate returns a token from
	 * {@link AuthService}. Currently not executed ({@code @Test} commented out).
	 *
	 * @throws Exception if authenticate fails
	 */
	// @Test
	public void testAuthenticateUseridPwd() throws Exception {
		AuthNResponseDto authNResponseDto = new AuthNResponseDto();
		when(authService.authenticateUser(loginUser)).thenReturn(authNResponseDto);
		String token = authNResponseDto.getToken();
	}

	/**
	 * Placeholder for send-OTP coverage. Currently not executed.
	 */
	// @Test
	public void testSendOTP() {

	}

	/**
	 * Placeholder for userid+OTP authenticate coverage. Currently not executed.
	 */
	// @Test
	public void testUserIdOTP() {

	}

	/**
	 * Placeholder for client-id/secret authenticate coverage. Currently not
	 * executed.
	 */
	// @Test
	public void testClientIdSecretKey() {

	}

	/**
	 * Placeholder for validate-token coverage. Currently not executed.
	 */
	// @Test
	public void testValidateToken() {

	}

	/**
	 * Placeholder for retry-token coverage. Currently not executed.
	 */
	// @Test
	public void testRetryToken() {

	}

	/**
	 * Placeholder for invalidate-token coverage. Currently not executed.
	 */
	// @Test
	public void testInvalidateToken() {

	}

}
