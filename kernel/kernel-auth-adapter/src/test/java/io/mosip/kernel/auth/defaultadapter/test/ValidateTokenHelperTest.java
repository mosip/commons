package io.mosip.kernel.auth.defaultadapter.test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import java.nio.charset.Charset;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.auth0.jwt.interfaces.DecodedJWT;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.RequestBodyUriSpec;
import org.springframework.web.reactive.function.client.WebClient.ResponseSpec;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.kernel.auth.defaultadapter.constant.AuthAdapterConstant;
import io.mosip.kernel.auth.defaultadapter.constant.AuthAdapterErrorCode;
import io.mosip.kernel.auth.defaultadapter.exception.AuthManagerException;
import io.mosip.kernel.auth.defaultadapter.helper.ValidateTokenHelper;
import io.mosip.kernel.openid.bridge.model.MosipUserDto;
import reactor.core.publisher.Mono;

/**
 * Tests {@link ValidateTokenHelper} online userinfo validation (RestTemplate and
 * WebClient), unsigned-token offline rejection, RSA algorithm variants, expiry
 * / issuer / signature / audience failures, and JWKS public-key lookup.
 */
@SpringBootTest(classes = { AuthTestBootApplication.class })
@RunWith(SpringRunner.class)
public class ValidateTokenHelperTest {

	/** JSON mapper from the test context. */
	@Autowired
	private ObjectMapper mapper;

	/** OIDC JWKS path from test properties. */
	@Value("${auth.server.admin.oidc.certs.path:/protocol/openid-connect/certs}")
	private String certsPath;

	/** OIDC userinfo path from test properties. */
	@Value("${auth.server.admin.oidc.userinfo.path:/protocol/openid-connect/userinfo}")
	private String userInfo;

	/** Whether issuer domain is validated. */
	@Value("${auth.server.admin.issuer.domain.validate:true}")
	private boolean validateIssuerDomain;

	/** Public IAM issuer URI. */
	@Value("${auth.server.admin.issuer.uri:}")
	private String issuerURI;

	/** Internal IAM issuer URI used to build the userinfo URL. */
	@Value("${auth.server.admin.issuer.internal.uri:}")
	private String issuerInternalURI;

	/** Whether audience claim validation is enabled. */
	@Value("${auth.server.admin.audience.claim.validate:true}")
	private boolean validateAudClaim;

	/** App-id to Keycloak realm mapping from test properties. */
	@Value("#{${mosip.kernel.auth.appids.realm.map}}")
	private Map<String, String> realmMap;

	/** Token validation helper under test. */
	@Autowired
	private ValidateTokenHelper validateTokenHelper;

	/** Mock RestTemplate for userinfo HTTP calls. */
	private RestTemplate restTemplate = Mockito.mock(RestTemplate.class);
	/** Mock WebClient for reactive userinfo HTTP calls. */
	private WebClient webClient = Mockito.mock(WebClient.class);

	/**
	 * Asserts RestTemplate userinfo HTTP 403 yields a pair with a {@code null}
	 * user.
	 *
	 * @throws Exception if the helper call fails
	 */
	@Test
	public void doOnlineTokenValidationTest() throws Exception {
		String token = "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJzNmYxcDYwYWVDTTBrNy1NaW9sN0Zib2FTdXlRYm95UC03S1RUTmVWLWZNIn0.eyJqdGkiOiJmYTU4Y2NjMC00ZDRiLTQ2ZjAtYjgwOC0yMWI4ZTdhNmMxNDMiLCJleHAiOjE2NDAxODc3MTksIm5iZiI6MCwiaWF0IjoxNjQwMTUxNzE5LCJpc3MiOiJodHRwczovL2Rldi5tb3NpcC5uZXQva2V5Y2xvYWsvYXV0aC9yZWFsbXMvbW9zaXAiLCJhdWQiOiJhY2NvdW50Iiwic3ViIjoiOWRiZTE0MDEtNTQ1NC00OTlhLTlhMWItNzVhZTY4M2Q0MjZhIiwidHlwIjoiQmVhcmVyIiwiYXpwIjoibW9zaXAtcmVzaWRlbnQtY2xpZW50IiwiYXV0aF90aW1lIjowLCJzZXNzaW9uX3N0YXRlIjoiY2QwYjU5NjEtOTYzMi00NmE0LWIzMzgtODc4MWEzNDVmMTZiIiwiYWNyIjoiMSIsImFsbG93ZWQtb3JpZ2lucyI6WyJodHRwczovL2Rldi5tb3NpcC5uZXQiXSwicmVhbG1fYWNjZXNzIjp7InJvbGVzIjpbIkNSRURFTlRJQUxfUkVRVUVTVCIsIlJFU0lERU5UIiwib2ZmbGluZV9hY2Nlc3MiLCJQQVJUTkVSX0FETUlOIiwidW1hX2F1dGhvcml6YXRpb24iXX0sInJlc291cmNlX2FjY2VzcyI6eyJtb3NpcC1yZXNpZGVudC1jbGllbnQiOnsicm9sZXMiOlsidW1hX3Byb3RlY3Rpb24iXX0sImFjY291bnQiOnsicm9sZXMiOlsibWFuYWdlLWFjY291bnQiLCJtYW5hZ2UtYWNjb3VudC1saW5rcyIsInZpZXctcHJvZmlsZSJdfX0sInNjb3BlIjoicHJvZmlsZSBlbWFpbCIsImNsaWVudEhvc3QiOiIxMC4yNDQuNS4xNDgiLCJlbWFpbF92ZXJpZmllZCI6ZmFsc2UsImNsaWVudElkIjoibW9zaXAtcmVzaWRlbnQtY2xpZW50IiwicHJlZmVycmVkX3VzZXJuYW1lIjoic2VydmljZS1hY2NvdW50LW1vc2lwLXJlc2lkZW50LWNsaWVudCIsImNsaWVudEFkZHJlc3MiOiIxMC4yNDQuNS4xNDgifQ.xZq1m3mBTEvFDENKFOI59QsSl3sd_TSDNbhTAOq4x_x_4voPc4hh08gIxUdsVHfXY4T0P8DdZ1xNt8xd1VWc33Hc4b_3kK7ksGY4wwqtb0-pDLQGajCGuG6vebC1rYcjsGRbJ1Gnrj_F2RNY4Ky6Nq5SAJ1Lh_NVKNKFghAXb3YrlmqlmCB1fCltC4XBqNnF5_k4uzLCu_Wr0lt_M87X97DktaRGLOD2_HY1Ire9YPsWkoO8y7X_DRCY59yQDVgYs2nAiR6Am-c55Q0fEQ0HuB4IJHlhtMHm27dXPdOEhFhR8ZPOyeO6ZIcIm0ZTDjusrruqWy2_yO5fe3XIHkCOAw";
		String userInfoPath = issuerInternalURI + "mosip" + userInfo;
		String resp = "{\r\n" + "  \"error\": \"Forbidden\",\r\n" + "  \"error_description\": \"forbidden\" }";
		when(restTemplate.exchange(Mockito.eq(userInfoPath), Mockito.eq(HttpMethod.GET), Mockito.any(),
				Mockito.eq(String.class)))
						.thenThrow(new HttpClientErrorException(HttpStatus.FORBIDDEN, "403", resp.getBytes(),
								Charset.defaultCharset()));
		ImmutablePair<HttpStatus, MosipUserDto> res = validateTokenHelper.doOnlineTokenValidation(token, restTemplate);
		assertNull(res.getValue());
	}

