package io.mosip.kernel.auth.controller.test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;

import jakarta.servlet.http.Cookie;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.kernel.auth.controller.AuthController;
import io.mosip.kernel.auth.defaultimpl.config.MosipEnvironment;
import io.mosip.kernel.auth.defaultimpl.dto.UserDetailsRequestDto;
import io.mosip.kernel.auth.test.AuthTestBootApplication;
import io.mosip.kernel.core.authmanager.model.AuthNResponse;
import io.mosip.kernel.core.authmanager.model.AuthNResponseDto;
import io.mosip.kernel.core.authmanager.model.ClientSecret;
import io.mosip.kernel.core.authmanager.model.IndividualIdDto;
import io.mosip.kernel.core.authmanager.model.MosipUserDto;
import io.mosip.kernel.core.authmanager.model.MosipUserListDto;
import io.mosip.kernel.core.authmanager.model.MosipUserSalt;
import io.mosip.kernel.core.authmanager.model.MosipUserSaltListDto;
import io.mosip.kernel.core.authmanager.model.MosipUserTokenDto;
import io.mosip.kernel.core.authmanager.model.RIdDto;
import io.mosip.kernel.core.authmanager.model.Role;
import io.mosip.kernel.core.authmanager.model.RolesListDto;
import io.mosip.kernel.core.authmanager.model.UserOtp;
import io.mosip.kernel.core.http.RequestWrapper;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.openid.bridge.api.service.AuthService;

/**
 * MockMvc tests for {@link AuthController} validate-token, OTP, client-secret,
 * logout, roles, user details, RID, and individual-id endpoints.
 * <p>
 * Uses Boot 4 {@link AutoConfigureMockMvc} from
 * {@code org.springframework.boot.webmvc.test.autoconfigure} and
 * {@link MockitoBean} for {@link AuthService}.
 */
@SpringBootTest(classes = { AuthTestBootApplication.class })
@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
public class AuthControllerTest {

	/** Whether auth cookies are marked Secure. */
	@Value("${mosip.security.secure-cookie:false}")
	private boolean isSecureCookie;

	/** Splitter used when encoding login redirect URIs. */
	@Value("${mosip.kernel.auth-code-url-splitter:#URISPLITTER#}")
	private String urlSplitter;

	/** Allowed login/logout redirect URLs from test properties. */
	@Value("#{'${auth.allowed.urls}'.split(',')}")
	private List<String> allowedUrls;

	/**
	 * Autowired reference for {@link MosipEnvironment}
	 */

	@Autowired
	private MosipEnvironment mosipEnvironment;

	/**
	 * Autowired reference for {@link AuthService}
	 */

	@MockitoBean
	private AuthService authService;

	/** MockMvc for HTTP calls against the auth controller. */
	@Autowired
	private MockMvc mockMvc;

	/** JSON mapper used to serialize request wrappers. */
	@Autowired
	private ObjectMapper objectMapper;

	/** Auth controller from the test context. */
	@Autowired
	private AuthController authController;
	
