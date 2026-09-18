package io.mosip.kernel.core.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;

/**
 * Utility class for offline JWT (Bearer token) validation.
 *
 * <p><b>Security Note:</b> Always use the overload that accepts an
 * {@link Algorithm} parameter so that the JWT signature is
 * cryptographically verified before any claims are trusted.</p>
 *
 * @author Srinivasan
 * @since 1.0.0
 */
public class TokenHandlerUtil {

	private static final Logger LOGGER = LoggerFactory.getLogger(TokenHandlerUtil.class);

	private TokenHandlerUtil() {
		// Utility class — no instantiation
	}

	/**
	 * Validates a Bearer token by <b>cryptographically verifying</b> its
	 * signature, issuer, expiry, and {@code clientId} claim.
	 *
	 * <p>The {@link JWTVerifier} built from the supplied {@link Algorithm}
	 * automatically rejects tokens whose:
	 * <ul>
	 *   <li>signature does not match,</li>
	 *   <li>{@code exp} claim is in the past, or</li>
	 *   <li>{@code iss} claim does not equal {@code issuerUrl}.</li>
	 * </ul>
	 *
	 * @param accessToken the raw JWT string (without the "Bearer " prefix)
	 * @param issuerUrl   expected issuer ({@code iss}) claim value
	 * @param clientId    expected {@code clientId} claim value
	 * @param algorithm   the {@link Algorithm} used to verify the token
	 *                    signature (e.g. {@code Algorithm.RSA256(publicKey)})
	 * @return {@code true} only when signature, issuer, expiry, and clientId
	 *         are all valid; {@code false} otherwise
	 */
	public static boolean isValidBearerToken(String accessToken, String issuerUrl,
			String clientId, Algorithm algorithm) {

		if (accessToken == null || accessToken.isEmpty()) {
			LOGGER.error("JWT validation failed: accessToken is null or empty");
			return false;
		}
		if (algorithm == null) {
			LOGGER.error("JWT validation failed: Algorithm must not be null");
			return false;
		}

		try {
			// Build a verifier that checks signature + issuer + expiry automatically
			JWTVerifier verifier = JWT.require(algorithm)
					.withIssuer(issuerUrl)
					.build();

			DecodedJWT decodedJWT = verifier.verify(accessToken);

			if (decodedJWT.getExpiresAt() == null) {
				LOGGER.error("JWT validation failed: 'exp' claim is missing");
				return false;
			}

			// Validate the clientId claim
			Claim clientIdClaim = decodedJWT.getClaim("clientId");
			if (clientIdClaim.isNull() || clientIdClaim.asString() == null) {
				LOGGER.error("JWT validation failed: 'clientId' claim is missing");
				return false;
			}

			return clientId.equals(clientIdClaim.asString());

		} catch (JWTVerificationException e) {
			LOGGER.error("JWT verification failed: {}", e.getMessage());
			return false;
		} catch (Exception e) {
			LOGGER.error("Unexpected error during JWT validation: {}", e.getMessage());
			return false;
		}
	}

	/**
	 * Validates a Bearer token <b>without</b> signature verification.
	 *
	 * @deprecated This method does <b>NOT</b> verify the JWT signature and
	 *             therefore accepts forged tokens. Use
	 *             {@link #isValidBearerToken(String, String, String, Algorithm)}
	 *             instead. This overload now always returns {@code false} to
	 *             prevent silent security bypass.
	 *
	 * @param accessToken the raw JWT string
	 * @param issuerUrl   expected issuer
	 * @param clientId    expected clientId claim
	 * @return always {@code false}
	 */
	@Deprecated(since = "1.3.0", forRemoval = true)
	public static boolean isValidBearerToken(String accessToken, String issuerUrl, String clientId) {
		LOGGER.warn("SECURITY WARNING: isValidBearerToken(String, String, String) is deprecated "
				+ "because it does NOT verify the JWT signature. "
				+ "Migrate to isValidBearerToken(String, String, String, Algorithm). "
				+ "This method now always returns false.");
		return false;
	}
}
