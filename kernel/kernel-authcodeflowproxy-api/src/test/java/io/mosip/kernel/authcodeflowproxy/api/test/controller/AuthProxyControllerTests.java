package io.mosip.kernel.authcodeflowproxy.api.test.controller;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.isA;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.net.URI;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import jakarta.servlet.http.Cookie;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator.Builder;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.kernel.authcodeflowproxy.api.test.AuthProxyFlowTestBootApplication;
import io.mosip.kernel.authcodeflowproxy.api.validator.ValidateTokenUtil;
import io.mosip.kernel.core.exception.ServiceError;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.util.CryptoUtil;
import io.mosip.kernel.core.util.DateUtils2;
import io.mosip.kernel.openid.bridge.api.constants.AuthConstant;
import io.mosip.kernel.openid.bridge.api.constants.AuthErrorCode;
import io.mosip.kernel.openid.bridge.api.constants.Errors;
import io.mosip.kernel.authcodeflowproxy.api.service.LoginServiceV2;
import io.mosip.kernel.openid.bridge.dto.AccessTokenResponse;
import io.mosip.kernel.openid.bridge.dto.IAMErrorResponseDto;
import io.mosip.kernel.openid.bridge.dto.JWTSignatureResponseDto;
import io.mosip.kernel.openid.bridge.model.MosipUserDto;

/**
 * MockMvc tests for the auth-code-flow proxy: validate-token, login,
 * login-redirect (issuer/audience/azp/Ant hashed URIs/private-key JWT), and
 * logout. Spies {@link ValidateTokenUtil} and {@link LoginServiceV2} with Boot
 * 4 {@link MockitoSpyBean}. JWTs are HMAC-signed; signature verification is
 * stubbed because PowerMock cannot run on Mockito 5.
 */
@SpringBootTest(classes = { AuthProxyFlowTestBootApplication.class })
@AutoConfigureMockMvc
@RunWith(SpringRunner.class)
@ActiveProfiles("test")
public class AuthProxyControllerTests {

	/** HMAC used only to mint compact JWTs for MockMvc bodies. */
	private static final Algorithm TEST_HMAC = Algorithm.HMAC256("unit-test-secret-unit-test-secret");


	/** HTTP 401 used in validate-token error assertions. */
	private static final int UNAUTHORIZED_STATUS = 401;

	/** IAM admin validate-token URL from test properties. */
	@Value("${auth.server.admin.validate.url}")
	private String validateUrl;

	/** Query parameter key for the post-logout redirect URI. */
	@Value("${mosip.iam.post-logout-uri-param-key}")
	private String postLogoutRedirectURIParamKey;

	/** RestTemplate bound to {@link MockRestServiceServer}. */
	@Autowired
	private RestTemplate restTemplate;

	/** Mock HTTP server used to stub IAM responses. */
	private MockRestServiceServer mockServer;
	
	/** Token validator spy used to toggle issuer/audience checks. */
	@MockitoSpyBean
	private ValidateTokenUtil validateTokenHelper;

	/** Login service spy used to toggle private-key JWT client auth. */
	@MockitoSpyBean
	private LoginServiceV2 loginService;

	/** Self-token RestTemplate replaced with a Mockito bean. */
	@MockitoBean
	@Qualifier("selfTokenRestTemplate")
	private RestTemplate selfTokenRestTemplate;
	
	/**
	 * Binds {@link MockRestServiceServer}, stubs JWKS/signature on the validator spy,
	 * and disables issuer / audience validation and private-key JWT auth.
	 */
	@Before
	public void init() {
		mockServer = MockRestServiceServer.createServer(restTemplate);
		doReturn(null).when(validateTokenHelper).getPublicKey(any(DecodedJWT.class));
		doReturn(ImmutablePair.of(Boolean.TRUE, null)).when(validateTokenHelper).verifyJWTSignagure(any(DecodedJWT.class));
		ReflectionTestUtils.setField(validateTokenHelper, "validateIssuerDomain", false);
		ReflectionTestUtils.setField(validateTokenHelper, "validateAudClaim", false);
		ReflectionTestUtils.setField(loginService, "isJwtAuthEnabled", false);
	}

	/** MockMvc for HTTP calls against the proxy controller. */
	@Autowired
	private MockMvc mockMvc;

	/** JSON mapper used to serialize IAM response wrappers. */
	@Autowired
	private ObjectMapper objectMapper;

	/**
	 * Asserts GET validate-token returns the user when IAM validate URL returns
	 * 200.
	 *
	 * @throws Exception if the HTTP call fails
	 */
	@Test
	public void validateTokenTest() throws Exception {
		ResponseWrapper<MosipUserDto> responseWrapper = new ResponseWrapper<MosipUserDto>();
		MosipUserDto mosipUserDto = new MosipUserDto();
		mosipUserDto.setUserId("mock-user");
		mosipUserDto.setMail("mock-user@mosip.io");
		mosipUserDto.setMobile("9999999999");
		mosipUserDto.setRole("MOCK-ROLE");
		responseWrapper.setResponse(mosipUserDto);

		mockServer.expect(ExpectedCount.once(), requestTo(new URI(validateUrl))).andExpect(method(HttpMethod.GET))
				.andRespond(withStatus(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON)
						.body(objectMapper.writeValueAsString(responseWrapper)));
		Cookie cookie = new Cookie("Authorization", "mock_access_token");
		mockMvc.perform(get("/authorize/admin/validateToken").contentType(MediaType.APPLICATION_JSON).cookie(cookie))
				.andExpect(status().isOk()).andExpect(jsonPath("$.response.userId", is("mock-user")));
	}

	@Test
	/**
	 * Asserts GET validate-token maps an IAM HTTP client error to 401.
	 *
	 * @throws Exception if the HTTP call fails
	 */
	public void validateTokenHttpClientExceptionTest() throws Exception {
		ResponseWrapper<MosipUserDto> responseWrapper = new ResponseWrapper<MosipUserDto>();
		ServiceError serviceError = new ServiceError("KER-ATH-401", "un auth");
		List<ServiceError> serviceErrors = new ArrayList<>();
		serviceErrors.add(serviceError);
		responseWrapper.setErrors(serviceErrors);
		mockServer.expect(ExpectedCount.once(), requestTo(new URI(validateUrl))).andExpect(method(HttpMethod.GET))
				.andRespond(withStatus(HttpStatus.UNAUTHORIZED).contentType(MediaType.APPLICATION_JSON)
						.body(objectMapper.writeValueAsString(responseWrapper)));
		Cookie cookie = new Cookie("Authorization", "mock_access_token");
		mockMvc.perform(get("/authorize/admin/validateToken").contentType(MediaType.APPLICATION_JSON).cookie(cookie))
				.andExpect(status().isUnauthorized()).andExpect(jsonPath("$.errors[0].errorCode", is("KER-ATH-401")));
	}

