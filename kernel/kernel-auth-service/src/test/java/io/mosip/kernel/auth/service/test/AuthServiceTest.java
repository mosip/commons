package io.mosip.kernel.auth.service.test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.isA;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.mosip.kernel.auth.defaultimpl.constant.AuthErrorCode;
import io.mosip.kernel.auth.defaultimpl.util.AuthUtil;
import io.mosip.kernel.core.authmanager.model.*;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.kernel.auth.defaultimpl.config.MosipEnvironment;
import io.mosip.kernel.auth.defaultimpl.constant.AuthConstant;
import io.mosip.kernel.auth.defaultimpl.dto.AccessTokenResponse;
import io.mosip.kernel.auth.defaultimpl.dto.AuthToken;
import io.mosip.kernel.auth.defaultimpl.exception.AuthManagerException;
import io.mosip.kernel.auth.defaultimpl.exception.LoginException;
import io.mosip.kernel.auth.defaultimpl.repository.impl.KeycloakImpl;
import io.mosip.kernel.auth.defaultimpl.service.OTPService;
import io.mosip.kernel.auth.defaultimpl.service.TokenService;
import io.mosip.kernel.auth.defaultimpl.service.UinService;
import io.mosip.kernel.auth.defaultimpl.util.TemplateUtil;
import io.mosip.kernel.auth.defaultimpl.util.TokenGenerator;
import io.mosip.kernel.auth.defaultimpl.util.TokenValidator;
import io.mosip.kernel.auth.test.AuthTestBootApplication;
import io.mosip.kernel.openid.bridge.api.service.AuthService;

/**
 * Tests {@link AuthService} OTP authenticate, role/user listing, login
 * redirect, token validate, and client-secret / username-password login with
 * Keycloak and RestTemplate collaborators mocked.
 * <p>
 * Uses Boot 4 {@link AutoConfigureMockMvc} from
 * {@code org.springframework.boot.webmvc.test.autoconfigure} and
 * {@link MockitoBean} for Keycloak, token, OTP, UIN, and RestTemplate beans.
 */
@SpringBootTest(classes = { AuthTestBootApplication.class })
@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
public class AuthServiceTest {
	/** Keycloak OpenID base URL from test properties. */
	@Value("${mosip.iam.open-id-url}")
	private String keycloakOpenIdUrl;

	/** Keycloak realm operations base URL from test properties. */
	@Value("${mosip.iam.realm.operations.base-url}")
	private String keycloakBaseUrl;

	/** Keycloak IAM repository replaced with a Mockito bean. */
	@MockitoBean
	KeycloakImpl keycloakImpl;

	/** Token generator from the test context. */
	@Autowired
	TokenGenerator tokenGenerator;

	/** Token validator replaced with a Mockito bean. */
	@MockitoBean
	TokenValidator tokenValidator;

	/** Custom token store replaced with a Mockito bean. */
	@MockitoBean
	TokenService customTokenServices;

	/** OTP service replaced with a Mockito bean. */
	@MockitoBean
	OTPService oTPService;

	/** UIN service replaced with a Mockito bean. */
	@MockitoBean
	UinService uinService;

	/** Auth RestTemplate replaced with a Mockito bean. */
	@Qualifier("authRestTemplate")
	@MockitoBean
	RestTemplate authRestTemplate;

	/** Auth util replaced with a Mockito bean. */
	@MockitoBean
	AuthUtil authUtil;

	/** Keycloak RestTemplate replaced with a Mockito bean. */
	@Qualifier(value = "keycloakRestTemplate")
	@MockitoBean
	private RestTemplate keycloakRestTemplate;

	/** Keycloak base URL from test properties. */
	@Value("${mosip.iam.base-url}")
	private String keycloakBaseURL;
	
	/** Keycloak token endpoint from test properties. */
	@Value("${mosip.iam.token_endpoint}")
	private String tokenEndpoint;

	/** Template util replaced with a Mockito bean. */
	@MockitoBean
	private TemplateUtil templateUtil;

	/** MOSIP environment URLs from the test context. */
	@Autowired
	MosipEnvironment mosipEnvironment;

	/** MockMvc for HTTP calls (unused by some service-level tests). */
	@Autowired
	private MockMvc mockMvc;

	/** JSON mapper from the test context. */
	@Autowired
	private ObjectMapper objectMapper;

	/** Auth service under test. */
	@Autowired
	private AuthService authService;

	/**
	 * Asserts authenticateUserWithOtp raises AuthManagerException when UIN
	 * details are missing.
	 *
	 * @throws Exception if the service call fails unexpectedly
	 */
	@Test(expected = AuthManagerException.class)
	public void authenticateUserWithOTPValidationErrorTest() throws Exception {
		when(keycloakImpl.isUserAlreadyPresent(Mockito.any(), Mockito.any())).thenReturn(false);
		when(uinService.getDetailsForValidateOtp(Mockito.any())).thenReturn(null);
		UserOtp userOtp = new UserOtp();
		userOtp.setAppId("ida");
		userOtp.setUserId("112211");
		userOtp.setOtp("112211");
		authService.authenticateUserWithOtp(userOtp);
	}

