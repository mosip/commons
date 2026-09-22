package io.mosip.kernel.authcodeflowproxy.api.utils;

import java.util.HashMap;
import java.util.Map;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;

/**
 * Helpers for decoding JWT access tokens used in the authorization-code flow proxy.
 * <p>
 * Caches {@link DecodedJWT} instances by raw token string so repeated issuer lookups
 * (for example when building Keycloak's end-session URL) do not re-parse the JWT.
 * This class is not instantiable.
 */
public class AuthCodeProxyFlowUtils {
	
	/**
	 * Prevents instantiation of this utility class.
	 */
	private AuthCodeProxyFlowUtils() {
		
	}

	/**
	 * In-memory cache of decoded JWTs keyed by the compact token string.
	 */
	static Map<String,DecodedJWT> decodedJWTCache = new HashMap<>();
	
	/**
	 * Returns the OpenID Connect {@code iss} (issuer) claim from {@code token}.
	 * <p>
	 * Uses {@link #decodedJWTCache} when the token was decoded previously; otherwise
	 * decodes with {@link JWT#decode(String)} and stores the result.
	 *
	 * @param token compact JWT access token
	 * @return issuer URL from the {@code iss} claim, or {@code null} if the claim is absent
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
