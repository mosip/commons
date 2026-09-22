package io.mosip.kernel.openid.bridge.api.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.lang.reflect.Constructor;

import org.junit.Before;
import org.junit.Test;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;

/**
 * Covers {@link JWTUtils} issuer cache, claim lookup, and the private constructor.
 */
public class JWTUtilsTest {

	private String token;

	@Before
	public void setUp() {
		JWTUtils.decodedJWTCache.clear();
		token = JWT.create().withIssuer("https://iam.example/realms/mosip").withSubject("user1")
				.sign(Algorithm.HMAC256("unit-test-secret-unit-test-secret"));
	}

	@Test
	public void getissuerDecodesAndCaches() {
		assertEquals("https://iam.example/realms/mosip", JWTUtils.getissuer(token));
		assertEquals("https://iam.example/realms/mosip", JWTUtils.getissuer(token));
		assertNotNull(JWTUtils.decodedJWTCache.get(token));
	}

	@Test
	public void getSubClaimValueFromTokenReadsSubject() {
		assertEquals("user1", JWTUtils.getSubClaimValueFromToken(token, "sub"));
		assertNull(JWTUtils.getSubClaimValueFromToken(token, "missing"));
	}

	@Test
	public void privateConstructorIsInvocable() throws Exception {
		Constructor<JWTUtils> constructor = JWTUtils.class.getDeclaredConstructor();
		constructor.setAccessible(true);
		assertNotNull(constructor.newInstance());
	}
}