	/**
	 * Asserts RestTemplate userinfo HTTP 200 yields a non-null user DTO.
	 *
	 * @throws Exception if the helper call fails
	 */
	@Test
	public void doOnlineTokenValidation_withResponseStatusAsOK() throws Exception {
		String token = "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJzNmYxcDYwYWVDTTBrNy1NaW9sN0Zib2FTdXlRYm95UC03S1RUTmVWLWZNIn0.eyJqdGkiOiJmYTU4Y2NjMC00ZDRiLTQ2ZjAtYjgwOC0yMWI4ZTdhNmMxNDMiLCJleHAiOjE2NDAxODc3MTksIm5iZiI6MCwiaWF0IjoxNjQwMTUxNzE5LCJpc3MiOiJodHRwczovL2Rldi5tb3NpcC5uZXQva2V5Y2xvYWsvYXV0aC9yZWFsbXMvbW9zaXAiLCJhdWQiOiJhY2NvdW50Iiwic3ViIjoiOWRiZTE0MDEtNTQ1NC00OTlhLTlhMWItNzVhZTY4M2Q0MjZhIiwidHlwIjoiQmVhcmVyIiwiYXpwIjoibW9zaXAtcmVzaWRlbnQtY2xpZW50IiwiYXV0aF90aW1lIjowLCJzZXNzaW9uX3N0YXRlIjoiY2QwYjU5NjEtOTYzMi00NmE0LWIzMzgtODc4MWEzNDVmMTZiIiwiYWNyIjoiMSIsImFsbG93ZWQtb3JpZ2lucyI6WyJodHRwczovL2Rldi5tb3NpcC5uZXQiXSwicmVhbG1fYWNjZXNzIjp7InJvbGVzIjpbIkNSRURFTlRJQUxfUkVRVUVTVCIsIlJFU0lERU5UIiwib2ZmbGluZV9hY2Nlc3MiLCJQQVJUTkVSX0FETUlOIiwidW1hX2F1dGhvcml6YXRpb24iXX0sInJlc291cmNlX2FjY2VzcyI6eyJtb3NpcC1yZXNpZGVudC1jbGllbnQiOnsicm9sZXMiOlsidW1hX3Byb3RlY3Rpb24iXX0sImFjY291bnQiOnsicm9sZXMiOlsibWFuYWdlLWFjY291bnQiLCJtYW5hZ2UtYWNjb3VudC1saW5rcyIsInZpZXctcHJvZmlsZSJdfX0sInNjb3BlIjoicHJvZmlsZSBlbWFpbCIsImNsaWVudEhvc3QiOiIxMC4yNDQuNS4xNDgiLCJlbWFpbF92ZXJpZmllZCI6ZmFsc2UsImNsaWVudElkIjoibW9zaXAtcmVzaWRlbnQtY2xpZW50IiwicHJlZmVycmVkX3VzZXJuYW1lIjoic2VydmljZS1hY2NvdW50LW1vc2lwLXJlc2lkZW50LWNsaWVudCIsImNsaWVudEFkZHJlc3MiOiIxMC4yNDQuNS4xNDgifQ.xZq1m3mBTEvFDENKFOI59QsSl3sd_TSDNbhTAOq4x_x_4voPc4hh08gIxUdsVHfXY4T0P8DdZ1xNt8xd1VWc33Hc4b_3kK7ksGY4wwqtb0-pDLQGajCGuG6vebC1rYcjsGRbJ1Gnrj_F2RNY4Ky6Nq5SAJ1Lh_NVKNKFghAXb3YrlmqlmCB1fCltC4XBqNnF5_k4uzLCu_Wr0lt_M87X97DktaRGLOD2_HY1Ire9YPsWkoO8y7X_DRCY59yQDVgYs2nAiR6Am-c55Q0fEQ0HuB4IJHlhtMHm27dXPdOEhFhR8ZPOyeO6ZIcIm0ZTDjusrruqWy2_yO5fe3XIHkCOAw";
		String userInfoPath = issuerInternalURI + "mosip" + userInfo;
		String resp = "{\r\n" + "  \"error\": \"Forbidden\",\r\n" + "  \"error_description\": \"forbidden\" }";

		when(restTemplate.exchange(Mockito.eq(userInfoPath), Mockito.eq(HttpMethod.GET), Mockito.any(),
				Mockito.eq(String.class))).thenReturn(ResponseEntity.ok("test"));
		ImmutablePair<HttpStatus, MosipUserDto> res = validateTokenHelper.doOnlineTokenValidation(token, restTemplate);
		Assert.assertNotNull(res.getValue());
	}