	@Test
	/**
	 * Asserts authenticateUserWithOtp returns a token when OTP validation succeeds.
	 *
	 * @throws Exception if the service call fails
	 */
	public void authenticateUserWithOTPMosipTokenTest() throws Exception {
		MosipUserDto mosipUserDto = new MosipUserDto();
		mosipUserDto.setUserId("mock-user");
		mosipUserDto.setMail("mock-user@mosip.io");
		mosipUserDto.setMobile("9999999999");
		mosipUserDto.setRole("MOCK-ROLE");
		MosipUserTokenDto mosipToken = new MosipUserTokenDto();
		mosipToken.setMosipUserDto(mosipUserDto);
		mosipToken.setToken("mock-token");
		mosipToken.setRefreshToken("mock-token");
		mosipToken.setExpTime(3600);
		mosipToken.setRefreshExpTime(3600);
		mosipToken.setMessage("success");
		mosipToken.setStatus("success");
		when(keycloakImpl.isUserAlreadyPresent(Mockito.any(), Mockito.any())).thenReturn(false);
		when(uinService.getDetailsForValidateOtp(Mockito.any())).thenReturn(mosipUserDto);
		when(oTPService.validateOTP(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(mosipToken);
		UserOtp userOtp = new UserOtp();
		userOtp.setAppId("ida");
		userOtp.setUserId("112211");
		userOtp.setOtp("112211");
		AuthNResponseDto authNResponseDto = authService.authenticateUserWithOtp(userOtp);
		assertThat(authNResponseDto.getStatus(), is("success"));
	}

	@Test
	/**
	 * Asserts invalid-token handling on AuthService returns success status.
	 *
	 * @throws Exception if the service call fails
	 */
	public void invalidTokenTest() throws Exception {
		// token service ready mocked in mock bean
		AuthNResponse authNResponse = authService.invalidateToken("mock-token");
		assertThat(authNResponse.getStatus(), is("success"));
	}

	@Test
	/**
	 * Asserts getAllRoles returns roles from the mocked Keycloak impl.
	 *
	 * @throws Exception if the service call fails
	 */
	public void getAllRolesTest() throws Exception {
		RolesListDto rolesListDto = new RolesListDto();
		Role role = new Role();
		role.setRoleId("123");
		role.setRoleName("processor");
		List<Role> roles = new ArrayList<>();
		roles.add(role);
		rolesListDto.setRoles(roles);
		when(keycloakImpl.getAllRoles(Mockito.any())).thenReturn(rolesListDto);
		RolesListDto rld = authService.getAllRoles("ida");
		assertThat(rld.getRoles().get(0).getRoleId(), is("123"));
	}

	@Test
	/**
	 * Asserts getListOfUsersDetails returns users from the mocked Keycloak impl.
	 *
	 * @throws Exception if the service call fails
	 */
	public void getListOfUsersDetailsTest() throws Exception {
		MosipUserListDto mosipUserListDto = new MosipUserListDto();
		MosipUserDto mosipUserDto = new MosipUserDto();
		mosipUserDto.setUserId("mock-user");
		mosipUserDto.setMail("mock-user@mosip.io");
		mosipUserDto.setMobile("9999999999");
		mosipUserDto.setRole("MOCK-ROLE");
		List<MosipUserDto> list = new ArrayList<>();
		list.add(mosipUserDto);
		mosipUserListDto.setMosipUserDtoList(list);
		when(keycloakImpl.getListOfUsersDetails(Mockito.any(),Mockito.any())).thenReturn(mosipUserListDto);
		List<String> userd = new ArrayList<>();
		userd.add("userdetails1");
		MosipUserListDto rld= authService.getListOfUsersDetails(userd,"ida");
		assertThat(rld.getMosipUserDtoList().get(0).getUserId(),is("mock-user"));
	}
	
	@Test
	/**
	 * Asserts getAllUserDetailsWithSalt returns salted user details from Keycloak.
	 *
	 * @throws Exception if the service call fails
	 */
	public void getAllUserDetailsWithSaltTest() throws Exception {
		MosipUserSaltListDto mosipUserListDto = new MosipUserSaltListDto();
		MosipUserSalt mosipUserDto = new MosipUserSalt();
		mosipUserDto.setUserId("mock-user");
		List<MosipUserSalt> list = new ArrayList<>();
		list.add(mosipUserDto);
		mosipUserListDto.setMosipUserSaltList(list);
		when(keycloakImpl.getAllUserDetailsWithSalt(Mockito.any(),Mockito.any())).thenReturn(mosipUserListDto);
		List<String> userd = new ArrayList<>();
		userd.add("userdetails1");
		MosipUserSaltListDto rld= authService.getAllUserDetailsWithSalt(userd,"ida");
		assertThat(rld.getMosipUserSaltList().get(0).getUserId(),is("mock-user"));
	}
	
	
	/*
	 * @Test public void getRidBasedOnUidTest() throws Exception { PasswordDto
	 * passwordDto = new PasswordDto(); passwordDto.setUserId("123");
	 * passwordDto.setNewPassword("newpass"); passwordDto.setOldPassword("oldpass");
	 * RIdDto rIdDto = new RIdDto(); rIdDto.setRId("mock-rid");
	 * when(userStoreFactory.getDataStoreBasedOnApp(Mockito.any())).thenReturn(
	 * keycloakImpl);
	 * when(keycloakImpl.getRidFromUserId(Mockito.any(),Mockito.any())).thenReturn(
	 * rIdDto); RIdDto rld= authService.getRidBasedOnUid("mock-user","ida");
	 * assertThat(rld.getRId(),is(rIdDto.getRId())); }
	 * 
	 * 
	 * @Test public void unBlockUserTest() throws Exception { AuthZResponseDto
	 * authZResponseDto = new AuthZResponseDto();
	 * authZResponseDto.setMessage("success");
	 * authZResponseDto.setStatus("success");
	 * when(userStoreFactory.getDataStoreBasedOnApp(Mockito.any())).thenReturn(
	 * keycloakImpl);
	 * when(keycloakImpl.unBlockAccount(Mockito.any())).thenReturn(authZResponseDto)
	 * ; AuthZResponseDto rld= authService.unBlockUser("mock-userid","ida");
	 * assertThat(rld.getStatus(),is("success")); }
	 * 
	 * 
	 * @Test public void changePasswordTest() throws Exception { PasswordDto
	 * passwordDto = new PasswordDto(); passwordDto.setUserId("123");
	 * passwordDto.setNewPassword("newpass"); passwordDto.setOldPassword("oldpass");
	 * AuthZResponseDto authZResponseDto = new AuthZResponseDto();
	 * authZResponseDto.setMessage("success");
	 * authZResponseDto.setStatus("success");
	 * when(userStoreFactory.getDataStoreBasedOnApp(Mockito.any())).thenReturn(
	 * keycloakImpl);
	 * when(keycloakImpl.changePassword(Mockito.any())).thenReturn(authZResponseDto)
	 * ; AuthZResponseDto rld= authService.changePassword("ida",passwordDto);
	 * assertThat(rld.getStatus(),is("success")); }
	 * 
	 * @Test public void resetPasswordTest() throws Exception { PasswordDto
	 * passwordDto = new PasswordDto(); passwordDto.setUserId("123");
	 * passwordDto.setNewPassword("newpass"); passwordDto.setOldPassword("oldpass");
	 * AuthZResponseDto authZResponseDto = new AuthZResponseDto();
	 * authZResponseDto.setMessage("success");
	 * authZResponseDto.setStatus("success");
	 * when(userStoreFactory.getDataStoreBasedOnApp(Mockito.any())).thenReturn(
	 * keycloakImpl);
	 * when(keycloakImpl.resetPassword(Mockito.any())).thenReturn(authZResponseDto);
	 * AuthZResponseDto rld= authService.resetPassword("ida",passwordDto);
	 * assertThat(rld.getStatus(),is("success")); }
	 * 
	 * @Test public void getUserNameBasedOnMobileNumberTest() throws Exception {
	 * UserNameDto userNameDto = new UserNameDto();
	 * userNameDto.setUserName("mock-user");
	 * when(userStoreFactory.getDataStoreBasedOnApp(Mockito.any())).thenReturn(
	 * keycloakImpl);
	 * when(keycloakImpl.getUserNameBasedOnMobileNumber(Mockito.any())).thenReturn(
	 * userNameDto); UserNameDto rld=
	 * authService.getUserNameBasedOnMobileNumber("registrationclient","9819283912")
	 * ; assertThat(rld.getUserName(),is("mock-user")); }
	 * 
	 * @Test public void addUserPasswordTest() throws Exception {
	 * UserPasswordRequestDto userNameDto = new UserPasswordRequestDto();
	 * userNameDto.setUserName("mock-user");
	 * 
	 * UserPasswordResponseDto userPasswordResponseDto = new
	 * UserPasswordResponseDto(); userPasswordResponseDto.setUserName("mock-user");
	 * when(userStoreFactory.getDataStoreBasedOnApp(Mockito.any())).thenReturn(
	 * keycloakImpl); when(keycloakImpl.addPassword(Mockito.any())).thenReturn(
	 * userPasswordResponseDto); UserPasswordResponseDto rld=
	 * authService.addUserPassword(userNameDto);
	 * assertThat(rld.getUserName(),is(userPasswordResponseDto.getUserName())); }
	 * 
	 * @Test public void getUserRoleTest() throws Exception { MosipUserDto
	 * mosipUserDto = new MosipUserDto(); mosipUserDto.setUserId("mock-user");
	 * mosipUserDto.setMail("mock-user@mosip.io");
	 * mosipUserDto.setMobile("9999999999"); mosipUserDto.setRole("MOCK-ROLE");
	 * when(userStoreFactory.getDataStoreBasedOnApp(Mockito.any())).thenReturn(
	 * keycloakImpl);
	 * when(keycloakImpl.getUserRoleByUserId(Mockito.any())).thenReturn(mosipUserDto
	 * ); UserRoleDto rld= authService.getUserRole("ida","9819283912");
	 * assertThat(rld.getUserId(),is(mosipUserDto.getUserId())); }
	 * 
	 * @Test public void getUserDetailBasedonMobileNumberTest() throws Exception {
	 * MosipUserDto mosipUserDto = new MosipUserDto();
	 * mosipUserDto.setUserId("mock-user");
	 * mosipUserDto.setMail("mock-user@mosip.io");
	 * mosipUserDto.setMobile("9999999999"); mosipUserDto.setRole("MOCK-ROLE");
	 * when(userStoreFactory.getDataStoreBasedOnApp(Mockito.any())).thenReturn(
	 * keycloakImpl);
	 * when(keycloakImpl.getUserDetailBasedonMobileNumber(Mockito.any())).thenReturn
	 * (mosipUserDto); MosipUserDto rld=
	 * authService.getUserDetailBasedonMobileNumber("ida","9819283912");
	 * assertThat(rld.getUserId(),is(mosipUserDto.getUserId())); }
	 * 
	 * @Test public void validateUserNameTest() throws Exception {
	 * ValidationResponseDto validationResponseDto = new ValidationResponseDto();
	 * validationResponseDto.setStatus("success");
	 * when(userStoreFactory.getDataStoreBasedOnApp(Mockito.any())).thenReturn(
	 * keycloakImpl); when(keycloakImpl.validateUserName(Mockito.any())).thenReturn(
	 * validationResponseDto); ValidationResponseDto rld=
	 * authService.validateUserName("ida","9819283912");
	 * assertThat(rld.getStatus(),is(validationResponseDto.getStatus())); }
	 * 
	 * @Test public void getUserDetailBasedOnUserIdTest() throws Exception {
	 * UserDetailsResponseDto resp = new UserDetailsResponseDto(); UserDetailsDto
	 * userDetailsDto = new UserDetailsDto(); userDetailsDto.setUserId("mock-user");
	 * List<UserDetailsDto> userDetailsDtos = new ArrayList<UserDetailsDto>();
	 * userDetailsDtos.add(userDetailsDto); resp.setUserDetails(userDetailsDtos);
	 * 
	 * List<String> userids= new ArrayList<String>(); userids.add("mock-user");
	 * when(userStoreFactory.getDataStoreBasedOnApp(Mockito.any())).thenReturn(
	 * keycloakImpl);
	 * when(keycloakImpl.getUserDetailBasedOnUid(Mockito.any())).thenReturn(resp);
	 * UserDetailsResponseDto rld=
	 * authService.getUserDetailBasedOnUserId("ida",userids);
	 * assertThat(rld.getUserDetails().get(0).getUserId(),is(resp.getUserDetails().
	 * get(0).getUserId())); }
	 */
	
	@Test
	/**
	 * Asserts Keycloak URI construction from realm and path.
	 *
	 * @throws Exception if URI building fails
	 */
	public void getKeycloakURITest() throws Exception {
		String uri=authService.getKeycloakURI("mock-redirect-uri","mock-state");
		assertThat(uri,isA(String.class));
	}
	
	/*
	 * @Test public void getIndividualIdBasedOnUserIDTest() throws Exception {
	 * IndividualIdDto resp = new IndividualIdDto(); resp.setIndividualId("12331");
	 * String userid= "mock-user";
	 * when(userStoreFactory.getDataStoreBasedOnApp(Mockito.any())).thenReturn(
	 * keycloakImpl);
	 * when(keycloakImpl.getIndividualIdFromUserId(Mockito.any(),Mockito.any())).
	 * thenReturn(resp); IndividualIdDto rld=
	 * authService.getIndividualIdBasedOnUserID(userid,"ida");
	 * assertThat(rld.getIndividualId(),is(resp.getIndividualId())); }
	 * 
	 * @Test public void getListOfUsersDetailsSearchTest() throws Exception {
	 * MosipUserListDto mosipUserListDto = new MosipUserListDto(); MosipUserDto
	 * mosipUserDto = new MosipUserDto(); mosipUserDto.setUserId("mock-user");
	 * mosipUserDto.setMail("mock-user@mosip.io");
	 * mosipUserDto.setMobile("9999999999"); mosipUserDto.setRole("MOCK-ROLE");
	 * List<MosipUserDto> list = new ArrayList<>(); list.add(mosipUserDto);
	 * mosipUserListDto.setMosipUserDtoList(list);
	 * when(userStoreFactory.getDataStoreBasedOnApp(Mockito.any())).thenReturn(
	 * keycloakImpl); when(keycloakImpl.getListOfUsersDetails(Mockito.any(),
	 * Mockito.any(), Mockito.eq(0), Mockito.eq(10), Mockito.any(), Mockito.any(),
	 * Mockito.any(), Mockito.any(),Mockito.any())).thenReturn(mosipUserListDto);
	 * MosipUserListDto rld= authService.getListOfUsersDetails("ida",
	 * "mock-roleName", 0, 10, "mock-email", "mock-firstName", "mock-lastName",
	 * "mock-username","userID");
	 * assertThat(rld.getMosipUserDtoList().get(0).getUserId(),is(mosipUserListDto.
	 * getMosipUserDtoList().get(0).getUserId())); }
	 */
	
	@Test
	/**
	 * Asserts loginRedirect succeeds for a valid IAM token response.
	 *
	 * @throws Exception if the service call fails
	 */
	public void loginRedirectTest() throws Exception {
		AccessTokenResponse accessTokenResponse = new AccessTokenResponse();
		accessTokenResponse.setAccess_token("mock-access-token");
		accessTokenResponse.setExpires_in("111");
		
		Map<String, String> pathParam = new HashMap<>();
		pathParam.put("realmId", "mosip");
		UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(tokenEndpoint);
		when(authRestTemplate.exchange(Mockito.eq(uriBuilder.buildAndExpand(pathParam).toUriString()),Mockito.eq(HttpMethod.POST),
				Mockito.any(), Mockito.eq(String.class)))
						.thenReturn(ResponseEntity.ok(objectMapper.writeValueAsString(accessTokenResponse)));
		AccessTokenResponseDTO rld=authService.loginRedirect("mock-state","mock-sessionState", "mock-code", "mock-state",
				"mock-redirectURI");
		assertThat(rld.getAccessToken(),is(accessTokenResponse.getAccess_token()));
	}
	
	
	@Test(expected = LoginException.class)
	/**
	 * Asserts loginRedirect maps an IAM client error to LoginException.
	 *
	 * @throws Exception if the service call fails unexpectedly
	 */
	public void loginRedirectClientErrorTest() throws Exception {
		AccessTokenResponse accessTokenResponse = new AccessTokenResponse();
		accessTokenResponse.setAccess_token("mock-access-token");
		accessTokenResponse.setExpires_in("111");
		String resp = "{\r\n" + "  \"error\": \"UNAUTHORIZED\",\r\n" + "  \"error_description\": \"UNAUTHORIZED\" }";
		Map<String, String> pathParam = new HashMap<>();
		pathParam.put("realmId", "mosip");
		UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(tokenEndpoint);
		when(authRestTemplate.exchange(Mockito.eq(uriBuilder.buildAndExpand(pathParam).toUriString()),Mockito.eq(HttpMethod.POST),
				Mockito.any(), Mockito.eq(String.class))).
		thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST, "400", resp.getBytes(),
				Charset.defaultCharset()));
		authService.loginRedirect("mock-state","mock-sessionState", "mock-code", "mock-state",
				"mock-redirectURI");
	}
	
	
	
