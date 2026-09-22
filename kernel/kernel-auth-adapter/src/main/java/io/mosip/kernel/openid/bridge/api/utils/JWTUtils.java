package io.mosip.kernel.openid.bridge.api.utils;

import java.util.HashMap;
import java.util.Map;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;

/**
 * Helpers for decoding Keycloak/OIDC JWTs with Auth0 {@link JWT#decode(String)}
 * (signature is not verified here).
 * <p>
 * {@link #getissuer(String)} caches decoded tokens so issuer lookup is cheap on
 * repeated logout/validate calls. {@link #getSubClaimValueFromToken(String, String)}
 * always decodes and does not use the cache.
 */
public class JWTUtils {
	
	/**
	 * Prevents instantiation of this utility class.
	 */
	private JWTUtils() {
		
	}

	/**
	 * Cache of decoded JWTs keyed by compact token string, used by
	 * {@link #getissuer(String)}.
	 */
	static Map<String,DecodedJWT> decodedJWTCache = new HashMap<>();
	
	/**
	 * Returns the JWT {@code iss} (issuer) claim, typically the Keycloak realm
	 * issuer URL. Decodes and caches the token on first use.
	 * <p>
	 * The method name retains the historical spelling {@code getissuer}.
	 *
	 * @param token compact JWT access or ID token
	 * @return issuer URL from the {@code iss} claim, or {@code null} if absent
	 */
	public static String getissuer(String token) {
		DecodedJWT decodedJWT  = null;
		if(decodedJWTCache.get(token)!=null)
			decodedJWT = decodedJWTCache.get(token);
		else{
			decodedJWT = JWT.decode(token);
			decodedJWTCache.put(token, decodedJWT);
		}	
		return decodedJWT.getClaim("iss").asString();
	}

	/**
	 * Returns the string value of an arbitrary JWT claim without using
	 * {@link #decodedJWTCache}.
	 *
	 * @param token        compact JWT
	 * @param propertyName claim name (for example the configured subject claim)
	 * @return claim value as a string, or {@code null} if the claim is absent
	 */
	public static String getSubClaimValueFromToken(String token, String propertyName) {
		return JWT.decode(token).getClaim(propertyName).asString();
	}
}
