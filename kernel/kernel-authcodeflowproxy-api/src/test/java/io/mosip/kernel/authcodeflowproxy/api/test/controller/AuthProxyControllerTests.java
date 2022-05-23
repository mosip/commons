package io.mosip.kernel.authcodeflowproxy.api.test.controller;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.isA;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.Cookie;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.kernel.authcodeflowproxy.api.constants.Errors;
import io.mosip.kernel.authcodeflowproxy.api.dto.AccessTokenResponse;
import io.mosip.kernel.authcodeflowproxy.api.dto.IAMErrorResponseDto;
import io.mosip.kernel.authcodeflowproxy.api.dto.MosipUserDto;
import io.mosip.kernel.authcodeflowproxy.api.test.AuthProxyFlowTestBootApplication;
import io.mosip.kernel.core.exception.ServiceError;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.util.CryptoUtil;

@SpringBootTest(classes = { AuthProxyFlowTestBootApplication.class })
@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
public class AuthProxyControllerTests {

	@Value("${auth.server.admin.validate.url}")
	private String validateUrl;

	@Value("${mosip.iam.post-logout-uri-param-key}")
	private String postLogoutRedirectURIParamKey;

	@Autowired
	private RestTemplate restTemplate;

	private MockRestServiceServer mockServer;

	@Before
	public void init() {
		mockServer = MockRestServiceServer.createServer(restTemplate);

	}

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

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
	public void logoutTest() throws Exception {
		String mockToken = "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJzNmYxcDYwYWVDTTBrNy1NaW9sN0Zib2FTdXlRYm95UC03S1RUTmVWLWZNIn0.eyJqdGkiOiJmYTU4Y2NjMC00ZDRiLTQ2ZjAtYjgwOC0yMWI4ZTdhNmMxNDMiLCJleHAiOjE2NDAxODc3MTksIm5iZiI6MCwiaWF0IjoxNjQwMTUxNzE5LCJpc3MiOiJodHRwczovL2Rldi5tb3NpcC5uZXQva2V5Y2xvYWsvYXV0aC9yZWFsbXMvbW9zaXAiLCJhdWQiOiJhY2NvdW50Iiwic3ViIjoiOWRiZTE0MDEtNTQ1NC00OTlhLTlhMWItNzVhZTY4M2Q0MjZhIiwidHlwIjoiQmVhcmVyIiwiYXpwIjoibW9zaXAtcmVzaWRlbnQtY2xpZW50IiwiYXV0aF90aW1lIjowLCJzZXNzaW9uX3N0YXRlIjoiY2QwYjU5NjEtOTYzMi00NmE0LWIzMzgtODc4MWEzNDVmMTZiIiwiYWNyIjoiMSIsImFsbG93ZWQtb3JpZ2lucyI6WyJodHRwczovL2Rldi5tb3NpcC5uZXQiXSwicmVhbG1fYWNjZXNzIjp7InJvbGVzIjpbIkNSRURFTlRJQUxfUkVRVUVTVCIsIlJFU0lERU5UIiwib2ZmbGluZV9hY2Nlc3MiLCJQQVJUTkVSX0FETUlOIiwidW1hX2F1dGhvcml6YXRpb24iXX0sInJlc291cmNlX2FjY2VzcyI6eyJtb3NpcC1yZXNpZGVudC1jbGllbnQiOnsicm9sZXMiOlsidW1hX3Byb3RlY3Rpb24iXX0sImFjY291bnQiOnsicm9sZXMiOlsibWFuYWdlLWFjY291bnQiLCJtYW5hZ2UtYWNjb3VudC1saW5rcyIsInZpZXctcHJvZmlsZSJdfX0sInNjb3BlIjoicHJvZmlsZSBlbWFpbCIsImNsaWVudEhvc3QiOiIxMC4yNDQuNS4xNDgiLCJlbWFpbF92ZXJpZmllZCI6ZmFsc2UsImNsaWVudElkIjoibW9zaXAtcmVzaWRlbnQtY2xpZW50IiwicHJlZmVycmVkX3VzZXJuYW1lIjoic2VydmljZS1hY2NvdW50LW1vc2lwLXJlc2lkZW50LWNsaWVudCIsImNsaWVudEFkZHJlc3MiOiIxMC4yNDQuNS4xNDgifQ.xZq1m3mBTEvFDENKFOI59QsSl3sd_TSDNbhTAOq4x_x_4voPc4hh08gIxUdsVHfXY4T0P8DdZ1xNt8xd1VWc33Hc4b_3kK7ksGY4wwqtb0-pDLQGajCGuG6vebC1rYcjsGRbJ1Gnrj_F2RNY4Ky6Nq5SAJ1Lh_NVKNKFghAXb3YrlmqlmCB1fCltC4XBqNnF5_k4uzLCu_Wr0lt_M87X97DktaRGLOD2_HY1Ire9YPsWkoO8y7X_DRCY59yQDVgYs2nAiR6Am-c55Q0fEQ0HuB4IJHlhtMHm27dXPdOEhFhR8ZPOyeO6ZIcIm0ZTDjusrruqWy2_yO5fe3XIHkCOAw";
		Cookie cookie = new Cookie("Authorization", mockToken);
		mockMvc.perform(get(
				"/logout/user?redirecturi=" + CryptoUtil.encodeToURLSafeBase64("http://localhost:5000/".getBytes()))
						.contentType(MediaType.APPLICATION_JSON).cookie(cookie))
				.andExpect(status().is3xxRedirection());
	}

