package io.mosip.kernel.openid.bridge.api.utils;

import java.util.HashMap;
import java.util.Map;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;

/**
 * JWT helpers for the authorization-code proxy: decode a token and read the
 * OIDC {@code iss} claim (Keycloak realm issuer) without verifying the
 * signature.
 * <p>
 * Decoded tokens are cached in {@link #decodedJWTCache}. Prefer
 * {@link JWTUtils} when claim-by-name lookup is also required.
 */
public class AuthCodeProxyFlowUtils {
	
	/**
	 * Prevents instantiation of this utility class.
	 */
	private AuthCodeProxyFlowUtils() {
		
	}

	/**
	 * Cache of decoded JWTs keyed by compact token string, used by
	 * {@link #getissuer(String)}.
	 */
	static Map<String,DecodedJWT> decodedJWTCache = new HashMap<>();
	
	/**
	 * Returns the JWT {@code iss} (issuer) claim. Decodes and caches the token on
	 * first use.
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
}