	/**
	 * Asserts POST /authorize/validateToken returns the user when the
	 * Authorization cookie is valid.
	 *
	 * @throws Exception if the HTTP call fails
	 */
	@Test
	public void getValidateTokenTest() throws Exception {

		// resp
		MosipUserTokenDto mosipUserTokenDto = new MosipUserTokenDto();
		mosipUserTokenDto.setToken("eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJzNmYxcDYwYWVDTTBrNy1NaW9sN0Zib2FTdXlRYm95UC03S1RUTmVWLWZNIn0.eyJqdGkiOiJmYTU4Y2NjMC00ZDRiLTQ2ZjAtYjgwOC0yMWI4ZTdhNmMxNDMiLCJleHAiOjE2NDAxODc3MTksIm5iZiI6MCwiaWF0IjoxNjQwMTUxNzE5LCJpc3MiOiJodHRwczovL2Rldi5tb3NpcC5uZXQva2V5Y2xvYWsvYXV0aC9yZWFsbXMvbW9zaXAiLCJhdWQiOiJhY2NvdW50Iiwic3ViIjoiOWRiZTE0MDEtNTQ1NC00OTlhLTlhMWItNzVhZTY4M2Q0MjZhIiwidHlwIjoiQmVhcmVyIiwiYXpwIjoibW9zaXAtcmVzaWRlbnQtY2xpZW50IiwiYXV0aF90aW1lIjowLCJzZXNzaW9uX3N0YXRlIjoiY2QwYjU5NjEtOTYzMi00NmE0LWIzMzgtODc4MWEzNDVmMTZiIiwiYWNyIjoiMSIsImFsbG93ZWQtb3JpZ2lucyI6WyJodHRwczovL2Rldi5tb3NpcC5uZXQiXSwicmVhbG1fYWNjZXNzIjp7InJvbGVzIjpbIkNSRURFTlRJQUxfUkVRVUVTVCIsIlJFU0lERU5UIiwib2ZmbGluZV9hY2Nlc3MiLCJQQVJUTkVSX0FETUlOIiwidW1hX2F1dGhvcml6YXRpb24iXX0sInJlc291cmNlX2FjY2VzcyI6eyJtb3NpcC1yZXNpZGVudC1jbGllbnQiOnsicm9sZXMiOlsidW1hX3Byb3RlY3Rpb24iXX0sImFjY291bnQiOnsicm9sZXMiOlsibWFuYWdlLWFjY291bnQiLCJtYW5hZ2UtYWNjb3VudC1saW5rcyIsInZpZXctcHJvZmlsZSJdfX0sInNjb3BlIjoicHJvZmlsZSBlbWFpbCIsImNsaWVudEhvc3QiOiIxMC4yNDQuNS4xNDgiLCJlbWFpbF92ZXJpZmllZCI6ZmFsc2UsImNsaWVudElkIjoibW9zaXAtcmVzaWRlbnQtY2xpZW50IiwicHJlZmVycmVkX3VzZXJuYW1lIjoic2VydmljZS1hY2NvdW50LW1vc2lwLXJlc2lkZW50LWNsaWVudCIsImNsaWVudEFkZHJlc3MiOiIxMC4yNDQuNS4xNDgifQ.xZq1m3mBTEvFDENKFOI59QsSl3sd_TSDNbhTAOq4x_x_4voPc4hh08gIxUdsVHfXY4T0P8DdZ1xNt8xd1VWc33Hc4b_3kK7ksGY4wwqtb0-pDLQGajCGuG6vebC1rYcjsGRbJ1Gnrj_F2RNY4Ky6Nq5SAJ1Lh_NVKNKFghAXb3YrlmqlmCB1fCltC4XBqNnF5_k4uzLCu_Wr0lt_M87X97DktaRGLOD2_HY1Ire9YPsWkoO8y7X_DRCY59yQDVgYs2nAiR6Am-c55Q0fEQ0HuB4IJHlhtMHm27dXPdOEhFhR8ZPOyeO6ZIcIm0ZTDjusrruqWy2_yO5fe3XIHkCOAw");
		mosipUserTokenDto.setExpTime(3000);
		MosipUserDto mosipUserDto = new MosipUserDto();
		mosipUserDto.setUserId("mock-user");
		mosipUserDto.setMail("mock-user@mosip.io");
		mosipUserDto.setMobile("9999999999");
		mosipUserDto.setRole("MOCK-ROLE");
		
		mosipUserTokenDto.setMosipUserDto(mosipUserDto);
		when(authService.validateToken(Mockito.any()))
						.thenReturn(mosipUserTokenDto);
		

		Cookie cookie = new Cookie("Authorization",
				"eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJzNmYxcDYwYWVDTTBrNy1NaW9sN0Zib2FTdXlRYm95UC03S1RUTmVWLWZNIn0.eyJqdGkiOiJmYTU4Y2NjMC00ZDRiLTQ2ZjAtYjgwOC0yMWI4ZTdhNmMxNDMiLCJleHAiOjE2NDAxODc3MTksIm5iZiI6MCwiaWF0IjoxNjQwMTUxNzE5LCJpc3MiOiJodHRwczovL2Rldi5tb3NpcC5uZXQva2V5Y2xvYWsvYXV0aC9yZWFsbXMvbW9zaXAiLCJhdWQiOiJhY2NvdW50Iiwic3ViIjoiOWRiZTE0MDEtNTQ1NC00OTlhLTlhMWItNzVhZTY4M2Q0MjZhIiwidHlwIjoiQmVhcmVyIiwiYXpwIjoibW9zaXAtcmVzaWRlbnQtY2xpZW50IiwiYXV0aF90aW1lIjowLCJzZXNzaW9uX3N0YXRlIjoiY2QwYjU5NjEtOTYzMi00NmE0LWIzMzgtODc4MWEzNDVmMTZiIiwiYWNyIjoiMSIsImFsbG93ZWQtb3JpZ2lucyI6WyJodHRwczovL2Rldi5tb3NpcC5uZXQiXSwicmVhbG1fYWNjZXNzIjp7InJvbGVzIjpbIkNSRURFTlRJQUxfUkVRVUVTVCIsIlJFU0lERU5UIiwib2ZmbGluZV9hY2Nlc3MiLCJQQVJUTkVSX0FETUlOIiwidW1hX2F1dGhvcml6YXRpb24iXX0sInJlc291cmNlX2FjY2VzcyI6eyJtb3NpcC1yZXNpZGVudC1jbGllbnQiOnsicm9sZXMiOlsidW1hX3Byb3RlY3Rpb24iXX0sImFjY291bnQiOnsicm9sZXMiOlsibWFuYWdlLWFjY291bnQiLCJtYW5hZ2UtYWNjb3VudC1saW5rcyIsInZpZXctcHJvZmlsZSJdfX0sInNjb3BlIjoicHJvZmlsZSBlbWFpbCIsImNsaWVudEhvc3QiOiIxMC4yNDQuNS4xNDgiLCJlbWFpbF92ZXJpZmllZCI6ZmFsc2UsImNsaWVudElkIjoibW9zaXAtcmVzaWRlbnQtY2xpZW50IiwicHJlZmVycmVkX3VzZXJuYW1lIjoic2VydmljZS1hY2NvdW50LW1vc2lwLXJlc2lkZW50LWNsaWVudCIsImNsaWVudEFkZHJlc3MiOiIxMC4yNDQuNS4xNDgifQ.xZq1m3mBTEvFDENKFOI59QsSl3sd_TSDNbhTAOq4x_x_4voPc4hh08gIxUdsVHfXY4T0P8DdZ1xNt8xd1VWc33Hc4b_3kK7ksGY4wwqtb0-pDLQGajCGuG6vebC1rYcjsGRbJ1Gnrj_F2RNY4Ky6Nq5SAJ1Lh_NVKNKFghAXb3YrlmqlmCB1fCltC4XBqNnF5_k4uzLCu_Wr0lt_M87X97DktaRGLOD2_HY1Ire9YPsWkoO8y7X_DRCY59yQDVgYs2nAiR6Am-c55Q0fEQ0HuB4IJHlhtMHm27dXPdOEhFhR8ZPOyeO6ZIcIm0ZTDjusrruqWy2_yO5fe3XIHkCOAw");
		mockMvc.perform(post("/authorize/validateToken").contentType(MediaType.APPLICATION_JSON).cookie(cookie))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.response.userId", is(mosipUserDto.getUserId())));
	
	}
	
