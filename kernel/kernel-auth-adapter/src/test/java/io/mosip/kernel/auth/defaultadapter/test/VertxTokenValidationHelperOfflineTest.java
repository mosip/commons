package io.mosip.kernel.auth.defaultadapter.test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.core.env.Environment;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.kernel.auth.defaultadapter.constant.AuthAdapterConstant;
import io.mosip.kernel.auth.defaultadapter.constant.AuthAdapterErrorCode;
import io.mosip.kernel.auth.defaultadapter.helper.ValidateTokenHelper;
import io.mosip.kernel.auth.defaultadapter.helper.VertxTokenValidationHelper;
import io.mosip.kernel.openid.bridge.model.MosipUserDto;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;

/**
 * Tests offline Vert.x token validation in {@link VertxTokenValidationHelper}
 * when {@link ValidateTokenHelper} is replaced with a Boot 4 {@link MockitoBean}.
 */
@SpringBootTest(classes = { AuthTestBootApplication.class })
@RunWith(SpringRunner.class)
public class VertxTokenValidationHelperOfflineTest {

	  /** Admin token-validate URL from test properties. */
	  @Value("${auth.server.admin.validate.url:}")
		private String adminValidateUrl;

	    /** Whether Vert.x validation uses the offline path. */
	    @Value("${auth.server.admin.offline.vertx.token.validate:true}")
		private boolean offlineTokenValidate;

	    /** Active Spring profile from test properties. */
	    @Value("${spring.profiles.active:}")
		String activeProfile;
	    
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

		/** Internal IAM issuer URI. */
		@Value("${auth.server.admin.issuer.internal.uri:}")
	    private String issuerInternalURI;

	    /** Whether audience claim validation is enabled. */
	    @Value("${auth.server.admin.audience.claim.validate:true}")
	    private boolean validateAudClaim;

	    /** Allowed audience list (not injected; unused in this test). */
	    //@Value("${auth.server.admin.allowed.audience:}")
	    private List<String> allowedAudience;

	    /** JSON mapper from the test context. */
	    @Autowired
		private ObjectMapper objectMapper;

	    /** Spring environment from the test context. */
	    @Autowired
		private Environment environment;
	    
	    /** Offline JWT validation collaborator replaced with a Mockito bean. */
	    @MockitoBean
	    private ValidateTokenHelper validateTokenHelper;

	
	/** Vert.x token validation helper under test. */
	@Autowired
	private VertxTokenValidationHelper vertxTokenValidationHelper;
	
	
	/** Mock RestTemplate passed into Vert.x validation. */
	private RestTemplate restTemplate = Mockito.mock(RestTemplate.class);
    /** Mock Vert.x routing context for the request under test. */
    private RoutingContext routingContext;
    /** Mock Vert.x HTTP request used to supply the Authorization cookie. */
    private HttpServerRequest httpServerRequest;
    /** Mock Vert.x HTTP response (unused in the active test). */
    private HttpServerResponse httpServerResponse;

