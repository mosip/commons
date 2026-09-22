package io.mosip.kernel.auth.defaultadapter.test;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.nio.charset.Charset;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.RequestBodySpec;
import org.springframework.web.reactive.function.client.WebClient.RequestBodyUriSpec;
import org.springframework.web.reactive.function.client.WebClient.RequestHeadersSpec;
import org.springframework.web.reactive.function.client.WebClient.ResponseSpec;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.kernel.auth.defaultadapter.exception.AuthRestException;
import io.mosip.kernel.auth.defaultadapter.helper.TokenHelper;
import reactor.core.publisher.Mono;

/**
 * Tests {@link TokenHelper#getClientToken} for RestTemplate and WebClient
 * client-credentials token requests, including HTTP errors, MOSIP error
 * payloads, and malformed token responses.
 */
@SpringBootTest(classes = { AuthTestBootApplication.class })
@RunWith(SpringRunner.class)
public class TokenHelperTest {

	/** Public IAM issuer URI from test properties. */
	@Value("${auth.server.admin.issuer.uri:}")
    private String issuerURI;
	
	/** Internal IAM issuer URI used to build the token endpoint. */
	@Value("${auth.server.admin.issuer.internal.uri:}")
    private String issuerInternalURI;

	/** JSON mapper from the test context. */
	@Autowired
	private ObjectMapper mapper;

	/** App-id to Keycloak realm mapping from test properties. */
	@Value("#{${mosip.kernel.auth.appids.realm.map}}")
	private Map<String, String> realmMap;

	/** OIDC token path appended to the issuer and realm. */
	@Value("${auth.server.admin.oidc.token.path:/protocol/openid-connect/token}")
    private String tokenPath;
	
	/** Token helper under test. */
	@Autowired
	private TokenHelper tokenHelper;

	/** Unused WebClient response-spec mock retained by the original test. */
	@Mock
	private WebClient.ResponseSpec responseSpec;
	
	
	/** Mock RestTemplate used for client-token HTTP calls. */
	private RestTemplate restTemplate = Mockito.mock(RestTemplate.class);
	/** Mock WebClient used for reactive client-token HTTP calls. */
	private WebClient webClient = Mockito.mock(WebClient.class);

	/** Placeholder setup method; no initialization is required. */
	public void init() {
		
	}
	
	
	/**
	 * Asserts RestTemplate client-token success returns the access token.
	 *
	 * @throws Exception if the helper call fails
	 */
	@Test
	public void getClientTokenTest() throws Exception {
		String tokenUrl = new StringBuilder(issuerInternalURI).append("mosip").append(tokenPath).toString();
		String resp= "{\"access_token\":\"mock-token\"}";
		when(restTemplate.postForEntity(Mockito.eq(tokenUrl),Mockito.any(),Mockito.eq(String.class))).thenReturn(ResponseEntity.ok(resp));
		String token=tokenHelper.getClientToken("mock-clientID", "mock-clientSecret", "ida", restTemplate);
	    assertTrue(token.equals("mock-token"));
	}
	