	@Test
	/**
	 * Asserts validateToken returns the user when the validator accepts the token.
	 *
	 * @throws Exception if the service call fails
	 */
	public void validateTokenTest() throws Exception {
		MosipUserTokenDto mosipUserTokenDto = new MosipUserTokenDto();
		mosipUserTokenDto.setToken("eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJzNmYxcDYwYWVDTTBrNy1NaW9sN0Zib2FTdXlRYm95UC03S1RUTmVWLWZNIn0.eyJqdGkiOiJmYTU4Y2NjMC00ZDRiLTQ2ZjAtYjgwOC0yMWI4ZTdhNmMxNDMiLCJleHAiOjE2NDAxODc3MTksIm5iZiI6MCwiaWF0IjoxNjQwMTUxNzE5LCJpc3MiOiJodHRwczovL2Rldi5tb3NpcC5uZXQva2V5Y2xvYWsvYXV0aC9yZWFsbXMvbW9zaXAiLCJhdWQiOiJhY2NvdW50Iiwic3ViIjoiOWRiZTE0MDEtNTQ1NC00OTlhLTlhMWItNzVhZTY4M2Q0MjZhIiwidHlwIjoiQmVhcmVyIiwiYXpwIjoibW9zaXAtcmVzaWRlbnQtY2xpZW50IiwiYXV0aF90aW1lIjowLCJzZXNzaW9uX3N0YXRlIjoiY2QwYjU5NjEtOTYzMi00NmE0LWIzMzgtODc4MWEzNDVmMTZiIiwiYWNyIjoiMSIsImFsbG93ZWQtb3JpZ2lucyI6WyJodHRwczovL2Rldi5tb3NpcC5uZXQiXSwicmVhbG1fYWNjZXNzIjp7InJvbGVzIjpbIkNSRURFTlRJQUxfUkVRVUVTVCIsIlJFU0lERU5UIiwib2ZmbGluZV9hY2Nlc3MiLCJQQVJUTkVSX0FETUlOIiwidW1hX2F1dGhvcml6YXRpb24iXX0sInJlc291cmNlX2FjY2VzcyI6eyJtb3NpcC1yZXNpZGVudC1jbGllbnQiOnsicm9sZXMiOlsidW1hX3Byb3RlY3Rpb24iXX0sImFjY291bnQiOnsicm9sZXMiOlsibWFuYWdlLWFjY291bnQiLCJtYW5hZ2UtYWNjb3VudC1saW5rcyIsInZpZXctcHJvZmlsZSJdfX0sInNjb3BlIjoicHJvZmlsZSBlbWFpbCIsImNsaWVudEhvc3QiOiIxMC4yNDQuNS4xNDgiLCJlbWFpbF92ZXJpZmllZCI6ZmFsc2UsImNsaWVudElkIjoibW9zaXAtcmVzaWRlbnQtY2xpZW50IiwicHJlZmVycmVkX3VzZXJuYW1lIjoic2VydmljZS1hY2NvdW50LW1vc2lwLXJlc2lkZW50LWNsaWVudCIsImNsaWVudEFkZHJlc3MiOiIxMC4yNDQuNS4xNDgifQ.xZq1m3mBTEvFDENKFOI59QsSl3sd_TSDNbhTAOq4x_x_4voPc4hh08gIxUdsVHfXY4T0P8DdZ1xNt8xd1VWc33Hc4b_3kK7ksGY4wwqtb0-pDLQGajCGuG6vebC1rYcjsGRbJ1Gnrj_F2RNY4Ky6Nq5SAJ1Lh_NVKNKFghAXb3YrlmqlmCB1fCltC4XBqNnF5_k4uzLCu_Wr0lt_M87X97DktaRGLOD2_HY1Ire9YPsWkoO8y7X_DRCY59yQDVgYs2nAiR6Am-c55Q0fEQ0HuB4IJHlhtMHm27dXPdOEhFhR8ZPOyeO6ZIcIm0ZTDjusrruqWy2_yO5fe3XIHkCOAw");
		mosipUserTokenDto.setExpTime(3000);
		MosipUserDto mosipUserDto = new MosipUserDto();
		mosipUserDto.setUserId("mock-user");
		mosipUserDto.setMail("mock-user@mosip.io");
		mosipUserDto.setMobile("9999999999");
		mosipUserDto.setRole("MOCK-ROLE");
		mosipUserTokenDto.setMosipUserDto(mosipUserDto);	
		AuthToken authToken = new AuthToken("mock-user", "mock-token", 3000, null);
		when(tokenValidator.validateToken(Mockito.anyString())).thenReturn(mosipUserTokenDto);
		when(customTokenServices.getTokenDetails(Mockito.anyString())).thenReturn(authToken);
		MosipUserTokenDto dto=authService.validateToken("mock-token");
		assertThat(dto.getMosipUserDto().getUserId(),is(mosipUserDto.getUserId()));
	}
	