	/**
	 * Creates Vert.x request/response mocks for each test.
	 */
	@Before
    public void init() {
		routingContext = Mockito.mock(RoutingContext.class);
		httpServerRequest = Mockito.mock(HttpServerRequest.class);
		httpServerResponse = Mockito.mock(HttpServerResponse.class);
	}
	
	
	/**
	 * Asserts offline validation returns the user built from a signed RSA JWT
	 * when {@link ValidateTokenHelper} reports the token valid.
	 *
	 * @throws Exception if key generation or validation fails
	 */
	@Test
	public void getTokenValidatedVertxUserResponseTest() throws Exception {
		// JWKS offline path (not deprecated local). Do not inherit SPRING_PROFILES_ACTIVE=local.
		ReflectionTestUtils.setField(vertxTokenValidationHelper, "activeProfile", "test");
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
		String cookie="Authorization="+token;
		when(routingContext.request()).thenReturn(httpServerRequest);
		when(httpServerRequest.getHeader(AuthAdapterConstant.AUTH_HEADER_COOKIE)).thenReturn(cookie);
		when(validateTokenHelper.getPublicKey(Mockito.any(DecodedJWT.class))).thenReturn(kp.getPublic());
		ImmutablePair<Boolean, AuthAdapterErrorCode> immutablePair = new ImmutablePair<Boolean, AuthAdapterErrorCode>(true, null);
		when(validateTokenHelper.isTokenValid(Mockito.any(DecodedJWT.class), Mockito.eq(kp.getPublic()))).thenReturn(immutablePair);
		MosipUserDto mosipUserDto = new MosipUserDto();
		mosipUserDto.setUserId("mock-user");
		mosipUserDto.setRole("PARTNER_ADMIN");
		when(validateTokenHelper.buildMosipUser(Mockito.any(DecodedJWT.class), Mockito.eq(token))).thenReturn(mosipUserDto);
		
		String[] roles= {"PARTNER_ADMIN"};
		MosipUserDto md=vertxTokenValidationHelper.getTokenValidatedVertxUserResponse(restTemplate, routingContext, roles);
		assertThat(md.getUserId(),is("mock-user"));
	}
	
	/*
	 * @Test public void getTokenValidatedVertxUserExceptionResponseTest() throws
	 * Exception { KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
	 * kpg.initialize(2048); KeyPair kp = kpg.generateKeyPair(); Map<String, Object>
	 * headers = new HashMap<>(); headers.put("alg", "RSA256"); String token =
	 * JWT.create().withHeader(headers).withClaim(AuthAdapterConstant.EMAIL,
	 * "mockuser!mosip.com") .withClaim(AuthAdapterConstant.MOBILE, "9210283991")
	 * .withClaim(AuthAdapterConstant.PREFERRED_USERNAME, "mock-user")
	 * .withClaim(AuthAdapterConstant.ROLES,
	 * "ADMIN").withClaim(AuthAdapterConstant.AZP, "account")
	 * .withClaim(AuthAdapterConstant.ISSUER,
	 * "https://iam.mosip.net/auth/realms/").withSubject("mock-user")
	 * .withIssuedAt(Date.from(Instant.now())).withExpiresAt(Date.from(Instant.now()
	 * .plusSeconds(345600))) .withAudience(new String[] { "account" })
	 * .sign(Algorithm.RSA256((RSAPublicKey) kp.getPublic(), (RSAPrivateKey)
	 * kp.getPrivate())); String cookie="Authorization="+token;
	 * when(routingContext.request()).thenReturn(httpServerRequest);
	 * when(httpServerRequest.getHeader(AuthAdapterConstant.AUTH_HEADER_COOKIE)).
	 * thenReturn(cookie);
	 * when(validateTokenHelper.getPublicKey(Mockito.any(DecodedJWT.class))).
	 * thenReturn(kp.getPublic()); ImmutablePair<Boolean, AuthAdapterErrorCode>
	 * immutablePair = new ImmutablePair<Boolean, AuthAdapterErrorCode>(false,
	 * AuthAdapterErrorCode.UNAUTHORIZED);
	 * when(validateTokenHelper.isTokenValid(Mockito.any(DecodedJWT.class),
	 * Mockito.eq(kp.getPublic()))).thenReturn(immutablePair); MosipUserDto
	 * mosipUserDto = new MosipUserDto(); mosipUserDto.setUserId("mock-user");
	 * mosipUserDto.setRole("PARTNER_ADMIN");
	 * when(validateTokenHelper.buildMosipUser(Mockito.any(DecodedJWT.class),
	 * Mockito.eq(token))).thenReturn(mosipUserDto);
	 * 
	 * String[] roles= {"PARTNER_ADMIN"}; MosipUserDto
	 * md=vertxTokenValidationHelper.getTokenValidatedVertxUserResponse(
	 * restTemplate, routingContext, roles);
	 * assertThat(md.getUserId(),is("mock-user")); }
	 */
	
}
