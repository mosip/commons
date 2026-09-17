package io.mosip.kernel.core.util;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.auth0.jwt.JWT;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;

import io.mosip.kernel.core.exception.ExceptionUtils;

/**
 * Offline JWT bearer-token checks against issuer, client id, and expiry.
 * <p>
 * Contract: does not call an authorization server. Decode failures return
 * {@code false} rather than throwing. {@link #decodedTokens} is a process-wide
 * cache keyed by raw token string.
 * </p>
 *
 * @author Srinivasan
 */
public class TokenHandlerUtil {
	  private static Logger LOGGER= LoggerFactory.getLogger(TokenHandlerUtil.class);
	  
	  
	  /**
	   * Process-wide cache of decoded JWTs keyed by the raw access-token string.
	   */
	  public static ConcurrentMap<String,DecodedJWT> decodedTokens = new ConcurrentHashMap<>();
	  /**
	   * Prevents instantiation of this utility.
	   */
	  private TokenHandlerUtil() {
		  
	  }

	/**
	 * Returns whether {@code accessToken} matches {@code issuerUrl} and {@code clientId} and is not expired.
	 *
	 * @param accessToken never-null JWT compact serialization
	 * @param issuerUrl   never-null expected issuer claim
	 * @param clientId    never-null expected {@code clientId} claim
	 * @return {@code true} if issuer, client, and UTC expiry all match; {@code false} on mismatch or decode failure
	 */
	public static boolean isValidBearerToken(String accessToken, String issuerUrl, String clientId) {

		try {
			DecodedJWT decodedJWT = decodedTokens.get(accessToken);
			if(decodedJWT==null) {
				decodedJWT = JWT.decode(accessToken);
				decodedTokens.put(accessToken, decodedJWT);
			}
			Map<String, Claim> claims = decodedJWT.getClaims();
			LocalDateTime expiryTime = DateUtils
					.convertUTCToLocalDateTime(DateUtils.getUTCTimeFromDate(decodedJWT.getExpiresAt()));

			if (!decodedJWT.getIssuer().equals(issuerUrl)) {
				return false;
			} else if (!DateUtils.before(DateUtils.getUTCCurrentDateTime(), expiryTime)) {
				return false;
			} else if (!claims.get("clientId").asString().equals(clientId)) {
				return false;
			} else {
				return true;
			}
		} catch (JWTDecodeException e) {
			LOGGER.error("JWT DECODE EXCEPTION ::" .concat(e.getMessage()).concat(ExceptionUtils.getStackTrace(e)));
			return false;
		} catch (Exception e) {
			LOGGER.error(e.getMessage().concat(ExceptionUtils.getStackTrace(e)));
			return false;
		}

	}
  
}