	@Test(expected = AuthManagerException.class)
	/**
	 * Asserts validateToken raises AuthManagerException on validator failure.
	 *
	 * @throws Exception if the service call fails unexpectedly
	 */
	public void validateTokenAuthManagerExceptionTest() throws Exception {
		MosipUserTokenDto mosipUserTokenDto = new MosipUserTokenDto();
		mosipUserTokenDto.setToken("eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJzNmYxcDYwYWVDTTBrNy1NaW9sN0Zib2FTdXlRYm95UC03S1RUTmVWLWZNIn0.eyJqdGkiOiJmYTU4Y2NjMC00ZDRiLTQ2ZjAtYjgwOC0yMWI4ZTdhNmMxNDMiLCJleHAiOjE2NDAxODc3MTksIm5iZiI6MCwiaWF0IjoxNjQwMTUxNzE5LCJpc3MiOiJodHRwczovL2Rldi5tb3NpcC5uZXQva2V5Y2xvYWsvYXV0aC9yZWFsbXMvbW9zaXAiLCJhdWQiOiJhY2NvdW50Iiwic3ViIjoiOWRiZTE0MDEtNTQ1NC00OTlhLTlhMWItNzVhZTY4M2Q0MjZhIiwidHlwIjoiQmVhcmVyIiwiYXpwIjoibW9zaXAtcmVzaWRlbnQtY2xpZW50IiwiYXV0aF90aW1lIjowLCJzZXNzaW9uX3N0YXRlIjoiY2QwYjU5NjEtOTYzMi00NmE0LWIzMzgtODc4MWEzNDVmMTZiIiwiYWNyIjoiMSIsImFsbG93ZWQtb3JpZ2lucyI6WyJodHRwczovL2Rldi5tb3NpcC5uZXQiXSwicmVhbG1fYWNjZXNzIjp7InJvbGVzIjpbIkNSRURFTlRJQUxfUkVRVUVTVCIsIlJFU0lERU5UIiwib2ZmbGluZV9hY2Nlc3MiLCJQQVJUTkVSX0FETUlOIiwidW1hX2F1dGhvcml6YXRpb24iXX0sInJlc291cmNlX2FjY2VzcyI6eyJtb3NpcC1yZXNpZGVudC1jbGllbnQiOnsicm9sZXMiOlsidW1hX3Byb3RlY3Rpb24iXX0sImFjY291bnQiOnsicm9sZXMiOlsibWFuYWdlLWFjY291bnQiLCJtYW5hZ2UtYWNjb3VudC1saW5rcyIsInZpZXctcHJvZmlsZSJdfX0sInNjb3BlIjoicHJvZmlsZSBlbWFpbCIsImNsaWVudEhvc3QiOiIxMC4yNDQuNS4xNDgiLCJlbWFpbF92ZXJpZmllZCI6ZmFsc2UsImNsaWVudElkIjoibW9zaXAtcmVzaWRlbnQtY2xpZW50IiwicHJlZmVycmVkX3VzZXJuYW1lIjoic2VydmljZS1hY2NvdW50LW1vc2lwLXJlc2lkZW50LWNsaWVudCIsImNsaWVudEFkZHJlc3MiOiIxMC4yNDQuNS4xNDgifQ.xZq1m3mBTEvFDENKFOI59QsSl3sd_TSDNbhTAOq4x_x_4voPc4hh08gIxUdsVHfXY4T0P8DdZ1xNt8xd1VWc33Hc4b_3kK7ksGY4wwqtb0-pDLQGajCGuG6vebC1rYcjsGRbJ1Gnrj_F2RNY4Ky6Nq5SAJ1Lh_NVKNKFghAXb3YrlmqlmCB1fCltC4XBqNnF5_k4uzLCu_Wr0lt_M87X97DktaRGLOD2_HY1Ire9YPsWkoO8y7X_DRCY59yQDVgYs2nAiR6Am-c55Q0fEQ0HuB4IJHlhtMHm27dXPdOEhFhR8ZPOyeO6ZIcIm0ZTDjusrruqWy2_yO5fe3XIHkCOAw");
		mosipUserTokenDto.setExpTime(3000);
		MosipUserDto mosipUserDto = new MosipUserDto();
		mosipUserDto.setUserId("mock-user");
		mosipUserDto.setMail("mock-user@mosip.io");
		mosipUserDto.setMobile("9999999999");
		mosipUserDto.setRole("MOCK-ROLE");
		mosipUserTokenDto.setMosipUserDto(mosipUserDto);	
		AuthToken authToken = new AuthToken("mock-user", "mock-token", 3000, null);
		when(tokenValidator.validateToken(Mockito.anyString())).thenReturn(mosipUserTokenDto);
		when(customTokenServices.getTokenDetails(Mockito.anyString())).thenReturn(null);
		MosipUserTokenDto dto=authService.validateToken("mock-token");
		assertThat(dto.getMosipUserDto().getUserId(),is(mosipUserDto.getUserId()));
	}
	
	
	@Test
	/**
	 * Asserts client-secret authenticate returns a token on HTTP 200.
	 *
	 * @throws Exception if the service call fails
	 */
	public void authenticateUserwithClientIDTest() throws Exception {
		AccessTokenResponse accessTokenResponse = new AccessTokenResponse();
		accessTokenResponse.setAccess_token("mock-access-token");
		accessTokenResponse.setExpires_in("111");
		accessTokenResponse.setRefresh_token("mock-ref-token");
		accessTokenResponse.setRefresh_expires_in("111");
		Map<String, String> pathParams = new HashMap<>();
		pathParams.put(AuthConstant.REALM_ID, "mosip");
		UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromUriString(keycloakOpenIdUrl + "/token");
		when(authUtil.getRealmIdFromAppId(Mockito.anyString())).thenReturn("mosip");
		when(authRestTemplate.postForEntity(Mockito.eq(uriComponentsBuilder.buildAndExpand(pathParams).toUriString()),
				Mockito.any(), Mockito.eq(AccessTokenResponse.class))).thenReturn(ResponseEntity.ok(accessTokenResponse));
		LoginUserWithClientId loginUserWithClientId = new LoginUserWithClientId();
		loginUserWithClientId.setAppId("ida");
		loginUserWithClientId.setClientId("ida-client");
		loginUserWithClientId.setClientSecret("client-secret");
		loginUserWithClientId.setUserName("mock-user");
		loginUserWithClientId.setPassword("mock-pass");
		AuthNResponseDto authNResponseDto= authService.authenticateUser(loginUserWithClientId);
		assertThat(authNResponseDto.getStatus(),is(AuthConstant.SUCCESS_STATUS));
	}
	