	@Test
	/**
	 * Asserts validateToken returns KER-ATH-007 when the cookie is not Authorization.
	 *
	 * @throws Exception if the HTTP call fails
	 */
	public void getValidateTokenNullTokenTest() throws Exception {

		// resp
		MosipUserTokenDto mosipUserTokenDto = new MosipUserTokenDto();
		mosipUserTokenDto.setToken("eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJzNmYxcDYwYWVDTTBrNy1NaW9sN0Zib2FTdXlRYm95UC03S1RUTmVWLWZNIn0.eyJqdGkiOiJmYTU4Y2NjMC00ZDRiLTQ2ZjAtYjgwOC0yMWI4ZTdhNmMxNDMiLCJleHAiOjE2NDAxODc3MTksIm5iZiI6MCwiaWF0IjoxNjQwMTUxNzE5LCJpc3MiOiJodHRwczovL2Rldi5tb3NpcC5uZXQva2V5Y2xvYWsvYXV0aC9yZWFsbXMvbW9zaXAiLCJhdWQiOiJhY2NvdW50Iiwic3ViIjoiOWRiZTE0MDEtNTQ1NC00OTlhLTlhMWItNzVhZTY4M2Q0MjZhIiwidHlwIjoiQmVhcmVyIiwiYXpwIjoibW9zaXAtcmVzaWRlbnQtY2xpZW50IiwiYXV0aF90aW1lIjowLCJzZXNzaW9uX3N0YXRlIjoiY2QwYjU5NjEtOTYzMi00NmE0LWIzMzgtODc4MWEzNDVmMTZiIiwiYWNyIjoiMSIsImFsbG93ZWQtb3JpZ2lucyI6WyJodHRwczovL2Rldi5tb3NpcC5uZXQiXSwicmVhbG1fYWNjZXNzIjp7InJvbGVzIjpbIkNSRURFTlRJQUxfUkVRVUVTVCIsIlJFU0lERU5UIiwib2ZmbGluZV9hY2Nlc3MiLCJQQVJUTkVSX0FETUlOIiwidW1hX2F1dGhvcml6YXRpb24iXX0sInJlc291cmNlX2FjY2VzcyI6eyJtb3NpcC1yZXNpZGVudC1jbGllbnQiOnsicm9sZXMiOlsidW1hX3Byb3RlY3Rpb24iXX0sImFjY291bnQiOnsicm9sZXMiOlsibWFuYWdlLWFjY291bnQiLCJtYW5hZ2UtYWNjb3VudC1saW5rcyIsInZpZXctcHJvZmlsZSJdfX0sInNjb3BlIjoicHJvZmlsZSBlbWFpbCIsImNsaWVudEhvc3QiOiIxMC4yNDQuNS4xNDgiLCJlbWFpbF92ZXJpZmllZCI6ZmFsc2UsImNsaWVudElkIjoibW9zaXAtcmVzaWRlbnQtY2xpZW50IiwicHJlZmVycmVkX3VzZXJuYW1lIjoic2VydmljZS1hY2NvdW50LW1vc2lwLXJlc2lkZW50LWNsaWVudCIsImNsaWVudEFkZHJlc3MiOiIxMC4yNDQuNS4xNDgifQ.xZq1m3mBTEvFDENKFOI59QsSl3sd_TSDNbhTAOq4x_x_4voPc4hh08gIxUdsVHfXY4T0P8DdZ1xNt8xd1VWc33Hc4b_3kK7ksGY4wwqtb0-pDLQGajCGuG6vebC1rYcjsGRbJ1Gnrj_F2RNY4Ky6Nq5SAJ1Lh_NVKNKFghAXb3YrlmqlmCB1fCltC4XBqNnF5_k4uzLCu_Wr0lt_M87X97DktaRGLOD2_HY1Ire9YPsWkoO8y7X_DRCY59yQDVgYs2nAiR6Am-c55Q0fEQ0HuB4IJHlhtMHm27dXPdOEhFhR8ZPOyeO6ZIcIm0ZTDjusrruqWy2_yO5fe3XIHkCOAw");
		mosipUserTokenDto.setExpTime(3000);
		MosipUserDto mosipUserDto = new MosipUserDto();
		mosipUserDto.setUserId("mock-user");
		mosipUserDto.setMail("mock-user@mosip.io");
		mosipUserDto.setMobile("9999999999");
		mosipUserDto.setRole("MOCK-ROLE");
		
		mosipUserTokenDto.setMosipUserDto(mosipUserDto);
		when(authService.validateToken(Mockito.any()))
						.thenReturn(mosipUserTokenDto);
		

		Cookie cookie = new Cookie("a",
				"eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJzNmYxcDYwYWVDTTBrNy1NaW9sN0Zib2FTdXlRYm95UC03S1RUTmVWLWZNIn0.eyJqdGkiOiJmYTU4Y2NjMC00ZDRiLTQ2ZjAtYjgwOC0yMWI4ZTdhNmMxNDMiLCJleHAiOjE2NDAxODc3MTksIm5iZiI6MCwiaWF0IjoxNjQwMTUxNzE5LCJpc3MiOiJodHRwczovL2Rldi5tb3NpcC5uZXQva2V5Y2xvYWsvYXV0aC9yZWFsbXMvbW9zaXAiLCJhdWQiOiJhY2NvdW50Iiwic3ViIjoiOWRiZTE0MDEtNTQ1NC00OTlhLTlhMWItNzVhZTY4M2Q0MjZhIiwidHlwIjoiQmVhcmVyIiwiYXpwIjoibW9zaXAtcmVzaWRlbnQtY2xpZW50IiwiYXV0aF90aW1lIjowLCJzZXNzaW9uX3N0YXRlIjoiY2QwYjU5NjEtOTYzMi00NmE0LWIzMzgtODc4MWEzNDVmMTZiIiwiYWNyIjoiMSIsImFsbG93ZWQtb3JpZ2lucyI6WyJodHRwczovL2Rldi5tb3NpcC5uZXQiXSwicmVhbG1fYWNjZXNzIjp7InJvbGVzIjpbIkNSRURFTlRJQUxfUkVRVUVTVCIsIlJFU0lERU5UIiwib2ZmbGluZV9hY2Nlc3MiLCJQQVJUTkVSX0FETUlOIiwidW1hX2F1dGhvcml6YXRpb24iXX0sInJlc291cmNlX2FjY2VzcyI6eyJtb3NpcC1yZXNpZGVudC1jbGllbnQiOnsicm9sZXMiOlsidW1hX3Byb3RlY3Rpb24iXX0sImFjY291bnQiOnsicm9sZXMiOlsibWFuYWdlLWFjY291bnQiLCJtYW5hZ2UtYWNjb3VudC1saW5rcyIsInZpZXctcHJvZmlsZSJdfX0sInNjb3BlIjoicHJvZmlsZSBlbWFpbCIsImNsaWVudEhvc3QiOiIxMC4yNDQuNS4xNDgiLCJlbWFpbF92ZXJpZmllZCI6ZmFsc2UsImNsaWVudElkIjoibW9zaXAtcmVzaWRlbnQtY2xpZW50IiwicHJlZmVycmVkX3VzZXJuYW1lIjoic2VydmljZS1hY2NvdW50LW1vc2lwLXJlc2lkZW50LWNsaWVudCIsImNsaWVudEFkZHJlc3MiOiIxMC4yNDQuNS4xNDgifQ.xZq1m3mBTEvFDENKFOI59QsSl3sd_TSDNbhTAOq4x_x_4voPc4hh08gIxUdsVHfXY4T0P8DdZ1xNt8xd1VWc33Hc4b_3kK7ksGY4wwqtb0-pDLQGajCGuG6vebC1rYcjsGRbJ1Gnrj_F2RNY4Ky6Nq5SAJ1Lh_NVKNKFghAXb3YrlmqlmCB1fCltC4XBqNnF5_k4uzLCu_Wr0lt_M87X97DktaRGLOD2_HY1Ire9YPsWkoO8y7X_DRCY59yQDVgYs2nAiR6Am-c55Q0fEQ0HuB4IJHlhtMHm27dXPdOEhFhR8ZPOyeO6ZIcIm0ZTDjusrruqWy2_yO5fe3XIHkCOAw");
		mockMvc.perform(post("/authorize/validateToken").contentType(MediaType.APPLICATION_JSON).cookie(cookie))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.errors[0].errorCode", is("KER-ATH-007")));
	
	}