	@Test
	public void logoutNullTokenTest() throws Exception {
		mockMvc.perform(get(
				"/logout/user?redirecturi=" + CryptoUtil.encodeToURLSafeBase64("http://localhost:5000/".getBytes()))
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.errors[0].errorCode", is(Errors.INVALID_TOKEN.getErrorCode())));
	}

	@Test
	public void logoutServerErrorTokenTest() throws Exception {

		String mockToken = "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJzNmYxcDYwYWVDTTBrNy1NaW9sN0Zib2FTdXlRYm95UC03S1RUTmVWLWZNIn0.eyJqdGkiOiJmYTU4Y2NjMC00ZDRiLTQ2ZjAtYjgwOC0yMWI4ZTdhNmMxNDMiLCJleHAiOjE2NDAxODc3MTksIm5iZiI6MCwiaWF0IjoxNjQwMTUxNzE5LCJpc3MiOiJodHRwczovL2Rldi5tb3NpcC5uZXQva2V5Y2xvYWsvYXV0aC9yZWFsbXMvbW9zaXAiLCJhdWQiOiJhY2NvdW50Iiwic3ViIjoiOWRiZTE0MDEtNTQ1NC00OTlhLTlhMWItNzVhZTY4M2Q0MjZhIiwidHlwIjoiQmVhcmVyIiwiYXpwIjoibW9zaXAtcmVzaWRlbnQtY2xpZW50IiwiYXV0aF90aW1lIjowLCJzZXNzaW9uX3N0YXRlIjoiY2QwYjU5NjEtOTYzMi00NmE0LWIzMzgtODc4MWEzNDVmMTZiIiwiYWNyIjoiMSIsImFsbG93ZWQtb3JpZ2lucyI6WyJodHRwczovL2Rldi5tb3NpcC5uZXQiXSwicmVhbG1fYWNjZXNzIjp7InJvbGVzIjpbIkNSRURFTlRJQUxfUkVRVUVTVCIsIlJFU0lERU5UIiwib2ZmbGluZV9hY2Nlc3MiLCJQQVJUTkVSX0FETUlOIiwidW1hX2F1dGhvcml6YXRpb24iXX0sInJlc291cmNlX2FjY2VzcyI6eyJtb3NpcC1yZXNpZGVudC1jbGllbnQiOnsicm9sZXMiOlsidW1hX3Byb3RlY3Rpb24iXX0sImFjY291bnQiOnsicm9sZXMiOlsibWFuYWdlLWFjY291bnQiLCJtYW5hZ2UtYWNjb3VudC1saW5rcyIsInZpZXctcHJvZmlsZSJdfX0sInNjb3BlIjoicHJvZmlsZSBlbWFpbCIsImNsaWVudEhvc3QiOiIxMC4yNDQuNS4xNDgiLCJlbWFpbF92ZXJpZmllZCI6ZmFsc2UsImNsaWVudElkIjoibW9zaXAtcmVzaWRlbnQtY2xpZW50IiwicHJlZmVycmVkX3VzZXJuYW1lIjoic2VydmljZS1hY2NvdW50LW1vc2lwLXJlc2lkZW50LWNsaWVudCIsImNsaWVudEFkZHJlc3MiOiIxMC4yNDQuNS4xNDgifQ.xZq1m3mBTEvFDENKFOI59QsSl3sd_TSDNbhTAOq4x_x_4voPc4hh08gIxUdsVHfXY4T0P8DdZ1xNt8xd1VWc33Hc4b_3kK7ksGY4wwqtb0-pDLQGajCGuG6vebC1rYcjsGRbJ1Gnrj_F2RNY4Ky6Nq5SAJ1Lh_NVKNKFghAXb3YrlmqlmCB1fCltC4XBqNnF5_k4uzLCu_Wr0lt_M87X97DktaRGLOD2_HY1Ire9YPsWkoO8y7X_DRCY59yQDVgYs2nAiR6Am-c55Q0fEQ0HuB4IJHlhtMHm27dXPdOEhFhR8ZPOyeO6ZIcIm0ZTDjusrruqWy2_yO5fe3XIHkCOAw";
		Cookie cookie = new Cookie("Authorization", mockToken);
		mockMvc.perform(get(
				"/logout/user?redirecturi=" + CryptoUtil.encodeToURLSafeBase64("http://localhost:2000/".getBytes())).contentType(MediaType.APPLICATION_JSON).cookie(cookie))
				.andExpect(status().isOk()).andExpect(jsonPath("$.errors[0].errorCode", is(Errors.ALLOWED_URL_EXCEPTION.getErrorCode())));
	}

	
	@Test
	public void loginTest() throws Exception {
		//http://localhost:8080/keycloak/auth/realms/mosip/protocol/openid-connect/auth?client_id=mosip-admin-client&redirect_uri=http://localhost:8082/v1/admin/login-redirect/abc&state=mock-state&response_type=code&scope=cls
		Cookie cookie = new Cookie("state", UUID.randomUUID().toString());
		mockMvc.perform(get("/login/abc").contentType(MediaType.APPLICATION_JSON).cookie(cookie)).andExpect(status().is3xxRedirection());
	}
  

	@Test
	public void loginRedirectTest() throws Exception {
		AccessTokenResponse accessTokenResponse = new AccessTokenResponse();
		accessTokenResponse.setAccess_token("mock-access-token");
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
	public void loginRedirectTestWithHash() throws Exception {
		AccessTokenResponse accessTokenResponse = new AccessTokenResponse();
		accessTokenResponse.setAccess_token("mock-access-token");
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
				"/login-redirect/aHR0cDovL2xvY2FsaG9zdDo1MDAwLyMvcmFuZG9tcGF0bS9yYW5kb21wYXRo?state=mockstate&session_state=mock-session-state&code=mockcode")
						.contentType(MediaType.APPLICATION_JSON).cookie(cookie))
				.andExpect(status().is3xxRedirection());
	}