	@Test(expected = AuthManagerException.class)
	/**
	 * Asserts client-secret authenticate raises AuthManagerException on HTTP 401.
	 *
	 * @throws Exception if the service call fails unexpectedly
	 */
	public void authenticateUserwithClientIDUnAuthTest() throws Exception {
		AccessTokenResponse accessTokenResponse = new AccessTokenResponse();
		accessTokenResponse.setAccess_token("mock-access-token");
		accessTokenResponse.setExpires_in("111");
		accessTokenResponse.setRefresh_token("mock-ref-token");
		accessTokenResponse.setRefresh_expires_in("111");
		Map<String, String> pathParams = new HashMap<>();
		pathParams.put(AuthConstant.REALM_ID, "mosip");
		UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromUriString(keycloakOpenIdUrl + "/token");
		when(authUtil.getRealmIdFromAppId(Mockito.anyString())).thenReturn("mosip");
		when(authRestTemplate.postForEntity(Mockito.eq(uriComponentsBuilder.buildAndExpand(pathParams).toUriString()),
				Mockito.any(), Mockito.eq(AccessTokenResponse.class))).thenThrow(new HttpClientErrorException(HttpStatus.UNAUTHORIZED, "401", "not auth".getBytes(),
						Charset.defaultCharset()));
		LoginUserWithClientId loginUserWithClientId = new LoginUserWithClientId();
		loginUserWithClientId.setAppId("ida");
		loginUserWithClientId.setClientId("ida-client");
		loginUserWithClientId.setClientSecret("client-secret");
		loginUserWithClientId.setUserName("mock-user");
		loginUserWithClientId.setPassword("mock-pass");
		AuthNResponseDto authNResponseDto= authService.authenticateUser(loginUserWithClientId);
		assertThat(authNResponseDto.getStatus(),is(AuthConstant.SUCCESS_STATUS));
	}