	/**
	 * Asserts WebClient userinfo HTTP 200 maps the preferred username onto the
	 * user DTO.
	 *
	 * @throws Exception if the helper call fails
	 */
	@Test
	public void doOnlineTokenValidationWebClientTest() throws Exception {
		String token = "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJzNmYxcDYwYWVDTTBrNy1NaW9sN0Zib2FTdXlRYm95UC03S1RUTmVWLWZNIn0.eyJqdGkiOiJmYTU4Y2NjMC00ZDRiLTQ2ZjAtYjgwOC0yMWI4ZTdhNmMxNDMiLCJleHAiOjE2NDAxODc3MTksIm5iZiI6MCwiaWF0IjoxNjQwMTUxNzE5LCJpc3MiOiJodHRwczovL2Rldi5tb3NpcC5uZXQva2V5Y2xvYWsvYXV0aC9yZWFsbXMvbW9zaXAiLCJhdWQiOiJhY2NvdW50Iiwic3ViIjoiOWRiZTE0MDEtNTQ1NC00OTlhLTlhMWItNzVhZTY4M2Q0MjZhIiwidHlwIjoiQmVhcmVyIiwiYXpwIjoibW9zaXAtcmVzaWRlbnQtY2xpZW50IiwiYXV0aF90aW1lIjowLCJzZXNzaW9uX3N0YXRlIjoiY2QwYjU5NjEtOTYzMi00NmE0LWIzMzgtODc4MWEzNDVmMTZiIiwiYWNyIjoiMSIsImFsbG93ZWQtb3JpZ2lucyI6WyJodHRwczovL2Rldi5tb3NpcC5uZXQiXSwicmVhbG1fYWNjZXNzIjp7InJvbGVzIjpbIkNSRURFTlRJQUxfUkVRVUVTVCIsIlJFU0lERU5UIiwib2ZmbGluZV9hY2Nlc3MiLCJQQVJUTkVSX0FETUlOIiwidW1hX2F1dGhvcml6YXRpb24iXX0sInJlc291cmNlX2FjY2VzcyI6eyJtb3NpcC1yZXNpZGVudC1jbGllbnQiOnsicm9sZXMiOlsidW1hX3Byb3RlY3Rpb24iXX0sImFjY291bnQiOnsicm9sZXMiOlsibWFuYWdlLWFjY291bnQiLCJtYW5hZ2UtYWNjb3VudC1saW5rcyIsInZpZXctcHJvZmlsZSJdfX0sInNjb3BlIjoicHJvZmlsZSBlbWFpbCIsImNsaWVudEhvc3QiOiIxMC4yNDQuNS4xNDgiLCJlbWFpbF92ZXJpZmllZCI6ZmFsc2UsImNsaWVudElkIjoibW9zaXAtcmVzaWRlbnQtY2xpZW50IiwicHJlZmVycmVkX3VzZXJuYW1lIjoic2VydmljZS1hY2NvdW50LW1vc2lwLXJlc2lkZW50LWNsaWVudCIsImNsaWVudEFkZHJlc3MiOiIxMC4yNDQuNS4xNDgifQ.xZq1m3mBTEvFDENKFOI59QsSl3sd_TSDNbhTAOq4x_x_4voPc4hh08gIxUdsVHfXY4T0P8DdZ1xNt8xd1VWc33Hc4b_3kK7ksGY4wwqtb0-pDLQGajCGuG6vebC1rYcjsGRbJ1Gnrj_F2RNY4Ky6Nq5SAJ1Lh_NVKNKFghAXb3YrlmqlmCB1fCltC4XBqNnF5_k4uzLCu_Wr0lt_M87X97DktaRGLOD2_HY1Ire9YPsWkoO8y7X_DRCY59yQDVgYs2nAiR6Am-c55Q0fEQ0HuB4IJHlhtMHm27dXPdOEhFhR8ZPOyeO6ZIcIm0ZTDjusrruqWy2_yO5fe3XIHkCOAw";
		String userInfoPath = issuerInternalURI + "mosip" + userInfo;
		RequestBodyUriSpec requestBodyUriSpec = Mockito.mock(RequestBodyUriSpec.class);
		ResponseSpec responseSpec = Mockito.mock(ResponseSpec.class);
		when(webClient.method(HttpMethod.GET)).thenReturn(requestBodyUriSpec);
		when(requestBodyUriSpec.uri(userInfoPath)).thenReturn(requestBodyUriSpec);
		when(requestBodyUriSpec.headers(Mockito.any())).thenReturn(requestBodyUriSpec);
		when(requestBodyUriSpec.retrieve()).thenReturn(responseSpec);
		when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just("{\"sub\":\"ok\"}"));
		ImmutablePair<HttpStatus, MosipUserDto> res = validateTokenHelper.doOnlineTokenValidation(token, webClient);
		assertThat(res.getRight().getUserId(), is("service-account-mosip-resident-client"));
	}

	/**
	 * Asserts WebClient userinfo HTTP 401 yields an UNAUTHORIZED status pair.
	 *
	 * @throws Exception if the helper call fails
	 */
	@Test
	public void doOnlineTokenValidationWebClientErrorTest() throws Exception {
		String token = "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJzNmYxcDYwYWVDTTBrNy1NaW9sN0Zib2FTdXlRYm95UC03S1RUTmVWLWZNIn0.eyJqdGkiOiJmYTU4Y2NjMC00ZDRiLTQ2ZjAtYjgwOC0yMWI4ZTdhNmMxNDMiLCJleHAiOjE2NDAxODc3MTksIm5iZiI6MCwiaWF0IjoxNjQwMTUxNzE5LCJpc3MiOiJodHRwczovL2Rldi5tb3NpcC5uZXQva2V5Y2xvYWsvYXV0aC9yZWFsbXMvbW9zaXAiLCJhdWQiOiJhY2NvdW50Iiwic3ViIjoiOWRiZTE0MDEtNTQ1NC00OTlhLTlhMWItNzVhZTY4M2Q0MjZhIiwidHlwIjoiQmVhcmVyIiwiYXpwIjoibW9zaXAtcmVzaWRlbnQtY2xpZW50IiwiYXV0aF90aW1lIjowLCJzZXNzaW9uX3N0YXRlIjoiY2QwYjU5NjEtOTYzMi00NmE0LWIzMzgtODc4MWEzNDVmMTZiIiwiYWNyIjoiMSIsImFsbG93ZWQtb3JpZ2lucyI6WyJodHRwczovL2Rldi5tb3NpcC5uZXQiXSwicmVhbG1fYWNjZXNzIjp7InJvbGVzIjpbIkNSRURFTlRJQUxfUkVRVUVTVCIsIlJFU0lERU5UIiwib2ZmbGluZV9hY2Nlc3MiLCJQQVJUTkVSX0FETUlOIiwidW1hX2F1dGhvcml6YXRpb24iXX0sInJlc291cmNlX2FjY2VzcyI6eyJtb3NpcC1yZXNpZGVudC1jbGllbnQiOnsicm9sZXMiOlsidW1hX3Byb3RlY3Rpb24iXX0sImFjY291bnQiOnsicm9sZXMiOlsibWFuYWdlLWFjY291bnQiLCJtYW5hZ2UtYWNjb3VudC1saW5rcyIsInZpZXctcHJvZmlsZSJdfX0sInNjb3BlIjoicHJvZmlsZSBlbWFpbCIsImNsaWVudEhvc3QiOiIxMC4yNDQuNS4xNDgiLCJlbWFpbF92ZXJpZmllZCI6ZmFsc2UsImNsaWVudElkIjoibW9zaXAtcmVzaWRlbnQtY2xpZW50IiwicHJlZmVycmVkX3VzZXJuYW1lIjoic2VydmljZS1hY2NvdW50LW1vc2lwLXJlc2lkZW50LWNsaWVudCIsImNsaWVudEFkZHJlc3MiOiIxMC4yNDQuNS4xNDgifQ.xZq1m3mBTEvFDENKFOI59QsSl3sd_TSDNbhTAOq4x_x_4voPc4hh08gIxUdsVHfXY4T0P8DdZ1xNt8xd1VWc33Hc4b_3kK7ksGY4wwqtb0-pDLQGajCGuG6vebC1rYcjsGRbJ1Gnrj_F2RNY4Ky6Nq5SAJ1Lh_NVKNKFghAXb3YrlmqlmCB1fCltC4XBqNnF5_k4uzLCu_Wr0lt_M87X97DktaRGLOD2_HY1Ire9YPsWkoO8y7X_DRCY59yQDVgYs2nAiR6Am-c55Q0fEQ0HuB4IJHlhtMHm27dXPdOEhFhR8ZPOyeO6ZIcIm0ZTDjusrruqWy2_yO5fe3XIHkCOAw";
		String userInfoPath = issuerInternalURI + "mosip" + userInfo;
		RequestBodyUriSpec requestBodyUriSpec = Mockito.mock(RequestBodyUriSpec.class);
		when(webClient.method(HttpMethod.GET)).thenReturn(requestBodyUriSpec);
		when(requestBodyUriSpec.uri(userInfoPath)).thenReturn(requestBodyUriSpec);
		when(requestBodyUriSpec.headers(Mockito.any())).thenReturn(requestBodyUriSpec);
		when(requestBodyUriSpec.retrieve()).thenThrow(
				WebClientResponseException.create(HttpStatus.UNAUTHORIZED.value(), "Unauthorized", null, null, null));
		ImmutablePair<HttpStatus, MosipUserDto> res = validateTokenHelper.doOnlineTokenValidation(token, webClient);
		assertThat(res.getKey(), is(HttpStatus.UNAUTHORIZED));
	}

	/**
	 * Asserts an unsigned ({@code alg=none}) JWT is rejected by offline local
	 * validation with {@link AuthManagerException}.
	 *
	 * @throws Exception if the helper call fails unexpectedly
	 */
	@Test(expected = AuthManagerException.class)
	public void doOfflineTokenValidationTest() throws Exception {
		String token = JWT.create().withClaim(AuthAdapterConstant.EMAIL, "mockuser!mosip.com")
				.withClaim(AuthAdapterConstant.MOBILE, "9210283991")
				.withClaim(AuthAdapterConstant.PREFERRED_USERNAME, "mock-user")
				.withClaim(AuthAdapterConstant.ROLES, "ADMIN").withSubject("mock-user")
				.withIssuedAt(Date.from(Instant.now())).withExpiresAt(Date.from(Instant.now().plusSeconds(345600)))
				.sign(Algorithm.none());

		validateTokenHelper.doOfflineLocalTokenValidation(token);
		//assertThat(validateTokenHelper.doOfflineLocalTokenValidation(token));

	}

	/**
	 * Asserts an RSA256-signed JWT with matching issuer and audience is valid.
	 *
	 * @throws Exception if key generation or validation fails
	 */
	@Test
	public void isTokenValidTest() throws Exception {
		KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
		kpg.initialize(2048);
		KeyPair kp = kpg.generateKeyPair();
		Map<String, Object> headers = new HashMap<>();
		headers.put("alg", "RSA256");
		String token = JWT.create().withHeader(headers).withClaim(AuthAdapterConstant.EMAIL, "mockuser!mosip.com")
				.withClaim(AuthAdapterConstant.MOBILE, "9210283991")
				.withClaim(AuthAdapterConstant.PREFERRED_USERNAME, "mock-user")
				.withClaim(AuthAdapterConstant.ROLES, "ADMIN").withClaim(AuthAdapterConstant.AZP, "account")
				.withClaim(AuthAdapterConstant.ISSUER, "https://iam.mosip.net/auth/realms/").withSubject("mock-user")
				.withIssuedAt(Date.from(Instant.now())).withExpiresAt(Date.from(Instant.now().plusSeconds(345600)))
				.withAudience(new String[] { "account" })
				.sign(Algorithm.RSA256((RSAPublicKey) kp.getPublic(), (RSAPrivateKey) kp.getPrivate()));

		ImmutablePair<Boolean, AuthAdapterErrorCode> res = validateTokenHelper.isTokenValid(
				JWT.require(Algorithm.RSA256((RSAPublicKey) kp.getPublic(), (RSAPrivateKey) kp.getPrivate())).build()
						.verify(token),
				kp.getPublic());
		assertThat(res.left, is(true));
	}

	/**
	 * Asserts an RSA384-signed JWT is accepted by {@code isTokenValid}.
	 *
	 * @throws Exception if key generation or validation fails
	 */
	@Test
	public void isTokenValid_withRSA384Algo_thenPass() throws Exception {
		KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
		kpg.initialize(2048);
		KeyPair kp = kpg.generateKeyPair();
		Map<String, Object> headers = new HashMap<>();
		headers.put("alg", "RSA384");
		String token = JWT.create().withHeader(headers).withClaim(AuthAdapterConstant.EMAIL, "mockuser!mosip.com")
				.withClaim(AuthAdapterConstant.MOBILE, "9210283991")
				.withClaim(AuthAdapterConstant.PREFERRED_USERNAME, "mock-user")
				.withClaim(AuthAdapterConstant.ROLES, "ADMIN").withClaim(AuthAdapterConstant.AZP, "account")
				.withClaim(AuthAdapterConstant.ISSUER, "https://iam.mosip.net/auth/realms/").withSubject("mock-user")
				.withIssuedAt(Date.from(Instant.now())).withExpiresAt(Date.from(Instant.now().plusSeconds(345600)))
				.withAudience(new String[] { "account" })
				.sign(Algorithm.RSA384((RSAPublicKey) kp.getPublic(), (RSAPrivateKey) kp.getPrivate()));

		ImmutablePair<Boolean, AuthAdapterErrorCode> res = validateTokenHelper.isTokenValid(
				JWT.require(Algorithm.RSA384((RSAPublicKey) kp.getPublic(), (RSAPrivateKey) kp.getPrivate())).build()
						.verify(token),
				kp.getPublic());
		assertThat(res.left, is(true));
	}

	/**
	 * Asserts an RSA512-signed JWT is accepted by {@code isTokenValid}.
	 *
	 * @throws Exception if key generation or validation fails
	 */
	@Test
	public void isTokenValid_withRSA512Algo_thenPass() throws Exception {
		KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
		kpg.initialize(2048);
		KeyPair kp = kpg.generateKeyPair();
		Map<String, Object> headers = new HashMap<>();
		headers.put("alg", "RS512");
		String token = JWT.create().withHeader(headers).withClaim(AuthAdapterConstant.EMAIL, "mockuser!mosip.com")
				.withClaim(AuthAdapterConstant.MOBILE, "9210283991")
				.withClaim(AuthAdapterConstant.PREFERRED_USERNAME, "mock-user")
				.withClaim(AuthAdapterConstant.ROLES, "ADMIN").withClaim(AuthAdapterConstant.AZP, "account")
				.withClaim(AuthAdapterConstant.ISSUER, "https://iam.mosip.net/auth/realms/").withSubject("mock-user")
				.withIssuedAt(Date.from(Instant.now())).withExpiresAt(Date.from(Instant.now().plusSeconds(345600)))
				.withAudience(new String[] { "account" })
				.sign(Algorithm.RSA512((RSAPublicKey) kp.getPublic(), (RSAPrivateKey) kp.getPrivate()));

		ImmutablePair<Boolean, AuthAdapterErrorCode> res = validateTokenHelper.isTokenValid(
				JWT.require(Algorithm.RSA512((RSAPublicKey) kp.getPublic(), (RSAPrivateKey) kp.getPrivate())).build()
						.verify(token),
				kp.getPublic());
		assertThat(res.left, is(true));
	}

	/**
	 * Asserts an expired JWT is invalid with {@code UNAUTHORIZED}.
	 *
	 * @throws Exception if key generation or validation fails
	 */
	@Test
	public void isTokenInvalidExpiryTest() throws Exception {
		KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
		kpg.initialize(2048);
		KeyPair kp = kpg.generateKeyPair();
		Map<String, Object> headers = new HashMap<>();
		headers.put("alg", "RSA256");
		String token = JWT.create().withHeader(headers).withClaim(AuthAdapterConstant.EMAIL, "mockuser!mosip.com")
				.withClaim(AuthAdapterConstant.MOBILE, "9210283991")
				.withClaim(AuthAdapterConstant.PREFERRED_USERNAME, "mock-user")
				.withClaim(AuthAdapterConstant.ROLES, "ADMIN").withClaim(AuthAdapterConstant.AZP, "account")
				.withClaim(AuthAdapterConstant.ISSUER, "https://iam.mosip.net/auth/realms/").withSubject("mock-user")
				.withIssuedAt(Date.from(Instant.now().minusSeconds(345600)))
				.withExpiresAt(Date.from(Instant.now().minusSeconds(100000))).withAudience(new String[] { "account" })
				.sign(Algorithm.RSA256((RSAPublicKey) kp.getPublic(), (RSAPrivateKey) kp.getPrivate()));

		ImmutablePair<Boolean, AuthAdapterErrorCode> res = validateTokenHelper.isTokenValid(JWT.decode(token),
				kp.getPublic());
		assertThat(res.left, is(false));
		assertThat(res.right, is(AuthAdapterErrorCode.UNAUTHORIZED));
	}

	/**
	 * Asserts a JWT with a non-configured issuer is invalid with
	 * {@code UNAUTHORIZED}.
	 *
	 * @throws Exception if key generation or validation fails
	 */
	@Test
	public void isTokenInvalidIssuerTest() throws Exception {
		KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
		kpg.initialize(2048);
		KeyPair kp = kpg.generateKeyPair();
		Map<String, Object> headers = new HashMap<>();
		headers.put("alg", "RSA256");
		String token = JWT.create().withHeader(headers).withClaim(AuthAdapterConstant.EMAIL, "mockuser!mosip.com")
				.withClaim(AuthAdapterConstant.MOBILE, "9210283991")
				.withClaim(AuthAdapterConstant.PREFERRED_USERNAME, "mock-user")
				.withClaim(AuthAdapterConstant.ROLES, "ADMIN").withClaim(AuthAdapterConstant.AZP, "account")
				.withClaim(AuthAdapterConstant.ISSUER, "https://dev.mosip.net/auth/realms/").withSubject("mock-user")
				.withIssuedAt(Date.from(Instant.now())).withExpiresAt(Date.from(Instant.now().plusSeconds(345600)))
				.withAudience(new String[] { "account" })
				.sign(Algorithm.RSA256((RSAPublicKey) kp.getPublic(), (RSAPrivateKey) kp.getPrivate()));

		ImmutablePair<Boolean, AuthAdapterErrorCode> res = validateTokenHelper.isTokenValid(JWT.decode(token),
				kp.getPublic());
		assertThat(res.left, is(false));
		assertThat(res.right, is(AuthAdapterErrorCode.UNAUTHORIZED));
	}

	/**
	 * Asserts a JWT verified against a different RSA public key is invalid with
	 * {@code UNAUTHORIZED}.
	 *
	 * @throws Exception if key generation or validation fails
	 */
	@Test
	public void isTokenInvalidSignatureTest() throws Exception {
		KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
		kpg.initialize(2048);
		KeyPair kp = kpg.generateKeyPair();
		Map<String, Object> headers = new HashMap<>();
		headers.put("alg", "RSA256");
		String token = JWT.create().withHeader(headers).withClaim(AuthAdapterConstant.EMAIL, "mockuser!mosip.com")
				.withClaim(AuthAdapterConstant.MOBILE, "9210283991")
				.withClaim(AuthAdapterConstant.PREFERRED_USERNAME, "mock-user")
				.withClaim(AuthAdapterConstant.ROLES, "ADMIN").withClaim(AuthAdapterConstant.AZP, "account")
				.withClaim(AuthAdapterConstant.ISSUER, "https://iam.mosip.net/auth/realms/").withSubject("mock-user")
				.withIssuedAt(Date.from(Instant.now())).withExpiresAt(Date.from(Instant.now().plusSeconds(345600)))
				.withAudience(new String[] { "account" })
				.sign(Algorithm.RSA256((RSAPublicKey) kp.getPublic(), (RSAPrivateKey) kp.getPrivate()));
		kp = kpg.generateKeyPair();
		ImmutablePair<Boolean, AuthAdapterErrorCode> res = validateTokenHelper.isTokenValid(JWT.decode(token),
				kp.getPublic());
		assertThat(res.left, is(false));
		assertThat(res.right, is(AuthAdapterErrorCode.UNAUTHORIZED));
	}
	
	/**
	 * Asserts a JWT whose audience is not allowed is invalid with
	 * {@code FORBIDDEN}.
	 *
	 * @throws Exception if key generation or validation fails
	 */
	@Test
	public void isTokenInvalidAUDTest() throws Exception {
		KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
		kpg.initialize(2048);
		KeyPair kp = kpg.generateKeyPair();
		Map<String, Object> headers = new HashMap<>();
		headers.put("alg", "RSA256");
		String token = JWT.create().withHeader(headers).withClaim(AuthAdapterConstant.EMAIL, "mockuser!mosip.com")
				.withClaim(AuthAdapterConstant.MOBILE, "9210283991")
				.withClaim(AuthAdapterConstant.PREFERRED_USERNAME, "mock-user")
				.withClaim(AuthAdapterConstant.ROLES, "ADMIN").withClaim(AuthAdapterConstant.AZP, "abc")
				.withClaim(AuthAdapterConstant.ISSUER, "https://iam.mosip.net/auth/realms/").withSubject("mock-user")
				.withIssuedAt(Date.from(Instant.now())).withExpiresAt(Date.from(Instant.now().plusSeconds(345600)))
				.withAudience(new String[] { "abc" })
				.sign(Algorithm.RSA256((RSAPublicKey) kp.getPublic(), (RSAPrivateKey) kp.getPrivate()));

		ImmutablePair<Boolean, AuthAdapterErrorCode> res = validateTokenHelper.isTokenValid(
				JWT.decode(token),
				kp.getPublic());
		assertThat(res.left, is(false));
		assertThat(res.right, is(AuthAdapterErrorCode.FORBIDDEN));
	}

	/**
	 * Asserts {@code getPublicKey} returns the cached JWKS key matching the JWT
	 * {@code kid}.
	 *
	 * @throws Exception if key generation or lookup fails
	 */
	@Test
	public void getPublicKey_withValidDetails_thenPass() throws Exception {
		// Setup: Mock dependencies and prepare the environment
		KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
		kpg.initialize(2048);
		KeyPair kp = kpg.generateKeyPair();
		Map<String, PublicKey> publicKeyMap=new HashMap<>();
		publicKeyMap.put("s6f1p60aeCM0k7-Miol7FboaSuyQboyP-7KTTNeV-fM",kp.getPublic());
		ReflectionTestUtils.setField(validateTokenHelper,"publicKeys",publicKeyMap);
		String jwtToken="eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJzNmYxcDYwYWVDTTBrNy1NaW9sN0Zib2FTdXlRYm95UC03S1RUTmVWLWZNIn0.eyJqdGkiOiJmYTU4Y2NjMC00ZDRiLTQ2ZjAtYjgwOC0yMWI4ZTdhNmMxNDMiLCJleHAiOjE2NDAxODc3MTksIm5iZiI6MCwiaWF0IjoxNjQwMTUxNzE5LCJpc3MiOiJodHRwczovL2Rldi5tb3NpcC5uZXQva2V5Y2xvYWsvYXV0aC9yZWFsbXMvbW9zaXAiLCJhdWQiOiJhY2NvdW50Iiwic3ViIjoiOWRiZTE0MDEtNTQ1NC00OTlhLTlhMWItNzVhZTY4M2Q0MjZhIiwidHlwIjoiQmVhcmVyIiwiYXpwIjoibW9zaXAtcmVzaWRlbnQtY2xpZW50IiwiYXV0aF90aW1lIjowLCJzZXNzaW9uX3N0YXRlIjoiY2QwYjU5NjEtOTYzMi00NmE0LWIzMzgtODc4MWEzNDVmMTZiIiwiYWNyIjoiMSIsImFsbG93ZWQtb3JpZ2lucyI6WyJodHRwczovL2Rldi5tb3NpcC5uZXQiXSwicmVhbG1fYWNjZXNzIjp7InJvbGVzIjpbIkNSRURFTlRJQUxfUkVRVUVTVCIsIlJFU0lERU5UIiwib2ZmbGluZV9hY2Nlc3MiLCJQQVJUTkVSX0FETUlOIiwidW1hX2F1dGhvcml6YXRpb24iXX0sInJlc291cmNlX2FjY2VzcyI6eyJtb3NpcC1yZXNpZGVudC1jbGllbnQiOnsicm9sZXMiOlsidW1hX3Byb3RlY3Rpb24iXX0sImFjY291bnQiOnsicm9sZXMiOlsibWFuYWdlLWFjY291bnQiLCJtYW5hZ2UtYWNjb3VudC1saW5rcyIsInZpZXctcHJvZmlsZSJdfX0sInNjb3BlIjoicHJvZmlsZSBlbWFpbCIsImNsaWVudEhvc3QiOiIxMC4yNDQuNS4xNDgiLCJlbWFpbF92ZXJpZmllZCI6ZmFsc2UsImNsaWVudElkIjoibW9zaXAtcmVzaWRlbnQtY2xpZW50IiwicHJlZmVycmVkX3VzZXJuYW1lIjoic2VydmljZS1hY2NvdW50LW1vc2lwLXJlc2lkZW50LWNsaWVudCIsImNsaWVudEFkZHJlc3MiOiIxMC4yNDQuNS4xNDgifQ.xZq1m3mBTEvFDENKFOI59QsSl3sd_TSDNbhTAOq4x_x_4voPc4hh08gIxUdsVHfXY4T0P8DdZ1xNt8xd1VWc33Hc4b_3kK7ksGY4wwqtb0-pDLQGajCGuG6vebC1rYcjsGRbJ1Gnrj_F2RNY4Ky6Nq5SAJ1Lh_NVKNKFghAXb3YrlmqlmCB1fCltC4XBqNnF5_k4uzLCu_Wr0lt_M87X97DktaRGLOD2_HY1Ire9YPsWkoO8y7X_DRCY59yQDVgYs2nAiR6Am-c55Q0fEQ0HuB4IJHlhtMHm27dXPdOEhFhR8ZPOyeO6ZIcIm0ZTDjusrruqWy2_yO5fe3XIHkCOAw";
		DecodedJWT decodedJWT = JWT.decode(jwtToken);

		// Execute
		PublicKey publicKey = validateTokenHelper.getPublicKey(decodedJWT);
		Assert.assertNotNull(publicKey);
		Assert.assertEquals(publicKey,kp.getPublic());
	}


	/**
	 * Asserts {@code getPublicKey} returns {@code null} when the JWKS cache has
	 * no matching {@code kid}.
	 *
	 * @throws Exception if JWT decode or lookup fails
	 */
	@Test
	public void getPublicKey_withInValidDetails_thenPass() throws Exception {
		Map<String, PublicKey> publicKeyMap=new HashMap<>();
		ReflectionTestUtils.setField(validateTokenHelper,"publicKeys",publicKeyMap);
		String jwtToken="eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJzNmYxcDYwYWVDTTBrNy1NaW9sN0Zib2FTdXlRYm95UC03S1RUTmVWLWZNIn0.eyJqdGkiOiJmYTU4Y2NjMC00ZDRiLTQ2ZjAtYjgwOC0yMWI4ZTdhNmMxNDMiLCJleHAiOjE2NDAxODc3MTksIm5iZiI6MCwiaWF0IjoxNjQwMTUxNzE5LCJpc3MiOiJodHRwczovL2Rldi5tb3NpcC5uZXQva2V5Y2xvYWsvYXV0aC9yZWFsbXMvbW9zaXAiLCJhdWQiOiJhY2NvdW50Iiwic3ViIjoiOWRiZTE0MDEtNTQ1NC00OTlhLTlhMWItNzVhZTY4M2Q0MjZhIiwidHlwIjoiQmVhcmVyIiwiYXpwIjoibW9zaXAtcmVzaWRlbnQtY2xpZW50IiwiYXV0aF90aW1lIjowLCJzZXNzaW9uX3N0YXRlIjoiY2QwYjU5NjEtOTYzMi00NmE0LWIzMzgtODc4MWEzNDVmMTZiIiwiYWNyIjoiMSIsImFsbG93ZWQtb3JpZ2lucyI6WyJodHRwczovL2Rldi5tb3NpcC5uZXQiXSwicmVhbG1fYWNjZXNzIjp7InJvbGVzIjpbIkNSRURFTlRJQUxfUkVRVUVTVCIsIlJFU0lERU5UIiwib2ZmbGluZV9hY2Nlc3MiLCJQQVJUTkVSX0FETUlOIiwidW1hX2F1dGhvcml6YXRpb24iXX0sInJlc291cmNlX2FjY2VzcyI6eyJtb3NpcC1yZXNpZGVudC1jbGllbnQiOnsicm9sZXMiOlsidW1hX3Byb3RlY3Rpb24iXX0sImFjY291bnQiOnsicm9sZXMiOlsibWFuYWdlLWFjY291bnQiLCJtYW5hZ2UtYWNjb3VudC1saW5rcyIsInZpZXctcHJvZmlsZSJdfX0sInNjb3BlIjoicHJvZmlsZSBlbWFpbCIsImNsaWVudEhvc3QiOiIxMC4yNDQuNS4xNDgiLCJlbWFpbF92ZXJpZmllZCI6ZmFsc2UsImNsaWVudElkIjoibW9zaXAtcmVzaWRlbnQtY2xpZW50IiwicHJlZmVycmVkX3VzZXJuYW1lIjoic2VydmljZS1hY2NvdW50LW1vc2lwLXJlc2lkZW50LWNsaWVudCIsImNsaWVudEFkZHJlc3MiOiIxMC4yNDQuNS4xNDgifQ.xZq1m3mBTEvFDENKFOI59QsSl3sd_TSDNbhTAOq4x_x_4voPc4hh08gIxUdsVHfXY4T0P8DdZ1xNt8xd1VWc33Hc4b_3kK7ksGY4wwqtb0-pDLQGajCGuG6vebC1rYcjsGRbJ1Gnrj_F2RNY4Ky6Nq5SAJ1Lh_NVKNKFghAXb3YrlmqlmCB1fCltC4XBqNnF5_k4uzLCu_Wr0lt_M87X97DktaRGLOD2_HY1Ire9YPsWkoO8y7X_DRCY59yQDVgYs2nAiR6Am-c55Q0fEQ0HuB4IJHlhtMHm27dXPdOEhFhR8ZPOyeO6ZIcIm0ZTDjusrruqWy2_yO5fe3XIHkCOAw";
		DecodedJWT decodedJWT = JWT.decode(jwtToken);

		// Execute
		PublicKey publicKey = validateTokenHelper.getPublicKey(decodedJWT);
		Assert.assertNull(publicKey);
	}



}