	@Test
	/**
	 * Asserts validateToken error path when no Authorization cookie is sent.
	 *
	 * @throws Exception if the HTTP call fails
	 */
	public void getValidateTokenNullCookieTest() throws Exception {

		// resp
		MosipUserTokenDto mosipUserTokenDto = new MosipUserTokenDto();
		mosipUserTokenDto.setToken("eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJzNmYxcDYwYWVDTTBrNy1NaW9sN0Zib2FTdXlRYm95UC03S1RUTmVWLWZNIn0.eyJqdGkiOiJmYTU4Y2NjMC00ZDRiLTQ2ZjAtYjgwOC0yMWI4ZTdhNmMxNDMiLCJleHAiOjE2NDAxODc3MTksIm5iZiI6MCwiaWF0IjoxNjQwMTUxNzE5LCJpc3MiOiJodHRwczovL2Rldi5tb3NpcC5uZXQva2V5Y2xvYWsvYXV0aC9yZWFsbXMvbW9zaXAiLCJhdWQiOiJhY2NvdW50Iiwic3ViIjoiOWRiZTE0MDEtNTQ1NC00OTlhLTlhMWItNzVhZTY4M2Q0MjZhIiwidHlwIjoiQmVhcmVyIiwiYXpwIjoibW9zaXAtcmVzaWRlbnQtY2xpZW50IiwiYXV0aF90aW1lIjowLCJzZXNzaW9uX3N0YXRlIjoiY2QwYjU5NjEtOTYzMi00NmE0LWIzMzgtODc4MWEzNDVmMTZiIiwiYWNyIjoiMSIsImFsbG93ZWQtb3JpZ2lucyI6WyJodHRwczovL2Rldi5tb3NpcC5uZXQiXSwicmVhbG1fYWNjZXNzIjp7InJvbGVzIjpbIkNSRURFTlRJQUxfUkVRVUVTVCIsIlJFU0lERU5UIiwib2ZmbGluZV9hY2Nlc3MiLCJQQVJUTkVSX0FETUlOIiwidW1hX2F1dGhvcml6YXRpb24iXX0sInJlc291cmNlX2FjY2VzcyI6eyJtb3NpcC1yZXNpZGVudC1jbGllbnQiOnsicm9sZXMiOlsidW1hX3Byb3RlY3Rpb24iXX0sImFjY291bnQiOnsicm9sZXMiOlsibWFuYWdlLWFjY291bnQiLCJtYW5hZ2UtYWNjb3VudC1saW5rcyIsInZpZXctcHJvZmlsZSJdfX0sInNjb3BlIjoicHJvZmlsZSBlbWFpbCIsImNsaWVudEhvc3QiOiIxMC4yNDQuNS4xNDgiLCJlbWFpbF92ZXJpZmllZCI6ZmFsc2UsImNsaWVudElkIjoibW9zaXAtcmVzaWRlbnQtY2xpZW50IiwicHJlZmVycmVkX3VzZXJuYW1lIjoic2VydmljZS1hY2NvdW50LW1vc2lwLXJlc2lkZW50LWNsaWVudCIsImNsaWVudEFkZHJlc3MiOiIxMC4yNDQuNS4xNDgifQ.xZq1m3mBTEvFDENKFOI59QsSl3sd_TSDNbhTAOq4x_x_4voPc4hh08gIxUdsVHfXY4T0P8DdZ1xNt8xd1VWc33Hc4b_3kK7ksGY4wwqtb0-pDLQGajCGuG6vebC1rYcjsGRbJ1Gnrj_F2RNY4Ky6Nq5SAJ1Lh_NVKNKFghAXb3YrlmqlmCB1fCltC4XBqNnF5_k4uzLCu_Wr0lt_M87X97DktaRGLOD2_HY1Ire9YPsWkoO8y7X_DRCY59yQDVgYs2nAiR6Am-c55Q0fEQ0HuB4IJHlhtMHm27dXPdOEhFhR8ZPOyeO6ZIcIm0ZTDjusrruqWy2_yO5fe3XIHkCOAw");
		mosipUserTokenDto.setExpTime(3000);
		MosipUserDto mosipUserDto = new MosipUserDto();
		mosipUserDto.setUserId("mock-user");
		mosipUserDto.setMail("mock-user@mosip.io");
		mosipUserDto.setMobile("9999999999");
		mosipUserDto.setRole("MOCK-ROLE");
		
		mosipUserTokenDto.setMosipUserDto(mosipUserDto);
		when(authService.validateToken(Mockito.any()))
						.thenReturn(mosipUserTokenDto);
		

		Cookie cookie = new Cookie("Authorization",
				"eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJzNmYxcDYwYWVDTTBrNy1NaW9sN0Zib2FTdXlRYm95UC03S1RUTmVWLWZNIn0.eyJqdGkiOiJmYTU4Y2NjMC00ZDRiLTQ2ZjAtYjgwOC0yMWI4ZTdhNmMxNDMiLCJleHAiOjE2NDAxODc3MTksIm5iZiI6MCwiaWF0IjoxNjQwMTUxNzE5LCJpc3MiOiJodHRwczovL2Rldi5tb3NpcC5uZXQva2V5Y2xvYWsvYXV0aC9yZWFsbXMvbW9zaXAiLCJhdWQiOiJhY2NvdW50Iiwic3ViIjoiOWRiZTE0MDEtNTQ1NC00OTlhLTlhMWItNzVhZTY4M2Q0MjZhIiwidHlwIjoiQmVhcmVyIiwiYXpwIjoibW9zaXAtcmVzaWRlbnQtY2xpZW50IiwiYXV0aF90aW1lIjowLCJzZXNzaW9uX3N0YXRlIjoiY2QwYjU5NjEtOTYzMi00NmE0LWIzMzgtODc4MWEzNDVmMTZiIiwiYWNyIjoiMSIsImFsbG93ZWQtb3JpZ2lucyI6WyJodHRwczovL2Rldi5tb3NpcC5uZXQiXSwicmVhbG1fYWNjZXNzIjp7InJvbGVzIjpbIkNSRURFTlRJQUxfUkVRVUVTVCIsIlJFU0lERU5UIiwib2ZmbGluZV9hY2Nlc3MiLCJQQVJUTkVSX0FETUlOIiwidW1hX2F1dGhvcml6YXRpb24iXX0sInJlc291cmNlX2FjY2VzcyI6eyJtb3NpcC1yZXNpZGVudC1jbGllbnQiOnsicm9sZXMiOlsidW1hX3Byb3RlY3Rpb24iXX0sImFjY291bnQiOnsicm9sZXMiOlsibWFuYWdlLWFjY291bnQiLCJtYW5hZ2UtYWNjb3VudC1saW5rcyIsInZpZXctcHJvZmlsZSJdfX0sInNjb3BlIjoicHJvZmlsZSBlbWFpbCIsImNsaWVudEhvc3QiOiIxMC4yNDQuNS4xNDgiLCJlbWFpbF92ZXJpZmllZCI6ZmFsc2UsImNsaWVudElkIjoibW9zaXAtcmVzaWRlbnQtY2xpZW50IiwicHJlZmVycmVkX3VzZXJuYW1lIjoic2VydmljZS1hY2NvdW50LW1vc2lwLXJlc2lkZW50LWNsaWVudCIsImNsaWVudEFkZHJlc3MiOiIxMC4yNDQuNS4xNDgifQ.xZq1m3mBTEvFDENKFOI59QsSl3sd_TSDNbhTAOq4x_x_4voPc4hh08gIxUdsVHfXY4T0P8DdZ1xNt8xd1VWc33Hc4b_3kK7ksGY4wwqtb0-pDLQGajCGuG6vebC1rYcjsGRbJ1Gnrj_F2RNY4Ky6Nq5SAJ1Lh_NVKNKFghAXb3YrlmqlmCB1fCltC4XBqNnF5_k4uzLCu_Wr0lt_M87X97DktaRGLOD2_HY1Ire9YPsWkoO8y7X_DRCY59yQDVgYs2nAiR6Am-c55Q0fEQ0HuB4IJHlhtMHm27dXPdOEhFhR8ZPOyeO6ZIcIm0ZTDjusrruqWy2_yO5fe3XIHkCOAw");
		mockMvc.perform(post("/authorize/validateToken").contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.errors[0].errorCode", is("KER-ATH-006")));
	
	}

	@Test
	/**
	 * Asserts send-OTP HTTP endpoint returns success from the mocked AuthService.
	 *
	 * @throws Exception if the HTTP call fails
	 */
	public void getDetailsForValidateOtpTest() throws Exception {

		// request
		RequestWrapper<UserOtp> userOtpDto = new RequestWrapper<UserOtp>();
		UserOtp userOtp = new UserOtp();
		userOtp.setAppId("ida");
		userOtp.setOtp("717171");
		userOtp.setUserId("112211");
		userOtpDto.setRequest(userOtp);

		// resp
		AuthNResponseDto authResponseDto = new AuthNResponseDto();
		authResponseDto.setToken("mock-token");
		authResponseDto.setStatus("SUCCESS");
		authResponseDto.setMessage("SUCCESS");
		when(authService.authenticateUserWithOtp(Mockito.any())).thenReturn(authResponseDto);
		mockMvc.perform(post("/authenticate/useridOTP").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(userOtpDto))).andExpect(status().isOk())
				.andExpect(jsonPath("$.response.status", is("SUCCESS")));
	}