	@Test
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
	public void loginUUIDEmptyTest() throws Exception {
		// http://localhost:8080/keycloak/auth/realms/mosip/protocol/openid-connect/auth?client_id=mosip-admin-client&redirect_uri=http://localhost:8082/v1/admin/login-redirect/abc&state=mock-state&response_type=code&scope=cls
		Cookie cookie = new Cookie("state", "");
		mockMvc.perform(get("/login/abc").contentType(MediaType.APPLICATION_JSON).cookie(cookie))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.errors[0].errorCode", is(Errors.STATE_NULL_EXCEPTION.getErrorCode())));
	}

	@Test
	public void loginUUIDNullTest() throws Exception {
		// http://localhost:8080/keycloak/auth/realms/mosip/protocol/openid-connect/auth?client_id=mosip-admin-client&redirect_uri=http://localhost:8082/v1/admin/login-redirect/abc&state=mock-state&response_type=code&scope=cls
		mockMvc.perform(get("/login/abc").contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				.andExpect(jsonPath("$.errors[0].errorCode", is(Errors.STATE_NULL_EXCEPTION.getErrorCode())));
	}

	@Test
	public void loginInvalidUUIDTest() throws Exception {
		// http://localhost:8080/keycloak/auth/realms/mosip/protocol/openid-connect/auth?client_id=mosip-admin-client&redirect_uri=http://localhost:8082/v1/admin/login-redirect/abc&state=mock-state&response_type=code&scope=cls
		Cookie cookie = new Cookie("state", "abc/nabc");
		mockMvc.perform(get("/login/abc").contentType(MediaType.APPLICATION_JSON).cookie(cookie))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.errors[0].errorCode", is(Errors.STATE_NOT_UUID_EXCEPTION.getErrorCode())));
	}

	@Test
	public void logoutRedirectHostCheckTest() throws Exception {
		AccessTokenResponse accessTokenResponse = new AccessTokenResponse();
		accessTokenResponse.setAccess_token("mock-access-token");
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
		;
	}

}
