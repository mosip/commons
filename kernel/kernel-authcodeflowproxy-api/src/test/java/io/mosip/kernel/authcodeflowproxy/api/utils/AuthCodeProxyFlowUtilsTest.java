package io.mosip.kernel.authcodeflowproxy.api.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.lang.reflect.Constructor;

import org.junit.Before;
import org.junit.Test;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;

/**
 * Covers {@link AuthCodeProxyFlowUtils} cache hit/miss and private constructor.
 */
public class AuthCodeProxyFlowUtilsTest {

	private String token;

	@Before
	public void setUp() {
		AuthCodeProxyFlowUtils.decodedJWTCache.clear();
		token = JWT.create().withIssuer("https://iam.example/realms/mosip")
				.sign(Algorithm.HMAC256("unit-test-secret-unit-test-secret"));
	}

	@Test
	public void getissuerDecodesAndCaches() {
		assertEquals("https://iam.example/realms/mosip", AuthCodeProxyFlowUtils.getissuer(token));
		assertEquals("https://iam.example/realms/mosip", AuthCodeProxyFlowUtils.getissuer(token));
		assertNotNull(AuthCodeProxyFlowUtils.decodedJWTCache.get(token));
	}

	@Test
	public void privateConstructorIsInvocable() throws Exception {
		Constructor<AuthCodeProxyFlowUtils> constructor = AuthCodeProxyFlowUtils.class.getDeclaredConstructor();
		constructor.setAccessible(true);
		assertNotNull(constructor.newInstance());
	}
}