	@Test
	/**
	 * Asserts validate-token maps IAM HTTP 500.
	 *
	 * @throws Exception if the HTTP call fails
	 */
	public void validateTokenInternalServerTest() throws Exception {
		ResponseWrapper<MosipUserDto> responseWrapper = new ResponseWrapper<MosipUserDto>();
		ServiceError serviceError = new ServiceError("KER-ATH-401", "un auth");
		List<ServiceError> serviceErrors = new ArrayList<>();
		serviceErrors.add(serviceError);
		responseWrapper.setErrors(serviceErrors);
		mockServer.expect(ExpectedCount.once(), requestTo(new URI(validateUrl))).andExpect(method(HttpMethod.GET))
				.andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON)
						.body(objectMapper.writeValueAsString("internal server error")));
		Cookie cookie = new Cookie("Authorization", "mock_access_token");
		mockMvc.perform(get("/authorize/admin/validateToken").contentType(MediaType.APPLICATION_JSON).cookie(cookie))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.errors[0].errorCode", is(Errors.REST_EXCEPTION.getErrorCode())));
	}

	@Test
	/**
	 * Asserts validate-token maps an IAM MOSIP errors payload.
	 *
	 * @throws Exception if the HTTP call fails
	 */
	public void validateTokenErrorResponseTest() throws Exception {
		ResponseWrapper<MosipUserDto> responseWrapper = new ResponseWrapper<MosipUserDto>();
		List<ServiceError> errors = new ArrayList<>();
		ServiceError error = new ServiceError("MOCKERRORCODE", "MOCKERROR");
		errors.add(error);
		responseWrapper.setErrors(errors);
		mockServer.expect(ExpectedCount.once(), requestTo(new URI(validateUrl))).andExpect(method(HttpMethod.GET))
				.andRespond(withStatus(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON)
						.body(objectMapper.writeValueAsString(responseWrapper)));
		Cookie cookie = new Cookie("Authorization", "mock_access_token");
		mockMvc.perform(get("/authorize/admin/validateToken").contentType(MediaType.APPLICATION_JSON).cookie(cookie))
				.andExpect(status().isOk()).andExpect(jsonPath("$.errors[0].errorCode", is("MOCKERRORCODE")));
	}

	@Test
	/**
	 * Asserts logout redirects using the configured post-logout URI param.
	 *
	 * @throws Exception if the HTTP call fails
	 */
	public void logoutTest() throws Exception {
		String mockToken = "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJzNmYxcDYwYWVDTTBrNy1NaW9sN0Zib2FTdXlRYm95UC03S1RUTmVWLWZNIn0.eyJqdGkiOiJmYTU4Y2NjMC00ZDRiLTQ2ZjAtYjgwOC0yMWI4ZTdhNmMxNDMiLCJleHAiOjE2NDAxODc3MTksIm5iZiI6MCwiaWF0IjoxNjQwMTUxNzE5LCJpc3MiOiJodHRwczovL2Rldi5tb3NpcC5uZXQva2V5Y2xvYWsvYXV0aC9yZWFsbXMvbW9zaXAiLCJhdWQiOiJhY2NvdW50Iiwic3ViIjoiOWRiZTE0MDEtNTQ1NC00OTlhLTlhMWItNzVhZTY4M2Q0MjZhIiwidHlwIjoiQmVhcmVyIiwiYXpwIjoibW9zaXAtcmVzaWRlbnQtY2xpZW50IiwiYXV0aF90aW1lIjowLCJzZXNzaW9uX3N0YXRlIjoiY2QwYjU5NjEtOTYzMi00NmE0LWIzMzgtODc4MWEzNDVmMTZiIiwiYWNyIjoiMSIsImFsbG93ZWQtb3JpZ2lucyI6WyJodHRwczovL2Rldi5tb3NpcC5uZXQiXSwicmVhbG1fYWNjZXNzIjp7InJvbGVzIjpbIkNSRURFTlRJQUxfUkVRVUVTVCIsIlJFU0lERU5UIiwib2ZmbGluZV9hY2Nlc3MiLCJQQVJUTkVSX0FETUlOIiwidW1hX2F1dGhvcml6YXRpb24iXX0sInJlc291cmNlX2FjY2VzcyI6eyJtb3NpcC1yZXNpZGVudC1jbGllbnQiOnsicm9sZXMiOlsidW1hX3Byb3RlY3Rpb24iXX0sImFjY291bnQiOnsicm9sZXMiOlsibWFuYWdlLWFjY291bnQiLCJtYW5hZ2UtYWNjb3VudC1saW5rcyIsInZpZXctcHJvZmlsZSJdfX0sInNjb3BlIjoicHJvZmlsZSBlbWFpbCIsImNsaWVudEhvc3QiOiIxMC4yNDQuNS4xNDgiLCJlbWFpbF92ZXJpZmllZCI6ZmFsc2UsImNsaWVudElkIjoibW9zaXAtcmVzaWRlbnQtY2xpZW50IiwicHJlZmVycmVkX3VzZXJuYW1lIjoic2VydmljZS1hY2NvdW50LW1vc2lwLXJlc2lkZW50LWNsaWVudCIsImNsaWVudEFkZHJlc3MiOiIxMC4yNDQuNS4xNDgifQ.xZq1m3mBTEvFDENKFOI59QsSl3sd_TSDNbhTAOq4x_x_4voPc4hh08gIxUdsVHfXY4T0P8DdZ1xNt8xd1VWc33Hc4b_3kK7ksGY4wwqtb0-pDLQGajCGuG6vebC1rYcjsGRbJ1Gnrj_F2RNY4Ky6Nq5SAJ1Lh_NVKNKFghAXb3YrlmqlmCB1fCltC4XBqNnF5_k4uzLCu_Wr0lt_M87X97DktaRGLOD2_HY1Ire9YPsWkoO8y7X_DRCY59yQDVgYs2nAiR6Am-c55Q0fEQ0HuB4IJHlhtMHm27dXPdOEhFhR8ZPOyeO6ZIcIm0ZTDjusrruqWy2_yO5fe3XIHkCOAw";
		Cookie cookie = new Cookie("Authorization", mockToken);
		mockMvc.perform(get(
				"/logout/user?redirecturi=" + CryptoUtil.encodeToURLSafeBase64("http://localhost:5000/".getBytes()))
						.contentType(MediaType.APPLICATION_JSON).cookie(cookie))
				.andExpect(status().is3xxRedirection());
	}

	@Test
	/**
	 * Asserts logout with a null token still produces a redirect.
	 *
	 * @throws Exception if the HTTP call fails
	 */
	public void logoutNullTokenTest() throws Exception {
		mockMvc.perform(get(
				"/logout/user?redirecturi=" + CryptoUtil.encodeToURLSafeBase64("http://localhost:5000/".getBytes()))
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.errors[0].errorCode", is(Errors.INVALID_TOKEN.getErrorCode())));
	}

	@Test
	/**
	 * Asserts logout maps an IAM server error.
	 *
	 * @throws Exception if the HTTP call fails
	 */
	public void logoutServerErrorTokenTest() throws Exception {

		String mockToken = "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJzNmYxcDYwYWVDTTBrNy1NaW9sN0Zib2FTdXlRYm95UC03S1RUTmVWLWZNIn0.eyJqdGkiOiJmYTU4Y2NjMC00ZDRiLTQ2ZjAtYjgwOC0yMWI4ZTdhNmMxNDMiLCJleHAiOjE2NDAxODc3MTksIm5iZiI6MCwiaWF0IjoxNjQwMTUxNzE5LCJpc3MiOiJodHRwczovL2Rldi5tb3NpcC5uZXQva2V5Y2xvYWsvYXV0aC9yZWFsbXMvbW9zaXAiLCJhdWQiOiJhY2NvdW50Iiwic3ViIjoiOWRiZTE0MDEtNTQ1NC00OTlhLTlhMWItNzVhZTY4M2Q0MjZhIiwidHlwIjoiQmVhcmVyIiwiYXpwIjoibW9zaXAtcmVzaWRlbnQtY2xpZW50IiwiYXV0aF90aW1lIjowLCJzZXNzaW9uX3N0YXRlIjoiY2QwYjU5NjEtOTYzMi00NmE0LWIzMzgtODc4MWEzNDVmMTZiIiwiYWNyIjoiMSIsImFsbG93ZWQtb3JpZ2lucyI6WyJodHRwczovL2Rldi5tb3NpcC5uZXQiXSwicmVhbG1fYWNjZXNzIjp7InJvbGVzIjpbIkNSRURFTlRJQUxfUkVRVUVTVCIsIlJFU0lERU5UIiwib2ZmbGluZV9hY2Nlc3MiLCJQQVJUTkVSX0FETUlOIiwidW1hX2F1dGhvcml6YXRpb24iXX0sInJlc291cmNlX2FjY2VzcyI6eyJtb3NpcC1yZXNpZGVudC1jbGllbnQiOnsicm9sZXMiOlsidW1hX3Byb3RlY3Rpb24iXX0sImFjY291bnQiOnsicm9sZXMiOlsibWFuYWdlLWFjY291bnQiLCJtYW5hZ2UtYWNjb3VudC1saW5rcyIsInZpZXctcHJvZmlsZSJdfX0sInNjb3BlIjoicHJvZmlsZSBlbWFpbCIsImNsaWVudEhvc3QiOiIxMC4yNDQuNS4xNDgiLCJlbWFpbF92ZXJpZmllZCI6ZmFsc2UsImNsaWVudElkIjoibW9zaXAtcmVzaWRlbnQtY2xpZW50IiwicHJlZmVycmVkX3VzZXJuYW1lIjoic2VydmljZS1hY2NvdW50LW1vc2lwLXJlc2lkZW50LWNsaWVudCIsImNsaWVudEFkZHJlc3MiOiIxMC4yNDQuNS4xNDgifQ.xZq1m3mBTEvFDENKFOI59QsSl3sd_TSDNbhTAOq4x_x_4voPc4hh08gIxUdsVHfXY4T0P8DdZ1xNt8xd1VWc33Hc4b_3kK7ksGY4wwqtb0-pDLQGajCGuG6vebC1rYcjsGRbJ1Gnrj_F2RNY4Ky6Nq5SAJ1Lh_NVKNKFghAXb3YrlmqlmCB1fCltC4XBqNnF5_k4uzLCu_Wr0lt_M87X97DktaRGLOD2_HY1Ire9YPsWkoO8y7X_DRCY59yQDVgYs2nAiR6Am-c55Q0fEQ0HuB4IJHlhtMHm27dXPdOEhFhR8ZPOyeO6ZIcIm0ZTDjusrruqWy2_yO5fe3XIHkCOAw";
		Cookie cookie = new Cookie("Authorization", mockToken);
		mockMvc.perform(get(
				"/logout/user?redirecturi=" + CryptoUtil.encodeToURLSafeBase64("http://localhost:2000/".getBytes())).contentType(MediaType.APPLICATION_JSON).cookie(cookie))
				.andExpect(status().isOk()).andExpect(jsonPath("$.errors[0].errorCode", is(Errors.ALLOWED_URL_EXCEPTION.getErrorCode())));
	}

	
	@Test
	/**
	 * Asserts login redirects to the IAM authorization endpoint.
	 *
	 * @throws Exception if the HTTP call fails
	 */
	public void loginTest() throws Exception {
		//http://localhost:8080/keycloak/auth/realms/mosip/protocol/openid-connect/auth?client_id=mosip-admin-client&redirect_uri=http://localhost:8082/v1/admin/login-redirect/abc&state=mock-state&response_type=code&scope=cls
		Cookie cookie = new Cookie("state", UUID.randomUUID().toString());
		mockMvc.perform(get("/login/abc").contentType(MediaType.APPLICATION_JSON).cookie(cookie)).andExpect(status().is3xxRedirection());
	}
  

	@Test
	/**
	 * Asserts login-redirect exchanges the auth code and sets cookies.
	 *
	 * @throws Exception if the HTTP call fails
	 */
	public void loginRedirectTest() throws Exception {
		AccessTokenResponse accessTokenResponse = new AccessTokenResponse();
		Builder withExpiresAt = JWT.create().withExpiresAt(Date.from(DateUtils2.getUTCCurrentDateTime().plusHours(1).toInstant(ZoneOffset.UTC)));
		withExpiresAt.withClaim(AuthConstant.ISSUER, "http://localhost");
		
		String token = withExpiresAt.withClaim("scope", "aaa bbb").sign(TEST_HMAC);
		
		accessTokenResponse.setAccess_token(token);
		accessTokenResponse.setId_token(token);
		accessTokenResponse.setExpires_in("111");

		mockServer
				.expect(ExpectedCount.once(),
						requestTo(new URI(
								"http://localhost:8080/keycloak/auth/realms/mosip/protocol/openid-connect/token")))
				.andExpect(method(HttpMethod.POST))
				.andRespond(withStatus(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON)
						.body(objectMapper.writeValueAsString(accessTokenResponse)));
		
		Cookie cookie = new Cookie("state", "mockstate");
		mockMvc.perform(get(
				"/login-redirect/aHR0cDovL2xvY2FsaG9zdDo1MDAwLw==?state=mockstate&session_state=mock-session-state&code=mockcode")
						.contentType(MediaType.APPLICATION_JSON).cookie(cookie))
				.andExpect(status().is3xxRedirection());
	}
	
	@Test
	/**
	 * Asserts login-redirect fails when JWT signature verification throws.
	 *
	 * @throws Exception if the HTTP call fails
	 */
	public void loginRedirectTest_signatureVerification_negative() throws Exception {
		AccessTokenResponse accessTokenResponse = new AccessTokenResponse();
		Builder withExpiresAt = JWT.create().withExpiresAt(Date.from(DateUtils2.getUTCCurrentDateTime().plusHours(1).toInstant(ZoneOffset.UTC)));
		withExpiresAt.withClaim(AuthConstant.ISSUER, "http://localhost");
		
		doReturn(ImmutablePair.of(Boolean.FALSE, AuthErrorCode.UNAUTHORIZED)).when(validateTokenHelper)
				.verifyJWTSignagure(any(DecodedJWT.class));
		String token = withExpiresAt.withClaim("scope", "aaa bbb").sign(TEST_HMAC);
		
		accessTokenResponse.setAccess_token(token);
		accessTokenResponse.setExpires_in("111");

		mockServer
				.expect(ExpectedCount.once(),
						requestTo(new URI(
								"http://localhost:8080/keycloak/auth/realms/mosip/protocol/openid-connect/token")))
				.andExpect(method(HttpMethod.POST))
				.andRespond(withStatus(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON)
						.body(objectMapper.writeValueAsString(accessTokenResponse)));
		
		Cookie cookie = new Cookie("state", "mockstate");
		mockMvc.perform(get(
				"/login-redirect/aHR0cDovL2xvY2FsaG9zdDo1MDAwLw==?state=mockstate&session_state=mock-session-state&code=mockcode")
						.contentType(MediaType.APPLICATION_JSON).cookie(cookie))
				.andExpect(status().is(UNAUTHORIZED_STATUS));
	}
	
	@Test
	/**
	 * Asserts login-redirect fails for an expired ID token.
	 *
	 * @throws Exception if the HTTP call fails
	 */
	public void loginRedirectTest_expiredToken() throws Exception {
		AccessTokenResponse accessTokenResponse = new AccessTokenResponse();
		Builder withExpiresAt = JWT.create().withExpiresAt(Date.from(DateUtils2.getUTCCurrentDateTime().minusDays(1).toInstant(ZoneOffset.UTC)));
		withExpiresAt.withClaim(AuthConstant.ISSUER, "http://localhost");
		
		String token = withExpiresAt.withClaim("scope", "aaa bbb").sign(TEST_HMAC);
		
		accessTokenResponse.setAccess_token(token);
		accessTokenResponse.setExpires_in("111");

		mockServer
				.expect(ExpectedCount.once(),
						requestTo(new URI(
								"http://localhost:8080/keycloak/auth/realms/mosip/protocol/openid-connect/token")))
				.andExpect(method(HttpMethod.POST))
				.andRespond(withStatus(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON)
						.body(objectMapper.writeValueAsString(accessTokenResponse)));
		
		Cookie cookie = new Cookie("state", "mockstate");
		mockMvc.perform(get(
				"/login-redirect/aHR0cDovL2xvY2FsaG9zdDo1MDAwLw==?state=mockstate&session_state=mock-session-state&code=mockcode")
						.contentType(MediaType.APPLICATION_JSON).cookie(cookie))
				.andExpect(status().is(401));
	}
	
	@Test
	/**
	 * Asserts login-redirect succeeds when the ID token issuer domain matches.
	 *
	 * @throws Exception if the HTTP call fails
	 */
	public void loginRedirectTest_domain_match_positive() throws Exception {
		AccessTokenResponse accessTokenResponse = new AccessTokenResponse();
		Builder withExpiresAt = JWT.create().withExpiresAt(Date.from(DateUtils2.getUTCCurrentDateTime().plusHours(1).toInstant(ZoneOffset.UTC)));
		withExpiresAt.withClaim(AuthConstant.ISSUER, "http://localhost");
		
		ReflectionTestUtils.setField(validateTokenHelper, "validateIssuerDomain", true);
		String token = withExpiresAt.withClaim("scope", "aaa bbb").sign(TEST_HMAC);
		
		accessTokenResponse.setAccess_token(token);
		accessTokenResponse.setId_token(token);
		accessTokenResponse.setExpires_in("111");

		mockServer
				.expect(ExpectedCount.once(),
						requestTo(new URI(
								"http://localhost:8080/keycloak/auth/realms/mosip/protocol/openid-connect/token")))
				.andExpect(method(HttpMethod.POST))
				.andRespond(withStatus(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON)
						.body(objectMapper.writeValueAsString(accessTokenResponse)));
		
		Cookie cookie = new Cookie("state", "mockstate");
		mockMvc.perform(get(
				"/login-redirect/aHR0cDovL2xvY2FsaG9zdDo1MDAwLw==?state=mockstate&session_state=mock-session-state&code=mockcode")
						.contentType(MediaType.APPLICATION_JSON).cookie(cookie))
				.andExpect(status().is3xxRedirection());
	}
	
	@Test
	/**
	 * Asserts login-redirect fails for an unexpected issuer.
	 *
	 * @throws Exception if the HTTP call fails
	 */
	public void loginRedirectTest_invalid_issuer() throws Exception {
		AccessTokenResponse accessTokenResponse = new AccessTokenResponse();
		Builder withExpiresAt = JWT.create().withExpiresAt(Date.from(DateUtils2.getUTCCurrentDateTime().plusHours(1).toInstant(ZoneOffset.UTC)));
		withExpiresAt.withClaim(AuthConstant.ISSUER, "~!::#@///wrongurl");
		ReflectionTestUtils.setField(validateTokenHelper, "validateIssuerDomain", true);

		String token = withExpiresAt.withClaim("scope", "aaa bbb").sign(TEST_HMAC);
		
		accessTokenResponse.setAccess_token(token);
		accessTokenResponse.setExpires_in("111");

		mockServer
				.expect(ExpectedCount.once(),
						requestTo(new URI(
								"http://localhost:8080/keycloak/auth/realms/mosip/protocol/openid-connect/token")))
				.andExpect(method(HttpMethod.POST))
				.andRespond(withStatus(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON)
						.body(objectMapper.writeValueAsString(accessTokenResponse)));
		
		Cookie cookie = new Cookie("state", "mockstate");
		mockMvc.perform(get(
				"/login-redirect/aHR0cDovL2xvY2FsaG9zdDo1MDAwLw==?state=mockstate&session_state=mock-session-state&code=mockcode")
						.contentType(MediaType.APPLICATION_JSON).cookie(cookie))
				.andExpect(status().is(401));
	}
	
	@Test
	/**
	 * Asserts login-redirect fails when the issuer domain does not match.
	 *
	 * @throws Exception if the HTTP call fails
	 */
	public void loginRedirectTest_domain_match_negative() throws Exception {
		AccessTokenResponse accessTokenResponse = new AccessTokenResponse();
		Builder withExpiresAt = JWT.create().withExpiresAt(Date.from(DateUtils2.getUTCCurrentDateTime().plusHours(1).toInstant(ZoneOffset.UTC)));
		withExpiresAt.withClaim(AuthConstant.ISSUER, "http://someotherdomain");
		
		ReflectionTestUtils.setField(validateTokenHelper, "validateIssuerDomain", true);
		String token = withExpiresAt.withClaim("scope", "aaa bbb").sign(TEST_HMAC);
		
		accessTokenResponse.setAccess_token(token);
		accessTokenResponse.setExpires_in("111");

		mockServer
				.expect(ExpectedCount.once(),
						requestTo(new URI(
								"http://localhost:8080/keycloak/auth/realms/mosip/protocol/openid-connect/token")))
				.andExpect(method(HttpMethod.POST))
				.andRespond(withStatus(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON)
						.body(objectMapper.writeValueAsString(accessTokenResponse)));
		
		Cookie cookie = new Cookie("state", "mockstate");
		mockMvc.perform(get(
				"/login-redirect/aHR0cDovL2xvY2FsaG9zdDo1MDAwLw==?state=mockstate&session_state=mock-session-state&code=mockcode")
						.contentType(MediaType.APPLICATION_JSON).cookie(cookie))
				.andExpect(status().is(401));
	}
	
	@Test
	/**
	 * Asserts login-redirect succeeds when the audience matches.
	 *
	 * @throws Exception if the HTTP call fails
	 */
	public void loginRedirectTest_aud_match_positive() throws Exception {
		AccessTokenResponse accessTokenResponse = new AccessTokenResponse();
		Builder withExpiresAt = JWT.create().withExpiresAt(Date.from(DateUtils2.getUTCCurrentDateTime().plusHours(1).toInstant(ZoneOffset.UTC)));
		withExpiresAt.withAudience("myapp-client");
		
		ReflectionTestUtils.setField(validateTokenHelper, "validateAudClaim", true);
		String token = withExpiresAt.withClaim("scope", "aaa bbb").sign(TEST_HMAC);
		
		accessTokenResponse.setAccess_token(token);
		accessTokenResponse.setId_token(token);
		accessTokenResponse.setExpires_in("111");

		mockServer
				.expect(ExpectedCount.once(),
						requestTo(new URI(
								"http://localhost:8080/keycloak/auth/realms/mosip/protocol/openid-connect/token")))
				.andExpect(method(HttpMethod.POST))
				.andRespond(withStatus(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON)
						.body(objectMapper.writeValueAsString(accessTokenResponse)));
		
		Cookie cookie = new Cookie("state", "mockstate");
		mockMvc.perform(get(
				"/login-redirect/aHR0cDovL2xvY2FsaG9zdDo1MDAwLw==?state=mockstate&session_state=mock-session-state&code=mockcode")
						.contentType(MediaType.APPLICATION_JSON).cookie(cookie))
				.andExpect(status().is3xxRedirection());
	}
	
	@Test
	/**
	 * Asserts login-redirect accepts azp when audience does not match.
	 *
	 * @throws Exception if the HTTP call fails
	 */
	public void loginRedirectTest_aud_match_negative_azp_positive() throws Exception {
		AccessTokenResponse accessTokenResponse = new AccessTokenResponse();
		Builder withExpiresAt = JWT.create().withExpiresAt(Date.from(DateUtils2.getUTCCurrentDateTime().plusHours(1).toInstant(ZoneOffset.UTC)));
		withExpiresAt.withAudience("somether-app-client");
		withExpiresAt.withClaim(AuthConstant.AZP, "myapp-client");
		
		ReflectionTestUtils.setField(validateTokenHelper, "validateAudClaim", true);
		String token = withExpiresAt.withClaim("scope", "aaa bbb").sign(TEST_HMAC);
		
		accessTokenResponse.setAccess_token(token);
		accessTokenResponse.setId_token(token);
		accessTokenResponse.setExpires_in("111");

		mockServer
				.expect(ExpectedCount.once(),
						requestTo(new URI(
								"http://localhost:8080/keycloak/auth/realms/mosip/protocol/openid-connect/token")))
				.andExpect(method(HttpMethod.POST))
				.andRespond(withStatus(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON)
						.body(objectMapper.writeValueAsString(accessTokenResponse)));
		
		Cookie cookie = new Cookie("state", "mockstate");
		mockMvc.perform(get(
				"/login-redirect/aHR0cDovL2xvY2FsaG9zdDo1MDAwLw==?state=mockstate&session_state=mock-session-state&code=mockcode")
						.contentType(MediaType.APPLICATION_JSON).cookie(cookie))
				.andExpect(status().is3xxRedirection());
	}
	
	@Test
	/**
	 * Asserts login-redirect fails when audience and azp are both missing.
	 *
	 * @throws Exception if the HTTP call fails
	 */
	public void loginRedirectTest_aud_match_null_azp_null() throws Exception {
		AccessTokenResponse accessTokenResponse = new AccessTokenResponse();
		Builder withExpiresAt = JWT.create().withExpiresAt(Date.from(DateUtils2.getUTCCurrentDateTime().plusHours(1).toInstant(ZoneOffset.UTC)));
		
		ReflectionTestUtils.setField(validateTokenHelper, "validateAudClaim", true);
		String token = withExpiresAt.withClaim("scope", "aaa bbb").sign(TEST_HMAC);
		
		accessTokenResponse.setAccess_token(token);
		accessTokenResponse.setExpires_in("111");

		mockServer
				.expect(ExpectedCount.once(),
						requestTo(new URI(
								"http://localhost:8080/keycloak/auth/realms/mosip/protocol/openid-connect/token")))
				.andExpect(method(HttpMethod.POST))
				.andRespond(withStatus(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON)
						.body(objectMapper.writeValueAsString(accessTokenResponse)));
		
		Cookie cookie = new Cookie("state", "mockstate");
		mockMvc.perform(get(
				"/login-redirect/aHR0cDovL2xvY2FsaG9zdDo1MDAwLw==?state=mockstate&session_state=mock-session-state&code=mockcode")
						.contentType(MediaType.APPLICATION_JSON).cookie(cookie))
				.andExpect(status().is(401));
	}
	
	@Test
	/**
	 * Asserts login-redirect fails when audience and azp both mismatch.
	 *
	 * @throws Exception if the HTTP call fails
	 */
	public void loginRedirectTest_aud_match_negative_azp_negative() throws Exception {
		AccessTokenResponse accessTokenResponse = new AccessTokenResponse();
		Builder withExpiresAt = JWT.create().withExpiresAt(Date.from(DateUtils2.getUTCCurrentDateTime().plusHours(1).toInstant(ZoneOffset.UTC)));
		withExpiresAt.withAudience("someother-app-client");
		withExpiresAt.withClaim(AuthConstant.AZP, "someother-app-client");
		
		ReflectionTestUtils.setField(validateTokenHelper, "validateAudClaim", true);
		String token = withExpiresAt.withClaim("scope", "aaa bbb").sign(TEST_HMAC);
		
		accessTokenResponse.setAccess_token(token);
		accessTokenResponse.setExpires_in("111");

		mockServer
				.expect(ExpectedCount.once(),
						requestTo(new URI(
								"http://localhost:8080/keycloak/auth/realms/mosip/protocol/openid-connect/token")))
				.andExpect(method(HttpMethod.POST))
				.andRespond(withStatus(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON)
						.body(objectMapper.writeValueAsString(accessTokenResponse)));
		
		Cookie cookie = new Cookie("state", "mockstate");
		mockMvc.perform(get(
				"/login-redirect/aHR0cDovL2xvY2FsaG9zdDo1MDAwLw==?state=mockstate&session_state=mock-session-state&code=mockcode")
						.contentType(MediaType.APPLICATION_JSON).cookie(cookie))
				.andExpect(status().is(401));
	}

	@Test
	/**
	 * Asserts login-redirect follows a hashed redirect URI.
	 *
	 * @throws Exception if the HTTP call fails
	 */
	public void loginRedirectTestWithHash() throws Exception {


		Builder jwtbuilder = JWT.create();
		jwtbuilder.withExpiresAt(Date.from(Instant.now().plusSeconds(100)));
		jwtbuilder.withClaim(AuthConstant.PREFERRED_USERNAME, "12345");
		jwtbuilder.withClaim(AuthConstant.ISSUER, "http://localhost");
		String jwtToken = jwtbuilder.sign(TEST_HMAC);
		
		AccessTokenResponse accessTokenResponse = new AccessTokenResponse();
		accessTokenResponse.setAccess_token(jwtToken);
		accessTokenResponse.setId_token(jwtToken);
		accessTokenResponse.setExpires_in("111");
		
		mockServer
				.expect(ExpectedCount.once(),
						requestTo(new URI(
								"http://localhost:8080/keycloak/auth/realms/mosip/protocol/openid-connect/token")))
				.andExpect(method(HttpMethod.POST))
				.andRespond(withStatus(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON)
						.body(objectMapper.writeValueAsString(accessTokenResponse)));
		
		mockServer
		.expect(ExpectedCount.once(),
				requestTo(new URI(
						"http://localhost:5000/keycloak/auth/realms/mosip/protocol/openid-connect/certs")))
		.andExpect(method(HttpMethod.POST))
		.andRespond(withStatus(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON)
				.body(objectMapper.writeValueAsString(accessTokenResponse)));
		
		Cookie cookie = new Cookie("state", "mockstate");
		mockMvc.perform(get(
				"/login-redirect/aHR0cDovL2xvY2FsaG9zdDo1MDAwLyMvcmFuZG9tcGF0bS9yYW5kb21wYXRo?state=mockstate&session_state=mock-session-state&code=mockcode")
						.contentType(MediaType.APPLICATION_JSON).cookie(cookie))
				.andExpect(status().is3xxRedirection());
	}
	
	@Test
	/**
	 * Asserts login-redirect allows a hashed URI matching the Ant pattern.
	 *
	 * @throws Exception if the HTTP call fails
	 */
	public void loginRedirectTestWithHash_AntPattern_success() throws Exception {
		Builder jwtbuilder = JWT.create();
		jwtbuilder.withExpiresAt(Date.from(Instant.now().plusSeconds(100)));
		jwtbuilder.withClaim(AuthConstant.PREFERRED_USERNAME, "12345");
		jwtbuilder.withClaim(AuthConstant.ISSUER, "http://localhost");
		String jwtToken = jwtbuilder.sign(TEST_HMAC);
		
		AccessTokenResponse accessTokenResponse = new AccessTokenResponse();
		accessTokenResponse.setAccess_token(jwtToken);
		accessTokenResponse.setId_token(jwtToken);
		accessTokenResponse.setExpires_in("111");
		
		mockServer
				.expect(ExpectedCount.once(),
						requestTo(new URI(
								"http://localhost:8080/keycloak/auth/realms/mosip/protocol/openid-connect/token")))
				.andExpect(method(HttpMethod.POST))
				.andRespond(withStatus(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON)
						.body(objectMapper.writeValueAsString(accessTokenResponse)));
		
		mockServer
		.expect(ExpectedCount.once(),
				requestTo(new URI(
						"http://localhost:5000/keycloak/auth/realms/mosip/protocol/openid-connect/certs")))
		.andExpect(method(HttpMethod.POST))
		.andRespond(withStatus(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON)
				.body(objectMapper.writeValueAsString(accessTokenResponse)));
		
		Cookie cookie = new Cookie("state", "mockstate");
		mockMvc.perform(get(
				"/login-redirect/aHR0cDovL2xvY2FsaG9zdDo1MDAwL3NvbWUtdWkvZGFzaGJvYXJk?state=mockstate&session_state=mock-session-state&code=mockcode")
						.contentType(MediaType.APPLICATION_JSON).cookie(cookie))
				.andExpect(status().is3xxRedirection());
	}
	
	@Test
	/**
	 * Asserts login-redirect allows a second hashed URI matching the Ant pattern.
	 *
	 * @throws Exception if the HTTP call fails
	 */
	public void loginRedirectTestWithHash_AntPattern_success2() throws Exception {
		Builder jwtbuilder = JWT.create();
		jwtbuilder.withExpiresAt(Date.from(Instant.now().plusSeconds(100)));
		jwtbuilder.withClaim(AuthConstant.PREFERRED_USERNAME, "12345");
		jwtbuilder.withClaim(AuthConstant.ISSUER, "http://localhost");
		String jwtToken = jwtbuilder.sign(TEST_HMAC);
		
		AccessTokenResponse accessTokenResponse = new AccessTokenResponse();
		accessTokenResponse.setAccess_token(jwtToken);
		accessTokenResponse.setId_token(jwtToken);
		accessTokenResponse.setExpires_in("111");
		
		mockServer
				.expect(ExpectedCount.once(),
						requestTo(new URI(
								"http://localhost:8080/keycloak/auth/realms/mosip/protocol/openid-connect/token")))
				.andExpect(method(HttpMethod.POST))
				.andRespond(withStatus(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON)
						.body(objectMapper.writeValueAsString(accessTokenResponse)));
		
		mockServer
		.expect(ExpectedCount.once(),
				requestTo(new URI(
						"http://localhost:5000/keycloak/auth/realms/mosip/protocol/openid-connect/certs")))
		.andExpect(method(HttpMethod.POST))
		.andRespond(withStatus(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON)
				.body(objectMapper.writeValueAsString(accessTokenResponse)));
		
		Cookie cookie = new Cookie("state", "mockstate");
		mockMvc.perform(get(
				"/login-redirect/aHR0cDovL2xvY2FsaG9zdDo1MDAwL3NvbWUtdWkvIy91aW5zZXJ2aWNlcw?state=mockstate&session_state=mock-session-state&code=mockcode")
						.contentType(MediaType.APPLICATION_JSON).cookie(cookie))
				.andExpect(status().is3xxRedirection());
	}
	
	@Test
	/**
	 * Asserts login-redirect rejects a hashed URI that does not match the Ant pattern.
	 *
	 * @throws Exception if the HTTP call fails
	 */
	public void loginRedirectTestWithHash_AntPattern_failure() throws Exception {
		Builder jwtbuilder = JWT.create();
		jwtbuilder.withExpiresAt(Date.from(Instant.now().plusSeconds(100)));
		jwtbuilder.withClaim(AuthConstant.PREFERRED_USERNAME, "12345");
		jwtbuilder.withClaim(AuthConstant.ISSUER, "http://localhost");
		String jwtToken = jwtbuilder.sign(TEST_HMAC);
		
		AccessTokenResponse accessTokenResponse = new AccessTokenResponse();
		accessTokenResponse.setAccess_token(jwtToken);
		accessTokenResponse.setId_token(jwtToken);
		accessTokenResponse.setExpires_in("111");
		
		mockServer
				.expect(ExpectedCount.once(),
						requestTo(new URI(
								"http://localhost:8080/keycloak/auth/realms/mosip/protocol/openid-connect/token")))
				.andExpect(method(HttpMethod.POST))
				.andRespond(withStatus(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON)
						.body(objectMapper.writeValueAsString(accessTokenResponse)));
		
		mockServer
		.expect(ExpectedCount.once(),
				requestTo(new URI(
						"http://localhost:5000/keycloak/auth/realms/mosip/protocol/openid-connect/certs")))
		.andExpect(method(HttpMethod.POST))
		.andRespond(withStatus(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON)
				.body(objectMapper.writeValueAsString(accessTokenResponse)));
		
		Cookie cookie = new Cookie("state", "mockstate");
		mockMvc.perform(get(
				"/login-redirect/aHR0cDovL2xvY2FsaG9zdDo1MDAwL3NvbWVvdGhlci11aS9kYXNoYm9hcmQ?state=mockstate&session_state=mock-session-state&code=mockcode")
						.contentType(MediaType.APPLICATION_JSON).cookie(cookie))
		.andExpect(status().isOk()).andExpect(jsonPath("$.errors[0].errorCode", is(Errors.ALLOWED_URL_EXCEPTION.getErrorCode())));
	}

	@Test
	/**
	 * Asserts login-redirect with extra ID-token claims succeeds.
	 *
	 * @throws Exception if the HTTP call fails
	 */
	public void loginRedirectWithClaimTest() throws Exception {
		AccessTokenResponse accessTokenResponse = new AccessTokenResponse();
		Builder withExpiresAt = JWT.create().withExpiresAt(Date.from(DateUtils2.getUTCCurrentDateTime().plusHours(1).toInstant(ZoneOffset.UTC)));
		withExpiresAt.withClaim(AuthConstant.ISSUER, "http://localhost");

		String token = withExpiresAt.withClaim("scope", "aaa bbb").sign(TEST_HMAC);

		accessTokenResponse.setAccess_token(token);
		accessTokenResponse.setId_token(token);
		accessTokenResponse.setExpires_in("111");

		mockServer
				.expect(ExpectedCount.once(),
						requestTo(new URI(
								"http://localhost:8080/keycloak/auth/realms/mosip/protocol/openid-connect/token")))
				.andExpect(method(HttpMethod.POST))
				.andRespond(withStatus(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON)
						.body(objectMapper.writeValueAsString(accessTokenResponse)));

		Cookie cookie = new Cookie("state", "mockstate");
		mockMvc.perform(get(
						"/login-redirect/aHR0cDovL2xvY2FsaG9zdDo1MDAwLw==?state=mockstate&session_state=mock-session-state&code=mockcode&claims=mockClaim")
						.contentType(MediaType.APPLICATION_JSON).cookie(cookie))
				.andExpect(status().is3xxRedirection());
	}

	@Test
	/**
	 * Asserts login-redirect uses private-key JWT client auth when enabled.
	 *
	 * @throws Exception if the HTTP call fails
	 */
	public void loginRedirectWithPrivateKeyJwtAuthEnabled() throws Exception {
		AccessTokenResponse accessTokenResponse = new AccessTokenResponse();

		Builder withExpiresAt = JWT.create().withExpiresAt(Date.from(DateUtils2.getUTCCurrentDateTime().plusHours(1).toInstant(ZoneOffset.UTC)));
		withExpiresAt.withClaim(AuthConstant.ISSUER, "http://localhost");
		ReflectionTestUtils.setField(loginService, "isJwtAuthEnabled", true);

		String token = withExpiresAt.withClaim("scope", "aaa bbb").sign(TEST_HMAC);
		JWTSignatureResponseDto jwtSignatureResponseDto = new JWTSignatureResponseDto();
		jwtSignatureResponseDto.setJwtSignedData("abc");
		jwtSignatureResponseDto.setTimestamp(DateUtils2.getUTCCurrentDateTime());
		ResponseWrapper<JWTSignatureResponseDto> responseWrapper = new ResponseWrapper<>();
		responseWrapper.setResponse(jwtSignatureResponseDto);
		when(selfTokenRestTemplate.exchange((URI) any(), (HttpMethod) any(), (HttpEntity<?>) any(), (Class<Object>) any()))
				.thenReturn(ResponseEntity.ok(responseWrapper));
		accessTokenResponse.setAccess_token(token);
		accessTokenResponse.setId_token(token);
		accessTokenResponse.setExpires_in("111");
		mockServer
				.expect(ExpectedCount.once(),
						requestTo(new URI(
								"http://localhost:8080/keycloak/auth/realms/mosip/protocol/openid-connect/token")))
				.andExpect(method(HttpMethod.POST))
				.andRespond(withStatus(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON)
						.body(objectMapper.writeValueAsString(accessTokenResponse)));
		Cookie cookie = new Cookie("state", "mockstate");
		mockMvc.perform(get(
						"/login-redirect/aHR0cDovL2xvY2FsaG9zdDo1MDAwLw==?state=mockstate&session_state=mock-session-state&code=mockcode&claims=mockClaim")
						.contentType(MediaType.APPLICATION_JSON).cookie(cookie))
				.andExpect(status().is3xxRedirection());
	}

	@Test
	/**
	 * Asserts login-redirect maps an IAM server exception.
	 *
	 * @throws Exception if the HTTP call fails
	 */
	public void loginServerExceptionRedirectTest() throws Exception {
		IAMErrorResponseDto errorResponseDto = new IAMErrorResponseDto();
		errorResponseDto.setError("seerver error");
		errorResponseDto.setError_description("sending mock error");

		mockServer
				.expect(ExpectedCount.once(),
						requestTo(new URI(
								"http://localhost:8080/keycloak/auth/realms/mosip/protocol/openid-connect/token")))
				.andExpect(method(HttpMethod.POST))
				.andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON)
						.body(objectMapper.writeValueAsString(errorResponseDto)));
		Cookie cookie = new Cookie("state", "mockstate");
		mockMvc.perform(get("/login-redirect/abc?state=mockstate&session_state=mock-session-state&code=mockcode")
				.contentType(MediaType.APPLICATION_JSON).cookie(cookie)).andExpect(status().is2xxSuccessful())
				.andExpect(jsonPath("$.errors[0].message", isA(String.class)));
	}

	@Test
	/**
	 * Asserts login with an empty state UUID is rejected.
	 *
	 * @throws Exception if the HTTP call fails
	 */
	public void loginUUIDEmptyTest() throws Exception {
		// http://localhost:8080/keycloak/auth/realms/mosip/protocol/openid-connect/auth?client_id=mosip-admin-client&redirect_uri=http://localhost:8082/v1/admin/login-redirect/abc&state=mock-state&response_type=code&scope=cls
		Cookie cookie = new Cookie("state", "");
		mockMvc.perform(get("/login/abc").contentType(MediaType.APPLICATION_JSON).cookie(cookie))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.errors[0].errorCode", is(Errors.STATE_NULL_EXCEPTION.getErrorCode())));
	}

	@Test
	/**
	 * Asserts login with a null state UUID is rejected.
	 *
	 * @throws Exception if the HTTP call fails
	 */
	public void loginUUIDNullTest() throws Exception {
		// http://localhost:8080/keycloak/auth/realms/mosip/protocol/openid-connect/auth?client_id=mosip-admin-client&redirect_uri=http://localhost:8082/v1/admin/login-redirect/abc&state=mock-state&response_type=code&scope=cls
		mockMvc.perform(get("/login/abc").contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				.andExpect(jsonPath("$.errors[0].errorCode", is(Errors.STATE_NULL_EXCEPTION.getErrorCode())));
	}

	@Test
	/**
	 * Asserts login with a non-UUID state is rejected.
	 *
	 * @throws Exception if the HTTP call fails
	 */
	public void loginInvalidUUIDTest() throws Exception {
		// http://localhost:8080/keycloak/auth/realms/mosip/protocol/openid-connect/auth?client_id=mosip-admin-client&redirect_uri=http://localhost:8082/v1/admin/login-redirect/abc&state=mock-state&response_type=code&scope=cls
		Cookie cookie = new Cookie("state", "abc/nabc");
		mockMvc.perform(get("/login/abc").contentType(MediaType.APPLICATION_JSON).cookie(cookie))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.errors[0].errorCode", is(Errors.STATE_NOT_UUID_EXCEPTION.getErrorCode())));
	}

	@Test
	/**
	 * Asserts logout redirect host is validated against allowed URLs.
	 *
	 * @throws Exception if the HTTP call fails
	 */
	public void logoutRedirectHostCheckTest() throws Exception {
		AccessTokenResponse accessTokenResponse = new AccessTokenResponse();
		Builder withExpiresAt = JWT.create().withExpiresAt(Date.from(DateUtils2.getUTCCurrentDateTime().plusHours(1).toInstant(ZoneOffset.UTC)));
		withExpiresAt.withClaim(AuthConstant.ISSUER, "http://localhost");
		
		String token = withExpiresAt.withClaim("scope", "aaa bbb").sign(TEST_HMAC);

		accessTokenResponse.setAccess_token(token);
		accessTokenResponse.setId_token(token);
		accessTokenResponse.setExpires_in("111");

		mockServer
				.expect(ExpectedCount.once(),
						requestTo(new URI(
								"http://localhost:8080/keycloak/auth/realms/mosip/protocol/openid-connect/token")))
				.andExpect(method(HttpMethod.POST))
				.andRespond(withStatus(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON)
						.body(objectMapper.writeValueAsString(accessTokenResponse)));
		Cookie cookie = new Cookie("state", "mockstate");
		mockMvc.perform(get(
				"/login-redirect/aHR0cDovL2FiOjUwMDAv?state=mockstate&session_state=mock-session-state&code=mockcode")
						.contentType(MediaType.APPLICATION_JSON).cookie(cookie))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.errors[0].errorCode", is(Errors.ALLOWED_URL_EXCEPTION.getErrorCode())));
		
	}

}