	@Test
	/**
	 * Asserts username/password authenticate succeeds for valid credentials.
	 *
	 * @throws Exception if the service call fails
	 */
	public void authenticateUser_withValidDetails_thenPass() throws Exception {

		AccessTokenResponse accessTokenResponse=new AccessTokenResponse();
		accessTokenResponse.setAccess_token("accessToken");
		accessTokenResponse.setRefresh_token("refresh_token");
		accessTokenResponse.setScope("openId");
		accessTokenResponse.setExpires_in("111");
		accessTokenResponse.setRefresh_expires_in("111");
		when(authUtil.getRealmIdFromAppId(Mockito.anyString())).thenReturn("mosip");
		when(authRestTemplate.postForEntity(Mockito.anyString(), Mockito.any(HttpEntity.class), Mockito.eq(AccessTokenResponse.class)))
				.thenReturn(new ResponseEntity<>(accessTokenResponse, HttpStatus.OK));

		LoginUser loginUser = new LoginUser(); // Populate loginUser fields
		AuthNResponseDto result = authService.authenticateUser(loginUser);


		Assert.assertNotNull(result);
		Assert.assertEquals("accessToken", result.getToken()); // Adjust according to expected values

	}

	@Test
	/**
	 * Asserts username/password authenticate fails on HTTP 400.
	 *
	 * @throws Exception if the service call fails
	 */
	public void authenticateUser_withErrorResponse400_thenFail() throws Exception {

		AccessTokenResponse accessTokenResponse=new AccessTokenResponse();
		accessTokenResponse.setAccess_token("accessToken");
		accessTokenResponse.setRefresh_token("refresh_token");
		accessTokenResponse.setScope("openId");
		accessTokenResponse.setExpires_in("111");
		accessTokenResponse.setRefresh_expires_in("111");
		when(authUtil.getRealmIdFromAppId(Mockito.anyString())).thenReturn("mosip");
		when(authRestTemplate.postForEntity(Mockito.anyString(), Mockito.any(HttpEntity.class), Mockito.eq(AccessTokenResponse.class)))
				.thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST, "400"));

		LoginUser loginUser = new LoginUser(); // Populate loginUser fields
		try{
			authService.authenticateUser(loginUser);
		}catch (Exception e){
			Assert.assertEquals(e.getMessage(), AuthErrorCode.REQUEST_VALIDATION_ERROR.getErrorMessage());
		}

	}

	@Test
	/**
	 * Asserts username/password authenticate fails on HTTP 401.
	 *
	 * @throws Exception if the service call fails
	 */
	public void authenticateUser_withErrorResponse401_thenFail() throws Exception {

		AccessTokenResponse accessTokenResponse=new AccessTokenResponse();
		accessTokenResponse.setAccess_token("accessToken");
		accessTokenResponse.setRefresh_token("refresh_token");
		accessTokenResponse.setScope("openId");
		accessTokenResponse.setExpires_in("111");
		accessTokenResponse.setRefresh_expires_in("111");
		when(authUtil.getRealmIdFromAppId(Mockito.anyString())).thenReturn("mosip");
		when(authRestTemplate.postForEntity(Mockito.anyString(), Mockito.any(HttpEntity.class), Mockito.eq(AccessTokenResponse.class)))
				.thenThrow(new HttpClientErrorException(HttpStatus.UNAUTHORIZED, "401"));

		LoginUser loginUser = new LoginUser(); // Populate loginUser fields
		try{
			authService.authenticateUser(loginUser);
		}catch (Exception e){
			Assert.assertEquals(e.getMessage(), AuthErrorCode.INVALID_CREDENTIALS.getErrorMessage());
		}

	}


}