	@Test
	/**
	 * Asserts client-id/secret authenticate HTTP endpoint returns a token cookie.
	 *
	 * @throws Exception if the HTTP call fails
	 */
	public void clientIdSecretKeyTest() throws Exception {

		// request
		RequestWrapper<ClientSecret> req = new RequestWrapper<>();
		ClientSecret clientSecret = new ClientSecret("ida-client", "secret", "ida");
		req.setRequest(clientSecret);
		// resp
		AuthNResponseDto authResponseDto = new AuthNResponseDto();
		authResponseDto.setToken("mock-token");
		authResponseDto.setStatus("SUCCESS");
		authResponseDto.setMessage("SUCCESS");
		when(authService.authenticateWithSecretKey(Mockito.any())).thenReturn(authResponseDto);
		mockMvc.perform(post("/authenticate/clientidsecretkey").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(req))).andExpect(status().isOk())
				.andExpect(jsonPath("$.response.status", is("SUCCESS")));
	}

	@Test
	/**
	 * Asserts logout/invalidate HTTP endpoint returns success.
	 *
	 * @throws Exception if the HTTP call fails
	 */
	public void invalidateTokenTest() throws Exception {
		// request
		RequestWrapper<ClientSecret> req = new RequestWrapper<>();
		ClientSecret clientSecret = new ClientSecret("ida-client", "secret", "ida");
		req.setRequest(clientSecret);
		// resp
		AuthNResponse authResponseDto = new AuthNResponse();
		authResponseDto.setStatus("SUCCESS");
		authResponseDto.setMessage("SUCCESS");
		when(authService.invalidateToken(Mockito.any())).thenReturn(authResponseDto);
		Cookie cookie = new Cookie("Authorization",
				"eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJzNmYxcDYwYWVDTTBrNy1NaW9sN0Zib2FTdXlRYm95UC03S1RUTmVWLWZNIn0.eyJqdGkiOiJmYTU4Y2NjMC00ZDRiLTQ2ZjAtYjgwOC0yMWI4ZTdhNmMxNDMiLCJleHAiOjE2NDAxODc3MTksIm5iZiI6MCwiaWF0IjoxNjQwMTUxNzE5LCJpc3MiOiJodHRwczovL2Rldi5tb3NpcC5uZXQva2V5Y2xvYWsvYXV0aC9yZWFsbXMvbW9zaXAiLCJhdWQiOiJhY2NvdW50Iiwic3ViIjoiOWRiZTE0MDEtNTQ1NC00OTlhLTlhMWItNzVhZTY4M2Q0MjZhIiwidHlwIjoiQmVhcmVyIiwiYXpwIjoibW9zaXAtcmVzaWRlbnQtY2xpZW50IiwiYXV0aF90aW1lIjowLCJzZXNzaW9uX3N0YXRlIjoiY2QwYjU5NjEtOTYzMi00NmE0LWIzMzgtODc4MWEzNDVmMTZiIiwiYWNyIjoiMSIsImFsbG93ZWQtb3JpZ2lucyI6WyJodHRwczovL2Rldi5tb3NpcC5uZXQiXSwicmVhbG1fYWNjZXNzIjp7InJvbGVzIjpbIkNSRURFTlRJQUxfUkVRVUVTVCIsIlJFU0lERU5UIiwib2ZmbGluZV9hY2Nlc3MiLCJQQVJUTkVSX0FETUlOIiwidW1hX2F1dGhvcml6YXRpb24iXX0sInJlc291cmNlX2FjY2VzcyI6eyJtb3NpcC1yZXNpZGVudC1jbGllbnQiOnsicm9sZXMiOlsidW1hX3Byb3RlY3Rpb24iXX0sImFjY291bnQiOnsicm9sZXMiOlsibWFuYWdlLWFjY291bnQiLCJtYW5hZ2UtYWNjb3VudC1saW5rcyIsInZpZXctcHJvZmlsZSJdfX0sInNjb3BlIjoicHJvZmlsZSBlbWFpbCIsImNsaWVudEhvc3QiOiIxMC4yNDQuNS4xNDgiLCJlbWFpbF92ZXJpZmllZCI6ZmFsc2UsImNsaWVudElkIjoibW9zaXAtcmVzaWRlbnQtY2xpZW50IiwicHJlZmVycmVkX3VzZXJuYW1lIjoic2VydmljZS1hY2NvdW50LW1vc2lwLXJlc2lkZW50LWNsaWVudCIsImNsaWVudEFkZHJlc3MiOiIxMC4yNDQuNS4xNDgifQ.xZq1m3mBTEvFDENKFOI59QsSl3sd_TSDNbhTAOq4x_x_4voPc4hh08gIxUdsVHfXY4T0P8DdZ1xNt8xd1VWc33Hc4b_3kK7ksGY4wwqtb0-pDLQGajCGuG6vebC1rYcjsGRbJ1Gnrj_F2RNY4Ky6Nq5SAJ1Lh_NVKNKFghAXb3YrlmqlmCB1fCltC4XBqNnF5_k4uzLCu_Wr0lt_M87X97DktaRGLOD2_HY1Ire9YPsWkoO8y7X_DRCY59yQDVgYs2nAiR6Am-c55Q0fEQ0HuB4IJHlhtMHm27dXPdOEhFhR8ZPOyeO6ZIcIm0ZTDjusrruqWy2_yO5fe3XIHkCOAw");
		mockMvc.perform(post("/authorize/invalidateToken").contentType(MediaType.APPLICATION_JSON).cookie(cookie))
				.andExpect(status().isOk()).andExpect(jsonPath("$.response.status", is("SUCCESS")));
	}

	@Test
	/**
	 * Asserts GET roles HTTP endpoint returns the mocked role list.
	 *
	 * @throws Exception if the HTTP call fails
	 */
	public void getAllRolesTest() throws Exception {

		RolesListDto rolesListDto = new RolesListDto();
		Role role = new Role();
		role.setRoleId("123");
		role.setRoleName("processor");
		List<Role> roles = new ArrayList<>();
		roles.add(role);
		rolesListDto.setRoles(roles);
		when(authService.getAllRoles(Mockito.any())).thenReturn(rolesListDto);
		mockMvc.perform(get("/roles/ida").contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				.andExpect(jsonPath("$.response.roles[0].roleId", is("123")));
	}

	@Test
	/**
	 * Asserts user-details HTTP endpoint returns the mocked user list.
	 *
	 * @throws Exception if the HTTP call fails
	 */
	public void getListOfUsersDetailsTest() throws Exception {

		// resp
		MosipUserListDto mosipUserListDto = new MosipUserListDto();
		MosipUserDto mosipUserDto = new MosipUserDto();
		mosipUserDto.setUserId("mock-user");
		mosipUserDto.setMail("mock-user@mosip.io");
		mosipUserDto.setMobile("9999999999");
		mosipUserDto.setRole("MOCK-ROLE");
		List<MosipUserDto> list = new ArrayList<>();
		list.add(mosipUserDto);
		mosipUserListDto.setMosipUserDtoList(list);

		// req
		RequestWrapper<UserDetailsRequestDto> userDetails = new RequestWrapper<UserDetailsRequestDto>();
		UserDetailsRequestDto userDetailsRequestDto = new UserDetailsRequestDto();
		List<String> userd = new ArrayList<>();
		userd.add("userdetails1");
		userDetailsRequestDto.setUserDetails(userd);
		userDetails.setRequest(userDetailsRequestDto);
		when(authService.getListOfUsersDetails(Mockito.any(), Mockito.any())).thenReturn(mosipUserListDto);
		mockMvc.perform(post("/userdetails/ida").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(userDetails))).andExpect(status().isOk())
				.andExpect(jsonPath("$.response.mosipUserDtoList[0].userId", is(mosipUserDto.getUserId())));
	}

	@Test
	/**
	 * Asserts salted user-details HTTP endpoint returns the mocked salts.
	 *
	 * @throws Exception if the HTTP call fails
	 */
	public void getUserDetailsWithSaltTest() throws Exception {

		// resp
		MosipUserSaltListDto mosipUserListDto = new MosipUserSaltListDto();
		MosipUserSalt mosipUserDto = new MosipUserSalt();
		mosipUserDto.setUserId("mock-user");
		List<MosipUserSalt> list = new ArrayList<>();
		list.add(mosipUserDto);
		mosipUserListDto.setMosipUserSaltList(list);

		// req
		RequestWrapper<UserDetailsRequestDto> userDetails = new RequestWrapper<UserDetailsRequestDto>();
		UserDetailsRequestDto userDetailsRequestDto = new UserDetailsRequestDto();
		List<String> userd = new ArrayList<>();
		userd.add("userdetails1");
		userDetailsRequestDto.setUserDetails(userd);
		userDetails.setRequest(userDetailsRequestDto);
		when(authService.getAllUserDetailsWithSalt(Mockito.any(), Mockito.any())).thenReturn(mosipUserListDto);
		mockMvc.perform(post("/usersaltdetails/ida").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(userDetails))).andExpect(status().isOk())
				.andExpect(jsonPath("$.response.mosipUserSaltList[0].userId", is(mosipUserDto.getUserId())));
	}

	@Test
	/**
	 * Asserts RID lookup HTTP endpoint returns the mocked RID.
	 *
	 * @throws Exception if the HTTP call fails
	 */
	public void getRIdTest() throws Exception {
		// resp
		RIdDto rIdDto = new RIdDto();
		rIdDto.setRId("mock-rid");
		when(authService.getRidBasedOnUid(Mockito.any(), Mockito.any())).thenReturn(rIdDto);
		mockMvc.perform(get("/rid/ida/10022").contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				.andExpect(jsonPath("$.response.rId", is(rIdDto.getRId())));
	}

	/*
	 * @Test public void getUserNameTest() throws Exception {
	 * 
	 * // resp AuthZResponseDto authZResponseDto = new AuthZResponseDto();
	 * authZResponseDto.setMessage("success");
	 * authZResponseDto.setStatus("success");
	 * 
	 * when(authService.unBlockUser(Mockito.any(),
	 * Mockito.any())).thenReturn(authZResponseDto);
	 * ResponseWrapper<AuthZResponseDto> resp =
	 * authController.getUserName("8172818291", "ida");
	 * assertThat(resp.getResponse().getStatus(), is(authZResponseDto.getStatus()));
	 * }
	 * 
	 * @Test public void changePasswordTest() throws Exception {
	 * 
	 * // resp AuthZResponseDto authZResponseDto = new AuthZResponseDto();
	 * authZResponseDto.setMessage("success");
	 * authZResponseDto.setStatus("success");
	 * 
	 * // req RequestWrapper<PasswordDto> req = new RequestWrapper<PasswordDto>();
	 * PasswordDto passwordDto = new PasswordDto(); passwordDto.setUserId("123");
	 * passwordDto.setNewPassword("Mosip@1282#");
	 * passwordDto.setOldPassword("Mosip@21021#"); req.setRequest(passwordDto);
	 * when(authService.changePassword(Mockito.any(),
	 * Mockito.any())).thenReturn(authZResponseDto);
	 * mockMvc.perform(post("/changepassword/ida").contentType(MediaType.
	 * APPLICATION_JSON)
	 * .content(objectMapper.writeValueAsString(req))).andExpect(status().isOk())
	 * .andExpect(jsonPath("$.response.status", is(authZResponseDto.getStatus())));
	 * }
	 * 
	 * @Test public void resetPasswordTest() throws Exception {
	 * 
	 * // resp AuthZResponseDto authZResponseDto = new AuthZResponseDto();
	 * authZResponseDto.setMessage("success");
	 * authZResponseDto.setStatus("success");
	 * 
	 * // req RequestWrapper<PasswordDto> req = new RequestWrapper<PasswordDto>();
	 * PasswordDto passwordDto = new PasswordDto(); passwordDto.setUserId("123");
	 * passwordDto.setNewPassword("Mosip@1282#");
	 * passwordDto.setOldPassword("Mosip@21021#"); req.setRequest(passwordDto);
	 * when(authService.resetPassword(Mockito.any(),
	 * Mockito.any())).thenReturn(authZResponseDto);
	 * mockMvc.perform(post("/resetpassword/ida").contentType(MediaType.
	 * APPLICATION_JSON)
	 * .content(objectMapper.writeValueAsString(req))).andExpect(status().isOk())
	 * .andExpect(jsonPath("$.response.status", is(authZResponseDto.getStatus())));
	 * }
	 * 
	 * @Test public void getUsernameBasedOnMobileNumberTest() throws Exception {
	 * 
	 * // resp UserNameDto userNameDto = new UserNameDto();
	 * userNameDto.setUserName("mock-user");
	 * 
	 * when(authService.getUserNameBasedOnMobileNumber(Mockito.any(),
	 * Mockito.any())).thenReturn(userNameDto); ResponseWrapper<UserNameDto> resp =
	 * authController.getUsernameBasedOnMobileNumber("8172818291", "ida");
	 * assertThat(resp.getResponse().getUserName(), is(userNameDto.getUserName()));
	 * }
	 * 
	 * @Test public void addPasswordTest() throws Exception {
	 * 
	 * // resp UserPasswordResponseDto userPasswordResponseDto = new
	 * UserPasswordResponseDto(); userPasswordResponseDto.setUserName("mock-user");
	 * 
	 * // req RequestWrapper<UserPasswordRequestDto> req = new
	 * RequestWrapper<UserPasswordRequestDto>(); UserPasswordRequestDto userNameDto
	 * = new UserPasswordRequestDto(); userNameDto.setUserName("mock-user");
	 * userNameDto.setAppId("ida"); userNameDto.setPassword("mock-pass");
	 * userNameDto.setRid("29382938"); req.setRequest(userNameDto);
	 * when(authService.addUserPassword(Mockito.any())).thenReturn(
	 * userPasswordResponseDto);
	 * mockMvc.perform(post("/user/addpassword").contentType(MediaType.
	 * APPLICATION_JSON)
	 * .content(objectMapper.writeValueAsString(req))).andExpect(status().isOk())
	 * .andExpect(jsonPath("$.response.userName", is(userNameDto.getUserName()))); }
	 * 
	 * @Test public void getUserRoleTest() throws Exception {
	 * 
	 * // resp UserRoleDto mosipUserDto = new UserRoleDto();
	 * mosipUserDto.setUserId("mock-user"); mosipUserDto.setRole("MOCK-ROLE");
	 * 
	 * when(authService.getUserRole(Mockito.any(),
	 * Mockito.any())).thenReturn(mosipUserDto); ResponseWrapper<UserRoleDto> resp =
	 * authController.getUserRole("ida", "110022");
	 * assertThat(resp.getResponse().getUserId(), is(mosipUserDto.getUserId())); }
	 * 
	 * @Test public void getUserDetailBasedOnMobileNumberTest() throws Exception {
	 * 
	 * // resp MosipUserDto mosipUserDto = new MosipUserDto();
	 * mosipUserDto.setUserId("mock-user");
	 * mosipUserDto.setMail("mock-user@mosip.io");
	 * mosipUserDto.setMobile("9999999999"); mosipUserDto.setRole("MOCK-ROLE");
	 * 
	 * when(authService.getUserDetailBasedonMobileNumber(Mockito.any(),
	 * Mockito.any())).thenReturn(mosipUserDto); ResponseWrapper<MosipUserDto> resp
	 * = authController.getUserDetailBasedOnMobileNumber("9283929392", "ida");
	 * assertThat(resp.getResponse().getUserId(), is(mosipUserDto.getUserId())); }
	 * 
	 * @Test public void validateUserNameTest() throws Exception {
	 * 
	 * // resp ValidationResponseDto validationResponseDto = new
	 * ValidationResponseDto(); validationResponseDto.setStatus("success");
	 * 
	 * when(authService.validateUserName(Mockito.any(),
	 * Mockito.any())).thenReturn(validationResponseDto);
	 * ResponseWrapper<ValidationResponseDto> resp =
	 * authController.validateUserName("10011", "ida");
	 * assertThat(resp.getResponse().getStatus(),
	 * is(validationResponseDto.getStatus())); }
	 * 
	 * @Test public void getUserDetailBasedOnUserIdTest() throws Exception {
	 * 
	 * // resp UserDetailsResponseDto resp = new UserDetailsResponseDto();
	 * UserDetailsDto userDetailsDto = new UserDetailsDto();
	 * userDetailsDto.setUserId("mock-user"); List<UserDetailsDto> userDetailsDtos =
	 * new ArrayList<UserDetailsDto>(); userDetailsDtos.add(userDetailsDto);
	 * resp.setUserDetails(userDetailsDtos);
	 * 
	 * // req RequestWrapper<UserDetailsRequestDto> req = new
	 * RequestWrapper<UserDetailsRequestDto>(); List<String> userids = new
	 * ArrayList<String>(); userids.add("mock-user"); UserDetailsRequestDto
	 * userNameDto = new UserDetailsRequestDto();
	 * userNameDto.setUserDetails(userids); req.setRequest(userNameDto);
	 * when(authService.getUserDetailBasedOnUserId(Mockito.any(),
	 * Mockito.any())).thenReturn(resp);
	 * mockMvc.perform(post("/userdetail/regid/ida").contentType(MediaType.
	 * APPLICATION_JSON)
	 * .content(objectMapper.writeValueAsString(req))).andExpect(status().isOk())
	 * .andExpect(jsonPath("$.response.userDetails[0].userId",
	 * is(resp.getUserDetails().get(0).getUserId()))); }
	 */

	/*
	 * @Test public void loginTest() throws Exception { //
	 * http://localhost:8080/keycloak/auth/realms/mosip/protocol/openid-connect/auth
	 * ?client_id=mosip-admin-client&redirect_uri=http://localhost:8082/v1/admin/
	 * login-redirect/abc&state=mock-state&response_type=code&scope=cls Cookie
	 * cookie = new Cookie("state", "mockstate");
	 * when(authService.getKeycloakURI(Mockito.any(),
	 * Mockito.any())).thenReturn("uri");
	 * mockMvc.perform(get("/login/abc").contentType(MediaType.APPLICATION_JSON).
	 * cookie(cookie)) .andExpect(status().is3xxRedirection()); }
	 * 
	 * @Test public void loginRedirectTest() throws Exception {
	 * AccessTokenResponseDTO accessTokenResponse = new AccessTokenResponseDTO();
	 * accessTokenResponse.setAccessToken("mock-access-token");
	 * accessTokenResponse.setExpiresIn("111"); Cookie cookie = new Cookie("state",
	 * "mockstate"); when(authService.loginRedirect(Mockito.any(), Mockito.any(),
	 * Mockito.any(), Mockito.any(), Mockito.any()))
	 * .thenReturn(accessTokenResponse); mockMvc.perform(get(
	 * "/login-redirect/aHR0cDovL2xvY2FsaG9zdDo1MDAwLw==?state=mockstate&session_state=mock-session-state&code=mockcode")
	 * .contentType(MediaType.APPLICATION_JSON).cookie(cookie)).andExpect(status().
	 * is3xxRedirection()); }
	 * 
	 * @Test public void loginRedirectExceptionTest() throws Exception {
	 * 
	 * AccessTokenResponseDTO accessTokenResponse = new AccessTokenResponseDTO();
	 * accessTokenResponse.setAccessToken("mock-access-token");
	 * accessTokenResponse.setExpiresIn("111"); Cookie cookie = new Cookie("state",
	 * "mockstate"); when(authService.loginRedirect(Mockito.any(), Mockito.any(),
	 * Mockito.any(), Mockito.any(), Mockito.any()))
	 * .thenReturn(accessTokenResponse); mockMvc.perform(get(
	 * "/login-redirect/aHR0cDovL2FiOjUwMDAv?state=mockstate&session_state=mock-session-state&code=mockcode")
	 * .contentType(MediaType.APPLICATION_JSON).cookie(cookie)).andExpect(status().
	 * is2xxSuccessful()).andExpect(jsonPath("$.errors[0].errorCode",
	 * is(AuthErrorCode.DOMAIN_EXCEPTION.getErrorCode())));
	 * 
	 * }
	 */
	@Test
	/**
	 * Asserts individual-id lookup HTTP endpoint returns the mocked id.
	 *
	 * @throws Exception if the HTTP call fails
	 */
	public void getIndividualIdTest() throws Exception {

		// resp

		IndividualIdDto respo = new IndividualIdDto();
		respo.setIndividualId("12331");
		String userid = "mock-user";
		when(authService.getIndividualIdBasedOnUserID(Mockito.any(), Mockito.any())).thenReturn(respo);
		mockMvc.perform(get("/individualId/ida/110022").contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andExpect(jsonPath("$.response.individualId", is(respo.getIndividualId())));
	}

	@Test
	/**
	 * Asserts users-details HTTP endpoint returns the mocked user details.
	 *
	 * @throws Exception if the HTTP call fails
	 */
	public void getUsersDetailsTest() throws Exception {

		// resp
		MosipUserListDto mosipUserListDto = new MosipUserListDto();
		MosipUserDto mosipUserDto = new MosipUserDto();
		mosipUserDto.setUserId("mock-user");
		mosipUserDto.setMail("mock-user@mosip.io");
		mosipUserDto.setMobile("9999999999");
		mosipUserDto.setRole("MOCK-ROLE");
		List<MosipUserDto> list = new ArrayList<>();
		list.add(mosipUserDto);
		mosipUserListDto.setMosipUserDtoList(list);
		when(authService.getListOfUsersDetails(Mockito.any(), Mockito.any(), Mockito.eq(0), Mockito.eq(10),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
						.thenReturn(mosipUserListDto);
		ResponseWrapper<MosipUserListDto> responseWrapper = authController.getUsersDetails("ida", "mock-roleName", 0,
				10, "mock-email", "mock-firstName", "mock-lastName", "mock-username", "userID");
		assertThat(responseWrapper.getResponse().getMosipUserDtoList().get(0).getUserId(),
				is(mosipUserListDto.getMosipUserDtoList().get(0).getUserId()));
	}
}
