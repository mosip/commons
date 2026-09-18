package io.mosip.kernel.core.test.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Date;

import org.junit.Test;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;

import io.mosip.kernel.core.util.TokenHandlerUtil;

/**
 * Unit tests for {@link TokenHandlerUtil}.
 */
public class TokenHandlerUtilTest {

	private static final String SECRET = "mySecretKeyForTestingJwtVerification123!";
	private static final String WRONG_SECRET = "wrongSecretKeyForTestingJwtVerification!";
	private static final String ISSUER = "https://auth.mosip.net";
	private static final String CLIENT_ID = "mosip-client";

	private final Algorithm algorithm = Algorithm.HMAC256(SECRET);
	private final Algorithm wrongAlgorithm = Algorithm.HMAC256(WRONG_SECRET);

	private String createToken(String issuer, String clientId, Date expiresAt, Algorithm alg) {
		return JWT.create()
				.withIssuer(issuer)
				.withClaim("clientId", clientId)
				.withExpiresAt(expiresAt)
				.sign(alg);
	}

	@Test
	public void validTokenWithCorrectSignatureReturnsTrue() {
		Date futureDate = new Date(System.currentTimeMillis() + 60_000); // 1 min in future
		String token = createToken(ISSUER, CLIENT_ID, futureDate, algorithm);

		boolean isValid = TokenHandlerUtil.isValidBearerToken(token, ISSUER, CLIENT_ID, algorithm);
		assertTrue("Valid token with valid signature must return true", isValid);
	}

	@Test
	public void tokenWithInvalidSignatureReturnsFalse() {
		Date futureDate = new Date(System.currentTimeMillis() + 60_000);
		// Signed with different secret
		String forgedToken = createToken(ISSUER, CLIENT_ID, futureDate, wrongAlgorithm);

		boolean isValid = TokenHandlerUtil.isValidBearerToken(forgedToken, ISSUER, CLIENT_ID, algorithm);
		assertFalse("Token with invalid signature must be rejected", isValid);
	}

	@Test
	public void expiredTokenReturnsFalse() {
		Date pastDate = new Date(System.currentTimeMillis() - 60_000); // 1 min in past
		String expiredToken = createToken(ISSUER, CLIENT_ID, pastDate, algorithm);

		boolean isValid = TokenHandlerUtil.isValidBearerToken(expiredToken, ISSUER, CLIENT_ID, algorithm);
		assertFalse("Expired token must be rejected", isValid);
	}

	@Test
	public void mismatchedIssuerReturnsFalse() {
		Date futureDate = new Date(System.currentTimeMillis() + 60_000);
		String token = createToken("https://evil-issuer.com", CLIENT_ID, futureDate, algorithm);

		boolean isValid = TokenHandlerUtil.isValidBearerToken(token, ISSUER, CLIENT_ID, algorithm);
		assertFalse("Token with mismatched issuer must be rejected", isValid);
	}

	@Test
	public void mismatchedClientIdReturnsFalse() {
		Date futureDate = new Date(System.currentTimeMillis() + 60_000);
		String token = createToken(ISSUER, "wrong-client", futureDate, algorithm);

		boolean isValid = TokenHandlerUtil.isValidBearerToken(token, ISSUER, CLIENT_ID, algorithm);
		assertFalse("Token with mismatched clientId must be rejected", isValid);
	}

	@Test
	public void missingClientIdClaimReturnsFalse() {
		Date futureDate = new Date(System.currentTimeMillis() + 60_000);
		String token = JWT.create()
				.withIssuer(ISSUER)
				.withExpiresAt(futureDate)
				.sign(algorithm);

		boolean isValid = TokenHandlerUtil.isValidBearerToken(token, ISSUER, CLIENT_ID, algorithm);
		assertFalse("Token without clientId claim must be rejected", isValid);
	}

	@Test
	public void nullOrEmptyInputsReturnFalse() {
		assertFalse(TokenHandlerUtil.isValidBearerToken(null, ISSUER, CLIENT_ID, algorithm));
		assertFalse(TokenHandlerUtil.isValidBearerToken("", ISSUER, CLIENT_ID, algorithm));
		assertFalse(TokenHandlerUtil.isValidBearerToken("some.token.value", ISSUER, CLIENT_ID, null));
	}

	@Test
	public void deprecatedMethodAlwaysReturnsFalse() {
		Date futureDate = new Date(System.currentTimeMillis() + 60_000);
		String token = createToken(ISSUER, CLIENT_ID, futureDate, algorithm);

		// Deprecated method without signature verification must always return false
		boolean isValid = TokenHandlerUtil.isValidBearerToken(token, ISSUER, CLIENT_ID);
		assertFalse("Deprecated method without signature verification must return false", isValid);
	}
}