	/**
	 * Asserts a 404 from the token endpoint yields a {@code null} token.
	 *
	 * @throws Exception if the helper call fails
	 */
	@Test
	public void getClientTokenHttpExceptionTest() throws Exception {
		String tokenUrl = new StringBuilder(issuerInternalURI).append("mosip").append(tokenPath).toString();
		String resp= "{\"error\":\"not found\"}";
		when(restTemplate.postForEntity(Mockito.eq(tokenUrl),Mockito.any(),Mockito.eq(String.class))).thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND, "404", resp.getBytes(),
				Charset.defaultCharset()));;
		String token=tokenHelper.getClientToken("mock-clientID", "mock-clientSecret", "ida", restTemplate);
	    assertNull(token);
	}
	
	/**
	 * Asserts a MOSIP errors payload from the token endpoint raises
	 * {@link AuthRestException}.
	 *
	 * @throws Exception if the helper call fails unexpectedly
	 */
	@Test(expected = AuthRestException.class)
	public void getClientTokenAuthRestExceptionTest() throws Exception {
		String tokenUrl = new StringBuilder(issuerInternalURI).append("mosip").append(tokenPath).toString();
		String resp="{ \"errors\": [{\"errorCode\":\"KER-ATH-001\",\"message\":\"no token\"}]}";
		when(restTemplate.postForEntity(Mockito.eq(tokenUrl),Mockito.any(),Mockito.eq(String.class))).thenReturn(ResponseEntity.ok(resp));
		String token=tokenHelper.getClientToken("mock-clientID", "mock-clientSecret", "ida", restTemplate);
	    assertNull(token);
	}
	
	/**
	 * Asserts a malformed token JSON body yields a {@code null} token.
	 *
	 * @throws Exception if the helper call fails
	 */
	@Test
	public void getTokenValidatedVertxUserResponse() throws Exception {
		String tokenUrl = new StringBuilder(issuerInternalURI).append("mosip").append(tokenPath).toString();
		String resp= "{error\":\"not found\"}";
		when(restTemplate.postForEntity(Mockito.eq(tokenUrl),Mockito.any(),Mockito.eq(String.class))).thenReturn(ResponseEntity.ok(resp));
		String token=tokenHelper.getClientToken("mock-clientID", "mock-clientSecret", "ida", restTemplate);
	    assertNull(token);
	}
	
	
	/**
	 * Asserts WebClient client-token success returns the access token.
	 *
	 * @throws Exception if JSON parsing or the helper call fails
	 */
	@Test
	public void getClientTokenWebClientTest() throws Exception {
		String tokenUrl = new StringBuilder(issuerInternalURI).append("mosip").append(tokenPath).toString();
		String resp = "{\"access_token\":\"mock-token\"}";
		RequestBodyUriSpec requestBodyUriSpec = Mockito.mock(RequestBodyUriSpec.class);
		RequestBodySpec requestBodySpec = Mockito.mock(RequestBodySpec.class);
		RequestHeadersSpec<?> requestHeadersSpec = Mockito.mock(RequestHeadersSpec.class);
		ResponseSpec responseSpecMock = Mockito.mock(ResponseSpec.class);
		when(webClient.method(HttpMethod.POST)).thenReturn(requestBodyUriSpec);
		when(requestBodyUriSpec.uri(UriComponentsBuilder.fromUriString(tokenUrl).toUriString())).thenReturn(requestBodySpec);
		when(requestBodySpec.contentType(MediaType.APPLICATION_FORM_URLENCODED)).thenReturn(requestBodySpec);
		Mockito.doReturn(requestHeadersSpec).when(requestBodySpec).body(Mockito.any());
		when(requestHeadersSpec.retrieve()).thenReturn(responseSpecMock);
		when(responseSpecMock.bodyToMono(String.class)).thenReturn(Mono.just(resp));
		String token = tokenHelper.getClientToken("mock-clientID", "mock-clientSecret", "ida", webClient);
		assertTrue(token.equals("mock-token"));
	}

	/**
	 * Asserts WebClient HTTP 401 from the token endpoint yields a {@code null}
	 * token.
	 *
	 * @throws Exception if the helper call fails
	 */
	@Test
	public void getClientTokenWebClientErrorTest() throws Exception {
		String tokenUrl = new StringBuilder(issuerInternalURI).append("mosip").append(tokenPath).toString();
		RequestBodyUriSpec requestBodyUriSpec = Mockito.mock(RequestBodyUriSpec.class);
		RequestBodySpec requestBodySpec = Mockito.mock(RequestBodySpec.class);
		RequestHeadersSpec<?> requestHeadersSpec = Mockito.mock(RequestHeadersSpec.class);
		when(webClient.method(HttpMethod.POST)).thenReturn(requestBodyUriSpec);
		when(requestBodyUriSpec.uri(UriComponentsBuilder.fromUriString(tokenUrl).toUriString())).thenReturn(requestBodySpec);
		when(requestBodySpec.contentType(MediaType.APPLICATION_FORM_URLENCODED)).thenReturn(requestBodySpec);
		Mockito.doReturn(requestHeadersSpec).when(requestBodySpec).body(Mockito.any());
		when(requestHeadersSpec.retrieve()).thenThrow(
				org.springframework.web.reactive.function.client.WebClientResponseException.create(
						HttpStatus.UNAUTHORIZED.value(), "Unauthorized", null, null, null));
		String token = tokenHelper.getClientToken("mock-clientID", "mock-clientSecret", "ida", webClient);
		assertNull(token);
	}
	
	
}
