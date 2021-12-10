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
 * 
 * @author Srinivasan
 *
 */

public class TokenHandlerUtil {
	  private static Logger LOGGER= LoggerFactory.getLogger(TokenHandlerUtil.class);
	  
	  
	  public static ConcurrentMap<String,DecodedJWT> decodedTokens = new ConcurrentHashMap<>();
	  private TokenHandlerUtil() {
		  
	  }

	/**
	 * Validates the token offline based on the Oauth2 standards.
	 * 
	 * @param accessToken
	 *            - Bearer token
	 * @param issuerUrl
	 *            - issuer URL to be read from the properties,
	 * @param clientId
	 *            - client Id to be read from the properties
	 * @return Boolean
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
